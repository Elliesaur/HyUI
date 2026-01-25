package au.ellie.hyui.html.handlers;

import au.ellie.hyui.builders.*;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.List;

public class DivHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        return element.tagName().equalsIgnoreCase("div");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        if (element.hasClass("container-title") || element.hasClass("container-contents")) {
            // These are special markers and should not be created as elements themselves.
            return null;
        }

        UIElementBuilder<?> builder;
        if (element.hasClass("page-overlay")) {
            builder = PageOverlayBuilder.pageOverlay();
        } else if (element.hasClass("container") || element.hasClass("decorated-container")) {
            builder = element.hasClass("decorated-container") ? 
                    ContainerBuilder.decoratedContainer() 
                    : ContainerBuilder.container();
            if (element.hasAttr("data-hyui-title")) {
                ((ContainerBuilder) builder).withTitleText(element.attr("data-hyui-title"));
            }
        } else {
            builder = GroupBuilder.group();
        }

        applyCommonAttributes(builder, element);

        for (Node childNode : element.childNodes()) {
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                if (childElement.hasClass("container-title") && builder instanceof ContainerBuilder) {
                    List<UIElementBuilder<?>> children = parser.parseChildren(childElement);
                    for (UIElementBuilder<?> child : children) {
                        ((ContainerBuilder) builder).addTitleChild(child);
                    }
                } else if (childElement.hasClass("container-contents") && builder instanceof ContainerBuilder) {
                    List<UIElementBuilder<?>> children = parser.parseChildren(childElement);
                    for (UIElementBuilder<?> child : children) {
                        ((ContainerBuilder) builder).addContentChild(child);
                    }
                } else {
                    UIElementBuilder<?> child = parser.handleElement(childElement);
                    if (child != null) {
                        if (builder instanceof ContainerBuilder && !child.getElementPath().contains("PageOverlay")) {
                            // By default add to content for container
                            ((ContainerBuilder) builder).addContentChild(child);
                        } else {
                            builder.addChild(child);
                        }
                    }
                }
            } else if (childNode instanceof TextNode) {
                String text = ((TextNode) childNode).text().trim();
                if (!text.isEmpty()) {
                    UIElementBuilder<?> label = LabelBuilder.label().withText(text);
                    if (builder instanceof ContainerBuilder) {
                        ((ContainerBuilder) builder).addContentChild(label);
                    } else {
                        builder.addChild(label);
                    }
                }
            }
        }

        return builder;
    }
}
