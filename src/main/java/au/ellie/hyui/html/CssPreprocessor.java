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

package au.ellie.hyui.html;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Preprocesses HTML by extracting styles from &lt;style&gt; tags and applying them to elements.
 */
public class CssPreprocessor {

    public void process(Document doc) {
        Elements styleElements = doc.getElementsByTag("style");
        Map<String, String> cssRules = new HashMap<>();

        for (Element styleElement : styleElements) {
            parseCss(styleElement.data(), cssRules);
            styleElement.remove();
        }

        applyRules(doc, cssRules);
    }

    private void parseCss(String css, Map<String, String> cssRules) {
        // Remove comments
        css = css.replaceAll("//.*|/\\*([\\s\\S]*?)\\*/", "");

        // Updated regex to handle multi-line and different brace placements
        // Use DOTALL (?s) to allow . to match newlines
        Pattern pattern = Pattern.compile("(?s)\\s*([^{]+)\\s*\\{\\s*([^}]+)\\s*\\}");
        Matcher matcher = pattern.matcher(css);

        while (matcher.find()) {
            String selectors = matcher.group(1).trim();
            String properties = matcher.group(2).trim();

            for (String selector : selectors.split(",")) {
                selector = selector.trim();
                if (!selector.isEmpty()) {
                    // Normalize properties: replace newlines/tabs with spaces and trim
                    String normalizedProperties = properties.replaceAll("\\s+", " ").trim();

                    // If multiple rules for same selector, append properties
                    String existing = cssRules.getOrDefault(selector, "");
                    if (!existing.isEmpty() && !existing.endsWith(";")) {
                        existing += ";";
                    }
                    cssRules.put(selector, existing + normalizedProperties);
                }
            }
        }
    }

    private void applyRules(Document doc, Map<String, String> cssRules) {
        for (Map.Entry<String, String> entry : cssRules.entrySet()) {
            String selector = entry.getKey();
            String properties = entry.getValue();

            try {
                if (selector.startsWith("@")) {
                    String name = selector.substring(1).trim();
                    if (!name.isEmpty()) {
                        doc.body().attr("data-hyui-style-def-" + name, properties);
                    }
                    continue;
                }

                // Handle :hover pseudo-class
                if (selector.endsWith(":hover")) {
                    String baseSelector = selector.substring(0, selector.length() - 6);
                    Elements elements = doc.select(baseSelector.isEmpty() ? "*" : baseSelector);
                    for (Element element : elements) {
                        String existingHover = element.attr("data-hyui-hover-style");
                        if (!existingHover.isEmpty() && !existingHover.endsWith(";")) {
                            existingHover += ";";
                        }
                        element.attr("data-hyui-hover-style", properties + (properties.endsWith(";") ? "" : ";") + existingHover);
                    }
                    continue;
                }

                Elements elements = doc.select(selector);
                for (Element element : elements) {
                    String existingStyle = element.attr("style");
                    if (!existingStyle.isEmpty() && !existingStyle.endsWith(";")) {
                        existingStyle += ";";
                    }
                    // CSS properties from style attribute should override CSS rules (specificity)
                    // So we prepend the CSS rules to the existing style
                    element.attr("style", properties + (properties.endsWith(";") ? "" : ";") + existingStyle);
                }
            } catch (Exception ignored) {
                // Ignore invalid selectors
            }
        }
    }
}
