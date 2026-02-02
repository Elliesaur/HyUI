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

package au.ellie.hyui.html.ast;

import au.ellie.hyui.html.ast.item.Node;
import au.ellie.hyui.html.ast.item.Node.BlockNode.EachBlockNode;
import au.ellie.hyui.html.ast.item.Node.BlockNode.IfBlockNode;
import au.ellie.hyui.html.ast.item.Node.ExpressionNode;
import au.ellie.hyui.html.ast.item.Node.ExpressionNode.BinaryOpNode;
import au.ellie.hyui.html.ast.item.Node.ExpressionNode.DefaultNode;
import au.ellie.hyui.html.ast.item.Node.ExpressionNode.LiteralNode;
import au.ellie.hyui.html.ast.item.Node.ExpressionNode.PipeNode;
import au.ellie.hyui.html.ast.item.Node.ExpressionNode.PropertyAccessNode;
import au.ellie.hyui.html.ast.item.Node.ExpressionNode.TextNode;
import au.ellie.hyui.html.ast.item.Node.ExpressionNode.VariableNode;
import au.ellie.hyui.html.ast.item.Token;

import java.util.ArrayList;
import java.util.List;

import static au.ellie.hyui.html.ast.item.Token.Type.*;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Parse the list of tokens into an AST
     *
     * @return List of AST nodes
     */
    public List<Node> parse() {
        List<Node> nodes = new ArrayList<>();

        while (!isAtEnd())
            nodes.add(parseNode());

        return nodes;
    }

    /**
     * Parse a single AST node
     *
     * @return AST node
     */
    private Node parseNode() {
        Token token = current();

        return switch (token.type()) {
            case GLOBAL_TEXT -> {
                advance();
                yield new TextNode(token.value());
            }
            case EXPR_OPEN -> parseExpression();
            case BLOCK_OPEN -> parseBlock();
            default -> throw new RuntimeException("Unexpected token: " + token);
        };
    }

    /**
     * Parse an expression
     *
     * @return AST node representing the expression
     */
    private Node parseExpression() {
        expect(EXPR_OPEN);
        ExpressionNode expr = parseExpressionContent();
        expect(EXPR_CLOSE);
        return expr;
    }

    /**
     * Parse the content of an expression
     *
     * @return Expression node
     */
    private ExpressionNode parseExpressionContent() {
        return parseDefault();
    }

    /**
     * Parse `nullish` coalescing expressions
     *
     * @return Expression node
     */
    private ExpressionNode parseDefault() {
        List<ExpressionNode> alternatives = new ArrayList<>();

        do {
            alternatives.add(parseOr());
        } while (match(EXPR_NULL_COALESCING));

        return alternatives.size() == 1 ? alternatives.getFirst() : new DefaultNode(alternatives);
    }

    /**
     * Parse logical `OR` expressions
     *
     * @return Expression node
     */
    private ExpressionNode parseOr() {
        ExpressionNode left = parseAnd();

        while (match(COMP_OR)) {
            Token operator = previous();
            ExpressionNode right = parseAnd();
            left = new BinaryOpNode(left, operator.type(), right);
        }

        return left;
    }

    /**
     * Parse logical `AND` expressions
     *
     * @return Expression node
     */
    private ExpressionNode parseAnd() {
        ExpressionNode left = parseComparison();

        while (match(COMP_AND)) {
            Token operator = previous();
            ExpressionNode right = parseComparison();
            left = new BinaryOpNode(left, operator.type(), right);
        }

        return left;
    }

    /**
     * Parse `comparison` expressions
     *
     * @return Expression node
     */
    private ExpressionNode parseComparison() {
        ExpressionNode left = parsePipe();

        if (match(COMP_EQUALS, COMP_NOT_EQUALS, COMP_LESS_THAN,
                COMP_GREATER_THAN, COMP_LESS_EQUALS, COMP_GREATER_EQUALS,
                COMP_IN)) {
            Token operator = previous();
            ExpressionNode right = parsePipe();
            return new BinaryOpNode(left, operator.type(), right);
        }

        return left;
    }

    /**
     * Parse `pipe` expressions
     *
     * @return Expression node
     */
    private ExpressionNode parsePipe() {
        ExpressionNode expr = parsePrimary();

        while (match(EXPR_PIPE)) {
            String filterName = expect(EXPR_IDENTIFIER).value();
            expr = new PipeNode(expr, filterName);
        }

        return expr;
    }

    /**
     * Parse primary expressions (literals, variables, property access)
     *
     * @return Expression node
     */
    private ExpressionNode parsePrimary() {
        // String literal
        if (match(EXPR_STRING))
            return new LiteralNode(previous().value());

        // Number literal
        if (match(EXPR_NUMBER)) {
            String value = previous().value();

            if (value.contains("."))
                return new LiteralNode(Double.parseDouble(value));
            else
                return new LiteralNode(Long.parseLong(value));
        }

        // Boolean literal
        if (match(EXPR_BOOLEAN))
            return new LiteralNode(Boolean.parseBoolean(previous().value()));

        // Variable with property access
        if (match(EXPR_VARIABLE)) {
            String varName = previous().value();
            ExpressionNode expr = new VariableNode(varName);

            while (match(EXPR_VARIABLE_DOT)) {
                String property = expect(EXPR_IDENTIFIER).value();
                expr = new PropertyAccessNode(expr, property);
            }

            return expr;
        }

        throw new RuntimeException("Unexpected token in expression: " + current());
    }

    /**
     * Parse a block (if, each, etc.)
     *
     * @return AST node representing the block
     */
    private Node parseBlock() {
        expect(BLOCK_OPEN);

        if (match(BLOCK_IF))
            return parseIfBlock();
        else if (match(BLOCK_EACH))
            return parseEachBlock();

        throw new RuntimeException("Unknown block type: " + current());
    }

    /**
     * Parse an `if` block
     *
     * @return IfBlockNode
     */
    private IfBlockNode parseIfBlock() {
        ExpressionNode condition = parseExpressionContent();
        expect(EXPR_CLOSE);

        List<Node> thenBody = new ArrayList<>();
        while (!check(BLOCK_CLOSE) && !(check(BLOCK_OPEN, BLOCK_ELSE)))
            thenBody.add(parseNode());

        List<Node> elseBody = new ArrayList<>();
        if (check(BLOCK_OPEN)) {
            int savedPos = pos;
            advance(); // Skip BLOCK_OPEN

            if (check(BLOCK_ELSE)) {
                advance(); // Skip EXPR_ELSE
                expect(EXPR_CLOSE);

                while (!check(BLOCK_CLOSE))
                    elseBody.add(parseNode());
            } else
                pos = savedPos;
        }

        expect(BLOCK_CLOSE, BLOCK_IF, EXPR_CLOSE);

        return new IfBlockNode(condition, thenBody, elseBody);
    }

    /**
     * Parse an `each` block
     *
     * @return EachBlockNode
     */
    private EachBlockNode parseEachBlock() {
        // Syntaxe : {{#each $collection}} or {{#each $collection customName}}
        // Optional name, default to "item"

        ExpressionNode collection = parseExpressionContent();

        String itemName = "item";
        if (check(EXPR_IDENTIFIER))
            itemName = expect(EXPR_IDENTIFIER).value();

        expect(EXPR_CLOSE);

        List<Node> body = new ArrayList<>();
        while (!check(BLOCK_CLOSE))
            body.add(parseNode());

        expect(BLOCK_CLOSE, BLOCK_EACH, EXPR_CLOSE);

        return new EachBlockNode(itemName, collection, body);
    }

    // ===== Helpers =====

    /**
     * Get the current token
     */
    private Token current() {
        return tokens.get(pos);
    }

    /**
     * Get the previous token
     */
    private Token previous() {
        return tokens.get(pos - 1);
    }

    /**
     * Get the next token
     */
    private Token next() {
        return tokens.get(pos + 1);
    }

    /**
     * Consume the current token and return it
     */
    private Token advance() {
        if (!isAtEnd()) pos++;
        return previous();
    }

    /**
     * If the current token matches any of the given types, consume it and return true.
     * Otherwise, return false.
     */
    private boolean match(Token.Type... types) {
        for (Token.Type type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    /**
     * Check if token matches the given types, without consuming them.
     * Starts from the current token and checks each type in order.
     */
    private boolean check(Token.Type... types) {
        var index = pos;
        for (Token.Type type : types) {
            if (tokens.get(index++).type() != type)
                return false;
        }

        return true;
    }

    /**
     * Except the tokens to match the given types, consuming them.
     * Starts from the current token and checks each type in order.
     *
     * @throws RuntimeException if any of the expected types do not match
     */
    private Token expect(Token.Type... types) {
        Token token = current();
        for (Token.Type type : types) {
            if (check(type))
                token = advance();
            else
                throw new RuntimeException("Expected " + type + " but got " + current().type() + " at position " + current().position());
        }

        return token;
    }

    /**
     * Check if we have reached the end of the token list
     */
    private boolean isAtEnd() {
        return current().type() == GLOBAL_EOF;
    }
}
