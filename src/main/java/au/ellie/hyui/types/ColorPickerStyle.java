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
 * ColorPickerStyle type definition.
 */
public class ColorPickerStyle implements HyUIBsonSerializable {
    private String opacitySelectorBackground;
    private String buttonBackground;
    private String buttonFill;
    private InputFieldDecorationStyle textFieldDecoration;
    private HyUIPadding textFieldPadding;
    private Integer textFieldHeight;

    public ColorPickerStyle withOpacitySelectorBackground(String opacitySelectorBackground) {
        this.opacitySelectorBackground = opacitySelectorBackground;
        return this;
    }

    public ColorPickerStyle withButtonBackground(String buttonBackground) {
        this.buttonBackground = buttonBackground;
        return this;
    }

    public ColorPickerStyle withButtonFill(String buttonFill) {
        this.buttonFill = buttonFill;
        return this;
    }

    public ColorPickerStyle withTextFieldDecoration(InputFieldDecorationStyle textFieldDecoration) {
        this.textFieldDecoration = textFieldDecoration;
        return this;
    }

    public ColorPickerStyle withTextFieldPadding(HyUIPadding textFieldPadding) {
        this.textFieldPadding = textFieldPadding;
        return this;
    }

    public ColorPickerStyle withTextFieldHeight(int textFieldHeight) {
        this.textFieldHeight = textFieldHeight;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (opacitySelectorBackground != null) doc.set("OpacitySelectorBackground", opacitySelectorBackground);
        if (buttonBackground != null) doc.set("ButtonBackground", buttonBackground);
        if (buttonFill != null) doc.set("ButtonFill", buttonFill);
        if (textFieldDecoration != null) doc.set("TextFieldDecoration", textFieldDecoration.toBsonDocument());
        if (textFieldPadding != null) doc.set("TextFieldPadding", textFieldPadding.toBsonDocument());
        if (textFieldHeight != null) doc.set("TextFieldHeight", textFieldHeight);
    }

    public static ColorPickerStyle defaultStyle() {
        return DefaultStyles.defaultColorPickerStyle();
    }
}
