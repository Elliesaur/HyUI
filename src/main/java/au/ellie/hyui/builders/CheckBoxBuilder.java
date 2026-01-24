package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventActions;
import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for creating checkbox UI elements with label.
 */
public class CheckBoxBuilder extends UIElementBuilder<CheckBoxBuilder> {
    private Boolean value;
    private String text;

    /**
     * Constructs a new instance of {@code CheckBoxBuilder}.
     * The builder is pre-configured to create a checkbox UI element with a label, 
     * wrapped within a grouping element.
     */
    public CheckBoxBuilder() {
        super(UIElements.CHECK_BOX_WITH_LABEL, "#HyUICheckBox");
        withWrappingGroup(true);
        withUiFile("Pages/Elements/CheckBox.ui");
        this.initialValue = false;
    }

    /**
     * Sets the value state for the checkbox being constructed.
     *
     * @param value the value to set for the checkbox; {@code true} indicates checked, and {@code false} indicates unchecked
     * @return the {@code CheckBoxBuilder} for method chaining
     */
    public CheckBoxBuilder withValue(boolean value) {
        this.value = value;
        this.initialValue = value;
        return this;
    }

    /**
     * Sets the style to be applied when the checkbox is in the checked state.
     *
     * @param checkedStyle the {@code HyUIStyle} to apply when checked
     * @return the {@code CheckBoxBuilder} for method chaining
     */
    public CheckBoxBuilder withCheckedStyle(HyUIStyle checkedStyle) {
        return withSecondaryStyle("CheckedStyle", checkedStyle);
    }

    /**
     * Sets the style to be applied when the checkbox is in the unchecked state.
     *
     * @param uncheckedStyle the {@code HyUIStyle} to apply when unchecked
     * @return the {@code CheckBoxBuilder} for method chaining
     */
    public CheckBoxBuilder withUncheckedStyle(HyUIStyle uncheckedStyle) {
        return withSecondaryStyle("UncheckedStyle", uncheckedStyle);
    }

    // TODO: Work out how I can set a variable within a variable given the Label for a checkbox with label has 
    //       no ID to work with.
    /*public CheckBoxBuilder withText(String text) {
        this.text = text;
        return this;
    }*/

    /**
     * Registers an event listener for the checkbox being constructed. The listener is triggered 
     * based on the specified event type and executes the provided callback when the event occurs.
     *
     * @param type     the type of UI event to bind the listener to, such as value changes or activation
     * @param callback the callback function to execute when the event is triggered, receiving the event value as a parameter
     * @return the {@code CheckBoxBuilder} instance for method chaining, allowing further configuration
     */
    public CheckBoxBuilder addEventListener(CustomUIEventBindingType type, Consumer<Boolean> callback) {
        return addEventListener(type, Boolean.class, callback);
    }

    /**
     * Adds an event listener to the checkbox builder with access to the UI context.
     *
     * @param type     The type of the event to bind the listener to.
     * @param callback The function to be executed when the specified event is triggered, with UI context.
     * @return This CheckBoxBuilder instance for method chaining.
     */
    public CheckBoxBuilder addEventListener(CustomUIEventBindingType type, BiConsumer<Boolean, UIContext> callback) {
        return addEventListenerWithContext(type, Boolean.class, callback);
    }

    @Override
    protected Object parseValue(String rawValue) {
        return Boolean.parseBoolean(rawValue);
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
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (value != null) {
            // For CheckBoxWithLabel, the actual CheckBox is a child
            HyUIPlugin.getLog().logInfo("Setting Value: " + value + " for " + selector + " #CheckBox");
            commands.set(selector + " #CheckBox.Value", value);
        }

        /*if (text != null) {
            HyUIPlugin.getInstance().logInfo("Setting Text: " + text + " for " + selector);
            commands.set(selector + " Label.Text", text);
        }*/

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
                HyUIPlugin.getLog().logInfo("Adding ValueChanged event binding for " + selector + " #CheckBox with eventId: " + eventId);
                events.addEventBinding(CustomUIEventBindingType.ValueChanged, selector + " #CheckBox", 
                        EventData.of("@ValueBool", selector + " #CheckBox.Value")
                            .append("Target", eventId)
                            .append("Action", UIEventActions.VALUE_CHANGED),
                        false);
            }
        });
    }
}
