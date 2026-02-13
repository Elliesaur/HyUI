package au.ellie.hyui.html;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static au.ellie.hyui.html.template.context.ExecutionPolicy.*;
import static au.ellie.hyui.html.template.item.Symbols.SCOPE_EACH_NAME;
import static org.junit.jupiter.api.Assertions.*;

class TemplateProcessorTest {
    private TemplateProcessor processor;

    // ========== UTILITY METHODS ==========

    /**
     * Normalizes indentation by removing common leading whitespace from all lines.
     * This allows templates in tests to be written with natural indentation.
     */
    private static String normalize(String input) {
        if (input == null || input.isBlank()) return "";

        var lines = input.lines().toList();
        var minIndent = lines.stream()
                .filter(line -> !line.isBlank())
                .mapToInt(line -> {
                    int i = 0;
                    while (i < line.length() && Character.isWhitespace(line.charAt(i))) i++;
                    return i;
                })
                .min()
                .orElse(0);

        return String.join("\n", lines.stream()
                        .map(line -> line.length() >= minIndent ? line.substring(minIndent) : line)
                        .dropWhile(String::isBlank)
                        .toList())
                .stripTrailing();
    }

    @BeforeEach
    void setUp() {
        processor = new TemplateProcessor();
    }

    // ========== TEST DATA RECORDS ==========

    record Address(String city, String country) {
    }

    record Person(String name, int age, Address address) {
    }

    record User(String name, String lastName) {
    }

    record Category(String name, List<String> items) {
    }

    record Item(String name, boolean active, String display, int count) {
    }

    record Product(String name, List<String> tags) {
        public String getTagsWithPrefix(String prefix) {
            return tags.stream().map(tag -> prefix + tag).collect(Collectors.joining(", "));
        }
    }

    class Modulator {
        public int size;

        public int increment() {
            size = (size + 1) % 2;
            return size;
        }
    }

    // ========== BASIC FUNCTIONALITY ==========

    @Nested
    @DisplayName("Basic Template Processing")
    class BasicProcessing {

        @Test
        @DisplayName("Should return plain text unchanged")
        void plainText() {
            String template = "<div>Hello World</div>";
            assertEquals("<div>Hello World</div>", processor.setTemplate(template).process());
        }

        @Test
        @DisplayName("Should replace simple variables")
        void simpleVariable() {
            processor.setVariable("name", "John");

            processor.setTemplate("Hello {{$name}}!");
            assertEquals("Hello John!", processor.process());
        }

        @Test
        @DisplayName("Should handle missing variables as empty strings")
        void missingVariable() {
            processor.setTemplate("Hello {{$name}}!");
            assertEquals("Hello !", processor.process());
        }

        @Test
        @DisplayName("Should support variables with hyphens and underscores")
        void variableNaming() {
            processor.setVariable("my-var", "value1");
            processor.setVariable("my_var", "value2");

            processor.setTemplate("{{$my-var}} {{$my_var}}");
            assertEquals("value1 value2", processor.process());
        }

        @Test
        @DisplayName("Should preserve intentional whitespace in HTML")
        void whitespacePreservation() {
            processor.setTemplate("<div>  Hello  </div>");
            assertEquals("<div>  Hello  </div>", processor.process());
        }
    }

    // ========== LITERALS ==========

    @Nested
    @DisplayName("Literal Values")
    class Literals {

        @Test
        @DisplayName("Should handle string literals with escaping")
        void stringLiterals() {
            processor.setTemplate("{{\"Hello World\"}}");
            assertEquals("Hello World", processor.process());

            processor.setTemplate("{{\"Hello \\\"World\\\"\"}}");
            assertEquals("Hello \"World\"", processor.process());
        }

        @Test
        @DisplayName("Should handle single-quoted string literals")
        void singleQuotedStringLiterals() {
            processor.setTemplate("{{'Hello World'}}");
            assertEquals("Hello World", processor.process());

            processor.setTemplate("{{'Hello \\'World\\''}}");
            assertEquals("Hello 'World'", processor.process());
        }

        @Test
        @DisplayName("Should handle single quotes in comparisons")
        void singleQuotesInComparisons() {
            processor.setVariable("name", "Alice");
            processor.setTemplate("{{if $name == 'Alice'}}Match{{else}}No match{{/if}}");
            assertEquals("Match", processor.process());

            processor.setVariable("name", "Bob");
            processor.setTemplate("{{if $name == 'Alice'}}Match{{else}}No match{{/if}}");
            assertEquals("No match", processor.process());
        }

        @ParameterizedTest
        @CsvSource({"{{42}}", "{{3.14}}", "{{-5}}"})
        @DisplayName("Should handle numeric literals")
        void numericLiterals(String template) {
            processor.setTemplate(template);
            var result = processor.process();

            assertNotNull(result);
            assertFalse(result.isBlank());
        }

        @Test
        @DisplayName("Should handle boolean literals")
        void booleanLiterals() {
            processor.setTemplate("{{true}} {{false}}");
            assertEquals("true false", processor.process());
        }
    }

    // ========== PROPERTY ACCESS ==========

    @Nested
    @DisplayName("Property Access")
    class PropertyAccess {

        @Test
        @DisplayName("Should access record properties")
        void recordProperties() {
            processor.setVariable("user", new Person("Alice", 30, null));

            processor.setTemplate("{{$user.name}} is {{$user.age}}");
            assertEquals("Alice is 30", processor.process());
        }

        @Test
        @DisplayName("Should access map properties")
        void mapProperties() {
            processor.setVariable("user", Map.of("name", "Bob", "age", 25));

            processor.setTemplate("{{$user.name}} is {{$user.age}}");
            assertEquals("Bob is 25", processor.process());
        }

        @Test
        @DisplayName("Should access nested properties")
        void nestedProperties() {
            processor.setVariable("user", new Person("Charlie", 21, new Address("Paris", "France")));

            processor.setTemplate("{{$user.address.city}}, {{$user.address.country}}");
            assertEquals("Paris, France", processor.process());
        }

        @Test
        @DisplayName("Should return empty string for missing properties")
        void missingProperties() {
            processor.setVariable("user", new Person("Dave", 32, null));

            processor.setTemplate("{{$user.id}}");
            assertEquals("", processor.process());
        }

