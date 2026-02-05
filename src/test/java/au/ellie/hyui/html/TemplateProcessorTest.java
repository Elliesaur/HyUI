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

    record Box(int size) {
    }

    record Product(String name, List<String> tags) {
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
        @DisplayName("Should handle supplier properties")
        void supplierEvaluationOnIfCondition(boolean condition, int value, String expected) {
            AtomicInteger evaluations = new AtomicInteger();

            processor.setVariable("enabled", condition);
            processor.setVariable("secret", () -> {
                evaluations.incrementAndGet();
                return "value_" + evaluations;
            });

            processor.setTemplate("""
                    {{#if $enabled}}
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
        @DisplayName("Should handle function properties")
        void functionEvaluationOnIfCondition(boolean condition, int value, String expected) {
            AtomicInteger evaluations = new AtomicInteger();

            processor.setVariable("enabled", condition);
            processor.setVariable("secret", (_) -> {
                evaluations.incrementAndGet();
                return "value_" + evaluations;
            });

            processor.setTemplate("""
                    {{#if $enabled}}
                    {{$secret}} - {{$secret}}
                    {{/if}}
                    """);
            assertEquals(expected != null ? expected : "", processor.process());
            assertEquals(value, evaluations.get());
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

            processor.setTemplate("{{#if $val}}true{{/if}}");
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

            processor.setTemplate("{{#if $show}}<div>Visible</div>{{/if}}");
            assertEquals("<div>Visible</div>", processor.process());
        }

        @Test
        @DisplayName("Should not render content when condition is false")
        void notRenderWhenFalse() {
            processor.setVariable("show", false);

            processor.setTemplate("{{#if $show}}<div>Hidden</div>{{/if}}");
            assertEquals("", processor.process());
        }

        @Test
        @DisplayName("Should evaluate complex conditions")
        void complexConditions() {
            processor.setVariable("count", 5);
            processor.setVariable("enabled", true);

            processor.setTemplate("{{#if $enabled && $count > 3}}<div>Show</div>{{/if}}");
            assertEquals("<div>Show</div>", processor.process());
        }

        @Test
        @DisplayName("Should support nested if blocks")
        void nestedIf() {
            processor.setVariable("outer", true);
            processor.setVariable("inner", true);

            processor.setTemplate(normalize("""
                    {{#if $outer}}
                    Outer
                    {{#if $inner}}
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
                    {{#if $score >= 90}}
                    A
                    {{/if}}
                    {{#if $score >= 80 && $score < 90}}
                    B
                    {{/if}}
                    {{#if $score < 80}}
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
                    {{#if $render}}
                    {{#if $loggedIn}}
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
    }

    // ========== EACH BLOCKS ==========

    @Nested
    @DisplayName("Iteration Blocks (each)")
    class EachBlocks {

        @Test
        @DisplayName("Should iterate with default item name")
        void iterateWithDefaultName() {
            processor.setVariable("items", List.of("A", "B", "C"));

            processor.setTemplate("{{#each $items}}{{$item}} {{/each}}");
            assertEquals("A B C ", processor.process());
        }

        @Test
        @DisplayName("Should iterate with custom item name")
        void iterateWithCustomName() {
            processor.setVariable("items", List.of("A", "B", "C"));

            processor.setTemplate("{{#each $items element}}{{$element}} {{/each}}");
            assertEquals("A B C ", processor.process());
        }

        @Test
        @DisplayName("Should iterate over records with property access")
        void iterateRecords() {
            processor.setVariable("items", List.of(new Item("First", false, "First", 1), new Item("Second", true, "Second", 2)));

            processor.setTemplate("{{#each $items}}{{$item.name}}:{{$item.count}} {{/each}}");
            assertEquals("First:1 Second:2 ", processor.process());

            processor.setTemplate("{{#each $items product}}{{$product.name}}:{{$product.count}} {{/each}}");
            assertEquals("First:1 Second:2 ", processor.process());
        }

        @Test
        @DisplayName("Should handle empty collections")
        void emptyCollection() {
            processor.setVariable("items", List.of());

            processor.setTemplate("{{#each $items}}{{$item}}{{/each}}");
            assertEquals("", processor.process());
        }

        @Test
        @DisplayName("Should access global variables inside loops")
        void globalVariablesInLoop() {
            processor.setVariable("prefix", "Item");
            processor.setVariable("numbers", List.of(1, 2, 3));

            processor.setTemplate("{{#each $numbers}}{{$prefix}} {{$item}} {{/each}}");
            assertEquals("Item 1 Item 2 Item 3 ", processor.process());

            processor.setTemplate("{{#each $numbers num}}Number {{$num}} {{/each}}");
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
                    {{#each $categories cat}}
                    {{$cat.name}}:
                    {{#each $cat.items product}}
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

            processor.setTemplate("{{#each $items}}{{$item}},{{/each}}");
            assertEquals("A,,C,", processor.process());
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
                    {{#each $items}}
                    {{#if $item.active}}
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


            processor.setTemplate(normalize("""
                    <div class="container-contents">
                        <div class="content-name">
                            <input placeholder="Preset..." type="text" value=""/>
                        </div>
                    
                        {{#if $render && $preset-list.size > 1}}
                        <div class="content-preset">
                            <select data-hyui-showlabel="true" value="{{$preset-active}}">
                                {{#each $preset-list}}
                                <option {{#if $preset-active == $item.name}}selected {{/if}}value="{{$item.name}}">{{$item.display}}</option>
                                {{/each}}
                            </select>
                        </div>
                        {{/if}}
                    </div>
                    """));

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
            processor.registerComponent("button", "<button style=\"{{$style}}\">Submit</button>");

            processor.setTemplate("<button style=\"align: center\" />");
            assertEquals(
                    "<button style=\"align: center\">Submit</button>",
                    processor.process()
            );
        }

        @Test
        @DisplayName("Should allow components to use children as content")
        void componentCanPassChildren() {
            processor.registerComponent("panel", "<div style=\"background: red\">{{ $children }}</div>");
            processor.registerComponent("bigButton", "<h1>{{ $children }}</h1>");

            processor.setTemplate("<panel><bigButton>Deep Big Button</bigButton></panel>");
            assertEquals(
                    "<div style=\"background: red\"><h1>Deep Big Button</h1></div>",
                    processor.process()
            );
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
            processor.setTemplate("{{#if $var}}Content");
            assertThrows(RuntimeException.class, () -> processor.process());
        }

        @Test
        @DisplayName("Should throw exception for unclosed each block")
        void unclosedEachBlock() {
            processor.setTemplate("{{#each $items}}Content");
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
            processor.setTemplate("{{#each $numbers}}{{$item}},{{/each}}");
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
                    {{#each $items}}
                        {{#if $item.active}}
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
