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
import au.ellie.hyui.html.template.item.Attribute.ParsedAttributes;
import au.ellie.hyui.html.template.item.Node;
import au.ellie.hyui.html.template.item.Node.AttributeValueNode;
import au.ellie.hyui.html.template.item.Node.AttributeValueNode.DynamicAttributeNode;
import au.ellie.hyui.html.template.item.Node.AttributeValueNode.ExpressionAttributeNode;
import au.ellie.hyui.html.template.item.Node.AttributeValueNode.FlagAttributeNode;
import au.ellie.hyui.html.template.item.Node.AttributeValueNode.MixedAttributeNode;
import au.ellie.hyui.html.template.item.Node.BlockNode.ComponentBlockNode;
import au.ellie.hyui.html.template.item.Node.BlockNode.ConditionalBlockNode;
import au.ellie.hyui.html.template.item.Node.BlockNode.ConditionalBlockNode.ConditionalBranch;
import au.ellie.hyui.html.template.item.Node.BlockNode.ForBlockNode;
import au.ellie.hyui.html.template.item.Node.BlockNode.SlotBlockNode;
import au.ellie.hyui.html.template.item.Node.ExpressionNode;
import au.ellie.hyui.html.template.item.Node.ExpressionNode.*;
import au.ellie.hyui.html.template.item.Node.MarkerNode;
import au.ellie.hyui.html.template.item.Token;
import au.ellie.hyui.html.template.item.Token.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Consumer;

import static au.ellie.hyui.html.template.item.Symbols.*;
import static au.ellie.hyui.utils.ObjectUtils.mutableListOf;

