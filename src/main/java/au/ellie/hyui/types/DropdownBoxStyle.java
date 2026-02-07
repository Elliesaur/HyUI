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

import au.ellie.hyui.builders.HyUIPatchStyle;
import au.ellie.hyui.builders.HyUIStyle;
import au.ellie.hyui.utils.BsonDocumentHelper;

/**
 * DropdownBoxStyle type definition.
 */
public class DropdownBoxStyle implements HyUIBsonSerializable {
    private HyUIPatchStyle defaultBackground;
    private HyUIPatchStyle hoveredBackground;
    private HyUIPatchStyle pressedBackground;
    private String defaultArrowTexturePath;
    private String hoveredArrowTexturePath;
    private String pressedArrowTexturePath;
    private Integer arrowWidth;
    private Integer arrowHeight;
    private HyUIStyle labelStyle;
    private HyUIStyle entryLabelStyle;
    private HyUIStyle noItemsLabelStyle;
    private HyUIStyle selectedEntryLabelStyle;
    private Integer horizontalPadding;
    private ScrollbarStyle panelScrollbarStyle;
    private HyUIPatchStyle panelBackground;
    private Integer panelPadding;
    private Integer panelWidth;
    private String panelAlign;
    private Integer panelOffset;
    private Integer entryHeight;
    private Integer entryIconWidth;
    private Integer entryIconHeight;
    private Integer entriesInViewport;
    private Integer horizontalEntryPadding;
    private HyUIPatchStyle hoveredEntryBackground;
    private HyUIPatchStyle pressedEntryBackground;
    private DropdownBoxSounds sounds;
    private ButtonSounds entrySounds;
    private Integer focusOutlineSize;
    private String focusOutlineColor;
    private HyUIStyle panelTitleLabelStyle;
    private HyUIPatchStyle selectedEntryIconBackground;
    private String iconTexturePath;
    private Integer iconWidth;
    private Integer iconHeight;

    public DropdownBoxStyle withDefaultBackground(HyUIPatchStyle defaultBackground) {
        this.defaultBackground = defaultBackground;
        return this;
    }

    public DropdownBoxStyle withHoveredBackground(HyUIPatchStyle hoveredBackground) {
        this.hoveredBackground = hoveredBackground;
        return this;
    }

    public DropdownBoxStyle withPressedBackground(HyUIPatchStyle pressedBackground) {
        this.pressedBackground = pressedBackground;
        return this;
    }

    public DropdownBoxStyle withDefaultArrowTexturePath(String defaultArrowTexturePath) {
        this.defaultArrowTexturePath = defaultArrowTexturePath;
        return this;
    }

    public DropdownBoxStyle withHoveredArrowTexturePath(String hoveredArrowTexturePath) {
        this.hoveredArrowTexturePath = hoveredArrowTexturePath;
        return this;
    }

    public DropdownBoxStyle withPressedArrowTexturePath(String pressedArrowTexturePath) {
        this.pressedArrowTexturePath = pressedArrowTexturePath;
        return this;
    }

    public DropdownBoxStyle withArrowWidth(int arrowWidth) {
        this.arrowWidth = arrowWidth;
        return this;
    }

    public DropdownBoxStyle withArrowHeight(int arrowHeight) {
        this.arrowHeight = arrowHeight;
        return this;
    }

    public DropdownBoxStyle withLabelStyle(HyUIStyle labelStyle) {
        this.labelStyle = labelStyle;
        return this;
    }

    public DropdownBoxStyle withEntryLabelStyle(HyUIStyle entryLabelStyle) {
        this.entryLabelStyle = entryLabelStyle;
        return this;
    }

    public DropdownBoxStyle withNoItemsLabelStyle(HyUIStyle noItemsLabelStyle) {
        this.noItemsLabelStyle = noItemsLabelStyle;
        return this;
    }

    public DropdownBoxStyle withSelectedEntryLabelStyle(HyUIStyle selectedEntryLabelStyle) {
        this.selectedEntryLabelStyle = selectedEntryLabelStyle;
        return this;
    }

    public DropdownBoxStyle withHorizontalPadding(int horizontalPadding) {
        this.horizontalPadding = horizontalPadding;
        return this;
    }

    public DropdownBoxStyle withPanelScrollbarStyle(ScrollbarStyle panelScrollbarStyle) {
        this.panelScrollbarStyle = panelScrollbarStyle;
        return this;
    }

    public DropdownBoxStyle withPanelBackground(HyUIPatchStyle panelBackground) {
        this.panelBackground = panelBackground;
        return this;
    }

    public DropdownBoxStyle withPanelPadding(int panelPadding) {
        this.panelPadding = panelPadding;
        return this;
    }

    public DropdownBoxStyle withPanelWidth(int panelWidth) {
        this.panelWidth = panelWidth;
        return this;
    }

    public DropdownBoxStyle withPanelAlign(String panelAlign) {
        this.panelAlign = panelAlign;
        return this;
    }

    public DropdownBoxStyle withPanelOffset(int panelOffset) {
        this.panelOffset = panelOffset;
        return this;
    }

    public DropdownBoxStyle withEntryHeight(int entryHeight) {
        this.entryHeight = entryHeight;
        return this;
    }

    public DropdownBoxStyle withEntryIconWidth(int entryIconWidth) {
        this.entryIconWidth = entryIconWidth;
        return this;
    }

    public DropdownBoxStyle withEntryIconHeight(int entryIconHeight) {
        this.entryIconHeight = entryIconHeight;
        return this;
    }

    public DropdownBoxStyle withEntriesInViewport(int entriesInViewport) {
        this.entriesInViewport = entriesInViewport;
        return this;
    }

