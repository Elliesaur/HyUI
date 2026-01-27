package au.ellie.hyui.html.handlers;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.builders.ButtonBuilder;
import au.ellie.hyui.builders.CustomButtonBuilder;
import au.ellie.hyui.builders.HyUIStyle;
import au.ellie.hyui.builders.HyUIPatchStyle;
import au.ellie.hyui.builders.LabelBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import au.ellie.hyui.utils.ParseUtils;
import au.ellie.hyui.utils.StyleUtils;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;

public class ButtonHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        String tag = element.tagName().toLowerCase();
        if (tag.equals("button")) return true;
        if (tag.equals("input")) {
            String type = element.attr("type").toLowerCase();
            return type.equals("submit") || type.equals("reset");
        }
        return false;
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        String tag = element.tagName().toLowerCase();
        UIElementBuilder<?> builder;

        boolean hasItemIcon = !element.select("> span.item-icon").isEmpty();
        boolean hasItemSlot = !element.select("> span.item-slot").isEmpty();
        boolean isRaw = hasItemIcon || hasItemSlot || element.hasClass("raw-button");
        boolean isCustomTextButton = element.hasClass("custom-textbutton");
        boolean isCustomButton = element.hasClass("custom-button");

        if (isCustomTextButton || isCustomButton) {
            builder = isCustomTextButton
                    ? CustomButtonBuilder.customTextButton()
                    : CustomButtonBuilder.customButton();
        } else {
            if (tag.equals("input") && element.attr("type").equalsIgnoreCase("reset")) {
                builder = ButtonBuilder.cancelTextButton();
            } else if (tag.equals("button") && element.hasClass("back-button")) {
                builder = ButtonBuilder.backButton();
            } else if (tag.equals("button") && element.hasClass("secondary-button")) {
                builder = ButtonBuilder.secondaryTextButton();
            } else if (tag.equals("button") && element.hasClass("small-secondary-button")) {
                builder = ButtonBuilder.smallSecondaryTextButton();
            } else if (tag.equals("button") && element.hasClass("tertiary-button")) {
                builder = ButtonBuilder.tertiaryTextButton();
            } else if (tag.equals("button") && element.hasClass("small-tertiary-button")) {
                builder = ButtonBuilder.smallTertiaryTextButton();
            } else if (isRaw) {
                builder = ButtonBuilder.rawButton();
            } else {
                builder = ButtonBuilder.textButton();
            }
        }

        // Parse children
        boolean allowTextContent = !isRaw && !isCustomButton;
        java.util.List<UIElementBuilder<?>> children = parser.parseChildren(element);
        if (!children.isEmpty()) {
            if (builder instanceof CustomButtonBuilder customButtonBuilder) {
                if (isCustomTextButton && children.size() == 1 && children.get(0) instanceof LabelBuilder label) {
                    customButtonBuilder.withText(label.getText());
                } else {
                    for (UIElementBuilder<?> child : children) {
                        builder.addChild(child);
                    }
                }
            } else {
                for (UIElementBuilder<?> child : children) {
                    if (allowTextContent && child instanceof LabelBuilder label) {
                        if (builder instanceof ButtonBuilder buttonBuilder) {
                            buttonBuilder.withText(label.getText());
                        }
                    } else {
                        builder.addChild(child);
                    }
                }
            }
        } else {
            String text = element.text();
            if (tag.equals("input") && element.hasAttr("value")) {
                text = element.attr("value");
            }

            if (!text.isEmpty() && allowTextContent) {
                if (builder instanceof ButtonBuilder buttonBuilder) {
                    buttonBuilder.withText(text);
                }
            }
        }

        if (builder instanceof CustomButtonBuilder customButtonBuilder) {
            applyCustomButtonStyleAttributes(customButtonBuilder, element, isCustomTextButton);
        }
        applyButtonStateAttributes(builder, element);
        applyCommonAttributes(builder, element);

        return builder;
    }

    private void applyButtonStateAttributes(UIElementBuilder<?> builder, Element element) {
        if (element.hasAttr("disabled") || element.hasAttr("data-hyui-disabled")) {
            boolean disabled = element.hasAttr("disabled")
                    || Boolean.parseBoolean(element.attr("data-hyui-disabled"));
            if (builder instanceof ButtonBuilder buttonBuilder) {
                buttonBuilder.withDisabled(disabled);
            } else if (builder instanceof CustomButtonBuilder customButtonBuilder) {
                customButtonBuilder.withDisabled(disabled);
            }
        }
        if (element.hasAttr("data-hyui-overscroll")) {
            boolean overscroll = Boolean.parseBoolean(element.attr("data-hyui-overscroll"));
            if (builder instanceof ButtonBuilder buttonBuilder) {
                buttonBuilder.withOverscroll(overscroll);
            } else if (builder instanceof CustomButtonBuilder customButtonBuilder) {
                customButtonBuilder.withOverscroll(overscroll);
            }
        }
    }

    private void applyCustomButtonStyleAttributes(CustomButtonBuilder builder, Element element, boolean isTextButton) {
        applyCustomButtonLabelStyle(builder, element, isTextButton, "data-hyui-default-label-style", "default");
        applyCustomButtonLabelStyle(builder, element, isTextButton, "data-hyui-hovered-label-style", "hovered");
        applyCustomButtonLabelStyle(builder, element, isTextButton, "data-hyui-pressed-label-style", "pressed");
        applyCustomButtonLabelStyle(builder, element, isTextButton, "data-hyui-disabled-label-style", "disabled");

        applyCustomButtonBackgroundStyle(builder, element, "data-hyui-default-bg", "default");
        applyCustomButtonBackgroundStyle(builder, element, "data-hyui-hovered-bg", "hovered");
        applyCustomButtonBackgroundStyle(builder, element, "data-hyui-pressed-bg", "pressed");
        applyCustomButtonBackgroundStyle(builder, element, "data-hyui-disabled-bg", "disabled");
    }

    private void applyCustomButtonLabelStyle(CustomButtonBuilder builder, Element element, boolean isTextButton,
                                             String attribute, String state) {
        if (!isTextButton || !element.hasAttr(attribute)) {
            return;
        }
        String resolved = resolveCssStyleDefinition(element, element.attr(attribute));
        HyUIStyle labelStyle = parseLabelStyle(resolved);
        if (labelStyle == null) {
            return;
        }
        switch (state) {
            case "default" -> builder.withDefaultLabelStyle(labelStyle);
            case "hovered" -> builder.withHoveredLabelStyle(labelStyle);
            case "pressed" -> builder.withPressedLabelStyle(labelStyle);
            case "disabled" -> builder.withDisabledLabelStyle(labelStyle);
            default -> {
            }
        }
    }

    private void applyCustomButtonBackgroundStyle(CustomButtonBuilder builder, Element element, String attribute, String state) {
        if (!element.hasAttr(attribute)) {
            return;
        }
        String resolved = resolveCssStyleDefinition(element, element.attr(attribute));
        HyUIPatchStyle background = parseBackgroundStyle(resolved);
        if (background == null) {
            return;
        }
        switch (state) {
            case "default" -> builder.withDefaultBackground(background);
            case "hovered" -> builder.withHoveredBackground(background);
            case "pressed" -> builder.withPressedBackground(background);
            case "disabled" -> builder.withDisabledBackground(background);
            default -> {
            }
        }
    }

    private HyUIStyle parseLabelStyle(String styleValue) {
        if (styleValue == null || styleValue.isBlank()) {
            return null;
        }
        Map<String, String> styles = parseStyleAttribute(styleValue);
        if (styles.isEmpty()) {
            return null;
        }
        HyUIStyle style = new HyUIStyle();
        boolean hasStyle = false;
        for (Map.Entry<String, String> entry : styles.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            switch (key) {
                case "color":
                    style.setTextColor(StyleUtils.normalizeBackgroundColor(value));
                    hasStyle = true;
                    break;
                case "font-size":
                    style.setFontSize(value);
                    hasStyle = true;
                    break;
                case "font-weight":
                    if (value.equalsIgnoreCase("bold")) {
                        style.setRenderBold(true);
                        hasStyle = true;
                    } else if (value.equalsIgnoreCase("normal")) {
                        style.setRenderBold(false);
                        hasStyle = true;
                    } else {
                        var fw = ParseUtils.parseInt(value);
                        if (fw.isPresent()) {
                            if (fw.get() >= 600) {
                                style.setRenderBold(true);
                            } else {
                                style.setRenderBold(false);
                            }
                            hasStyle = true;
                        }
                        
                    }
                    break;
                case "font-style":
                    if (value.equalsIgnoreCase("italic")) {
                        style.setRenderItalics(true);
                        hasStyle = true;
                    } else if (value.equalsIgnoreCase("normal")) {
                        style.setRenderItalics(false);
                        hasStyle = true;
                    }
                    break;
                case "text-transform":
                    if (value.equalsIgnoreCase("uppercase")) {
                        style.setRenderUppercase(true);
                        hasStyle = true;
                    }
                    break;
                case "letter-spacing":
                    style.setLetterSpacing(value);
                    hasStyle = true;
                    break;
                case "white-space":
                    if (value.equalsIgnoreCase("nowrap")) {
                        style.setWrap(false);
                        hasStyle = true;
                    } else if (value.equalsIgnoreCase("normal") || value.equalsIgnoreCase("wrap")) {
                        style.setWrap(true);
                        hasStyle = true;
                    }
                    break;
                case "font-family":
                case "font-name":
                    style.setFontName(value);
                    hasStyle = true;
                    break;
                case "outline-color":
                case "text-outline-color":
                    style.setOutlineColor(StyleUtils.normalizeBackgroundColor(value));
                    hasStyle = true;
                    break;
                case "vertical-align":
                    style.setVerticalAlignment(capitalize(value));
                    hasStyle = true;
                    break;
                case "text-align":
                case "horizontal-align":
                    style.setHorizontalAlignment(capitalize(value));
                    hasStyle = true;
                    break;
                case "align":
                    style.setAlignment(capitalize(value));
                    hasStyle = true;
                    break;
            }
        }
        return hasStyle ? style : null;
    }

    private HyUIPatchStyle parseBackgroundStyle(String styleValue) {
        if (styleValue == null || styleValue.isBlank()) {
            return null;
        }
        Map<String, String> styles = parseStyleAttribute(styleValue);
        if (styles.isEmpty()) {
            return null;
        }
        HyUIPatchStyle background = null;
        for (Map.Entry<String, String> entry : styles.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            switch (key) {
                case "background-image": {
                    StyleUtils.BackgroundParts parts = StyleUtils.parseBackgroundParts(value, true);
                    String realUrl = parts.value();
                    if (realUrl == null) {
                        break;
                    }
                    if (background == null) {
                        background = new HyUIPatchStyle().setTexturePath(realUrl);
                    } else {
                        background.setTexturePath(realUrl);
                    }
                    StyleUtils.applyBorders(background, parts);
                    break;
                }
                case "background-color": {
                    StyleUtils.BackgroundParts parts = StyleUtils.parseBackgroundParts(value, false);
                    String normalizedColor = StyleUtils.normalizeBackgroundColor(parts.value());
                    if (normalizedColor == null) {
                        break;
                    }
                    if (background == null) {
                        background = new HyUIPatchStyle().setColor(normalizedColor);
                    } else {
                        background.setColor(normalizedColor);
                    }
                    StyleUtils.applyBorders(background, parts);
                    break;
                }
            }
        }
        return background;
    }

    private Map<String, String> parseStyleAttribute(String styleAttr) {
        Map<String, String> styles = new HashMap<>();
        if (styleAttr == null) {
            return styles;
        }
        String[] declarations = styleAttr.split(";");
        for (String declaration : declarations) {
            String[] parts = declaration.split(":", 2);
            if (parts.length == 2) {
                styles.put(parts[0].trim().toLowerCase(), parts[1].trim());
            }
        }
        return styles;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
