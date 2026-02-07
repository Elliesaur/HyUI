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

import au.ellie.hyui.builders.HyUIAnchor;
import au.ellie.hyui.builders.HyUIPatchStyle;
import au.ellie.hyui.builders.MenuItemBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import au.ellie.hyui.utils.ParseUtils;
import org.jsoup.nodes.Element;

public class MenuItemHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        String tag = element.tagName().toLowerCase();
        return tag.equals("menu-item") || tag.equals("hyui-menu-item");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        MenuItemBuilder builder = MenuItemBuilder.menuItem();

        if (element.hasAttr("data-hyui-text")) {
            builder.withText(element.attr("data-hyui-text"));
        } else if (!element.text().isBlank()) {
            builder.withText(element.text());
        }

        if (element.hasAttr("data-hyui-text-tooltip-style")) {
            applyStyleReference(element.attr("data-hyui-text-tooltip-style"), true, builder);
        }
        if (element.hasAttr("data-hyui-popup-style")) {
            applyStyleReference(element.attr("data-hyui-popup-style"), false, builder);
        }

        if (element.hasAttr("data-hyui-icon")) {
            HyUIPatchStyle icon = new HyUIPatchStyle().setTexturePath(element.attr("data-hyui-icon"));
            if (element.hasAttr("data-hyui-icon-border")) {
                ParseUtils.parseInt(element.attr("data-hyui-icon-border"))
                        .ifPresent(icon::setBorder);
            }
            builder.withIcon(icon);
        }

        HyUIAnchor anchor = parseAnchor(element, "data-hyui-icon-anchor-");
        if (anchor != null) {
            builder.withIconAnchor(anchor);
        }

        applyCommonAttributes(builder, element);
        return builder;
    }

    private void applyStyleReference(String rawValue, boolean isTooltipStyle, MenuItemBuilder builder) {
        if (rawValue == null || rawValue.isBlank()) {
            return;
        }
        String[] parts = rawValue.trim().split("\\s+");
        if (parts.length >= 2) {
            if (isTooltipStyle) {
                builder.withTextTooltipStyle(parts[0], parts[1]);
            } else {
                builder.withPopupStyle(parts[0], parts[1]);
            }
        } else {
            if (isTooltipStyle) {
                builder.withTextTooltipStyle(rawValue.trim());
            } else {
                builder.withPopupStyle(rawValue.trim());
            }
        }
    }

    private HyUIAnchor parseAnchor(Element element, String prefix) {
        HyUIAnchor anchor = new HyUIAnchor();
        boolean found = false;
        found |= setAnchorValue(element, prefix + "left", value -> anchor.setLeft(value));
        found |= setAnchorValue(element, prefix + "right", value -> anchor.setRight(value));
        found |= setAnchorValue(element, prefix + "top", value -> anchor.setTop(value));
        found |= setAnchorValue(element, prefix + "bottom", value -> anchor.setBottom(value));
        found |= setAnchorValue(element, prefix + "width", value -> anchor.setWidth(value));
        found |= setAnchorValue(element, prefix + "height", value -> anchor.setHeight(value));
        found |= setAnchorValue(element, prefix + "full", value -> anchor.setFull(value));
        found |= setAnchorValue(element, prefix + "horizontal", value -> anchor.setHorizontal(value));
        found |= setAnchorValue(element, prefix + "vertical", value -> anchor.setVertical(value));
        return found ? anchor : null;
    }

    private boolean setAnchorValue(Element element, String attr, java.util.function.IntConsumer setter) {
        if (!element.hasAttr(attr)) {
            return false;
        }
        return ParseUtils.parseInt(element.attr(attr)).map(value -> {
            setter.accept(value);
            return true;
        }).orElse(false);
    }
}
