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
import au.ellie.hyui.html.template.item.Attribute;
import au.ellie.hyui.html.template.item.Attribute.ConditionAttribute;
import au.ellie.hyui.html.template.item.Attribute.ControlAttribute;
import au.ellie.hyui.html.template.item.Attribute.ElseAttribute;
import au.ellie.hyui.html.template.item.Attribute.ElseIfAttribute;
import au.ellie.hyui.html.template.item.Attribute.ParsedAttributes;
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
        groupConditionalChains(nodes);
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
            if (consumeSymbol(Type.KEYWORD, KEYWORD_IF))
                return parseIfBlock();
            else if (consumeSymbol(Type.KEYWORD, KEYWORD_EACH))
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
        // Parse control attribute
        var control = parseIfAttributeValue(Type.CLOSE_EXPRESSION);
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
        var indent = cleanStandaloneLineWhitespace();
        var node = new IfBlockNode(control.condition(), mergeTextNodes(thenBody), mergeTextNodes(elseBody));

        stack.pop();
        return indent ? new MarkerNode(NEW_LINE, node) : node;
    }

    /**
     * Parse a loop block
     * <pre>
     *   {{#each $item in $list}}...{{/each}}
     * </pre>
     */
    private Node parseEachBlock() {
        // Parse control attribute
        var control = parseEachAttributeValue(Type.CLOSE_EXPRESSION);
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
        var indent = cleanStandaloneLineWhitespace();
        var node = new EachBlockNode(control.itemName(), control.indexName(), control.collection(), mergeTextNodes(body));

        stack.pop();
        return indent ? new MarkerNode(NEW_LINE, node) : node;
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

        // Parse attributes (including control flow attributes)
        var parsed = parseAttributes();
        var attributes = parsed.attributes();

        // Check for self-closing tag
        if (consume(Type.SLASH)) {
            expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>' after '/'");

            return parsed.build(
                    new ComponentBlockNode(tagName, attributes, List.of())
            );
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

        return parsed.build(
                new ComponentBlockNode(tagName, attributes, mergeTextNodes(children))
        );
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

        var parsed = parseAttributes();
        var attributes = parsed.attributes();

        // Check for self-closing
        if (consume(Type.SLASH)) {
            expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>' after '/'");
            return parsed.build(
                    new SlotBlockNode(slotName, attributes, List.of(), true)
            );
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

        return parsed.build(
                new SlotBlockNode(slotName, attributes, mergeTextNodes(children), true)
        );
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

        var parsed = parseAttributes();
        var attributes = parsed.attributes();

        // Check for self-closing
        if (consume(Type.SLASH)) {
            expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>'");
            return parsed.build(
                    new SlotBlockNode(slotName, attributes, List.of(), false)
            );
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

        return parsed.build(
                new SlotBlockNode(slotName, attributes, mergeTextNodes(children), false)
        );
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
     * Parse tag attributes and extract control flow attributes (each, if)
     */
    private ParsedAttributes parseAttributes() {
        var attributes = new ArrayList<AttributeValueNode>();
        List<Attribute> flowAttributes = new ArrayList<>();
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

                // Flow attributes: each="$items itemName"
                if (name.equals(KEYWORD_EACH) && consume(Type.QUOTE)) {
                    flowAttributes.add(parseEachAttributeValue(Type.QUOTE));
                    continue;
                }

                // Flow attributes: if="$condition"
                if (name.equals(KEYWORD_IF) && consume(Type.QUOTE)) {
                    flowAttributes.add(parseIfAttributeValue(Type.QUOTE));
                    continue;
                }

                // Flow attributes: else-if="$condition"
                if (name.equals(KEYWORD_ELSE_IF) && consume(Type.QUOTE)) {
                    flowAttributes.add(parseElseIfAttributeValue(Type.QUOTE));
                    continue;
                }

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
            else {
                // Flow attributes: else (flag attribute)
                if (name.equals(KEYWORD_ELSE)) {
                    flowAttributes.add(new ElseAttribute());
                } else {
                    attributes.add(new FlagAttributeNode(name));
                }
            }

            skipWhitespace();
        }

        return new ParsedAttributes(attributes, flowAttributes);
    }

    /**
     * Parse the value of an "each" block
     * Supports three syntaxes:
     * - each="$items" (item name defaults to "item", no index)
     * - each="$item in $items" (custom item name, no index)
     * - each="$item, $index in $items" (custom item name and index name)
     *
     * @param delimiter The expected delimiter type to end the attribute value
     */
    private ControlAttribute parseEachAttributeValue(Type delimiter) {
        skipWhitespace();

        // Check if we have the old syntax: "{{each $collection itemName}}" or new syntax
        var firstToken = peek();
        var itemName = "item";
        var indexName = (String) null;
        ExpressionNode collection;

        // Try to detect if this is "$item in $collection" or "$item, $index in $collection" syntax
        if (match(Type.VARIABLE)) {
            var firstVar = parseVariable();
            skipWhitespace();

            // Check for comma (indicates index is present)
            if (consume(Type.COMMA)) {
                skipWhitespace();

                // Parse index variable
                if (!match(Type.VARIABLE))
                    throw new ParserException("Expected index variable after comma in each block", peek(), pos);

                var indexVar = parseVariable();
                if (!(indexVar instanceof VariableNode indexVarNode))
                    throw new ParserException("Expected variable for index in each block", peek(), pos);

                indexName = indexVarNode.name();
                skipWhitespace();

                // Expect "in" keyword
                if (!consumeSymbol(Type.KEYWORD, KEYWORD_IN))
                    throw new ParserException("Expected 'in' keyword in each block", peek(), pos);

                skipWhitespace();

                // Parse collection
                collection = parseExpression();

                // Extract item name from first variable
                if (!(firstVar instanceof VariableNode itemVarNode))
                    throw new ParserException("Expected variable for item in each block", peek(), pos);
                itemName = itemVarNode.name();
            }
            // Check for "in" keyword
            else if (consumeSymbol(Type.KEYWORD, KEYWORD_IN)) {
                skipWhitespace();

                // Parse collection
                collection = parseExpression();

                // Extract item name from first variable
                if (!(firstVar instanceof VariableNode itemVarNode))
                    throw new ParserException("Expected variable for item in each block", peek(), pos);
                itemName = itemVarNode.name();
            }
            // Old syntax or just collection: "{{each $collection}}" or "{{each $collection itemName}}"
            else {
                collection = firstVar;

                // Check for optional item name (old syntax)
                if (match(Type.TEXT)) {
                    itemName = joinTokens(Type.TEXT, Type.NUMBER, Type.KEYWORD);
                    skipWhitespace();
                }
            }
        } else {
            throw new ParserException("Expected variable in each block", peek(), pos);
        }

        skipWhitespace();
        expect(delimiter, "Expected delimiter around `each` block");
        return new ControlAttribute(collection, itemName, indexName);
    }

    /**
     * Parse the value of an "if" block
     *
     * @param delimiter The expected delimiter type to end the attribute value
     */
    private ConditionAttribute parseIfAttributeValue(Type delimiter) {
        skipWhitespace();

        // Parse condition expression
        var condition = parseExpression();
        skipWhitespace();

        expect(delimiter, "Expected delimiter around `if` block");
        return new ConditionAttribute(condition);
    }

    /**
     * Parse the value of an "else-if" block
     *
     * @param delimiter The expected delimiter type to end the attribute value
     */
    private ElseIfAttribute parseElseIfAttributeValue(Type delimiter) {
        skipWhitespace();

        // Parse condition expression
        var condition = parseExpression();
        skipWhitespace();

        expect(delimiter, "Expected delimiter around `else-if` block");
        return new ElseIfAttribute(condition);
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
     * Group consecutive if/else-if/else elements into proper conditional chains.
     * This ensures that only the first matching condition renders.
     * Also recursively processes nested blocks.
     */
    private void groupConditionalChains(List<Node> nodes) {
        if (nodes.isEmpty())
            return;

        var result = new ArrayList<Node>();
        var i = 0;

        while (i < nodes.size()) {
            var node = nodes.get(i);

            // Recursively process nested blocks first
            node = processNodeRecursively(node);

            // Check if this is the start of an if/else-if/else chain
            if (isIfNode(node)) {
                var chain = new ArrayList<Node>();
                chain.add(node);
                i++;

                // Collect consecutive else-if and else nodes
                while (i < nodes.size()) {
                    var next = nodes.get(i);

                    // Skip whitespace/newline text nodes between conditional elements
                    if (next instanceof TextNode textNode && textNode.content().trim().isEmpty()) {
                        i++;
                        continue;
                    }

                    // Recursively process before checking
                    next = processNodeRecursively(next);

                    if (isElseIfNode(next) || isElseNode(next)) {
                        chain.add(next);
                        i++;
                    } else {
                        break;
                    }
                }

                // If we have a chain (if followed by else-if/else), group them
                if (chain.size() > 1) {
                    result.add(buildConditionalChain(chain));
                } else {
                    result.add(node);
                }
            } else {
                result.add(node);
                i++;
            }
        }

        nodes.clear();
        nodes.addAll(result);
    }

    /**
     * Recursively process a node to group conditional chains in nested blocks
     */
    private Node processNodeRecursively(Node node) {
        return switch (node) {
            case IfBlockNode ifNode -> {
                var thenBody = new ArrayList<>(ifNode.thenBody());
                var elseBody = new ArrayList<>(ifNode.elseBody());
                groupConditionalChains(thenBody);
                groupConditionalChains(elseBody);
                yield new IfBlockNode(ifNode.condition(), thenBody, elseBody);
            }
            case EachBlockNode eachNode -> {
                var body = new ArrayList<>(eachNode.body());
                groupConditionalChains(body);
                yield new EachBlockNode(eachNode.itemName(), eachNode.indexName(), eachNode.collection(), body);
            }
            case ComponentBlockNode componentNode -> {
                var children = new ArrayList<>(componentNode.children());
                groupConditionalChains(children);
                yield new ComponentBlockNode(componentNode.tag(), componentNode.attributes(), children);
            }
            case SlotBlockNode slotNode -> {
                var children = new ArrayList<>(slotNode.children());
                groupConditionalChains(children);
                yield new SlotBlockNode(slotNode.name(), slotNode.attributes(), children, slotNode.output());
            }
            default -> node;
        };
    }

    /**
     * Check if a node is an if block (wrapping an element with if attribute)
     */
    private boolean isIfNode(Node node) {
        return node instanceof IfBlockNode ifNode &&
               ifNode.elseBody().isEmpty() &&
               ifNode.thenBody().size() == 1;
    }

    /**
     * Check if a node is an else-if block (wrapping an element with else-if attribute)
     */
    private boolean isElseIfNode(Node node) {
        // else-if is represented as an IfBlockNode created from ElseIfAttribute
        return node instanceof IfBlockNode ifNode &&
               ifNode.elseBody().isEmpty() &&
               ifNode.thenBody().size() == 1;
    }

    /**
     * Check if a node is an else block (wrapping an element with else attribute)
     */
    private boolean isElseNode(Node node) {
        // else is represented as an IfBlockNode with condition=true
        if (!(node instanceof IfBlockNode ifNode))
            return false;

        return ifNode.condition() instanceof LiteralNode literal &&
               Boolean.TRUE.equals(literal.value()) &&
               ifNode.elseBody().isEmpty() &&
               ifNode.thenBody().size() == 1;
    }

    /**
     * Build a proper conditional chain from a list of if/else-if/else nodes
     */
    private Node buildConditionalChain(List<Node> chain) {
        if (chain.isEmpty())
            throw new IllegalArgumentException("Chain cannot be empty");

        // Start from the end and build backwards
        Node result = null;

        for (int i = chain.size() - 1; i >= 0; i--) {
            var node = chain.get(i);

            if (!(node instanceof IfBlockNode ifNode))
                continue;

            if (i == 0) {
                // First node (the if) - use its condition and set the elseBody to the accumulated chain
                List<Node> thenBody = new ArrayList<>(ifNode.thenBody());
                List<Node> elseBody = result != null ? List.of(result) : List.of();
                result = new IfBlockNode(ifNode.condition(), thenBody, elseBody);
            } else {
                // else-if or else nodes - wrap in if block with accumulated chain as else
                List<Node> thenBody = new ArrayList<>(ifNode.thenBody());
                List<Node> elseBody = result != null ? List.of(result) : List.of();
                result = new IfBlockNode(ifNode.condition(), thenBody, elseBody);
            }
        }

        return result;
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