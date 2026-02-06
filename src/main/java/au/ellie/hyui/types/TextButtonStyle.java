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
 * TextButtonStyle type definition.
 */
public class TextButtonStyle implements HyUIBsonSerializable {
    private TextButtonStyleState def;
    private TextButtonStyleState hovered;
    private TextButtonStyleState pressed;
    private TextButtonStyleState disabled;
    private ButtonSounds sounds;

    public TextButtonStyle withDefault(TextButtonStyleState def) {
        this.def = def;
        return this;
    }

    public TextButtonStyle withHovered(TextButtonStyleState hovered) {
        this.hovered = hovered;
        return this;
    }

    public TextButtonStyle withPressed(TextButtonStyleState pressed) {
        this.pressed = pressed;
        return this;
    }

    public TextButtonStyle withDisabled(TextButtonStyleState disabled) {
        this.disabled = disabled;
        return this;
    }

    public TextButtonStyle withSounds(ButtonSounds sounds) {
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

    public static TextButtonStyle primaryStyle() {
        return DefaultStyles.primaryTextButtonStyle();
    }

    public static TextButtonStyle destructiveStyle() {
        return DefaultStyles.destructiveTextButtonStyle();
    }

    public static TextButtonStyle secondaryStyle() {
        return DefaultStyles.secondaryTextButtonStyle();
    }

    public static TextButtonStyle smallSecondaryStyle() {
        return DefaultStyles.smallSecondaryTextButtonStyle();
    }

    public static TextButtonStyle tertiaryStyle() {
        return DefaultStyles.tertiaryTextButtonStyle();
    }
}
