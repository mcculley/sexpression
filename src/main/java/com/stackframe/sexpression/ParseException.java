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
