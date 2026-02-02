package au.ellie.hyui.html.ast.context;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public interface VariableStack {

    /**
     * Retrieve a variable from the stack.
     *
     * @param name Variable name
     * @return Variable value, or null if not found
     */
    Object getVariable(String name);

    /**
     * Retrieve a variable from the stack, with a default value.
     *
     * @param name         Variable name
     * @param defaultValue Default value if variable not found
     * @return Variable value, or defaultValue if not found
     */
    Object getVariable(String name, Object defaultValue);

    // ========== INTERNAL RECORDS ==========

    sealed interface VariableValue permits Value, Lazy, Computed {
    }

    record Value(Object value) implements VariableValue {
    }

    record Lazy(Supplier<?> supplier) implements VariableValue {
    }

    record Computed(Function<VariableStack, ?> function) implements VariableValue {
    }

    // ========== IMPLEMENTATION ==========

    class VariableStackImpl implements VariableStack {
        private final ArrayDeque<Map<String, Object>> stack = new ArrayDeque<>();

        public VariableStackImpl(Map<String, Object> globalScope) {
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

        public Object getVariable(String name) {
            return getVariable(name, null);
        }

        public Object getVariable(String name, Object defaultValue) {
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

            return defaultValue;
        }
    }
}
