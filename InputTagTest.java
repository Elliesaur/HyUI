import au.ellie.hyui.html.TemplateProcessor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class InputTagTest {
    public static void main(String[] args) {
        TemplateProcessor processor = new TemplateProcessor();

        // Test 1: Input WITHOUT self-closing /
        String template1 = """
                <div class="parent">
                    <input type="text" id="test" value="test" >
                    <div class="sibling">
                        <p>Should be sibling</p>
                    </div>
                </div>
                """;

        String processed1 = processor.setTemplate(template1).process();
        System.out.println("=== INPUT WITHOUT /> ===");
        System.out.println(processed1);

        Document doc1 = Jsoup.parseBodyFragment(processed1);
        System.out.println("\n=== JSOUP PARSED ===");
        System.out.println(doc1.body().html());
        System.out.println();

        // Test 2: Input WITH self-closing />
        String template2 = """
                <div class="parent">
                    <input type="text" id="test" value="test" />
                    <div class="sibling">
                        <p>Should be sibling</p>
                    </div>
                </div>
                """;

        String processed2 = processor.setTemplate(template2).process();
        System.out.println("=== INPUT WITH /> ===");
        System.out.println(processed2);

        Document doc2 = Jsoup.parseBodyFragment(processed2);
        System.out.println("\n=== JSOUP PARSED ===");
        System.out.println(doc2.body().html());
    }
}

