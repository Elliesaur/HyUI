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
 * CheckBoxStyleState type definition.
 */
public class CheckBoxStyleState implements HyUIBsonSerializable {
    private HyUIPatchStyle defaultBackground;
    private HyUIPatchStyle hoveredBackground;
    private HyUIPatchStyle pressedBackground;
    private HyUIPatchStyle disabledBackground;
    private SoundStyle changedSound;

    public CheckBoxStyleState withDefaultBackground(HyUIPatchStyle defaultBackground) {
        this.defaultBackground = defaultBackground;
        return this;
    }

    public CheckBoxStyleState withHoveredBackground(HyUIPatchStyle hoveredBackground) {
        this.hoveredBackground = hoveredBackground;
        return this;
    }

    public CheckBoxStyleState withPressedBackground(HyUIPatchStyle pressedBackground) {
        this.pressedBackground = pressedBackground;
        return this;
    }

    public CheckBoxStyleState withDisabledBackground(HyUIPatchStyle disabledBackground) {
        this.disabledBackground = disabledBackground;
        return this;
    }

    public CheckBoxStyleState withChangedSound(SoundStyle changedSound) {
        this.changedSound = changedSound;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (defaultBackground != null) doc.set("DefaultBackground", defaultBackground.toBsonDocument());
        if (hoveredBackground != null) doc.set("HoveredBackground", hoveredBackground.toBsonDocument());
        if (pressedBackground != null) doc.set("PressedBackground", pressedBackground.toBsonDocument());
        if (disabledBackground != null) doc.set("DisabledBackground", disabledBackground.toBsonDocument());
        if (changedSound != null) doc.set("ChangedSound", changedSound.toBsonDocument());
    }
}
