package com.stackframe.sexpression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Arrays;
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
        assertEquals(Arrays.asList(Arrays.asList("foo")), SExpression.parse("(foo)"));
    }

    @Test
    public void testTwoSingleAtomLists() throws ParseException {
        assertEquals(Arrays.asList(Arrays.asList("foo"), Arrays.asList("bar")), SExpression.parse("(foo) (bar)"));
    }

    @Test
    public void testTwoAtomsInOneList() throws ParseException {
        assertEquals(Arrays.asList(Arrays.asList("foo", "bar")), SExpression.parse("(foo bar)"));
    }

    @Test
    public void testBadToken() {
        ParseException e = assertThrows(ParseException.class, () -> SExpression.parse("(foo bar baz() buzz)"));
        assertEquals("unexpected ( inside token starting with baz", e.getMessage());
        assertEquals(12, e.getErrorOffset());
    }

    @Test
    public void testNestedLists() throws ParseException {
        assertEquals(Arrays.asList(Arrays.asList("foo", "bar", Arrays.asList("baz"))), SExpression.parse("(foo bar (baz))"));
    }

    @Test
    public void testExtraClose() {
        ParseException e = assertThrows(ParseException.class, () -> SExpression.parse("(foo bar (baz)))"));
        assertEquals("unexpected )", e.getMessage());
        assertEquals(15, e.getErrorOffset());
    }

    @Test
    public void testUnquoted() throws ParseException {
        assertEquals(Arrays.asList(Arrays.asList("foo", "bar", "baz", "buzz", "fuzz")),
                     SExpression.parse("(foo bar baz buzz fuzz)"));
    }

    @Test
    public void testQuoted() throws ParseException {
        String source = "(foo bar \"baz buzz\" fuzz)";
        Object parsed = SExpression.parse(source);
        assertEquals(Arrays.asList(Arrays.asList("foo", "bar", "baz buzz", "fuzz")), parsed);
        assertEquals(source, SExpression.toString(((List)parsed).get(0)));
    }

    @Test
    public void testNewLine() throws ParseException {
        assertEquals(Arrays.asList(Arrays.asList("foo", "bar", "baz", "buzz", "fuzz")),
                     SExpression.parse("(foo bar baz\nbuzz fuzz)"));
    }

    @Test
    public void testDOSNewLine() throws ParseException {
        assertEquals(Arrays.asList(Arrays.asList("foo", "bar", "baz", "buzz", "fuzz")),
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

}
