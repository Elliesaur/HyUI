/*
 *     Copyright (C) 2026 EllieAU
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package au.ellie.hyui.html.handlers;

import au.ellie.hyui.builders.LabelBuilder;
import au.ellie.hyui.builders.TabContentBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

public class TabContentHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        return element.tagName().equalsIgnoreCase("div") && element.hasClass("tab-content");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        TabContentBuilder builder = TabContentBuilder.tabContent();

        applyCommonAttributes(builder, element);

        if (element.hasAttr("data-hyui-tab-id")) {
            builder.withTabId(element.attr("data-hyui-tab-id"));
        }

        if (element.hasAttr("data-hyui-tab-nav")) {
            builder.withTabNavigationId(element.attr("data-hyui-tab-nav"));
        }

        for (Node childNode : element.childNodes()) {
            if (childNode instanceof Element childElement) {
                UIElementBuilder<?> child = parser.handleElement(childElement);
                if (child != null) {
                    builder.addChild(child);
                }
            } else if (childNode instanceof TextNode textNode) {
                String text = textNode.text().trim();
                if (!text.isEmpty()) {
                    builder.addChild(LabelBuilder.label().withText(text));
                }
            }
        }

        return builder;
    }
}