        @ParameterizedTest
        @CsvSource({
                "false, 0, ",
                "true, 1, value_1 - value_1"
        })
        @DisplayName("Should ensure the variable is evaluated only once and cached")
        void policyCachedEvaluation(boolean condition, int value, String expected) {
            AtomicInteger evaluations = new AtomicInteger();

            processor.setVariable("enabled", condition);
            processor.setVariable("secret", () -> {
                evaluations.incrementAndGet();
                return "value_" + evaluations;
            }, CACHED);

            processor.setTemplate("""
                    {{if $enabled}}
                    {{$secret}} - {{$secret}}
                    {{/if}}
                    """);
            assertEquals(expected != null ? expected : "", processor.process());
            assertEquals(value, evaluations.get());
        }

        @ParameterizedTest
        @CsvSource({
                "false, 0, ",
                "true, 2, value_1 - value_2"
        })
        @DisplayName("Should ensure the variable is evaluated every time it's accessed")
        void policyDynamicEvaluation(boolean condition, int value, String expected) {
            AtomicInteger evaluations = new AtomicInteger();

            processor.setVariable("enabled", condition);
            processor.setVariable("secret", (_) -> "value_" + evaluations.incrementAndGet(), DYNAMIC);

            processor.setTemplate("""
                    {{if $enabled}}
                    {{$secret}} - {{$secret}}
                    {{/if}}
                    """);
            assertEquals(expected != null ? expected : "", processor.process());
            assertEquals(value, evaluations.get());
        }

        @ParameterizedTest
        @CsvSource({
                "false, secret value",
                "true, secret already revealed: disappeared"
        })
        @DisplayName("Should ensure the variable is evaluated only once and then removed")
        void policyEphemeralEvaluation(boolean condition, String expected) {
            processor.setVariable("condition", condition);
            processor.setVariable("secret", (_) -> "secret value", EPHEMERAL);

            processor.setTemplate("""
                    {{if $condition && $secret}}{{/if}}
                    {{$secret ?? "secret already revealed: disappeared"}}
                    """);
            assertEquals(expected, processor.process().trim());
        }

        @Test
        @DisplayName("Should ensure the variable is evaluated until it returns null, then removed")
        void policyNonNullEvaluation() {
            AtomicInteger evaluations = new AtomicInteger();

            processor.setVariable("loop", List.of(1, 2, 3, 4));
            processor.setVariable("secret", (_) -> {
                var value = evaluations.incrementAndGet();
                if (value == 3)
                    return null; // Simulate a value that disappears after 2 uses

                return "This is a value that can only be twice once, " + (2 - value) + " remaining";
            }, NON_NULL);

            processor.setTemplate("""
                    {{each $loop}}
                    {{$secret ?? "disappeared"}}
                    {{/each}}
                    """);
            assertEquals(normalize("""
                    This is a value that can only be twice once, 1 remaining
                    This is a value that can only be twice once, 0 remaining
                    disappeared
                    disappeared
                    """), processor.process());
        }
    }

    // ========== COMPARISON OPERATORS ==========

    @Nested
    @DisplayName("Comparison Operators")
    class ComparisonOperators {

        @ParameterizedTest
        @CsvSource({
                "5, ==, 5, true", "5, ==, 3, false",
                "5, !=, 3, true", "5, !=, 5, false",
                "5, <, 10, true", "5, <, 3, false",
                "5, >, 3, true", "5, >, 10, false",
                "5, <=, 5, true", "5, <=, 3, false",
                "5, >=, 5, true", "5, >=, 3, true"
        })
        @DisplayName("Should evaluate comparison operators correctly")
        void comparisonOperators(int left, String op, int right, boolean expected) {
            processor.setVariable("a", left);
            processor.setVariable("b", right);

            processor.setTemplate("{{$a " + op + " $b}}");
            assertEquals(String.valueOf(expected), processor.process());
        }

        @Test
        @DisplayName("Should compare strings")
        void stringComparison() {
            processor.setVariable("name", "Alice");

            processor.setTemplate("{{$name == \"Alice\"}}");
            assertEquals("true", processor.process());

            processor.setTemplate("{{$name == \"Bob\"}}");
            assertEquals("false", processor.process());
        }

        @Test
        @DisplayName("Should handle numeric type mixing (int, long, double)")
        void numericTypeMixing() {
            processor.setVariable("a", 5);
            processor.setVariable("b", 5.0);

            processor.setTemplate("{{$a == $b}}");
            assertEquals("true", processor.process());

            processor.setTemplate("{{$a != $b}}");
            assertEquals("false", processor.process());
        }

        @Test
        @DisplayName("Should handle floating-point comparison with epsilon")
        void floatingPointEpsilon() {
            processor.setVariable("a", 0.1 + 0.2);
            processor.setVariable("b", 0.3);

            processor.setTemplate("{{$a == $b}}");
            assertEquals("true", processor.process());
        }

        @Test
        @DisplayName("Should handle null comparisons")
        void nullComparison() {
            processor.setTemplate("{{$value == $missing}}");
            assertEquals("true", processor.process());
        }
    }

    // ========== LOGICAL OPERATORS ==========

    @Nested
    @DisplayName("Logical Operators")
    class LogicalOperators {

        @Test
        @DisplayName("Should evaluate AND operator")
        void andOperator() {
            processor.setVariable("a", true);
            processor.setVariable("b", true);
            processor.setVariable("c", false);

            processor.setTemplate("{{$a && $b}}");
            assertEquals("true", processor.process());

            processor.setTemplate("{{$a && $c}}");
            assertEquals("false", processor.process());

            processor.setTemplate("{{$c && $c}}");
            assertEquals("false", processor.process());
        }

        @Test
        @DisplayName("Should evaluate OR operator")
        void orOperator() {
            processor.setVariable("a", true);
            processor.setVariable("b", false);

            processor.setTemplate("{{$a || $b}}");
            assertEquals("true", processor.process());

            processor.setTemplate("{{$b || $b}}");
            assertEquals("false", processor.process());
        }

        @Test
        @DisplayName("Should combine AND and OR operators")
        void combinedLogicalOperators() {
            processor.setVariable("a", true);
            processor.setVariable("b", false);
            processor.setVariable("c", true);

            processor.setTemplate("{{$a && $b || $c}}");
            assertEquals("true", processor.process());
        }

        @ParameterizedTest
        @CsvSource({
                "'', false",
                "Hello, true",
                "0, false",
                "5, true"
        })
        @DisplayName("Should evaluate truthiness correctly")
        void truthiness(String value, boolean isTruthy) {
            if (value.isEmpty())
                processor.setVariable("val", "");
            else if (value.matches("\\d+"))
                processor.setVariable("val", Integer.parseInt(value));
            else
                processor.setVariable("val", value);

            processor.setTemplate("{{if $val}}true{{/if}}");
            assertEquals(isTruthy ? "true" : "", processor.process());
        }
    }

