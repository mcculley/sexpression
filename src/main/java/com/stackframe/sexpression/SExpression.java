package com.stackframe.sexpression;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.text.ParseException;
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

    private static Object parse(PushbackReader r, AtomicInteger offset, List<Object> parent) throws IOException, ParseException {
        boolean quoted = false;
        List<Object> currentList = null;
        StringBuilder currentAtom = new StringBuilder();
        int i;
        while ((i = r.read()) != -1) {
            offset.incrementAndGet();
            char c = (char)i;
            if (c == '(') {
                if (currentAtom.length() > 0) {
                    throw new ParseException("unexpected ( inside token starting with " + currentAtom, offset.get() - 1);
                }

                if (currentList == null) {
                    currentList = new ArrayList<>();
                }

                currentList.add(parse(r, offset, currentList));
                r.read();
                offset.incrementAndGet();
            } else if (c == ')') {
                if (parent == null) {
                    throw new ParseException("unexpected )", offset.get() - 1);
                }

                if (currentAtom.length() > 0) {
                    if (currentList == null) {
                        currentList = new ArrayList<>();
                    }

                    currentList.add(currentAtom.toString());
                }

                r.unread(c);
                offset.decrementAndGet();
                if (currentList == null) {
                    return Collections.emptyList();
                } else {
                    return Collections.unmodifiableList(currentList);
                }
            } else if (c == ' ' || c == '\n') {
                if (quoted) {
                    currentAtom.append(c);
                } else {
                    if (currentAtom.length() > 0) {
                        if (currentList == null) {
                            currentList = new ArrayList<>();
                        }

                        currentList.add(currentAtom.toString());
                        currentAtom = new StringBuilder();
                    }
                }
            } else if (c == '\r') {
                // Ignore. We assume we will get a \n right after for DOS files.
            } else if (c == '\"') {
                quoted = !quoted;
            } else {
                currentAtom.append(c);
            }
        }

        if (currentList == null) {
            return currentAtom.toString();
        } else {
            return Collections.unmodifiableList(currentList);
        }
    }

    /**
     * Convert an object into a human readable S-expression.
     *
     * @param e the object to convert
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
     * Parse a String into a list of S-expressions.
     *
     * @param s the String to parse
     * @return an Object or a list of parsed S-expressions. If the string is a single atom, it will be returned as an Object.
     * @throws ParseException if the String does not represent a legal S-expression
     */
    public static Object parse(String s) throws ParseException {
        try {
            Object parsed = parse(new PushbackReader(new StringReader(s)), new AtomicInteger(), null);
            System.out.printf("source='%s' parsed='%s'\n", s, SExpression.toCharSequence(parsed));
            return parsed;
        } catch (IOException e) {
            // We cannot get an IOException when reading from String.
            throw new AssertionError(e);
        }
    }

}
