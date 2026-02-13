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

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.html.TemplateProcessor.CachedComponent;
import au.ellie.hyui.html.template.context.FilterRegistry;
import au.ellie.hyui.html.template.context.SlotSupplier;
import au.ellie.hyui.html.template.context.VariableStack;
import au.ellie.hyui.html.template.context.VariableStack.VariableScope;
import au.ellie.hyui.html.template.exception.EvaluationException;
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
import au.ellie.hyui.html.template.item.Symbols;
import au.ellie.hyui.utils.NumericUtils;
import au.ellie.hyui.utils.ReflectionUtils;

import java.util.*;
import java.util.function.Supplier;

import static au.ellie.hyui.html.template.item.Attribute.inlineAttributes;
import static au.ellie.hyui.html.template.item.Symbols.SCOPE_COMPONENT_PREFIX;
import static au.ellie.hyui.html.template.item.Symbols.SCOPE_EACH_NAME;
import static au.ellie.hyui.utils.ObjectUtils.*;

public class Evaluator {
    private final static Stack<String> STACK = new Stack<>();

    private final FilterRegistry filterRegistry;
    private final VariableStack contextStack;
    private final Map<String, CachedComponent> components;

    public Evaluator(VariableStack context, FilterRegistry filterRegistry, Map<String, CachedComponent> components) {
        this.components = components;
        this.contextStack = context;
        this.filterRegistry = filterRegistry;
    }

    /**
     * Evaluate a list of AST nodes and return the resulting string.
     *
     * @param nodes The list of AST nodes to evaluate.
     * @return The resulting string after evaluation.
     */
    public String evaluate(List<Node> nodes) {
        var result = new StringBuilder();

        for (Node node : nodes)
            result.append(evaluateNode(node));

        return result.toString().replaceAll("\\n+$", "");
    }

    /**
     * Evaluate a single AST node and return the resulting string.
     *
     * @param node The AST node to evaluate.
     * @return The resulting string after evaluation.
     */
    private String evaluateNode(Node node) {
        return switch (node) {
            case MarkerNode marker -> {
                if (marker.inside() != null)
                    yield evaluateNode(marker.inside());
                yield "";
            }
            case TextNode text -> text.content();
            case ExpressionNode expr -> {
                var value = evaluateExpression(expr);
                yield value == null ? "" : value.toString();
            }
            case IfBlockNode ifBlock -> evaluateIfBlock(ifBlock);
            case EachBlockNode eachBlock -> evaluateEachBlock(eachBlock);
            case SlotBlockNode slotBlockNode -> evaluateSlotBlock(slotBlockNode);
            case ComponentBlockNode component -> evaluateComponent(component);
            case AttributeValueNode attributeValueNode -> evaluateAttributeString(attributeValueNode);

            default -> throw new EvaluationException("Unexpected value", node);
        };
    }

    /**
     * Evaluate an expression node and return the resulting value.
     *
     * @param node The expression node to evaluate.
     * @return The resulting value after evaluation.
     */
    private Object evaluateExpression(ExpressionNode node) {
        return switch (node) {
            case TextNode literal -> literal.content();
            case LiteralNode literal -> literal.value();
            case PropertyAccessNode prop -> evaluatePropertyAccess(prop);
            case BinaryOpNode binary -> evaluateBinaryOp(binary);
            case PipeNode pipe -> evaluatePipe(pipe);
            case DefaultNode def -> evaluateDefault(def);
            case VariableNode var -> contextStack.getVariable(var.name(), () -> {
                for (String key : contextStack.getScopeKeys()) {
                    // Prevent accessing variables from scopes
                    if (key.startsWith(Symbols.HTML_SLOT_KEY))
                        continue;

                    try {
                        return ReflectionUtils.getObjectProperty(contextStack.getVariable(key), var.name());
                    } catch (Exception _) {
                        // Ignore and return null
                    }
                }

                return null;
            });
        };
    }