    // ========== IN OPERATOR ==========

    @Nested
    @DisplayName("IN Operator")
    class InOperator {

        @Test
        @DisplayName("Should check presence in list")
        void listContains() {
            processor.setVariable("items", List.of("apple", "banana", "cherry"));

            processor.setTemplate("{{\"apple\" in $items}}");
            assertEquals("true", processor.process());

            processor.setTemplate("{{\"orange\" in $items}}");
            assertEquals("false", processor.process());
        }

        @Test
        @DisplayName("Should check key presence in map")
        void mapContainsKey() {
            processor.setVariable("user", Map.of("name", "Alice", "age", 30));

            processor.setTemplate("{{\"name\" in $user}}");
            assertEquals("true", processor.process());

            processor.setTemplate("{{\"email\" in $user}}");
            assertEquals("false", processor.process());
        }

        @Test
        @DisplayName("Should check substring in string")
        void stringContains() {
            processor.setVariable("text", "Hello World");

            processor.setTemplate("{{\"World\" in $text}}");
            assertEquals("true", processor.process());

            processor.setTemplate("{{\"Java\" in $text}}");
            assertEquals("false", processor.process());
        }
    }

    // ========== FILTERS ==========

    @Nested
    @DisplayName("Filter transformations")
    class Filters {

        @ParameterizedTest
        @CsvSource({
                "john, uppercase, JOHN",
                "JANE, lowercase, jane",
                "alice, capitalize, Alice",
                "'  Hello  ', trim, Hello"
        })
        @DisplayName("Should apply built-in filters")
        void builtInFilters(String input, String filter, String expected) {
            processor.setVariable("value", input);

            processor.setTemplate("{{$value | " + filter + "}}");
            assertEquals(expected, processor.process());
        }

        @Test
        @DisplayName("Should chain multiple filters")
        void chainedFilters() {
            processor.setVariable("name", "  john doe  ");

            processor.setTemplate("{{$name | trim | uppercase}}");
            assertEquals("JOHN DOE", processor.process());
        }

        @Test
        @DisplayName("Should support custom filters")
        void customFilter() {
            processor.registerFilter("reverse", value ->
                    value == null ? null : new StringBuilder(value.toString()).reverse().toString()
            );
            processor.setVariable("text", "Hello");

            processor.setTemplate("{{$text | reverse}}");
            assertEquals("olleH", processor.process());
        }

        @Test
        @DisplayName("Should apply length filter to strings and collections")
        void lengthFilter() {
            processor.setVariable("text", "Hello");
            processor.setVariable("items", List.of("a", "b", "c"));

            processor.setTemplate("{{$text | length}}");
            assertEquals("5", processor.process());

            processor.setTemplate("{{$items | length}}");
            assertEquals("3", processor.process());
        }

        @Test
        @DisplayName("Should format numbers with number filter")
        void number_formatsNumber() {
            processor.setVariable("value", 1234);

            processor.setTemplate("{{$value | number}}");
            assertEquals("1,234", processor.process());
        }

        @Test
        @DisplayName("Should format percentages with percent filter")
        void percent_formatsPercent() {
            processor.setVariable("value", 0.125);

            processor.setTemplate("{{$value | percent}}");
            assertEquals("13%", processor.process());
        }
    }

    // ========== DEFAULT VALUES ==========

    @Nested
    @DisplayName("Default Values (Nullish Coalescing)")
    class DefaultValues {

        @Test
        @DisplayName("Should use first non-null value")
        void firstNonNull() {
            processor.setVariable("name", "Alice");

            processor.setTemplate("{{$name ?? \"Guest\"}}");
            assertEquals("Alice", processor.process());
        }

        @Test
        @DisplayName("Should fallback to default when variable is null")
        void fallbackToDefault() {
            processor.setTemplate("{{$name ?? \"Guest\"}}");
            assertEquals("Guest", processor.process());
        }

        @Test
        @DisplayName("Should chain multiple defaults")
        void chainedDefaults() {
            processor.setVariable("b", "Value B");

            processor.setTemplate("{{$a ?? $b ?? \"Default\"}}");
            assertEquals("Value B", processor.process());

            processor.setTemplate("{{$a ?? $c ?? \"Default\"}}");
            assertEquals("Default", processor.process());
        }

        @Test
        @DisplayName("Should combine defaults with filters")
        void defaultsWithFilters() {
            processor.setTemplate("{{$name | uppercase ?? \"GUEST\"}}");
            assertEquals("GUEST", processor.process());

            processor.setVariable("name", "john");
            assertEquals("JOHN", processor.process());
        }

        @Test
        @DisplayName("Should handle complex expressions with defaults, filters, and properties")
        void complexDefaultExpression() {
            processor.setVariable("user", new User(null, "Doe"));

            processor.setTemplate("{{$user.firstName | uppercase ?? $user.lastName | uppercase ?? \"GUEST\"}}");
            assertEquals("DOE", processor.process());
        }
    }

    // ========== IF BLOCKS ==========

    @Nested
    @DisplayName("Conditional Blocks (if)")
    class IfBlocks {

        @Test
        @DisplayName("Should render content when condition is true")
        void renderWhenTrue() {
            processor.setVariable("show", true);

            processor.setTemplate("{{if $show}}<div>Visible</div>{{/if}}");
            assertEquals("<div>Visible</div>", processor.process());
        }

        @Test
        @DisplayName("Should not render content when condition is false")
        void notRenderWhenFalse() {
            processor.setVariable("show", false);

            processor.setTemplate("{{if $show}}<div>Hidden</div>{{/if}}");
            assertEquals("", processor.process());
        }

        @Test
        @DisplayName("Should evaluate complex conditions")
        void complexConditions() {
            processor.setVariable("count", 5);
            processor.setVariable("enabled", true);

            processor.setTemplate("{{if $enabled && $count > 3}}<div>Show</div>{{/if}}");
            assertEquals("<div>Show</div>", processor.process());
        }

        @Test
        @DisplayName("Should support nested if blocks")
        void nestedIf() {
            processor.setVariable("outer", true);
            processor.setVariable("inner", true);

            processor.setTemplate(normalize("""
                    {{if $outer}}
                    Outer
                    {{if $inner}}
                    Inner
                    {{/if}}
                    {{/if}}
                    """));

            String result = processor.process();
            assertTrue(result.contains("Outer"));
            assertTrue(result.contains("Inner"));

            processor.setVariable("inner", false);

            result = processor.process();
            assertTrue(result.contains("Outer"));
            assertFalse(result.contains("Inner"));
        }

