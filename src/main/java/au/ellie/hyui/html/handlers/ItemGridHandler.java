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

import au.ellie.hyui.builders.ItemGridBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;
import com.hypixel.hytale.server.core.ui.PatchStyle;
import com.hypixel.hytale.server.core.ui.Value;
import org.jsoup.nodes.Element;

public class ItemGridHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        return element.tagName().equalsIgnoreCase("div") && element.hasClass("item-grid");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        ItemGridBuilder builder = ItemGridBuilder.itemGrid();

        if (element.hasAttr("data-hyui-background-mode")) {
            builder.withBackgroundMode(element.attr("data-hyui-background-mode"));
        }
        if (element.hasAttr("data-hyui-render-item-quality-background")) {
            builder.withRenderItemQualityBackground(Boolean.parseBoolean(
                    element.attr("data-hyui-render-item-quality-background")));
        }
        if (element.hasAttr("data-hyui-are-items-draggable")) {
            builder.withAreItemsDraggable(Boolean.parseBoolean(
                    element.attr("data-hyui-are-items-draggable")));
        }
        if (element.hasAttr("data-hyui-keep-scroll-position")) {
            builder.withKeepScrollPosition(Boolean.parseBoolean(
                    element.attr("data-hyui-keep-scroll-position")));
        }
        if (element.hasAttr("data-hyui-show-scrollbar")) {
            builder.withShowScrollbar(Boolean.parseBoolean(
                    element.attr("data-hyui-show-scrollbar")));
        }
        if (element.hasAttr("data-hyui-slots-per-row")) {
            try {
                builder.withSlotsPerRow(Integer.parseInt(element.attr("data-hyui-slots-per-row")));
            } catch (NumberFormatException ignored) {}
        }

        applyCommonAttributes(builder, element);

        for (Element child : element.children()) {
            if (child.hasClass("item-grid-slot")) {
                ItemGridSlot slot = parseSlot(child);
                if (slot != null) {
                    builder.addSlot(slot);
                }
            }
        }
        return builder;
    }

    private ItemGridSlot parseSlot(Element element) {
        ItemGridSlot slot = new ItemGridSlot();

        if (element.hasAttr("data-hyui-item-id")) {
            int quantity = 1;
            if (element.hasAttr("data-hyui-quantity")) {
                try {
                    quantity = Integer.parseInt(element.attr("data-hyui-quantity"));
                } catch (NumberFormatException ignored) {}
            }
            ItemStack stack = createItemStack(element.attr("data-hyui-item-id"), quantity);
            if (stack != null) {
                slot.setItemStack(stack);
            }
        } else {
        }
        if (element.hasAttr("data-hyui-name")) {
            slot.setName(element.attr("data-hyui-name"));
        }
        if (element.hasAttr("data-hyui-description")) {
            slot.setDescription(element.attr("data-hyui-description"));
        }
        if (element.hasAttr("data-hyui-item-incompatible")) {
            slot.setItemIncompatible(Boolean.parseBoolean(element.attr("data-hyui-item-incompatible")));
        }
        if (element.hasAttr("data-hyui-item-uncraftable")) {
            slot.setItemUncraftable(Boolean.parseBoolean(element.attr("data-hyui-item-uncraftable")));
        }
        if (element.hasAttr("data-hyui-activatable")) {
            slot.setActivatable(Boolean.parseBoolean(element.attr("data-hyui-activatable")));
        }
        if (element.hasAttr("data-hyui-skip-item-quality-background")) {
            slot.setSkipItemQualityBackground(Boolean.parseBoolean(
                    element.attr("data-hyui-skip-item-quality-background")));
        }
        if (element.hasAttr("data-hyui-slot-background")) {
            Value<PatchStyle> background = parsePatchStyleValue(element.attr("data-hyui-slot-background"));
            if (background != null) {
                slot.setBackground(background);
            }
        }
        if (element.hasAttr("data-hyui-slot-overlay")) {
            Value<PatchStyle> overlay = parsePatchStyleValue(element.attr("data-hyui-slot-overlay"));
            if (overlay != null) {
                slot.setOverlay(overlay);
            }
        }
        if (element.hasAttr("data-hyui-slot-icon")) {
            Value<PatchStyle> icon = parsePatchStyleValue(element.attr("data-hyui-slot-icon"));
            if (icon != null) {
                slot.setIcon(icon);
            }
        }
        return slot;
    }

    private ItemStack createItemStack(String itemId, int quantity) {
        if (itemId == null || itemId.isBlank()) {
            // We default to empty instead of null, this way we still retain some item in it.
            return ItemStack.EMPTY;
        }
        return new ItemStack(itemId, quantity);
    }

    private Value<PatchStyle> parsePatchStyleValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String[] parts = value.trim().split("\\s+");
        if (parts.length >= 2) {
            return Value.ref(parts[0].replace("\"", "").replace("'", ""),
                    parts[1].replace("\"", "").replace("'", ""));
        }
        return Value.ref("Common.ui", value.replace("\"", "").replace("'", ""));
    }
}
