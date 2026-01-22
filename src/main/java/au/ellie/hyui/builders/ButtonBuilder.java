package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.elements.BackgroundSupported;
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.events.UIEventActions;
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.theme.Theme;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for creating button UI elements. 
 * Buttons are interactive elements that can trigger actions when clicked.
 */
public class ButtonBuilder extends UIElementBuilder<ButtonBuilder> implements 
        LayoutModeSupported<ButtonBuilder>,
        BackgroundSupported<ButtonBuilder> {
    private String text;
    private String layoutMode;
    private HyUIPatchStyle background;

    /**
     * You do not need to call this.
     */
    public ButtonBuilder() {
        super(UIElements.BUTTON, UIElements.BUTTON);
        withWrappingGroup(true);
    }
    
    /**
     * You do not need to call this.
     */
    public ButtonBuilder(Theme theme) {
        super(theme, UIElements.BUTTON, UIElements.BUTTON);
        withWrappingGroup(true);
        if (theme == Theme.GAME_THEME) {
            withUiFile("Pages/Elements/TextButton.ui");
        } else if (theme == Theme.RAW) {
            withUiFile("Pages/Elements/Button.ui");
        }
    }

    /**
     * You do not need to call this.
     * 
     * @param theme
     * @param elementPath
     */
    public ButtonBuilder(Theme theme, String elementPath) {
        super(theme, elementPath, getButtonTypeSelector(elementPath));
        withWrappingGroup(true);
        if (UIElements.TEXT_BUTTON.equals(elementPath)) {
            withUiFile("Pages/Elements/TextButton.ui");
        } else if (UIElements.CANCEL_TEXT_BUTTON.equals(elementPath)) {
            withUiFile("Pages/Elements/CancelTextButton.ui");
        } else if (UIElements.BACK_BUTTON.equals(elementPath)) {
            withUiFile("Pages/Elements/BackButton.ui");
        } else if (UIElements.BUTTON.equals(elementPath)) {
            withUiFile("Pages/Elements/Button.ui");
        }
    }

    private static String getButtonTypeSelector(String elementPath) {
        if (elementPath.contains("CancelTextButton")) return "#HyUICancelTextButton";
        if (elementPath.contains("TextButton")) return "#HyUITextButton";
        return "#HyUIButton";
    }

    /**
     * Creates a ButtonBuilder instance for a text-based button styled with the GAME_THEME and the TEXT_BUTTON element.
     *
     * @return a ButtonBuilder configured for creating a text button with predefined theme and style.
     */
    public static ButtonBuilder textButton() {
        return new ButtonBuilder(Theme.GAME_THEME, UIElements.TEXT_BUTTON);
    }

    /**
     * Creates a ButtonBuilder instance for a cancel text button styled with the GAME_THEME and 
     * the CANCEL_TEXT_BUTTON element.
     *
     * @return a ButtonBuilder configured for creating a cancel text button with predefined theme and style.
     */
    public static ButtonBuilder cancelTextButton() {
        return new ButtonBuilder(Theme.GAME_THEME, UIElements.CANCEL_TEXT_BUTTON);
    }

    public static ButtonBuilder backButton() {
        return new ButtonBuilder(Theme.GAME_THEME, UIElements.BACK_BUTTON);
    }

    public static ButtonBuilder rawButton() {
        return new ButtonBuilder(Theme.GAME_THEME, UIElements.BUTTON);
    }
    
    /**
     * Sets the text for the button being built. Replaces any other text.
     *
     * @param text the text to be displayed on the button
     * @return the current instance of ButtonBuilder for method chaining
     */
    public ButtonBuilder withText(String text) {
        // Not supported in back buttons.
        if (elementPath.contains("BackButton"))
            return this;
        this.text = text;
        return this;
    }

    /**
     * Adds an item icon to the button.
     *
     * @param itemIcon the ItemIconBuilder to add.
     * @return the current instance of ButtonBuilder for method chaining.
     */
    public ButtonBuilder withItemIcon(ItemIconBuilder itemIcon) {
        return addChild(itemIcon);
    }

    @Override
    public ButtonBuilder withLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    @Override
    public String getLayoutMode() {
        return this.layoutMode;
    }

    @Override
    public ButtonBuilder withBackground(HyUIPatchStyle background) {
        this.background = background;
        return this;
    }

    @Override
    public HyUIPatchStyle getBackground() {
        return this.background;
    }

    /**
     * Adds an event listener to this button. This allows the button to respond to specific UI events
     * that are triggered during interaction.
     *
     * @param type the type of UI event to listen for, specified as a {@link CustomUIEventBindingType}
     * @param callback a callback function to handle the event, expressed as a {@link Consumer<Void>}
     * @return the current instance of {@code ButtonBuilder} for method chaining
     */
    public ButtonBuilder addEventListener(CustomUIEventBindingType type, Consumer<Void> callback) {
        return addEventListener(type, Void.class, callback);
    }

    /**
     * Adds an event listener to this button with access to the UI context.
     *
     * @param type the type of UI event to listen for, specified as a {@link CustomUIEventBindingType}
     * @param callback a callback function to handle the event, expressed as a {@link BiConsumer<Void, UIContext>}
     * @return the current instance of {@code ButtonBuilder} for method chaining
     */
    public ButtonBuilder addEventListener(CustomUIEventBindingType type, BiConsumer<Void, UIContext> callback) {
        return addEventListenerWithContext(type, Void.class, callback);
    }

    @Override
    protected boolean supportsStyling() {
        return true;
    }

    @Override
    protected Set<String> getUnsupportedStyleProperties() {
        if (this.theme == Theme.GAME_THEME) {
            return Set.of("FontSize", "TextColor", "Alignment", "HorizontalAlignment", "VerticalAlignment");
        }
        return Collections.emptySet();
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        applyLayoutMode(commands, selector);
        applyBackground(commands, selector);

        if (text != null) {
            HyUIPlugin.getLog().logInfo("Setting Text: " + text + " for " + selector);
            commands.set(selector + ".Text", text);
        }

        if (hyUIStyle == null && style != null) {
            HyUIPlugin.getLog().logInfo("Setting Style: " + style + " for " + selector);
            commands.set(selector + ".Style", style);
        }

        listeners.forEach(listener -> {
            if (listener.type() == CustomUIEventBindingType.Activating) {
                String eventId = getEffectiveId();
                HyUIPlugin.getLog().logInfo("Adding Activating event binding: " + eventId + " for " + selector);
                events.addEventBinding(CustomUIEventBindingType.Activating, selector, 
                        EventData.of("Action", UIEventActions.BUTTON_CLICKED)
                            .append("Target", eventId), 
                        false);
            }
        });
    }
}
