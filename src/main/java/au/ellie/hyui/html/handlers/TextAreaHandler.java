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

package au.ellie.hyui.html.handlers;

import au.ellie.hyui.builders.HyUIPadding;
import au.ellie.hyui.builders.HyUIStyle;
import au.ellie.hyui.builders.TextFieldBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import au.ellie.hyui.utils.ParseUtils;
import org.jsoup.nodes.Element;

import java.util.Optional;

public class TextAreaHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        return element.tagName().equalsIgnoreCase("textarea");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        TextFieldBuilder builder = TextFieldBuilder.multilineTextField();
        applyTextAreaAttributes(builder, element);
        applyCommonAttributes(builder, element);
        return builder;
    }

    private void applyTextAreaAttributes(TextFieldBuilder builder, Element element) {
        if (element.hasAttr("value")) {
            builder.withValue(element.attr("value"));
        } else if (!element.text().isBlank()) {
            builder.withValue(element.text());
        }
        if (element.hasAttr("placeholder")) {
            builder.withPlaceholderText(element.attr("placeholder"));
        }
        if (element.hasAttr("maxlength")) {
            ParseUtils.parseInt(element.attr("maxlength"))
                    .ifPresent(builder::withMaxLength);
        }
        if (element.hasAttr("readonly")) {
            builder.withReadOnly(true);
        }
        if (element.hasAttr("rows")) {
            ParseUtils.parseInt(element.attr("rows"))
                    .ifPresent(builder::withMaxVisibleLines);
        }
        if (element.hasAttr("data-hyui-max-visible-lines")) {
            ParseUtils.parseInt(element.attr("data-hyui-max-visible-lines"))
                    .ifPresent(builder::withMaxVisibleLines);
        }
        if (element.hasAttr("data-hyui-auto-grow")) {
            builder.withAutoGrow(Boolean.parseBoolean(element.attr("data-hyui-auto-grow")));
        }
        if (element.hasAttr("data-hyui-scrollbar-style")) {
            parseStyleReference(element.attr("data-hyui-scrollbar-style"))
                    .ifPresent(ref -> builder.withScrollbarStyle(ref.document, ref.reference));
        }
        if (element.hasAttr("data-hyui-background")) {
            parseStyleReference(element.attr("data-hyui-background"))
                    .ifPresent(ref -> builder.withBackground(ref.document, ref.reference));
        }
        if (element.hasAttr("data-hyui-placeholder-style")) {
            parseStyleReference(element.attr("data-hyui-placeholder-style"))
                    .ifPresent(ref -> builder.withPlaceholderStyle(
                            new HyUIStyle().withStyleReference(ref.document, ref.reference)));
        }
        if (element.hasAttr("data-hyui-content-padding")) {
            parsePadding(element.attr("data-hyui-content-padding"))
                    .ifPresent(builder::withContentPadding);
        }
    }

    private Optional<StyleReference> parseStyleReference(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }
        String[] parts = rawValue.trim().split("\\s+");
        if (parts.length >= 2) {
            return Optional.of(new StyleReference(stripQuotes(parts[0]), stripQuotes(parts[1])));
        }
        return Optional.of(new StyleReference("Common.ui", stripQuotes(rawValue.trim())));
    }

    private Optional<HyUIPadding> parsePadding(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }
        String cleaned = rawValue.trim();
        if (cleaned.startsWith("(") && cleaned.endsWith(")")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        HyUIPadding padding = new HyUIPadding();
        boolean hasValue = false;
        for (String part : cleaned.split(",")) {
            String[] tokens = part.trim().split(":", 2);
            if (tokens.length != 2) {
                continue;
            }
            String key = tokens[0].trim().toLowerCase();
            Optional<Integer> value = ParseUtils.parseInt(tokens[1].trim());
            if (value.isEmpty()) {
                continue;
            }
            int v = value.get();
            switch (key) {
                case "horizontal":
                    padding.setLeft(v).setRight(v);
                    hasValue = true;
                    break;
                case "vertical":
                    padding.setTop(v).setBottom(v);
                    hasValue = true;
                    break;
                case "left":
                    padding.setLeft(v);
                    hasValue = true;
                    break;
                case "right":
                    padding.setRight(v);
                    hasValue = true;
                    break;
                case "top":
                    padding.setTop(v);
                    hasValue = true;
                    break;
                case "bottom":
                    padding.setBottom(v);
                    hasValue = true;
                    break;
                default:
                    break;
            }
        }
        return hasValue ? Optional.of(padding) : Optional.empty();
    }

    private String stripQuotes(String value) {
        String trimmed = value.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    private static final class StyleReference {
        private final String document;
        private final String reference;

        private StyleReference(String document, String reference) {
            this.document = document;
            this.reference = reference;
        }
    }
}
