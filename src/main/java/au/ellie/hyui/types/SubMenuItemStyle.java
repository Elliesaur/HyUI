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
 * SubMenuItemStyle type definition.
 */
public class SubMenuItemStyle implements HyUIBsonSerializable {
    private SubMenuItemStyleState def;
    private SubMenuItemStyleState hovered;
    private SubMenuItemStyleState pressed;
    private SubMenuItemStyleState disabled;
    private ButtonSounds sounds;

    public SubMenuItemStyle withDefault(SubMenuItemStyleState def) {
        this.def = def;
        return this;
    }

    public SubMenuItemStyle withHovered(SubMenuItemStyleState hovered) {
        this.hovered = hovered;
        return this;
    }

    public SubMenuItemStyle withPressed(SubMenuItemStyleState pressed) {
        this.pressed = pressed;
        return this;
    }

    public SubMenuItemStyle withDisabled(SubMenuItemStyleState disabled) {
        this.disabled = disabled;
        return this;
    }

    public SubMenuItemStyle withSounds(ButtonSounds sounds) {
        this.sounds = sounds;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (def != null) doc.set("Default", def.toBsonDocument());
        if (hovered != null) doc.set("Hovered", hovered.toBsonDocument());
        if (pressed != null) doc.set("Pressed", pressed.toBsonDocument());
        if (disabled != null) doc.set("Disabled", disabled.toBsonDocument());
        if (sounds != null) doc.set("Sounds", sounds.toBsonDocument());
    }
}
