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

    record User(String name) {
    }

    record Category(String name, List<String> items) {
    }

    record Item(String name, boolean active, String display) {
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
            assertEquals("<div>Hello World</div>", processor.process(template));
        }

        @Test
        @DisplayName("Should replace simple variables")
        void simpleVariable() {
            processor.setVariable("name", "John");
            assertEquals("Hello John!", processor.process("Hello {{$name}}!"));
        }

        @Test
        @DisplayName("Should handle missing variables as empty strings")
        void missingVariable() {
            assertEquals("Hello !", processor.process("Hello {{$name}}!"));
        }

        @Test
        @DisplayName("Should support variables with hyphens and underscores")
        void variableNaming() {
            processor.setVariable("my-var", "value1");
            processor.setVariable("my_var", "value2");
            assertEquals("value1 value2", processor.process("{{$my-var}} {{$my_var}}"));
        }

        @Test
        @DisplayName("Should preserve intentional whitespace in HTML")
        void whitespacePreservation() {
            assertEquals("<div>  Hello  </div>", processor.process("<div>  Hello  </div>"));
        }
    }

    // ========== LITERALS ==========

    @Nested
    @DisplayName("Literal Values")
    class Literals {

        @Test
        @DisplayName("Should handle string literals with escaping")
        void stringLiterals() {
            assertEquals("Hello World", processor.process("{{\"Hello World\"}}"));
            assertEquals("Hello \"World\"", processor.process("{{\"Hello \\\"World\\\"\"}}"));
        }

        @ParameterizedTest
        @CsvSource({"{{42}}", "{{3.14}}", "{{-5}}"})
        @DisplayName("Should handle numeric literals")
        void numericLiterals(String template) {
            assertNotNull(processor.process(template));
            assertFalse(processor.process(template).isBlank());
        }

        @Test
        @DisplayName("Should handle boolean literals")
        void booleanLiterals() {
            assertEquals("true false", processor.process("{{true}} {{false}}"));
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
            assertEquals("Alice is 30", processor.process("{{$user.name}} is {{$user.age}}"));
        }

        @Test
        @DisplayName("Should access map properties")
        void mapProperties() {
            processor.setVariable("user", Map.of("name", "Bob", "age", 25));
            assertEquals("Bob is 25", processor.process("{{$user.name}} is {{$user.age}}"));
        }

        @Test
        @DisplayName("Should access nested properties")
        void nestedProperties() {
            processor.setVariable("user", new Person("Charlie", 21, new Address("Paris", "France")));
            assertEquals("Paris, France", processor.process("{{$user.address.city}}, {{$user.address.country}}"));
        }

        @Test
        @DisplayName("Should return empty string for missing properties")
        void missingProperties() {
            processor.setVariable("user", new Person("Dave", 32, null));
            assertEquals("", processor.process("{{$user.id}}"));
        }

        @ParameterizedTest
        @CsvSource({
                "false, 0, ",
                "true, 1, value_1 - value_1"
        })
        @DisplayName("Should handle supplier properties")
        void supplierEvaluationOnIfCondition(boolean condition, int value, String expected) {
            AtomicInteger evaluations = new AtomicInteger();

            processor
                    .setVariable("enabled", condition)
                    .setVariable("secret", () -> {
                        evaluations.incrementAndGet();
                        return "value_" + evaluations;
                    });

            String template = """
                    {{#if $enabled}}
                    {{$secret}} - {{$secret}}
                    {{/if}}
                    """;

            assertEquals(expected != null ? expected : "", processor.process(template).trim());
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

            processor
                    .setVariable("enabled", condition)
                    .setVariable("secret", (stack) -> {
                        evaluations.incrementAndGet();
                        return "value_" + evaluations;
                    });

            String template = """
                    {{#if $enabled}}
                    {{$secret}} - {{$secret}}
                    {{/if}}
                    """;

            assertEquals(expected != null ? expected : "", processor.process(template).trim());
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
            assertEquals(String.valueOf(expected), processor.process("{{$a " + op + " $b}}"));
        }

        @Test
        @DisplayName("Should compare strings")
        void stringComparison() {
            processor.setVariable("name", "Alice");
            assertEquals("true", processor.process("{{$name == \"Alice\"}}"));
            assertEquals("false", processor.process("{{$name == \"Bob\"}}"));
        }

        @Test
        @DisplayName("Should handle numeric type mixing (int, long, double)")
        void numericTypeMixing() {
            processor.setVariable("a", 5);
            processor.setVariable("b", 5.0);
            assertEquals("true", processor.process("{{$a == $b}}"));
            assertEquals("false", processor.process("{{$a != $b}}"));
        }

        @Test
        @DisplayName("Should handle floating-point comparison with epsilon")
        void floatingPointEpsilon() {
            processor.setVariable("a", 0.1 + 0.2);
            processor.setVariable("b", 0.3);
            assertEquals("true", processor.process("{{$a == $b}}"));
        }

        @Test
        @DisplayName("Should handle null comparisons")
        void nullComparison() {
            assertEquals("true", processor.process("{{$value == $missing}}"));
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

            assertEquals("true", processor.process("{{$a && $b}}"));
            assertEquals("false", processor.process("{{$a && $c}}"));
            assertEquals("false", processor.process("{{$c && $c}}"));
        }

        @Test
        @DisplayName("Should evaluate OR operator")
        void orOperator() {
            processor.setVariable("a", true);
            processor.setVariable("b", false);

            assertEquals("true", processor.process("{{$a || $b}}"));
            assertEquals("false", processor.process("{{$b || $b}}"));
        }

        @Test
        @DisplayName("Should combine AND and OR operators")
        void combinedLogicalOperators() {
            processor.setVariable("a", true);
            processor.setVariable("b", false);
            processor.setVariable("c", true);

            assertEquals("true", processor.process("{{$a && $b || $c}}"));
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
            if (value.isEmpty()) {
                processor.setVariable("val", "");
            } else if (value.matches("\\d+")) {
                processor.setVariable("val", Integer.parseInt(value));
            } else {
                processor.setVariable("val", value);
            }

            String expected = isTruthy ? "true" : "";
            assertEquals(expected, processor.process("{{#if $val}}true{{/if}}"));
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
            assertEquals("true", processor.process("{{\"apple\" in $items}}"));
            assertEquals("false", processor.process("{{\"orange\" in $items}}"));
        }

        @Test
        @DisplayName("Should check key presence in map")
        void mapContainsKey() {
            processor.setVariable("user", Map.of("name", "Alice", "age", 30));
            assertEquals("true", processor.process("{{\"name\" in $user}}"));
            assertEquals("false", processor.process("{{\"email\" in $user}}"));
        }

        @Test
        @DisplayName("Should check substring in string")
        void stringContains() {
            processor.setVariable("text", "Hello World");
            assertEquals("true", processor.process("{{\"World\" in $text}}"));
            assertEquals("false", processor.process("{{\"Java\" in $text}}"));
        }
    }

    // ========== FILTERS ==========

    @Nested
    @DisplayName("Filters")
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
            assertEquals(expected, processor.process("{{$value | " + filter + "}}"));
        }

        @Test
        @DisplayName("Should chain multiple filters")
        void chainedFilters() {
            processor.setVariable("name", "  john doe  ");
            assertEquals("JOHN DOE", processor.process("{{$name | trim | uppercase}}"));
        }

        @Test
        @DisplayName("Should support custom filters")
        void customFilter() {
            processor.registerFilter("reverse", value ->
                    value == null ? null : new StringBuilder(value.toString()).reverse().toString()
            );

            processor.setVariable("text", "Hello");
            assertEquals("olleH", processor.process("{{$text | reverse}}"));
        }

        @Test
        @DisplayName("Should apply length filter to strings and collections")
        void lengthFilter() {
            processor.setVariable("text", "Hello");
            processor.setVariable("items", List.of("a", "b", "c"));

            assertEquals("5", processor.process("{{$text | length}}"));
            assertEquals("3", processor.process("{{$items | length}}"));
        }

        @Test
        @DisplayName("Should format numbers with number filter")
        void number_formatsNumber() {
            processor.setVariable("value", 1234);

            assertEquals(
                    "1,234",
                    processor.process("{{$value | number}}")
            );
        }

        @Test
        @DisplayName("Should format percentages with percent filter")
        void percent_formatsPercent() {
            processor.setVariable("value", 0.125);

            assertEquals(
                    "13%",
                    processor.process("{{$value | percent}}")
            );
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
            assertEquals("Alice", processor.process("{{$name ?? \"Guest\"}}"));
        }

        @Test
        @DisplayName("Should fallback to default when variable is null")
        void fallbackToDefault() {
            assertEquals("Guest", processor.process("{{$name ?? \"Guest\"}}"));
        }

        @Test
        @DisplayName("Should chain multiple defaults")
        void chainedDefaults() {
            processor.setVariable("b", "Value B");
            assertEquals("Value B", processor.process("{{$a ?? $b ?? \"Default\"}}"));
            assertEquals("Default", processor.process("{{$a ?? $c ?? \"Default\"}}"));
        }

        @Test
        @DisplayName("Should combine defaults with filters")
        void defaultsWithFilters() {
            assertEquals("GUEST", processor.process("{{$name | uppercase ?? \"GUEST\"}}"));

            processor.setVariable("name", "john");
            assertEquals("JOHN", processor.process("{{$name | uppercase ?? \"GUEST\"}}"));
        }

        @Test
        @DisplayName("Should handle complex expressions with defaults, filters, and properties")
        void complexDefaultExpression() {
            record User(String firstName, String lastName) {
            }
            processor.setVariable("user", new User(null, "Doe"));

            assertEquals("DOE", processor.process("{{$user.firstName | uppercase ?? $user.lastName | uppercase ?? \"GUEST\"}}"));
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
            assertEquals("<div>Visible</div>", processor.process("{{#if $show}}<div>Visible</div>{{/if}}"));
        }

        @Test
        @DisplayName("Should not render content when condition is false")
        void notRenderWhenFalse() {
            processor.setVariable("show", false);
            assertEquals("", processor.process("{{#if $show}}<div>Hidden</div>{{/if}}"));
        }

        @Test
        @DisplayName("Should evaluate complex conditions")
        void complexConditions() {
            processor.setVariable("count", 5);
            processor.setVariable("enabled", true);

            assertEquals("<div>Show</div>", processor.process("{{#if $enabled && $count > 3}}<div>Show</div>{{/if}}"));
        }

        @Test
        @DisplayName("Should support nested if blocks")
        void nestedIf() {
            processor.setVariable("outer", true);
            processor.setVariable("inner", true);

            String template = normalize("""
                    {{#if $outer}}
                    Outer
                    {{#if $inner}}
                    Inner
                    {{/if}}
                    {{/if}}
                    """);

            String result = processor.process(template);
            assertTrue(result.contains("Outer"));
            assertTrue(result.contains("Inner"));

            processor.setVariable("inner", false);
            result = processor.process(template);
            assertTrue(result.contains("Outer"));
            assertFalse(result.contains("Inner"));
        }

        @Test
        @DisplayName("Should handle complex if with comparisons")
        void complexIfComparison() {
            processor.setVariable("score", 85);

            String template = normalize("""
                    {{#if $score >= 90}}
                    A
                    {{/if}}
                    {{#if $score >= 80 && $score < 90}}
                    B
                    {{/if}}
                    {{#if $score < 80}}
                    C
                    {{/if}}
                    """);

            String result = processor.process(template);
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

            String template = """
                    {{#if $render}}
                    {{#if $loggedIn}}
                    Welcome back!
                    {{#else}}
                    Please log in
                    {{/if}}
                    {{#else}}
                    Rendering is disabled
                    {{/if}}
                    """;

            assertEquals(expected, processor.process(template).trim());
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
            assertEquals("A B C ", processor.process("{{#each $items}}{{$item}} {{/each}}"));
        }

        @Test
        @DisplayName("Should iterate with custom item name")
        void iterateWithCustomName() {
            processor.setVariable("items", List.of("A", "B", "C"));
            assertEquals("A B C ", processor.process("{{#each $items element}}{{$element}} {{/each}}"));
        }

        @Test
        @DisplayName("Should iterate over records with property access")
        void iterateRecords() {
            record Item(String name, int value) {
            }
            processor.setVariable("items", List.of(new Item("First", 1), new Item("Second", 2)));

            assertEquals("First:1 Second:2 ", processor.process("{{#each $items}}{{$item.name}}:{{$item.value}} {{/each}}"));
            assertEquals("First:1 Second:2 ", processor.process("{{#each $items product}}{{$product.name}}:{{$product.value}} {{/each}}"));
        }

        @Test
        @DisplayName("Should handle empty collections")
        void emptyCollection() {
            processor.setVariable("items", List.of());
            assertEquals("", processor.process("{{#each $items}}{{$item}}{{/each}}"));
        }

        @Test
        @DisplayName("Should access global variables inside loops")
        void globalVariablesInLoop() {
            processor.setVariable("prefix", "Item");
            processor.setVariable("numbers", List.of(1, 2, 3));

            assertEquals("Item 1 Item 2 Item 3 ", processor.process("{{#each $numbers}}{{$prefix}} {{$item}} {{/each}}"));
            assertEquals("Number 1 Number 2 Number 3 ", processor.process("{{#each $numbers num}}Number {{$num}} {{/each}}"));
        }

        @Test
        @DisplayName("Should support nested loops with custom names to avoid conflicts")
        void nestedLoopsWithCustomNames() {
            processor.setVariable("categories", List.of(
                    new Category("Fruits", List.of("Apple", "Banana")),
                    new Category("Vegetables", List.of("Carrot", "Lettuce"))
            ));

            String template = normalize("""
                    {{#each $categories cat}}
                    {{$cat.name}}:
                    {{#each $cat.items product}}
                    - {{$product}}
                    {{/each}}
                    {{/each}}
                    """);

            assertEquals(normalize("""
                    Fruits:
                    - Apple
                    - Banana
                    Vegetables:
                    - Carrot
                    - Lettuce
                    """), processor.process(template));
        }

        @Test
        @DisplayName("Should handle null values in collections")
        void nullValuesInCollection() {
            List<String> items = new ArrayList<>();
            items.add("A");
            items.add(null);
            items.add("C");
            processor.setVariable("items", items);

            assertEquals("A,,C,", processor.process("{{#each $items}}{{$item}},{{/each}}"));
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
                    new Item("First", true, null),
                    new Item("Second", false, null),
                    new Item("Third", true, null)
            ));

            String template = normalize("""
                    {{#each $items}}
                    {{#if $item.active}}
                    <div>{{$item.name}}</div>
                    {{/if}}
                    {{/each}}
                    """);

            assertEquals(normalize("""
                    <div>First</div>
                    <div>Third</div>
                    """), processor.process(template));
        }

        @Test
        @DisplayName("Should handle complex real-world template")
        void complexRealWorldTemplate() {
            processor.setVariable("preset-active", "preset_01");
            processor.setVariable("render", true);
            processor.setVariable("preset-list", List.of(
                    new Item("preset_01", true, "Test name"),
                    new Item("preset_02", true, "Test name 02")
            ));

            String template = normalize("""
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
                    """);

            String result = processor.process(template);
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
                    """), result);
        }
    }

    // ========== COMPONENTS ==========

//    @Nested
//    @DisplayName("Components")
//    class Components {
//
//        @Test
//        @DisplayName("Should expand simple component with parameters")
//        void expandsComponentWithParameters() {
//            processor.registerComponent(
//                    "button",
//                    "<button id=\"{{$id}}\">{{$text}}</button>"
//            );
//
//            assertEquals(
//                    "<button id=\"myBtn\">Click Me</button>",
//                    processor.process("{{@button:text=Click Me,id=myBtn}}")
//            );
//        }
//
//        @Test
//        @DisplayName("Should allow components to access variables from scope")
//        void componentCanAccessVariablesFromScope() {
//            processor
//                    .setVariable("label", "Submit")
//                    .registerComponent("button", "<button>{{$label}}</button>");
//
//            assertEquals(
//                    "<button>Submit</button>",
//                    processor.process("{{@button}}")
//            );
//        }
//    }

    // ========== ERROR HANDLING ==========

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should throw exception for unterminated string")
        void unterminatedString() {
            assertThrows(RuntimeException.class, () -> processor.process("{{\"unterminated}}"));
        }

        @Test
        @DisplayName("Should throw exception for unknown filter")
        void unknownFilter() {
            processor.setVariable("name", "John");
            assertThrows(RuntimeException.class, () -> processor.process("{{$name | unknownfilter}}"));
        }

        @Test
        @DisplayName("Should throw exception for unclosed if block")
        void unclosedIfBlock() {
            assertThrows(RuntimeException.class, () -> processor.process("{{#if $var}}Content"));
        }

        @Test
        @DisplayName("Should throw exception for unclosed each block")
        void unclosedEachBlock() {
            assertThrows(RuntimeException.class, () -> processor.process("{{#each $items}}Content"));
        }
    }

    // ========== PERFORMANCE ==========

    @Nested
    @DisplayName("Performance")
    class Performance {

        @Test
        @DisplayName("Should handle large lists efficiently")
        void largeListPerformance() {
            List<Integer> largeList = new ArrayList<>();
            for (int i = 0; i < 1000; i++) largeList.add(i);
            processor.setVariable("numbers", largeList);

            long start = System.currentTimeMillis();
            String result = processor.process("{{#each $numbers}}{{$item}},{{/each}}");
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
            for (int i = 0; i < 100; i++) processor.process(template);
            long duration = System.currentTimeMillis() - start;

            assertTrue(duration < 1000, "100 iterations should complete in less than 1 second");
        }
    }
}
