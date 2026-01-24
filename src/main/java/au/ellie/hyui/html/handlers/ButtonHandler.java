package au.ellie.hyui.html.handlers;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.builders.ButtonBuilder;
import au.ellie.hyui.builders.LabelBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import org.jsoup.nodes.Element;

public class ButtonHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        String tag = element.tagName().toLowerCase();
        if (tag.equals("button")) return true;
        if (tag.equals("input")) {
            String type = element.attr("type").toLowerCase();
            return type.equals("submit") || type.equals("reset");
        }
        return false;
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        String tag = element.tagName().toLowerCase();
        ButtonBuilder builder;

        boolean hasItemIcon = !element.select("> span.item-icon").isEmpty();
        boolean hasItemSlot = !element.select("> span.item-slot").isEmpty();
        boolean isRaw = hasItemIcon || hasItemSlot;

        if (tag.equals("input") && element.attr("type").equalsIgnoreCase("reset")) {
            builder = ButtonBuilder.cancelTextButton();
        } else if (tag.equals("button") && element.hasClass("back-button")) {
            builder = ButtonBuilder.backButton();
        } else if (tag.equals("button") && element.hasClass("secondary-button")) {
            builder = ButtonBuilder.secondaryTextButton();
        } else if (tag.equals("button") && element.hasClass("tertiary-button")) {
            builder = ButtonBuilder.tertiaryTextButton();
        } else if (isRaw) {
            builder = ButtonBuilder.rawButton();
        } else {
            builder = ButtonBuilder.textButton();
        }

        // Parse children
        java.util.List<UIElementBuilder<?>> children = parser.parseChildren(element);
        if (!children.isEmpty()) {
            for (UIElementBuilder<?> child : children) {
                if (!isRaw && child instanceof LabelBuilder label) {
                    builder.withText(label.getText());
                } else {
                    builder.addChild(child);
                }
            }
        } else {
            String text = element.text();
            if (tag.equals("input") && element.hasAttr("value")) {
                text = element.attr("value");
            }
            
            if (!text.isEmpty()) {
                builder.withText(text);
            }
        }
        applyCommonAttributes(builder, element);

        return builder;
    }
}
