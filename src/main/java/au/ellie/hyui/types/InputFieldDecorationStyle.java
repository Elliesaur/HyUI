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
 * InputFieldDecorationStyle type definition.
 */
public class InputFieldDecorationStyle implements HyUIBsonSerializable {
    private InputFieldDecorationStyleState def;
    private InputFieldDecorationStyleState focused;

    public InputFieldDecorationStyle withDefault(InputFieldDecorationStyleState def) {
        this.def = def;
        return this;
    }

    public InputFieldDecorationStyle withFocused(InputFieldDecorationStyleState focused) {
        this.focused = focused;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (def != null) doc.set("Default", def.toBsonDocument());
        if (focused != null) doc.set("Focused", focused.toBsonDocument());
    }
}
