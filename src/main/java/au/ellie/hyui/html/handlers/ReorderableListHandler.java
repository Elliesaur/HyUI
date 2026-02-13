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

import au.ellie.hyui.builders.ReorderableListBuilder;
import au.ellie.hyui.builders.LabelBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.elements.ScrollbarStyleSupported;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.Optional;

public class ReorderableListHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        String tag = element.tagName().toLowerCase();
        return tag.equals("reorderable-list") || tag.equals("hyui-reorderable-list");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        ReorderableListBuilder builder = ReorderableListBuilder.reorderableList();
        applyCommonAttributes(builder, element);
        applyScrollbarStyle(builder, element);

        for (Node childNode : element.childNodes()) {
            if (childNode instanceof Element childElement) {
                UIElementBuilder<?> child = parser.handleElement(childElement);
                if (child != null) {
                    builder.addChild(child);
                }
            } else if (childNode instanceof TextNode textNode) {
                String text = textNode.text().trim();
                if (!text.isEmpty()) {
                    builder.addChild(LabelBuilder.label().withText(text));
                }
            }
        }

        return builder;
    }

    private void applyScrollbarStyle(UIElementBuilder<?> builder, Element element) {
        if (!(builder instanceof ScrollbarStyleSupported<?> scrollbarSupported)) {
            return;
        }
        if (element.hasAttr("data-hyui-scrollbar-style")) {
            parseStyleReference(element.attr("data-hyui-scrollbar-style"))
                    .ifPresent(ref -> scrollbarSupported.withScrollbarStyle(ref.document, ref.reference));
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
