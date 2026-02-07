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

import au.ellie.hyui.utils.BsonDocumentHelper;

/**
 * InputFieldStyle type definition.
 */
public class InputFieldStyle implements HyUIBsonSerializable {
    private String textColor;
    private Integer fontSize;
    private Boolean renderBold;
    private Boolean renderItalics;
    private Boolean renderUppercase;

    public InputFieldStyle withTextColor(String textColor) {
        this.textColor = textColor;
        return this;
    }

    public InputFieldStyle withFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public InputFieldStyle withFontSize(Integer fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public InputFieldStyle withRenderBold(boolean renderBold) {
        this.renderBold = renderBold;
        return this;
    }

    public InputFieldStyle withRenderBold(Boolean renderBold) {
        this.renderBold = renderBold;
        return this;
    }

    public InputFieldStyle withRenderItalics(boolean renderItalics) {
        this.renderItalics = renderItalics;
        return this;
    }

    public InputFieldStyle withRenderItalics(Boolean renderItalics) {
        this.renderItalics = renderItalics;
        return this;
    }

    public InputFieldStyle withRenderUppercase(boolean renderUppercase) {
        this.renderUppercase = renderUppercase;
        return this;
    }

    public InputFieldStyle withRenderUppercase(Boolean renderUppercase) {
        this.renderUppercase = renderUppercase;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (textColor != null) doc.set("TextColor", textColor);
        if (fontSize != null) doc.set("FontSize", fontSize);
        if (renderBold != null) doc.set("RenderBold", renderBold);
        if (renderItalics != null) doc.set("RenderItalics", renderItalics);
        if (renderUppercase != null) doc.set("RenderUppercase", renderUppercase);
    }

    public static InputFieldStyle defaultStyle() {
        return DefaultStyles.defaultInputFieldStyle();
    }

    public static InputFieldStyle defaultPlaceholderStyle() {
        return DefaultStyles.defaultInputFieldPlaceholderStyle();
    }
}
