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

import au.ellie.hyui.builders.TabNavigationBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import au.ellie.hyui.utils.ParseUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * Handler for tab navigation elements in HYUIML.
 *
 * Supports:
 * - &lt;nav class="tabs"&gt; or &lt;nav class="tab-navigation"&gt;
 * - &lt;div class="tabs"&gt; or &lt;div class="tab-navigation"&gt;
 *
 * Structure:
 * <pre>
 * &lt;nav class="tabs"&gt;
 *     &lt;button data-tab="tab1"&gt;Tab 1&lt;/button&gt;
 *     &lt;button data-tab="tab2" class="active"&gt;Tab 2&lt;/button&gt;
 *     &lt;button data-tab="tab3"&gt;Tab 3&lt;/button&gt;
 * &lt;/nav&gt;
 * </pre>
 *
 * Or simplified:
 * <pre>
 * &lt;nav class="tabs" data-tabs="inventory:Inventory:inventory-content,stats:Statistics:stats-content" data-selected="inventory"&gt;
 * &lt;/nav&gt;
 * </pre>
 *
 * Content can be linked with a third entry in data-tabs or a data-tab-content attribute:
 * <pre>
 * &lt;button data-tab="inventory" data-tab-content="inventory-content"&gt;Inventory&lt;/button&gt;
 * </pre>
 */
public class TabNavigationHandler implements TagHandler {

    @Override
    public boolean canHandle(Element element) {
        String tagName = element.tagName().toLowerCase();
        return (tagName.equals("nav") || tagName.equals("div")) &&
               (element.hasClass("tabs") || element.hasClass("tab-navigation"));
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        TabNavigationBuilder builder = TabNavigationBuilder.tabNavigation();

        applyCommonAttributes(builder, element);

        String selectedTabId = element.hasAttr("data-selected") ? element.attr("data-selected").trim() : null;
        if (selectedTabId != null && selectedTabId.isBlank()) {
            selectedTabId = null;
        }

        // Check for simplified data-tabs attribute
        if (element.hasAttr("data-tabs")) {
            String tabsAttr = element.attr("data-tabs");
            for (String tabDef : tabsAttr.split(",")) {
                String[] parts = tabDef.trim().split(":", 3);
                if (parts.length == 3) {
                    String contentId = parts[2].trim();
                    builder.addTab(parts[0].trim(), parts[1].trim(), contentId.isEmpty() ? null : contentId);
                } else if (parts.length == 2) {
                    builder.addTab(parts[0].trim(), parts[1].trim());
                } else if (parts.length == 1 && !parts[0].isEmpty()) {
                    // Use the same value for id and label
                    builder.addTab(parts[0].trim(), parts[0].trim());
                }
            }
        }

        // Parse child button elements for tab definitions
        Elements buttons = element.select("> button, > a");
        for (Element button : buttons) {
            String tabId = button.hasAttr("data-tab") ? button.attr("data-tab") : button.attr("id");
            String label = button.text().trim();
            String contentId = button.hasAttr("data-tab-content") ? button.attr("data-tab-content") : null;

            if (tabId != null && !tabId.isEmpty() && !label.isEmpty()) {
                if (contentId != null && !contentId.isBlank()) {
                    builder.addTab(tabId, label, contentId.trim());
                } else {
                    builder.addTab(tabId, label);
                }

                // Check if this tab is marked as active/selected
                if ((button.hasClass("active") || button.hasClass("selected")) && selectedTabId == null) {
                    selectedTabId = tabId;
                }
            }
        }

        if (selectedTabId == null && !builder.getTabs().isEmpty()) {
            selectedTabId = builder.getTabs().get(0).id();
        }

        if (selectedTabId != null && !selectedTabId.isBlank()) {
            builder.withSelectedTab(selectedTabId);
            applyTabContentVisibility(element, builder.getTabs(), selectedTabId);
        }

        // Apply tab spacing if specified
        if (element.hasAttr("data-tab-spacing")) {
            ParseUtils.parseInt(element.attr("data-tab-spacing"))
                    .ifPresent(builder::withTabSpacing);
        }

        return builder;
    }

    private void applyTabContentVisibility(Element navElement, List<TabNavigationBuilder.Tab> tabs, String selectedTabId) {
        if (selectedTabId == null || selectedTabId.isBlank()) {
            return;
        }
        var doc = navElement.ownerDocument();
        if (doc == null) {
            return;
        }
        for (TabNavigationBuilder.Tab tab : tabs) {
            String contentId = tab.contentId();
            if (contentId == null || contentId.isBlank()) {
                continue;
            }
            Element content = doc.getElementById(contentId);
            if (content == null) {
                continue;
            }
            boolean isSelected = tab.id().equals(selectedTabId);
            String updatedStyle = upsertStyleProperty(content.attr("style"), "visibility", isSelected ? "shown" : "hidden");
            content.attr("style", updatedStyle);
        }
    }

    private String upsertStyleProperty(String styleAttr, String property, String value) {
        String normalizedProperty = property.toLowerCase();
        String style = styleAttr == null ? "" : styleAttr.trim();
        StringBuilder sb = new StringBuilder();
        boolean replaced = false;

        if (!style.isEmpty()) {
            String[] declarations = style.split(";");
            for (String declaration : declarations) {
                String trimmed = declaration.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                String[] parts = trimmed.split(":", 2);
                if (parts.length != 2) {
                    continue;
                }
                String key = parts[0].trim().toLowerCase();
                String val = parts[1].trim();
                if (key.equals(normalizedProperty)) {
                    if (!replaced) {
                        appendStyle(sb, property, value);
                        replaced = true;
                    }
                } else {
                    appendStyle(sb, parts[0].trim(), val);
                }
            }
        }

        if (!replaced) {
            appendStyle(sb, property, value);
        }

        return sb.toString();
    }

    private void appendStyle(StringBuilder sb, String property, String value) {
        if (sb.length() > 0) {
            sb.append("; ");
        }
        sb.append(property).append(": ").append(value);
    }
}
