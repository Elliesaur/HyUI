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

public class Symbols {
    public final static String VARIABLE = "$";
    public final static String DOT = ".";
    public final static String QUOTE = "\"";
    public final static String ASSIGN = "=";

    public final static String EXPRESSION_START = "{{";
    public final static String EXPRESSION_END = "}}";
    public final static String BLOCK_START = "#";
    public final static String BLOCK_END = "/";

    public final static String COMPONENT_START = "<";
    public final static String COMPONENT_END = ">";
    public final static String COMPONENT_SELF_CLOSE = "/>";
    public final static String COMPONENT_CLOSE = "</";

    public final static String PIPE = "|";
    public final static String EQUALS = "==";
    public final static String NOT_EQUALS = "!=";
    public final static String LESS_THAN = "<";
    public final static String GREATER_THAN = ">";
    public final static String LESS_THAN_EQUALS = "<=";
    public final static String GREATER_THAN_EQUALS = ">=";
    public final static String NULL_COALESCING = "??";
    public final static String NOT_IN = "not in";
    public final static String IN = "in";
    public final static String AND = "&&";
    public final static String OR = "||";

    public final static String SECTION_IF = "if";
    public final static String SECTION_ELSE = "else";
    public final static String SECTION_EACH = "each";

    // List of all comparators
    public final static String[] COMPARATORS = new String[]{
            EQUALS,
            NOT_EQUALS,
            GREATER_THAN_EQUALS,
            GREATER_THAN,
            LESS_THAN_EQUALS,
            LESS_THAN,
            NOT_IN,
            IN
    };

    // List of all operators
    public final static String[] OPERATORS = new String[]{
            NULL_COALESCING,
            OR,
            AND,
            PIPE
    };
}
