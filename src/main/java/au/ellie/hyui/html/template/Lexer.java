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

package au.ellie.hyui.html.template;

import au.ellie.hyui.html.template.item.Symbols;
import au.ellie.hyui.html.template.item.Token;
import au.ellie.hyui.html.template.item.Token.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static au.ellie.hyui.html.template.item.Symbols.*;

public class Lexer {
    private final String input;

    private int lineStart = 0;
    private int line = 1;
    private int pos = 0;

    public Lexer(String input) {
        this.input = input;
    }

    /**
     * Tokenize the input string into a list of tokens
     */
    public List<Token> tokenize() {
        var tokens = new ArrayList<Token>();

        while (pos < input.length()) {
            if (peek(EXPRESSION_OPEN))
                tokenizeExpressionOrBlock(tokens);
            else if (isHtml()) {
                var startPos = pos;
                var executed = peek(HTML_END) ?
                        tokenizeEndComponent(tokens) :
                        tokenizeStartComponent(tokens);

                if (!executed) {
                    pos = startPos;
                    tokenizeText(tokens);
                }
            } else
                tokenizeText(tokens);
        }

        tokens.add(new Token(Type.EOF, "", pos));

        return tokens;
    }

    /**
     * Tokenize expressions and blocks
     *
     * @param tokens The list to add tokens to
     */
    private void tokenizeExpressionOrBlock(List<Token> tokens) {
        var blockStartPos = pos;
        var isBlock = true;

        expect(EXPRESSION_OPEN);
        tokens.add(new Token(Type.EXPRESSION_OPEN, EXPRESSION_OPEN, pos - EXPRESSION_OPEN.length()));

        skipWhitespace();

        if (consume(BLOCK_START)) {
            tokens.add(new Token(Type.BLOCK_HEAD, BLOCK_START, pos - BLOCK_START.length()));
            tokens.add(tokenizeIdentifier(Type.IDENTIFIER));
            skipWhitespace();
        } else if (consume(BLOCK_END)) {
            tokens.add(new Token(Type.BLOCK_TAIL, BLOCK_END, pos - BLOCK_END.length()));
            tokens.add(tokenizeIdentifier(Type.IDENTIFIER));
            skipWhitespace();
        } else
            isBlock = false;

        tokenizeExpression(tokens);

        expect(EXPRESSION_CLOSE);
        tokens.add(new Token(Type.EXPRESSION_CLOSE, EXPRESSION_CLOSE, pos - EXPRESSION_CLOSE.length()));

        if (isBlock && isStandaloneLine(blockStartPos, pos))
            consumeStandaloneLine(tokens);
    }

    /**
     * Tokenize an expression until the closing "}}"
     *
     * @param tokens The list to add tokens to
     */
    private void tokenizeExpression(List<Token> tokens) {
        skipWhitespace();

        while (pos < input.length()) {
            if (peek(EXPRESSION_CLOSE))
                break;

            var current = current();

            if (peek(QUOTE)) {
                tokens.add(tokenizeString());
            } else if (peek(VARIABLE)) {
                tokens.add(tokenizeVariable());
            } else if (peek(DOT)) {
                tokens.add(new Token(Type.VARIABLE_DOT, DOT, pos));
                skip(DOT);
            } else if (isNumberType()) {
                tokens.add(tokenizeNumber());
            } else {
                var comparator = filter(COMPARATORS);
                if (comparator != null) {
                    tokens.add(new Token(Type.COMPARATOR, comparator, pos));
                    skip(comparator);
                } else {
                    var operator = filter(OPERATORS);
                    if (operator != null) {
                        tokens.add(new Token(Type.OPERATOR, operator, pos));
                        skip(operator);
                    } else if (Character.isLetter(current))
                        tokens.add(tokenizeIdentifier());
                    else
                        throwError("Unexpected character: " + current(), pos);
                }
            }

            skipWhitespace();
        }
    }

