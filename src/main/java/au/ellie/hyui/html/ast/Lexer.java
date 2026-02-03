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

package au.ellie.hyui.html.ast;

import au.ellie.hyui.html.ast.item.Token;

import java.util.ArrayList;
import java.util.List;

import static au.ellie.hyui.html.ast.item.Token.Type.*;

public class Lexer {
    private final String input;
    private int line = 1;
    private int pos = 0;

    public Lexer(String input) {
        this.input = input;
    }

    /**
     * Tokenize the input string into a list of tokens
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < input.length()) {
            if (peek(EXPR_OPEN))
                tokenizeMustache(tokens);
            else
                tokenizeText(tokens);
        }

        tokens.add(new Token(GLOBAL_EOF, pos));

        return tokens;
    }

    /**
     * Tokenize mustache-style expressions: `{{ ... }}`, `{{# ... }}` or `{{/ ... }}`
     *
     * @param tokens The list to add tokens to
     */
    private void tokenizeMustache(List<Token> tokens) {
        boolean clean = false;

        if (peek(BLOCK_START)) {
            clean = true;

            trimWhitespaceForBlock(tokens);
            tokens.add(new Token(BLOCK_START, pos));
            advance(BLOCK_START);
        } else if (peek(BLOCK_END)) {
            clean = true;

            trimWhitespaceForBlock(tokens);
            tokens.add(new Token(BLOCK_END, pos));
            advance(BLOCK_END);
        } else {
            tokens.add(new Token(EXPR_OPEN, pos));
            advance(EXPR_OPEN);
        }

        skipWhitespace();
        tokenizeExpression(tokens);

        if (clean)
            skipBlockLineEnd();
    }

    /**
     * Tokenize an expression until the closing "}}"
     *
     * @param tokens The list to add tokens to
     */
    private void tokenizeExpression(List<Token> tokens) {
        skipWhitespace();

        while (pos < input.length()) {
            if (peek(EXPR_CLOSE))
                break;

            var current = current();

            // String
            if (EXPR_STRING.match(current)) {
                tokens.add(tokenizeString());
            }

            // Variable
            else if (EXPR_VARIABLE.match(current)) {
                tokens.add(tokenizeVariable());
            }

            // Numbers
            else if (Character.isDigit(current) ||
                    (current == '-' &&
                            pos + 1 < input.length() &&
                            Character.isDigit(input.charAt(pos + 1))
                    )
            ) {
                tokens.add(tokenizeNumber());
            }

            // Keyword / Operator
            else if (peek(COMP_EQUALS)) {
                tokens.add(new Token(COMP_EQUALS, pos));
                advance(COMP_EQUALS);
            } else if (peek(COMP_NOT_EQUALS)) {
                tokens.add(new Token(COMP_NOT_EQUALS, pos));
                advance(COMP_NOT_EQUALS);
            } else if (peek(COMP_LESS_EQUALS)) {
                tokens.add(new Token(COMP_LESS_EQUALS, pos));
                advance(COMP_LESS_EQUALS);
            } else if (peek(COMP_GREATER_EQUALS)) {
                tokens.add(new Token(COMP_GREATER_EQUALS, pos));
                advance(COMP_GREATER_EQUALS);
            } else if (peek(COMP_LESS_THAN)) {
                tokens.add(new Token(COMP_LESS_THAN, pos));
                advance(COMP_LESS_THAN);
            } else if (peek(COMP_GREATER_THAN)) {
                tokens.add(new Token(COMP_GREATER_THAN, pos));
                advance(COMP_GREATER_THAN);
            } else if (peek(COMP_AND)) {
                tokens.add(new Token(COMP_AND, pos));
                advance(COMP_AND);
            } else if (peek(EXPR_NULL_COALESCING)) {
                tokens.add(new Token(EXPR_NULL_COALESCING, pos));
                advance(EXPR_NULL_COALESCING);
            } else if (peek(COMP_OR)) {
                tokens.add(new Token(COMP_OR, pos));
                advance(COMP_OR);
            } else if (peek(EXPR_PIPE)) {
                tokens.add(new Token(EXPR_PIPE, pos));
                advance(EXPR_PIPE);
            } else if (peek(EXPR_VARIABLE_DOT)) {
                tokens.add(new Token(EXPR_VARIABLE_DOT, pos));
                advance(EXPR_VARIABLE_DOT);
            }

            // Identifiers
            else if (Character.isLetter(current))
                tokens.add(tokenizeIdentifier());

            else
                throwError("Unexpected character: " + current(), pos);

            skipWhitespace();
        }

        if (peek(EXPR_CLOSE)) {
            tokens.add(new Token(EXPR_CLOSE, pos));
            advance(2);
        }
    }

