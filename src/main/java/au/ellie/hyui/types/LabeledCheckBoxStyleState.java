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
 * LabeledCheckBoxStyleState type definition.
 */
public class LabeledCheckBoxStyleState implements HyUIBsonSerializable {
    private HyUIPatchStyle defaultBackground;
    private HyUIPatchStyle hoveredBackground;
    private HyUIPatchStyle pressedBackground;
    private String text;
    private HyUIStyle defaultLabelStyle;
    private HyUIStyle hoveredLabelStyle;
    private HyUIStyle pressedLabelStyle;
    private SoundStyle changedSound;

    public LabeledCheckBoxStyleState withDefaultBackground(HyUIPatchStyle defaultBackground) {
        this.defaultBackground = defaultBackground;
        return this;
    }

    public LabeledCheckBoxStyleState withHoveredBackground(HyUIPatchStyle hoveredBackground) {
        this.hoveredBackground = hoveredBackground;
        return this;
    }

    public LabeledCheckBoxStyleState withPressedBackground(HyUIPatchStyle pressedBackground) {
        this.pressedBackground = pressedBackground;
        return this;
    }

    public LabeledCheckBoxStyleState withText(String text) {
        this.text = text;
        return this;
    }

    public LabeledCheckBoxStyleState withDefaultLabelStyle(HyUIStyle defaultLabelStyle) {
        this.defaultLabelStyle = defaultLabelStyle;
        return this;
    }

    public LabeledCheckBoxStyleState withHoveredLabelStyle(HyUIStyle hoveredLabelStyle) {
        this.hoveredLabelStyle = hoveredLabelStyle;
        return this;
    }

    public LabeledCheckBoxStyleState withPressedLabelStyle(HyUIStyle pressedLabelStyle) {
        this.pressedLabelStyle = pressedLabelStyle;
        return this;
    }

    public LabeledCheckBoxStyleState withChangedSound(SoundStyle changedSound) {
        this.changedSound = changedSound;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (defaultBackground != null) doc.set("DefaultBackground", defaultBackground.toBsonDocument());
        if (hoveredBackground != null) doc.set("HoveredBackground", hoveredBackground.toBsonDocument());
        if (pressedBackground != null) doc.set("PressedBackground", pressedBackground.toBsonDocument());
        if (text != null) doc.set("Text", text);
        if (defaultLabelStyle != null) doc.set("DefaultLabelStyle", defaultLabelStyle.toBsonDocument());
        if (hoveredLabelStyle != null) doc.set("HoveredLabelStyle", hoveredLabelStyle.toBsonDocument());
        if (pressedLabelStyle != null) doc.set("PressedLabelStyle", pressedLabelStyle.toBsonDocument());
        if (changedSound != null) doc.set("ChangedSound", changedSound.toBsonDocument());
    }
}