        @Test
        @DisplayName("Should handle complex if with comparisons")
        void complexIfComparison() {
            processor.setVariable("score", 85);

            processor.setTemplate(normalize("""
                    {{if $score >= 90}}
                    A
                    {{/if}}
                    {{if $score >= 80 && $score < 90}}
                    B
                    {{/if}}
                    {{if $score < 80}}
                    C
                    {{/if}}
                    """));

            String result = processor.process();
            assertTrue(result.contains("B"));
            assertFalse(result.contains("A"));
            assertFalse(result.contains("C"));
        }

        @ParameterizedTest
        @CsvSource({
                "true, true, Welcome back!",
                "true, true, Welcome back!",
                "false, true, Rendering is disabled",
        })
        @DisplayName("Should render inner else branch when condition switch")
        void rendersIfElseBranch(boolean render, boolean loggedIn, String expected) {
            processor.setVariable("render", render);
            processor.setVariable("loggedIn", loggedIn);

            processor.setTemplate("""
                    {{if $render}}
                    {{if $loggedIn}}
                    Welcome back!
                    {{else}}
                    Please log in
                    {{/if}}
                    {{else}}
                    Rendering is disabled
                    {{/if}}
                    """);

            assertEquals(expected, processor.process().trim());
        }

        @Test
        @DisplayName("Should render only first matching element in if/else-if/else chain")
        void elseIfChainRendersOnlyFirst() {
            processor.setVariable("score", 85);

            processor.setTemplate(normalize("""
                    <div if="$score >= 90">A</div>
                    <div else-if="$score >= 80">B</div>
                    <div else-if="$score >= 70">C</div>
                    <div else>F</div>
                    """));

            String result = processor.process();
            assertFalse(result.contains(">A<"));
            assertTrue(result.contains(">B<"));
            assertFalse(result.contains(">C<"));
            assertFalse(result.contains(">F<"));
        }

        @Test
        @DisplayName("Should render else when all conditions in chain fail")
        void elseInChainWhenAllFail() {
            processor.setVariable("score", 65);

            processor.setTemplate(normalize("""
                    <div if="$score >= 90">A</div>
                    <div else-if="$score >= 80">B</div>
                    <div else-if="$score >= 70">C</div>
                    <div else>F</div>
                    """));

            String result = processor.process();
            assertFalse(result.contains(">A<"));
            assertFalse(result.contains(">B<"));
            assertFalse(result.contains(">C<"));
            assertTrue(result.contains(">F<"));
        }

        @Test
        @DisplayName("Should render first condition in chain when it matches")
        void ifChainFirstMatches() {
            processor.setVariable("value", 100);

            processor.setTemplate(normalize("""
                    <p if="$value > 90">Excellent</p>
                    <p else-if="$value > 70">Good</p>
                    <p else-if="$value > 50">Average</p>
                    <p else>Poor</p>
                    """));

            String result = processor.process();
            assertTrue(result.contains("Excellent"));
            assertFalse(result.contains("Good"));
            assertFalse(result.contains("Average"));
            assertFalse(result.contains("Poor"));
        }

        @Test
        @DisplayName("Should render middle else-if in chain when it's first match")
        void ifChainMiddleMatches() {
            processor.setVariable("value", 75);

            processor.setTemplate(normalize("""
                    <p if="$value > 90">Excellent</p>
                    <p else-if="$value > 70">Good</p>
                    <p else-if="$value > 50">Average</p>
                    <p else>Poor</p>
                    """));

            String result = processor.process();
            assertFalse(result.contains("Excellent"));
            assertTrue(result.contains("Good"));
            assertFalse(result.contains("Average"));
            assertFalse(result.contains("Poor"));
        }

        @Test
        @DisplayName("Should handle complex conditions in else-if chain")
        void complexElseIfChain() {
            processor.setVariable("age", 25);
            processor.setVariable("member", true);

            processor.setTemplate(normalize("""
                    <p if="$age < 18">Child ticket</p>
                    <p else-if="$age >= 65">Senior ticket</p>
                    <p else-if="$member">Member ticket</p>
                    <p else>Regular ticket</p>
                    """));

            String result = processor.process();
            assertFalse(result.contains("Child"));
            assertFalse(result.contains("Senior"));
            assertTrue(result.contains("Member"));
            assertFalse(result.contains("Regular"));
        }

        @Test
        @DisplayName("Should handle if/else-if/else chains within each loops")
        void ifElseIfChainInEach() {
            processor.setVariable("scores", List.of(95, 85, 75, 65));

            processor.setTemplate(normalize("""
                    {{each $score, $idx in $scores}}
                    <div if="$score >= 90">{{$idx}}: A</div>
                    <div else-if="$score >= 80">{{$idx}}: B</div>
                    <div else-if="$score >= 70">{{$idx}}: C</div>
                    <div else>{{$idx}}: F</div>
                    {{/each}}
                    """));

            String result = processor.process();
            assertTrue(result.contains("0: A"));
            assertTrue(result.contains("1: B"));
            assertTrue(result.contains("2: C"));
            assertTrue(result.contains("3: F"));
            // Ensure no duplicates
            assertEquals(1, result.split("0:").length - 1);
            assertEquals(1, result.split("1:").length - 1);
            assertEquals(1, result.split("2:").length - 1);
            assertEquals(1, result.split("3:").length - 1);
        }

        @Test
        @DisplayName("Should handle standalone if without chain")
        void standaloneIfWithoutChain() {
            processor.setVariable("show", true);

            processor.setTemplate("<div if=\"$show\">Content</div>");

            String result = processor.process();
            assertTrue(result.contains("Content"));
        }

        @Test
        @DisplayName("Should handle standalone else-if (acts like if when not in chain)")
        void standaloneElseIf() {
            processor.setVariable("score", 85);

            processor.setTemplate("<div else-if=\"$score >= 80\">B</div>");

            String result = processor.process();
            assertTrue(result.contains(">B<"));
        }

