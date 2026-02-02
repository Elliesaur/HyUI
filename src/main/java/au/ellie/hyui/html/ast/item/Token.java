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

public record Token(Type type, String value, int position) {
    public enum Type {
        // Expression
        EXPR_OPEN,             // {{
        EXPR_CLOSE,            // }}
        EXPR_VARIABLE,         // $name
        EXPR_VARIABLE_DOT,     // .
        EXPR_STRING,           // "text"
        EXPR_NUMBER,           // 123, 45.6
        EXPR_BOOLEAN,          // true, false
        EXPR_PIPE,             // |
        EXPR_NULL_COALESCING,  // ??  (DEFAULT)
        EXPR_IDENTIFIER,       // Function name, properties

        // Block
        BLOCK_OPEN,            // {{#
        BLOCK_CLOSE,           // {{/
        BLOCK_IF,              // if
        BLOCK_EACH,            // each
        BLOCK_ELSE,            // else

        // Html
        TAG_OPEN,              // <
        TAG_CLOSE,             // >
        TAG_SELF_CLOSE,        // />
        TAG_END_OPEN,          // </
        TAG_IDENTIFIER,        // div, container, p, etc.
        TAG_ATTRIBUTE_NAME,    // class, style, --data-value, etc.

        // Operator
        COMP_EQUALS,           // ==
        COMP_NOT_EQUALS,       // !=
        COMP_LESS_THAN,        //
        COMP_GREATER_THAN,     // >
        COMP_LESS_EQUALS,      // <=
        COMP_GREATER_EQUALS,   // >=
        COMP_IN,               // in
        COMP_AND,              // &&
        COMP_OR,               // ||

        // Special
        GLOBAL_ASSIGN,         // =
        GLOBAL_TEXT,           // Text / Html
        GLOBAL_EOF
    }
}
