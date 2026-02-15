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

import java.util.Set;

public class Symbols {

    // Template delimiters
    public final static String CLOSE_ANGLE_BRACKET = ">";
    public final static String OPEN_ANGLE_BRACKET = "<";
    public final static String CLOSE_EXPRESSION = "}}";
    public final static String OPEN_EXPRESSION = "{{";

    // Global symbols
    public final static String BACK_SLASH = "\\";
    public final static String NEW_LINE = "\n";
    public final static String VARIABLE = "$";
    public final static String ASSIGN = "=";
    public final static String COLON = ":";
    public final static String COMMA = ",";
    public final static String QUOTE = "\"";
    public final static String SINGLE_QUOTE = "'";
    public final static String SLASH = "/";
    public final static String PIPE = "|";
    public final static String DOT = ".";

    // Logical operators
    public final static String NULL_COALESCING = "??";
    public final static String AND = "&&";
    public final static String OR = "||";

    // List of all Operators
    public final static String[] OPERATORS = new String[]{
            NULL_COALESCING,
            AND,
            OR,
    };

    // Comparison operators
    public final static String EQUALS = "==";
    public final static String NOT_EQUALS = "!=";
    public final static String LESS_THAN = "<";
    public final static String GREATER_THAN = ">";
    public final static String LESS_THAN_EQUALS = "<=";
    public final static String GREATER_THAN_EQUALS = ">=";

    // List of all comparators
    public final static String[] COMPARATORS = new String[]{
            EQUALS,
            NOT_EQUALS
    };

    // Keywords
    public final static String KEYWORD_NOT_IN = "not in";
    public final static String KEYWORD_IN = "in";

    public final static String KEYWORD_FOR = "for";
    public final static String KEYWORD_ELSE_IF = "else-if";
    public final static String KEYWORD_ELSE = "else";
    public final static String KEYWORD_IF = "if";

    public final static String KEYWORD_FALSE = "false";
    public final static String KEYWORD_TRUE = "true";

    // List of all Keywords
    public final static String[] KEYWORDS = new String[]{
            KEYWORD_NOT_IN,
            KEYWORD_IN,
            KEYWORD_FOR,
            KEYWORD_ELSE_IF,
            KEYWORD_ELSE,
            KEYWORD_IF,
            KEYWORD_FALSE,
            KEYWORD_TRUE,
    };

    public final static Set<String> KEYWORD_CONDITIONALS = Set.of(
            KEYWORD_ELSE_IF,
            KEYWORD_ELSE,
            KEYWORD_IF
    );

    // Html slot related symbols
    public static final String HTML_TAG_TEMPLATE = "template";
    public static final String HTML_TAG_SLOT = "slot";

    public static final String HTML_SLOT_DEFAULT = "default";
    public static final String HTML_SLOT_KEY = "slot:";

    // Scope names
    public static final String SCOPE_COMPONENT_PREFIX = "component:";
    public static final String SCOPE_ROOT_NAME = "root";
    public static final String SCOPE_FOR_NAME = "for";

    // Void elements that do not require a closing tag
    public static final java.util.Set<String> VOID_ELEMENTS = java.util.Set.of(
            "area", "base", "br", "col", "embed", "hr", "img", "input",
            "link", "meta", "param", "source", "track", "wbr"
    );
}