        @Test
        @DisplayName("Should handle if/else-if without final else")
        void ifElseIfWithoutElse() {
            processor.setVariable("score", 85);

            processor.setTemplate(normalize("""
                    <div if="$score >= 90">A</div>
                    <div else-if="$score >= 80">B</div>
                    <div else-if="$score >= 70">C</div>
                    """));

            String result = processor.process();
            assertFalse(result.contains(">A<"));
            assertTrue(result.contains(">B<"));
            assertFalse(result.contains(">C<"));
        }

        @Test
        @DisplayName("Should handle inline if blocks in attribute values")
        void inlineIfInAttributeValue() {
            processor.setVariable("mode", "sell");

            processor.setTemplate("<button type=\"{{if $mode == \\\"sell\\\"}}Primary{{else}}Slate{{/if}}\">Action</button>");

            String result = processor.process();
            assertTrue(result.contains("type=\"Primary\""));
            assertFalse(result.contains("Slate"));
        }

        @Test
        @DisplayName("Should handle inline if blocks in attribute values with different condition")
        void inlineIfInAttributeValueAlternate() {
            processor.setVariable("mode", "buy");

            processor.setTemplate("<button type=\"{{if $mode == \\\"sell\\\"}}Primary{{else}}Slate{{/if}}\">Action</button>");

            String result = processor.process();
            assertTrue(result.contains("type=\"Slate\""));
            assertFalse(result.contains("Primary"));
        }

        @Test
        @DisplayName("Should handle inline if blocks with single quotes in attribute values")
        void inlineIfWithSingleQuotesInAttributeValue() {
            processor.setVariable("mode", "sell");

            processor.setTemplate("<button type=\"{{if $mode == 'sell'}}Primary{{else}}Slate{{/if}}\">Action</button>");

            String result = processor.process();
            assertTrue(result.contains("type=\"Primary\""));
            assertFalse(result.contains("Slate"));
        }

        @Test
        @DisplayName("Should handle inline if blocks with single quotes - else branch")
        void inlineIfWithSingleQuotesInAttributeValueElse() {
            processor.setVariable("mode", "buy");

            processor.setTemplate("<button type=\"{{if $mode == 'sell'}}Primary{{else}}Slate{{/if}}\">Action</button>");

            String result = processor.process();
            assertTrue(result.contains("type=\"Slate\""));
            assertFalse(result.contains("Primary"));
        }

        @Test
        @DisplayName("Should handle inline if blocks without else in attribute values")
        void inlineIfWithoutElseInAttributeValue() {
            processor.setVariable("active", true);

            processor.setTemplate("<div class=\"base {{if $active}}active{{/if}}\">Content</div>");

            String result = processor.process();
            assertTrue(result.contains("class=\"base active\""));
        }
    }

    // ========== EACH BLOCKS ==========

    @Nested
    @DisplayName("Iteration Blocks (each)")
    class EachBlocks {

        @Test
        @DisplayName("Should iterate with default item name")
        void iterateWithDefaultName() {
            processor.setVariable("items", List.of("A", "B", "C"));

            processor.setTemplate("{{each $items}}{{$item}} {{/each}}");
            assertEquals("A B C ", processor.process());
        }

        @Test
        @DisplayName("Should iterate with custom item name")
        void iterateWithCustomName() {
            processor.setVariable("items", List.of("A", "B", "C"));

            processor.setTemplate("{{each $items element}}{{$element}} {{/each}}");
            assertEquals("A B C ", processor.process());
        }

        @Test
        @DisplayName("Should iterate over records with property access")
        void iterateRecords() {
            processor.setVariable("items", List.of(new Item("First", false, "First", 1), new Item("Second", true, "Second", 2)));

            processor.setTemplate("{{each $items}}{{$item.name}}:{{$item.count}} {{/each}}");
            assertEquals("First:1 Second:2 ", processor.process());

            processor.setTemplate("{{each $items product}}{{$product.name}}:{{$product.count}} {{/each}}");
            assertEquals("First:1 Second:2 ", processor.process());
        }

        @Test
        @DisplayName("Should handle empty collections")
        void emptyCollection() {
            processor.setVariable("items", List.of());

            processor.setTemplate("{{each $items}}{{$item}}{{/each}}");
            assertEquals("", processor.process());
        }

        @Test
        @DisplayName("Should access global variables inside loops")
        void globalVariablesInLoop() {
            processor.setVariable("prefix", "Item");
            processor.setVariable("numbers", List.of(1, 2, 3));

            processor.setTemplate("{{each $numbers}}{{$prefix}} {{$item}} {{/each}}");
            assertEquals("Item 1 Item 2 Item 3 ", processor.process());

            processor.setTemplate("{{each $numbers num}}Number {{$num}} {{/each}}");
            assertEquals("Number 1 Number 2 Number 3 ", processor.process());
        }

        @Test
        @DisplayName("Should support nested loops with custom names to avoid conflicts")
        void nestedLoopsWithCustomNames() {
            processor.setVariable("categories", List.of(
                    new Category("Fruits", List.of("Apple", "Banana")),
                    new Category("Vegetables", List.of("Carrot", "Lettuce"))
            ));

            processor.setTemplate(normalize("""
                    {{each $categories cat}}
                    {{$cat.name}}:
                    {{each $cat.items product}}
                    - {{$product}}
                    {{/each}}
                    {{/each}}
                    """));

            assertEquals(normalize("""
                    Fruits:
                    - Apple
                    - Banana
                    Vegetables:
                    - Carrot
                    - Lettuce
                    """), processor.process());
        }

        @Test
        @DisplayName("Should handle null values in collections")
        void nullValuesInCollection() {
            processor.setVariable("items", new ArrayList<>() {{
                add("A");
                add(null);
                add("C");
            }});

            processor.setTemplate("{{each $items}}{{$item}},{{/each}}");
            assertEquals("A,,C,", processor.process());
        }

        @Test
        @DisplayName("Should iterate with new syntax: $item in $items")
        void iterateWithNewSyntaxItemIn() {
            processor.setVariable("items", List.of("A", "B", "C"));

            processor.setTemplate("{{each $elem in $items}}{{$elem}} {{/each}}");
            assertEquals("A B C ", processor.process());
        }

        @Test
        @DisplayName("Should iterate with new syntax: $item, $index in $items")
        void iterateWithNewSyntaxItemIndexIn() {
            processor.setVariable("items", List.of("A", "B", "C"));

            processor.setTemplate("{{each $elem, $i in $items}}{{$i}}:{{$elem}} {{/each}}");
            assertEquals("0:A 1:B 2:C ", processor.process());
        }

