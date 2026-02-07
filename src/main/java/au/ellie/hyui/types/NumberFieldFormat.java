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
 * NumberFieldFormat type definition.
 */
public class NumberFieldFormat implements HyUIBsonSerializable {
    private Integer maxDecimalPlaces;
    private Float step;
    private Float minValue;
    private Float maxValue;

    public NumberFieldFormat withMaxDecimalPlaces(int maxDecimalPlaces) {
        this.maxDecimalPlaces = maxDecimalPlaces;
        return this;
    }

    public NumberFieldFormat withStep(float step) {
        this.step = step;
        return this;
    }

    public NumberFieldFormat withMinValue(float minValue) {
        this.minValue = minValue;
        return this;
    }

    public NumberFieldFormat withMaxValue(float maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (maxDecimalPlaces != null) doc.set("MaxDecimalPlaces", maxDecimalPlaces);
        if (step != null) doc.set("Step", step.doubleValue());
        if (minValue != null) doc.set("MinValue", minValue.doubleValue());
        if (maxValue != null) doc.set("MaxValue", maxValue.doubleValue());
    }
}
