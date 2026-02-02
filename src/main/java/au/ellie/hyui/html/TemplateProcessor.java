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
import au.ellie.hyui.html.ast.Evaluator;
import au.ellie.hyui.html.ast.Lexer;
import au.ellie.hyui.html.ast.Parser;
import au.ellie.hyui.html.ast.context.FilterRegistry;
import au.ellie.hyui.html.ast.context.VariableStack;
import au.ellie.hyui.html.ast.item.Node;
import au.ellie.hyui.html.ast.item.Token;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

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

    private static final Object NULL_SENTINEL = new Object();

    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, String> components = new HashMap<>();
    private final FilterRegistry filterRegistry = new FilterRegistry();
    private ValueResolver valueResolver;
    private boolean preferDynamicValues;

    /**
     * Sets a template variable from any object.
     *
     * @param name  Variable name (without $)
     * @param value Variable value (will be converted to string)
     * @return This processor for chaining
     */
    public TemplateProcessor setVariable(String name, Object value) {
        if (value instanceof Function)
            throw new RuntimeException("Use the Function overload to set a variable from a function.");

        variables.put(name, value);
        return this;
    }

    /**
     * Sets a template variable from a supplier.
     * Resolved at processing time and cached for the duration of the processing.
     *
     * @param name  Variable name (without $)
     * @param value Supplier that provides the variable value
     * @return This processor for chaining
     */
    public TemplateProcessor setVariable(String name, Supplier<?> value) {
        variables.put(name, value);
        return this;
    }

    /**
     * Sets a template variable from a function.
     * Resolved at processing time with access to the variable stack, not cached.
     *
     * @param name  Variable name (without $)
     * @param value Function that provides the variable value
     * @return This processor for chaining
     */
    public TemplateProcessor setVariable(String name, Function<VariableStack, Object> value) {
        variables.put(name, value);
        return this;
    }

    /**
     * Sets multiple template variables at once.
     *
     * @param vars Map of variable names to values
     * @return This processor for chaining
     */
    public TemplateProcessor setVariables(Map<String, ?> vars) {
        for (Map.Entry<String, ?> entry : vars.entrySet())
            setVariable(entry.getKey(), entry.getValue());
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
    public TemplateProcessor registerComponent(String name, String template) {
        components.put(name, template);
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
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("Resource path cannot be null or blank.");
        }
        String trimmed = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        String template = loadHtmlFromResources("/Common/UI/Custom/" + trimmed);
        return registerComponent(name, template);
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
     * @param template The template string.
     * @return The processed template.
     */
    public String process(String template) {
        return process(template, (Map<String, Object>) null);
    }

    /**
     * Processes the template using the provided UI context to resolve element IDs.
     *
     * @param template The template string
     * @param context  The UI context for runtime values
     * @return Processed HTML string
     */
    public String process(String template, @Nullable UIContext context) {
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
            return process(template, new HashMap<>(variables));
        } finally {
            this.valueResolver = previousResolver;
            this.preferDynamicValues = previousPreferDynamic;
        }
    }

    public String process(String template, @Nullable Map<String, Object> additionalVariables) {
        // Lexer / Parser
        List<Token> tokens = new Lexer(template).tokenize();
        List<Node> ast = new Parser(tokens).parse();

        // Inject additional variables, this allows for per-call variable overrides
        Map<String, Object> parameters = additionalVariables == null ? variables : new HashMap<>(variables);
        if (additionalVariables != null)
            parameters.putAll(additionalVariables);

        // Evaluator
        return new Evaluator(parameters, filterRegistry).evaluate(ast);
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
}
