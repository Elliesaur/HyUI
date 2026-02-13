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
import au.ellie.hyui.utils.StringReader;
import au.ellie.hyui.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static au.ellie.hyui.html.template.item.Symbols.*;

public class Lexer {
    private final StringReader reader;

    public Lexer(String input) {
        this.reader = new StringReader(input);
    }

    /**
     * Tokenize the input string into a list of tokens
     */
    public List<Token> tokenize() {
        var tokens = new ArrayList<Token>();

        outer:
        while (reader.hasNext()) {
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
                    if (reader.startsWith(entry.getKey())) {
                        tokens.add(new Token(entry.getValue(), entry.getKey(), reader.getPosition()));
                        reader.skip(entry.getKey());
                        continue outer;
                    }
                }

                if (reader.peek() == ' ')
                    tokens.add(tokenizeSpacer());
                else
                    tokens.add(tokenizeText());
            }


        }

        tokens.add(new Token(Type.EOI, "\0", reader.getPosition()));

        return tokens;
    }

    /**
     * Tokenize a number (integer or decimal)
     */
    private Token tokenizeNumber() {
        var start = reader.getPosition();
        var builder = new StringBuilder();

        // Handle optional leading minus sign
        if (reader.peek() == '-')
            builder.append(reader.advance());

        var hasDecimal = false;
        while (reader.hasNext()) {
            var current = reader.peek();

            if (Character.isDigit(current)) {
                builder.append(reader.advance());
            } else if (current == '.') {
                if (hasDecimal)
                    break;

                hasDecimal = true;
                builder.append(reader.advance());
            } else
                break;
        }

        return new Token(Type.NUMBER, builder.toString(), start);
    }

    /**
     * Tokenize a comparator operator
     */
    private Token tokenizeArray(String[] symbols, Type type) {
        var symbol = reader.filter(symbols);
        Token result = null;

        if (symbol != null) {
            result = new Token(type, symbol, reader.getPosition());
            reader.skip(symbol);
        }

        return result;
    }

    /**
     * Tokenize a spacer (sequence of spaces/tabs)
     */
    private Token tokenizeSpacer() {
        var start = reader.getPosition();
        var builder = new StringBuilder();

        while (reader.hasNext() && (reader.peek() == ' ' || reader.peek() == '\t'))
            builder.append(reader.advance());

        return new Token(Type.SPACER, builder.toString(), start);
    }

    /**
     * Tokenize plain text until the next expression or HTML tag
     */
    private Token tokenizeText() {
        int start = reader.getPosition();
        var builder = new StringBuilder();

        do {
            builder.append(reader.advance());
        } while (reader.hasNext() && StringUtils.isAsciiLetter(reader.peek()));

        return new Token(Type.TEXT, builder.toString(), start);
    }

    // ===== Helper =====

    /**
     * Check if the current position starts a number
     */
    private boolean isNumberType() {
        char current = reader.peek();
        return Character.isDigit(current) ||
                (current == '-' && Character.isDigit(reader.next()));
    }
}
