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
