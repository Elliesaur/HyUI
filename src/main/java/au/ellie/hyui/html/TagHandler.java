package au.ellie.hyui.html;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.HyUIPluginLogger;
import au.ellie.hyui.builders.*;
import au.ellie.hyui.elements.BackgroundSupported;
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.utils.ParseUtils;
import au.ellie.hyui.utils.StyleUtils;
import com.hypixel.hytale.server.core.Message;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface for handling a specific HTML tag and converting it to a HyUI builder.
 */
public interface TagHandler {
    /**
     * Checks if this handler can handle the given element.
     *
     * @param element The Jsoup element to check.
     * @return true if this handler can process the element, false otherwise.
     */
    boolean canHandle(Element element);

    /**
     * Handles the conversion of the HTML element to a UIElementBuilder.
     *
     * @param element The Jsoup element to convert.
     * @param parser  The parser instance for recursive calls if needed.
     * @return A UIElementBuilder representing the element, or null if it should be ignored.
     */
    UIElementBuilder<?> handle(Element element, HtmlParser parser);

    /**
     * Applies common attributes like id, style, data-*, etc. to the builder.
     *
     * @param builder The builder to apply attributes to.
     * @param element The HTML element containing the attributes.
     */
    default void applyCommonAttributes(UIElementBuilder<?> builder, Element element) {
        if (element.hasAttr("id")) {
            builder.withId(element.attr("id"));
        }

        if (element.hasAttr("data-hyui-tooltiptext")) {
            builder.withTooltipTextSpan(Message.raw(element.attr("data-hyui-tooltiptext")));
        }

        if (element.hasAttr("data-hyui-flexweight")) {
            try {
                builder.withFlexWeight(Integer.parseInt(element.attr("data-hyui-flexweight")));
            } catch (NumberFormatException ignored) {}
        }

        if (element.hasAttr("style")) {
            Map<String, Object> styles = parseStyleAttribute(element.attr("style"));
            applyStyles(builder, styles);
        }

        if (element.hasAttr("data-hyui-hover-style")) {
            Map<String, Object> hoverStyles = parseStyleAttribute(element.attr("data-hyui-hover-style"));
            ParsedStyles parsed = getStylesAnchorsPadding(hoverStyles, builder);
            if (parsed.hasStyle) {
                HyUIStyle currentStyle = builder.getHyUIStyle();
                if (currentStyle == null) {
                    currentStyle = new HyUIStyle();
                }
                HyUIPlugin.getLog().logInfo("Applying hover style: " + parsed.style.toString());
                HyUIPlugin.getLog().logInfo("Applying style: " + currentStyle.toString());
                builder.withStyle(currentStyle.setHoverStyle(parsed.style));
            }
        }

        if (element.hasAttr("data-hyui-style")) {
            Map<String, Object> rawStyles = parseStyleAttribute(element.attr("data-hyui-style"));
            if (!rawStyles.isEmpty()) {
                HyUIStyle currentStyle = builder.getHyUIStyle();
                if (currentStyle == null) {
                    currentStyle = new HyUIStyle();
                }
                for (Map.Entry<String, Object> entry : rawStyles.entrySet()) {
                    HyUIPlugin.getLog().logInfo("Applying style property: " + entry.getKey() + " with value: " + entry.getValue());
                    currentStyle.set(entry.getKey(), entry.getValue());
                }
                builder.withStyle(currentStyle);
            }
        }

        if (element.tagName().equalsIgnoreCase("img") || element.tagName().equalsIgnoreCase("hyvatar")) {
            HyUIAnchor anchor = builder.getAnchor();
            if (anchor == null) {
                anchor = new HyUIAnchor();
            }
            boolean hasImgAttr = false;
            if (element.hasAttr("width")) {
                var width = ParseUtils.parseInt(element.attr("width"));
                if (width.isPresent()) {
                    anchor.setWidth(width.get());
                    hasImgAttr = true;
                }
            }
            if (element.hasAttr("height")) {
                var height = ParseUtils.parseInt(element.attr("height"));
                if (height.isPresent()) {
                    anchor.setHeight(height.get());
                    hasImgAttr = true;
                }
            }
            if (hasImgAttr) {
                builder.withAnchor(anchor);
            }
        }
    }

    default String resolveCssStyleDefinition(Element element, String rawValue) {
        if (rawValue == null) {
            return null;
        }
        String trimmed = rawValue.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        if (trimmed.startsWith("@")) {
            String name = trimmed.substring(1).trim();
            if (!name.isEmpty() && element.ownerDocument() != null && element.ownerDocument().body() != null) {
                String resolved = element.ownerDocument().body().attr("data-hyui-style-def-" + name);
                if (resolved != null && !resolved.isBlank()) {
                    return resolved;
                }
            }
        }
        return trimmed;
    }

