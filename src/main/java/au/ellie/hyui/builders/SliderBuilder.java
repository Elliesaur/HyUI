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
import au.ellie.hyui.theme.Theme;
import au.ellie.hyui.utils.PropertyBatcher;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for creating slider UI elements.
 */
public class SliderBuilder extends UIElementBuilder<SliderBuilder> {
    private Integer min;
    private Integer max;
    private Integer step;
    private Integer value;
    private Boolean isReadOnly;

    public SliderBuilder() {
        super(UIElements.SLIDER, "#HyUISlider");
        withWrappingGroup(true);
        withUiFile("Pages/Elements/Slider.ui");
    }

    public SliderBuilder(Theme theme) {
        super(theme, UIElements.SLIDER, "#HyUISlider");
        withWrappingGroup(true);
        withUiFile("Pages/Elements/Slider.ui");
    }

    /**
     * Factory method to create a new instance of {@code SliderBuilder}.
     *
     * @return A new {@code SliderBuilder} instance.
     */
    public static SliderBuilder slider() {
        return new SliderBuilder();
    }

    /**
     * Factory method to create a new instance of {@code SliderBuilder} with the game theme.
     *
     * @return A new {@code SliderBuilder} instance with the game theme.
     */
    public static SliderBuilder gameSlider() {
        return new SliderBuilder(Theme.GAME_THEME);
    }

    public SliderBuilder withMin(int min) {
        this.min = min;
        return this;
    }

    public SliderBuilder withMax(int max) {
        this.max = max;
        return this;
    }

    public SliderBuilder withStep(int step) {
        this.step = step;
        return this;
    }
    
    public SliderBuilder withValue(int value) {
        this.value = value;
        return this;
    }

    public SliderBuilder withIsReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
        return this;
    }

    /**
     * Adds an event listener for the MouseButtonReleased event.
     */
    public SliderBuilder onMouseButtonReleased(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.MouseButtonReleased, Void.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the ValueChanged event.
     */
    public SliderBuilder onValueChanged(Consumer<Integer> callback) {
        return addEventListener(CustomUIEventBindingType.ValueChanged, Integer.class, callback);
    }

    /**
     * Adds an event listener for the ValueChanged event with context.
     */
    public SliderBuilder onValueChanged(BiConsumer<Integer, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.ValueChanged, Integer.class, callback);
    }

    /**
     * Adds an event listener to the slider builder. The only type it accepts will be ValueChanged.
     *
     * @param type     the type of the event to listen for, represented by {@code CustomUIEventBindingType}.
     *                 This defines the specific event binding, such as {@code ValueChanged}.
     * @param callback the function to execute when the specified event occurs. The callback receives
     *                 a {@code Float} value.
     * @return the current instance of {@code SliderBuilder}, enabling method chaining.
     */
    public SliderBuilder addEventListener(CustomUIEventBindingType type, Consumer<Integer> callback) {
        return addEventListener(type, Integer.class, callback);
    }

    /**
     * Adds an event listener to the slider builder with access to the UI context.
     *
     * @param type     The type of the event to bind the listener to.
     * @param callback The function to be executed when the specified event is triggered, with UI context.
     * @return This SliderBuilder instance for method chaining.
     */
    public SliderBuilder addEventListener(CustomUIEventBindingType type, BiConsumer<Integer, UIContext> callback) {
        return addEventListenerWithContext(type, Integer.class, callback);
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
    protected boolean supportsStyling() {
        return true;
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
    protected boolean usesRefValue() {
        return true;
    }
    
    @Override
    protected Object parseValue(String rawValue) {
        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

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
        if (isReadOnly != null) {
            HyUIPlugin.getLog().logFinest("Setting IsReadOnly: " + isReadOnly + " for " + selector);
            commands.set(selector + ".IsReadOnly", isReadOnly);
        }

        if ( hyUIStyle == null && typedStyle == null && style != null) {
            HyUIPlugin.getLog().logFinest("Setting Style for Slider " + selector);
            commands.set(selector + ".Style", style);
        } else if (hyUIStyle == null && typedStyle != null) {
            PropertyBatcher.endSet(selector + ".Style", filterStyleDocument(typedStyle.toBsonDocument()), commands);
        } else {
            HyUIPlugin.getLog().logFinest("Setting Style for Slider to DefaultSliderStyle " + selector);
            commands.set(selector + ".Style", Value.ref("Common.ui", "DefaultSliderStyle"));
        }
        if (listeners.isEmpty()) {
            // To handle data back to the .getValue, we need to add at least one listener.
            addEventListener(CustomUIEventBindingType.ValueChanged, (_, _) -> {});
        }

        // Register event listeners
        listeners.forEach(listener -> {
            String eventId = getEffectiveId();
            if (listener.type() == CustomUIEventBindingType.MouseButtonReleased) {
                HyUIPlugin.getLog().logFinest("Adding MouseButtonReleased event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.MouseButtonReleased, selector,
                        EventData.of("Action", UIEventActions.MOUSE_BUTTON_RELEASED)
                            .append("Target", eventId), false);
            } else if (listener.type() == CustomUIEventBindingType.ValueChanged) {
                HyUIPlugin.getLog().logFinest("Adding ValueChanged event binding for " + selector + " with eventId: " + eventId);
                events.addEventBinding(CustomUIEventBindingType.ValueChanged, selector,
                        EventData.of("@ValueInt", selector + ".Value")
                            .append("Target", eventId)
                            .append("Action", UIEventActions.VALUE_CHANGED), false);
            }
        });
    }
}
