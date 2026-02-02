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
import au.ellie.hyui.elements.BackgroundSupported;
import au.ellie.hyui.elements.ScrollbarStyleSupported;
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.theme.Theme;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for creating text field UI elements. Also known as Text Input elements.
 */
public class TextFieldBuilder extends UIElementBuilder<TextFieldBuilder>
        implements BackgroundSupported<TextFieldBuilder>, ScrollbarStyleSupported<TextFieldBuilder> {
    private String value;
    private String placeholderText;
    private Integer maxLength;
    private Integer maxVisibleLines;
    private Boolean readOnly;
    private Boolean password;
    private String passwordChar;
    private Boolean autoGrow;
    private HyUIPatchStyle background;
    private String backgroundStyleReference;
    private String backgroundStyleDocument;
    private String scrollbarStyleReference;
    private String scrollbarStyleDocument;
    private HyUIPadding contentPadding;
    private boolean isMultiline;

    /**
     * DO NOT USE UNLESS YOU KNOW WHAT YOU ARE DOING.
     *
     * Not normally used, only used when creating a text field element from scratch.
     */
    public TextFieldBuilder() {
        super(UIElements.TEXT_FIELD, "#HyUITextField");
        withWrappingGroup(true);
    }

    /**
     * DO NOT USE UNLESS YOU KNOW WHAT YOU ARE DOING.
     *
     * Constructor for creating a text field element with a specified theme.
     * @param theme The theme to use for the text field element.
     */
    public TextFieldBuilder(Theme theme) {
        super(theme, UIElements.TEXT_FIELD, "#HyUITextField");
        withWrappingGroup(true);
        if (theme == Theme.GAME_THEME) {
            withUiFile("Pages/Elements/TextInput.ui");
        }
    }

    /**
     * DO NOT USE UNLESS YOU KNOW WHAT YOU ARE DOING.
     *
     * Constructor for creating a text field element with a specified theme and element path.
     * @param theme The theme to use for the text field element.
     * @param elementPath The path to the UI element definition file.
     */
    public TextFieldBuilder(Theme theme, String elementPath) {
        super(theme, elementPath, "#HyUITextField");
        withWrappingGroup(true);
        if (UIElements.MACRO_TEXT_FIELD.equals(elementPath)) {
            withUiFile("Pages/Elements/TextInput.ui");
        }
    }

    private TextFieldBuilder(Theme theme, String elementPath, String typeSelector) {
        super(theme, elementPath, typeSelector);
        withWrappingGroup(true);
    }

    /**
     * Creates a text input field with the game theme.
     * @return A new TextFieldBuilder instance configured for text input.
     */
    public static TextFieldBuilder textInput() {
        return new TextFieldBuilder(Theme.GAME_THEME, UIElements.MACRO_TEXT_FIELD);
    }

    /**
     * Creates a multiline text input field with the game theme.
     * @return A new TextFieldBuilder instance configured for multiline input.
     */
    public static TextFieldBuilder multilineTextField() {
        TextFieldBuilder builder = new TextFieldBuilder(Theme.GAME_THEME,
                UIElements.MULTILINE_TEXT_FIELD,
                "#HyUIMultilineTextField");
        builder.withUiFile("Pages/Elements/MultilineTextField.ui");
        builder.isMultiline = true;
        return builder;
    }

    /**
     * Sets the initial value of the text field.
     * @param value The initial value to set for the text field.
     * @return This TextFieldBuilder instance for method chaining.
     */
    public TextFieldBuilder withValue(String value) {
        this.value = value;
        this.initialValue = value;
        return this;
    }

    /**
     * Sets the placeholder text for the text field.
     * @param placeholderText The placeholder text to set.
     * @return This TextFieldBuilder instance for method chaining.
     */
    public TextFieldBuilder withPlaceholderText(String placeholderText) {
        this.placeholderText = placeholderText;
        return this;
    }

    /**
     * Sets the style for the placeholder text.
     * @param placeholderStyle The style reference for the placeholder text.
     * @return This TextFieldBuilder instance for method chaining.
     */
    public TextFieldBuilder withPlaceholderStyle(HyUIStyle placeholderStyle) {
        return withSecondaryStyle("PlaceholderStyle", placeholderStyle);
    }

    /**
     * Sets the maximum length of the text field.
     * @param maxLength The maximum length to set.
     * @return This TextFieldBuilder instance for method chaining.
     */
    public TextFieldBuilder withMaxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    /**
     * Sets the maximum visible lines for the text field.
     * @param maxVisibleLines The maximum number of visible lines to set.
     * @return This TextFieldBuilder instance for method chaining.
     */
    public TextFieldBuilder withMaxVisibleLines(int maxVisibleLines) {
        this.maxVisibleLines = maxVisibleLines;
        return this;
    }

    /**
     * Sets whether the text field is read-only.
     * @param readOnly Whether the field should be read-only.
     * @return This TextFieldBuilder instance for method chaining.
     */
    public TextFieldBuilder withReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    /**
     * Sets whether the text field is in password mode.
     * @param password Whether password mode should be enabled.
     * @return This TextFieldBuilder instance for method chaining.
     */
    public TextFieldBuilder withPassword(boolean password) {
        this.password = password;
        return this;
    }

    /**
     * Sets the character to display in password mode.
     * @param passwordChar The password character to set.
     * @return This TextFieldBuilder instance for method chaining.
     */
    public TextFieldBuilder withPasswordChar(String passwordChar) {
        this.passwordChar = passwordChar;
        return this;
    }

    /**
     * Sets whether the text field should automatically grow with content.
     * @param autoGrow Whether the field should automatically grow.
     * @return This TextFieldBuilder instance for method chaining.
     */
    public TextFieldBuilder withAutoGrow(boolean autoGrow) {
        this.autoGrow = autoGrow;
        return this;
    }

    /**
     * Sets the background patch style for the text field.
     * @param background The background patch style to use.
     * @return This TextFieldBuilder instance for method chaining.
     */
    @Override
    public TextFieldBuilder withBackground(HyUIPatchStyle background) {
        if (!isMultiline) return this;
        this.background = background;
        return this;
    }

    @Override
    public HyUIPatchStyle getBackground() {
        return this.background;
    }

    /**
     * Sets the background style reference for the text field.
     * @param styleReference The style reference (e.g., "InputBoxBackground").
     * @return This TextFieldBuilder instance for method chaining.
     */
    public TextFieldBuilder withBackground(String styleReference) {
        if (!isMultiline) return this;
        return withBackground("Common.ui", styleReference);
    }

    /**
     * Sets the background style reference for the text field from a document.
     * @param document The style document (e.g., "Common.ui").
     * @param styleReference The style reference (e.g., "InputBoxBackground").
     * @return This TextFieldBuilder instance for method chaining.
     */
    public TextFieldBuilder withBackground(String document, String styleReference) {
        if (!isMultiline) return this;
        this.backgroundStyleDocument = document;
        this.backgroundStyleReference = styleReference;
        return this;
    }

    /**
     * Sets the padding used for the text contents.
     * @param padding The content padding to apply.
     * @return This TextFieldBuilder instance for method chaining.
     */
    public TextFieldBuilder withContentPadding(HyUIPadding padding) {
        if (!isMultiline) return this;
        this.contentPadding = padding;
        return this;
    }

    @Override
    public TextFieldBuilder withScrollbarStyle(String document, String styleReference) {
        if (!isMultiline) return this;
        this.scrollbarStyleDocument = document;
        this.scrollbarStyleReference = styleReference;
        return this;
    }

    @Override
    public String getScrollbarStyleReference() {
        return scrollbarStyleReference;
    }

    @Override
    public String getScrollbarStyleDocument() {
        return scrollbarStyleDocument;
    }

    /**
     * Adds an event listener to the text field builder for handling a specific type of UI event.
     *
     * @param type The type of the event to bind the listener to. This specifies what kind of UI event 
     *             should trigger the provided callback.
     * @param callback The function to be executed when the specified event is triggered. The callback 
     *                 processes a string argument associated with the event.
     * @return This TextFieldBuilder instance for method chaining.
     */
    public TextFieldBuilder addEventListener(CustomUIEventBindingType type, Consumer<String> callback) {
        return addEventListener(type, String.class, callback);
    }

    /**
     * Adds an event listener to the text field builder with access to the UI context.
     *
     * @param type     The type of the event to bind the listener to.
     * @param callback The function to be executed when the specified event is triggered, with UI context.
     * @return This TextFieldBuilder instance for method chaining.
     */
    public TextFieldBuilder addEventListener(CustomUIEventBindingType type, BiConsumer<String, UIContext> callback) {
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
            HyUIPlugin.getLog().logFinest("Setting Value: " + value + " for " + selector);
            commands.set(selector + ".Value", value);
        }

        if (placeholderText != null) {
            commands.set(selector + ".PlaceholderText", placeholderText);
        }
        
        if (maxLength != null) {
            commands.set(selector + ".MaxLength", maxLength);
        }

        if (maxVisibleLines != null) {
            commands.set(selector + ".MaxVisibleLines", maxVisibleLines);
        }

        if (readOnly != null) {
            commands.set(selector + ".ReadOnly", readOnly);
        }

        if (password != null) {
            commands.set(selector + ".Password", password);
        }

        if (passwordChar != null) {
            commands.set(selector + ".PasswordChar", passwordChar);
        }

        if (autoGrow != null) {
            commands.set(selector + ".AutoGrow", autoGrow);
        }

        if (backgroundStyleReference != null && backgroundStyleDocument != null) {
            commands.set(selector + ".Background", Value.ref(backgroundStyleDocument, backgroundStyleReference));
        } else {
            applyBackground(commands, selector);
        }

        applyScrollbarStyle(commands, selector);

        if (contentPadding != null) {
            if (contentPadding.getLeft() != null) commands.set(selector + ".ContentPadding.Left", contentPadding.getLeft());
            if (contentPadding.getTop() != null) commands.set(selector + ".ContentPadding.Top", contentPadding.getTop());
            if (contentPadding.getRight() != null) commands.set(selector + ".ContentPadding.Right", contentPadding.getRight());
            if (contentPadding.getBottom() != null) commands.set(selector + ".ContentPadding.Bottom", contentPadding.getBottom());
        }

        if (hyUIStyle == null && style != null) {
            HyUIPlugin.getLog().logFinest("Setting Style: " + style + " for " + selector);
            commands.set(selector + ".Style", style);
        }
        if (listeners.isEmpty()) {
            // To handle data back to the .getValue, we need to add at least one listener.
            addEventListener(CustomUIEventBindingType.ValueChanged, (_, _) -> {});
            if (!isMultiline) {
                addEventListener(CustomUIEventBindingType.FocusLost, (_, _) -> {});
                addEventListener(CustomUIEventBindingType.FocusGained, (_, _) -> {});
                // Causes target element in custom UI event binding is not marked as bindable.
                addEventListener(CustomUIEventBindingType.Validating, (_, _) -> {});
            }
        }
        listeners.forEach(listener -> {
            if (listener.type() == CustomUIEventBindingType.ValueChanged ||
                    listener.type() == CustomUIEventBindingType.FocusLost ||
                    listener.type() == CustomUIEventBindingType.Validating ||
                    listener.type() == CustomUIEventBindingType.FocusGained) {
                String eventId = getEffectiveId();
                HyUIPlugin.getLog().logFinest("Adding " + listener.type() + " event binding for " + selector + " with eventId: " + eventId);
                events.addEventBinding(listener.type(), selector,
                        EventData.of("@Value", selector + ".Value")
                                .append("Target", eventId)
                                .append("Action", listener.type().name()),
                        false);
            }
        });
    }
}
