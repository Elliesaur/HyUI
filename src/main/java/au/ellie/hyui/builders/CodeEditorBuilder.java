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
import au.ellie.hyui.elements.ScrollbarStyleSupported;
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventActions;
import au.ellie.hyui.theme.Theme;
import au.ellie.hyui.types.InputFieldDecorationStyle;
import au.ellie.hyui.types.InputFieldStyle;
import au.ellie.hyui.types.ScrollbarStyle;
import au.ellie.hyui.utils.BsonDocumentHelper;
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
 * Builder for creating code editor UI elements.
 */
public class CodeEditorBuilder extends UIElementBuilder<CodeEditorBuilder>
        implements ScrollbarStyleSupported<CodeEditorBuilder> {
    private String value;
    private String placeholderText;
    private Integer maxLength;
    private Integer maxLines;
    private Integer maxVisibleLines;
    private Boolean readOnly;
    private Boolean autoGrow;
    private Boolean autoFocus;
    private Boolean autoSelectAll;
    private String language;
    private Integer lineNumberWidth;
    private Integer lineNumberPadding;
    private String lineNumberTextColor;
    private HyUIPatchStyle lineNumberBackground;
    private String lineNumberBackgroundValue;
    private String lineNumberBackgroundDocument;
    private String lineNumberBackgroundReference;
    private HyUIPadding contentPadding;
    private InputFieldDecorationStyle decoration;
    private String scrollbarStyleReference;
    private String scrollbarStyleDocument;
    private ScrollbarStyle scrollbarStyle;

    public CodeEditorBuilder() {
        super(Theme.GAME_THEME, UIElements.CODE_EDITOR, "#HyUICodeEditor");
        withWrappingGroup(true);
        withUiFile("Pages/Elements/CodeEditor.ui");
    }

    public static CodeEditorBuilder codeEditor() {
        return new CodeEditorBuilder();
    }

    public CodeEditorBuilder withValue(String value) {
        this.value = value;
        this.initialValue = value;
        return this;
    }

    public CodeEditorBuilder withPlaceholderText(String placeholderText) {
        this.placeholderText = placeholderText;
        return this;
    }

    @Deprecated(forRemoval = true)
    public CodeEditorBuilder withPlaceholderStyle(HyUIStyle placeholderStyle) {
        if (placeholderStyle == null) {
            return this;
        }
        InputFieldStyle mapped = new InputFieldStyle()
                .withTextColor(placeholderStyle.getTextColor())
                .withFontSize(placeholderStyle.getFontSize() != null ? placeholderStyle.getFontSize().intValue() : null)
                .withRenderBold(placeholderStyle.getRenderBold())
                .withRenderItalics(placeholderStyle.getRenderItalics())
                .withRenderUppercase(placeholderStyle.getRenderUppercase());
        return withPlaceholderStyle(mapped);
    }

    public CodeEditorBuilder withPlaceholderStyle(InputFieldStyle placeholderStyle) {
        return withSecondaryStyle("PlaceholderStyle", placeholderStyle);
    }

    public CodeEditorBuilder withDecoration(InputFieldDecorationStyle decoration) {
        this.decoration = decoration;
        return this;
    }

    public CodeEditorBuilder withMaxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public CodeEditorBuilder withMaxLines(int maxLines) {
        this.maxLines = maxLines;
        return this;
    }

    public CodeEditorBuilder withMaxVisibleLines(int maxVisibleLines) {
        this.maxVisibleLines = maxVisibleLines;
        return this;
    }

    public CodeEditorBuilder withReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    public CodeEditorBuilder withAutoGrow(boolean autoGrow) {
        this.autoGrow = autoGrow;
        return this;
    }

    public CodeEditorBuilder withAutoFocus(boolean autoFocus) {
        this.autoFocus = autoFocus;
        return this;
    }

    public CodeEditorBuilder withAutoSelectAll(boolean autoSelectAll) {
        this.autoSelectAll = autoSelectAll;
        return this;
    }

    public CodeEditorBuilder withLanguage(String language) {
        this.language = language;
        return this;
    }

    public CodeEditorBuilder withLineNumberWidth(int lineNumberWidth) {
        this.lineNumberWidth = lineNumberWidth;
        return this;
    }

    public CodeEditorBuilder withLineNumberBackground(HyUIPatchStyle background) {
        this.lineNumberBackground = background;
        this.lineNumberBackgroundDocument = null;
        this.lineNumberBackgroundReference = null;
        this.lineNumberBackgroundValue = null;
        return this;
    }

    public CodeEditorBuilder withLineNumberBackground(String value) {
        this.lineNumberBackgroundValue = value;
        this.lineNumberBackground = null;
        this.lineNumberBackgroundDocument = null;
        this.lineNumberBackgroundReference = null;
        return this;
    }

    public CodeEditorBuilder withLineNumberBackground(String document, String styleReference) {
        this.lineNumberBackgroundDocument = document;
        this.lineNumberBackgroundReference = styleReference;
        this.lineNumberBackground = null;
        this.lineNumberBackgroundValue = null;
        return this;
    }

    public CodeEditorBuilder withLineNumberTextColor(String lineNumberTextColor) {
        this.lineNumberTextColor = lineNumberTextColor;
        return this;
    }

    public CodeEditorBuilder withLineNumberPadding(int lineNumberPadding) {
        this.lineNumberPadding = lineNumberPadding;
        return this;
    }

    public CodeEditorBuilder withContentPadding(HyUIPadding padding) {
        this.contentPadding = padding;
        return this;
    }

    @Override
    public CodeEditorBuilder withScrollbarStyle(String document, String styleReference) {
        this.scrollbarStyleDocument = document;
        this.scrollbarStyleReference = styleReference;
        this.scrollbarStyle = null;
        return this;
    }

    @Override
    public CodeEditorBuilder withScrollbarStyle(ScrollbarStyle style) {
        this.scrollbarStyle = style;
        this.scrollbarStyleDocument = null;
        this.scrollbarStyleReference = null;
        return this;
    }

    @Override
    public String getScrollbarStyleReference() {
        return scrollbarStyleReference;
    }

    @Override
    public ScrollbarStyle getScrollbarStyle() {
        return scrollbarStyle;
    }

    @Override
    public String getScrollbarStyleDocument() {
        return scrollbarStyleDocument;
    }

    public CodeEditorBuilder addEventListener(CustomUIEventBindingType type, Consumer<String> callback) {
        return addEventListener(type, String.class, callback);
    }

    public CodeEditorBuilder addEventListener(CustomUIEventBindingType type, BiConsumer<String, UIContext> callback) {
        return addEventListenerWithContext(type, String.class, callback);
    }

    /**
     * Adds an event listener for the ValueChanged event.
     */
    public CodeEditorBuilder onValueChanged(Consumer<String> callback) {
        return addEventListener(CustomUIEventBindingType.ValueChanged, String.class, callback);
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
    protected boolean isStyleWhitelist() {
        return true;
    }

    @Override
    protected Set<String> getSupportedStyleProperties() {
        return StylePropertySets.merge(
                StylePropertySets.ANCHOR,
                StylePropertySets.PADDING,
                StylePropertySets.PATCH_STYLE,
                StylePropertySets.INPUT_FIELD_STYLE,
                StylePropertySets.INPUT_FIELD_ICON,
                StylePropertySets.INPUT_FIELD_BUTTON,
                StylePropertySets.INPUT_FIELD_DECORATION_STATE
        );
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

        if (maxLines != null) {
            commands.set(selector + ".MaxLines", maxLines);
        }

        if (maxVisibleLines != null) {
            commands.set(selector + ".MaxVisibleLines", maxVisibleLines);
        }

        if (readOnly != null) {
            commands.set(selector + ".IsReadOnly", readOnly);
            commands.set(selector + ".ReadOnly", readOnly);
        }

        if (autoGrow != null) {
            commands.set(selector + ".AutoGrow", autoGrow);
        }

        if (autoFocus != null) {
            commands.set(selector + ".AutoFocus", autoFocus);
        }

        if (autoSelectAll != null) {
            commands.set(selector + ".AutoSelectAll", autoSelectAll);
        }

        if (language != null) {
            commands.set(selector + ".Language", language);
        }

        if (lineNumberWidth != null) {
            commands.set(selector + ".LineNumberWidth", lineNumberWidth);
        }

        if (lineNumberPadding != null) {
            commands.set(selector + ".LineNumberPadding", lineNumberPadding);
        }

        if (lineNumberTextColor != null) {
            commands.set(selector + ".LineNumberTextColor", lineNumberTextColor);
        }

        if (lineNumberBackground != null) {
            commands.setObject(selector + ".LineNumberBackground", lineNumberBackground.getHytalePatchStyle());
        } else if (lineNumberBackgroundDocument != null && lineNumberBackgroundReference != null) {
            commands.set(selector + ".LineNumberBackground", Value.ref(lineNumberBackgroundDocument, lineNumberBackgroundReference));
        } else if (lineNumberBackgroundValue != null) {
            commands.set(selector + ".LineNumberBackground", lineNumberBackgroundValue);
        }

        if (decoration != null) {
            BsonDocumentHelper decorationDoc = PropertyBatcher.beginSet();
            decoration.applyTo(decorationDoc);
            filterStyleDocument(decorationDoc.getDocument());
            PropertyBatcher.endSet(selector + ".Decoration", decorationDoc, commands);
        }

        if (contentPadding != null) {
            if (contentPadding.getLeft() != null) commands.set(selector + ".ContentPadding.Left", contentPadding.getLeft());
            if (contentPadding.getTop() != null) commands.set(selector + ".ContentPadding.Top", contentPadding.getTop());
            if (contentPadding.getRight() != null) commands.set(selector + ".ContentPadding.Right", contentPadding.getRight());
            if (contentPadding.getBottom() != null) commands.set(selector + ".ContentPadding.Bottom", contentPadding.getBottom());
        }

        if (hyUIStyle == null && typedStyle == null && style != null) {
            HyUIPlugin.getLog().logFinest("Setting Style: " + style + " for " + selector);
            commands.set(selector + ".Style", style);
        } else if (hyUIStyle == null && typedStyle != null) {
            PropertyBatcher.endSet(selector + ".Style", filterStyleDocument(typedStyle.toBsonDocument()), commands);
        }

        applyScrollbarStyle(commands, selector);

        if (listeners.isEmpty()) {
            addEventListener(CustomUIEventBindingType.ValueChanged, (_, _) -> {});
        }

        listeners.forEach(listener -> {
            if (listener.type() == CustomUIEventBindingType.ValueChanged) {
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
