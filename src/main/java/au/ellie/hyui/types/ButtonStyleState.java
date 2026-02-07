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
 * ButtonStyleState type definition.
 */
public class ButtonStyleState implements HyUIBsonSerializable {
    private HyUIPatchStyle background;
    private HyUIStyle labelStyle;

    public ButtonStyleState withBackground(HyUIPatchStyle background) {
        this.background = background;
        return this;
    }

    public ButtonStyleState withLabelStyle(HyUIStyle labelStyle) {
        this.labelStyle = labelStyle;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (background != null) doc.set("Background", background.toBsonDocument());
        if (labelStyle != null) doc.set("LabelStyle", labelStyle.toBsonDocument());
    }
}
