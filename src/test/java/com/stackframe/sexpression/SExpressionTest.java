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


import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for SExpression.
 */
public class SExpressionTest {

    @Test
    public void testSingleAtom() throws ParseException {
        assertEquals("foo", SExpression.parse("foo"));
    }

    @Test
    public void testSingleAtomList() throws ParseException {
        assertEquals(Collections.singletonList(Collections.singletonList("foo")), SExpression.parse("(foo)"));
    }

    @Test
    public void testEmptyString() throws ParseException {
        assertEquals(Collections.emptyList(), SExpression.parse(""));
    }

    @Test
    public void testEmptyList() throws ParseException {
        assertEquals(Collections.singletonList(Collections.emptyList()), SExpression.parse("()"));
    }

    @Test
    public void testTwoSingleAtomLists() throws ParseException {
        assertEquals(Arrays.asList(Collections.singletonList("foo"), Collections.singletonList("bar")),
                     SExpression.parse("(foo) (bar)"));
    }

    @Test
    public void testTwoAtomsInOneList() throws ParseException {
        assertEquals(Collections.singletonList(Arrays.asList("foo", "bar")), SExpression.parse("(foo bar)"));
    }

    @Test
    public void testInteger() throws ParseException {
        assertEquals(Collections.singletonList(Arrays.asList("foo", new Long(12))), SExpression.parse("(foo 12)"));
    }

    @Test
    public void testInteger2() throws ParseException {
        assertEquals("12", SExpression.toCharSequence(SExpression.parse("12")).toString());
    }

    @Test
    public void testNegativeInteger() throws ParseException {
        assertEquals(Collections.singletonList(Arrays.asList("foo", new Long(-12))), SExpression.parse("(foo -12)"));
    }

    @Test
    public void testFloat() throws ParseException {
        assertEquals("12.0", SExpression.toCharSequence(SExpression.parse("12.0")).toString());
    }

    @Test
    public void testFloat2() throws ParseException {
        assertEquals(Collections.singletonList(Arrays.asList("foo", new Double(12.5))), SExpression.parse("(foo 12.5)"));
    }

    @Test
    public void testNegativeFloat() throws ParseException {
        assertEquals(Collections.singletonList(Arrays.asList("foo", new Double(-12.5))), SExpression.parse("(foo -12.5)"));
    }

    @Test
    public void testQuotedString() throws ParseException {
        assertEquals("pachyderms (elephants)", SExpression.parse("\"pachyderms (elephants)\""));
    }

    @Test
    public void testTabs() throws ParseException {
        Object parsed = SExpression.parse("  (func (export \"i32_load8_s\") (param $i i32) (result i32)\n" +
                                          "\t(i32.store8 (i32.const 8) (local.get $i))\n" +
                                          "\t(i32.load8_s (i32.const 8))\n" +
                                          "  )\n");
        Object expected = Arrays.asList(
                Arrays.asList("func", Arrays.asList("export", "i32_load8_s"), Arrays.asList("param", "$i", "i32"),
                              Arrays.asList("result", "i32"),
                              Arrays.asList("i32.store8", Collections
                                                    .unmodifiableList(Arrays.asList("i32.const", Long.valueOf(8))),
                                            Arrays.asList("local.get", "$i")),
                              Arrays.asList("i32.load8_s", Arrays.asList("i32.const", Long.valueOf(8)))));
        assertEquals(expected, parsed);
    }

    @Test
    public void testBadToken() {
        ParseException e = assertThrows(ParseException.class, () -> SExpression.parse("(foo bar baz() buzz)"));
        assertEquals("unexpected ( inside token starting with \"baz\"", e.getMessage());
        assertEquals(12, e.getErrorOffset());
    }

    @Test
    public void testNestedLists() throws ParseException {
        assertEquals(Collections.singletonList(Arrays.asList("foo", "bar", Collections.singletonList("baz"))),
                     SExpression.parse("(foo bar (baz))"));
    }

    @Test
    public void testExtraClose() {
        ParseException e = assertThrows(ParseException.class, () -> SExpression.parse("(foo bar (baz)))"));
        assertEquals("unexpected )", e.getMessage());
        assertEquals(15, e.getErrorOffset());
    }

