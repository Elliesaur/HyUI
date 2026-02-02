package au.ellie.hyui.html.ast;

import au.ellie.hyui.html.ast.item.Token;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int pos = 0;

    // May be used for debug
    private int line = 1;
    private int column = 1;

    public Lexer(String input) {
        this.input = input;
    }

    /**
     * Tokenize the input string into a list of tokens
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < input.length()) {
            if (peek("{{#")) {
                trimWhitespaceForBlock(tokens);

                tokens.add(new Token(Token.Type.BLOCK_OPEN, "{{#", pos));
                advance(3);
                tokenizeExpression(tokens);

                skipBlockLineEnd();
            } else if (peek("{{/")) {
                trimWhitespaceForBlock(tokens);

                tokens.add(new Token(Token.Type.BLOCK_CLOSE, "{{/", pos));
                advance(3);
                tokenizeExpression(tokens);

                skipBlockLineEnd();
            } else if (peek("{{")) {
                tokens.add(new Token(Token.Type.EXPR_OPEN, "{{", pos));
                advance(2);
                tokenizeExpression(tokens);
            } else
                tokenizeText(tokens);
        }

        tokens.add(new Token(Token.Type.GLOBAL_EOF, "", pos));

        return tokens;
    }

    /**
     * Tokenize an expression until the closing "}}"
     *
     * @param tokens The list to add tokens to
     */
    private void tokenizeExpression(List<Token> tokens) {
        skipWhitespace();

        while (pos < input.length()) {
            if (peek("}}"))
                break;

            var current = current();

            // String
            if (current == '"') {
                tokens.add(tokenizeString());
            }

            // Variable
            else if (current == '$') {
                tokens.add(tokenizeVariable());
            }

            // Numbers
            else if (Character.isDigit(current) ||
                    (current == '-' &&
                            pos + 1 < input.length() &&
                            Character.isDigit(input.charAt(pos + 1))
                    )
            ) {
                tokens.add(tokenizeNumber());
            }

            // Keyword / Operator
            else if (peek("==")) {
                tokens.add(new Token(Token.Type.COMP_EQUALS, "==", pos));
                advance(2);
            } else if (peek("!=")) {
                tokens.add(new Token(Token.Type.COMP_NOT_EQUALS, "!=", pos));
                advance(2);
            } else if (peek("<=")) {
                tokens.add(new Token(Token.Type.COMP_LESS_EQUALS, "<=", pos));
                advance(2);
            } else if (peek(">=")) {
                tokens.add(new Token(Token.Type.COMP_GREATER_EQUALS, ">=", pos));
                advance(2);
            } else if (peek("<")) {
                tokens.add(new Token(Token.Type.COMP_LESS_THAN, "<", pos));
                advance(1);
            } else if (peek(">")) {
                tokens.add(new Token(Token.Type.COMP_GREATER_THAN, ">", pos));
                advance(1);
            } else if (peek("&&")) {
                tokens.add(new Token(Token.Type.COMP_AND, "&&", pos));
                advance(2);
            } else if (peek("??")) {
                tokens.add(new Token(Token.Type.EXPR_NULL_COALESCING, "??", pos));
                advance(2);
            } else if (peek("||")) {
                tokens.add(new Token(Token.Type.COMP_OR, "||", pos));
                advance(2);
            } else if (peek("|")) {
                tokens.add(new Token(Token.Type.EXPR_PIPE, "|", pos));
                advance(1);
            } else if (peek(".")) {
                tokens.add(new Token(Token.Type.EXPR_VARIABLE_DOT, ".", pos));
                advance(1);
            }

            // Identifiers
            else if (Character.isLetter(current))
                tokens.add(tokenizeIdentifier());

            else
                throwError("Unexpected character: " + current(), pos);

            skipWhitespace();
        }

        if (peek("}}")) {
            tokens.add(new Token(Token.Type.EXPR_CLOSE, "}}", pos));
            advance(2);
        }
    }

    /**
     * Tokenize a string literal
     */
    private Token tokenizeString() {
        int start = pos;
        advance(); // Skip opening "

        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && current() != '"') {
            if (current() == '\\' && pos + 1 < input.length()) {
                advance();
                char escaped = current();
                switch (escaped) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    default -> sb.append(escaped);
                }
                advance();
            } else {
                sb.append(current());
                advance();
            }
        }

        if (current() != '"')
            throwError("Unterminated string", start);

        advance(); // Skip closing "

        return new Token(Token.Type.EXPR_STRING, sb.toString(), start);
    }

    /**
     * Tokenize a variable (starts with $)
     */
    private Token tokenizeVariable() {
        int start = pos;
        advance(); // Skip $
        StringBuilder sb = new StringBuilder();

        while (pos < input.length() && (Character.isLetterOrDigit(current()) || current() == '_' || current() == '-')) {
            sb.append(current());
            advance();
        }

        return new Token(Token.Type.EXPR_VARIABLE, sb.toString(), start);
    }

    /**
     * Tokenize a number (integer or decimal)
     */
    private Token tokenizeNumber() {
        StringBuilder sb = new StringBuilder();
        if (current() == '-') {
            sb.append(current());
            advance();
        }

        // Must have at least one digit after the sign
        int start = pos;
        if (!Character.isDigit(current())) {
            pos = start;

            throwError("Expected digit after '-'", pos);
        }

        boolean hasDecimal = false;
        while (pos < input.length() && (Character.isDigit(current()) || current() == '.')) {
            if (current() == '.') {
                if (hasDecimal)
                    break;

                hasDecimal = true;
            }

            sb.append(current());
            advance();
        }

        return new Token(Token.Type.EXPR_NUMBER, sb.toString(), start);
    }

    /**
     * Tokenize an identifier or keyword
     */
    private Token tokenizeIdentifier() {
        int start = pos;

        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && (Character.isLetterOrDigit(current()) || current() == '_' || current() == '-')) {
            sb.append(current());
            advance();
        }

        String value = sb.toString();
        Token.Type type = switch (value) {
            case "if" -> Token.Type.BLOCK_IF;
            case "else" -> Token.Type.BLOCK_ELSE;
            case "each" -> Token.Type.BLOCK_EACH;
            case "true", "false" -> Token.Type.EXPR_BOOLEAN;
            case "in" -> Token.Type.COMP_IN;
            default -> Token.Type.EXPR_IDENTIFIER;
        };

        return new Token(type, value, start);
    }

    /**
     * Tokenize plain text until the next "{{"
     *
     * @param tokens The list to add tokens to
     */
    private void tokenizeText(List<Token> tokens) {
        int start = pos;

        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && !peek("{{")) {
            sb.append(current());
            advance();
        }

        if (!sb.isEmpty())
            tokens.add(new Token(Token.Type.GLOBAL_TEXT, sb.toString(), start));
    }

    // ===== Helpers =====

    /**
     * Returns the current character or '\0' if at the end of input
     */
    private char current() {
        return pos < input.length() ? input.charAt(pos) : '\0';
    }

    /**
     * Peeks ahead to see if the next characters match the given string
     *
     * @param str The string to match
     */
    private boolean peek(String str) {
        return input.startsWith(str, pos);
    }

    /**
     * Advance the current position by one character
     */
    private void advance() {
        advance(1);
    }

    /**
     * Advance the current position by count characters
     *
     * @param count Number of characters to advance
     */
    private void advance(int count) {
        for (int i = 0; i < count && pos < input.length(); i++) {
            if (input.charAt(pos) == '\n') {
                line++;
                column = 1;
            } else
                column++;

            pos++;
        }
    }

    /**
     * Skip whitespace characters
     */
    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(current()))
            advance();
    }

    /**
     * Trim trailing whitespace from the last text token in a block
     * if it only contains whitespace after the last newline
     *
     * @param tokens The list of tokens to trim
     */
    private void trimWhitespaceForBlock(List<Token> tokens) {
        if (tokens.isEmpty())
            return;

        Token last = tokens.getLast();
        if (last.type() != Token.Type.GLOBAL_TEXT)
            return;

        String text = last.value();
        int lastNewlineIndex = text.lastIndexOf('\n');

        if (lastNewlineIndex == -1) {
            if (tokens.size() == 1 && text.matches("^[ \\t]+$"))
                tokens.removeFirst();

            return;
        }

        String afterLastNewline = text.substring(lastNewlineIndex + 1);
        if (afterLastNewline.matches("^[ \\t]*$")) {
            String keepPart = text.substring(0, lastNewlineIndex + 1);
            tokens.set(tokens.size() - 1, new Token(Token.Type.GLOBAL_TEXT, keepPart, last.position()));
        }
    }

    /**
     * Skip whitespace and a newline if present after a standalone tag
     */
    private void skipBlockLineEnd() {
        int start = pos;

        // Skip spaces and tabs
        while (pos < input.length() && (current() == ' ' || current() == '\t'))
            advance();

        // Check for newline
        if (pos < input.length() && current() == '\n')
            advance();
        else if (pos < input.length() && current() == '\r') {
            advance();
            if (pos < input.length() && current() == '\n')
                advance();
        } else
            pos = start;
    }

    private String getLine(int lineNumber) {
        String[] lines = input.split("\\R", -1); // handles \n, \r\n, etc.
        if (lineNumber < 1 || lineNumber > lines.length)
            return "";

        return lines[lineNumber - 1];
    }

    private void throwError(String message, int errorPos) {
        var arrow = " ".repeat(Math.max(0, errorPos)) +
                "↳ " + message;

        String formattedMessage = String.format("""
                An error occurred when parsing the input at line %d, column %d
                %s
                %s
                """, line, errorPos, getLine(line), arrow
        );

        throw new RuntimeException(formattedMessage);
    }
}
