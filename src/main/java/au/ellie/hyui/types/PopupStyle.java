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
import au.ellie.hyui.utils.BsonDocumentHelper;

/**
 * PopupStyle type definition.
 */
public class PopupStyle implements HyUIBsonSerializable {
    private String background;
    private HyUIPadding buttonPadding;
    private HyUIPadding padding;
    private TextTooltipStyle tooltipStyle;
    private ButtonStyle buttonStyle;
    private ButtonStyle selectedButtonStyle;

    public PopupStyle withBackground(String background) {
        this.background = background;
        return this;
    }

    public PopupStyle withButtonPadding(HyUIPadding buttonPadding) {
        this.buttonPadding = buttonPadding;
        return this;
    }

    public PopupStyle withPadding(HyUIPadding padding) {
        this.padding = padding;
        return this;
    }

    public PopupStyle withTooltipStyle(TextTooltipStyle tooltipStyle) {
        this.tooltipStyle = tooltipStyle;
        return this;
    }

    public PopupStyle withButtonStyle(ButtonStyle buttonStyle) {
        this.buttonStyle = buttonStyle;
        return this;
    }

    public PopupStyle withSelectedButtonStyle(ButtonStyle selectedButtonStyle) {
        this.selectedButtonStyle = selectedButtonStyle;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (background != null) doc.set("Background", background);
        if (buttonPadding != null) doc.set("ButtonPadding", buttonPadding.toBsonDocument());
        if (padding != null) doc.set("Padding", padding.toBsonDocument());
        if (tooltipStyle != null) doc.set("TooltipStyle", tooltipStyle.toBsonDocument());
        if (buttonStyle != null) doc.set("ButtonStyle", buttonStyle.toBsonDocument());
        if (selectedButtonStyle != null) doc.set("SelectedButtonStyle", selectedButtonStyle.toBsonDocument());
    }
}
