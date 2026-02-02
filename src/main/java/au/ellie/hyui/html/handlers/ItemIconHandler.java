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

import au.ellie.hyui.builders.ItemIconBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import org.jsoup.nodes.Element;

public class ItemIconHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        return element.tagName().equalsIgnoreCase("span") && element.hasClass("item-icon");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        ItemIconBuilder builder = ItemIconBuilder.itemIcon();

        if (element.hasAttr("data-hyui-item-id")) {
            builder.withItemId(element.attr("data-hyui-item-id"));
        } else if (element.hasAttr("src")) {
            builder.withItemId(element.attr("src"));
        }
        
        applyCommonAttributes(builder, element);
        
        return builder;
    }
}
