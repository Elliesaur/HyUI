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

public record Token(Type type, String value, int position) {

    /**
     * Check if the token matches the given type and value
     *
     * @param type    The type to check
     * @param symbols The values to check
     */
    public boolean match(Type type, String... symbols) {
        if (this.type != type && type != Type.ANY)
            return false;

        if (symbols.length == 0)
            return true;

        return this.match(symbols);
    }

    /**
     * Check if the token matches the given type and value
     *
     * @param symbols The values to check
     */
    public boolean match(String... symbols) {
        for (String v : symbols) {
            if (this.value.equals(v))
                return true;
        }

        return false;
    }

    /**
     * Token types
     */
    public enum Type {
        // Global
        TEXT,
        VARIABLE,
        VARIABLE_DOT,
        STRING,
        NUMBER,
        BOOLEAN,
        IDENTIFIER,
        ATTRIBUTE,
        COMPARATOR,
        OPERATOR,
        ASSIGN,
        SLOT,

        // Expression
        EXPRESSION_OPEN,
        EXPRESSION_CLOSE,

        // Components
        HTML_OPEN,
        HTML_CLOSE,

        // Block
        BLOCK_HEAD,
        BLOCK_TAIL,

        // Special
        ANY,
        EOF
    }
}
