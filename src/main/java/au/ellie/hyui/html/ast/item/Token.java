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