    /**
     * Evaluate a property access on an object.
     *
     * @param node The property access node.
     * @return The value of the accessed property, or null if not found.
     */
    private Object evaluatePropertyAccess(PropertyAccessNode node) {
        var obj = evaluateExpression(node.object());
        if (obj == null) return null;

        var property = node.property();

        try {
            return ReflectionUtils.getObjectProperty(obj, property);
        } catch (Exception _) {
            HyUIPlugin.getLog().logWarn("Error accessing property " + property + " on " + obj.getClass());
        }

        return null;
    }

    /**
     * Evaluate a `binary` operation between two expressions.
     *
     * @param node The binary operation node.
     * @return The result of the binary operation.
     */
    private Object evaluateBinaryOp(BinaryOpNode node) {
        Supplier<Object> right = () -> evaluateExpression(node.right());
        var left = evaluateExpression(node.left());

        return switch (node.operator()) {
            case Symbols.EQUALS -> evaluateEquals(left, right.get());
            case Symbols.NOT_EQUALS -> !evaluateEquals(left, right.get());
            case Symbols.LESS_THAN -> evaluateComparison(node, left, right.get()) < 0;
            case Symbols.GREATER_THAN -> evaluateComparison(node, left, right.get()) > 0;
            case Symbols.LESS_THAN_EQUALS -> evaluateComparison(node, left, right.get()) <= 0;
            case Symbols.GREATER_THAN_EQUALS -> evaluateComparison(node, left, right.get()) >= 0;
            case Symbols.AND -> toBoolean(left) && toBoolean(right.get());
            case Symbols.OR -> toBoolean(left) || toBoolean(right.get());
            case Symbols.KEYWORD_IN -> containedIn(left, right.get());
            case Symbols.KEYWORD_NOT_IN -> !containedIn(left, right.get());
            default -> throw new EvaluationException("Unknown operator " + node.operator(), node);
        };
    }

    /**
     * Evaluate `equality` between two values.
     *
     * @param left  Left value of equation
     * @param right Right value of equation
     * @return True if equal, false otherwise
     */
    private boolean evaluateEquals(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;

        var leftNum = NumericUtils.toNumber(left);
        var rightNum = NumericUtils.toNumber(right);

        if (leftNum != null && rightNum != null)
            return NumericUtils.equals(leftNum, rightNum);

        return Objects.equals(left, right);
    }

    /**
     * Evaluate comparison between two values.
     *
     * @param left  Left value of comparison
     * @param right Right value of comparison
     * @return Negative if left < right, 0 if left == right, positive if left > right
     */
    @SuppressWarnings("unchecked")
    private int evaluateComparison(Node node, Object left, Object right) {
        if (left == null && right == null) return 0;
        if (left == null) return -1;
        if (right == null) return 1;

        var leftNum = NumericUtils.toNumber(left);
        var rightNum = NumericUtils.toNumber(right);

        if (leftNum != null && rightNum != null)
            return NumericUtils.compare(leftNum, rightNum);

        if (left instanceof Comparable && left.getClass().isInstance(right)) {
            var leftComp = (Comparable<Object>) left;
            return leftComp.compareTo(right);
        }

        throw new EvaluationException("Cannot compare " + left.getClass().getSimpleName() +
                " and " + right.getClass().getSimpleName(), node);
    }

    /**
     * Evaluate a `pipe` expression (filter application).
     *
     * @param node The pipe node
     * @return The result of the filter application
     */
    private Object evaluatePipe(PipeNode node) {
        var value = evaluateExpression(node.expression());
        var filter = filterRegistry.get(node.filterName());

        return filter.apply(value);
    }

    /**
     * Evaluate a `default` expression and return the first non-null, non-empty alternative.
     *
     * @param node The default node to evaluate.
     * @return The first non-null, non-empty alternative value, or null if none found.
     */
    private Object evaluateDefault(DefaultNode node) {
        for (ExpressionNode alternative : node.alternatives()) {
            var value = evaluateExpression(alternative);
            if (value != null && !value.toString().isEmpty())
                return value;
        }

        return null;
    }

