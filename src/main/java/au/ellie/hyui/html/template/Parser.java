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

import au.ellie.hyui.html.template.exception.ParserException;
import au.ellie.hyui.html.template.item.Node;
import au.ellie.hyui.html.template.item.Node.AttributeValueNode;
import au.ellie.hyui.html.template.item.Node.AttributeValueNode.DynamicAttributeNode;
import au.ellie.hyui.html.template.item.Node.AttributeValueNode.ExpressionAttributeNode;
import au.ellie.hyui.html.template.item.Node.AttributeValueNode.FlagAttributeNode;
import au.ellie.hyui.html.template.item.Node.AttributeValueNode.MixedAttributeNode;
import au.ellie.hyui.html.template.item.Node.BlockNode.ComponentBlockNode;
import au.ellie.hyui.html.template.item.Node.BlockNode.EachBlockNode;
import au.ellie.hyui.html.template.item.Node.BlockNode.IfBlockNode;
import au.ellie.hyui.html.template.item.Node.BlockNode.SlotBlockNode;
import au.ellie.hyui.html.template.item.Node.ExpressionNode;
import au.ellie.hyui.html.template.item.Node.ExpressionNode.*;
import au.ellie.hyui.html.template.item.Node.MarkerNode;
import au.ellie.hyui.html.template.item.Token;
import au.ellie.hyui.html.template.item.Token.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static au.ellie.hyui.html.template.item.Symbols.*;

public class Parser {
    private final Stack<List<Node>> stack = new Stack<>();
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Parse the tokens into an AST
     */
    public List<Node> parse() {
        var nodes = new ArrayList<Node>();
        stack.push(nodes);

        while (!isAtEnd()) {
            var node = parseNode();
            if (node != null)
                nodes.add(node);
        }

        mergeTextNodes(nodes);
        return stack.pop();
    }

    /**
     * Parse and return the next node from the tokens list
     * Unknown tokens are parsed as text nodes
     */
    private Node parseNode() {
        // Handle mustache expressions
        if (match(Type.OPEN_EXPRESSION))
            return parseMustacheExpression();

        // Handle HTML/component tags
        if (match(Type.OPEN_ANGLE_BRACKET))
            return parseTag();

        // Others tokens are treated as text
        if (!isAtEnd())
            return new TextNode(advance().value());

        return null;
    }

    /**
     * Parse mustache expression
     * <pre>
     *   {{if}}, {{$var}}, {{expr}}
     * </pre>
     */
    private Node parseMustacheExpression() {
        advance(); // consume {{
        skipWhitespace();

        // Parse control flow blocks
        if (match(Type.KEYWORD)) {
            if (matchSymbol(Type.KEYWORD, KEYWORD_IF))
                return parseIfBlock();
            else if (matchSymbol(Type.KEYWORD, KEYWORD_EACH))
                return parseEachBlock();
        }

        // Parse expression
        var expr = parseExpression();

        skipWhitespace();
        expect(Type.CLOSE_EXPRESSION, "Expected '}}' after expression");

        return expr;
    }

