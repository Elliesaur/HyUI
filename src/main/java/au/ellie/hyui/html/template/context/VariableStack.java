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

package au.ellie.hyui.html.template.context;

import au.ellie.hyui.html.TemplateProcessor.ValueResolver;
import au.ellie.hyui.utils.LambdaUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class VariableStack {
    public static final Object NULL_SENTINEL = new Object();

    private final ArrayDeque<VariableScope> stack = new ArrayDeque<>();
    private final ValueResolver valueResolver;
    private final boolean preferDynamicValues;

    public VariableStack(VariableScope globalScope, @Nullable ValueResolver valueResolver, boolean preferDynamicValues) {
        this.valueResolver = valueResolver;
        this.preferDynamicValues = preferDynamicValues;

        pushScope(globalScope);
    }

    /**
     * Push a new scope onto the stack.
     *
     * @param scope Scope to push
     */
    public void pushScope(VariableScope scope) {
        stack.push(scope);
    }

    /**
     * Pop the current scope from the stack.
     * Cannot pop the global scope.
     */
    public void popScope() {
        if (stack.size() > 1)
            stack.pop();
        else
            throw new IllegalStateException("Cannot pop the global scope");
    }

    /**
     * Retrieve a variable from the stack.
     *
     * @param name Variable name
     * @return Variable value, or null if not found
     */
    public Object getVariable(String name) {
        return getVariable(name, null);
    }

    /**
     * Retrieve a variable from the stack, with a default value.
     *
     * @param name         Variable name
     * @param defaultValue Default value if variable not found
     * @return Variable value, or defaultValue if not found
     */
    public Object getVariable(String name, Supplier<Object> defaultValue) {
        // Check dynamic values first if preferred
        if (preferDynamicValues && valueResolver != null) {
            Optional<Object> resolved = valueResolver.resolve(name);
            if (resolved.isPresent() && resolved.get() != NULL_SENTINEL)
                return resolved;
        }

        for (VariableScope scope : stack) {
            if (scope.containsKey(name)) {
                var object = scope.get(name);
                if (object == null)
                    return null;

                if (object instanceof VariableHandler handler) {
                    object = resolveVariable(handler.get(), defaultValue);
                    handler.handle(name, object, scope);
                } else
                    object = resolveVariable(object, defaultValue);

                if (object instanceof Optional<?> optional)
                    return optional.orElse(null);

                return object;
            }
        }

        // Check dynamic values last if not preferred
        if (valueResolver != null) {
            Optional<Object> resolved = valueResolver.resolve(name);
            if (resolved.isPresent()) {
                Object value = resolved.get();
                return value == NULL_SENTINEL ? null : value;
            }
        }

        return defaultValue.get();
    }

    /**
     * Get the name of the current scope.
     */
    @Nonnull
    public String getScopeName() {
        var scope = stack.peek();

        return scope != null ? scope.getName() : "none";
    }

    /**
     * Check if the current scope has the given name.
     *
     * @param name Scope name to check
     */
    public boolean isScope(String name) {
        var scope = stack.peek();

        return scope != null && scope.getName().equals(name);
    }

    /**
     * Get the keys of the current scope, if available.
     */
    public Set<String> getScopeKeys() {
        var scope = stack.peek();

        return scope != null ? scope.getKeys() : null;
    }

    /**
     * Resolve a variable that may be a function or supplier.
     *
     * @param object       The variable value to resolve
     * @param defaultValue Default value supplier for function calls
     * @return Resolved variable value
     */
    private Object resolveVariable(Object object, Supplier<Object> defaultValue) {
        if (LambdaUtils.isFunction(object))
            object = LambdaUtils.call(object, this, defaultValue);

        return object;
    }

    // ===== Scope =====

    public static class VariableScope {

        private final Map<String, Object> content;
        private final Set<String> keys;
        private final String name;

        public VariableScope(String name) {
            this(name, new HashMap<>(), new HashSet<>());
        }

        public VariableScope(String name, Map<String, Object> content) {
            this(name, content, new HashSet<>());
        }

        public VariableScope(String name, Map<String, Object> content, @Nonnull Set<String> keys) {
            this.name = name;
            this.content = content;
            this.keys = keys;
        }

        public String getName() {
            return name;
        }

        public Map<String, Object> getContent() {
            return content;
        }

        public Set<String> getKeys() {
            return keys;
        }

        public boolean containsKey(String key) {
            return content.containsKey(key);
        }

        public Object get(String key) {
            return content.get(key);
        }

        @SuppressWarnings("unchecked")
        public <T> T computeIfAbsent(String key, Function<String, T> defaultValue) {
            return (T) content.computeIfAbsent(key, defaultValue);
        }

        public void put(String key, Object value) {
            content.put(key, value);
        }

        public void remove(String key) {
            content.remove(key);
            keys.remove(key);
        }

        public void putKeyed(String key, Object value) {
            content.put(key, value);
            keys.add(key);
        }
    }
}
