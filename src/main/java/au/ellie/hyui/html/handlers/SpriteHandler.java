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

import au.ellie.hyui.builders.SpriteBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import au.ellie.hyui.utils.ParseUtils;
import org.jsoup.nodes.Element;

public class SpriteHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        return element.tagName().equalsIgnoreCase("sprite");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        SpriteBuilder builder = SpriteBuilder.sprite();

        if (element.hasAttr("src")) {
            builder.withTexture(element.attr("src"));
        }

        int width = ParseUtils.parseIntOrDefault(element.attr("data-hyui-frame-width"), 0);
        int height = ParseUtils.parseIntOrDefault(element.attr("data-hyui-frame-height"), 0);
        int perRow = ParseUtils.parseIntOrDefault(element.attr("data-hyui-frame-per-row"), 0);
        int count = ParseUtils.parseIntOrDefault(element.attr("data-hyui-frame-count"), 0);

        if (width > 0 && height > 0 && perRow > 0 && count > 0) {
            builder.withFrame(width, height, perRow, count);
        }

        if (element.hasAttr("data-hyui-fps")) {
            ParseUtils.parseInt(element.attr("data-hyui-fps"))
                    .ifPresent(builder::withFramesPerSecond);
        }

        applyCommonAttributes(builder, element);
        return builder;
    }
}
