package com.stackframe.sexpression;

/*-
 * #%L
 * S-expression
 * %%
 * Copyright (C) 2019 Gene McCulley
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A utility class for parsing S-expressions.
 */
public class SExpression {

    private SExpression() {
        // Inhibit construction of utility class.
    }

    private static boolean isInteger(CharSequence s) {
        int length = s.length();
        int i = 0;
        char c = s.charAt(i);
        if (c == '-') {
            i++;
        }

        for (; i < length; i++) {
            c = s.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isFloat(CharSequence s) {
        int length = s.length();
        int i = 0;
        char c = s.charAt(i);
        if (c == '-') {
            i++;
        }

        for (; i < length; i++) {
            c = s.charAt(i);
            if (!(Character.isDigit(c) || c == '.')) {
                return false;
            }
        }

        return true;
    }

    private static Object box(String s) {
        if (isInteger(s)) {
            BigInteger i = new BigInteger(s);
            if (i.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                return i;
            } else {
                return Long.valueOf(i.longValue());
            }
        } else if (isFloat(s)) {
            return Double.valueOf(s);
        } else {
            return s;
        }
    }

    private static Object parse(PushbackReader r, AtomicInteger line, AtomicInteger column, AtomicInteger offset,
                                boolean hasParent) throws IOException, ParseException {
        boolean quoted = false;
        boolean inLineComment = false;
        int blockCommentDepth = 0;
        List<Object> l = new ArrayList<>();
        StringBuilder atom = null;
        int i;
        while ((i = r.read()) != -1) {
            offset.incrementAndGet();
            column.incrementAndGet();
            char c = (char)i;
            if (c == '\n') {
                line.incrementAndGet();
                column.set(0);
                if (inLineComment) {
                    inLineComment = false;
                } else {
                    if (quoted) {
                        if (atom == null) {
                            atom = new StringBuilder();
                        }

                        atom.append(c);
                    } else {
                        if (atom != null) {
                            l.add(atom.toString());
                            atom = null;
                        }
                    }
                }

                continue;
            } else if (inLineComment) {
                continue;
            } else if (c == '(') {
                char next = (char)r.read();
                if (next == ';') {
                    blockCommentDepth++;
                    if (atom != null) {
                        l.add(atom.toString());
                        atom = null;
                    }

                    continue;
                } else {
                    r.unread(next);
                }
            } else if (c == ';') {
                char next = (char)r.read();
                if (next == ';') {
                    inLineComment = true;
                    if (atom != null) {
                        l.add(atom.toString());
                        atom = null;
                    }

                    continue;
                } else if (next == ')') {
                    blockCommentDepth--;
                    continue;
                } else {
                    r.unread(next);
                }
            } else if (blockCommentDepth > 0) {
                continue;
            }

            if (c == '(') {
                if (quoted) {
                    if (atom == null) {
                        atom = new StringBuilder();
                    }

                    atom.append(c);
                } else {
                    if (atom != null) {
                        l.add(atom.toString());
                        atom = null;
                    }

                    l.add(parse(r, line, column, offset, true));
                }
            } else if (c == ')') {
                if (quoted) {
                    if (atom == null) {
                        atom = new StringBuilder();
                    }

                    atom.append(c);
                } else {
                    if (!hasParent) {
                        throw new ParseException("unexpected )", offset.get() - 1, line.get(), column.get());
                    }

                    if (atom != null) {
                        try {
                            l.add(box(atom.toString()));
                        } catch (NumberFormatException e) {
                            throw new ParseException(String.format("could not parse number '%s'", atom), offset.get() - 1,
                                                     line.get(),
                                                     column.get());
                        }
                    }

                    if (l.isEmpty()) {
                        return Collections.emptyList();
                    } else {
                        return Collections.unmodifiableList(l);
                    }
                }
            } else if (c == ' ' || c == '\t') {
                if (quoted) {
                    if (atom == null) {
                        atom = new StringBuilder();
                    }

                    atom.append(c);
                } else {
                    if (atom != null) {
                        l.add(atom.toString());
                        atom = null;
                    }
                }
            } else if (c == '\"') {
                quoted = !quoted;
            } else if (c != '\r') { // Ignore carriage return. We assume we will get a \n right after for DOS files.
                if (atom == null) {
                    atom = new StringBuilder();
                }

                atom.append(c);
            }
        }

        if (l.isEmpty()) {
            if (atom == null) {
                return Collections.emptyList();
            } else {
                return box(atom.toString());
            }
        } else {
            return Collections.unmodifiableList(l);
        }
    }

    /**
     * Convert an Object into a human readable S-expression.
     *
     * @param e the Object to convert
     * @return a human readable S-expression
     */
    public static CharSequence toCharSequence(Object e) {
        StringBuilder b = new StringBuilder();
        if (e instanceof String) {
            if (((String)e).contains(" ")) {
                b.append("\"");
                b.append(e);
                b.append("\"");
            } else {
                b.append(e);
            }
        } else if (e instanceof Long || e instanceof Double || e instanceof BigInteger) {
            b.append(e);
        } else if (e instanceof List) {
            b.append('(');
            b.append(String.join(" ", ((List<?>)e).stream().map(SExpression::toCharSequence)::iterator));
            b.append(')');
        } else {
            throw new IllegalArgumentException("unexpected type in S-expression: " + e.getClass().getName());
        }

        return b;
    }

    /**
     * Parse a stream into a list of S-expressions.
     *
     * @param r a PushbackReader to read from
     * @return an Object or a List of parsed S-expressions. If the string is a single atom, it will be returned as a String,
     * BigInteger, Long, or Double. For each parsed S-expression, the Object will be a String, Long, Double, or List of such, recursively.
     * The sequence ';;' marks a comment until the end of line. The sequences, '(;' and ';)' indicate block comments, which can be nested.
     * @throws ParseException if the String does not represent a legal S-expression
     */
    public static Object parse(PushbackReader r) throws ParseException, IOException {
        return parse(r, new AtomicInteger(1), new AtomicInteger(), new AtomicInteger(), false);
    }

    /**
     * Parse a stream into a list of S-expressions.
     *
     * @param r a Reader to read from
     * @return an Object or a List of parsed S-expressions. If the string is a single atom, it will be returned as a String,
     * BigInteger, Long, or Double. For each parsed S-expression, the Object will be a String, Long, Double, or List of such, recursively.
     * The sequence ';;' marks a comment until the end of line. The sequences, '(;' and ';)' indicate block comments, which can be nested.
     * @throws ParseException if the String does not represent a legal S-expression
     */
    public static Object parse(Reader r) throws ParseException, IOException {
        return parse(new PushbackReader(r), new AtomicInteger(1), new AtomicInteger(), new AtomicInteger(), false);
    }

    /**
     * Parse a String into a list of S-expressions.
     *
     * @param s the String to parse
     * @return an Object or a List of parsed S-expressions. If the string is a single atom, it will be returned as a String,
     * BigInteger, Long, or Double. For each parsed S-expression, the Object will be a String, Long, Double, or List of such, recursively.
     * The sequence ';;' marks a comment until the end of line. The sequences, '(;' and ';)' indicate block comments, which can be nested.
     * @throws ParseException if the String does not represent a legal S-expression
     */
    public static Object parse(String s) throws ParseException {
        try {
            return parse(new StringReader(s));
        } catch (IOException e) {
            // We cannot get an IOException when reading from String.
            throw new AssertionError(e);
        }
    }

    /**
     * A simple command line utility to read in files containing S-expressions, parse them, and print them out again for testing.
     *
     * @param args the command line arguments
     * @throws Exception if anything goes wrong with reading
     */
    @SuppressWarnings("squid:S106") // Ignore Sonar warning about use of System.out and System.err.
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("expected filenames");
            System.exit(-1);
        }

        for (String arg : args) {
            try {
                try (FileReader r = new FileReader(arg)) {
                    System.out.println(toCharSequence(parse(r)));
                }
            } catch (ParseException e) {
                System.err.println(arg + ":" + e);
                System.exit(-1);
            }
        }
    }

}
