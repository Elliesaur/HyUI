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
import au.ellie.hyui.html.template.context.VariableStack;
import au.ellie.hyui.html.template.item.Node;
import au.ellie.hyui.html.template.item.Node.AttributeValueNode.DynamicAttributeNode;
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
import au.ellie.hyui.html.template.utils.NumericUtils;
import au.ellie.hyui.html.template.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class Evaluator {
    private final FilterRegistry filterRegistry;
    private final VariableStack contextStack;
    private Map<String, CachedComponent> components;

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
            case TextNode text -> text.content();
            case ExpressionNode expr -> {
                var value = evaluateExpression(expr);
                yield value == null ? "" : value.toString();
            }
            case IfBlockNode ifBlock -> evaluateIfBlock(ifBlock);
            case EachBlockNode eachBlock -> evaluateEachBlock(eachBlock);
            case ComponentElementNode component -> evaluateComponent(component);

            default -> throw new IllegalStateException("Unexpected value: " + node);
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
            case VariableNode var -> contextStack.getVariable(var.name());
            case PropertyAccessNode prop -> evaluatePropertyAccess(prop);
            case BinaryOpNode binary -> evaluateBinaryOp(binary);
            case PipeNode pipe -> evaluatePipe(pipe);
            case DefaultNode def -> evaluateDefault(def);
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

        // Access via Map
        if (obj instanceof Map<?, ?> map)
            return map.get(property);

        // Access via Reflection
        try {
            var clazz = obj.getClass();

            try {
                var field = clazz.getDeclaredField(property);
                field.setAccessible(true);

                return field.get(obj);
            } catch (NoSuchFieldException e) {
                var propName = property.substring(0, 1).toUpperCase() + property.substring(1);
                var methodNames = new ArrayList<String>() {{
                    add(property);
                    add("get" + propName);
                    add("is" + propName);
                }};

                // Open methods
                for (var name : methodNames) {
                    var method = ReflectionUtils.getPublicMethod(clazz, name);

                    if (method.isPresent())
                        return method.get().invoke(obj);
                }
            }
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
        var left = evaluateExpression(node.left());
        var right = evaluateExpression(node.right());

        return switch (node.operator()) {
            case Symbols.EQUALS -> evaluateEquals(left, right);
            case Symbols.NOT_EQUALS -> !evaluateEquals(left, right);
            case Symbols.LESS_THAN -> evaluateComparison(left, right) < 0;
            case Symbols.GREATER_THAN -> evaluateComparison(left, right) > 0;
            case Symbols.LESS_THAN_EQUALS -> evaluateComparison(left, right) <= 0;
            case Symbols.GREATER_THAN_EQUALS -> evaluateComparison(left, right) >= 0;
            case Symbols.AND -> toBoolean(left) && toBoolean(right);
            case Symbols.OR -> toBoolean(left) || toBoolean(right);
            case Symbols.IN -> evaluateIn(left, right);
            case Symbols.NOT_IN -> !evaluateIn(left, right);
            default -> throw new RuntimeException("Unknown operator: " + node.operator());
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
    private int evaluateComparison(Object left, Object right) {
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

        throw new RuntimeException("Cannot compare " + left.getClass().getSimpleName() +
                " and " + right.getClass().getSimpleName());
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

        for (Object item : items) {
            var context = new HashMap<String, Object>();
            context.put(node.itemName(), item);

            contextStack.pushScope(context);
            try {
                for (Node child : node.body())
                    result.append(evaluateNode(child));
            } finally {
                contextStack.popScope();
            }
        }

        return result.toString();
    }

    /**
     * Evaluate a `component` element node and return the resulting string.
     *
     * @param component The `component` element node to evaluate.
     * @return The resulting string after evaluation.
     */
    private String evaluateComponent(ComponentElementNode component) {
        var componentDef = component.tagName();
        var cachedComponent = components.get(componentDef);

        if (cachedComponent == null)
            throw new RuntimeException("Component not found: " + componentDef);

        var context = new HashMap<String, Object>();
        for (var entry : component.attributes().entrySet()) {
            switch (entry.getValue()) {
                case DynamicAttributeNode dynamicAttributeNode ->
                        context.put(entry.getKey(), evaluateExpression(dynamicAttributeNode.expression()));
                case StaticAttributeNode staticAttributeNode ->
                        context.put(entry.getKey(), staticAttributeNode.value());
                case FlagAttributeNode _ -> context.put(entry.getKey(), true);
            }
        }

        context.put("children", (Supplier<String>) () -> {
            var result = new StringBuilder();
            for (Node child : component.children())
                result.append(evaluateNode(child));
            return result.toString();
        });

        contextStack.pushScope(context);
        try {
            return evaluate(cachedComponent.getAst(components));
        } finally {
            contextStack.popScope();
        }
    }

    // ===== Helpers =====

    /**
     * Convert an object to a boolean value.
     *
     * @param value The object to convert
     * @return The boolean value
     */
    private boolean toBoolean(Object value) {
        return switch (value) {
            case null -> false;
            case Boolean b -> b;
            case Number n -> n.doubleValue() != 0;
            case String s -> !s.isEmpty();
            case Collection<?> c -> !c.isEmpty();
            case Map<?, ?> m -> !m.isEmpty();
            default -> true;
        };
    }

    /**
     * Convert an object to an iterable or throw an exception.
     *
     * @param value The object to convert
     * @return The iterable
     */
    private Iterable<?> toIterable(Object value) {
        if (value instanceof Iterable<?> iterable)
            return iterable;

        if (value.getClass().isArray())
            return Arrays.asList((Object[]) value);

        throw new RuntimeException("Cannot iterate over " + value.getClass());
    }

    /**
     * Evaluate if needle is in haystack.
     *
     * @param needle   Object to search for
     * @param haystack Object to search in
     * @return True if needle is in haystack, false otherwise
     */
    private boolean evaluateIn(Object needle, Object haystack) {
        return switch (haystack) {
            case Collection<?> collection -> collection.contains(needle);
            case Map<?, ?> map -> map.containsKey(needle);
            case String str when needle != null -> str.contains(needle.toString());
            case null, default -> false;
        };
    }
}
