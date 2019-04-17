package com.stackframe.sexpression;

/**
 * Signals that an error has been reached unexpectedly while parsing.
 */
public class ParseException extends java.text.ParseException {

    private final int errorLine, errorColumn;

    /**
     * Constructs a ParseException with the specified detail message, line, column, and offset. A detail message is a String that
     * describes this particular exception.
     *
     * @param s           the detail message
     * @param errorOffset the position where the error is found while parsing.
     * @param errorLine   the line where the error is found while parsing.
     * @param errorColumn the column where the error is found while parsing.
     */
    public ParseException(String s, int errorOffset, int errorLine, int errorColumn) {
        super(s, errorOffset);
        this.errorLine = errorLine;
        this.errorColumn = errorColumn;
    }

    public int getErrorLine() {
        return errorLine;
    }

    public int getErrorColumn() {
        return errorColumn;
    }

}
