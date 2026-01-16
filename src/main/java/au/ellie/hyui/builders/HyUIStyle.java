package au.ellie.hyui.builders;

import java.util.HashMap;
import java.util.Map;

public class HyUIStyle {
    public enum Alignment {
        Left, Center, Right, End, Start
    }

    private Float fontSize;
    private Boolean renderBold;
    private Boolean renderUppercase;
    private String textColor;
    private Alignment horizontalAlignment;
    private Alignment verticalAlignment;
    private Alignment alignment;
    private final Map<String, HyUIStyle> states = new HashMap<>();
    private final Map<String, Object> rawProperties = new HashMap<>();

    public HyUIStyle setFontSize(float fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public HyUIStyle setFontSize(String fontSize) {
        try {
            this.fontSize = Float.parseFloat(fontSize);
        } catch (NumberFormatException ignored) {}
        return this;
    }

    public HyUIStyle setRenderBold(boolean renderBold) {
        this.renderBold = renderBold;
        return this;
    }

    public HyUIStyle setRenderBold(String renderBold) {
        this.renderBold = Boolean.parseBoolean(renderBold);
        return this;
    }

    public HyUIStyle setRenderUppercase(boolean renderUppercase) {
        this.renderUppercase = renderUppercase;
        return this;
    }

    public HyUIStyle setRenderUppercase(String renderUppercase) {
        this.renderUppercase = Boolean.parseBoolean(renderUppercase);
        return this;
    }

    public HyUIStyle setTextColor(String textColor) {
        this.textColor = textColor;
        return this;
    }

    public HyUIStyle setHorizontalAlignment(Alignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        return this;
    }

    public HyUIStyle setHorizontalAlignment(String horizontalAlignment) {
        try {
            this.horizontalAlignment = Alignment.valueOf(horizontalAlignment);
        } catch (IllegalArgumentException ignored) {}
        return this;
    }

    public HyUIStyle setVerticalAlignment(Alignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
        return this;
    }

    public HyUIStyle setVerticalAlignment(String verticalAlignment) {
        try {
            this.verticalAlignment = Alignment.valueOf(verticalAlignment);
        } catch (IllegalArgumentException ignored) {}
        return this;
    }

    public HyUIStyle setAlignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public HyUIStyle setAlignment(String alignment) {
        try {
            this.alignment = Alignment.valueOf(alignment);
        } catch (IllegalArgumentException ignored) {}
        return this;
    }

    public HyUIStyle setDisabledStyle(HyUIStyle style) {
        states.put("Disabled", style);
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

    public Boolean getRenderUppercase() {
        return renderUppercase;
    }

    public String getTextColor() {
        return textColor;
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

    public Map<String, HyUIStyle> getStates() {
        return states;
    }

    public Map<String, Object> getRawProperties() {
        return rawProperties;
    }
}
