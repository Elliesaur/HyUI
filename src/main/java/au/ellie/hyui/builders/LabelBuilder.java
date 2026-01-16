package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.theme.Theme;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

/**
 * Builder for creating label UI elements. 
 * Labels are used to display text or other static content.
 */
public class LabelBuilder extends UIElementBuilder<LabelBuilder> {
    private String text;

    /**
     * Constructs a new instance of {@code LabelBuilder} for creating label UI elements.
     * This class is used to define and customize labels which are used to display text 
     * or other static content in the user interface.
     *
     * By default, the label element type is set to {@code UIElements.LABEL}.
     */
    public LabelBuilder() {
        super(UIElements.LABEL);
    }

    public LabelBuilder(Theme theme) {
        super(theme, UIElements.LABEL);
    }

    /**
     * Factory method to create a new instance of {@code LabelBuilder}.
     *
     * @return A new {@code LabelBuilder} instance for creating and customizing labels.
     */
    public static LabelBuilder label() {
        return new LabelBuilder();
    }

    /**
     * Sets the text to be displayed by the label being built.
     *
     * @param text The text content to set for the label. This will replace any
     *             previously set text value.
     *
     * @return The current instance of the {@code LabelBuilder} for method chaining.
     */
    public LabelBuilder withText(String text) {
        this.text = text;
        return this;
    }

    @Override
    protected boolean supportsStyling() {
        return true;
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (text != null) {
            HyUIPlugin.getInstance().logInfo("Setting Text: " + text + " for " + selector);
            commands.set(selector + ".Text", text);
        }

        if (hyUIStyle == null && style != null) {
            HyUIPlugin.getInstance().logInfo("Setting Raw Style: " + style + " for " + selector);
            commands.set(selector + ".Style", style);
        }
    }
}
