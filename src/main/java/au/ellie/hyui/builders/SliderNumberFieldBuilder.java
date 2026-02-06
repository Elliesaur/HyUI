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
 * Builder for SliderNumberField UI elements.
 */
public class SliderNumberFieldBuilder extends UIElementBuilder<SliderNumberFieldBuilder> {
    private HyUIAnchor numberFieldContainerAnchor;
    private Integer min;
    private Integer max;
    private Integer step;
    private Integer value;

    public SliderNumberFieldBuilder() {
        super(UIElements.SLIDER_NUMBER_FIELD, "#HyUISliderNumberField");
        withUiFile("Pages/Elements/SliderNumberField.ui");
        withWrappingGroup(true);
    }

    public static SliderNumberFieldBuilder sliderNumberField() {
        return new SliderNumberFieldBuilder();
    }

    public SliderNumberFieldBuilder withSliderStyle(SliderStyle style) {
        return withSecondaryStyle("SliderStyle", style);
    }

    public SliderNumberFieldBuilder withNumberFieldStyle(InputFieldStyle style) {
        return withSecondaryStyle("NumberFieldStyle", style);
    }

    public SliderNumberFieldBuilder withNumberFieldContainerAnchor(HyUIAnchor anchor) {
        this.numberFieldContainerAnchor = anchor;
        return this;
    }

    public SliderNumberFieldBuilder withMin(int min) {
        this.min = min;
        return this;
    }

    public SliderNumberFieldBuilder withMax(int max) {
        this.max = max;
        return this;
    }

    public SliderNumberFieldBuilder withStep(int step) {
        this.step = step;
        return this;
    }

    public SliderNumberFieldBuilder withValue(int value) {
        this.value = value;
        this.initialValue = value;
        return this;
    }

    public SliderNumberFieldBuilder addEventListener(CustomUIEventBindingType type, Consumer<Double> callback) {
        return addEventListener(type, Double.class, callback);
    }

    public SliderNumberFieldBuilder addEventListener(CustomUIEventBindingType type, BiConsumer<Double, UIContext> callback) {
        return addEventListenerWithContext(type, Double.class, callback);
    }

    @Override
    protected void applyRuntimeValue(Object value) {
        if (value instanceof Number number) {
            Integer next = number.intValue();
            this.value = next;
            this.initialValue = next;
        }
    }

    @Override
    protected Object parseValue(String rawValue) {
        try {
            return Double.parseDouble(rawValue);
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
        return true;
    }

    @Override
    protected boolean isStyleWhitelist() {
        return true;
    }

    @Override
    protected Set<String> getSupportedStyleProperties() {
        return Set.of(
                "TextColor",
                "FontSize",
                "RenderBold",
                "RenderItalics",
                "RenderUppercase"
        );
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (numberFieldContainerAnchor != null) {
            commands.setObject(selector + ".NumberFieldContainerAnchor", numberFieldContainerAnchor.toHytaleAnchor());
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
                        EventData.of("@ValueDouble", selector + ".Value")
                                .append("Target", eventId)
                                .append("Action", UIEventActions.VALUE_CHANGED),
                        false);
            }
        });
    }
}