    /**
     * Parse a conditional block
     * <pre>
     *   {{#if condition}}...{{else}}...{{/if}}
     * </pre>
     */
    private Node parseIfBlock() {
        advance(); // consume 'if'
        skipWhitespace();

        var condition = parseExpression();

        skipWhitespace();
        expect(Type.CLOSE_EXPRESSION, "Expected '}}' after if condition");

        // Clean whitespace for standalone tags
        cleanStandaloneLineWhitespace();

        // Parse then body
        var thenBody = new ArrayList<Node>();
        var elseBody = new ArrayList<Node>();
        var elseDef = false;

        stack.push(thenBody);
        while (!isAtEnd()) {
            if (consume(Type.OPEN_EXPRESSION)) {
                int savedPos = pos - 1;
                skipWhitespace();

                // Looking for else token
                if (consumeSymbol(Type.KEYWORD, KEYWORD_ELSE)) {
                    skipWhitespace();
                    expect(Type.CLOSE_EXPRESSION, "Expected '}}' after else");

                    // Clean whitespace for standalone tags
                    cleanStandaloneLineWhitespace();

                    elseDef = true;
                    stack.pop();

                    break;
                }

                // Looking for end token
                if (consume(Type.SLASH)) {
                    skipWhitespace();

                    if (consumeSymbol(Type.KEYWORD, KEYWORD_IF))
                        break;
                }

                pos = savedPos;
            }

            var node = parseNode();
            if (node != null)
                thenBody.add(node);
        }

        // Parse else body (if it exists)
        if (elseDef) {
            stack.push(elseBody);
            while (!isAtEnd()) {
                // Looking for end token
                if (consume(Type.OPEN_EXPRESSION)) {
                    int savedPos = pos - 1;
                    skipWhitespace();

                    if (consume(Type.SLASH)) {
                        skipWhitespace();

                        if (consumeSymbol(Type.KEYWORD, KEYWORD_IF))
                            break;
                    }

                    pos = savedPos;
                }

                var node = parseNode();
                if (node != null)
                    elseBody.add(node);
            }
        }

        skipWhitespace();
        expect(Type.CLOSE_EXPRESSION, "Expected '}}' after closing if tag");

        // Clean whitespace for standalone tags
        var clean = cleanStandaloneLineWhitespace();
        var node = new IfBlockNode(condition, mergeTextNodes(thenBody), mergeTextNodes(elseBody));

        stack.pop();
        return clean ? new MarkerNode(NEW_LINE, node) : node;
    }

    /**
     * Parse a loop block
     * <pre>
     *   {{#each $item in $list}}...{{/each}}
     * </pre>
     */
    private Node parseEachBlock() {
        advance(); // consume 'each'
        skipWhitespace();

        var collection = parseVariable();

        skipWhitespace();

        var itemName = "item";
        if (match(Type.TEXT)) {
            itemName = joinTokens(Type.TEXT, Type.NUMBER, Type.KEYWORD);
            skipWhitespace();
        }

        expect(Type.CLOSE_EXPRESSION, "Expected '}}' after each expression");

        // Clean whitespace for standalone tags
        cleanStandaloneLineWhitespace();

        // Parse body
        var body = new ArrayList<Node>();

        stack.push(body);
        while (!isAtEnd()) {
            if (consume(Type.OPEN_EXPRESSION)) {
                int savedPos = pos - 1;
                skipWhitespace();

                if (consume(Type.SLASH)) {
                    skipWhitespace();

                    if (consumeSymbol(Type.KEYWORD, KEYWORD_EACH))
                        break;
                }

                pos = savedPos;
            }

            var node = parseNode();
            if (node != null)
                body.add(node);
        }

        skipWhitespace();
        expect(Type.CLOSE_EXPRESSION, "Expected '}}' after closing each tag");

        // Clean whitespace for standalone tags
        var clean = cleanStandaloneLineWhitespace();
        var node = new EachBlockNode(itemName, collection, mergeTextNodes(body));

        stack.pop();
        return clean ? new MarkerNode(NEW_LINE, node) : node;
    }

    /**
     * Parse an expression (variable, property access, operators, etc.)
     */
    private ExpressionNode parseExpression() {
        return parseNullCoalescingExpression();
    }

    /**
     * Parse null coalescing expression
     */
    private ExpressionNode parseNullCoalescingExpression() {
        var alternatives = new ArrayList<ExpressionNode>();

        do {
            alternatives.add(parseOrExpression());
        } while (consumeSymbol(Type.OPERATOR, NULL_COALESCING));

        return alternatives.size() == 1 ? alternatives.getFirst() : new DefaultNode(alternatives);
    }