    /**
     * Evaluate an `if` / `else` block node and return the resulting string.
     *
     * @param node The `if` block node to evaluate.
     * @return The resulting string after evaluation.
     */
    private String evaluateIfBlock(IfBlockNode node) {
        var conditionValue = evaluateExpression(node.condition());

        var result = new StringBuilder();
        if (toBoolean(conditionValue)) {
            for (Node child : node.thenBody())
                result.append(evaluateNode(child));
        } else {
            for (Node child : node.elseBody())
                result.append(evaluateNode(child));
        }

        return result.toString();
    }

    /**
     * Evaluate an `each` block node and return the resulting string.
     *
     * @param node The `each` block node to evaluate.
     * @return The resulting string after evaluation.
     */
    private String evaluateEachBlock(EachBlockNode node) {
        var collectionValue = evaluateExpression(node.collection());

        if (collectionValue == null)
            return "";

        var items = toIterable(collectionValue);
        var result = new StringBuilder();
        var index = 0;

        for (Object item : items) {
            var scope = new VariableScope(SCOPE_EACH_NAME);
            scope.putKeyed(node.itemName(), item);

            // Add index if indexName is provided
            if (node.indexName() != null)
                scope.putKeyed(node.indexName(), index);

            contextStack.pushScope(scope);
            try {
                for (Node child : node.body())
                    result.append(evaluateNode(child));
            } finally {
                contextStack.popScope();
            }

            index++;
        }

        return result.toString();
    }

    /**
     * Evaluate a `component` element node and return the resulting string.
     *
     * @param component The `component` element node to evaluate.
     * @return The resulting string after evaluation.
     */
    private String evaluateComponent(ComponentBlockNode component) {
        var componentDef = component.tag();

        // Handle <template> tag as renderless wrapper - just render children
        if (componentDef.equals("template")) {
            var result = new StringBuilder();
            for (Node child : component.children())
                result.append(evaluateNode(child));
            return result.toString();
        }

        if (!components.containsKey(componentDef) || STACK.contains(componentDef))
            return evaluateComponentString(component);

        // Attributes
        var context = new HashMap<String, Object>();
        for (var attribute : component.attributes()) {
            switch (attribute) {
                case DynamicAttributeNode dynamicAttributeNode ->
                        context.put(attribute.getName(), evaluateExpression(dynamicAttributeNode.expression()));
                case MixedAttributeNode mixedAttr -> {
                    var builder = new StringBuilder();
                    for (var part : mixedAttr.parts()) {
                        if (part instanceof String text)
                            builder.append(text);
                        else if (part instanceof ExpressionNode expr) {
                            var value = evaluateExpression(expr);
                            if (value != null)
                                builder.append(value);
                        } else if (part instanceof Node node) {
                            // Handle inline blocks (e.g., IfBlockNode)
                            var value = evaluateNode(node);
                            if (value != null && !value.isEmpty())
                                builder.append(value);
                        }
                    }

                    context.put(mixedAttr.name(), builder.toString());
                }
                case ExpressionAttributeNode expressionAttributeNode -> {
                    var evaluatedValue = evaluateNode(expressionAttributeNode.expressions());
                    if (!evaluatedValue.isEmpty())
                        inlineAttributes(evaluatedValue, context);
                }
                case FlagAttributeNode _ -> context.put(attribute.getName(), true);
            }
        }

        // Children
        var scope = new VariableScope(SCOPE_COMPONENT_PREFIX + componentDef, context);
        for (var child : component.children()) {
            var slotName = Symbols.HTML_SLOT_DEFAULT;
            if (child instanceof SlotBlockNode slot)
                slotName = slot.name();

            // Saved as "slot.{slotName}" in component scope
            scope.computeIfAbsent(Symbols.HTML_SLOT_KEY + slotName, key -> {
                scope.getKeys().add(key);
                return new SlotSupplier(this::evaluateNode);
            }).add(child);
        }

        STACK.push(componentDef);
        contextStack.pushScope(scope);
        try {
            var cachedComponent = components.get(componentDef);
            return evaluate(cachedComponent.getAst());
        } finally {
            STACK.pop();
            contextStack.popScope();
        }
    }