    @Test
    public void testExtraClose2() {
        ParseException e = assertThrows(ParseException.class, () -> SExpression.parse("(foo bar (baz)) buzz)"));
        assertEquals("unexpected )", e.getMessage());
        assertEquals(20, e.getErrorOffset());
        assertEquals(1, e.getErrorLine());
        assertEquals(21, e.getErrorColumn());
    }

    @Test
    public void testExtraClose3() {
        ParseException e = assertThrows(ParseException.class, () -> SExpression.parse("(foo bar (baz) ) buzz)"));
        assertEquals("unexpected )", e.getMessage());
        assertEquals(21, e.getErrorOffset());
        assertEquals(1, e.getErrorLine());
        assertEquals(22, e.getErrorColumn());
    }

    @Test
    public void testExtraClose4() {
        ParseException e = assertThrows(ParseException.class, () -> SExpression.parse("(foo \nbar \n(baz) ) buzz)"));
        assertEquals("unexpected )", e.getMessage());
        assertEquals(23, e.getErrorOffset());
        assertEquals(3, e.getErrorLine());
        assertEquals(13, e.getErrorColumn());
    }

    @Test
    public void testUnquoted() throws ParseException {
        assertEquals(Collections.singletonList(Arrays.asList("foo", "bar", "baz", "buzz", "fuzz")),
                     SExpression.parse("(foo bar baz buzz fuzz)"));
    }

    @Test
    public void testQuoted() throws ParseException {
        String source = "(foo bar \"baz buzz\" fuzz)";
        Object parsed = SExpression.parse(source);
        assertEquals(Collections.singletonList(Arrays.asList("foo", "bar", "baz buzz", "fuzz")), parsed);
        assertEquals(source, SExpression.toCharSequence(((List)parsed).get(0)).toString());
    }

    @Test
    public void testQuoted2() throws ParseException {
        String source = "(foo bar \" baz buzz\" fuzz)";
        Object parsed = SExpression.parse(source);
        assertEquals(Collections.singletonList(Arrays.asList("foo", "bar", " baz buzz", "fuzz")), parsed);
        assertEquals(source, SExpression.toCharSequence(((List)parsed).get(0)).toString());
    }

    @Test
    public void testBadObject() {
        assertThrows(IllegalArgumentException.class, () -> SExpression.toCharSequence(Class.class));
    }

    @Test
    public void testNewLine() throws ParseException {
        assertEquals(Collections.singletonList(Arrays.asList("foo", "bar", "baz", "buzz", "fuzz")),
                     SExpression.parse("(foo bar baz\nbuzz fuzz)"));
    }

    @Test
    public void testDOSNewLine() throws ParseException {
        assertEquals(Collections.singletonList(Arrays.asList("foo", "bar", "baz", "buzz", "fuzz")),
                     SExpression.parse("(foo bar baz\r\nbuzz fuzz)"));
    }

    // FIXME: Need to handle escape with '\' (or some other character).
    // FIXME: Need to handle an escaped '"'.
    // FIXME: Need to handle an escaped '('.
    // FIXME: Need to handle an escaped ')'.
    // FIXME: Need to handle an escaped ' '.
    // FIXME: Need to keep track of line and column numbers.
    // FIXME: Need pretty printer.
    // FIXME: Need to return integers as Integer or Long objects.
    // FIXME: Need to return floats as Double objects.
    // FIXME: Any value in returning Unicode pi value as a special object?
    // FIXME: Any value in returning fractional numbers as some special object like Scheme's numeric tower does?
    // FIXME: Handle reading hexadecimal literals.
    // FIXME: Need to support comment until end of line character.
    // FIXME: Need to support block comment scheme.
    // See https://github.com/WebAssembly/spec/blob/master/interpreter/README.md#s-expression-syntax for grammar
    // FIXME: Need to throw parse exception if reading any weird control character.
    // FIXME: Handle Unicode characters for atoms and strings.
    // FIXME: Handle scientific notation?

}
