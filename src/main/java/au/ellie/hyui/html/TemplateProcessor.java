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

package au.ellie.hyui.html;

import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.html.template.Evaluator;
import au.ellie.hyui.html.template.Lexer;
import au.ellie.hyui.html.template.Parser;
import au.ellie.hyui.html.template.context.ExecutionPolicy;
import au.ellie.hyui.html.template.context.FilterRegistry;
import au.ellie.hyui.html.template.context.VariableHandler.CachingVariableHandler;
import au.ellie.hyui.html.template.context.VariableHandler.EphemeralVariableHandler;
import au.ellie.hyui.html.template.context.VariableHandler.NonNullVariableHandler;
import au.ellie.hyui.html.template.context.VariableStack;
import au.ellie.hyui.html.template.context.VariableStack.VariableScope;
import au.ellie.hyui.html.template.item.Node;
import au.ellie.hyui.html.template.item.Token;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static au.ellie.hyui.html.template.context.VariableStack.NULL_SENTINEL;
import static au.ellie.hyui.html.template.item.Symbols.SCOPE_ROOT_NAME;

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
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, CachedComponent> components = new HashMap<>();
    private final FilterRegistry filterRegistry = new FilterRegistry();
    private final CachedComponent root = new CachedComponent();

    private ValueResolver valueResolver;
    private boolean preferDynamicValues;

    /**
     * Sets the template string to be processed.
     *
     * @param template The template string
     */
    public TemplateProcessor setTemplate(String template) {
        root.setTemplate(template);

        return this;
    }

    /**
     * Registers a template variable backed by an arbitrary object.
     * <p>
     *
     * @param name  Variable name (without the '$' prefix)
     * @param value Variable value
     * @return This {@link TemplateProcessor} instance, allowing method chaining
     */
    public TemplateProcessor setVariable(String name, Object value) {
        return setVariable(name, value, ExecutionPolicy.CACHED);
    }

    /**
     * Registers a template variable backed by an arbitrary object.
     * <p>
     * The behavior of the evaluation is controlled by the provided {@link ExecutionPolicy}.
     *
     * @param name   The variable name (without the '$' prefix)
     * @param value  Variable value
     * @param policy The execution policy controlling how the value is evaluated and retained
     * @return This {@link TemplateProcessor} instance, allowing method chaining
     */
    public TemplateProcessor setVariable(String name, Object value, ExecutionPolicy policy) {
        var result = switch (policy) {
            case CACHED -> new CachingVariableHandler(value);
            case NON_NULL -> new NonNullVariableHandler(value);
            case EPHEMERAL -> new EphemeralVariableHandler(value);
            default -> value;
        };

        variables.put(name, result);
        return this;
    }

    /**
     * Registers a template variable whose value is evaluated lazily using a {@link Supplier}.
     * <p>
     * The behavior of the evaluation is controlled by the provided {@link ExecutionPolicy}.
     *
     * @param name   The variable name (without the '$' prefix)
     * @param value  A {@link Supplier} that provides the variable's value
     * @param policy The execution policy controlling how the value is evaluated and retained
     * @return This {@link TemplateProcessor} instance, allowing method chaining
     */
    public TemplateProcessor setVariable(String name, Supplier<Object> value, ExecutionPolicy policy) {
        return setVariable(name, (Object) value, policy);
    }

    /**
     * Registers a template variable whose value is evaluated lazily using a {@link Function}.
     * <p>
     * The behavior of the evaluation is controlled by the provided {@link ExecutionPolicy}.
     *
     * @param name   The variable name (without the '$' prefix)
     * @param value  A {@link Function} that provides the variable's value
     * @param policy The execution policy controlling how the value is evaluated and retained
     * @return This {@link TemplateProcessor} instance, allowing method chaining
     */
    public TemplateProcessor setVariable(String name, Function<VariableStack, Object> value, ExecutionPolicy policy) {
        return setVariable(name, (Object) value, policy);
    }

    /**
     * Sets multiple template variables at once.
     *
     * @param vars Map of variable names to values
     * @return This processor for chaining
     */
    public TemplateProcessor setVariables(Map<String, ?> vars) {
        for (Map.Entry<String, ?> entry : vars.entrySet())
            setVariable(entry.getKey(), entry.getValue(), ExecutionPolicy.CACHED);

        return this;
    }

    /**
     * Register a new filter.
     *
     * @param name   The name of the filter.
     * @param filter The filter implementation.
     */
    public TemplateProcessor registerFilter(String name, FilterRegistry.Filter filter) {
        filterRegistry.register(name, filter);

        return this;
    }

    /**
     * Registers a reusable component template.
     *
     * @param name     Component name (e.g., "button", "card")
     * @param template Component HTML template
     * @return This processor for chaining
     */
    public TemplateProcessor registerComponent(@Nonnull String name, @Nonnull String template) {
        assert !name.isEmpty() : "Component name cannot be empty.";
        assert !template.isEmpty() : "Component template cannot be empty.";


        var cache = components.computeIfAbsent(name, _ -> new CachedComponent());
        var updated = cache.setTemplate(template);

        // Invalidate other components cache
        if (updated && root.invalidate())
            for (Map.Entry<String, CachedComponent> entry : components.entrySet())
                entry.getValue().invalidate();

        return this;
    }

    /**
     * Registers a reusable component template loaded from resources.
     *
     * @param name         Component name (e.g., "button", "card")
     * @param resourcePath Resource path to the component HTML - located in Common/UI/Custom/.
     * @return This processor for chaining
     */
    public TemplateProcessor registerComponentFromFile(String name, String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank())
            throw new IllegalArgumentException("Resource path cannot be null or blank.");

        String trimmed = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        String template = loadHtmlFromResources("/Common/UI/Custom/" + trimmed);
        return registerComponent(name, template);
    }

    /**
     * Process a template with the current variables.
     *
     * @return The processed template.
     */
    public String process() {
        return process((Map<String, Object>) null);
    }

    /**
     * Processes the template using the provided UI context to resolve element IDs.
     *
     * @param context The UI context for runtime values
     * @return Processed HTML string
     */
    public String process(@Nullable UIContext context) {
        ValueResolver previousResolver = this.valueResolver;
        boolean previousPreferDynamic = this.preferDynamicValues;
        this.valueResolver = name -> {
            if (context == null)
                return Optional.empty();

            Optional<Object> value = context.getValue(name);
            if (value.isPresent())
                return value;

            return hasElement(context, name) ? Optional.of(NULL_SENTINEL) : Optional.empty();
        };

        this.preferDynamicValues = true;
        try {
            return process(new HashMap<>(variables));
        } finally {
            this.valueResolver = previousResolver;
            this.preferDynamicValues = previousPreferDynamic;
        }
    }

    /**
     * Processes the template with additional variables that can override existing ones.
     *
     * @param additionalVariables Additional variables to use during processing
     * @return The processed template.
     */
    public String process(@Nullable Map<String, Object> additionalVariables) {
        // Inject additional variables, this allows for per-call variable overrides
        Map<String, Object> parameters = additionalVariables == null ? variables : new HashMap<>(variables);
        if (additionalVariables != null)
            parameters.putAll(additionalVariables);

        var rootAst = this.root.getAst();
        var scope = new VariableScope(SCOPE_ROOT_NAME, parameters);
        var stack = new VariableStack(scope, valueResolver, preferDynamicValues);

        return new Evaluator(stack, filterRegistry, components).evaluate(rootAst);
    }

    // ===== Internal =====

    /**
     * Load HTML content from resource files.
     *
     * @param resourceFileName The resource file name/path.
     * @return The HTML content as a string.
     */
    private String loadHtmlFromResources(String resourceFileName) {
        if (resourceFileName == null || resourceFileName.isBlank())
            throw new IllegalArgumentException("Resource path cannot be null or blank.");

        String normalized = resourceFileName.startsWith("/") ? resourceFileName.substring(1) : resourceFileName;
        List<Path> candidatePaths = List.of(
                Paths.get("src/main/resources").resolve(normalized),
                Paths.get("..", "src", "main", "resources").resolve(normalized),
                Paths.get("build/resources/main").resolve(normalized),
                Paths.get("..", "build", "resources", "main").resolve(normalized),
                Paths.get(normalized)
        );

        for (Path path : candidatePaths) {
            if (Files.isRegularFile(path)) {
                try {
                    return Files.readString(path, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load HTML from file: " + path, e);
                }
            }
        }

        String resourceLookup = resourceFileName.startsWith("/") ? resourceFileName : "/" + resourceFileName;
        try (InputStream inputStream = TemplateProcessor.class.getResourceAsStream(resourceLookup)) {
            if (inputStream == null)
                throw new IllegalArgumentException("Resource not found: " + resourceFileName);

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load HTML from resource: " + resourceFileName, e);
        }
    }

    /**
     * Check if the UI context has an element with the given name.
     *
     * @param context UI context
     * @param name    Element name
     */
    @SuppressWarnings("unchecked")
    private boolean hasElement(UIContext context, String name) {
        return context.getById(name, UIElementBuilder.class).isPresent();
    }

    // ===== Interface =====

    @FunctionalInterface
    public interface ValueResolver {
        Optional<Object> resolve(String name);
    }

    public static class CachedComponent {
        private List<Node> ast;
        private String template;

        public CachedComponent() {
            this.template = "";
        }

        /**
         * Gets the current template string for this component.
         */
        public String getTemplate() {
            return template;
        }

        /**
         * Sets the template string for this component
         * and invalidates the cached AST if the template has changed.
         *
         * @param template The new template string
         * @return True if the template was updated, false if the template was unchanged.
         */
        public boolean setTemplate(String template) {
            if (!Objects.equals(template, this.template)) {
                this.template = template;
                this.ast = null; // Invalidate cache
                return true;
            }

            return false;
        }

        /**
         * Invalidates the cached AST for this component.
         * Should be called if the template is modified externally after being set.
         *
         * @return True if the cache was invalidated, false if it was already null.
         */
        public boolean invalidate() {
            boolean wasCached = this.ast != null;
            ast = null;

            return wasCached;
        }

        /**
         * Retrieve the AST for this component,
         * parsing the template if it hasn't been parsed yet.
         *
         * @return The built template processor.
         */
        public List<Node> getAst() {
            if (ast == null) {
                List<Token> tokens = new Lexer(template).tokenize();
                ast = new Parser(tokens, template).parse();
            }

            return ast;
        }
    }
}
