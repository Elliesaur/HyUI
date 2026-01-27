package au.ellie.hyui.html;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.builders.UIElementBuilder;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Preprocessor for HyUIML templates that supports variable interpolation and component inclusion.
 *
 * <h2>Variable Interpolation</h2>
 * Use <code>{{$variableName}}</code> syntax for variable substitution:
 * <pre>
 * &lt;p&gt;Hello, {{$playerName}}!&lt;/p&gt;
 * </pre>
 *
 * <h2>Default Values</h2>
 * Use the pipe syntax for default values:
 * <pre>
 * &lt;p&gt;Score: {{$score|0}}&lt;/p&gt;
 * </pre>
 *
 * <h2>Component Inclusion</h2>
 * Include reusable components with {@code {{&#64;component}}}:
 * <pre>
 * {{&#64;component/button:text=Click Me,id=myBtn}}
 * </pre>
 *
 * <h2>Filters</h2>
 * Apply transformations with filters:
 * <pre>
 * {{$name|upper}}     - Uppercase
 * {{$name|lower}}     - Lowercase
 * {{$value|number}}   - Format as number
 * {{$value|percent}}  - Format as percentage
 * </pre>
 */
public class TemplateProcessor {

    // Pattern for {{$variable}} or {{$variable|default}} or {{$variable|filter}}
    private static final Pattern VARIABLE_PATTERN = Pattern.compile(
            "\\{\\{\\$([a-zA-Z_][a-zA-Z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)*)(?:\\|([^}]*))?\\}\\}"
    );

    private static final String EACH_START = "{{#each";
    private static final String EACH_END = "{{/each}}";
    private static final String IF_START = "{{#if";
    private static final String IF_END = "{{/if}}";
    private static final String ELSE_TAG = "{{else}}";

    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, String> components = new HashMap<>();
    private final Map<String, Function<String, String>> filters = new HashMap<>();
    private ValueResolver valueResolver;
    private static final Object NULL_SENTINEL = new Object();
    private boolean preferDynamicValues;

    @FunctionalInterface
    public interface ValueResolver {
        Optional<Object> resolve(String name);
    }

    public TemplateProcessor() {
        // Register default filters
        registerFilter("upper", String::toUpperCase);
        registerFilter("lower", String::toLowerCase);
        registerFilter("trim", String::trim);
        registerFilter("capitalize", this::capitalize);
        registerFilter("number", this::formatNumber);
        registerFilter("percent", this::formatPercent);
    }

    /**
     * Sets a template variable.
     *
     * @param name  Variable name (without $)
     * @param value Variable value
     * @return This processor for chaining
     */
    public TemplateProcessor setVariable(String name, String value) {
        variables.put(name, value);
        return this;
    }

    /**
     * Sets a template variable from any object.
     *
     * @param name  Variable name (without $)
     * @param value Variable value (will be converted to string)
     * @return This processor for chaining
     */
    public TemplateProcessor setVariable(String name, Object value) {
        variables.put(name, value);
        return this;
    }

    /**
     * Sets multiple variables at once.
     *
     * @param vars Map of variable names to values
     * @return This processor for chaining
     */
    public TemplateProcessor setVariables(Map<String, ?> vars) {
        for (Map.Entry<String, ?> entry : vars.entrySet()) {
            setVariable(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Registers a reusable component template.
     *
     * @param name     Component name (e.g., "button", "card")
     * @param template Component HTML template
     * @return This processor for chaining
     */
    public TemplateProcessor registerComponent(String name, String template) {
        components.put(name, template);
        return this;
    }

    /**
     * Registers a custom filter function.
     *
     * @param name   Filter name
     * @param filter Filter function
     * @return This processor for chaining
     */
    public TemplateProcessor registerFilter(String name, Function<String, String> filter) {
        filters.put(name, filter);
        return this;
    }

    /**
     * Processes the template, substituting variables and including components.
     *
     * @param template The template string
     * @return Processed HTML string
     */
    public String process(String template) {
        return processTemplate(template, new HashMap<>(variables));
    }

    /**
     * Processes the template using the provided UI context to resolve element IDs.
     *
     * @param template The template string
     * @param context The UI context for runtime values
     * @return Processed HTML string
     */
    public String process(String template, UIContext context) {
        ValueResolver previousResolver = this.valueResolver;
        boolean previousPreferDynamic = this.preferDynamicValues;
        this.valueResolver = name -> {
            if (context == null) {
                return Optional.empty();
            }
            Optional<Object> value = context.getValue(name);
            if (value.isPresent()) {
                return value;
            }
            return hasElement(context, name) ? Optional.of(NULL_SENTINEL) : Optional.empty();
        };
        this.preferDynamicValues = true;
        try {
            return processTemplate(template, new HashMap<>(variables));
        } finally {
            this.valueResolver = previousResolver;
            this.preferDynamicValues = previousPreferDynamic;
        }
    }

    private String processTemplate(String template, Map<String, Object> scope) {
        String result = template;

        // Process control structures first so false branches aren't expanded.
        result = processEachBlocks(result, scope);
        result = processIfBlocks(result, scope);

        // Expand components with the current scope.
        result = processComponents(result, scope);

        // Process control structures again for blocks inside component templates.
        result = processEachBlocks(result, scope);
        result = processIfBlocks(result, scope);

        // Then process variables.
        result = processVariables(result, scope);

        return result;
    }

    private String processVariables(String template, Map<String, Object> scope) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String varName = matcher.group(1);
            String filterOrDefault = matcher.group(2);

            Object rawValue = resolveVariable(scope, varName);
            String value = rawValue != null ? String.valueOf(rawValue) : "";

            // Apply filter or use default value
            if (filterOrDefault != null && !filterOrDefault.isEmpty()) {
                if (filters.containsKey(filterOrDefault)) {
                    // It's a filter
                    value = filters.get(filterOrDefault).apply(value);
                } else if (value.isEmpty()) {
                    // It's a default value
                    value = filterOrDefault;
                }
            }

            HyUIPlugin.getLog().logInfo("Template variable: $" + varName + " = " + value);
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String processEachBlocks(String template, Map<String, Object> scope) {
        StringBuilder result = new StringBuilder();
        int index = 0;

        while (true) {
            int start = template.indexOf(EACH_START, index);
            if (start < 0) {
                result.append(template.substring(index));
                break;
            }

            result.append(template, index, start);

            int startClose = template.indexOf("}}", start);
            if (startClose < 0) {
                result.append(template.substring(start));
                break;
            }

            String listName = template.substring(start + EACH_START.length(), startClose).trim();
            int end = findMatchingEnd(template, startClose + 2, EACH_START, EACH_END);
            if (end < 0) {
                result.append(template.substring(start));
                break;
            }

            String inner = template.substring(startClose + 2, end);
            Object listObj = resolveVariable(scope, listName);
            Iterable<?> items = toIterable(listObj);

            for (Object item : items) {
                Map<String, Object> childScope = new HashMap<>(scope);
                childScope.putAll(extractModelVariables(item));
                childScope.put("item", item);
                result.append(processTemplate(inner, childScope));
            }

            index = end + EACH_END.length();
        }

        return result.toString();
    }

    private String processIfBlocks(String template, Map<String, Object> scope) {
        StringBuilder result = new StringBuilder();
        int index = 0;

        while (true) {
            int start = template.indexOf(IF_START, index);
            if (start < 0) {
                result.append(template.substring(index));
                break;
            }

            result.append(template, index, start);

            int startClose = template.indexOf("}}", start);
            if (startClose < 0) {
                result.append(template.substring(start));
                break;
            }

            String conditionName = template.substring(start + IF_START.length(), startClose).trim();
            int end = findMatchingEnd(template, startClose + 2, IF_START, IF_END);
            if (end < 0) {
                result.append(template.substring(start));
                break;
            }

            int elseIndex = findElseIndex(template, startClose + 2, end);
            String trueBlock;
            String falseBlock = "";

            if (elseIndex >= 0) {
                trueBlock = template.substring(startClose + 2, elseIndex);
                falseBlock = template.substring(elseIndex + ELSE_TAG.length(), end);
            } else {
                trueBlock = template.substring(startClose + 2, end);
            }

            boolean conditionResult = evaluateCondition(conditionName, scope);
            String chosen = conditionResult ? trueBlock : falseBlock;
            result.append(processTemplate(chosen, scope));

            index = end + IF_END.length();
        }

        return result.toString();
    }

    private String processComponents(String template, Map<String, Object> scope) {
        StringBuilder result = new StringBuilder();
        int index = 0;

        while (true) {
            int start = template.indexOf("{{@", index);
            if (start < 0) {
                result.append(template.substring(index));
                break;
            }

            result.append(template, index, start);
            int cursor = start + 3;
            int depth = 1;

            while (cursor < template.length()) {
                if (template.startsWith("{{", cursor)) {
                    depth++;
                    cursor += 2;
                    continue;
                }
                if (template.startsWith("}}", cursor)) {
                    depth--;
                    if (depth == 0) {
                        break;
                    }
                    cursor += 2;
                    continue;
                }
                cursor++;
            }

            if (depth != 0) {
                result.append(template.substring(start));
                break;
            }

            String content = template.substring(start + 3, cursor).trim();
            String componentName;
            String paramsStr = null;
            int colonIndex = content.indexOf(':');
            if (colonIndex >= 0) {
                componentName = content.substring(0, colonIndex).trim();
                paramsStr = content.substring(colonIndex + 1).trim();
            } else {
                componentName = content.trim();
            }

            String componentHtml = components.get(componentName);
            if (componentHtml == null) {
                HyUIPlugin.getLog().logInfo("Unknown component: @" + componentName);
                result.append("<!-- Unknown component: ").append(componentName).append(" -->");
                index = cursor + 2;
                continue;
            }

            if (paramsStr != null && !paramsStr.isEmpty()) {
                Map<String, String> params = parseParams(paramsStr);
                for (Map.Entry<String, String> param : params.entrySet()) {
                    String rawValue = param.getValue();
                    String value = processVariables(rawValue, scope);
                    HyUIPlugin.getLog().logInfo("Component param @" + componentName + " " + param.getKey()
                            + " raw=" + rawValue + " -> " + value + " scope=" + scope.keySet());
                    componentHtml = componentHtml.replace("{{$" + param.getKey() + "}}", value);
                }
            }

            HyUIPlugin.getLog().logInfo("Including component: @" + componentName);
            result.append(componentHtml);
            index = cursor + 2;
        }

        return result.toString();
    }

    private Map<String, String> parseParams(String paramsStr) {
        Map<String, String> params = new HashMap<>();
        for (String param : paramsStr.split(",")) {
            String[] parts = param.trim().split("=", 2);
            if (parts.length == 2) {
                params.put(parts[0].trim(), parts[1].trim());
            }
        }
        return params;
    }

    private boolean evaluateCondition(String rawCondition, Map<String, Object> scope) {
        String condition = rawCondition != null ? rawCondition.trim() : "";
        if (condition.isEmpty()) {
            return false;
        }

        return evaluateLogical(condition, scope);
    }

    private boolean evaluateLogical(String condition, Map<String, Object> scope) {
        for (String orPart : splitByOperator(condition, "||")) {
            if (evaluateAnd(orPart, scope)) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateAnd(String condition, Map<String, Object> scope) {
        for (String andPart : splitByOperator(condition, "&&")) {
            if (!evaluateUnary(andPart, scope)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateUnary(String condition, Map<String, Object> scope) {
        String trimmed = condition.trim();
        if (trimmed.startsWith("!")) {
            return !evaluateUnary(trimmed.substring(1), scope);
        }

        return evaluateComparison(trimmed, scope);
    }

    private boolean evaluateComparison(String condition, Map<String, Object> scope) {
        Matcher containsMatcher = Pattern.compile("(.+?)\\s+contains\\s+(.+)").matcher(condition);
        if (containsMatcher.matches()) {
            Object left = resolveOperand(containsMatcher.group(1).trim(), scope);
            Object right = resolveOperand(containsMatcher.group(2).trim(), scope);
            return containsValue(left, right);
        }

        Matcher matcher = Pattern.compile("(.+?)(==|!=|>=|<=|>|<)(.+)").matcher(condition);
        if (matcher.matches()) {
            Object left = resolveOperand(matcher.group(1).trim(), scope);
            Object right = resolveOperand(matcher.group(3).trim(), scope);
            String operator = matcher.group(2);
            return compareValues(left, right, operator);
        }

        Object value = resolveOperand(condition, scope);
        return isTruthy(value);
    }

    private Object resolveOperand(String token, Map<String, Object> scope) {
        if (token == null) {
            return null;
        }
        String trimmed = token.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }

        if ("null".equalsIgnoreCase(trimmed)) {
            return null;
        }

        if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
            return Boolean.parseBoolean(trimmed);
        }

        try {
            if (trimmed.contains(".")) {
                return Double.parseDouble(trimmed);
            }
            return Long.parseLong(trimmed);
        } catch (NumberFormatException ignored) {
            // Not a number literal.
        }

        if (hasVariable(scope, trimmed)) {
            return resolveVariable(scope, trimmed);
        }

        return trimmed;
    }

    private boolean compareValues(Object left, Object right, String operator) {
        if (left == null || right == null) {
            if ("==".equals(operator)) {
                return left == right;
            }
            if ("!=".equals(operator)) {
                return left != right;
            }
            return false;
        }

        Double leftNum = toNumber(left);
        Double rightNum = toNumber(right);
        if (leftNum != null && rightNum != null) {
            return switch (operator) {
                case "==" -> Double.compare(leftNum, rightNum) == 0;
                case "!=" -> Double.compare(leftNum, rightNum) != 0;
                case ">" -> leftNum > rightNum;
                case "<" -> leftNum < rightNum;
                case ">=" -> leftNum >= rightNum;
                case "<=" -> leftNum <= rightNum;
                default -> false;
            };
        }

        if (left instanceof Boolean || right instanceof Boolean) {
            boolean leftVal = left instanceof Boolean ? (Boolean) left : Boolean.parseBoolean(left.toString());
            boolean rightVal = right instanceof Boolean ? (Boolean) right : Boolean.parseBoolean(right.toString());
            return switch (operator) {
                case "==" -> leftVal == rightVal;
                case "!=" -> leftVal != rightVal;
                default -> false;
            };
        }

        String leftStr = String.valueOf(left);
        String rightStr = String.valueOf(right);
        return switch (operator) {
            case "==" -> leftStr.equals(rightStr);
            case "!=" -> !leftStr.equals(rightStr);
            default -> false;
        };
    }

    private Double toNumber(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean containsValue(Object left, Object right) {
        if (left == null || right == null) {
            return false;
        }

        if (left instanceof CharSequence seq) {
            return seq.toString().contains(String.valueOf(right));
        }

        if (left instanceof Map<?, ?> map) {
            return map.containsKey(right);
        }

        if (left instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (item == null && right == null) {
                    return true;
                }
                if (item != null && item.equals(right)) {
                    return true;
                }
            }
            return false;
        }

        if (left.getClass().isArray()) {
            int length = Array.getLength(left);
            for (int i = 0; i < length; i++) {
                Object item = Array.get(left, i);
                if (item == null && right == null) {
                    return true;
                }
                if (item != null && item.equals(right)) {
                    return true;
                }
            }
            return false;
        }

        return left.toString().contains(String.valueOf(right));
    }

    private List<String> splitByOperator(String input, String operator) {
        List<String> parts = new java.util.ArrayList<>();
        boolean inSingle = false;
        boolean inDouble = false;
        int start = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"' && !inSingle) {
                inDouble = !inDouble;
                continue;
            }
            if (c == '\'' && !inDouble) {
                inSingle = !inSingle;
                continue;
            }

            if (!inSingle && !inDouble && input.startsWith(operator, i)) {
                parts.add(input.substring(start, i));
                start = i + operator.length();
                i += operator.length() - 1;
            }
        }

        parts.add(input.substring(start));
        return parts;
    }

    private boolean hasVariable(Map<String, Object> scope, String name) {
        if (name == null || name.isBlank()) {
            return false;
        }

        if (scope.containsKey(name)) {
            return true;
        }

        Optional<Object> resolved = resolveDynamicValue(name);
        if (resolved.isPresent()) {
            return true;
        }

        int dotIndex = name.indexOf('.');
        if (dotIndex > 0) {
            String root = name.substring(0, dotIndex);
            return scope.containsKey(root);
        }

        return false;
    }

    private Object resolveVariable(Map<String, Object> scope, String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        if (preferDynamicValues) {
            Optional<Object> resolved = resolveDynamicValue(name);
            if (resolved.isPresent() && resolved.get() != NULL_SENTINEL) {
                return resolved.get();
            }
        }

        if (scope.containsKey(name)) {
            return scope.get(name);
        }

        Optional<Object> resolved = resolveDynamicValue(name);
        if (resolved.isPresent()) {
            Object value = resolved.get();
            return value == NULL_SENTINEL ? null : value;
        }

        String[] path = name.split("\\.");
        if (path.length == 0) {
            return null;
        }

        String first = path[0];
        if (!scope.containsKey(first)) {
            return null;
        }

        Object current = scope.get(first);
        for (int i = 1; i < path.length; i++) {
            if (current == null) {
                return null;
            }
            current = getPropertyValue(current, path[i]);
        }

        return current;
    }

    private Optional<Object> resolveDynamicValue(String name) {
        if (valueResolver == null) {
            return Optional.empty();
        }
        return valueResolver.resolve(name);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean hasElement(UIContext context, String name) {
        return context.getById(name, (Class) UIElementBuilder.class).isPresent();
    }

    private Iterable<?> toIterable(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof Iterable<?> iterable) {
            return iterable;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> list = new java.util.ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                list.add(Array.get(value, i));
            }
            return list;
        }
        return List.of();
    }

    private int findMatchingEnd(String template, int searchFrom, String startTag, String endTag) {
        int depth = 1;
        int index = searchFrom;

        while (index < template.length()) {
            int nextStart = template.indexOf(startTag, index);
            int nextEnd = template.indexOf(endTag, index);

            if (nextEnd < 0) {
                return -1;
            }

            if (nextStart != -1 && nextStart < nextEnd) {
                depth++;
                index = nextStart + startTag.length();
            } else {
                depth--;
                if (depth == 0) {
                    return nextEnd;
                }
                index = nextEnd + endTag.length();
            }
        }

        return -1;
    }

    private int findElseIndex(String template, int searchFrom, int endIndex) {
        int depth = 1;
        int index = searchFrom;

        while (index < endIndex) {
            int nextStart = template.indexOf(IF_START, index);
            int nextEnd = template.indexOf(IF_END, index);
            int nextElse = template.indexOf(ELSE_TAG, index);

            int next = minPositive(nextStart, nextEnd, nextElse);
            if (next < 0 || next >= endIndex) {
                return -1;
            }

            if (next == nextStart) {
                depth++;
                index = nextStart + IF_START.length();
            } else if (next == nextEnd) {
                depth--;
                if (depth == 0) {
                    return -1;
                }
                index = nextEnd + IF_END.length();
            } else {
                if (depth == 1) {
                    return nextElse;
                }
                index = nextElse + ELSE_TAG.length();
            }
        }

        return -1;
    }

    private int minPositive(int... values) {
        int min = Integer.MAX_VALUE;
        for (int value : values) {
            if (value >= 0 && value < min) {
                min = value;
            }
        }
        return min == Integer.MAX_VALUE ? -1 : min;
    }

    private Map<String, Object> extractModelVariables(Object item) {
        Map<String, Object> values = new HashMap<>();
        if (item == null) {
            return values;
        }

        if (item instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() instanceof String key) {
                    values.put(key, entry.getValue());
                }
            }
            return values;
        }

        for (Field field : item.getClass().getFields()) {
            if (values.containsKey(field.getName())) {
                continue;
            }
            try {
                if (!field.canAccess(item)) {
                    field.setAccessible(true);
                }
                values.put(field.getName(), field.get(item));
            } catch (IllegalAccessException ignored) {
                // Skip inaccessible fields.
            }
        }

        for (Method method : item.getClass().getMethods()) {
            if (method.getParameterCount() != 0) {
                continue;
            }
            String name = method.getName();
            if (name.equals("getClass")) {
                continue;
            }

            String propName = null;
            if (name.startsWith("get") && name.length() > 3) {
                propName = decapitalize(name.substring(3));
            } else if (name.startsWith("is") && name.length() > 2) {
                propName = decapitalize(name.substring(2));
            }

            if (propName != null && !values.containsKey(propName)) {
                try {
                    if (!method.canAccess(item)) {
                        method.setAccessible(true);
                    }
                    values.put(propName, method.invoke(item));
                } catch (Exception ignored) {
                    // Skip getters that throw.
                }
            }
        }

        for (Field field : item.getClass().getDeclaredFields()) {
            if (field.isSynthetic() || values.containsKey(field.getName())) {
                continue;
            }
            try {
                if (!field.canAccess(item)) {
                    field.setAccessible(true);
                }
                values.put(field.getName(), field.get(item));
            } catch (IllegalAccessException ignored) {
                // Skip inaccessible fields.
            }
        }

        for (Method method : item.getClass().getDeclaredMethods()) {
            if (method.getParameterCount() != 0) {
                continue;
            }
            String name = method.getName();
            if (name.equals("getClass")) {
                continue;
            }

            String propName = null;
            if (name.startsWith("get") && name.length() > 3) {
                propName = decapitalize(name.substring(3));
            } else if (name.startsWith("is") && name.length() > 2) {
                propName = decapitalize(name.substring(2));
            }

            if (propName != null && !values.containsKey(propName)) {
                try {
                    if (!method.canAccess(item)) {
                        method.setAccessible(true);
                    }
                    values.put(propName, method.invoke(item));
                } catch (Exception ignored) {
                    // Skip getters that throw.
                }
            }
        }

        return values;
    }

    private Object getPropertyValue(Object target, String name) {
        if (target == null || name == null || name.isBlank()) {
            return null;
        }

        if (target instanceof Map<?, ?> map) {
            return map.get(name);
        }

        if (target instanceof List<?> list) {
            Integer index = parseIndex(name);
            if (index != null && index >= 0 && index < list.size()) {
                return list.get(index);
            }
            return null;
        }

        if (target.getClass().isArray()) {
            Integer index = parseIndex(name);
            if (index != null && index >= 0 && index < Array.getLength(target)) {
                return Array.get(target, index);
            }
            return null;
        }

        try {
            Field field = target.getClass().getField(name);
            if (!field.canAccess(target)) {
                field.setAccessible(true);
            }
            return field.get(target);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            // Fall back to getters.
        }

        String suffix = name.substring(0, 1).toUpperCase() + name.substring(1);
        for (String prefix : new String[] {"get", "is"}) {
            try {
                Method method = target.getClass().getMethod(prefix + suffix);
                if (method.getParameterCount() == 0) {
                    if (!method.canAccess(target)) {
                        method.setAccessible(true);
                    }
                    return method.invoke(target);
                }
            } catch (Exception ignored) {
                // Try next getter.
            }
        }

        try {
            Field field = target.getClass().getDeclaredField(name);
            if (!field.canAccess(target)) {
                field.setAccessible(true);
            }
            return field.get(target);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            // Ignore.
        }

        for (String prefix : new String[] {"get", "is"}) {
            try {
                Method method = target.getClass().getDeclaredMethod(prefix + suffix);
                if (method.getParameterCount() == 0) {
                    if (!method.canAccess(target)) {
                        method.setAccessible(true);
                    }
                    return method.invoke(target);
                }
            } catch (Exception ignored) {
                // Ignore.
            }
        }

        return null;
    }

    private Integer parseIndex(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return null;
            }
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String decapitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() > 1 && Character.isUpperCase(value.charAt(0)) && Character.isUpperCase(value.charAt(1))) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private boolean isTruthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.doubleValue() != 0;
        }
        if (value instanceof CharSequence seq) {
            String text = seq.toString().trim();
            return !text.isEmpty() && !"false".equalsIgnoreCase(text);
        }
        if (value instanceof Iterable<?> iterable) {
            return iterable.iterator().hasNext();
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) > 0;
        }
        return true;
    }

    // Default filters
    private String capitalize(String value) {
        if (value == null || value.isEmpty()) return value;
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    private String formatNumber(String value) {
        try {
            double num = Double.parseDouble(value);
            if (num == (long) num) {
                return String.format("%,d", (long) num);
            }
            return String.format("%,.2f", num);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private String formatPercent(String value) {
        try {
            double num = Double.parseDouble(value);
            return String.format("%.0f%%", num * 100);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    /**
     * Creates a new TemplateProcessor with common game-related variables.
     *
     * @param playerName The player's name
     * @return A new TemplateProcessor with player variable set
     */
    public static TemplateProcessor forPlayer(String playerName) {
        return new TemplateProcessor().setVariable("playerName", playerName);
    }
}