    /**
     * Parse OR expression
     */
    private ExpressionNode parseOrExpression() {
        var left = parseAndExpression();

        while (consumeSymbol(Type.OPERATOR, OR)) {
            var right = parseAndExpression();
            left = new BinaryOpNode(left, OR, right);
        }

        return left;
    }

    /**
     * Parse AND expression
     */
    private ExpressionNode parseAndExpression() {
        var left = parseComparisonExpression();

        while (consumeSymbol(Type.OPERATOR, AND)) {
            var right = parseComparisonExpression();
            left = new BinaryOpNode(left, AND, right);
        }

        return left;
    }

    /**
     * Parse comparison expression
     */
    private ExpressionNode parseComparisonExpression() {
        var left = parsePipeExpression();

        while (match(Type.COMPARATOR) || matchSymbol(Type.KEYWORD, KEYWORD_IN, KEYWORD_NOT_IN) || match(Type.CLOSE_ANGLE_BRACKET, Type.OPEN_ANGLE_BRACKET, Type.ASSIGN)) {
            var op = advance().value();
            if (consume(Type.ASSIGN))
                op += ASSIGN;

            var right = parsePipeExpression();
            left = new BinaryOpNode(left, op, right);
        }

        return left;
    }

    /**
     * Parse pipe expression
     */
    private ExpressionNode parsePipeExpression() {
        var expr = parsePrimaryExpression();
        skipWhitespace();

        while (consume(Type.PIPE)) {
            skipWhitespace();

            var token = expect(Type.TEXT, "Expected filter name after '|'");
            skipWhitespace();

            expr = new PipeNode(expr, token.value());
        }

        return expr;
    }

    /**
     * Parse primary expression (literals, variables, property access)
     */
    private ExpressionNode parsePrimaryExpression() {
        skipWhitespace();

        // Boolean literals true
        if (consumeSymbol(Type.KEYWORD, KEYWORD_TRUE))
            return new LiteralNode(true);

        // Boolean literals false
        if (consumeSymbol(Type.KEYWORD, KEYWORD_FALSE))
            return new LiteralNode(false);

        // Numbers
        if (match(Type.NUMBER))
            return parseNumberLiteral();

        // Backslash
        if (consume(Type.BACK_SLASH))
            return new LiteralNode(advance().value());

        // String literals
        if (match(Type.QUOTE))
            return parseStringLiteral();

        // Variables and property access
        if (match(Type.VARIABLE))
            return parseVariable();

        // Text
        if (match(Type.TEXT))
            return new LiteralNode(advance().value());

        throw new ParserException("Unexpected token in expression", peek(), pos);
    }

    /**
     * Parse a variable reference, including property access
     * <pre>
     *   $var
     *   $var.property
     *   $var.property.subproperty
     * </pre>
     */
    private ExpressionNode parseVariable() {
        advance(); // consume $

        var varName = joinTokens(Type.TEXT, Type.NUMBER, Type.COLON, Type.KEYWORD);
        if (varName.isEmpty())
            throw new ParserException("Expected variable name after '$'", peek(), pos);

        // Check for property access
        ExpressionNode expr = new VariableNode(varName);

        while (match(Type.DOT)) {
            advance(); // consume .

            var property = joinTokens(Type.TEXT, Type.NUMBER, Type.COLON, Type.KEYWORD);
            if (property.isEmpty())
                throw new ParserException("Expected property name after '.'", peek(), pos);

            expr = new PropertyAccessNode(expr, property);
        }

        return expr;
    }

    /**
     * Parse a number literal
     */
    private LiteralNode parseNumberLiteral() {
        var num = advance().value();
        if (num.contains("."))
            return new LiteralNode(Double.parseDouble(num));

        return new LiteralNode(Integer.parseInt(num));
    }

    /**
     * Parse a string literal
     */
    private LiteralNode parseStringLiteral() {
        advance(); // consume opening "

        var builder = new StringBuilder();
        while (!isAtEnd() && !match(Type.QUOTE)) {
            consume(Type.BACK_SLASH);
            builder.append(advance().value());
        }

        expect(Type.QUOTE, "Expected closing quote");

        return new LiteralNode(builder.toString());
    }

