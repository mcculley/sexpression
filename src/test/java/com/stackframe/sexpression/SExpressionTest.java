package com.stackframe.sexpression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    public void testBadToken() {
        ParseException e = assertThrows(ParseException.class, () -> SExpression.parse("(foo bar baz() buzz)"));
        assertEquals("unexpected ( inside token starting with baz", e.getMessage());
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

}