    /**
     * Tokenize a string literal
     */
    private Token tokenizeString() {
        var start = pos;
        expect(QUOTE);

        var current = current();
        var builder = new StringBuilder();
        while (pos < input.length() && !peek(QUOTE)) {
            if (current == '\\' && pos + 1 < input.length()) {
                current = skip();

                switch (current) {
                    case 'n' -> builder.append('\n');
                    case 't' -> builder.append('\t');
                    case '"' -> builder.append('"');
                    case '\\' -> builder.append('\\');
                    default -> builder.append(current);
                }
            } else
                builder.append(current);

            current = skip();
        }

        expect(QUOTE);
        return new Token(Type.STRING, builder.toString(), start);
    }

    /**
     * Tokenize a variable (starts with $)
     */
    private Token tokenizeVariable() {
        var start = pos;
        expect(VARIABLE);

        var current = current();
        var builder = new StringBuilder();
        while (pos < input.length() && (Character.isLetterOrDigit(current) || current == '_' || current == '-' || current == ':')) {
            builder.append(current);
            current = skip();
        }

        return new Token(Type.VARIABLE, builder.toString(), start);
    }

    /**
     * Tokenize a number (integer or decimal)
     */
    private Token tokenizeNumber() {
        var current = current();
        var builder = new StringBuilder();
        if (current == '-') {
            builder.append(current());
            current = skip();
        }

        // Must have at least one digit after the sign
        var start = pos;
        if (!Character.isDigit(current)) {
            pos = start;

            throwError("Expected digit after '-'", pos);
        }

        var hasDecimal = false;
        while (pos < input.length() && (Character.isDigit(current) || current == '.')) {
            if (current == '.') {
                if (hasDecimal)
                    break;

                hasDecimal = true;
            }

            builder.append(current());
            current = skip();
        }

        return new Token(Type.NUMBER, builder.toString(), start);
    }

    /**
     * Tokenize an identifier or keyword
     */
    private Token tokenizeIdentifier() {
        return tokenizeIdentifier(null);
    }

    /**
     * Tokenize an identifier or keyword with specified type
     */
    private Token tokenizeIdentifier(Type type) {
        var start = pos;

        var current = current();
        var builder = new StringBuilder();
        while (pos < input.length() && (Character.isLetterOrDigit(current) || current == '_' || current == '-')) {
            builder.append(current);
            current = skip();
        }

        var value = builder.toString();
        if (type == null) {
            type = switch (value) {
                case "true", "false" -> Type.BOOLEAN;
                default -> Type.IDENTIFIER;
            };
        }

        return new Token(type, value, start);
    }

    // ===== Components =====

    /**
     * Tokenize plain text until the next expression or HTML tag
     *
     * @param tokens The list to add tokens to
     */
    private void tokenizeText(List<Token> tokens) {
        var start = pos;

        var builder = new StringBuilder();
        while (pos < input.length() && !peek(EXPRESSION_OPEN) && !isHtml()) {
            var current = current();
            builder.append(current);
            skip();

            if (current == '\n')
                break;
        }

        if (!builder.isEmpty())
            tokens.add(new Token(Type.TEXT, builder.toString(), start));
    }

    /**
     * Tokenize an HTML start tag: "<tagname attr="value" --custom />"
     *
     * @param tokens The list to add tokens to
     */
    private boolean tokenizeStartComponent(List<Token> tokens) {
        expect(HTML_OPEN);
        tokens.add(new Token(Type.HTML_OPEN, HTML_OPEN, pos - HTML_OPEN.length()));

        // Identifier
        tokens.add(tokenizeComponentName());
        skipWhitespace();

        // Attributes
        while (pos < input.length() && !peek(HTML_CLOSE, HTML_END_SELF)) {
            // Attribute name
            if (Character.isLetter(current()))
                tokens.add(tokenizeComponentAttributeName());

            skipWhitespace();

            // Check for = and value
            if (consume(ASSIGN)) {
                tokens.add(new Token(Type.ASSIGN, ASSIGN, pos - ASSIGN.length()));
                skipWhitespace();

                if (peek(EXPRESSION_OPEN))
                    tokenizeExpressionOrBlock(tokens);
                else if (peek(QUOTE))
                    tokens.add(tokenizeString());
                else if (isNumberType())
                    tokens.add(tokenizeNumber());
                else
                    throwError("Unexpected character in attribute value: " + current(), pos);
            } else if (peek(EXPRESSION_OPEN))
                tokenizeExpressionOrBlock(tokens);
            else
                throwError("Unexpected character in attribute value: " + current(), pos);

            skipWhitespace();
        }

        // Self-closing or normal close
        var close = filter(HTML_CLOSE, HTML_END_SELF);
        if (close != null) {
            tokens.add(new Token(Type.HTML_CLOSE, close, pos));
            skip(close);
        } else
            throwError("Expected '" + HTML_CLOSE + "' or '" + HTML_END_SELF + "' to close tag", pos);

        return true;
    }

