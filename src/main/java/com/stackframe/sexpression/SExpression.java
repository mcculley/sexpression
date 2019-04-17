package com.stackframe.sexpression;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
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
                    l.add(atom.toString());
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
                    atom.append(c);
                } else {
                    if (atom != null) {
                        l.add(atom.toString());
                        atom = null;
                    }
                }
            } else if (c == '\r') {
                // Ignore. We assume we will get a \n right after for DOS files.
            } else if (c == '\"') {
                quoted = !quoted;
            } else {
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
                return atom.toString();
            }
        } else {
            return Collections.unmodifiableList(l);
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
     * @return an Object or a list of parsed S-expressions. If the string is a single atom, it will be returned as an Object. For
     * each parsed S-expression, the Object will either be a String or a List, recursively.
     * @throws ParseException if the String does not represent a legal S-expression
     */
    public static Object parse(String s) throws ParseException {
        try {
            return parse(new StringReader(s), new AtomicInteger(1), new AtomicInteger(), new AtomicInteger(), false);
        } catch (IOException e) {
            // We cannot get an IOException when reading from String.
            throw new AssertionError(e);
        }
    }

}