        @Test
        @DisplayName("Should iterate with index using default item name")
        void iterateWithIndexDefaultName() {
            processor.setVariable("items", List.of("X", "Y", "Z"));

            processor.setTemplate("{{each $item, $idx in $items}}[{{$idx}}]={{$item}} {{/each}}");
            assertEquals("[0]=X [1]=Y [2]=Z ", processor.process());
        }

        @Test
        @DisplayName("Should support index in nested loops")
        void indexInNestedLoops() {
            processor.setVariable("categories", List.of(
                    new Category("A", List.of("a1", "a2")),
                    new Category("B", List.of("b1", "b2"))
            ));

            processor.setTemplate(normalize("""
                    {{each $cat, $i in $categories}}
                    {{$i}}.{{$cat.name}}:
                    {{each $item, $j in $cat.items}}
                      {{$i}}.{{$j}}: {{$item}}
                    {{/each}}
                    {{/each}}
                    """));

            assertEquals(normalize("""
                    0.A:
                      0.0: a1
                      0.1: a2
                    1.B:
                      1.0: b1
                      1.1: b2
                    """), processor.process());
        }

        @Test
        @DisplayName("Should support new syntax in attributes: each=\"$item in $items\"")
        void attributeSyntaxItemIn() {
            processor.setVariable("items", List.of("A", "B", "C"));

            processor.setTemplate("<div each=\"$elem in $items\">{{$elem}}</div>");
            assertEquals("<div>A</div><div>B</div><div>C</div>", processor.process());
        }

        @Test
        @DisplayName("Should support new syntax in attributes: each=\"$item, $index in $items\"")
        void attributeSyntaxItemIndexIn() {
            processor.setVariable("items", List.of("A", "B", "C"));

            processor.setTemplate("<div each=\"$elem, $i in $items\">{{$i}}:{{$elem}}</div>");
            assertEquals("<div>0:A</div><div>1:B</div><div>2:C</div>", processor.process());
        }

        @Test
        @DisplayName("Should support shorthand syntax in attributes: each=\"$items\"")
        void attributeSyntaxShorthand() {
            processor.setVariable("items", List.of("A", "B", "C"));

            processor.setTemplate("<div each=\"$items\">{{$item}}</div>");
            assertEquals("<div>A</div><div>B</div><div>C</div>", processor.process());
        }
    }

    // ========== FUNCTION CALLS ==========

    @Nested
    @DisplayName("Function Calls in Templates")
    class FunctionCalls {

        @Test
        @DisplayName("Should call function with arguments")
        void callFunctionWithArguments() {
            processor.setVariable("products", List.of(
                    new Product("Weapon", List.of("sword", "axe")),
                    new Product("Potion", List.of("healing", "mana"))
            ));
            processor.setVariable("tags", (stack) -> {
                if (!stack.isScope(SCOPE_EACH_NAME))
                    return "";

                String key = stack.getScopeKeys().iterator().next();
                Object value = stack.getVariable(key);
                if (!(value instanceof Product product))
                    return "";

                return product.getTagsWithPrefix("tag_");
            }, DYNAMIC);

            processor.setTemplate(normalize("""
                    {{each $products product}}
                    {{$name}}: {{$tags}}
                    {{/each}}
                    """));

            assertEquals(normalize("""
                    Weapon: tag_sword, tag_axe
                    Potion: tag_healing, tag_mana
                    """), processor.process());
        }

        @Test
        @DisplayName("Should call function with arguments and dynamic variables")
        void callFunctionWithArgumentsAndDynamic() {
            final var modulator = new Modulator();

            processor.setVariable("list", List.of(1, 2, 3, 4));
            processor.setVariable("modulation", (_) -> modulator.increment(), DYNAMIC);
            processor.setVariable("style", (stack) -> (int) stack.getVariable("key") < 3 ? "color: red;" : null, DYNAMIC);

            processor.registerComponent("module", """
                    <div {{if $style}}style="{{$style}}"{{/if}}>Module {{$key}} -> Active : {{ $active ?? false }}</div>
                    """);

            processor.setTemplate(normalize("""
                    {{each $list key}}
                    <module key={{$key}} {{if $modulation == 0 }} {{if $key < 3 }} active {{/if}} {{/if}} style={{$style}} />
                    {{/each}}
                    """));

            assertEquals(normalize("""
                    <div style="color: red;">Module 1 -> Active : false</div>
                    <div style="color: red;">Module 2 -> Active : true</div>
                    <div>Module 3 -> Active : false</div>
                    <div>Module 4 -> Active : false</div>
                    """), processor.process());
        }
    }

    // ========== COMBINED BLOCKS ==========

    @Nested
    @DisplayName("Combined Conditional and Iteration")
    class CombinedBlocks {

        @Test
        @DisplayName("Should combine if and each blocks")
        void ifInsideEach() {
            processor.setVariable("items", List.of(
                    new Item("First", true, null, 0),
                    new Item("Second", false, null, 0),
                    new Item("Third", true, null, 0)
            ));

            processor.setTemplate(normalize("""
                    {{each $items}}
                    {{if $item.active}}
                    <div>{{$item.name}}</div>
                    {{/if}}
                    {{/each}}
                    """));

            assertEquals(normalize("""
                    <div>First</div>
                    <div>Third</div>
                    """), processor.process());
        }

        @Test
        @DisplayName("Should handle complex real-world template")
        void complexRealWorldTemplate() {
            processor.setVariable("preset-active", "preset_01");
            processor.setVariable("render", true);
            processor.setVariable("preset-list", List.of(
                    new Item("preset_01", true, "Test name", 0),
                    new Item("preset_02", true, "Test name 02", 1)
            ));

            processor.setTemplate("""
                    <div class="container-contents">
                        <div class="content-name">
                            <input placeholder="Preset..." type="text" value=""/>
                        </div>
                    
                        {{if $render && $preset-list.size > 1}}
                        <div class="content-preset">
                            <select data-hyui-showlabel="true" value={{$preset-active}}>
                                {{each $preset-list}}
                                <option {{if $preset-active == $item.name}}selected {{/if}}value={{$item.name}}>{{$item.display}}</option>
                                {{/each}}
                            </select>
                        </div>
                        {{/if}}
                    </div>
                    """);

            assertEquals(normalize("""
                    <div class="container-contents">
                        <div class="content-name">
                            <input placeholder="Preset..." type="text" value=""/>
                        </div>
                    
                        <div class="content-preset">
                            <select data-hyui-showlabel="true" value="preset_01">
                                <option selected value="preset_01">Test name</option>
                                <option value="preset_02">Test name 02</option>
                            </select>
                        </div>
                    </div>
                    """), processor.process());
        }
    }