    /**
     * Tokenize an HTML end tag: </tagname>
     */
    private boolean tokenizeEndComponent(List<Token> tokens) {
        expect(HTML_END);
        tokens.add(new Token(Type.HTML_OPEN, HTML_END, pos - HTML_OPEN.length()));

        // Identifier
        tokens.add(tokenizeComponentName());
        skipWhitespace();

        if (consume(HTML_CLOSE))
            tokens.add(new Token(Type.HTML_CLOSE, HTML_CLOSE, pos - HTML_CLOSE.length()));
        else
            throwError("Expected '" + HTML_CLOSE + "' to close end tag", pos);

        return true;
    }

    /**
     * Tokenize an HTML attribute string value
     */
    private Token tokenizeComponentAttributeName() {
        var start = pos;

        var current = current();
        var builder = new StringBuilder();
        while (pos < input.length() && (Character.isLetterOrDigit(current) || current == '-' || current == ':')) {
            builder.append(current);
            current = skip();
        }

        return new Token(Type.ATTRIBUTE, builder.toString(), start);
    }

    /**
     * Tokenize an HTML attribute name
     */
    private Token tokenizeComponentName() {
        var start = pos;

        var current = current();
        var builder = new StringBuilder();
        while (pos < input.length() && (Character.isLetterOrDigit(current) || current == '-' || current == ':')) {
            builder.append(current);
            current = skip();
        }

        var identifier = builder.toString();
        var type = isSlotIdentifier(identifier) ?
                Type.SLOT : Type.IDENTIFIER;

        return new Token(type, identifier, start);
    }

    // ===== Helpers =====

    /**
     * Returns the current character or '\0' if at the end of input
     */
    private char current() {
        return pos < input.length() ? input.charAt(pos) : '\0';
    }

