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
            return new Long(s);
        } else if (isFloat(s)) {
            return new Double(s);
        } else {
            return s;
        }
    }

    private static Object parse(Reader r, AtomicInteger line, AtomicInteger column, AtomicInteger offset,
                                boolean hasParent) throws IOException, ParseException {
        boolean quoted = false;
        List<Object> l = new ArrayList<>();
        StringBuilder atom = null;
        int i;
        while ((i = r.read()) != -1) {
            offset.incrementAndGet();
            column.incrementAndGet();
            char c = (char)i;
            if (c == '(') {
                if (atom != null) {
                    throw new ParseException("unexpected ( inside token starting with " + atom, offset.get() - 1, line.get(),
                                             column.get());
                }

                l.add(parse(r, line, column, offset, true));
            } else if (c == ')') {
                if (!hasParent) {
                    throw new ParseException("unexpected )", offset.get() - 1, line.get(), column.get());
                }

                if (atom != null) {
                    l.add(box(atom.toString()));
                }

                if (l.isEmpty()) {
                    return Collections.emptyList();
                } else {
                    return Collections.unmodifiableList(l);
                }
            } else if (c == ' ' || c == '\n') {
                if (c == '\n') {
                    line.incrementAndGet();
                    column.set(0);
                }

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
        } else if (e instanceof Long || e instanceof Double) {
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
     * @param r a Reader to read from
     * @return an Object or a List of parsed S-expressions. If the string is a single atom, it will be returned as a String,
     * Long, or Double. For each parsed S-expression, the Object will be a String, Long, Double, or List of such, recursively.
     * @throws ParseException if the String does not represent a legal S-expression
     */
    public static Object parse(Reader r) throws ParseException, IOException {
        return parse(r, new AtomicInteger(1), new AtomicInteger(), new AtomicInteger(), false);
    }

    /**
     * Parse a String into a list of S-expressions.
     *
     * @param s the String to parse
     * @return an Object or a List of parsed S-expressions. If the string is a single atom, it will be returned as a String,
     * Long, or Double. For each parsed S-expression, the Object will be a String, Long, Double, or List of such, recursively.
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
     * A simple command line utility to read in a file containing S-expressions, parse them, and print them out again for testing.
     *
     * @param args the command line arguments
     * @throws Exception if anything goes wrong with reading or parsing
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("expected a single argument with a filename");
            System.exit(-1);
        }

        System.out.println(toCharSequence(parse(new FileReader(args[0]))));
    }

}