    /**
     * Parse HTML/component tags
     */
    private Node parseTag() {
        advance(); // consume <

        // Check for slot input syntax: <:name>
        if (match(Type.COLON))
            return parseSlotInput();

        // Check for closing tag
        if (match(Type.SLASH))
            return parseClosingTag();

        // Parse tag name
        var tagName = joinTokens(Type.TEXT, Type.NUMBER, Type.KEYWORD);
        if (tagName.isEmpty())
            throw new ParserException("Expected tag name after '<'", peek(), pos);

        // Check if it's a slot tag
        if (tagName.equals("slot"))
            return parseSlotOutput();

        // Parse attributes
        var attributes = parseAttributes();

        // Check for self-closing tag
        if (consume(Type.SLASH)) {
            expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>' after '/'");

            return new ComponentBlockNode(tagName, attributes, List.of());
        }

        expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>' after tag");

        // Parse children
        var children = new ArrayList<Node>();

        stack.push(children);
        while (!isAtEnd()) {
            // Check for closing tag
            if (consume(Type.OPEN_ANGLE_BRACKET)) {
                int savedPos = pos - 1;

                if (consume(Type.SLASH)) {
                    skipWhitespace();

                    if (joinTokens(Type.TEXT, Type.NUMBER, Type.KEYWORD).equals(tagName)) {
                        expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>' after closing tag");
                        break;
                    }
                }

                pos = savedPos;
            }

            var child = parseNode();
            if (child != null)
                children.add(child);
        }
        stack.pop();

        return new ComponentBlockNode(tagName, attributes, mergeTextNodes(children));
    }

    /**
     * Parse slot output: <slot> or <slot:name>
     */
    private Node parseSlotOutput() {
        String slotName = HTML_SLOT_DEFAULT;

        // Check for named slot: <slot:name>
        if (consume(Type.COLON)) {
            slotName = joinTokens(Type.TEXT, Type.NUMBER, Type.KEYWORD);
            if (slotName.isEmpty())
                expect(Type.TEXT, "Expected slot name after 'slot:'");
        }

        var attributes = parseAttributes();

        // Check for self-closing
        if (consume(Type.SLASH)) {
            expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>' after '/'");
            return new SlotBlockNode(slotName, attributes, List.of(), true);
        }

        expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>' after slot tag");

        // Parse default content
        var children = new ArrayList<Node>();

        stack.push(children);
        while (!isAtEnd()) {
            if (consume(Type.OPEN_ANGLE_BRACKET)) {
                int savedPos = pos - 1;

                if (consume(Type.SLASH)) {
                    skipWhitespace();

                    if (consumeSymbol(Type.TEXT, "slot")) {
                        if (consume(Type.COLON)) {
                            var closeName = joinTokens(Type.TEXT, Type.NUMBER, Type.KEYWORD);
                            if (closeName.equals(slotName)) {
                                skipWhitespace();
                                expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>'");
                                break;
                            }
                        } else {
                            skipWhitespace();
                            expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>'");
                            break;
                        }
                    }
                }

                pos = savedPos;
            }

            var child = parseNode();
            if (child != null)
                children.add(child);
        }
        stack.pop();

        return new SlotBlockNode(slotName, attributes, mergeTextNodes(children), true);
    }