public class Parser {
    private final Stack<List<Node>> stack = new Stack<>();
    private final List<Token> tokens;
    private final String source;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this(tokens, null);
    }

    public Parser(List<Token> tokens, String source) {
        this.tokens = tokens;
        this.source = source;
    }

    /**
     * Parse the tokens into an AST
     */
    public List<Node> parse() {
        var nodes = new ArrayList<Node>();
        stack.push(nodes);

        while (hasNext()) {
            var node = parseNode();
            if (node != null)
                nodes.add(node);
        }

        postProcessNodes(nodes, List.of(
                this::optimizeTextNodes,
                this::optimizeConditionalChains
        ));

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
        if (hasNext())
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
                return parseConditionalBlock();
            else if (consumeSymbol(Type.KEYWORD, KEYWORD_FOR))
                return parseForBlock();
        }

        // Parse expression
        var expr = parseExpression(true);

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
    private Node parseConditionalBlock() {
        // Parse branches (if, else-if, else)
        var branches = parseConditionalBranch(KEYWORD_IF, null);

        skipWhitespace();
        expect(Type.CLOSE_EXPRESSION, "Expected '}}' after closing if tag");

        // Clean whitespace for standalone tags
        var indent = cleanStandaloneLineWhitespace();
        var node = new ConditionalBlockNode(KEYWORD_IF, branches);

        stack.pop();
        return indent ? new MarkerNode(NEW_LINE, node) : node;
    }

    /**
     * Recursively parse conditional branches (if, else-if, else) and return a list of branches in reverse order
     * This allows for proper nesting of else-if and else blocks within the AST
     *
     * @param keyword The keyword of the current branch being parsed (if, else-if, else)
     * @param list    The list to accumulate branches into (used for recursion)
     */
    private List<ConditionalBranch> parseConditionalBranch(String keyword, List<ConditionalBranch> list) {
        var branch = (String) null;
        if (list == null)
            list = new ArrayList<>();

        // Parse control attribute
        var control = parseConditionAttribute(keyword, Type.CLOSE_EXPRESSION);
        var body = new ArrayList<Node>();

        cleanStandaloneLineWhitespace();

        stack.push(body);
        while (hasNext()) {
            if (consume(Type.OPEN_EXPRESSION)) {
                int savedPos = pos - 1;
                skipWhitespace();

                // Looking for else token
                if (consumeSymbol(Type.KEYWORD, KEYWORD_ELSE)) {
                    branch = KEYWORD_ELSE;
                    skipWhitespace();

                    // Support else-if by checking for if after else
                    if (consumeSymbol(Type.KEYWORD, KEYWORD_IF)) {
                        branch = KEYWORD_ELSE_IF;
                        skipWhitespace();
                    }

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
                body.add(node);
        }

        if (branch != null) {
            stack.pop();
            parseConditionalBranch(branch, list);
        }

        list.addFirst(new ConditionalBranch(control.condition(), body));
        return list;
    }

    /**
     * Parse a loop block
     * <pre>
     *   {{#for $item in $list}}...{{/for}}
     * </pre>
     */
    private Node parseForBlock() {
        // Parse control attribute
        var control = parseControlAttribute(Type.CLOSE_EXPRESSION);
        cleanStandaloneLineWhitespace();

        // Parse body
        var body = new ArrayList<Node>();

        stack.push(body);
        while (hasNext()) {
            if (consume(Type.OPEN_EXPRESSION)) {
                int savedPos = pos - 1;
                skipWhitespace();

                if (consume(Type.SLASH)) {
                    skipWhitespace();

                    if (consumeSymbol(Type.KEYWORD, KEYWORD_FOR))
                        break;
                }

                pos = savedPos;
            }

            var node = parseNode();
            if (node != null)
                body.add(node);
        }

        skipWhitespace();
        expect(Type.CLOSE_EXPRESSION, "Expected '}}' after closing for tag");

        // Clean whitespace for standalone tags
        var indent = cleanStandaloneLineWhitespace();
        var node = new ForBlockNode(control.itemName(), control.indexName(), control.collection(), body);

        stack.pop();
        return indent ? new MarkerNode(NEW_LINE, node) : node;
    }

    /**
     * Parse an expression (variable, property access, operators, etc.)
     */
    private ExpressionNode parseExpression(boolean initial) {
        return parseNullCoalescingExpression(initial);
    }

    /**
     * Parse null coalescing expression
     */
    private ExpressionNode parseNullCoalescingExpression(boolean initial) {
        var alternatives = new ArrayList<ExpressionNode>();

        do {
            alternatives.add(parseOrExpression(initial));
            initial = false;
        } while (consumeSymbol(Type.OPERATOR, NULL_COALESCING));

        return alternatives.size() == 1 ? alternatives.getFirst() : new DefaultNode(alternatives);
    }

    /**
     * Parse OR expression
     */
    private ExpressionNode parseOrExpression(boolean initial) {
        var left = parseAndExpression(initial);

        while (consumeSymbol(Type.OPERATOR, OR)) {
            var right = parseAndExpression(initial);
            left = new BinaryOpNode(left, OR, right);
        }

        return left;
    }

    /**
     * Parse AND expression
     */
    private ExpressionNode parseAndExpression(boolean initial) {
        var left = parseComparisonExpression(initial);

        while (consumeSymbol(Type.OPERATOR, AND)) {
            var right = parseComparisonExpression(initial);
            left = new BinaryOpNode(left, AND, right);
        }

        return left;
    }

    /**
     * Parse comparison expression
     */
    private ExpressionNode parseComparisonExpression(boolean initial) {
        var left = parsePipeExpression(initial);

        while (matchAll(Type.EXCLAMATION, Type.ASSIGN) || match(Type.CLOSE_ANGLE_BRACKET, Type.OPEN_ANGLE_BRACKET, Type.ASSIGN) || matchSymbol(Type.KEYWORD, KEYWORD_IN, KEYWORD_NOT_IN)) {
            var op = advance().value();
            if (consume(Type.ASSIGN))
                op += ASSIGN;

            var right = parsePipeExpression(false);
            left = new BinaryOpNode(left, op, right);
        }

        return left;
    }

    /**
     * Parse pipe expression
     */
    private ExpressionNode parsePipeExpression(boolean initial) {
        var expr = parsePrimaryExpression(initial);
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
    private ExpressionNode parsePrimaryExpression(boolean initial) {
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
        if (consume(Type.BACK_SLASH)) {
            var token = peek();
            if (token.type() == Type.QUOTE || token.type() == Type.SINGLE_QUOTE)
                return parseStringLiteral(token.type(), true);

            return new LiteralNode(token.value());
        }

        // String literals with double quotes
        if (match(Type.QUOTE))
            return parseStringLiteral(Type.QUOTE, false);

        // String literals with single quotes
        if (match(Type.SINGLE_QUOTE))
            return parseStringLiteral(Type.SINGLE_QUOTE, false);

        // Variables and property access
        if (match(Type.VARIABLE, Type.EXCLAMATION))
            return parseVariable();

        // Text
        if (match(Type.TEXT) && !initial)
            return new LiteralNode(advance().value());

        throw error("Unexpected token in expression");
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
        var reversed = consume(Type.EXCLAMATION);
        advance(); // consume $

        var varName = joinTokens(Type.TEXT, Type.NUMBER, Type.COLON, Type.KEYWORD);
        if (varName.isEmpty())
            throw error("Expected variable name after '$'");

        // Check for property access
        ExpressionNode expr = new VariableNode(varName, reversed);

        while (match(Type.DOT)) {
            advance(); // consume .

            var property = joinTokens(Type.TEXT, Type.NUMBER, Type.COLON, Type.KEYWORD);
            if (property.isEmpty())
                throw error("Expected property name after '.'");

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
     *
     * @param quoteType The type of quote used (QUOTE or SINGLE_QUOTE)
     * @param escaped   Whether the string started with an escaped quote (\")
     */
    private LiteralNode parseStringLiteral(Type quoteType, boolean escaped) {
        advance(); // consume opening quote

        var builder = new StringBuilder();
        while (hasNext()) {
            if (matchAll(Type.BACK_SLASH, quoteType)) {
                advance(); // consume backslash
                if (escaped) {
                    advance(); // consume quote
                    break;
                }

                builder.append(advance().value());
            } else if (!escaped && match(quoteType)) {
                advance(); // consume quote
                break;
            } else
                builder.append(advance().value());
        }

        return new LiteralNode(builder.toString());
    }

    /**
     * Parse HTML/component tags
     */
    private Node parseTag() {
        advance(); // consume <

        // Html comments
        if (match(Type.EXCLAMATION, Type.MARKER_COMMENTS))
            return parseComment();

        // Check for slot input syntax: <:name>
        if (match(Type.COLON))
            return parseSlotTag(true);

        // Check for closing tag
        if (match(Type.SLASH))
            return parseClosingTag();

        // Parse tag name
        var tagName = joinTokens(Type.TEXT, Type.NUMBER, Type.KEYWORD);
        if (tagName.isEmpty())
            throw error("Expected tag name after '<'");

        // Check if it's a slot tag
        if (tagName.equals(HTML_TAG_SLOT))
            return parseSlotTag(false);

        // Parse attributes (including control flow attributes)
        var parsed = parseAttributes();
        var attributes = parsed.attributes();

        skipWhitespaceAndNewlines();

        // Check for self-closing tag
        if (consume(Type.SLASH)) {
            expect(Type.CLOSE_ANGLE_BRACKET, String.format("""
                    Expected '>' after '/' but found '%s'.
                    If you have a comparison in an attribute value, make sure it's properly quoted.
                    """, peek().value())
            );

            return parsed.build(
                    new ComponentBlockNode(tagName, attributes, mutableListOf())
            );
        }

        expect(Type.CLOSE_ANGLE_BRACKET, String.format("""
                Expected '>' after '/' but found '%s'.
                If you have a comparison in an attribute value, make sure it's properly quoted.
                """, peek().value())
        );

        // Check if this is a void element (cannot have children or closing tag)
        if (VOID_ELEMENTS.contains(tagName.toLowerCase()))
            return parsed.build(
                    new ComponentBlockNode(tagName, attributes, mutableListOf())
            );

        // Parse children
        var children = new ArrayList<Node>();

        stack.push(children);
        while (hasNext()) {
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
                new ComponentBlockNode(tagName, attributes, children)
        );
    }

    /**
     * Parse an HTML comment
     */
    private Node parseComment() {
        expect(Type.EXCLAMATION, "Expected '!' after '<' for comment");
        expect(Type.MARKER_COMMENTS, "Expected '--' after '<!' for comment");

        var builder = new StringBuilder();

        while (hasNext()) {
            var savedPos = pos;

            if (consume(Type.MARKER_COMMENTS)) {
                if (consume(Type.CLOSE_ANGLE_BRACKET))
                    break;

                pos = savedPos;
            }

            builder.append(advance().value());
        }

        return new CommentNode(builder.toString());
    }

    /**
     * Parse slot tag, depending on the syntax used:
     * <pre>
     *   input: &lt;:name&gt;
     *   output: &lt;slot&gt; or &lt;slot:name&gt;
     * </pre>
     */
    private Node parseSlotTag(boolean input) {
        String slotName = HTML_SLOT_DEFAULT;

        // Check for named slot: <slot:name>
        if (consume(Type.COLON)) {
            var itemName = joinTokens(Type.TEXT, Type.NUMBER, Type.KEYWORD);
            if (!itemName.isEmpty())
                slotName = itemName;
            else if (!input)
                throw error("Expected slot name after 'slot:'");
        }

        var parsed = parseAttributes();
        var attributes = parsed.attributes();

        // Check for self-closing
        if (consume(Type.SLASH)) {
            expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>' after '/'");
            return parsed.build(
                    new SlotBlockNode(slotName, attributes, mutableListOf(), !input)
            );
        }

        expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>' after slot tag");

        // Parse default content
        var children = new ArrayList<Node>();

        stack.push(children);
        while (hasNext()) {
            if (consume(Type.OPEN_ANGLE_BRACKET)) {
                int savedPos = pos - 1;

                if (consume(Type.SLASH)) {
                    skipWhitespace();

                    if (input || consumeSymbol(Type.TEXT, "slot")) {
                        if (consume(Type.COLON)) {
                            var closeName = joinTokens(Type.TEXT, Type.NUMBER, Type.KEYWORD);
                            if (closeName.equals(slotName)) {
                                skipWhitespace();
                                expect(Type.CLOSE_ANGLE_BRACKET, "Expected '>'");
                                break;
                            }
                        } else if (!input) {
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
                new SlotBlockNode(slotName, attributes, children, !input)
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
     * Parse tag attributes and extract control flow attributes (for, if)
     */
    private ParsedAttributes parseAttributes() {
        var attributes = new ArrayList<AttributeValueNode>();
        List<Attribute> flowAttributes = new ArrayList<>();
        skipWhitespaceAndNewlines();

        while (hasNext() && !match(Type.CLOSE_ANGLE_BRACKET, Type.SLASH)) {
            // Check for dynamic attribute with curly braces
            if (match(Type.OPEN_EXPRESSION)) {
                var expr = parseMustacheExpression();
                attributes.add(new ExpressionAttributeNode(expr));

                skipWhitespaceAndNewlines();
                continue;
            }

            // Parse attribute name
            var name = joinTokens(Type.TEXT, Type.NUMBER, Type.KEYWORD, Type.AT);
            if (name.isEmpty())
                break;

            skipWhitespace();

            // Check for attribute value
            if (consume(Type.ASSIGN)) {
                skipWhitespace();

                // Flow attributes: for="..."
                if (name.equals(KEYWORD_FOR) && consume(Type.QUOTE)) {
                    flowAttributes.add(parseControlAttribute(Type.QUOTE));
                    skipWhitespaceAndNewlines();
                    continue;
                }

                // Flow attributes like if="$condition"
                if (KEYWORD_CONDITIONALS.contains(name) && consume(Type.QUOTE)) {
                    flowAttributes.add(parseConditionAttribute(name, Type.QUOTE));
                    skipWhitespaceAndNewlines();
                    continue;
                }

                // Dynamic attribute: attr={expr}
                if (consume(Type.OPEN_EXPRESSION)) {
                    var expr = parseExpression(true);

                    skipWhitespace();
                    expect(Type.CLOSE_EXPRESSION, "Expected '}}' after attribute expression");

                    attributes.add(new DynamicAttributeNode(name, expr));
                }

                // mixed attribute: attr="value {{$expr}} value"
                else if (consume(Type.QUOTE)) {
                    var parts = new ArrayList<>();
                    var builder = new StringBuilder();

                    while (hasNext() && !match(Type.QUOTE)) {
                        if (match(Type.OPEN_EXPRESSION)) {
                            if (!builder.isEmpty()) {
                                parts.add(builder.toString());
                                builder.setLength(0);
                            }

                            parts.add(parseMustacheExpression());
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
                    attributes.add(new MixedAttributeNode(name, mutableListOf(value)));
                }
            } else if (name.equals(KEYWORD_ELSE))
                flowAttributes.add(new ConditionAttribute(KEYWORD_ELSE, new LiteralNode(true)));
            else
                attributes.add(new FlagAttributeNode(name));

            skipWhitespaceAndNewlines();
        }

        return new ParsedAttributes(attributes, flowAttributes);
    }

    /**
     * Parse the value of a "for" block
     * Supports three syntaxes:
     * - for="$items" (item name defaults to "item", no index)
     * - for="$item in $items" (custom item name, no index)
     * - for="$item, $index in $items" (custom item name and index name)
     *
     * @param delimiter The expected delimiter type to end the attribute value
     */
    private ControlAttribute parseControlAttribute(Type delimiter) {
        skipWhitespace();

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
                    throw error("Expected index variable after comma in for block");

                var indexVar = parseVariable();
                if (!(indexVar instanceof VariableNode(String indexVarName, boolean reversed)))
                    throw error("Expected variable for index in for block");

                indexName = indexVarName;
                skipWhitespace();

                // Expect "in" keyword
                if (!matchSymbol(Type.KEYWORD, KEYWORD_IN))
                    throw error("Expected 'in' keyword in for block");
            }

            // Check for "in" keyword
            if (consumeSymbol(Type.KEYWORD, KEYWORD_IN)) {
                skipWhitespace();

                // Parse collection
                collection = parseExpression(false);

                // Extract item name from first variable
                if (!(firstVar instanceof VariableNode(String name, boolean reversed)))
                    throw error("Expected variable for item in for block");

                itemName = name;
            }

            // Simplified syntax: "{{for $collection}}"
            else
                collection = firstVar;

        } else
            throw error("Expected variable in for block");

        skipWhitespace();

        expect(delimiter, "Expected delimiter around `for` block");
        return new ControlAttribute(collection, itemName, indexName);
    }

    /**
     * Parse the value of an "if" block
     *
     * @param delimiter The expected delimiter type to end the attribute value
     */
    private ConditionAttribute parseConditionAttribute(String keyword, Type delimiter) {
        skipWhitespace();

        // Parse condition expression, or use "true" if no condition is provided (e.g. {{if}} or if="")
        // This allows for else-if and else blocks without conditions
        var condition = (ExpressionNode) null;
        if (match(delimiter))
            condition = new LiteralNode(true);
        else
            condition = parseExpression(true);

        skipWhitespace();

        expect(delimiter, "Expected delimiter around `if` block");
        return new ConditionAttribute(keyword, condition);
    }

    // ===== Navigation =====

    private boolean hasNext() {
        return pos < tokens.size() && !peek().match(Type.EOI);
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
        if (hasNext())
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
        if (!hasNext())
            return false;

        for (var type : types)
            if (peek().type() == type)
                return true;

        return false;
    }

    private boolean matchAll(Type... types) {
        int index = 0;

        for (var type : types) {
            var token = peek(pos + index++);

            if (token == null || token.type() != type)
                return false;
        }

        return true;
    }

    private boolean matchSymbol(Type type, String... symbols) {
        if (!hasNext() || !match(type))
            return false;

        return peek().match(symbols);
    }

    private Token expect(Type type, String message) {
        if (!match(type))
            throw error(message);

        return advance();
    }

    private void skipWhitespace() {
        while (match(Type.SPACER))
            advance();
    }

    /**
     * Skip all whitespace including newlines
     */
    private void skipWhitespaceAndNewlines() {
        while (match(Type.SPACER, Type.NEW_LINE))
            advance();
    }

    // ===== Helper =====

    /**
     * Create a ParserException with source context
     */
    private ParserException error(String message) {
        return new ParserException(message, peek(), source);
    }

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

    // ==== Optimization Passes ====

    /**
     * Apply post-processing functions to a node and its children recursively
     *
     * @param nodes          The list of nodes to process
     * @param postProcessors The list of post-processing functions to apply
     */
    private void postProcessNodes(List<Node> nodes, List<Consumer<List<Node>>> postProcessors) {
        // Apply post-processing functions to children first
        for (var node : nodes) {
            switch (node) {
                case ConditionalBlockNode ifNode -> {
                    for (var branch : ifNode.branches())
                        postProcessNodes(branch.body(), postProcessors);
                }
                case ForBlockNode forNode -> postProcessNodes(forNode.body(), postProcessors);
                case ComponentBlockNode componentNode -> postProcessNodes(componentNode.children(), postProcessors);
                case SlotBlockNode slotNode -> postProcessNodes(slotNode.children(), postProcessors);
                default -> {
                    // No children to process
                }
            }
        }

        // Then apply post-processing functions to the node itself
        for (var processor : postProcessors)
            processor.accept(nodes);
    }

    /**
     * Merge consecutive TextNodes in the given list into single TextNodes
     *
     * @param nodes The list of nodes to optimize
     */
    @SuppressWarnings("unchecked")
    private <T extends Node> void optimizeTextNodes(List<T> nodes) {
        if (nodes.isEmpty())
            return;

        var result = new ArrayList<T>();
        var textBuilder = new StringBuilder();

        for (var node : nodes) {
            if (node instanceof TextNode(String content))
                textBuilder.append(content);
            else {
                if (!textBuilder.isEmpty()) {
                    result.add((T) new TextNode(textBuilder.toString()));
                    textBuilder.setLength(0);
                }

                result.add(node);
            }
        }

        if (!textBuilder.isEmpty())
            result.add((T) new TextNode(textBuilder.toString()));

        nodes.clear();
        nodes.addAll(result);
    }

    /**
     * Group consecutive if/else-if/else elements into proper conditional chains.
     * This ensures that only the first matching condition renders.
     *
     * @param nodes The list of nodes to optimize
     */
    private void optimizeConditionalChains(List<Node> nodes) {
        if (nodes.isEmpty())
            return;

        var result = new ArrayList<Node>();
        var spaces = new ArrayList<Integer>();
        var condition = (ConditionalBlockNode) null;

        for (var node : nodes) {
            // Check if this is a conditional block that can be part of a chain
            if (node instanceof ConditionalBlockNode conditionalBlockNode) {
                if (condition == null)
                    condition = conditionalBlockNode;
                else if (!Objects.equals(conditionalBlockNode.name(), KEYWORD_IF))
                    condition.branches().addAll(conditionalBlockNode.branches());
                else {
                    result.add(condition);
                    condition = conditionalBlockNode;
                }

                continue;
            }

            // Special handling for whitespace nodes between conditional blocks
            if (condition != null) {
                var isSpace = node instanceof TextNode(String content) && content.isBlank();

                if (!isSpace) {
                    result.add(condition);
                    condition = null;
                } else
                    spaces.add(result.size());
            }

            result.add(node);
        }

        // Add any remaining condition at the end
        if (condition != null)
            result.add(condition);

        // Clear whitespace
        for (var spaceIndex : spaces.reversed())
            result.remove(spaceIndex.intValue());

        nodes.clear();
        nodes.addAll(result);
    }
}