    // ========== COMPONENTS ==========

    @Nested
    @DisplayName("Components")
    class Components {

        @Test
        @DisplayName("Should expand component with parameters")
        void expandsComponentWithParameters() {
            processor.setVariable("number", 12.847);
            processor.registerComponent("statCard", """
                    <div style="background-color: #2a2a3e; padding: 10; anchor-width: 120; anchor-height: 60;">
                        <p style="color: #888888; font-size: 11;">{{$label}}</p>
                        <p style="color: #ffffff; font-size: 18; font-weight: bold;">{{$value}}</p>
                    </div>
                    """);

            processor.setTemplate("<statCard label=\"Blocks Placed\" value={{$number}} />");
            assertEquals(normalize("""
                    <div style="background-color: #2a2a3e; padding: 10; anchor-width: 120; anchor-height: 60;">
                        <p style="color: #888888; font-size: 11;">Blocks Placed</p>
                        <p style="color: #ffffff; font-size: 18; font-weight: bold;">12.847</p>
                    </div>
                    """), processor.process());
        }

        @Test
        @DisplayName("Should expand component inside another component with parameters")
        void expandsComponentWithinComponent() {
            processor.setVariable("text", "Deep Component");
            processor.registerComponent("panel", """
                    <div style="background-color: #2a2a3e; padding: 10; anchor-width: 120; anchor-height: 60;">
                        <view content={{$text}} />
                        <view />
                    </div>
                    """);
            processor.registerComponent("view", """
                    <span>{{ $content ?? "undefined" }}</span>
                    """);

            processor.setTemplate("<panel content={{ $number }} />");
            assertEquals(normalize("""
                    <div style="background-color: #2a2a3e; padding: 10; anchor-width: 120; anchor-height: 60;">
                        <span>Deep Component</span>
                        <span>undefined</span>
                    </div>
                    """), processor.process());
        }

        @Test
        @DisplayName("Should allow components to access variables from global scope")
        void componentCanAccessVariablesFromGlobalScope() {
            processor.setVariable("label", "Submit");
            processor.registerComponent("submit", "<button>{{$label}}</button>");

            processor.setTemplate("<submit/>");
            assertEquals(
                    "<button>Submit</button>",
                    processor.process()
            );
        }

        @Test
        @DisplayName("Should prioritize local scope over global scope in components")
        void componentPrioritizeVariableFromLocalScope() {
            processor.setVariable("label", "global scope");
            processor.registerComponent("submit", "<button>{{$label}}</button>");

            processor.setTemplate("<submit label=\"local scope\"/>");
            assertEquals(
                    "<button>local scope</button>",
                    processor.process()
            );
        }

        @Test
        @DisplayName("Should allow components to pass existing parameters")
        void componentCanPassExistingParameters() {
            processor.registerComponent("button", "<button style={{$style}}>Submit</button>");

            processor.setTemplate("<button style=\"align: center\" />");
            assertEquals(
                    "<button style=\"align: center\">Submit</button>",
                    processor.process()
            );
        }

        @Test
        @DisplayName("Should allow components to use children as content")
        void componentCanPassChildren() {
            processor.registerComponent("panel", "<div style=\"background: red\"><slot/></div>");
            processor.registerComponent("bigButton", "<h1><slot/></h1>");

            processor.setTemplate("<panel><bigButton>Deep Big Button</bigButton></panel>");
            assertEquals(
                    "<div style=\"background: red\"><h1>Deep Big Button</h1></div>",
                    processor.process()
            );
        }

        @Test
        @DisplayName("Should allow default slot content when no children are provided")
        void defaultSlot() {
            processor.registerComponent("panel", "<h1><slot>Default</slot></h1>");

            processor.setTemplate("<panel />");
            assertEquals(
                    "<h1>Default</h1>",
                    processor.process()
            );
        }

        @Test
        @DisplayName("Should handle named slots with default content")
        void complexSlotHandling() {
            processor.registerComponent("panel", """
                    <div class="panel">
                        <h1><slot:header>Default Header</slot></h1>
                        {{if $slot:default}}
                        <div class="content">
                            <slot>Default Content</slot>
                        </div>
                        {{/if}}
                        {{if $slot:footer}}
                        <footer><slot:footer/></footer>
                        {{/if}}
                    </div>
                    """);

            processor.setTemplate("""
                    <panel>
                        Custom Content
                        <:header>Custom Header</:header>
                        <:footer>Custom Footer</:footer>
                    </panel>
                    """);
            assertEquals(normalize("""
                            <div class="panel">
                                <h1>Custom Header</h1>
                                <div class="content">
                                    Custom Content
                                </div>
                                <footer>Custom Footer</footer>
                            </div>
                            """),
                    processor.process()
            );
        }

        @Test
        @DisplayName("Should handle control flow as attributes")
        void componentAttributesFlow() {
            processor.setVariable("items", List.of("A", "B", "C"));
            processor.registerComponent("bigButton", "<h1><slot/></h1>");
            processor.registerComponent("panel", """
                    <div style="background: red">
                        <slot/>
                    </div>
                    """);

            processor.setTemplate("""
                    <panel>
                        <bigButton each="$items key">Button {{$key}} <hidden if="$key == B">secret</hidden></bigButton>
                    </panel>
                    """);
            assertEquals(normalize("""
                    <div style="background: red">
                        <h1>Button A</h1><h1>Button B <hidden>secret</hidden></h1><h1>Button C</h1>
                    </div>
                    """), processor.process());
        }
    }

    // ========== TEMPLATE TAG ==========

    @Nested
    @DisplayName("Template Tag (Renderless Wrapper)")
    class TemplateTag {

        @Test
        @DisplayName("Should render template tag children without wrapper element")
        void renderChildrenWithoutWrapper() {
            processor.setTemplate("<template><div>Content</div></template>");
            assertEquals("<div>Content</div>", processor.process());
        }

        @Test
        @DisplayName("Should use template tag with if attribute")
        void templateWithIf() {
            processor.setVariable("show", true);

            processor.setTemplate("<template if=\"$show\"><div>Visible</div></template>");
            assertEquals("<div>Visible</div>", processor.process());

            processor.setVariable("show", false);
            assertEquals("", processor.process());
        }

