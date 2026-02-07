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

package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventActions;
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.types.InputFieldStyle;
import au.ellie.hyui.types.SliderStyle;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for FloatSliderNumberField UI elements.
 */
public class FloatSliderNumberFieldBuilder extends UIElementBuilder<FloatSliderNumberFieldBuilder> {
    private HyUIAnchor numberFieldContainerAnchor;
    private Integer numberFieldMaxDecimalPlaces;
    private Float min;
    private Float max;
    private Float step;
    private Float value;

    public FloatSliderNumberFieldBuilder() {
        super(UIElements.FLOAT_SLIDER_NUMBER_FIELD, "#HyUIFloatSliderNumberField");
        withUiFile("Pages/Elements/FloatSliderNumberField.ui");
        withWrappingGroup(true);
    }

    public static FloatSliderNumberFieldBuilder floatSliderNumberField() {
        return new FloatSliderNumberFieldBuilder();
    }

    public FloatSliderNumberFieldBuilder withSliderStyle(SliderStyle style) {
        return withSecondaryStyle("SliderStyle", style);
    }

    public FloatSliderNumberFieldBuilder withNumberFieldStyle(InputFieldStyle style) {
        return withSecondaryStyle("NumberFieldStyle", style);
    }

    public FloatSliderNumberFieldBuilder withNumberFieldContainerAnchor(HyUIAnchor anchor) {
        this.numberFieldContainerAnchor = anchor;
        return this;
    }

    public FloatSliderNumberFieldBuilder withNumberFieldMaxDecimalPlaces(int maxDecimalPlaces) {
        this.numberFieldMaxDecimalPlaces = maxDecimalPlaces;
        return this;
    }

    public FloatSliderNumberFieldBuilder withMin(float min) {
        this.min = min;
        return this;
    }

    public FloatSliderNumberFieldBuilder withMax(float max) {
        this.max = max;
        return this;
    }

    public FloatSliderNumberFieldBuilder withStep(float step) {
        this.step = step;
        return this;
    }

    public FloatSliderNumberFieldBuilder withValue(float value) {
        this.value = value;
        this.initialValue = value;
        return this;
    }

    public FloatSliderNumberFieldBuilder addEventListener(CustomUIEventBindingType type, Consumer<Float> callback) {
        return addEventListener(type, Float.class, callback);
    }

    public FloatSliderNumberFieldBuilder addEventListener(CustomUIEventBindingType type, BiConsumer<Float, UIContext> callback) {
        return addEventListenerWithContext(type, Float.class, callback);
    }

    @Override
    protected void applyRuntimeValue(Object value) {
        if (value instanceof Number number) {
            Float next = number.floatValue();
            this.value = next;
            this.initialValue = next;
        }
    }

    @Override
    protected Object parseValue(String rawValue) {
        try {
            return Float.parseFloat(rawValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected boolean usesRefValue() {
        return true;
    }

    @Override
    protected boolean supportsStyling() {
        return false;
    }

    @Override
    protected boolean isStyleWhitelist() {
        return true;
    }

    @Override
    protected Set<String> getSupportedStyleProperties() {
        return StylePropertySets.merge(
                StylePropertySets.ANCHOR,
                StylePropertySets.PADDING,
                StylePropertySets.PATCH_STYLE,
                StylePropertySets.SOUND_STYLE,
                StylePropertySets.INPUT_FIELD_STYLE,
                Set.of(
                        "Background",
                        "Fill",
                        "Handle",
                        "HandleWidth",
                        "HandleHeight"
                )
        );
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (numberFieldContainerAnchor != null) {
            commands.setObject(selector + ".NumberFieldContainerAnchor", numberFieldContainerAnchor.toHytaleAnchor());
        }
        if (numberFieldMaxDecimalPlaces != null) {
            commands.set(selector + ".NumberFieldMaxDecimalPlaces", numberFieldMaxDecimalPlaces);
        }
        if (min != null) {
            commands.set(selector + ".Min", min);
        }
        if (max != null) {
            commands.set(selector + ".Max", max);
        }
        if (step != null) {
            commands.set(selector + ".Step", step);
        }
        if (value != null) {
            commands.set(selector + ".Value", value);
        }

        if (listeners.isEmpty()) {
            addEventListener(CustomUIEventBindingType.ValueChanged, (_, _) -> {});
        }
        listeners.forEach(listener -> {
            if (listener.type() == CustomUIEventBindingType.ValueChanged) {
                String eventId = getEffectiveId();
                HyUIPlugin.getLog().logFinest("Adding ValueChanged event binding for " + selector + " with eventId: " + eventId);
                events.addEventBinding(CustomUIEventBindingType.ValueChanged, selector,
                        EventData.of("@ValueFloat", selector + ".Value")
                                .append("Target", eventId)
                                .append("Action", UIEventActions.VALUE_CHANGED),
                        false);
            }
        });
    }
}
