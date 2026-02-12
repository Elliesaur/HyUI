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

package au.ellie.hyui.html.template.item;

import java.util.HashMap;
import java.util.Map;

import static au.ellie.hyui.html.template.item.Token.Type.*;

public record Token(Type type, String value, int position) {

    /**
     * Check if the token matches the given type and value
     *
     * @param type    The type to check
     * @param symbols The values to check
     */
    public boolean match(Type type, String... symbols) {
        if (this.type != type)
            return false;

        if (symbols.length == 0)
            return true;

        return this.match(symbols);
    }

    /**
     * Check if the token matches one of the given values
     *
     * @param symbols The values to check
     */
    public boolean match(String... symbols) {
        for (var symbol : symbols)
            if (this.value.equals(symbol))
                return true;

        return false;
    }

    /**
     * Token types
     */
    public enum Type {
        // Template delimiters
        CLOSE_ANGLE_BRACKET,
        OPEN_ANGLE_BRACKET,
        CLOSE_EXPRESSION,
        OPEN_EXPRESSION,

        // Global tokens
        BACK_SLASH,
        NEW_LINE,
        VARIABLE,
        ASSIGN,
        COLON,
        QUOTE,
        SLASH,
        PIPE,
        DOT,

        // Special
        COMPARATOR,
        OPERATOR,
        KEYWORD,
        SPACER,
        NUMBER,
        TEXT,

        // INTERNAL
        EOI
    }

    public static final Map<String, Type> TOKEN_MAPPER = new HashMap<>() {{
        put(Symbols.CLOSE_ANGLE_BRACKET, CLOSE_ANGLE_BRACKET);
        put(Symbols.OPEN_ANGLE_BRACKET, OPEN_ANGLE_BRACKET);
        put(Symbols.CLOSE_EXPRESSION, CLOSE_EXPRESSION);
        put(Symbols.OPEN_EXPRESSION, OPEN_EXPRESSION);
        put(Symbols.BACK_SLASH, BACK_SLASH);
        put(Symbols.NEW_LINE, NEW_LINE);
        put(Symbols.VARIABLE, VARIABLE);
        put(Symbols.ASSIGN, ASSIGN);
        put(Symbols.COLON, COLON);
        put(Symbols.QUOTE, QUOTE);
        put(Symbols.SLASH, SLASH);
        put(Symbols.PIPE, PIPE);
        put(Symbols.DOT, DOT);
    }};
}
