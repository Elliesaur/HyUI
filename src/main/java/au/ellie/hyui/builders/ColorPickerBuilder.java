package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventActions;
import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for creating ColorPicker UI element.
 */
public class ColorPickerBuilder extends UIElementBuilder<ColorPickerBuilder> {
    private String value;

    /**
     * Constructs a new instance of {@code ColorPickerBuilder}, initializing it with
     * default configuration for creating a ColorPicker UI element.
     *
     * The builder is pre-configured with:
     * - The element type defined by {@code UIElements.COLOR_PICKER}.
     * - A wrapping group setting enabled.
     */
    public ColorPickerBuilder() {
        super(UIElements.COLOR_PICKER, "#HyUIColorPicker");
        withWrappingGroup(true);
        withUiFile("Pages/Elements/ColorPicker.ui");
        this.initialValue = "";
    }

    /**
     * Sets the value for the ColorPicker UI element in hexadecimal color code format.
     *
     * @param hexColor the hexadecimal color code value to set, represented as a string.
     *                 For example, "#FFFFFF" for white or "#000000" for black. You
     *                 may add the alpha channel to the end like "#FFFFFF(0.5)".
     * @return the {@code ColorPickerBuilder} instance with the updated value,
     *         allowing for method chaining.
     */
    public ColorPickerBuilder withValue(String hexColor) {
        this.value = hexColor;
        this.initialValue = hexColor;
        return this;
    }

    /**
     * Adds an event listener to the ColorPicker UI element for handling specific types of events.
     * The specified callback will be invoked when the event of the provided type occurs.
     *
     * @param type the type of event to listen for, represented by {@code CustomUIEventBindingType}.
     *             This determines which event the callback will respond to.
     * @param callback a {@code Consumer<String>} function to handle the event.
     *                 It will be triggered with the event data when the event occurs.
     * @return the {@code ColorPickerBuilder} instance with the event listener added,
     *         allowing for method chaining.
     */
    public ColorPickerBuilder addEventListener(CustomUIEventBindingType type, Consumer<String> callback) {
        return addEventListener(type, String.class, callback);
    }

    /**
     * Adds an event listener to the ColorPicker UI element with access to the UI context.
     *
     * @param type     The type of the event to bind the listener to.
     * @param callback The function to be executed when the specified event is triggered, with UI context.
     * @return This ColorPickerBuilder instance for method chaining.
     */
    public ColorPickerBuilder addEventListener(CustomUIEventBindingType type, BiConsumer<String, UIContext> callback) {
        return addEventListenerWithContext(type, String.class, callback);
    }

    @Override
    protected void applyRuntimeValue(Object value) {
        if (value != null) {
            String next = String.valueOf(value);
            this.value = next;
            this.initialValue = next;
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
    protected Set<String> getUnsupportedStyleProperties() {
        return Set.of("TextColor");
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (value != null) {
            HyUIPlugin.getLog().logInfo("Setting Value: " + value + " for " + selector);
            commands.set(selector + ".Value", value);
        }

        if (hyUIStyle == null && style != null) {
            HyUIPlugin.getLog().logInfo("Setting Style: " + style + " for " + selector);
            commands.set(selector + ".Style", style);
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
                        EventData.of("@Value", selector + ".Value")
                            .append("Target", eventId)
                            .append("Action", UIEventActions.VALUE_CHANGED), 
                        false);
            }
        });
    }
}
