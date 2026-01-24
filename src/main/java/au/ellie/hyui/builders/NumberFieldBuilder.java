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
 * A builder class for constructing a number input field UI element. This class extends 
 * the UIElementBuilder to provide functionality specific to creating and customizing 
 * number input fields.
 *
 * The NumberFieldBuilder supports setting the initial numeric value, attaching event 
 * listeners, and integrating with specific themes and styles. It facilitates the seamless 
 * generation of commands and events during the UI build phase.
 */
public class NumberFieldBuilder extends UIElementBuilder<NumberFieldBuilder> {
    private Double value;
    private String format;
    private Double maxDecimalPlaces;
    private Double minValue;
    private Double maxValue;
    private Double step;

    /**
     * Do not use. Instead, use the static .numberInput().
     * @param theme
     */
    public NumberFieldBuilder(Theme theme) {
        super(theme, UIElements.MACRO_NUMBER_FIELD, "#HyUINumberField");
        withWrappingGroup(false);
        this.initialValue = 0.0;
    }

    /**
     * Creates a new instance of {@code NumberFieldBuilder} configured for the game theme.
     *
     * @return a new {@code NumberFieldBuilder} instance for defining and customizing a number input field.
     */
    public static NumberFieldBuilder numberInput() {
        return new NumberFieldBuilder(Theme.GAME_THEME);
    }

    /**
     * Sets the value for the number input field.
     *
     * @param value the numeric value to be set for the number input field
     * @return the current instance of {@code NumberFieldBuilder} for method chaining
     */
    public NumberFieldBuilder withValue(double value) {
        this.value = value;
        this.initialValue = value;
        return this;
    }

    /**
     * Sets the format string for the number field.
     *
     * @param format the number format string
     * @return the current instance of {@code NumberFieldBuilder} for method chaining
     */
    public NumberFieldBuilder withFormat(String format) {
        this.format = format;
        return this;
    }

    /**
     * Sets the maximum number of decimal places for the number field.
     *
     * @param maxDecimalPlaces the maximum decimal places
     * @return the current instance of {@code NumberFieldBuilder} for method chaining
     */
    public NumberFieldBuilder withMaxDecimalPlaces(double maxDecimalPlaces) {
        this.maxDecimalPlaces = maxDecimalPlaces;
        return this;
    }

    /**
     * Sets the minimum allowed value for the number field.
     *
     * @param minValue the minimum value
     * @return the current instance of {@code NumberFieldBuilder} for method chaining
     */
    public NumberFieldBuilder withMinValue(double minValue) {
        this.minValue = minValue;
        return this;
    }

    /**
     * Sets the maximum allowed value for the number field.
     *
     * @param maxValue the maximum value
     * @return the current instance of {@code NumberFieldBuilder} for method chaining
     */
    public NumberFieldBuilder withMaxValue(double maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    /**
     * Sets the step value for the number field.
     *
     * @param step the step value
     * @return the current instance of {@code NumberFieldBuilder} for method chaining
     */
    public NumberFieldBuilder withStep(double step) {
        this.step = step;
        return this;
    }

    /**
     * Sets the style for the number field.
     *
     * @param style the style reference for the number field
     * @return the current instance of {@code NumberFieldBuilder} for method chaining
     */
    public NumberFieldBuilder withNumberFieldStyle(HyUIStyle style) {
        return withSecondaryStyle("NumberFieldStyle", style);
    }

    /**
     * Adds an event listener to the number field builder. The only type it accepts will be ValueChanged.
     *
     * @param type     the type of the event to listen for, represented by {@code CustomUIEventBindingType}. 
     *                 This defines the specific event binding, such as {@code ValueChanged}.
     * @param callback the function to execute when the specified event occurs. The callback receives 
     *                 a {@code Double} value, which typically represents the current numeric value 
     *                 associated with the event.
     * @return the current instance of {@code NumberFieldBuilder}, enabling method chaining.
     */
    public NumberFieldBuilder addEventListener(CustomUIEventBindingType type, Consumer<Double> callback) {
        return addEventListener(type, Double.class, callback);
    }

    /**
     * Adds an event listener to the number field builder with access to the UI context.
     *
     * @param type     The type of the event to bind the listener to.
     * @param callback The function to be executed when the specified event is triggered, with UI context.
     * @return This NumberFieldBuilder instance for method chaining.
     */
    public NumberFieldBuilder addEventListener(CustomUIEventBindingType type, BiConsumer<Double, UIContext> callback) {
        return addEventListenerWithContext(type, Double.class, callback);
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
    protected boolean hasCustomInlineContent() {
        return true;
    }

    @Override
    protected String generateCustomInlineContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("NumberField #" + getEffectiveId() + " { ");
        if (maxDecimalPlaces != null || step != null || minValue != null || maxValue != null) {
            sb.append("Format: (");
            boolean needsComma = false;
            if (maxDecimalPlaces != null) {
                sb.append("MaxDecimalPlaces: ").append(maxDecimalPlaces);
                needsComma = true;
            }
            if (step != null) {
                if (needsComma) sb.append(", ");
                sb.append("Step: ").append(step);
                needsComma = true;
            }
            if (minValue != null) {
                if (needsComma) sb.append(", ");
                sb.append("MinValue: ").append(minValue);
                needsComma = true;
            }
            if (maxValue != null) {
                if (needsComma) sb.append(", ");
                sb.append("MaxValue: ").append(maxValue);
            }
            sb.append("); ");
        }
        sb.append("Padding: (Horizontal: 10); }");
        return sb.toString();
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
        } else if (hyUIStyle == null) {
            commands.set(selector + ".Style", Value.ref("Common.ui", "DefaultInputFieldStyle"));
        }

        if (!secondaryStyles.containsKey("PlaceholderStyle")) {
            commands.set(selector + ".PlaceholderStyle", Value.ref("Common.ui", "DefaultInputFieldPlaceholderStyle"));
        }
        
        commands.set(selector + ".Background", Value.ref("Common.ui", "InputBoxBackground"));

        if (anchor == null || anchor.getHeight() < 38) {
            if (anchor == null) {
                anchor = new HyUIAnchor();
            }
            anchor.setHeight(38);
            // Need to force anchor setting.
            commands.setObject(selector + ".Anchor", anchor.toHytaleAnchor());
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
                        EventData.of("@ValueDouble", selector + ".Value")
                            .append("Target", eventId)
                            .append("Action", UIEventActions.VALUE_CHANGED), 
                        false);
            }
        });
    }
}
