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
import au.ellie.hyui.elements.BackgroundSupported;
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.theme.Theme;
import au.ellie.hyui.types.LabelSpan;
import au.ellie.hyui.utils.PropertyBatcher;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Builder for creating label UI elements. 
 * Labels are used to display text or other static content.
 */
public class LabelBuilder extends UIElementBuilder<LabelBuilder> {
    private String text;
    private List<LabelSpan> textSpans;

    /**
     * Constructs a new instance of {@code LabelBuilder} for creating label UI elements.
     * This class is used to define and customize labels which are used to display text 
     * or other static content in the user interface.
     *
     * By default, the label element type is set to {@code UIElements.LABEL}.
     */
    public LabelBuilder() {
        super(UIElements.LABEL, "Label");
    }

    public LabelBuilder(Theme theme) {
        super(theme, UIElements.LABEL, "Label");
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

    /**
     * Sets the text spans to be displayed by the label.
     * TextSpans allow for rich text formatting with different styles per span.
     *
     * @param textSpans The list of LabelSpan objects defining formatted text sections.
     * @return The current instance of the {@code LabelBuilder} for method chaining.
     */
    public LabelBuilder withTextSpans(List<LabelSpan> textSpans) {
        this.textSpans = textSpans;
        return this;
    }

    /**
     * Adds a single text span to the label.
     * TextSpans allow for rich text formatting with different styles per span.
     *
     * @param textSpan The LabelSpan object to add.
     * @return The current instance of the {@code LabelBuilder} for method chaining.
     */
    public LabelBuilder addTextSpan(LabelSpan textSpan) {
        if (this.textSpans == null) {
            this.textSpans = new ArrayList<>();
        }
        this.textSpans.add(textSpan);
        return this;
    }

    public String getText() {
        return text;
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
                StylePropertySets.PADDING, 
                Set.of(
                    "HorizontalAlignment",
                    "VerticalAlignment",
                    "Wrap",
                    "FontName",
                    "FontSize",
                    "TextColor",
                    "OutlineColor",
                    "LetterSpacing",
                    "RenderUppercase",
                    "RenderBold",
                    "RenderItalics",
                    "RenderUnderlined",
                    "Alignment")
        );
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (text != null) {
            HyUIPlugin.getLog().logFinest("Setting Text: " + text + " for " + selector);
            commands.set(selector + ".Text", text);
        }
        if (textSpans != null && !textSpans.isEmpty()) {
            HyUIPlugin.getLog().logFinest("Setting TextSpans for " + selector);
            // TODO: Work out way to set arrays of bson properly.
            //PropertyBatcher.endSet(selector + ".TextSpans", textSpans, commands);
        }

        if ( hyUIStyle == null && typedStyle == null  && style != null) {
            HyUIPlugin.getLog().logFinest("Setting Raw Style: " + style + " for " + selector);
            commands.set(selector + ".Style", style);
        }
    }
}