    public DropdownBoxStyle withHorizontalEntryPadding(int horizontalEntryPadding) {
        this.horizontalEntryPadding = horizontalEntryPadding;
        return this;
    }

    public DropdownBoxStyle withHoveredEntryBackground(HyUIPatchStyle hoveredEntryBackground) {
        this.hoveredEntryBackground = hoveredEntryBackground;
        return this;
    }

    public DropdownBoxStyle withPressedEntryBackground(HyUIPatchStyle pressedEntryBackground) {
        this.pressedEntryBackground = pressedEntryBackground;
        return this;
    }

    public DropdownBoxStyle withSounds(DropdownBoxSounds sounds) {
        this.sounds = sounds;
        return this;
    }

    public DropdownBoxStyle withEntrySounds(ButtonSounds entrySounds) {
        this.entrySounds = entrySounds;
        return this;
    }

    public DropdownBoxStyle withFocusOutlineSize(int focusOutlineSize) {
        this.focusOutlineSize = focusOutlineSize;
        return this;
    }

    public DropdownBoxStyle withFocusOutlineColor(String focusOutlineColor) {
        this.focusOutlineColor = focusOutlineColor;
        return this;
    }

    public DropdownBoxStyle withPanelTitleLabelStyle(HyUIStyle panelTitleLabelStyle) {
        this.panelTitleLabelStyle = panelTitleLabelStyle;
        return this;
    }

    public DropdownBoxStyle withSelectedEntryIconBackground(HyUIPatchStyle selectedEntryIconBackground) {
        this.selectedEntryIconBackground = selectedEntryIconBackground;
        return this;
    }

    public DropdownBoxStyle withIconTexturePath(String iconTexturePath) {
        this.iconTexturePath = iconTexturePath;
        return this;
    }

    public DropdownBoxStyle withIconWidth(int iconWidth) {
        this.iconWidth = iconWidth;
        return this;
    }

    public DropdownBoxStyle withIconHeight(int iconHeight) {
        this.iconHeight = iconHeight;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (defaultBackground != null) doc.set("DefaultBackground", defaultBackground.toBsonDocument());
        if (hoveredBackground != null) doc.set("HoveredBackground", hoveredBackground.toBsonDocument());
        if (pressedBackground != null) doc.set("PressedBackground", pressedBackground.toBsonDocument());
        if (defaultArrowTexturePath != null) doc.set("DefaultArrowTexturePath", defaultArrowTexturePath);
        if (hoveredArrowTexturePath != null) doc.set("HoveredArrowTexturePath", hoveredArrowTexturePath);
        if (pressedArrowTexturePath != null) doc.set("PressedArrowTexturePath", pressedArrowTexturePath);
        if (arrowWidth != null) doc.set("ArrowWidth", arrowWidth);
        if (arrowHeight != null) doc.set("ArrowHeight", arrowHeight);
        if (labelStyle != null) doc.set("LabelStyle", labelStyle.toBsonDocument());
        if (entryLabelStyle != null) doc.set("EntryLabelStyle", entryLabelStyle.toBsonDocument());
        if (noItemsLabelStyle != null) doc.set("NoItemsLabelStyle", noItemsLabelStyle.toBsonDocument());
        if (selectedEntryLabelStyle != null) doc.set("SelectedEntryLabelStyle", selectedEntryLabelStyle.toBsonDocument());
        if (horizontalPadding != null) doc.set("HorizontalPadding", horizontalPadding);
        if (panelScrollbarStyle != null) doc.set("PanelScrollbarStyle", panelScrollbarStyle.toBsonDocument());
        if (panelBackground != null) doc.set("PanelBackground", panelBackground.toBsonDocument());
        if (panelPadding != null) doc.set("PanelPadding", panelPadding);
        if (panelWidth != null) doc.set("PanelWidth", panelWidth);
        if (panelAlign != null) doc.set("PanelAlign", panelAlign);
        if (panelOffset != null) doc.set("PanelOffset", panelOffset);
        if (entryHeight != null) doc.set("EntryHeight", entryHeight);
        if (entryIconWidth != null) doc.set("EntryIconWidth", entryIconWidth);
        if (entryIconHeight != null) doc.set("EntryIconHeight", entryIconHeight);
        if (entriesInViewport != null) doc.set("EntriesInViewport", entriesInViewport);
        if (horizontalEntryPadding != null) doc.set("HorizontalEntryPadding", horizontalEntryPadding);
        if (hoveredEntryBackground != null) doc.set("HoveredEntryBackground", hoveredEntryBackground.toBsonDocument());
        if (pressedEntryBackground != null) doc.set("PressedEntryBackground", pressedEntryBackground.toBsonDocument());
        if (sounds != null) doc.set("Sounds", sounds.toBsonDocument());
        if (entrySounds != null) doc.set("EntrySounds", entrySounds.toBsonDocument());
        if (focusOutlineSize != null) doc.set("FocusOutlineSize", focusOutlineSize);
        if (focusOutlineColor != null) doc.set("FocusOutlineColor", focusOutlineColor);
        if (panelTitleLabelStyle != null) doc.set("PanelTitleLabelStyle", panelTitleLabelStyle.toBsonDocument());
        if (selectedEntryIconBackground != null) doc.set("SelectedEntryIconBackground", selectedEntryIconBackground.toBsonDocument());
        if (iconTexturePath != null) doc.set("IconTexturePath", iconTexturePath);
        if (iconWidth != null) doc.set("IconWidth", iconWidth);
        if (iconHeight != null) doc.set("IconHeight", iconHeight);
    }

    public static DropdownBoxStyle defaultStyle() {
        return DefaultStyles.defaultDropdownBoxStyle();
    }

    public static DropdownBoxStyle titledStyle() {
        return DefaultStyles.titledDropdownBoxStyle();
    }
}
