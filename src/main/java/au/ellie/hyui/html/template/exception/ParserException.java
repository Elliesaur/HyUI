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

public class ParserException extends RuntimeException {

    /**
     * The token in cause at the time of the exception
     */
    public final Token token;

    /**
     * The index of the token in the original template string
     */
    public final int index;

    /**
     * The source template text (optional)
     */
    private final String source;

    /**
     * Create a parser exception with position information
     */
    public ParserException(String message, Token token, int index) {
        this(message, token, index, null);
    }

    /**
     * Create a parser exception with position information and source text
     */
    public ParserException(String message, Token token, int index, String source) {
        super(formatMessage(message, token, index, source));
        this.token = token;
        this.index = index;
        this.source = source;
    }

    /**
     * Format the exception message with line/column information and source preview
     */
    private static String formatMessage(String message, Token token, int index, String source) {
        if (source == null || token == null) {
            return String.format("%s at position %d (token: %s)", message, index, token);
        }

        int position = token.position();
        var lineCol = getLineAndColumn(source, position);
        int line = lineCol[0];
        int col = lineCol[1];

        // Get the line content and create a caret pointer
        String lineContent = getLine(source, position);
        String caret = " ".repeat(Math.max(0, col - 1)) + "^";

        return String.format(
            "%s at line %d, column %d\n" +
            "  %s\n" +
            "  %s",
            message, line, col, lineContent, caret
        );
    }

    /**
     * Calculate line and column number from character position
     * @return array with [line, column] (1-based)
     */
    private static int[] getLineAndColumn(String source, int position) {
        int line = 1;
        int col = 1;

        for (int i = 0; i < position && i < source.length(); i++) {
            if (source.charAt(i) == '\n') {
                line++;
                col = 1;
            } else {
                col++;
            }
        }

        return new int[]{line, col};
    }

    /**
     * Extract the line containing the given position
     */
    private static String getLine(String source, int position) {
        if (position < 0 || position >= source.length()) {
            return "";
        }

        // Find start of line
        int start = position;
        while (start > 0 && source.charAt(start - 1) != '\n') {
            start--;
        }

        // Find end of line
        int end = position;
        while (end < source.length() && source.charAt(end) != '\n') {
            end++;
        }

        // Extract line and replace tabs with spaces for better display
        return source.substring(start, end).replace('\t', ' ');
    }
}
