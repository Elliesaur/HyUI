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
import au.ellie.hyui.utils.BsonDocumentHelper;

/**
 * MenuItemStyle type definition.
 */
public class MenuItemStyle implements HyUIBsonSerializable {
    private HyUIPatchStyle def;
    private HyUIPatchStyle hovered;

    public MenuItemStyle withDefault(HyUIPatchStyle def) {
        this.def = def;
        return this;
    }

    public MenuItemStyle withHovered(HyUIPatchStyle hovered) {
        this.hovered = hovered;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (def != null) doc.set("Default", def.toBsonDocument());
        if (hovered != null) doc.set("Hovered", hovered.toBsonDocument());
    }
}
