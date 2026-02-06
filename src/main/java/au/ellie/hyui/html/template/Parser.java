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

import au.ellie.hyui.html.TemplateProcessor.CachedComponent;
import au.ellie.hyui.html.template.item.Node;
import au.ellie.hyui.html.template.item.Node.AttributeValueNode;
import au.ellie.hyui.html.template.item.Node.AttributeValueNode.DynamicAttributeNode;
import au.ellie.hyui.html.template.item.Node.AttributeValueNode.ExpressionAttributeNode;
import au.ellie.hyui.html.template.item.Node.AttributeValueNode.FlagAttributeNode;
import au.ellie.hyui.html.template.item.Node.AttributeValueNode.StaticAttributeNode;
import au.ellie.hyui.html.template.item.Node.BlockNode.EachBlockNode;
import au.ellie.hyui.html.template.item.Node.BlockNode.IfBlockNode;
import au.ellie.hyui.html.template.item.Node.ComponentElementNode;
import au.ellie.hyui.html.template.item.Node.ExpressionNode;
import au.ellie.hyui.html.template.item.Node.ExpressionNode.BinaryOpNode;
import au.ellie.hyui.html.template.item.Node.ExpressionNode.DefaultNode;
import au.ellie.hyui.html.template.item.Node.ExpressionNode.LiteralNode;
import au.ellie.hyui.html.template.item.Node.ExpressionNode.PipeNode;
import au.ellie.hyui.html.template.item.Node.ExpressionNode.PropertyAccessNode;
import au.ellie.hyui.html.template.item.Node.ExpressionNode.TextNode;
import au.ellie.hyui.html.template.item.Node.ExpressionNode.VariableNode;
import au.ellie.hyui.html.template.item.Symbols;
import au.ellie.hyui.html.template.item.Token;
import au.ellie.hyui.html.template.item.Token.Type;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static au.ellie.hyui.html.template.item.Token.Type.*;