    /**
     * Evaluate a `slot` block node and return the resulting string.
     *
     * @param slotBlockNode The `slot` block node to evaluate.
     */
    private String evaluateSlotBlock(SlotBlockNode slotBlockNode) {
        var slotName = slotBlockNode.name();

        if (slotBlockNode.output()) {
            var content = contextStack.getVariable(Symbols.HTML_SLOT_KEY + slotName, () -> null);
            if (content != null)
                return content.toString();
        }

        return evaluate(slotBlockNode.children());
    }

    /**
     * HTML void elements that should be self-closing when empty.
     * These are elements that cannot have any content.
     */
    private static final java.util.Set<String> VOID_ELEMENTS = java.util.Set.of(
            "area", "base", "br", "col", "embed", "hr", "img", "input",
            "link", "meta", "param", "source", "track", "wbr"
    );

    /**
     * Evaluate a component as a string without processing it as a component.
     *
     * @param component The component element node to evaluate as a string.
     * @return The resulting string representation of the component.
     */
    private String evaluateComponentString(ComponentBlockNode component) {
        // Void elements should ALWAYS be self-closing (they cannot have children)
        boolean isVoid = VOID_ELEMENTS.contains(component.tag().toLowerCase());

        // Check if element is completely empty (no children, no attributes)
        boolean isEmpty = component.children().isEmpty();
        boolean hasNoAttributes = component.attributes().isEmpty();

        // Skip rendering completely empty non-void elements with no attributes
        // These cause flexbox layout issues and are typically unintentional
        if (!isVoid && isEmpty && hasNoAttributes) {
            return "";
        }

        var sb = new StringBuilder();
        sb.append("<").append(component.tag());

        for (var attribute : component.attributes()) {
            var attrStr = evaluateAttributeString(attribute).trim();
            if (!attrStr.isEmpty())
                sb.append(" ").append(attrStr);
        }

        if (isVoid) {
            sb.append("/>");
            return sb.toString();
        }

        // Non-void elements always have opening and closing tags
        sb.append(">");

        for (Node child : component.children())
            sb.append(evaluateNode(child));

        sb.append("</").append(component.tag()).append(">");

        return sb.toString();
    }

    /**
     * Evaluate an attribute value node and return the resulting string.
     *
     * @param attributeValueNode The attribute value node to evaluate.
     * @return The resulting string after evaluation.
     */
    private String evaluateAttributeString(AttributeValueNode attributeValueNode) {
        var sb = new StringBuilder();

        switch (attributeValueNode) {
            case DynamicAttributeNode dynamic ->
                    sb.append(dynamic.getName()).append("=\"").append(evaluateNode(dynamic.expression())).append("\"");
            case MixedAttributeNode mixedAttr -> {
                var builder = new StringBuilder();
                for (var part : mixedAttr.parts()) {
                    if (part instanceof String text)
                        builder.append(text);
                    else if (part instanceof ExpressionNode expr) {
                        var value = evaluateExpression(expr);
                        if (value != null)
                            builder.append(value);
                    } else if (part instanceof Node node) {
                        // Handle inline blocks (e.g., IfBlockNode)
                        var value = evaluateNode(node);
                        if (value != null && !value.isEmpty())
                            builder.append(value);
                    }
                }

                sb.append(mixedAttr.getName()).append("=\"").append(builder).append("\"");
            }
            case FlagAttributeNode flag -> sb.append(flag.getName());
            case ExpressionAttributeNode expression -> sb.append(evaluateNode(expression.expressions()));
        }

        return sb.toString();
    }
}
