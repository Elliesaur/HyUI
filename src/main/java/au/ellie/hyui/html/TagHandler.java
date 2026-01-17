package au.ellie.hyui.html;

import au.ellie.hyui.builders.HyUIAnchor;
import au.ellie.hyui.builders.HyUIStyle;
import au.ellie.hyui.builders.UIElementBuilder;
import com.hypixel.hytale.server.core.Message;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface for handling a specific HTML tag and converting it to a HyUI builder.
 */
public interface TagHandler {
    /**
     * Checks if this handler can handle the given element.
     *
     * @param element The Jsoup element to check.
     * @return true if this handler can process the element, false otherwise.
     */
    boolean canHandle(Element element);

    /**
     * Handles the conversion of the HTML element to a UIElementBuilder.
     *
     * @param element The Jsoup element to convert.
     * @param parser  The parser instance for recursive calls if needed.
     * @return A UIElementBuilder representing the element, or null if it should be ignored.
     */
    UIElementBuilder<?> handle(Element element, HtmlParser parser);

    /**
     * Applies common attributes like id, style, data-*, etc. to the builder.
     *
     * @param builder The builder to apply attributes to.
     * @param element The HTML element containing the attributes.
     */
    default void applyCommonAttributes(UIElementBuilder<?> builder, Element element) {
        if (element.hasAttr("id")) {
            builder.withId(element.attr("id"));
        }

        if (element.hasAttr("data-hyui-tooltiptext")) {
            builder.withTooltipTextSpan(Message.raw(element.attr("data-hyui-tooltiptext")));
        }

        if (element.hasAttr("data-hyui-flexweight")) {
            try {
                builder.withFlexWeight(Integer.parseInt(element.attr("data-hyui-flexweight")));
            } catch (NumberFormatException ignored) {}
        }

        if (element.hasAttr("style")) {
            Map<String, String> styles = parseStyleAttribute(element.attr("style"));
            applyStyles(builder, styles);
        }
    }

    private Map<String, String> parseStyleAttribute(String styleAttr) {
        Map<String, String> styles = new HashMap<>();
        String[] declarations = styleAttr.split(";");
        for (String declaration : declarations) {
            String[] parts = declaration.split(":", 2);
            if (parts.length == 2) {
                styles.put(parts[0].trim().toLowerCase(), parts[1].trim());
            }
        }
        return styles;
    }

    private void applyStyles(UIElementBuilder<?> builder, Map<String, String> styles) {
        HyUIStyle hyStyle = new HyUIStyle();
        HyUIAnchor anchor = new HyUIAnchor();
        boolean hasStyle = false;
        boolean hasAnchor = false;

        for (Map.Entry<String, String> entry : styles.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            switch (key) {
                case "color":
                    hyStyle.setTextColor(value);
                    hasStyle = true;
                    break;
                case "font-size":
                    hyStyle.setFontSize(value);
                    hasStyle = true;
                    break;
                case "font-weight":
                    if (value.equalsIgnoreCase("bold")) {
                        hyStyle.setRenderBold(true);
                        hasStyle = true;
                    }
                    break;
                case "text-transform":
                    if (value.equalsIgnoreCase("uppercase")) {
                        hyStyle.setRenderUppercase(true);
                        hasStyle = true;
                    }
                    break;
                case "text-align":
                case "layout-mode":
                case "layout":
                    // This maps to LayoutMode in some builders
                    if (builder instanceof au.ellie.hyui.builders.GroupBuilder) {
                        ((au.ellie.hyui.builders.GroupBuilder) builder).withLayoutMode(capitalize(value));
                    }
                    break;
                case "vertical-align":
                    hyStyle.setVerticalAlignment(capitalize(value));
                    hasStyle = true;
                    break;
                case "horizontal-align":
                    hyStyle.setHorizontalAlignment(capitalize(value));
                    hasStyle = true;
                    break;
                case "align":
                    hyStyle.setAlignment(capitalize(value));
                    hasStyle = true;
                    break;
                case "flex-weight":
                    try {
                        builder.withFlexWeight(Integer.parseInt(value));
                    } catch (NumberFormatException ignored) {}
                    break;
                case "anchor-left":
                    try {
                        anchor.setLeft(Integer.parseInt(value));
                        hasAnchor = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "anchor-right":
                    try {
                        anchor.setRight(Integer.parseInt(value));
                        hasAnchor = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "anchor-top":
                    try {
                        anchor.setTop(Integer.parseInt(value));
                        hasAnchor = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "anchor-bottom":
                    try {
                        anchor.setBottom(Integer.parseInt(value));
                        hasAnchor = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "anchor-width":
                    try {
                        anchor.setWidth(Integer.parseInt(value));
                        hasAnchor = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "anchor-height":
                    try {
                        anchor.setHeight(Integer.parseInt(value));
                        hasAnchor = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "anchor":
                    try {
                        anchor.setFull(Integer.parseInt(value));
                        hasAnchor = true;
                    } catch (NumberFormatException ignored) {}
                    break;
            }
        }

        if (hasStyle) {
            builder.withStyle(hyStyle);
        }
        if (hasAnchor) {
            builder.withAnchor(anchor);
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        // Check for specific multi-word LayoutModes
        if (str.equalsIgnoreCase("topscrolling")) return "TopScrolling";
        if (str.equalsIgnoreCase("bottomscrolling")) return "BottomScrolling";
        if (str.equalsIgnoreCase("middlecenter")) return "MiddleCenter";
        if (str.equalsIgnoreCase("centermiddle")) return "CenterMiddle";
        if (str.equalsIgnoreCase("leftcenterwrap")) return "LeftCenterWrap";
        
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