    /**
     * Parse slot input: <:name>content</:name>
     */
    private Node parseSlotInput() {
        advance(); // consume :

        // Allow for <:> as default slot input with no name
        var slotName = joinTokens(Type.TEXT, Type.NUMBER, Type.KEYWORD);
        if (slotName.isEmpty())
            slotName = HTML_SLOT_DEFAULT;

        var attributes = parseAttributes();

        // Check for self-closing
        if (consume(Type.SLASH)) {
            expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>'");
            return new SlotBlockNode(slotName, attributes, List.of(), false);
        }

        expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>'");

        // Parse content
        var children = new ArrayList<Node>();

        stack.push(children);
        while (!isAtEnd()) {
            if (consume(Type.OPEN_ANGLE_BRACKET)) {
                int savedPos = pos - 1;

                if (consume(Type.SLASH)) {
                    skipWhitespace();

                    if (consume(Type.COLON)) {
                        var closeName = joinTokens(Type.TEXT, Type.NUMBER, Type.KEYWORD);
                        if (closeName.equals(slotName)) {
                            skipWhitespace();
                            expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>'");
                            break;
                        }
                    }
                }

                pos = savedPos;
            }

            var child = parseNode();
            if (child != null)
                children.add(child);
        }
        stack.pop();

        return new SlotBlockNode(slotName, attributes, mergeTextNodes(children), false);
    }

    /**
     * Parse closing tag (removed from the AST)
     */
    private Node parseClosingTag() {
        advance(); // consume /
        skipWhitespace();

        if (joinTokens(Type.TEXT, Type.NUMBER, Type.KEYWORD).isEmpty()) {
            if (consume(Type.COLON))
                joinTokens(Type.TEXT, Type.NUMBER, Type.KEYWORD);
        }

        expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>' after closing tag");
        return null;
    }

    /**
     * Parse tag attributes
     */
    private List<AttributeValueNode> parseAttributes() {
        var attributes = new ArrayList<AttributeValueNode>();
        skipWhitespace();

        while (!isAtEnd() && !match(Type.CLOSE_ANGLE_BRACKET, Type.SLASH)) {
            // Check for dynamic attribute with curly braces
            if (match(Type.OPEN_EXPRESSION)) {
                var expr = parseMustacheExpression();
                attributes.add(new ExpressionAttributeNode(expr));

                skipWhitespace();
                continue;
            }

            // Parse attribute name
            var name = joinTokens(Type.TEXT, Type.NUMBER, Type.KEYWORD);
            if (name.isEmpty())
                break;

            skipWhitespace();

            // Check for attribute value
            if (consume(Type.ASSIGN)) {
                skipWhitespace();

                // Dynamic attribute: attr={expr}
                if (consume(Type.OPEN_EXPRESSION)) {
                    var expr = parseExpression();

                    skipWhitespace();
                    expect(Type.CLOSE_EXPRESSION, "Expected '}}' after attribute expression");

                    attributes.add(new DynamicAttributeNode(name, expr));
                }

                // mixed attribute: attr="value {{$expr}} value"
                else if (consume(Type.QUOTE)) {
                    var parts = new ArrayList<>();
                    var builder = new StringBuilder();

                    while (!isAtEnd() && !match(Type.QUOTE)) {
                        if (consume(Type.OPEN_EXPRESSION)) {
                            if (!builder.isEmpty()) {
                                parts.add(builder.toString());
                                builder.setLength(0);
                            }

                            parts.add(parseExpression());

                            skipWhitespace();
                            expect(Type.CLOSE_EXPRESSION, "Expected '}}' in attribute value");
                        } else
                            builder.append(advance().value());
                    }

                    // Remaining static part
                    if (!builder.isEmpty())
                        parts.add(builder.toString());

                    expect(Type.QUOTE, "Expected closing quote");
                    attributes.add(new MixedAttributeNode(name, parts));
                }

                // Unquoted value
                else {
                    var value = joinTokens(Type.TEXT, Type.NUMBER, Type.KEYWORD);
                    attributes.add(new MixedAttributeNode(name, List.of(value)));
                }
            }

            // Flag attribute
            else
                attributes.add(new FlagAttributeNode(name));

            skipWhitespace();
        }

        return attributes;
    }

    // ===== Navigation =====

    private boolean isAtEnd() {
        return pos >= tokens.size() || peek().match(Type.EOI);
    }

    private Token peek() {
        return peek(pos);
    }

