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

import au.ellie.hyui.builders.BlockSelectorBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import au.ellie.hyui.utils.ParseUtils;
import org.jsoup.nodes.Element;

public class BlockSelectorHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        String tag = element.tagName().toLowerCase();
        return tag.equals("block-selector") || tag.equals("hyui-block-selector");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        BlockSelectorBuilder builder = BlockSelectorBuilder.blockSelector();

        if (element.hasAttr("data-hyui-capacity")) {
            ParseUtils.parseInt(element.attr("data-hyui-capacity"))
                    .ifPresent(builder::withCapacity);
        } else if (element.hasAttr("capacity")) {
            ParseUtils.parseInt(element.attr("capacity"))
                    .ifPresent(builder::withCapacity);
        }

        applyCommonAttributes(builder, element);
        return builder;
    }
}
