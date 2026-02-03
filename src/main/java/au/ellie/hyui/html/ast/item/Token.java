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

package au.ellie.hyui.html.ast.item;

import java.util.Arrays;
import java.util.Objects;

public record Token(Type type, String value, int position) {

    public Token(Type type, int position) {
        this(type, type.getSymbol(), position);
    }

    public enum Type {
        // Expression
        EXPR_OPEN("{{"),              // {{
        EXPR_CLOSE("}}"),             // }}
        EXPR_VARIABLE("$"),           // $name
        EXPR_VARIABLE_DOT("."),       // .
        EXPR_STRING("\""),            // "text"
        EXPR_NUMBER,                         // 123, 45.6
        EXPR_BOOLEAN,                        // true, false
        EXPR_PIPE("|"),               // |
        EXPR_NULL_COALESCING("??"),   // ??  (DEFAULT)
        EXPR_IDENTIFIER,                     // Function name, properties

        // Block
        BLOCK_START("#", EXPR_OPEN),  // {{#
        BLOCK_END("/", EXPR_OPEN),    // {{/
        BLOCK_IF("if"),               // if
        BLOCK_EACH("each"),           // each
        BLOCK_ELSE("else"),           // else

        // Html
        TAG_OPEN("<"),                // <
        TAG_CLOSE(">"),               // >
        TAG_SELF_CLOSE("/>"),         // />
        TAG_END_OPEN("</"),           // </
        TAG_IDENTIFIER,                      // div, container, p, etc.
        TAG_ATTRIBUTE_NAME,                  // class, style, --data-value, etc.

        // Operator
        COMP_EQUALS("=="),            // ==
        COMP_NOT_EQUALS("!="),        // !=
        COMP_LESS_THAN("<"),          // <
        COMP_GREATER_THAN(">"),       // >
        COMP_LESS_EQUALS("<="),       // <=
        COMP_GREATER_EQUALS(">="),    // >=
        COMP_IN("in"),                // in
        COMP_AND("&&"),               // &&
        COMP_OR("||"),                // ||

        // Special
        GLOBAL_ASSIGN("="),           // =
        GLOBAL_TEXT,                         // Text / Html
        GLOBAL_EOF;                          // End of File

        // ==========================

        private final String symbol;

        Type(String symbol) {
            this.symbol = symbol;
        }

        Type(String symbol, Type... parents) {
            this.symbol = Arrays.stream(parents).map((t) -> t.symbol).reduce("", String::concat) + symbol;
        }

        Type() {
            this.symbol = null;
        }

        /**
         * Get the symbol associated with the token type.
         */
        public String getSymbol() {
            return symbol;
        }

        /**
         * Check if the token symbol matches the given character.
         *
         * @param value The value to check against.
         */
        public boolean match(Character value) {
            return symbol != null &&
                    symbol.length() == 1 &&
                    Objects.equals(symbol.charAt(0), value);
        }

        /**
         * Check if the token symbol matches the given value.
         *
         * @param value The value to check against.
         */
        public boolean match(String value) {
            return Objects.equals(symbol, value);
        }
    }
}