    /**
     * Returns the next character without advancing the position
     */
    private char next() {
        return (pos + 1) < input.length() ? input.charAt(pos + 1) : '\0';
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
     * Filter the next characters to see if they match any of the given strings
     *
     * @param str The string(s) to match
     * @return The matched string, or null if none matched
     */
    private String filter(String... str) {
        for (var s : str)
            if (input.startsWith(s, pos))
                return s;

        return null;
    }

    /**
     * Move the current position forward by the length of the given symbol
     *
     * @param str The string(s) to consume
     */
    private boolean consume(String... str) {
        for (var s : str) {
            if (input.startsWith(s, pos)) {
                skip(s);
                return true;
            }
        }

        return false;
    }

    /**
     * Expect the next characters to match the given string
     *
     * @param str The string to expect
     */
    private void expect(String str) {
        expect(str, "Expected " + str + ", got '" + (pos < input.length() ? input.charAt(pos) : "EOF") + "'");
    }

    /**
     * Expect the next characters to match the given string
     *
     * @param str     The string to expect
     * @param message The error message to use
     */
    private void expect(String str, String message) {
        if (input.startsWith(str, pos)) {
            skip(str);
            return;
        }

        throwError(message, pos);
    }

    /**
     * Move the current position forward by one character
     */
    private char skip() {
        return skip(1);
    }

    /**
     * Advance the current position by the length of the given symbol
     *
     * @param symbol The symbol to advance by
     */
    private char skip(String symbol) {
        return skip(symbol.length());
    }

    /**
     * Advance the current position by count characters
     *
     * @param count Number of characters to advance
     */
    private char skip(int count) {
        for (var i = 0; i < count && pos < input.length(); i++) {
            if (input.charAt(pos) == '\n') {
                lineStart = pos + 1;
                line++;
            }

            pos++;
        }

        return current();
    }

    /**
     * Check if the current position starts a number
     */
    private boolean isNumberType() {
        var current = current();
        return Character.isDigit(current) ||
                (current == '-' && Character.isDigit(next()));
    }

    /**
     * Check if the given identifier is a slot name
     * <pre>
     * - Start with {@link Symbols#HTML_SLOT_OUTPUT} for output slots
     * - Start with {@link Symbols#HTML_SLOT_INPUT} for input slots
     * </pre>
     *
     * @param id The identifier to check
     */
    private boolean isSlotIdentifier(String id) {
        return id.equals(HTML_SLOT_OUTPUT)
                || id.startsWith(HTML_SLOT_OUTPUT + HTML_SLOT_INPUT)
                || id.startsWith(HTML_SLOT_INPUT);
    }

    /**
     * Check if current position starts an HTML tag
     * and not just a less-than operator
     */
    private boolean isHtml() {
        if (!peek(HTML_OPEN))
            return false;

        var next = next();
        return next == '/' || next == ':' || Character.isLetter(next);
    }

    // === Whitespace ===

    /**
     * Skip whitespace characters
     */
    private void skipWhitespace() {
        var current = current();
        while (pos < input.length() && Character.isWhitespace(current))
            current = skip();
    }

    /**
     * Check if the block between blockStart and blockEnd is on a line by itself
     *
     * @param blockStart The start position of the block (inclusive)
     * @param blockEnd   The end position of the block (exclusive)
     */
    private boolean isStandaloneLine(int blockStart, int blockEnd) {
        // Check if there's only whitespace before the block on this line
        for (var i = lineStart; i < blockStart; i++) {
            var c = input.charAt(i);
            if (c != ' ' && c != '\t')
                return false;
        }

        // Check if there's only whitespace after the block until newline
        var i = blockEnd;
        while (i < input.length()) {
            var c = input.charAt(i);
            if (c == '\n' || c == '\r')
                return true;

            if (c != ' ' && c != '\t')
                return false;

            i++;
        }

        return true;
    }

    /**
     * Consume the rest of the line after a standalone block
     */
    private void consumeStandaloneLine(List<Token> tokens) {
        var index = tokens.size() - 1;
        while (index >= 0) {
            var token = tokens.get(index);
            if (token.type() == Type.TEXT) {
                if (!Objects.equals(token.value(), "\n") && token.value().isBlank())
                    tokens.remove(index);

                break;
            }

            index--;
        }

        // Skip trailing spaces/tabs on the same line
        while (pos < input.length()) {
            var c = input.charAt(pos);
            if (c == ' ' || c == '\t')
                pos++;
            else
                break;
        }

        // Skip the newline character(s)
        if (pos < input.length()) {
            var c = input.charAt(pos);
            if (c == '\r') {
                pos++;
                if (pos < input.length() && input.charAt(pos) == '\n')
                    pos++;
                lineStart = pos;
                line++;
            } else if (c == '\n') {
                pos++;
                lineStart = pos;
                line++;
            }
        }
    }

    // === Errors ===

    private String getLine(int lineNumber) {
        var lines = input.split("\\R", -1); // handles \n,\t,\r\n, etc.
        if (lineNumber < 1 || lineNumber > lines.length)
            return "";

        return lines[lineNumber - 1];
    }

    private void throwError(String message, int errorPos) {
        int column = errorPos - lineStart;
        var arrow = " ".repeat(Math.max(0, column)) +
                "↳ " + message;

        String formattedMessage = String.format("""
                An error occurred when parsing the input at line %d, column %d
                %s
                %s
                """, line, column, getLine(line), arrow
        );

        throw new RuntimeException(formattedMessage);
    }
}
