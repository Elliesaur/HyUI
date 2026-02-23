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

import au.ellie.hyui.utils.ParseUtils;
import au.ellie.hyui.utils.BsonDocumentHelper;
import org.bson.BsonDocument;

import java.util.HashMap;
import java.util.Map;

/**
 * Fluent label style definition used by HyUI builders.
 * This maps to Hytale LabelStyle fields and supports state overrides.
 */
public class HyUIStyle {

    private Float fontSize;
    private Float minShrinkTextToFitFontSize;
    private Boolean shrinkTextToFit;
    private Boolean renderBold;
    private Boolean renderItalics;
    private Boolean renderUppercase;
    private String textColor;
    private Integer letterSpacing;
    private Boolean wrap;
    private String fontName;
    private String outlineColor;
    private Alignment horizontalAlignment;
    private Alignment verticalAlignment;
    private Alignment alignment;
    private String styleReference;
    private String styleDocument = "Common.ui";
    private final Map<String, HyUIStyle> states = new HashMap<>();
    private final Map<String, Object> rawProperties = new HashMap<>();

    /**
     * Cleans the input string by removing units like rem, em, pt, px, and %.
     *
     * @param input The input string to clean
     * @return The cleaned string
     */
    public static String cleanUnits(String input) {
        return input.replaceAll("(rem|em|pt|px|%)", "").trim();
    }

