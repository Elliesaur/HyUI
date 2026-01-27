package au.ellie.hyui.builders;

import au.ellie.hyui.utils.ParseUtils;

import java.util.HashMap;
import java.util.Map;

public class HyUIStyle {
    public enum Alignment {
        Left, Center, Right, End, Start
    }

    private Float fontSize;
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
     * @param input The input string to clean
     * @return The cleaned string
     */
    public static String cleanUnits(String input) {
        return input.replaceAll("(rem|em|pt|px|%)", "").trim();
    }
    public HyUIStyle setFontSize(float fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public HyUIStyle setFontSize(String fontSize) {
        fontSize = cleanUnits(fontSize);
        ParseUtils.parseFloat(fontSize)
                .ifPresent(v -> this.fontSize = v);
        return this;
    }

    public HyUIStyle setRenderBold(boolean renderBold) {
        this.renderBold = renderBold;
        return this;
    }

    public HyUIStyle setRenderBold(String renderBold) {
        renderBold = cleanUnits(renderBold);
        this.renderBold = Boolean.parseBoolean(renderBold);
        return this;
    }

    public HyUIStyle setRenderUppercase(boolean renderUppercase) {
        this.renderUppercase = renderUppercase;
        return this;
    }

    public HyUIStyle setRenderUppercase(String renderUppercase) {
        renderUppercase = cleanUnits(renderUppercase);
        this.renderUppercase = Boolean.parseBoolean(renderUppercase);
        return this;
    }

    public HyUIStyle setRenderItalics(boolean renderItalics) {
        this.renderItalics = renderItalics;
        return this;
    }

    public HyUIStyle setRenderItalics(String renderItalics) {
        renderItalics = cleanUnits(renderItalics);
        this.renderItalics = Boolean.parseBoolean(renderItalics);
        return this;
    }

    public HyUIStyle setTextColor(String textColor) {
        textColor = cleanUnits(textColor);
        this.textColor = textColor;
        return this;
    }

    public HyUIStyle setLetterSpacing(int letterSpacing) {
        this.letterSpacing = letterSpacing;
        return this;
    }

    public HyUIStyle setLetterSpacing(String letterSpacing) {
        try {
            letterSpacing = cleanUnits(letterSpacing);
            this.letterSpacing = Integer.parseInt(letterSpacing);
        } catch (NumberFormatException ignored) {}
        return this;
    }

    public HyUIStyle setWrap(boolean wrap) {
        this.wrap = wrap;
        return this;
    }

    public HyUIStyle setWrap(String wrap) {
        wrap = cleanUnits(wrap);
        this.wrap = Boolean.parseBoolean(wrap);
        return this;
    }

    public HyUIStyle setFontName(String fontName) {
        String normalized = normalizeFontName(fontName);
        if (normalized != null) {
            this.fontName = normalized;
        }
        return this;
    }

    public HyUIStyle setOutlineColor(String outlineColor) {
        outlineColor = cleanUnits(outlineColor);
        this.outlineColor = outlineColor;
        return this;
    }

    public HyUIStyle setHorizontalAlignment(Alignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        return this;
    }

    public HyUIStyle setHorizontalAlignment(String horizontalAlignment) {
        ParseUtils.parseEnum(horizontalAlignment, Alignment.class)
                .ifPresent(v -> this.horizontalAlignment = v);
        return this;
    }

    public HyUIStyle setVerticalAlignment(Alignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
        return this;
    }

    public HyUIStyle setVerticalAlignment(String verticalAlignment) {
        ParseUtils.parseEnum(verticalAlignment, Alignment.class)
                .ifPresent(v -> this.verticalAlignment = v);
        return this;
    }

    public HyUIStyle setAlignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public HyUIStyle setAlignment(String alignment) {
        alignment = cleanUnits(alignment);
        ParseUtils.parseEnum(alignment, Alignment.class)
                .ifPresent(v -> this.alignment = v);
        return this;
    }

    public HyUIStyle withStyleReference(String reference) {
        this.styleReference = reference;
        return this;
    }

    public HyUIStyle withStyleReference(String document, String reference) {
        this.styleDocument = document;
        this.styleReference = reference;
        return this;
    }

    public HyUIStyle setDisabledStyle(HyUIStyle style) {
        states.put("Disabled", style);
        return this;
    }

    public HyUIStyle setHoverStyle(HyUIStyle style) {
        states.put("Hovered", style);
        return this;
    }

    public HyUIStyle set(String key, Object value) {
        this.rawProperties.put(key, value);
        return this;
    }

    public HyUIStyle set(Map<String, Object> properties) {
        this.rawProperties.putAll(properties);
        return this;
    }

    public Float getFontSize() {
        return fontSize;
    }

    public Boolean getRenderBold() {
        return renderBold;
    }

    public Boolean getRenderItalics() {
        return renderItalics;
    }

    public Boolean getRenderUppercase() {
        return renderUppercase;
    }

    public String getTextColor() {
        return textColor;
    }

    public Integer getLetterSpacing() {
        return letterSpacing;
    }

    public Boolean getWrap() {
        return wrap;
    }

    public String getFontName() {
        return fontName;
    }

    public String getOutlineColor() {
        return outlineColor;
    }

    public Alignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public Alignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public String getStyleReference() {
        return styleReference;
    }

    public String getStyleDocument() {
        return styleDocument;
    }

    @Override
    public String toString() {
        return "HyUIStyle{" +
                "fontSize=" + fontSize +
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
    
    public String toLabelStyle() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        boolean isFirst = true;
        if (fontSize != null) {
            sb.append("FontSize: ").append(fontSize.intValue());
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

    public Map<String, HyUIStyle> getStates() {
        return states;
    }

    public Map<String, Object> getRawProperties() {
        return rawProperties;
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