public class Parser {
    private final Map<String, CachedComponent> components;
    private final Stack<Token> context;
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens, Map<String, CachedComponent> componentCache) {
        this.components = componentCache;
        this.context = new Stack<>();
        this.tokens = tokens;
    }

    /**
     * Parse the list of tokens into an AST
     *
     * @return List of AST nodes
     */
    public List<Node> parse() {
        List<Node> nodes = new ArrayList<>();

        while (!isAtEnd()) {
            var node = parseNode();
            if (node != null)
                nodes.add(node);
        }

        return nodes;
    }

    /**
     * Parse a single AST node
     *
     * @return AST node
     */
    private Node parseNode() {
        var token = current();

        return switch (token.type()) {
            case TEXT -> parseText();
            case EXPRESSION_OPEN -> parseExpressionOrBlock();
            case COMPONENT_OPEN -> parseComponentElement();
            case ATTRIBUTE -> parseAttribute();
            default -> throw new RuntimeException("Unexpected token: " + token);
        };
    }

    // ===== Primary Expression =====

    /**
     * Parse a text that represents value we ignore here
     *
     * @return TextNode
     */
    private TextNode parseText() {
        return new TextNode(expect(TEXT).value());
    }

    /**
     * Parse primary expressions (literals, variables, property access)
     *
     * @return Expression node
     */
    private ExpressionNode parsePrimary() {
        // String literal
        if (consume(STRING))
            return new LiteralNode(previous().value());

        // Number literal
        if (consume(NUMBER)) {
            var value = previous().value();

            if (value.contains("."))
                return new LiteralNode(Double.parseDouble(value));
            else
                return new LiteralNode(Long.parseLong(value));
        }

        // Boolean literal
        if (consume(BOOLEAN))
            return new LiteralNode(Boolean.parseBoolean(previous().value()));

        // Variable with property access
        if (consume(VARIABLE)) {
            var name = previous().value();

            ExpressionNode expr = new VariableNode(name);
            while (consume(VARIABLE_DOT)) {
                var property = expect(IDENTIFIER).value();
                expr = new PropertyAccessNode(expr, property);
            }

            return expr;
        }

        throw new RuntimeException("Unexpected token in expression: " + current());
    }

    // ===== Logical Expression =====

    /**
     * Parse logical `OR` expressions
     *
     * @return Expression node
     */
    private ExpressionNode parseOr() {
        var left = parseAnd();

        while (consume(OPERATOR, Symbols.OR)) {
            var right = parseAnd();
            left = new BinaryOpNode(left, Symbols.OR, right);
        }

        return left;
    }

    /**
     * Parse logical `AND` expressions
     *
     * @return Expression node
     */
    private ExpressionNode parseAnd() {
        var left = parseComparison();

        while (consume(OPERATOR, Symbols.AND)) {
            var right = parseComparison();
            left = new BinaryOpNode(left, Symbols.AND, right);
        }

        return left;
    }

    /**
     * Parse `comparison` expressions
     *
     * @return Expression node
     */
    private ExpressionNode parseComparison() {
        var left = parsePipe();

        var operation = get(COMPARATOR, Symbols.COMPARATORS);
        if (operation != null) {
            var right = parsePipe();
            return new BinaryOpNode(left, operation.value(), right);
        }

        return left;
    }

    /**
     * Parse `pipe` expressions
     *
     * @return Expression node
     */
    private ExpressionNode parsePipe() {
        var expr = parsePrimary();

        while (consume(OPERATOR, Symbols.PIPE)) {
            var name = expect(IDENTIFIER).value();
            expr = new PipeNode(expr, name);
        }

        return expr;
    }

    /**
     * Parse `nullish` coalescing expressions
     *
     * @return Expression node
     */
    private ExpressionNode parseNullish() {
        var alternatives = new ArrayList<ExpressionNode>();

        do {
            alternatives.add(parseOr());
        } while (consume(OPERATOR, Symbols.NULL_COALESCING));

        return alternatives.size() == 1 ? alternatives.getFirst() : new DefaultNode(alternatives);
    }

    // ===== Expression and Block =====

    /**
     * Parse either an expression or a block
     *
     * @return AST node representing the expression or block
     */
    private Node parseExpressionOrBlock() {
        expect(EXPRESSION_OPEN);
        Node node;

        if (consume(BLOCK_HEAD))
            node = parseBlock();
        else
            node = parseExpression();

        expect(EXPRESSION_CLOSE);
        return node;
    }

    /**
     * Parse an expression
     *
     * @return AST node representing the expression
     */
    private ExpressionNode parseExpression() {
        return parseNullish();
    }

    /**
     * Parse a block (if, each, etc.)
     *
     * @return AST node representing the block
     */
    private Node parseBlock() {
        var token = current();

        return switch (token.value()) {
            case Symbols.SECTION_IF -> parseIfBlock();
            case Symbols.SECTION_EACH -> parseEachBlock();
            default ->
                    throw new RuntimeException("Unknown block value \"" + token.value() + "\" for token " + token.type());
        };
    }

    /**
     * Parse an `if` block
     * <pre>{@code
     * {{#if condition}}
     *   ...
     * {{else}}
     *   ...
     * {{/if}}
     * }</pre>
     */
    private IfBlockNode parseIfBlock() {
        expect(IDENTIFIER, Symbols.SECTION_IF);
        var condition = parseExpression();
        expect(EXPRESSION_CLOSE);

        var thenBody = new ArrayList<Node>();
        while (!(peek(EXPRESSION_OPEN) && (next().match(BLOCK_TAIL) || next().match(IDENTIFIER, Symbols.SECTION_ELSE))))
            thenBody.add(parseNode());

        expect(EXPRESSION_OPEN);

        var elseBody = new ArrayList<Node>();
        if (consume(IDENTIFIER, Symbols.SECTION_ELSE)) {
            expect(EXPRESSION_CLOSE);

            while (!(peek(EXPRESSION_OPEN) && next().match(BLOCK_TAIL)))
                elseBody.add(parseNode());

            expect(EXPRESSION_OPEN);
        }

        expect(BLOCK_TAIL);
        expect(IDENTIFIER, Symbols.SECTION_IF);
        return new IfBlockNode(condition, thenBody, elseBody);
    }

    /**
     * Parse an `each` block.
     * <pre>{@code
     * {{#each $collection <item>}}
     *   ...
     * {{/each}}
     * }</pre>
     */
    private EachBlockNode parseEachBlock() {
        expect(IDENTIFIER, Symbols.SECTION_EACH);
        var collection = parseExpression();

        var itemName = "item";
        if (peek(IDENTIFIER))
            itemName = expect(IDENTIFIER).value();

        expect(EXPRESSION_CLOSE);

        var body = new ArrayList<Node>();
        while (!(peek(EXPRESSION_OPEN) && next().match(BLOCK_TAIL)))
            body.add(parseNode());

        expect(EXPRESSION_OPEN);
        expect(BLOCK_TAIL);
        expect(IDENTIFIER, Symbols.SECTION_EACH);
        return new EachBlockNode(itemName, collection, body);
    }

    // ===== Component =====

    /**
     * Parse an component element
     */
    private ComponentElementNode parseComponentElement() {
        expect(COMPONENT_OPEN, Symbols.COMPONENT_START);
        var identifier = expect(IDENTIFIER);
        context.push(identifier);

        // Parse attributes
        var attributes = new LinkedList<AttributeValueNode>();

        try {
            while (!peek(COMPONENT_CLOSE) && !isAtEnd()) {
                var attribute = parseAttribute();
                if (attribute instanceof AttributeValueNode attrNode)
                    attributes.add(attrNode);
                else {

                }
            }
        } finally {
            context.pop();
        }

        // Check for self-closing or regular close
        var selfClosing = expect(COMPONENT_CLOSE).match(Symbols.COMPONENT_SELF_CLOSE);
        var children = selfClosing ? new ArrayList<Node>() : parseHtmlChildren(identifier.value());

        return new ComponentElementNode(identifier.value(), attributes, children);
    }

    /**
     * Parse an HTML attribute
     */

    private Node parseAttribute() {
        var context = this.context.peek();
        if (context == null)
            throw new RuntimeException("No HTML tag context for attribute at position " + pos);

        var attribute = get(ATTRIBUTE);
        if (attribute != null) {
            var name = attribute.value();
            if (!peek(ASSIGN))
                return new FlagAttributeNode(name);

            expect(ASSIGN);

            var token = get(STRING);
            if (token != null) {
                return new StaticAttributeNode(name, token.value());
            } else if (consume(EXPRESSION_OPEN)) {
                var expr = parseExpression();
                expect(EXPRESSION_CLOSE);

                return new DynamicAttributeNode(name, expr);
            }

            throw new RuntimeException("Expected attribute value at position " + current().position());
        } else if (peek(EXPRESSION_OPEN))
            return new ExpressionAttributeNode(parseExpressionOrBlock());

        throw new RuntimeException("Unexpected token in tag <" + context.value() + ">: " + current());
    }

    /**
     * Parse the children of an HTML element until the closing tag
     */
    private List<Node> parseHtmlChildren(String parentTag) {
        var children = new ArrayList<Node>();

        while (!isAtEnd()) {
            var savedPos = pos;

            // Detect closing tag
            if (consume(COMPONENT_OPEN, Symbols.COMPONENT_CLOSE) && consume(IDENTIFIER, parentTag)) {
                expect(COMPONENT_CLOSE);
                return children;
            } else
                pos = savedPos;

            // Parse children
            Node child = parseNode();
            if (child != null)
                children.add(child);
        }

        throw new RuntimeException("Unclosed tag: " + parentTag + " at position " + tokens.getLast().position());
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
    private Token skip() {
        if (!isAtEnd()) pos++;
        return current();
    }

    /**
     * If the current token matches the given type and value, return true.
     * Otherwise, return false.
     *
     * @param type   The token type to check
     * @param values The token values to check
     */
    private boolean peek(Type type, String... values) {
        return current().match(type, values);
    }

    /**
     * If the current token matches the given type, consume it and return true.
     * Otherwise, return false.
     *
     * @param type   The token type to check
     * @param values The token values to check
     */
    private boolean consume(Type type, String... values) {
        if (current().match(type, values)) {
            skip();
            return true;
        }

        return false;
    }

    /**
     * If the current token matches any of the values in the given types, consume it and return the value.
     * Otherwise, return null.
     *
     * @param type   The token type to check
     * @param values The token values to check
     */
    private Token get(Type type, String... values) {
        var token = current();
        if (token.match(type, values)) {
            skip();
            return token;
        }

        return null;
    }

    /**
     * Except the token to match the given type and any of the given values, consuming it.
     *
     * @throws RuntimeException if the expected type or value do not match
     */
    private Token expect(Type type, String... values) {
        var token = current();
        if (token.match(type, values)) {
            skip();
            return token;
        }

        throw new RuntimeException("Expected " + type + (values.length > 0 ? " with value \"" + String.join("/", values) + "\"" : "") + " but got " + current().type() + " with value " + current().value() + " at index " + pos);
    }

    /**
     * Check if we have reached the end of the token list
     */
    private boolean isAtEnd() {
        return current().match(EOF);
    }
}
