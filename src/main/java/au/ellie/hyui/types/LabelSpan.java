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
 * LabelSpan type definition.
 */
public class LabelSpan implements HyUIBsonSerializable {
    private String text;
    private Boolean isUppercase;
    private Boolean isBold;
    private Boolean isItalics;
    private Boolean isMonospace;
    private Boolean isUnderlined;
    private String color;
    private String outlineColor;
    private String link;

    public LabelSpan withText(String text) {
        this.text = text;
        return this;
    }

    public LabelSpan withUppercase(boolean uppercase) {
        isUppercase = uppercase;
        return this;
    }

    public LabelSpan withBold(boolean bold) {
        isBold = bold;
        return this;
    }

    public LabelSpan withItalics(boolean italics) {
        isItalics = italics;
        return this;
    }

    public LabelSpan withMonospace(boolean monospace) {
        isMonospace = monospace;
        return this;
    }

    public LabelSpan withUnderlined(boolean underlined) {
        isUnderlined = underlined;
        return this;
    }

    public LabelSpan withColor(String color) {
        this.color = color;
        return this;
    }

    public LabelSpan withOutlineColor(String outlineColor) {
        this.outlineColor = outlineColor;
        return this;
    }

    public LabelSpan withLink(String link) {
        this.link = link;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (text != null) doc.set("Text", text);
        if (isUppercase != null) doc.set("IsUppercase", isUppercase);
        if (isBold != null) doc.set("IsBold", isBold);
        if (isItalics != null) doc.set("IsItalics", isItalics);
        if (isMonospace != null) doc.set("IsMonospace", isMonospace);
        if (isUnderlined != null) doc.set("IsUnderlined", isUnderlined);
        if (color != null) doc.set("Color", color);
        if (outlineColor != null) doc.set("OutlineColor", outlineColor);
        if (link != null) doc.set("Link", link);
    }
}
