package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventActions;
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.theme.Theme;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

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
    protected boolean supportsStyling() {
        return true;
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

        if (hyUIStyle == null && style != null) {
            HyUIPlugin.getLog().logInfo("Setting Style for Slider " + selector);
            commands.set(selector + ".Style", style);
        } else {
            HyUIPlugin.getLog().logInfo("Setting Style for Slider to DefaultSliderStyle " + selector);
            commands.set(selector + ".Style", Value.ref("Common.ui", "DefaultSliderStyle"));
        }
        if (listeners.isEmpty()) {
            // To handle data back to the .getValue, we need to add at least one listener.
            addEventListener(CustomUIEventBindingType.ValueChanged, (_, _) -> {});
        }
        listeners.forEach(listener -> {
            if (listener.type() == CustomUIEventBindingType.ValueChanged) {
                String eventId = getEffectiveId();
                HyUIPlugin.getLog().logInfo("Adding ValueChanged event binding for " + selector + " with eventId: " + eventId);
                events.addEventBinding(CustomUIEventBindingType.ValueChanged, selector,
                        EventData.of("@ValueInt", selector + ".Value")
                            .append("Target", eventId)
                            .append("Action", UIEventActions.VALUE_CHANGED),
                        false);
            }
        });
    }
}
