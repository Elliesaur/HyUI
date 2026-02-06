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
 * ButtonStyle type definition.
 */
public class ButtonStyle implements HyUIBsonSerializable {
    private ButtonStyleState def;
    private ButtonStyleState hovered;
    private ButtonStyleState pressed;
    private ButtonStyleState disabled;
    private ButtonSounds sounds;

    public ButtonStyle withDefault(ButtonStyleState def) {
        this.def = def;
        return this;
    }

    public ButtonStyle withHovered(ButtonStyleState hovered) {
        this.hovered = hovered;
        return this;
    }

    public ButtonStyle withPressed(ButtonStyleState pressed) {
        this.pressed = pressed;
        return this;
    }

    public ButtonStyle withDisabled(ButtonStyleState disabled) {
        this.disabled = disabled;
        return this;
    }

    public ButtonStyle withSounds(ButtonSounds sounds) {
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

    public static ButtonStyle primaryStyle() {
        return DefaultStyles.primaryButtonStyle();
    }

    public static ButtonStyle primarySquareStyle() {
        return DefaultStyles.primarySquareButtonStyle();
    }

    public static ButtonStyle destructiveStyle() {
        return DefaultStyles.destructiveButtonStyle();
    }

    public static ButtonStyle secondaryStyle() {
        return DefaultStyles.secondaryButtonStyle();
    }

    public static ButtonStyle tertiaryStyle() {
        return DefaultStyles.tertiaryButtonStyle();
    }
}