    private Map<String, Object> parseStyleAttribute(String styleAttr) {
        Map<String, Object> styles = new HashMap<>();
        String[] declarations = styleAttr.split(";");
        for (String declaration : declarations) {
            String[] parts = declaration.split(":", 2);
            if (parts.length == 2) {
                styles.put(parts[0].trim(), parseStyleValue(parts[1].trim()));
            }
        }
        return styles;
    }

    private void applyStyles(UIElementBuilder<?> builder, Map<String, Object> styles) {
        ParsedStyles parsed = getStylesAnchorsPadding(styles, builder);
        if (parsed.hasStyle) {
            builder.withStyle(parsed.style);
        }
        if (parsed.hasAnchor) {
            builder.withAnchor(parsed.anchor);
        }
        if (parsed.hasPadding) {
            builder.withPadding(parsed.padding);
        }
    }

    class ParsedStyles {
        HyUIStyle style = new HyUIStyle();
        HyUIAnchor anchor = new HyUIAnchor();
        HyUIPadding padding = new HyUIPadding();
        boolean hasStyle = false;
        boolean hasAnchor = false;
        boolean hasPadding = false;
    }

    private ParsedStyles getStylesAnchorsPadding(Map<String, Object> styles, UIElementBuilder<?> builder) {
        ParsedStyles parsed = new ParsedStyles();

        for (Map.Entry<String, Object> entry : styles.entrySet()) {
            String key = entry.getKey();
            String value = toStyleString(entry.getValue());

            switch (key) {
                case "color":
                    parsed.style.setTextColor(value);
                    parsed.hasStyle = true;
                    break;
                case "font-size":
                    parsed.style.setFontSize(value);
                    parsed.hasStyle = true;
                    break;
                case "font-weight":
                    if (value.equalsIgnoreCase("bold")) {
                        parsed.style.setRenderBold(true);
                        parsed.hasStyle = true;
                    }
                    break;
                case "font-style":
                    if (value.equalsIgnoreCase("italic")) {
                        parsed.style.setRenderItalics(true);
                        parsed.hasStyle = true;
                    } else if (value.equalsIgnoreCase("normal")) {
                        parsed.style.setRenderItalics(false);
                        parsed.hasStyle = true;
                    }
                    break;
                case "text-transform":
                    if (value.equalsIgnoreCase("uppercase")) {
                        parsed.style.setRenderUppercase(true);
                        parsed.hasStyle = true;
                    }
                    break;
                case "letter-spacing":
                    try {
                        parsed.style.setLetterSpacing(Integer.parseInt(value));
                        parsed.hasStyle = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "white-space":
                    if (value.equalsIgnoreCase("nowrap")) {
                        parsed.style.setWrap(false);
                        parsed.hasStyle = true;
                    } else if (value.equalsIgnoreCase("normal") || value.equalsIgnoreCase("wrap")) {
                        parsed.style.setWrap(true);
                        parsed.hasStyle = true;
                    }
                    break;
                case "font-family":
                case "font-name":
                    parsed.style.setFontName(value);
                    parsed.hasStyle = true;
                    break;
                case "outline-color":
                case "text-outline-color":
                    parsed.style.setOutlineColor(value);
                    parsed.hasStyle = true;
                    break;
                case "layout-mode":
                case "layout":
                    if (builder instanceof LayoutModeSupported) {
                        ((LayoutModeSupported<?>) builder).withLayoutMode(normalizeLayoutMode(value));
                    }
                    break;
                case "flex-direction":
                    // Map CSS flex-direction to Hytale LayoutMode
                    // row = Left (horizontal), column = Top (vertical)
                    if (builder instanceof LayoutModeSupported) {
                        String layoutMode = switch (value.toLowerCase()) {
                            case "row" -> "Left";
                            case "row-reverse" -> "Right";
                            case "column" -> "Top";
                            case "column-reverse" -> "Bottom";
                            default -> capitalize(value);
                        };
                        ((LayoutModeSupported<?>) builder).withLayoutMode(layoutMode);
                    }
                    break;
                case "justify-content":
                    // Map CSS justify-content to horizontal alignment
                    String hAlign = switch (value.toLowerCase()) {
                        case "flex-start", "start" -> "Left";
                        case "flex-end", "end" -> "Right";
                        case "center" -> "Center";
                        case "space-between", "space-around", "space-evenly" -> "Center"; // approximation
                        default -> capitalize(value);
                    };
                    parsed.style.setHorizontalAlignment(hAlign);
                    parsed.hasStyle = true;
                    break;
                case "align-items":
                    // Map CSS align-items to vertical alignment
                    String vAlign = switch (value.toLowerCase()) {
                        case "flex-start", "start" -> "Start";
                        case "flex-end", "end" -> "End";
                        case "center" -> "Center";
                        case "stretch", "baseline" -> "Center"; // approximation
                        default -> capitalize(value);
                    };
                    parsed.style.setVerticalAlignment(vAlign);
                    parsed.hasStyle = true;
                    break;
                case "vertical-align":
                    parsed.style.setVerticalAlignment(capitalize(value));
                    parsed.hasStyle = true;
                    break;
                case "text-align":
                case "horizontal-align":
                    parsed.style.setHorizontalAlignment(capitalize(value));
                    parsed.hasStyle = true;
                    break;
                case "align":
                    parsed.style.setAlignment(capitalize(value));
                    parsed.hasStyle = true;
                    break;
                case "visibility":
                    if (value.equalsIgnoreCase("hidden")) {
                        builder.withVisible(false);
                    } else if (value.equalsIgnoreCase("shown")) {
                        builder.withVisible(true);
                    }
                    break;
                case "display":
                    if (value.equalsIgnoreCase("none")) {
                        builder.withVisible(false);
                    } else if (value.equalsIgnoreCase("block")) {
                        builder.withVisible(true);
                    }
                    break;
                case "flex-weight":
                    ParseUtils.parseInt(value)
                            .ifPresent(builder::withFlexWeight);
                    break;
                case "anchor-left":
                    ParseUtils.parseInt(value).ifPresent(v -> {
                        parsed.anchor.setLeft(v);
                        parsed.hasAnchor = true;
                    });
                    break;
                case "anchor-right":
                    ParseUtils.parseInt(value).ifPresent(v -> {
                        parsed.anchor.setRight(v);
                        parsed.hasAnchor = true;
                    });
                    break;
                case "anchor-top":
                    ParseUtils.parseInt(value).ifPresent(v -> {
                        parsed.anchor.setTop(v);
                        parsed.hasAnchor = true;
                    });
                    break;
                case "anchor-bottom":
                    ParseUtils.parseInt(value).ifPresent(v -> {
                        parsed.anchor.setBottom(v);
                        parsed.hasAnchor = true;
                    });
                    break;
                case "anchor-width":
                    ParseUtils.parseInt(value).ifPresent(v -> {
                        parsed.anchor.setWidth(v);
                        parsed.hasAnchor = true;
                    });
                    break;
                case "anchor-height":
                    ParseUtils.parseInt(value).ifPresent(v -> {
                        parsed.anchor.setHeight(v);
                        parsed.hasAnchor = true;
                    });
                    break;
                case "anchor-full", "anchor":
                    ParseUtils.parseInt(value).ifPresent(v -> {
                        parsed.anchor.setFull(v);
                        parsed.hasAnchor = true;
                    });
                    break;
                case "anchor-horizontal":
                    ParseUtils.parseInt(value).ifPresent(v -> {
                        parsed.anchor.setHorizontal(v);
                        parsed.hasAnchor = true;
                    });
                    break;
                case "anchor-vertical":
                    ParseUtils.parseInt(value).ifPresent(v -> {
                        parsed.anchor.setVertical(v);
                        parsed.hasAnchor = true;
                    });
                    break;
                case "anchor-min-width":
                    ParseUtils.parseInt(value).ifPresent(v -> {
                        parsed.anchor.setMinWidth(v);
                        parsed.hasAnchor = true;
                    });
                    break;
                case "anchor-max-width":
                    ParseUtils.parseInt(value).ifPresent(v -> {
                        parsed.anchor.setMaxWidth(v);
                        parsed.hasAnchor = true;
                    });
                    break;
                case "padding-left":
                    value = HyUIStyle.cleanUnits(value);
                    ParseUtils.parseInt(value).ifPresent(v -> {
                        parsed.padding.setLeft(v);
                        parsed.hasPadding = true;
                    });
                    break;
                case "padding-right":
                    value = HyUIStyle.cleanUnits(value);
                    ParseUtils.parseInt(value).ifPresent(v -> {
                        parsed.padding.setRight(v);
                        parsed.hasPadding = true;
                    });
                    break;
                case "padding-top":
                    value = HyUIStyle.cleanUnits(value);
                    ParseUtils.parseInt(value).ifPresent(v -> {
                        parsed.padding.setTop(v);
                        parsed.hasPadding = true;
                    });
                    break;
                case "padding-bottom":
                    value = HyUIStyle.cleanUnits(value);
                    ParseUtils.parseInt(value).ifPresent(v -> {
                        parsed.padding.setBottom(v);
                        parsed.hasPadding = true;
                    });
                    break;
                case "padding":
                    String[] paddingValues = value.split("\\s+");
                    if (paddingValues.length == 1) {
                        ParseUtils.parseInt(HyUIStyle.cleanUnits(paddingValues[0])).ifPresent(v -> {
                            parsed.padding.setFull(v);
                            parsed.hasPadding = true;
                        });
                    } else if (paddingValues.length >= 2) {
                        var vertical = ParseUtils.parseInt(HyUIStyle.cleanUnits(paddingValues[0]));
                        var horizontal = ParseUtils.parseInt(HyUIStyle.cleanUnits(paddingValues[1]));
                        if (vertical.isPresent() && horizontal.isPresent()) {
                            parsed.padding.setSymmetric(vertical.get(), horizontal.get());
                            parsed.hasPadding = true;
                        }
                    }
                    break;
                case "background-image":
                    if (builder instanceof BackgroundSupported) {
                        StyleUtils.BackgroundParts parts = StyleUtils.parseBackgroundParts(value, true);
                        String realUrl = parts.value();
                        HyUIPatchStyle background = ((BackgroundSupported<?>) builder).getBackground();
                        if (background == null) {
                            HyUIPatchStyle bg = new HyUIPatchStyle().setTexturePath(realUrl);
                            StyleUtils.applyBorders(bg, parts);
                            ((BackgroundSupported<?>) builder).withBackground(bg);
                        } else {
                            background.setTexturePath(realUrl);
                            StyleUtils.applyBorders(background, parts);
                        }
                    }
                    break;
                case "background-color":
                    if (builder instanceof BackgroundSupported) {
                        StyleUtils.BackgroundParts parts = StyleUtils.parseBackgroundParts(value, false);
                        String normalizedColor = StyleUtils.normalizeBackgroundColor(parts.value());
                        HyUIPatchStyle background = ((BackgroundSupported<?>) builder).getBackground();
                        if (background == null) {
                            HyUIPatchStyle bg = new HyUIPatchStyle().setColor(normalizedColor);
                            StyleUtils.applyBorders(bg, parts);
                            ((BackgroundSupported<?>) builder).withBackground(bg);
                        } else {
                            background.setColor(normalizedColor);
                            StyleUtils.applyBorders(background, parts);
                        }
                    }
                    break;
                case "hyui-style-reference":
                    String[] styleParts = value.split("\\s+");
                    if (styleParts.length == 1) {
                        parsed.style.withStyleReference(styleParts[0].replace("\"", "").replace("'", ""));
                        parsed.hasStyle = true;
                    } else if (styleParts.length >= 2) {
                        parsed.style.withStyleReference(
                                styleParts[0].replace("\"", "").replace("'", ""),
                                styleParts[1].replace("\"", "").replace("'", "")
                        );
                        parsed.hasStyle = true;
                    }
                    break;
                case "hyui-entry-label-style":
                    builder.withSecondaryStyle("EntryLabelStyle", parseStyleReference(value));
                    break;
                case "hyui-selected-entry-label-style":
                    builder.withSecondaryStyle("SelectedEntryLabelStyle", parseStyleReference(value));
                    break;
                case "hyui-popup-style":
                    builder.withSecondaryStyle("PopupStyle", parseStyleReference(value));
                    break;
                case "hyui-number-field-style":
                    builder.withSecondaryStyle("NumberFieldStyle", parseStyleReference(value));
                    break;
                case "hyui-checked-style":
                    builder.withSecondaryStyle("CheckedStyle", parseStyleReference(value));
                    break;
                case "hyui-unchecked-style":
                    builder.withSecondaryStyle("UncheckedStyle", parseStyleReference(value));
                    break;
            }
        }
        return parsed;
    }

    private Object parseStyleValue(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.equalsIgnoreCase("true") || trimmed.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(trimmed);
        }
        return ParseUtils.parseInt(trimmed)
                .<Object>map(Integer::valueOf)
                .or(() -> ParseUtils.parseDouble(trimmed).map(Double::valueOf))
                .orElse(trimmed);
    }

    private String toStyleString(Object value) {
        return value != null ? String.valueOf(value) : "";
    }

    private HyUIStyle parseStyleReference(String value) {
        HyUIStyle style = new HyUIStyle();
        String[] parts = value.split("\\s+");
        if (parts.length == 1) {
            style.withStyleReference(parts[0].replace("\"", "").replace("'", ""));
        } else if (parts.length >= 2) {
            style.withStyleReference(
                    parts[0].replace("\"", "").replace("'", ""),
                    parts[1].replace("\"", "").replace("'", "")
            );
        }
        return style;
    }

    private String normalizeLayoutMode(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String normalized = value.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        for (LayoutModeSupported.LayoutMode mode : LayoutModeSupported.LayoutMode.values()) {
            String modeNormalized = mode.name().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            if (modeNormalized.equals(normalized)) {
                return mode.name();
            }
        }
        return capitalize(value);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
