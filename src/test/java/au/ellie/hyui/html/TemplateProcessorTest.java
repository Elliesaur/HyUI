package au.ellie.hyui.html;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateProcessorTest {

    private TemplateProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TemplateProcessor();
    }

    /* --------------------------------------------------
     * Variable substitution
     * -------------------------------------------------- */

    @Test
    void replacesSimpleVariable() {
        processor.setVariable("name", "Ellie");

        assertEquals(
                "Hello Ellie!",
                processor.process("Hello {{$name}}!")
        );
    }

    @Test
    void usesDefaultValueWhenVariableIsMissing() {
        assertEquals(
                "Score: 0",
                processor.process("Score: {{$score|0}}")
        );
    }

    /* --------------------------------------------------
     * Filters
     * -------------------------------------------------- */

    @Nested
    class StringFilters {

        @Test
        void upper_convertsToUppercase() {
            processor.setVariable("value", "hello");

            assertEquals(
                    "HELLO",
                    processor.process("{{$value|upper}}")
            );
        }

        @Test
        void lower_convertsToLowercase() {
            processor.setVariable("value", "HeLLo");

            assertEquals(
                    "hello",
                    processor.process("{{$value|lower}}")
            );
        }

        @Test
        void trim_removesLeadingAndTrailingWhitespace() {
            processor.setVariable("value", "  hello   ");

            assertEquals(
                    "hello",
                    processor.process("{{$value|trim}}")
            );
        }

        @Test
        void capitalize_capitalizesFirstLetterOnly() {
            processor.setVariable("value", "hello world");

            assertEquals(
                    "Hello world",
                    processor.process("{{$value|capitalize}}")
            );
        }
    }

    @Nested
    class NumberFilters {

        @Test
        void number_formatsNumber() {
            processor.setVariable("value", 1234);

            assertEquals(
                    "1,234",
                    processor.process("{{$value|number}}")
            );
        }

        @Test
        void percent_formatsPercent() {
            processor.setVariable("value", 0.125);

            assertEquals(
                    "13%",
                    processor.process("{{$value|percent}}")
            );
        }
    }

    /* --------------------------------------------------
     * If blocks
     * -------------------------------------------------- */

    @Nested
    class IfBlocks {

        @Test
        void rendersTrueBranch() {
            processor.setVariable("loggedIn", true);

            String template = """
                    {{#if loggedIn}}
                    Welcome back!
                    {{else}}
                    Please log in
                    {{/if}}
                    """;

            assertEquals(
                    "Welcome back!",
                    processor.process(template).trim()
            );
        }

        @Test
        void rendersFalseBranch() {
            processor.setVariable("loggedIn", false);

            String template = """
                    {{#if loggedIn}}
                    Welcome!
                    {{else}}
                    Please log in
                    {{/if}}
                    """;

            assertEquals(
                    "Please log in",
                    processor.process(template).trim()
            );
        }

        @Test
        void withoutElse_rendersNothingWhenFalse() {
            processor.setVariable("enabled", false);

            String template = """
                    Before
                    {{#if enabled}}
                    Enabled
                    {{/if}}
                    After
                    """;

            String result = processor.process(template)
                    .replaceAll("\\s+", " ")
                    .trim();

            assertEquals("Before After", result);
        }
    }

    /* --------------------------------------------------
     * Each blocks
     * -------------------------------------------------- */

    @Nested
    class EachBlocks {

        @Test
        void iteratesOverList() {
            processor.setVariable("items", List.of("A", "B", "C"));

            String template = """
                    {{#each items}}
                    <span>{{$item}}</span>
                    {{/each}}
                    """;

            String result = processor.process(template)
                    .replaceAll("\\s+", "");

            assertEquals(
                    "<span>A</span><span>B</span><span>C</span>",
                    result
            );
        }

        @Test
        void exposesMapEntriesAsVariables() {
            processor.setVariable(
                    "users",
                    List.of(
                            Map.of("name", "Alice"),
                            Map.of("name", "Bob")
                    )
            );

            String template = """
                    {{#each users}}
                    {{$name}}
                    {{/each}}
                    """;

            assertEquals(
                    "AliceBob",
                    processor.process(template).replaceAll("\\s+", "")
            );
        }
    }

    /* --------------------------------------------------
     * Components
     * -------------------------------------------------- */

    @Nested
    class Components {

        @Test
        void expandsComponentWithParameters() {
            processor.registerComponent(
                    "button",
                    "<button id=\"{{$id}}\">{{$text}}</button>"
            );

            assertEquals(
                    "<button id=\"myBtn\">Click Me</button>",
                    processor.process("{{@button:text=Click Me,id=myBtn}}")
            );
        }

        @Test
        void componentCanAccessVariablesFromScope() {
            processor
                    .setVariable("label", "Submit")
                    .registerComponent("button", "<button>{{$label}}</button>");

            assertEquals(
                    "<button>Submit</button>",
                    processor.process("{{@button}}")
            );
        }
    }

    @Nested
    class TagComponentsWithSlots {

        @Test
        void usesNamedSlotProvidedByCaller() {
            processor.registerComponent("card",
                    "<div class=\"card\">\n" +
                    "  <h1>{{$title}}</h1>\n" +
                    "  <div class=\"body\"><::body><p>No body</p></::body></div>\n" +
                    "</div>");

            String template = "<card title=\"Hi\"><:body><p>Custom body</p></:body></card>";

            String result = processor.process(template).replaceAll("\\s+", " ").trim();
            assertEquals("<div class=\"card\"> <h1>Hi</h1> <div class=\"body\"> <p>Custom body</p> </div> </div>", result);
        }

        @Test
        void fallsBackToComponentDefaultWhenSlotMissing() {
            processor.registerComponent("card",
                    "<div class=\"card\">\n" +
                    "  <h1>{{$title}}</h1>\n" +
                    "  <div class=\"body\"><::body><p>Default body</p></::body></div>\n" +
                    "</div>");

            String template = "<card title=\"Hi\"></card>";

            String result = processor.process(template).replaceAll("\\s+", " ").trim();
            assertEquals("<div class=\"card\"> <h1>Hi</h1> <div class=\"body\"> <p>Default body</p> </div> </div>", result);
        }

        @Test
        void selfClosingComponentSlotProducesEmptyDefault() {
            processor.registerComponent("frag",
                    "<div><::slot /></div>");

            String result = processor.process("<frag></frag>").replaceAll("\\s+", " ").trim();
            assertEquals("<div> </div>", result);
        }

        @Test
        void tokenComponentRecursionThrowsAtLimit() {
            // Create a component that includes itself via token-style to trigger recursion
            processor.registerComponent("loop", "{{@loop}}");

            // Expect an IllegalStateException due to recursion depth exceeded
            org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
                processor.process("{{@loop}}");
            });
        }

        @Test
        void tagComponentReceivesObjectAttribute() {
            processor.registerComponent("playerDisplay",
                    "<div>{{$player.name}} - {{$player.id}}</div>");

            TestPlayer player = new TestPlayer("Name", 123);
            processor.setVariable("player", player);

            String result = processor.process("<playerDisplay player=\"{{$player}}\"></playerDisplay>")
                    .replaceAll("\\s+", " ").trim();
            assertEquals("<div>Name - 123</div>", result);
        }

        @Test
        void componentTemplateUtilityActsAsPassthrough() {
            processor.setVariable("id", 1);

            String tpl = "<template if=\"id > 0\">Positive</template><template else>Other</template>";
            String result = processor.process(tpl).replaceAll("\\s+", " ").trim();
            assertEquals("Positive", result, "got: '" + result + "'");
        }

        @Test
        void unnamedContentMapsToDefaultSlot() {
            processor.registerComponent("box",
                    "<div class=\"box\"> <::default><p>Default</p></::default> </div>");

            String tpl = "<box>Custom content</box>";
            String result = processor.process(tpl).replaceAll("\\s+", " ").trim();
            assertEquals("<div class=\"box\"> Custom content </div>", result);
        }
    }

    @Nested
    class TokenComponentObjectParams {
        @Test
        void tokenComponentReceivesObjectParameter() {
            processor.registerComponent("playerDisplay",
                    "<div>{{$player.name}} - {{$player.id}}</div>");

            TestPlayer player = new TestPlayer("Name", 123);
            processor.setVariable("player", player);

            String result = processor.process("<playerDisplay player=\"{{$player}}\" />")
                    .replaceAll("\\s+", " ").trim();
            assertEquals("<div>Name - 123</div>", result);
        }
    }

    @Nested
    class ForAttributeTests {

        @Test
        void tagComponentForIteratesWithIndex() {
            processor.registerComponent("playerRow", "<div>{{$idx}}: {{$player.name}}</div>");

            processor.setVariable("players", List.of(
                    new TestPlayer("Alice", 1),
                    new TestPlayer("Bob", 2)
            ));

            String tpl = "<playerRow for=\"player, idx in players\"></playerRow>";
            String result = processor.process(tpl).replaceAll("\\s+", " ").trim();
            assertEquals("<div>0: Alice</div><div>1: Bob</div>", result);
        }

        @Test
        void templateUtilityForIteratesWithoutIndex() {
            processor.setVariable("players", List.of(
                    new TestPlayer("Alice", 1),
                    new TestPlayer("Bob", 2)
            ));

            String tpl = "<template for=\"player in players\">Player {{$player.name}}</template>";
            String result = processor.process(tpl).replaceAll("\\s+", " ").trim();
            assertEquals("Player Alice Player Bob", result);
        }

        @Test
        void htmlTagForIteratesWithIndex() {
            processor.setVariable("players", List.of(
                    new TestPlayer("Alice", 1),
                    new TestPlayer("Bob", 2)
            ));

            String tpl = "<div for=\"player, idx in players\">Player {{$idx}} {{$player.name}}</div>";
            String result = processor.process(tpl).replaceAll("\\s+", " ").trim();
            assertEquals("<div>Player 0 Alice</div><div>Player 1 Bob</div>", result);
        }
    }

    /* --------------------------------------------------
     * Supplier laziness
     * -------------------------------------------------- */

    @Nested
    class SupplierEvaluation {

        @Test
        void supplierIsNotEvaluatedWhenIfConditionIsFalse() {
            AtomicInteger evaluations = new AtomicInteger();

            processor
                    .setVariable("enabled", false)
                    .setVariable("secret", () -> {
                        evaluations.incrementAndGet();
                        return "SHOULD NOT HAPPEN";
                    });

            String template = """
                    {{#if enabled}}
                    {{$secret}}
                    {{/if}}
                    """;

            assertEquals("", processor.process(template).trim());
            assertEquals(0, evaluations.get(), "Supplier must not be evaluated");
        }

        @Test
        void supplierIsEvaluatedWhenIfConditionIsTrue() {
            AtomicInteger evaluations = new AtomicInteger();

            processor
                    .setVariable("enabled", true)
                    .setVariable("value", () -> {
                        evaluations.incrementAndGet();
                        return "OK";
                    });

            String template = """
                    {{#if enabled}}
                    {{$value}}
                    {{/if}}
                    """;

            assertEquals("OK", processor.process(template).trim());
            assertEquals(1, evaluations.get());
        }
    }

    /* --------------------------------------------------
     * Combined scenario
     * -------------------------------------------------- */

    @Test
    void complexTemplateRendersCorrectly() {
        processor
                .setVariable("player", "Ellie")
                .setVariable("online", true)
                .setVariable("scores", List.of(10, 20))
                .registerComponent("score", "<li>{{$item}}</li>");

        String template = """
                <h1>Hello {{$player}}</h1>
                
                {{#if online}}
                <ul>
                    {{#each scores}}
                        {{@score}}
                    {{/each}}
                </ul>
                {{else}}
                Offline
                {{/if}}
                """;

        String result = processor.process(template)
                .replaceAll("\\s+", "");

        assertEquals(
                "<h1>HelloEllie</h1><ul><li>10</li><li>20</li></ul>",
                result
        );
    }

    @Nested
    class IfAttributeElements {

        @Test
        void htmlTagIfAttribute_rendersWhenTrue() {
            processor.setVariable("show", true);
            String tpl = "<div if=\"show\">Visible</div>";
            String result = processor.process(tpl).replaceAll("\\s+", " ").trim();
            assertEquals("<div>Visible</div>", result);
        }

        @Test
        void htmlTagIfAttribute_rendersNothingWhenFalse() {
            processor.setVariable("show", false);
            String tpl = "<div if=\"show\">Visible</div>";
            String result = processor.process(tpl).replaceAll("\\s+", " ").trim();
            assertEquals("", result);
        }

        @Test
        void componentTagIfAttribute_rendersWhenTrue() {
            processor.registerComponent("panel", "<section class=\"panel\">{{$default}}</section>");
            processor.setVariable("ok", true);

            String tpl = "<panel if=\"ok\">Content</panel>";
            String result = processor.process(tpl).replaceAll("\\s+", " ").trim();
            assertEquals("<section class=\"panel\"> Content </section>", result);
        }

        @Test
        void componentTagIfAttribute_rendersNothingWhenFalse() {
            processor.registerComponent("panel", "<section class=\"panel\">{{$default}}</section>");
            processor.setVariable("ok", false);

            String tpl = "<panel if=\"ok\">Content</panel>";
            String result = processor.process(tpl).replaceAll("\\s+", " ").trim();
            assertEquals("", result);
        }

        @Test
        void consecutiveElseIfElse_onPlainTags_resolvesCorrectBranch() {
            processor.setVariable("a", false);
            processor.setVariable("b", true);

            String tpl = "<div if=\"a\">A</div><div else-if=\"b\">B</div><div else>C</div>";
            String result = processor.process(tpl).replaceAll("\\s+", " ").trim();
            assertEquals("<div>B</div>", result);
        }

        @Test
        void consecutiveElseIfElse_onPlainTags_usesElseWhenAllFalse() {
            processor.setVariable("a", false);
            processor.setVariable("b", false);

            String tpl = "<div if=\"a\">A</div><div else-if=\"b\">B</div><div else>C</div>";
            String result = processor.process(tpl).replaceAll("\\s+", " ").trim();
            assertEquals("<div>C</div>", result);
        }
    }
}

/* Simple test helper */
class TestPlayer {
    public final String name;
    public final int id;
    public TestPlayer(String name, int id) { this.name = name; this.id = id; }
    public String getName() { return name; }
    public int getId() { return id; }
}
