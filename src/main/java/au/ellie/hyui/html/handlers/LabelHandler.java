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
import au.ellie.hyui.builders.NativeTimerLabelBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import au.ellie.hyui.types.TimerDirection;
import au.ellie.hyui.utils.ParseUtils;
import com.hypixel.hytale.server.core.Message;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.List;

public class LabelHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        String tag = element.tagName().toLowerCase();
        return tag.equals("label") || tag.equals("p");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        // Check if this is a native timer label
        if (element.hasClass("native-timer-label")) {
            NativeTimerLabelBuilder builder = NativeTimerLabelBuilder.nativeTimerLabel();

            if (element.hasAttr("data-hyui-seconds")) {
                ParseUtils.parseInt(element.attr("data-hyui-seconds"))
                        .ifPresent(builder::withSeconds);
            }
            if (element.hasAttr("data-hyui-direction")) {
                try {
                    TimerDirection direction = TimerDirection.valueOf(element.attr("data-hyui-direction"));
                    builder.withDirection(direction);
                } catch (IllegalArgumentException e) {
                    // Invalid direction, ignore
                }
            }
            if (element.hasAttr("data-hyui-paused")) {
                builder.withPaused(Boolean.parseBoolean(element.attr("data-hyui-paused")));
            }
            if (!element.text().isEmpty()) {
                builder.withText(element.text());
            }

            applyCommonAttributes(builder, element);
            return builder;
        }

        // Regular label
        LabelBuilder builder = LabelBuilder.label();
        List<Message> spans = parseMessageSpansFromChildren(element, true);
        if (spans != null && !spans.isEmpty()) {
            builder.withTextSpans(spans);
        } else {
            builder.withText(readDirectText(element));
        }
        applyCommonAttributes(builder, element);
        return builder;
    }

    private String readDirectText(Element element) {
        StringBuilder sb = new StringBuilder();
        for (Node child : element.childNodes()) {
            if (child instanceof TextNode textNode) {
                String text = textNode.text();
                if (!text.isBlank()) {
                    sb.append(text);
                }
            }
        }
        String text = sb.toString().trim();
        return text.isEmpty() ? element.ownText() : text;
    }
}
