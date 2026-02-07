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

package au.ellie.hyui.types;

import au.ellie.hyui.builders.HyUIPadding;
import au.ellie.hyui.builders.HyUIPatchStyle;
import au.ellie.hyui.builders.HyUIStyle;
import au.ellie.hyui.utils.BsonDocumentHelper;

/**
 * PopupMenuLayerStyle type definition.
 */
public class PopupMenuLayerStyle implements HyUIBsonSerializable {
    private HyUIPatchStyle background;
    private Integer padding;
    private Integer baseHeight;
    private Integer maxWidth;
    private HyUIStyle titleStyle;
    private HyUIPatchStyle titleBackground;
    private Integer rowHeight;
    private HyUIStyle itemLabelStyle;
    private HyUIPadding itemPadding;
    private HyUIPatchStyle itemBackground;
    private Integer itemIconSize;
    private HyUIPatchStyle hoveredItemBackground;
    private HyUIPatchStyle pressedItemBackground;
    private SoundsStyle itemSounds;

    public PopupMenuLayerStyle withBackground(HyUIPatchStyle background) {
        this.background = background;
        return this;
    }

    public PopupMenuLayerStyle withPadding(int padding) {
        this.padding = padding;
        return this;
    }

    public PopupMenuLayerStyle withBaseHeight(int baseHeight) {
        this.baseHeight = baseHeight;
        return this;
    }

    public PopupMenuLayerStyle withMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public PopupMenuLayerStyle withTitleStyle(HyUIStyle titleStyle) {
        this.titleStyle = titleStyle;
        return this;
    }

    public PopupMenuLayerStyle withTitleBackground(HyUIPatchStyle titleBackground) {
        this.titleBackground = titleBackground;
        return this;
    }

    public PopupMenuLayerStyle withRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
        return this;
    }

    public PopupMenuLayerStyle withItemLabelStyle(HyUIStyle itemLabelStyle) {
        this.itemLabelStyle = itemLabelStyle;
        return this;
    }

    public PopupMenuLayerStyle withItemPadding(HyUIPadding itemPadding) {
        this.itemPadding = itemPadding;
        return this;
    }

    public PopupMenuLayerStyle withItemBackground(HyUIPatchStyle itemBackground) {
        this.itemBackground = itemBackground;
        return this;
    }

    public PopupMenuLayerStyle withItemIconSize(int itemIconSize) {
        this.itemIconSize = itemIconSize;
        return this;
    }

    public PopupMenuLayerStyle withHoveredItemBackground(HyUIPatchStyle hoveredItemBackground) {
        this.hoveredItemBackground = hoveredItemBackground;
        return this;
    }

    public PopupMenuLayerStyle withPressedItemBackground(HyUIPatchStyle pressedItemBackground) {
        this.pressedItemBackground = pressedItemBackground;
        return this;
    }

    public PopupMenuLayerStyle withItemSounds(SoundsStyle itemSounds) {
        this.itemSounds = itemSounds;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (background != null) doc.set("Background", background.toBsonDocument());
        if (padding != null) doc.set("Padding", padding);
        if (baseHeight != null) doc.set("BaseHeight", baseHeight);
        if (maxWidth != null) doc.set("MaxWidth", maxWidth);
        if (titleStyle != null) doc.set("TitleStyle", titleStyle.toBsonDocument());
        if (titleBackground != null) doc.set("TitleBackground", titleBackground.toBsonDocument());
        if (rowHeight != null) doc.set("RowHeight", rowHeight);
        if (itemLabelStyle != null) doc.set("ItemLabelStyle", itemLabelStyle.toBsonDocument());
        if (itemPadding != null) doc.set("ItemPadding", itemPadding.toBsonDocument());
        if (itemBackground != null) doc.set("ItemBackground", itemBackground.toBsonDocument());
        if (itemIconSize != null) doc.set("ItemIconSize", itemIconSize);
        if (hoveredItemBackground != null) doc.set("HoveredItemBackground", hoveredItemBackground.toBsonDocument());
        if (pressedItemBackground != null) doc.set("PressedItemBackground", pressedItemBackground.toBsonDocument());
        if (itemSounds != null) doc.set("ItemSounds", itemSounds.toBsonDocument());
    }

    public static PopupMenuLayerStyle defaultStyle() {
        return DefaultStyles.defaultPopupMenuLayerStyle();
    }
}
