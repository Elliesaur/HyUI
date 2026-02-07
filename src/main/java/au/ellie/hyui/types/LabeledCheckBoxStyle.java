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
 * LabeledCheckBoxStyle type definition.
 */
public class LabeledCheckBoxStyle implements HyUIBsonSerializable {
    private LabeledCheckBoxStyleState checked;
    private LabeledCheckBoxStyleState unchecked;

    public LabeledCheckBoxStyle withChecked(LabeledCheckBoxStyleState checked) {
        this.checked = checked;
        return this;
    }

    public LabeledCheckBoxStyle withUnchecked(LabeledCheckBoxStyleState unchecked) {
        this.unchecked = unchecked;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (checked != null) doc.set("Checked", checked.toBsonDocument());
        if (unchecked != null) doc.set("Unchecked", unchecked.toBsonDocument());
    }
}
