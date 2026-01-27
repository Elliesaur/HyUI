package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventActions;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for creating custom text buttons and square buttons with inline styles.
 */
public class CustomButtonBuilder extends UIElementBuilder<CustomButtonBuilder>
        implements LayoutModeSupported<CustomButtonBuilder> {
    private static final String CUSTOM_TEXT_BUTTON_SELECTOR = "#HyUICustomTextButton";
    private static final String CUSTOM_BUTTON_SELECTOR = "#HyUICustomButton";

    private enum ButtonType {
        TEXT_BUTTON,
        BUTTON
    }

    private final ButtonType buttonType;
    private String text;
    private String layoutMode;
    private Boolean disabled;
    private Boolean overscroll;

    private HyUIPatchStyle defaultBackground;
    private HyUIPatchStyle hoveredBackground;
    private HyUIPatchStyle pressedBackground;
    private HyUIPatchStyle disabledBackground;

    private HyUIStyle defaultLabelStyle;
    private HyUIStyle hoveredLabelStyle;
    private HyUIStyle pressedLabelStyle;
    private HyUIStyle disabledLabelStyle;

    private CustomButtonBuilder(ButtonType buttonType) {
        super(buttonType == ButtonType.TEXT_BUTTON ? UIElements.CUSTOM_TEXT_BUTTON : UIElements.CUSTOM_BUTTON,
                buttonType == ButtonType.TEXT_BUTTON ? CUSTOM_TEXT_BUTTON_SELECTOR : CUSTOM_BUTTON_SELECTOR);
        this.buttonType = buttonType;
        withWrappingGroup(true);
        initDefaults();
    }

    public static CustomButtonBuilder customTextButton() {
        return new CustomButtonBuilder(ButtonType.TEXT_BUTTON);
    }

    public static CustomButtonBuilder customButton() {
        return new CustomButtonBuilder(ButtonType.BUTTON);
    }

    public CustomButtonBuilder withText(String text) {
        if (buttonType == ButtonType.TEXT_BUTTON) {
            this.text = text;
        }
        return this;
    }

    public CustomButtonBuilder withDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public CustomButtonBuilder withOverscroll(boolean overscroll) {
        this.overscroll = overscroll;
        return this;
    }

    @Override
    public CustomButtonBuilder withLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    @Override
    public String getLayoutMode() {
        return this.layoutMode;
    }

    public CustomButtonBuilder withDefaultBackground(HyUIPatchStyle background) {
        this.defaultBackground = background;
        return this;
    }

    public CustomButtonBuilder withHoveredBackground(HyUIPatchStyle background) {
        this.hoveredBackground = background;
        return this;
    }

    public CustomButtonBuilder withPressedBackground(HyUIPatchStyle background) {
        this.pressedBackground = background;
        return this;
    }

    public CustomButtonBuilder withDisabledBackground(HyUIPatchStyle background) {
        this.disabledBackground = background;
        return this;
    }

    public CustomButtonBuilder withDefaultLabelStyle(HyUIStyle style) {
        this.defaultLabelStyle = style;
        return this;
    }

    public CustomButtonBuilder withHoveredLabelStyle(HyUIStyle style) {
        this.hoveredLabelStyle = style;
        return this;
    }

    public CustomButtonBuilder withPressedLabelStyle(HyUIStyle style) {
        this.pressedLabelStyle = style;
        return this;
    }

    public CustomButtonBuilder withDisabledLabelStyle(HyUIStyle style) {
        this.disabledLabelStyle = style;
        return this;
    }

    public CustomButtonBuilder addEventListener(CustomUIEventBindingType type, Consumer<Void> callback) {
        return addEventListener(type, Void.class, callback);
    }

    public CustomButtonBuilder addEventListener(CustomUIEventBindingType type, BiConsumer<Void, UIContext> callback) {
        return addEventListenerWithContext(type, Void.class, callback);
    }

    @Override
    protected boolean supportsStyling() {
        return false;
    }

    @Override
    protected boolean hasCustomInlineContent() {
        return true;
    }

    @Override
    protected String generateCustomInlineContent() {
        StringBuilder sb = new StringBuilder();
        if (buttonType == ButtonType.TEXT_BUTTON) {
            sb.append("TextButton ").append(CUSTOM_TEXT_BUTTON_SELECTOR).append(" { ");
            sb.append("Style: (");
            appendTextButtonState(sb, "Default", defaultBackground, defaultLabelStyle,
                    "../Common/Buttons/Primary.png");
            sb.append(", ");
            appendTextButtonState(sb, "Hovered", hoveredBackground, hoveredLabelStyle,
                    "../Common/Buttons/Primary_Hovered.png");
            sb.append(", ");
            appendTextButtonState(sb, "Pressed", pressedBackground, pressedLabelStyle,
                    "../Common/Buttons/Primary_Pressed.png");
            sb.append(", ");
            appendTextButtonState(sb, "Disabled", disabledBackground, disabledLabelStyle,
                    "../Common/Buttons/Disabled.png");
            sb.append(", ");
            appendButtonSounds(sb);
            sb.append("); ");
            sb.append("Anchor: (Height: 44); ");
            sb.append("Padding: (Horizontal: 24); ");
            String inlineText = text != null ? text : "TEXT_VALUE_HERE";
            sb.append("Text: \"").append(escapeText(inlineText)).append("\"; ");
            sb.append("}");
        } else {
            sb.append("Button ").append(CUSTOM_BUTTON_SELECTOR).append(" { ");
            sb.append("Style: (");
            appendButtonState(sb, "Default", defaultBackground, "Common/Buttons/Primary_Square.png");
            sb.append(", ");
            appendButtonState(sb, "Hovered", hoveredBackground, "Common/Buttons/Primary_Square_Hovered.png");
            sb.append(", ");
            appendButtonState(sb, "Pressed", pressedBackground, "Common/Buttons/Primary_Square_Pressed.png");
            sb.append(", ");
            appendButtonState(sb, "Disabled", disabledBackground, "Common/Buttons/Disabled.png");
            sb.append(", ");
            appendButtonSounds(sb);
            sb.append("); ");
            sb.append("Anchor: (Height: 44, Width: 44); ");
            sb.append("Padding: (Horizontal: 24); ");
            sb.append("}");
        }
        var ret = sb.toString();
        return ret;
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (buttonType == ButtonType.BUTTON) {
            applyLayoutMode(commands, selector);
        }

        if (buttonType == ButtonType.TEXT_BUTTON && text != null) {
            HyUIPlugin.getLog().logInfo("Setting Text: " + text + " for " + selector);
            commands.set(selector + ".Text", text);
        }

        if (disabled != null) {
            HyUIPlugin.getLog().logInfo("Setting Disabled: " + disabled + " for " + selector);
            commands.set(selector + ".Disabled", disabled);
        }

        if (overscroll != null && buttonType == ButtonType.TEXT_BUTTON) {
            HyUIPlugin.getLog().logInfo("Setting Overscroll: " + overscroll + " for " + selector);
            commands.set(selector + ".Overscroll", overscroll);
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

    private void initDefaults() {
        if (buttonType == ButtonType.TEXT_BUTTON) {
            defaultBackground = createTextBackground("Common/Buttons/Primary.png");
            hoveredBackground = createTextBackground("Common/Buttons/Primary_Hovered.png");
            pressedBackground = createTextBackground("Common/Buttons/Primary_Pressed.png");
            disabledBackground = createTextBackground("Common/Buttons/Disabled.png");

            defaultLabelStyle = createDefaultLabelStyle();
            hoveredLabelStyle = createDefaultLabelStyle();
            pressedLabelStyle = createDefaultLabelStyle();
            disabledLabelStyle = createDefaultLabelStyle();
        } else {
            defaultBackground = createButtonBackground("Common/Buttons/Primary_Square.png");
            hoveredBackground = createButtonBackground("Common/Buttons/Primary_Square_Hovered.png");
            pressedBackground = createButtonBackground("Common/Buttons/Primary_Square_Pressed.png");
            disabledBackground = createButtonBackground("Common/Buttons/Disabled.png");
        }
    }

    private HyUIPatchStyle createTextBackground(String texturePath) {
        return new HyUIPatchStyle()
                .setTexturePath(texturePath)
                .setVerticalBorder(12)
                .setHorizontalBorder(80);
    }

    private HyUIPatchStyle createButtonBackground(String texturePath) {
        return new HyUIPatchStyle()
                .setTexturePath(texturePath)
                .setBorder(12);
    }

    private HyUIStyle createDefaultLabelStyle() {
        return new HyUIStyle()
                .setFontSize(17)
                .setTextColor("#bfcdd5")
                .setRenderBold(true)
                .setRenderUppercase(true)
                .setHorizontalAlignment(HyUIStyle.Alignment.Center)
                .setVerticalAlignment(HyUIStyle.Alignment.Center);
    }

    private void appendTextButtonState(StringBuilder sb, String stateName, HyUIPatchStyle background, HyUIStyle labelStyle,
                                       String defaultTexturePath) {
        // Hmm, honor defaults? Merge? Currently merge.
        HyUIPatchStyle bg = background != null ? background : createTextBackground(defaultTexturePath);
        HyUIStyle ls = labelStyle != null ? labelStyle : createDefaultLabelStyle();
        sb.append(stateName).append(": (Background: ").append(bg)
                .append(", LabelStyle: ").append(ls.toLabelStyle()).append(")");
    }

    private void appendButtonState(StringBuilder sb, String stateName, HyUIPatchStyle background, String defaultTexturePath) {
        HyUIPatchStyle bg = background != null ? background : createButtonBackground(defaultTexturePath);
        sb.append(stateName).append(": (Background: ").append(bg).append(")");
    }

    private void appendButtonSounds(StringBuilder sb) {
        sb.append("Sounds: (");
        sb.append("Activate: (SoundPath: \"Sounds/ButtonsLightActivate.ogg\", MinPitch: -0.4, MaxPitch: 0.4, Volume: 4), ");
        sb.append("MouseHover: (SoundPath: \"Sounds/ButtonsLightHover.ogg\", Volume: 6)");
        sb.append(")");
    }

    private String escapeText(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
