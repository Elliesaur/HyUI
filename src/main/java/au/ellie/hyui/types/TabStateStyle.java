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
import au.ellie.hyui.builders.HyUIStyle;
import au.ellie.hyui.utils.BsonDocumentHelper;

/**
 * TabStateStyle type definition.
 */
public class TabStateStyle implements HyUIBsonSerializable {
    private HyUIStyle labelStyle;
    private HyUIPadding padding;
    private HyUIPatchStyle background;
    private HyUIPatchStyle overlay;
    private HyUIAnchor iconAnchor;
    private HyUIAnchor anchor;
    private TextTooltipStyle tooltipStyle;
    private Float iconOpacity;
    private String contentMaskTexturePath;

    public TabStateStyle withLabelStyle(HyUIStyle labelStyle) {
        this.labelStyle = labelStyle;
        return this;
    }

    public TabStateStyle withPadding(HyUIPadding padding) {
        this.padding = padding;
        return this;
    }

    public TabStateStyle withBackground(HyUIPatchStyle background) {
        this.background = background;
        return this;
    }

    public TabStateStyle withOverlay(HyUIPatchStyle overlay) {
        this.overlay = overlay;
        return this;
    }

    public TabStateStyle withIconAnchor(HyUIAnchor iconAnchor) {
        this.iconAnchor = iconAnchor;
        return this;
    }

    public TabStateStyle withAnchor(HyUIAnchor anchor) {
        this.anchor = anchor;
        return this;
    }

    public TabStateStyle withTooltipStyle(TextTooltipStyle tooltipStyle) {
        this.tooltipStyle = tooltipStyle;
        return this;
    }

    public TabStateStyle withIconOpacity(float iconOpacity) {
        this.iconOpacity = iconOpacity;
        return this;
    }

    public TabStateStyle withContentMaskTexturePath(String contentMaskTexturePath) {
        this.contentMaskTexturePath = contentMaskTexturePath;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (labelStyle != null) doc.set("LabelStyle", labelStyle.toBsonDocument());
        if (padding != null) doc.set("Padding", padding.toBsonDocument());
        if (background != null) doc.set("Background", background.toBsonDocument());
        if (overlay != null) doc.set("Overlay", overlay.toBsonDocument());
        if (iconAnchor != null) doc.set("IconAnchor", iconAnchor.toBsonDocument());
        if (anchor != null) doc.set("Anchor", anchor.toBsonDocument());
        if (tooltipStyle != null) doc.set("TooltipStyle", tooltipStyle.toBsonDocument());
        if (iconOpacity != null) doc.set("IconOpacity", iconOpacity.doubleValue());
        if (contentMaskTexturePath != null) doc.set("ContentMaskTexturePath", contentMaskTexturePath);
    }
}