    private Token peek(int index) {
        if (index < 0 || index >= tokens.size())
            return null;

        return tokens.get(index);
    }

    private Token advance() {
        if (!isAtEnd())
            return tokens.get(pos++);

        return tokens.getLast();
    }

    private boolean consume(Type... type) {
        if (match(type)) {
            advance();
            return true;
        }

        return false;
    }

    private boolean consumeSymbol(Type type, String... symbols) {
        if (matchSymbol(type, symbols)) {
            advance();
            return true;
        }

        return false;
    }

    private boolean match(Type... types) {
        if (isAtEnd())
            return false;

        for (var type : types)
            if (peek().type() == type)
                return true;

        return false;
    }

    private boolean matchSymbol(Type type, String... symbols) {
        if (isAtEnd() || !match(type))
            return false;

        return peek().match(symbols);
    }

    private Token expect(Type type, String message) {
        if (!match(type))
            throw new ParserException(message, peek(), pos);

        return advance();
    }

    private Token expectSymbol(Type type, String symbol, String message) {
        if (!matchSymbol(type, symbol))
            throw new ParserException(message, peek(), pos);

        return advance();
    }

    private void skipWhitespace() {
        while (match(Type.SPACER))
            advance();
    }

    // ===== Helper =====

    /**
     * Remove whitespace for standalone block tags.
     * <p>
     * If a block tag ({{if}}, {{/if}}, etc.) is alone on a line,
     * remove the entire line including leading/trailing whitespace
     *
     * @return true if whitespace was removed, false otherwise
     */
    private boolean cleanStandaloneLineWhitespace() {
        var list = stack.peek();

        // Check previous nodes - look backwards for whitespace and newline
        // Stop if we find any non-whitespace text before the block tag on the same line
        var nodesToRemove = 0;
        for (int i = list.size() - 1; i >= 0; i--) {
            Node node = list.get(i);

            if (node instanceof TextNode(String content)) {
                if (content.matches("^[ \\t]+$"))
                    nodesToRemove++;
                else if (content.equals("\n"))
                    break;
                else
                    return false;
            } else if (node instanceof MarkerNode(String content, Node _)) {
                if (content.equals(NEW_LINE))
                    break;

                return false;
            } else
                return false;
        }

        var checkPos = pos;
        var tokensToSkip = 0;

        // Check after the block tag using the tokens
        while (checkPos < tokens.size()) {
            Token token = tokens.get(checkPos);

            if (token.match(Type.SPACER)) {
                tokensToSkip++;
                checkPos++;
            } else if (token.match(Type.NEW_LINE)) {
                tokensToSkip++;
                break;
            } else
                return false;
        }

        // Remove the nodes from the list
        for (var i = 0; i < nodesToRemove; i++)
            list.removeLast();

        // Skip the tokens after the block tag
        pos += tokensToSkip;
        return true;
    }

    /**
     * Join consecutive tokens of the given types into a single string
     */
    private String joinTokens(Type... tokens) {
        var builder = new StringBuilder();
        while (match(tokens))
            builder.append(advance().value());

        return builder.toString();
    }

    /**
     * Merge consecutive TextNodes in the given list into single TextNodes
     */
    @SuppressWarnings("unchecked")
    private <T extends Node> List<T> mergeTextNodes(List<T> nodes) {
        if (nodes.isEmpty())
            return nodes;

        var merged = new ArrayList<T>();
        var textBuilder = new StringBuilder();
        for (var node : nodes) {
            if (node instanceof TextNode(String content))
                textBuilder.append(content);
            else {
                if (!textBuilder.isEmpty()) {
                    merged.add((T) new TextNode(textBuilder.toString()));
                    textBuilder.setLength(0);
                }

                merged.add(node);
            }
        }

        if (!textBuilder.isEmpty())
            merged.add((T) new TextNode(textBuilder.toString()));

        nodes.clear();
        nodes.addAll(merged);

        return nodes;
    }
}