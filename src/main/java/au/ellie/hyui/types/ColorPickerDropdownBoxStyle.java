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

import au.ellie.hyui.builders.HyUIAnchor;
import au.ellie.hyui.builders.HyUIPadding;
import au.ellie.hyui.builders.HyUIPatchStyle;
import au.ellie.hyui.utils.BsonDocumentHelper;

/**
 * ColorPickerDropdownBoxStyle type definition.
 */
public class ColorPickerDropdownBoxStyle implements HyUIBsonSerializable {
    private ColorPickerStyle colorPickerStyle;
    private ColorPickerDropdownBoxStateBackground background;
    private ColorPickerDropdownBoxStateBackground arrowBackground;
    private ColorPickerDropdownBoxStateBackground overlay;
    private HyUIPatchStyle panelBackground;
    private HyUIPadding panelPadding;
    private Integer panelWidth;
    private Integer panelOffset;
    private HyUIAnchor arrowAnchor;
    private ButtonSounds sounds;

    public ColorPickerDropdownBoxStyle withColorPickerStyle(ColorPickerStyle colorPickerStyle) {
        this.colorPickerStyle = colorPickerStyle;
        return this;
    }

    public ColorPickerDropdownBoxStyle withBackground(ColorPickerDropdownBoxStateBackground background) {
        this.background = background;
        return this;
    }

    public ColorPickerDropdownBoxStyle withArrowBackground(ColorPickerDropdownBoxStateBackground arrowBackground) {
        this.arrowBackground = arrowBackground;
        return this;
    }

    public ColorPickerDropdownBoxStyle withOverlay(ColorPickerDropdownBoxStateBackground overlay) {
        this.overlay = overlay;
        return this;
    }

    public ColorPickerDropdownBoxStyle withPanelBackground(HyUIPatchStyle panelBackground) {
        this.panelBackground = panelBackground;
        return this;
    }

    public ColorPickerDropdownBoxStyle withPanelPadding(HyUIPadding panelPadding) {
        this.panelPadding = panelPadding;
        return this;
    }

    public ColorPickerDropdownBoxStyle withPanelWidth(int panelWidth) {
        this.panelWidth = panelWidth;
        return this;
    }

    public ColorPickerDropdownBoxStyle withPanelOffset(int panelOffset) {
        this.panelOffset = panelOffset;
        return this;
    }

    public ColorPickerDropdownBoxStyle withArrowAnchor(HyUIAnchor arrowAnchor) {
        this.arrowAnchor = arrowAnchor;
        return this;
    }

    public ColorPickerDropdownBoxStyle withSounds(ButtonSounds sounds) {
        this.sounds = sounds;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (colorPickerStyle != null) doc.set("ColorPickerStyle", colorPickerStyle.toBsonDocument());
        if (background != null) doc.set("Background", background.toBsonDocument());
        if (arrowBackground != null) doc.set("ArrowBackground", arrowBackground.toBsonDocument());
        if (overlay != null) doc.set("Overlay", overlay.toBsonDocument());
        if (panelBackground != null) doc.set("PanelBackground", panelBackground.toBsonDocument());
        if (panelPadding != null) doc.set("PanelPadding", panelPadding.toBsonDocument());
        if (panelWidth != null) doc.set("PanelWidth", panelWidth);
        if (panelOffset != null) doc.set("PanelOffset", panelOffset);
        if (arrowAnchor != null) doc.set("ArrowAnchor", arrowAnchor.toBsonDocument());
        if (sounds != null) doc.set("Sounds", sounds.toBsonDocument());
    }

    public static ColorPickerDropdownBoxStyle defaultStyle() {
        return DefaultStyles.defaultColorPickerDropdownBoxStyle();
    }
}
