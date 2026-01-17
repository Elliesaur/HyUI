package au.ellie.hyui.html;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.builders.LabelBuilder;
import au.ellie.hyui.builders.PageBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.handlers.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.List;

/**
 * A modular parser that converts HTML/XML-like language to HyUI builders.
 */
public class HtmlParser {
    private final List<TagHandler> handlers = new ArrayList<>();

    public HtmlParser() {
        // Register default handlers
        registerHandler(new DivHandler());
        registerHandler(new LabelHandler());
        registerHandler(new InputHandler());
        registerHandler(new ButtonHandler());
    }

    /**
     * Registers a new tag handler.
     *
     * @param handler The handler to register.
     */
    public void registerHandler(TagHandler handler) {
        handlers.add(handler);
    }

    /**
     * Parses the HTML string and adds elements to the PageBuilder.
     *
     * @param builder The PageBuilder to add elements to.
     * @param html    The HTML string to parse.
     */
    public void parseToPage(PageBuilder builder, String html) {
        List<UIElementBuilder<?>> elements = parse(html);
        for (UIElementBuilder<?> element : elements) {
            builder.addElement(element);
        }
    }

    /**
     * Parses the HTML string into a list of UIElementBuilders.
     *
     * @param html The HTML string to parse.
     * @return A list of UIElementBuilders.
     */
    public List<UIElementBuilder<?>> parse(String html) {
        Document doc = Jsoup.parseBodyFragment(html);
        new CssPreprocessor().process(doc);
        HyUIPlugin.getInstance().logInfo("Document elements after preprocessing: " + doc.body().html());
        return parseChildren(doc.body());
    }

    /**
     * Parses the children of a Jsoup element.
     *
     * @param parent The parent Jsoup element.
     * @return A list of UIElementBuilders.
     */
    public List<UIElementBuilder<?>> parseChildren(Element parent) {
        List<UIElementBuilder<?>> builders = new ArrayList<>();
        for (Node child : parent.childNodes()) {
            HyUIPlugin.getInstance().logInfo("Parsing child node: " + child.nodeName());
            
            if (child instanceof Element) {
                HyUIPlugin.getInstance().logInfo("Parsing ELEMENT node: " + child.nodeName());
                
                UIElementBuilder<?> builder = handleElement((Element) child);
                if (builder != null) {
                    HyUIPlugin.getInstance().logInfo("Parsed element: " + builder.getClass().getSimpleName());
                    builders.add(builder);
                }
            } else if (child instanceof TextNode) {
                String text = ((TextNode) child).text().trim();
                if (!text.isEmpty()) {
                    builders.add(LabelBuilder.label().withText(text));
                }
            }
        }
        return builders;
    }

    public UIElementBuilder<?> handleElement(Element element) {
        for (TagHandler handler : handlers) {
            if (handler.canHandle(element)) {
                return handler.handle(element, this);
            }
        }
        return null;
    }
}
