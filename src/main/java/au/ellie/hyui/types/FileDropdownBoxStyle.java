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
 * FileDropdownBoxStyle type definition.
 */
public class FileDropdownBoxStyle implements HyUIBsonSerializable {
    private HyUIPatchStyle defaultBackground;
    private HyUIPatchStyle hoveredBackground;
    private HyUIPatchStyle pressedBackground;
    private String defaultArrowTexturePath;
    private String hoveredArrowTexturePath;
    private String pressedArrowTexturePath;
    private Integer arrowWidth;
    private Integer arrowHeight;
    private HyUIStyle labelStyle;
    private Integer horizontalPadding;
    private String panelAlign;
    private Integer panelOffset;
    private DropdownBoxSounds sounds;

    public FileDropdownBoxStyle withDefaultBackground(HyUIPatchStyle defaultBackground) {
        this.defaultBackground = defaultBackground;
        return this;
    }

    public FileDropdownBoxStyle withHoveredBackground(HyUIPatchStyle hoveredBackground) {
        this.hoveredBackground = hoveredBackground;
        return this;
    }

    public FileDropdownBoxStyle withPressedBackground(HyUIPatchStyle pressedBackground) {
        this.pressedBackground = pressedBackground;
        return this;
    }

    public FileDropdownBoxStyle withDefaultArrowTexturePath(String defaultArrowTexturePath) {
        this.defaultArrowTexturePath = defaultArrowTexturePath;
        return this;
    }

    public FileDropdownBoxStyle withHoveredArrowTexturePath(String hoveredArrowTexturePath) {
        this.hoveredArrowTexturePath = hoveredArrowTexturePath;
        return this;
    }

    public FileDropdownBoxStyle withPressedArrowTexturePath(String pressedArrowTexturePath) {
        this.pressedArrowTexturePath = pressedArrowTexturePath;
        return this;
    }

    public FileDropdownBoxStyle withArrowWidth(int arrowWidth) {
        this.arrowWidth = arrowWidth;
        return this;
    }

    public FileDropdownBoxStyle withArrowHeight(int arrowHeight) {
        this.arrowHeight = arrowHeight;
        return this;
    }

    public FileDropdownBoxStyle withLabelStyle(HyUIStyle labelStyle) {
        this.labelStyle = labelStyle;
        return this;
    }

    public FileDropdownBoxStyle withHorizontalPadding(int horizontalPadding) {
        this.horizontalPadding = horizontalPadding;
        return this;
    }

    public FileDropdownBoxStyle withPanelAlign(String panelAlign) {
        this.panelAlign = panelAlign;
        return this;
    }

    public FileDropdownBoxStyle withPanelOffset(int panelOffset) {
        this.panelOffset = panelOffset;
        return this;
    }

    public FileDropdownBoxStyle withSounds(DropdownBoxSounds sounds) {
        this.sounds = sounds;
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
        if (horizontalPadding != null) doc.set("HorizontalPadding", horizontalPadding);
        if (panelAlign != null) doc.set("PanelAlign", panelAlign);
        if (panelOffset != null) doc.set("PanelOffset", panelOffset);
        if (sounds != null) doc.set("Sounds", sounds.toBsonDocument());
    }

    public static FileDropdownBoxStyle defaultStyle() {
        return DefaultStyles.defaultFileDropdownBoxStyle();
    }
}