        @Test
        @DisplayName("Should use template tag with if/else-if/else chain")
        void templateWithConditionalChain() {
            processor.setVariable("status", "warning");

            processor.setTemplate(normalize("""
                    <template if="$status == warning">
                        <div class="success">Success!</div>
                    </template>
                    <template else-if="$status == success">
                        <div class="warning">Warning!</div>
                    </template>
                    <template else>
                        <div class="error">Error!</div>
                    </template>
                    """));

            String result = processor.process();
            assertTrue(result.contains("Success"));
            assertFalse(result.contains("Warning"));
            assertFalse(result.contains("Error"));
            assertFalse(result.contains("<template"));
        }

        @Test
        @DisplayName("Should use template tag with each attribute")
        void templateWithEach() {
            processor.setVariable("items", List.of("A", "B", "C"));

            processor.setTemplate("<template each=\"$items\"><span>{{$item}}</span></template>");
            assertEquals("<span>A</span><span>B</span><span>C</span>", processor.process());
        }

        @Test
        @DisplayName("Should use template tag with each and index")
        void templateWithEachAndIndex() {
            processor.setVariable("items", List.of("X", "Y", "Z"));

            processor.setTemplate("<template each=\"$item, $idx in $items\"><p>{{$idx}}:{{$item}}</p></template>");
            assertEquals("<p>0:X</p><p>1:Y</p><p>2:Z</p>", processor.process());
        }

        @Test
        @DisplayName("Should use template tag for multiple children without wrapper")
        void templateWithMultipleChildren() {
            processor.setVariable("show", true);

            processor.setTemplate(normalize("""
                    <template if="$show">
                        <h1>Title</h1>
                        <p>Paragraph</p>
                        <span>Span</span>
                    </template>
                    """));

            String result = processor.process();
            assertTrue(result.contains("<h1>Title</h1>"));
            assertTrue(result.contains("<p>Paragraph</p>"));
            assertTrue(result.contains("<span>Span</span>"));
            assertFalse(result.contains("<template"));
        }

        @Test
        @DisplayName("Should nest template tags")
        void nestedTemplateTags() {
            processor.setVariable("outer", true);
            processor.setVariable("inner", true);

            processor.setTemplate(normalize("""
                    <template if="$outer">
                        <div>Outer</div>
                        <template if="$inner">
                            <div>Inner</div>
                        </template>
                    </template>
                    """));

            String result = processor.process();
            assertTrue(result.contains("<div>Outer</div>"));
            assertTrue(result.contains("<div>Inner</div>"));
            assertFalse(result.contains("<template"));
        }

        @Test
        @DisplayName("Should use template tag in each loop for conditional rendering")
        void templateInEachLoop() {
            processor.setVariable("items", List.of(
                    new Item("First", true, null, 0),
                    new Item("Second", false, null, 0),
                    new Item("Third", true, null, 0)
            ));

            processor.setTemplate(normalize("""
                    {{each $item in $items}}
                    <template if="$item.active">
                        <div>{{$item.name}}</div>
                    </template>
                    {{/each}}
                    """));

            String result = processor.process();
            assertTrue(result.contains("First"));
            assertFalse(result.contains("Second"));
            assertTrue(result.contains("Third"));
            assertFalse(result.contains("<template"));
        }

        @Test
        @DisplayName("Should combine template tag with each and conditional chain")
        void templateWithEachAndConditionalChain() {
            processor.setVariable("scores", List.of(95, 85, 75));

            processor.setTemplate(normalize("""
                    <template each="$score, $idx in $scores">
                        <template if="$score >= 90">
                            <div class="a">{{$idx}}: A</div>
                        </template>
                        <template else-if="$score >= 80">
                            <div class="b">{{$idx}}: B</div>
                        </template>
                        <template else>
                            <div class="c">{{$idx}}: C</div>
                        </template>
                    </template>
                    """));

            String result = processor.process();
            assertTrue(result.contains("0: A"));
            assertTrue(result.contains("1: B"));
            assertTrue(result.contains("2: C"));
            assertFalse(result.contains("<template"));
        }

        @Test
        @DisplayName("Should handle empty template tag")
        void emptyTemplate() {
            processor.setTemplate("<template></template>");
            assertEquals("", processor.process());
        }

        @Test
        @DisplayName("Should handle self-closing template tag")
        void selfClosingTemplate() {
            processor.setTemplate("<template/>");
            assertEquals("", processor.process());
        }
    }

    // ========== ERROR HANDLING ==========

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should throw exception for unterminated string")
        void unterminatedString() {
            processor.setTemplate("{{\"unterminated}}");
            assertThrows(RuntimeException.class, () -> processor.process());
        }

        @Test
        @DisplayName("Should throw exception for unknown filter")
        void unknownFilter() {
            processor.setVariable("name", "John");

            processor.setTemplate("{{$name | unknownfilter}}");
            assertThrows(RuntimeException.class, () -> processor.process());
        }

        @Test
        @DisplayName("Should throw exception for unclosed if block")
        void unclosedIfBlock() {
            processor.setTemplate("{{if $var}}Content");
            assertThrows(RuntimeException.class, () -> processor.process());
        }

        @Test
        @DisplayName("Should throw exception for unclosed each block")
        void unclosedEachBlock() {
            processor.setTemplate("{{each $items}}Content");
            assertThrows(RuntimeException.class, () -> processor.process());
        }
    }

    // ========== PERFORMANCE ==========

    @Nested
    @DisplayName("Performance measurements")
    class Performance {

        @Test
        @DisplayName("Should handle large lists efficiently")
        void largeListPerformance() {
            List<Integer> largeList = new ArrayList<>();
            for (int i = 0; i < 1000; i++) largeList.add(i);
            processor.setVariable("numbers", largeList);

            long start = System.currentTimeMillis();
            processor.setTemplate("{{each $numbers}}{{$item}},{{/each}}");
            String result = processor.process();
            long duration = System.currentTimeMillis() - start;

            assertTrue(duration < 1000, "Processing 1000 items should complete in less than 1 second");
            assertTrue(result.contains("0,"));
            assertTrue(result.contains("999,"));
        }

        @Test
        @DisplayName("Should parse complex templates quickly")
        void complexTemplatePerformance() {
            String template = normalize("""
                    {{each $items}}
                        {{if $item.active}}
                            <div>{{$item.name | uppercase}}</div>
                        {{/if}}
                    {{/each}}
                    """);

            long start = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) processor.setTemplate(template).process();
            long duration = System.currentTimeMillis() - start;

            assertTrue(duration < 1000, "100 iterations should complete in less than 1 second");
        }
    }
}
