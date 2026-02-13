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
import au.ellie.hyui.builders.HyUIPatchStyle;
import au.ellie.hyui.utils.BsonDocumentHelper;

/**
 * Native tab entry for TabNavigation.
 */
public class NativeTab implements HyUIBsonSerializable {
    private String id;
    private HyUIPatchStyle icon;
    private HyUIPatchStyle iconSelected;
    private HyUIAnchor iconAnchor;
    private String text;
    private String tooltipText;

    public NativeTab withId(String id) {
        this.id = id;
        return this;
    }

    public NativeTab withIcon(HyUIPatchStyle icon) {
        this.icon = icon;
        return this;
    }

    public NativeTab withIcon(String texturePath) {
        if (texturePath != null) {
            this.icon = new HyUIPatchStyle().setTexturePath(texturePath);
        }
        return this;
    }

    public NativeTab withIconSelected(HyUIPatchStyle iconSelected) {
        this.iconSelected = iconSelected;
        return this;
    }

    public NativeTab withIconSelected(String texturePath) {
        if (texturePath != null) {
            this.iconSelected = new HyUIPatchStyle().setTexturePath(texturePath);
        }
        return this;
    }

    public NativeTab withIconAnchor(HyUIAnchor iconAnchor) {
        this.iconAnchor = iconAnchor;
        return this;
    }

    public NativeTab withText(String text) {
        this.text = text;
        return this;
    }

    public NativeTab withTooltipText(String tooltipText) {
        this.tooltipText = tooltipText;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (id != null) doc.set("Id", id);
        if (icon != null) doc.set("Icon", icon.toBsonDocument());
        if (iconSelected != null) doc.set("IconSelected", iconSelected.toBsonDocument());
        if (iconAnchor != null) doc.set("IconAnchor", iconAnchor.toBsonDocument());
        if (text != null) doc.set("Text", text);
        if (tooltipText != null) doc.set("TooltipText", tooltipText);
    }
}
