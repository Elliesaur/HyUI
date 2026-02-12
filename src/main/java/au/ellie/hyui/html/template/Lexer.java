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

import au.ellie.hyui.html.template.item.Token;
import au.ellie.hyui.html.template.item.Token.Type;
import au.ellie.hyui.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static au.ellie.hyui.html.template.item.Symbols.*;

public class Lexer {
    private final String input;
    private int pos = 0;

    public Lexer(String input) {
        this.input = input;
    }

    /**
     * Tokenize the input string into a list of tokens
     */
    public List<Token> tokenize() {
        var tokens = new ArrayList<Token>();

        outer:
        while (pos < input.length()) {
            if (isNumberType())
                tokens.add(tokenizeNumber());
            else {
                var comparator = tokenizeArray(COMPARATORS, Type.COMPARATOR);
                if (comparator != null) {
                    tokens.add(comparator);
                    continue;
                }

                var operator = tokenizeArray(OPERATORS, Type.OPERATOR);
                if (operator != null) {
                    tokens.add(operator);
                    continue;
                }

                var keyword = tokenizeArray(KEYWORDS, Type.KEYWORD);
                if (keyword != null) {
                    tokens.add(keyword);
                    continue;
                }

                for (var entry : Token.TOKEN_MAPPER.entrySet()) {
                    if (peek(entry.getKey())) {
                        tokens.add(new Token(entry.getValue(), entry.getKey(), pos));
                        skip(entry.getKey());
                        continue outer;
                    }
                }

                if (peek(" "))
                    tokens.add(tokenizeSpacer());
                else
                    tokens.add(tokenizeText());
            }


        }

        tokens.add(new Token(Type.EOI, "\0", pos));

        return tokens;
    }

    /**
     * Tokenize a number (integer or decimal)
     */
    private Token tokenizeNumber() {
        var start = pos;
        var current = current();

        var builder = new StringBuilder();
        if (current == '-') {
            builder.append(current());
            current = skip();
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
     * Tokenize a comparator operator
     */
    private Token tokenizeArray(String[] symbols, Type type) {
        var symbol = filter(symbols);
        if (symbol != null) {
            var token = new Token(type, symbol, pos);
            skip(symbol);
            return token;
        }

        return null;
    }

    /**
     * Tokenize a spacer (sequence of spaces/tabs)
     */
    private Token tokenizeSpacer() {
        var start = pos;
        var builder = new StringBuilder();

        while (pos < input.length() && (current() == ' ' || current() == '\t')) {
            builder.append(current());
            skip();
        }

        return new Token(Type.SPACER, builder.toString(), start);
    }

    /**
     * Tokenize plain text until the next expression or HTML tag
     */
    private Token tokenizeText() {
        var start = pos;

        var current = current();
        var builder = new StringBuilder();

        do {
            builder.append(current);
            current = skip();
        } while (pos < input.length() && StringUtils.isAsciiLetter(current));

        return new Token(Type.TEXT, builder.toString(), start);
    }

    // ===== Navigation =====

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
        for (var i = 0; i < count && pos < input.length(); i++)
            pos++;

        return current();
    }

    // ===== Helper =====

    /**
     * Check if the current position starts a number
     */
    private boolean isNumberType() {
        var current = current();
        return Character.isDigit(current) ||
                (current == '-' && Character.isDigit(next()));
    }
}
