package au.ellie.hyui.html.handlers;

import au.ellie.hyui.builders.HyvatarImageBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import au.ellie.hyui.utils.HyvatarUtils;
import au.ellie.hyui.utils.ParseUtils;
import org.jsoup.nodes.Element;

public class HyvatarHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        return element.tagName().equalsIgnoreCase("hyvatar");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        HyvatarImageBuilder builder = HyvatarImageBuilder.hyvatar();

        if (element.hasAttr("username")) {
            builder.withUsername(element.attr("username"));
        }
        if (element.hasAttr("render")) {
            HyvatarUtils.RenderType renderType = HyvatarUtils.parseRenderType(element.attr("render"));
            builder.withRenderType(renderType);
        }
        if (element.hasAttr("size")) {
            ParseUtils.parseInt(element.attr("size")).ifPresent(builder::withSize);
            element.attr("width", element.attr("size"));
            element.attr("height", element.attr("size"));
        } else {
            // We know it defaults to 64, let's set anchors based on this.
            element.attr("width", "64");
            element.attr("height", "64");
        }
        if (element.hasAttr("rotate")) {
            ParseUtils.parseInt(element.attr("rotate")).ifPresent(builder::withRotate);
        }
        if (element.hasAttr("cape")) {
            builder.withCape(element.attr("cape"));
        }

        applyCommonAttributes(builder, element);
        return builder;
    }
}
