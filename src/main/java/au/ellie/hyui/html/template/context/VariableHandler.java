package au.ellie.hyui.html.template.context;

import au.ellie.hyui.html.template.context.VariableStack.VariableScope;

public interface VariableHandler {

    /**
     * Retrieve the variable stored in the handler
     */
    Object get();

    /**
     * Process the value after transformation,
     * allowing for custom handling of the variable.
     *
     * @param key   The key associated with the variable
     * @param value The stored value after transformation
     * @param scope The {@link VariableScope} used to retrieve the variable
     */
    void handle(String key, Object value, VariableScope scope);

    /**
     * A simple implementation of VariableHandler that caches the value.
     * This handle simply stores the value in the {@link VariableScope} without any additional processing.
     */
    class CachingVariableHandler implements VariableHandler {
        private final Object cachedValue;

        public CachingVariableHandler(Object value) {
            this.cachedValue = value;
        }

        @Override
        public Object get() {
            return cachedValue;
        }

        @Override
        public void handle(String key, Object value, VariableScope scope) {
            scope.put(key, value);
        }
    }

    /**
     * A simple implementation of VariableHandler that delete the variable if the value is null.
     * This handle remove the key from the {@link VariableScope} if the value is null, otherwise it does nothing.
     */
    class NonNullVariableHandler implements VariableHandler {
        private final Object cachedValue;

        public NonNullVariableHandler(Object value) {
            this.cachedValue = value;
        }

        @Override
        public Object get() {
            return cachedValue;
        }

        @Override
        public void handle(String key, Object value, VariableScope scope) {
            if (value == null)
                scope.remove(key);
        }
    }

    /**
     * A simple implementation of VariableHandler that delete the variable after the first access.
     * This handle remove the key from the {@link VariableScope} after the first retrieval of the variable,
     * allowing for one-time use variables.
     */
    class EphemeralVariableHandler implements VariableHandler {
        private final Object cachedValue;

        public EphemeralVariableHandler(Object value) {
            this.cachedValue = value;
        }

        @Override
        public Object get() {
            return cachedValue;
        }

        @Override
        public void handle(String key, Object value, VariableScope scope) {
            scope.remove(key);
        }
    }
}
