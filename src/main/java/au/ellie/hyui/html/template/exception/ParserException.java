/*
 *     Copyright (C) 2026 EllieAU
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package au.ellie.hyui.html.template.exception;

import au.ellie.hyui.html.template.item.Token;

import javax.annotation.Nonnull;

public class ParserException extends RuntimeException {

    /**
     * The token in cause at the time of the exception
     */
    public final Token token;

    /**
     * The source template text (optional)
     */
    public final String source;

    /**
     * Create a parser exception with position information
     *
     * @param message The error message
     * @param token   The token where the error occurred
     */
    public ParserException(String message, @Nonnull Token token) {
        this(message, token, null);
    }

    /**
     * Create a parser exception with position information and source text
     *
     * @param message The error message
     * @param token   The token where the error occurred
     * @param source  The original template source text (used for line/column info)
     */
    public ParserException(String message, Token token, String source) {
        super(formatMessage(message, token, source));
        this.token = token;
        this.source = source;
    }

    /**
     * Format the exception message. If source is provided, include line
     * and column information with a caret pointing to the error location
     *
     * @param message The error message
     * @param token   The token where the error occurred
     * @param source  The original template source text (optional)
     */
    private static String formatMessage(String message, Token token, String source) {
        var position = token.position();
        if (source == null)
            return String.format("%s at position %d (token: %s)", message, position, token);

        var lineCol = getLineAndColumn(source, position);
        var line = lineCol[0];
        var col = lineCol[1];

        // Get the line content and create a caret pointer
        var lineContent = getLine(source, position);
        var caret = " ".repeat(Math.max(0, col - 1)) + "^";

        return String.format("""
                        %s at line %d, column %d
                          %s
                          %s
                        """,
                message, line, col, lineContent, caret
        );
    }

    /**
     * Calculate line and column number from character position
     *
     * @param source   The original template source text
     * @param position The character index in the source text (0-based)
     * @return array with [line, column] (1-based)
     */
    private static int[] getLineAndColumn(String source, int position) {
        var line = 1;
        var col = 1;

        for (var i = 0; i < position && i < source.length(); i++) {
            if (source.charAt(i) == '\n') {
                line++;
                col = 1;
            } else
                col++;
        }

        return new int[]{line, col};
    }

    /**
     * Extract the line containing the given position
     *
     * @param source   The original template source text
     * @param position The character index in the source text (0-based)
     */
    private static String getLine(String source, int position) {
        if (position < 0 || position >= source.length())
            return "";

        // Find start of line
        var start = position;
        while (start > 0 && source.charAt(start - 1) != '\n')
            start--;

        // Find end of line
        var end = position;
        while (end < source.length() && source.charAt(end) != '\n')
            end++;

        return source.substring(start, end)
                .replace('\t', ' ');
    }
}
