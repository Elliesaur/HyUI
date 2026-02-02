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

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.html.ast.context.FilterRegistry;
import au.ellie.hyui.html.ast.context.VariableStack.VariableStackImpl;
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
import au.ellie.hyui.html.ast.utils.NumericUtils;
import au.ellie.hyui.html.ast.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Evaluator {
    private final FilterRegistry filterRegistry;
    private final VariableStackImpl contextStack;

    public Evaluator(Map<String, Object> variables, FilterRegistry filterRegistry) {
        this.contextStack = new VariableStackImpl(variables);
        this.filterRegistry = filterRegistry;
    }

    /**
     * Evaluate a list of AST nodes and return the resulting string.
     *
     * @param nodes The list of AST nodes to evaluate.
     * @return The resulting string after evaluation.
     */
    public String evaluate(List<Node> nodes) {
        StringBuilder result = new StringBuilder();

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
                Object value = evaluateExpression(expr);
                yield value == null ? "" : value.toString();
            }
            case IfBlockNode ifBlock -> evaluateIfBlock(ifBlock);
            case EachBlockNode eachBlock -> evaluateEachBlock(eachBlock);

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
        Object obj = evaluateExpression(node.object());
        if (obj == null) return null;

        String property = node.property();

        // Access via Map
        if (obj instanceof Map<?, ?> map)
            return map.get(property);

        // Access via Reflection
        try {
            Class<?> clazz = obj.getClass();

            try {
                Field field = clazz.getDeclaredField(property);
                field.setAccessible(true);

                return field.get(obj);
            } catch (NoSuchFieldException e) {
                var propName = property.substring(0, 1).toUpperCase() + property.substring(1);
                List<String> methodNames = new ArrayList<>() {{
                    add(property);
                    add("get" + propName);
                    add("is" + propName);
                }};

                // Open methods
                for (String name : methodNames) {
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
        Object left = evaluateExpression(node.left());
        Object right = evaluateExpression(node.right());

        return switch (node.operator()) {
            case COMP_EQUALS -> evaluateEquals(left, right);
            case COMP_NOT_EQUALS -> !evaluateEquals(left, right);
            case COMP_LESS_THAN -> evaluateComparison(left, right) < 0;
            case COMP_GREATER_THAN -> evaluateComparison(left, right) > 0;
            case COMP_LESS_EQUALS -> evaluateComparison(left, right) <= 0;
            case COMP_GREATER_EQUALS -> evaluateComparison(left, right) >= 0;
            case COMP_AND -> toBoolean(left) && toBoolean(right);
            case COMP_OR -> toBoolean(left) || toBoolean(right);
            case COMP_IN -> evaluateIn(left, right);
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

        Number leftNum = NumericUtils.toNumber(left);
        Number rightNum = NumericUtils.toNumber(right);

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
    private int evaluateComparison(Object left, Object right) {
        if (left == null && right == null) return 0;
        if (left == null) return -1;
        if (right == null) return 1;

        Number leftNum = NumericUtils.toNumber(left);
        Number rightNum = NumericUtils.toNumber(right);

        if (leftNum != null && rightNum != null)
            return NumericUtils.compare(leftNum, rightNum);

        if (left instanceof Comparable && left.getClass().isInstance(right)) {
            @SuppressWarnings("unchecked")
            Comparable<Object> leftComp = (Comparable<Object>) left;
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
        Object value = evaluateExpression(node.expression());
        FilterRegistry.Filter filter = filterRegistry.get(node.filterName());

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
            Object value = evaluateExpression(alternative);
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
        Object conditionValue = evaluateExpression(node.condition());

        StringBuilder result = new StringBuilder();
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
        Object collectionValue = evaluateExpression(node.collection());

        if (collectionValue == null)
            return "";

        Iterable<?> items = toIterable(collectionValue);
        StringBuilder result = new StringBuilder();

        for (Object item : items) {
            Map<String, Object> context = new HashMap<>();
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
