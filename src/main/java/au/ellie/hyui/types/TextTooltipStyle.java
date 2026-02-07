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
 * TextTooltipStyle type definition.
 */
public class TextTooltipStyle implements HyUIBsonSerializable {
    private HyUIPatchStyle background;
    private Integer maxWidth;
    private HyUIStyle labelStyle;
    private Integer padding;
    private String alignment;

    public TextTooltipStyle withBackground(HyUIPatchStyle background) {
        this.background = background;
        return this;
    }

    public TextTooltipStyle withMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public TextTooltipStyle withLabelStyle(HyUIStyle labelStyle) {
        this.labelStyle = labelStyle;
        return this;
    }

    public TextTooltipStyle withPadding(int padding) {
        this.padding = padding;
        return this;
    }

    public TextTooltipStyle withAlignment(String alignment) {
        this.alignment = alignment;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (background != null) doc.set("Background", background.toBsonDocument());
        if (maxWidth != null) doc.set("MaxWidth", maxWidth);
        if (labelStyle != null) doc.set("LabelStyle", labelStyle.toBsonDocument());
        if (padding != null) doc.set("Padding", padding);
        if (alignment != null) doc.set("Alignment", alignment);
    }

    public static TextTooltipStyle buttonTextTooltipStyle() {
        return DefaultStyles.buttonTextTooltipStyle();
    }
}