    /**
     * Sets the font size in points.
     */
    public HyUIStyle setFontSize(float fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    /**
     * Sets the font size from a string with optional units.
     */
    public HyUIStyle setFontSize(String fontSize) {
        fontSize = cleanUnits(fontSize);
        ParseUtils.parseFloat(fontSize)
                .ifPresent(v -> this.fontSize = v);
        return this;
    }

    /**
     * Sets the minimum font size used when shrinking to fit.
     */
    public HyUIStyle setMinShrinkTextToFitFontSize(float minShrinkTextToFitFontSize) {
        this.minShrinkTextToFitFontSize = minShrinkTextToFitFontSize;
        return this;
    }

    /**
     * Sets the minimum font size used when shrinking to fit, from a string.
     */
    public HyUIStyle setMinShrinkTextToFitFontSize(String minShrinkTextToFitFontSize) {
        minShrinkTextToFitFontSize = cleanUnits(minShrinkTextToFitFontSize);
        ParseUtils.parseFloat(minShrinkTextToFitFontSize)
                .ifPresent(v -> this.minShrinkTextToFitFontSize = v);
        return this;
    }

    /**
     * Enables or disables shrink-to-fit.
     */
    public HyUIStyle setShrinkTextToFit(boolean shrinkTextToFit) {
        this.shrinkTextToFit = shrinkTextToFit;
        return this;
    }

    /**
     * Enables or disables shrink-to-fit from a string value.
     */
    public HyUIStyle setShrinkTextToFit(String shrinkTextToFit) {
        shrinkTextToFit = cleanUnits(shrinkTextToFit);
        this.shrinkTextToFit = Boolean.parseBoolean(shrinkTextToFit);
        return this;
    }

    /**
     * Enables or disables bold rendering.
     */
    public HyUIStyle setRenderBold(boolean renderBold) {
        this.renderBold = renderBold;
        return this;
    }

    /**
     * Enables or disables bold rendering from a string value.
     */
    public HyUIStyle setRenderBold(String renderBold) {
        renderBold = cleanUnits(renderBold);
        this.renderBold = Boolean.parseBoolean(renderBold);
        return this;
    }

    /**
     * Enables or disables uppercase rendering.
     */
    public HyUIStyle setRenderUppercase(boolean renderUppercase) {
        this.renderUppercase = renderUppercase;
        return this;
    }

    /**
     * Enables or disables uppercase rendering from a string value.
     */
    public HyUIStyle setRenderUppercase(String renderUppercase) {
        renderUppercase = cleanUnits(renderUppercase);
        this.renderUppercase = Boolean.parseBoolean(renderUppercase);
        return this;
    }

    /**
     * Enables or disables italics rendering.
     */
    public HyUIStyle setRenderItalics(boolean renderItalics) {
        this.renderItalics = renderItalics;
        return this;
    }

    /**
     * Enables or disables italics rendering from a string value.
     */
    public HyUIStyle setRenderItalics(String renderItalics) {
        renderItalics = cleanUnits(renderItalics);
        this.renderItalics = Boolean.parseBoolean(renderItalics);
        return this;
    }

    /**
     * Sets the text color (hex or named).
     */
    public HyUIStyle setTextColor(String textColor) {
        textColor = cleanUnits(textColor);
        this.textColor = textColor;
        return this;
    }

    /**
     * Sets the letter spacing.
     */
    public HyUIStyle setLetterSpacing(int letterSpacing) {
        this.letterSpacing = letterSpacing;
        return this;
    }

    /**
     * Sets the letter spacing from a string value.
     */
    public HyUIStyle setLetterSpacing(String letterSpacing) {
        try {
            letterSpacing = cleanUnits(letterSpacing);
            this.letterSpacing = Integer.parseInt(letterSpacing);
        } catch (NumberFormatException ignored) {}
        return this;
    }

    /**
     * Enables or disables text wrapping.
     */
    public HyUIStyle setWrap(boolean wrap) {
        this.wrap = wrap;
        return this;
    }

    /**
     * Enables or disables text wrapping from a string value.
     */
    public HyUIStyle setWrap(String wrap) {
        wrap = cleanUnits(wrap);
        this.wrap = Boolean.parseBoolean(wrap);
        return this;
    }

    /**
     * Sets the font name (currently a no-op due to engine limitations).
     */
    public HyUIStyle setFontName(String fontName) {
        // Literally do nothing. TODO: Figure out font crashes.
        /*String normalized = normalizeFontName(fontName);
        if (normalized != null) {
            this.fontName = normalized;
        }*/
        return this;
    }

    /**
     * Sets the outline color.
     */
    public HyUIStyle setOutlineColor(String outlineColor) {
        outlineColor = cleanUnits(outlineColor);
        this.outlineColor = outlineColor;
        return this;
    }

    /**
     * Sets the horizontal alignment.
     */
    public HyUIStyle setHorizontalAlignment(Alignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        return this;
    }

    /**
     * Sets the horizontal alignment from a string value.
     */
    public HyUIStyle setHorizontalAlignment(String horizontalAlignment) {
        ParseUtils.parseEnum(horizontalAlignment, Alignment.class)
                .ifPresent(v -> this.horizontalAlignment = v);
        return this;
    }

    /**
     * Sets the vertical alignment.
     */
    public HyUIStyle setVerticalAlignment(Alignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
        return this;
    }

    /**
     * Sets the vertical alignment from a string value.
     */
    public HyUIStyle setVerticalAlignment(String verticalAlignment) {
        ParseUtils.parseEnum(verticalAlignment, Alignment.class)
                .ifPresent(v -> this.verticalAlignment = v);
        return this;
    }

    /**
     * Sets the overall alignment.
     */
    public HyUIStyle setAlignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    /**
     * Sets the overall alignment from a string value.
     */
    public HyUIStyle setAlignment(String alignment) {
        alignment = cleanUnits(alignment);
        ParseUtils.parseEnum(alignment, Alignment.class)
                .ifPresent(v -> this.alignment = v);
        return this;
    }

    /**
     * Sets a style reference within the default document.
     */
    public HyUIStyle withStyleReference(String reference) {
        this.styleReference = reference;
        return this;
    }

    /**
     * Sets a style reference within a specific document.
     */
    public HyUIStyle withStyleReference(String document, String reference) {
        this.styleDocument = document;
        this.styleReference = reference;
        return this;
    }

    /**
     * Sets the disabled state style.
     */
    public HyUIStyle setDisabledStyle(HyUIStyle style) {
        states.put("Disabled", style);
        return this;
    }

    /**
     * Sets the hovered state style.
     */
    public HyUIStyle setHoverStyle(HyUIStyle style) {
        states.put("Hovered", style);
        return this;
    }

    /**
     * Sets a raw property value.
     */
    public HyUIStyle set(String key, Object value) {
        this.rawProperties.put(key, value);
        return this;
    }

    /**
     * Sets multiple raw property values.
     */
    public HyUIStyle set(Map<String, Object> properties) {
        this.rawProperties.putAll(properties);
        return this;
    }

    /**
     * @return the configured font size
     */
    public Float getFontSize() {
        return fontSize;
    }

    /**
     * @return the minimum font size used when shrinking to fit
     */
    public Float getMinShrinkTextToFitFontSize() {
        return minShrinkTextToFitFontSize;
    }

    /**
     * @return whether shrink-to-fit is enabled
     */
    public Boolean getShrinkTextToFit() {
        return shrinkTextToFit;
    }

    /**
     * @return whether bold rendering is enabled
     */
    public Boolean getRenderBold() {
        return renderBold;
    }

    /**
     * @return whether italics rendering is enabled
     */
    public Boolean getRenderItalics() {
        return renderItalics;
    }

    /**
     * @return whether uppercase rendering is enabled
     */
    public Boolean getRenderUppercase() {
        return renderUppercase;
    }

    /**
     * @return the text color
     */
    public String getTextColor() {
        return textColor;
    }

    /**
     * @return the letter spacing
     */
    public Integer getLetterSpacing() {
        return letterSpacing;
    }

    /**
     * @return whether wrapping is enabled
     */
    public Boolean getWrap() {
        return wrap;
    }

    /**
     * @return the font name (may be null)
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * @return the outline color
     */
    public String getOutlineColor() {
        return outlineColor;
    }

    /**
     * @return horizontal alignment
     */
    public Alignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * @return vertical alignment
     */
    public Alignment getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * @return overall alignment
     */
    public Alignment getAlignment() {
        return alignment;
    }

    /**
     * @return style reference name
     */
    public String getStyleReference() {
        return styleReference;
    }

    /**
     * @return style document name
     */
    public String getStyleDocument() {
        return styleDocument;
    }

    @Override
    public String toString() {
        return "HyUIStyle{" +
                "fontSize=" + fontSize +
                ", minShrinkTextToFitFontSize=" + minShrinkTextToFitFontSize +
                ", shrinkTextToFit=" + shrinkTextToFit +
                ", renderBold=" + renderBold +
                ", renderItalics=" + renderItalics +
                ", renderUppercase=" + renderUppercase +
                ", textColor='" + textColor + '\'' +
                ", letterSpacing=" + letterSpacing +
                ", wrap=" + wrap +
                ", fontName='" + fontName + '\'' +
                ", outlineColor='" + outlineColor + '\'' +
                ", horizontalAlignment=" + horizontalAlignment +
                ", verticalAlignment=" + verticalAlignment +
                ", alignment=" + alignment +
                ", styleReference='" + styleReference + '\'' +
                ", styleDocument='" + styleDocument + '\'' +
                ", states=" + states +
                ", rawProperties=" + rawProperties +
                '}';
    }
    
    /**
     * Converts this style to a Hytale LabelStyle string.
     */
    public String toLabelStyle() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        boolean isFirst = true;
        if (fontSize != null) {
            sb.append("FontSize: ").append(fontSize.intValue());
            isFirst = false;
        }
        if (minShrinkTextToFitFontSize != null) {
            if (!isFirst) sb.append(", ");
            sb.append("MinShrinkTextToFitFontSize: ").append(minShrinkTextToFitFontSize.intValue());
            isFirst = false;
        }
        if (shrinkTextToFit != null) {
            if (!isFirst) sb.append(", ");
            sb.append("ShrinkTextToFit: ").append(shrinkTextToFit);
            isFirst = false;
        }
        if (fontName != null) {
            if (!isFirst) sb.append(", ");
            sb.append("FontName: ").append(fontName);
            isFirst = false;
        }
        if (letterSpacing != null) {
            if (!isFirst) sb.append(", ");
            sb.append("LetterSpacing: ").append(letterSpacing);
            isFirst = false;
        }
        if (textColor != null) {
            if (!isFirst) sb.append(", ");
            sb.append("TextColor: ").append(textColor);
            isFirst = false;
        }
        if (renderBold != null) {
            if (!isFirst) sb.append(", ");
            sb.append("RenderBold: ").append(renderBold);
            isFirst = false;
        }
        if (renderUppercase != null) {
            if (!isFirst) sb.append(", ");
            sb.append("RenderUppercase: ").append(renderUppercase);
            isFirst = false;
        }
        if (renderItalics != null) {
            if (!isFirst) sb.append(", ");
            sb.append("RenderItalics: ").append(renderItalics);
            isFirst = false;
        }
        if (alignment != null) {
            if (!isFirst) sb.append(", ");
            sb.append("Alignment: ").append(alignment.name());
            isFirst = false;
        }
        if (horizontalAlignment != null) {
            if (!isFirst) sb.append(", ");
            sb.append("HorizontalAlignment: ").append(horizontalAlignment.name());
            isFirst = false;
        }
        if (verticalAlignment != null) {
            if (!isFirst) sb.append(", ");
            sb.append("VerticalAlignment: ").append(verticalAlignment.name());
            isFirst = false;
        }
        if (outlineColor != null) {
            if (!isFirst) sb.append(", ");
            sb.append("OutlineColor: ").append(outlineColor);
            isFirst = false;
        }
        if (wrap != null) {
            if (!isFirst) sb.append(", ");
            sb.append("Wrap: ").append(wrap);
            isFirst = false;
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * @return per-state overrides (e.g., Disabled, Hovered)
     */
    public Map<String, HyUIStyle> getStates() {
        return states;
    }

    /**
     * @return raw style properties applied alongside typed fields
     */
    public Map<String, Object> getRawProperties() {
        return rawProperties;
    }

    /**
     * Converts this style into a BSON document for UI commands.
     */
    public BsonDocument toBsonDocument() {
        BsonDocumentHelper doc = new BsonDocumentHelper();
        if (fontSize != null) doc.set("FontSize", fontSize.doubleValue());
        if (minShrinkTextToFitFontSize != null) doc.set("MinShrinkTextToFitFontSize", minShrinkTextToFitFontSize.doubleValue());
        if (shrinkTextToFit != null) doc.set("ShrinkTextToFit", shrinkTextToFit);
        if (fontName != null) doc.set("FontName", fontName);
        if (letterSpacing != null) doc.set("LetterSpacing", letterSpacing);
        if (textColor != null) doc.set("TextColor", textColor);
        if (renderBold != null) doc.set("RenderBold", renderBold);
        if (renderUppercase != null) doc.set("RenderUppercase", renderUppercase);
        if (renderItalics != null) doc.set("RenderItalics", renderItalics);
        if (alignment != null) doc.set("Alignment", alignment.name());
        if (horizontalAlignment != null) doc.set("HorizontalAlignment", horizontalAlignment.name());
        if (verticalAlignment != null) doc.set("VerticalAlignment", verticalAlignment.name());
        if (outlineColor != null) doc.set("OutlineColor", outlineColor);
        if (wrap != null) doc.set("Wrap", wrap);
        rawProperties.forEach((key, value) -> {
            if (value instanceof String s) doc.set(key, s);
            else if (value instanceof Boolean b) doc.set(key, b);
            else if (value instanceof Integer i) doc.set(key, i);
            else if (value instanceof Double d) doc.set(key, d);
            else if (value instanceof Float f) doc.set(key, f);
            else if (value != null) doc.set(key, String.valueOf(value));
        });
        return doc.getDocument();
    }

    private String normalizeFontName(String fontName) {
        if (fontName == null || fontName.isBlank()) {
            return null;
        }
        if (fontName.equalsIgnoreCase("default")) {
            return "Default";
        }
        if (fontName.equalsIgnoreCase("secondary")) {
            return "Secondary";
        }
        return null;
    }
}