    /**
     * Tokenize a string literal
     */
    private Token tokenizeString() {
        int start = pos;
        advance(EXPR_STRING);

        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && current() != '"') {
            if (current() == '\\' && pos + 1 < input.length()) {
                advance();
                char escaped = current();
                switch (escaped) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    default -> sb.append(escaped);
                }
                advance();
            } else {
                sb.append(current());
                advance();
            }
        }

        if (current() != '"')
            throwError("Unterminated string", start);

        advance(EXPR_STRING);

        return new Token(EXPR_STRING, sb.toString(), start);
    }

    /**
     * Tokenize a variable (starts with $)
     */
    private Token tokenizeVariable() {
        int start = pos;
        advance(EXPR_VARIABLE); // Skip $
        StringBuilder sb = new StringBuilder();

        while (pos < input.length() && (Character.isLetterOrDigit(current()) || current() == '_' || current() == '-')) {
            sb.append(current());
            advance();
        }

        return new Token(EXPR_VARIABLE, sb.toString(), start);
    }

    /**
     * Tokenize a number (integer or decimal)
     */
    private Token tokenizeNumber() {
        StringBuilder sb = new StringBuilder();
        if (current() == '-') {
            sb.append(current());
            advance();
        }

        // Must have at least one digit after the sign
        int start = pos;
        if (!Character.isDigit(current())) {
            pos = start;

            throwError("Expected digit after '-'", pos);
        }

        boolean hasDecimal = false;
        while (pos < input.length() && (Character.isDigit(current()) || current() == '.')) {
            if (current() == '.') {
                if (hasDecimal)
                    break;

                hasDecimal = true;
            }

            sb.append(current());
            advance();
        }

        return new Token(EXPR_NUMBER, sb.toString(), start);
    }

    /**
     * Tokenize an identifier or keyword
     */
    private Token tokenizeIdentifier() {
        int start = pos;

        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && (Character.isLetterOrDigit(current()) || current() == '_' || current() == '-')) {
            sb.append(current());
            advance();
        }

        String value = sb.toString();
        Token.Type type = switch (value) {
            case "if" -> BLOCK_IF;
            case "else" -> BLOCK_ELSE;
            case "each" -> BLOCK_EACH;
            case "true", "false" -> EXPR_BOOLEAN;
            case "in" -> COMP_IN;
            default -> EXPR_IDENTIFIER;
        };

        return new Token(type, value, start);
    }

    /**
     * Tokenize plain text until the next "{{"
     *
     * @param tokens The list to add tokens to
     */
    private void tokenizeText(List<Token> tokens) {
        int start = pos;

        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && !peek(EXPR_OPEN)) {
            sb.append(current());
            advance();
        }

        if (!sb.isEmpty())
            tokens.add(new Token(GLOBAL_TEXT, sb.toString(), start));
    }

    // ===== Helpers =====

    /**
     * Returns the current character or '\0' if at the end of input
     */
    private char current() {
        return pos < input.length() ? input.charAt(pos) : '\0';
    }

    /**
     * Peeks ahead to see if the next characters match the given string
     *
     * @param str The string(s) to match
     */
    private boolean peek(String... str) {
        for (var s : str)
            if (input.startsWith(s, pos))
                return true;

        return false;
    }

    /**
     * Peeks ahead to see if the next characters match the given string
     *
     * @param types The type of token(s) to match
     */
    private boolean peek(Token.Type... types) {
        for (var type : types) {
            var symbol = type.getSymbol();
            if (symbol != null && input.startsWith(symbol, pos))
                return true;
        }

        return false;
    }

    /**
     * Advance the current position by one character
     */
    private void advance() {
        advance(1);
    }

    /**
     * Advance the current position by one character
     */
    private void advance(Token.Type type) {
        var symbol = type.getSymbol();

        advance(symbol != null ? symbol.length() : 0);
    }

    /**
     * Advance the current position by count characters
     *
     * @param count Number of characters to advance
     */
    private void advance(int count) {
        for (int i = 0; i < count && pos < input.length(); i++) {
            if (input.charAt(pos) == '\n')
                line++;

            pos++;
        }
    }

    /**
     * Check if current position starts an HTML tag (not just a less-than operator)
     */
    private boolean isTagStart() {
        if (pos + 1 >= input.length()) return false;
        char next = input.charAt(pos + 1);
        return Character.isLetter(next) || next == '/';
    }

    // === Whitespace ===

    /**
     * Skip whitespace characters
     */
    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(current()))
            advance();
    }

    /**
     * Trim trailing whitespace from the last text token in a block
     * if it only contains whitespace after the last newline
     *
     * @param tokens The list of tokens to trim
     */
    private void trimWhitespaceForBlock(List<Token> tokens) {
        if (tokens.isEmpty())
            return;

        Token last = tokens.getLast();
        if (last.type() != GLOBAL_TEXT)
            return;

        String text = last.value();
        int lastNewlineIndex = text.lastIndexOf('\n');

        if (lastNewlineIndex == -1) {
            if (tokens.size() == 1 && text.matches("^[ \\t]+$"))
                tokens.removeFirst();

            return;
        }

        String afterLastNewline = text.substring(lastNewlineIndex + 1);
        if (afterLastNewline.matches("^[ \\t]*$")) {
            String keepPart = text.substring(0, lastNewlineIndex + 1);
            tokens.set(tokens.size() - 1, new Token(GLOBAL_TEXT, keepPart, last.position()));
        }
    }

    /**
     * Skip whitespace and a newline if present after a standalone tag
     */
    private void skipBlockLineEnd() {
        int start = pos;

        // Skip spaces and tabs
        while (pos < input.length() && (current() == ' ' || current() == '\t'))
            advance();

        // Check for newline
        if (pos < input.length() && current() == '\n')
            advance();
        else if (pos < input.length() && current() == '\r') {
            advance();
            if (pos < input.length() && current() == '\n')
                advance();
        } else
            pos = start;
    }

    // === Errors ===

    private String getLine(int lineNumber) {
        String[] lines = input.split("\\R", -1); // handles \n, \r\n, etc.
        if (lineNumber < 1 || lineNumber > lines.length)
            return "";

        return lines[lineNumber - 1];
    }

    private void throwError(String message, int errorPos) {
        var arrow = " ".repeat(Math.max(0, errorPos)) +
                "↳ " + message;

        String formattedMessage = String.format("""
                An error occurred when parsing the input at line %d, column %d
                %s
                %s
                """, line, errorPos, getLine(line), arrow
        );

        throw new RuntimeException(formattedMessage);
    }
}
