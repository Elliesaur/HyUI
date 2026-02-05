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

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class VariableStack {
    public static final Object NULL_SENTINEL = new Object();

    private final ArrayDeque<Map<String, Object>> stack = new ArrayDeque<>();
    private final ValueResolver valueResolver;
    private final boolean preferDynamicValues;

    public VariableStack(Map<String, Object> globalScope, @Nullable ValueResolver valueResolver, boolean preferDynamicValues) {
        this.valueResolver = valueResolver;
        this.preferDynamicValues = preferDynamicValues;

        pushScope(globalScope);
    }

    public void pushScope(Map<String, Object> scope) {
        stack.push(scope);
    }

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
    public Object getVariable(String name, Object defaultValue) {
        // Check dynamic values first if preferred
        if (preferDynamicValues && valueResolver != null) {
            Optional<Object> resolved = valueResolver.resolve(name);
            if (resolved.isPresent() && resolved.get() != NULL_SENTINEL)
                return resolved;
        }

        for (Map<String, Object> scope : stack) {
            if (scope.containsKey(name)) {
                var object = scope.get(name);

                switch (object) {
                    case Supplier<?> supplier -> {
                        object = supplier.get();
                        scope.put(name, object);
                        return object;
                    }
                    case Function<?, ?> function -> {
                        @SuppressWarnings("unchecked")
                        Function<VariableStack, ?> stackFunction =
                                (Function<VariableStack, ?>) function;

                        return stackFunction.apply(this);
                    }
                    case null -> {
                        return null;
                    }
                    default -> {
                        return object;
                    }
                }
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

        return defaultValue;
    }
}
