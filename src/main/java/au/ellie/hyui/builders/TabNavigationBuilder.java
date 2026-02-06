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
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.theme.Theme;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Builder for creating tab navigation UI elements.
 * Creates a horizontal row of tab buttons for navigation between different content sections.
 *
 * Example usage:
 * TabNavigationBuilder.tabNavigation()
 *     .withId("main-tabs")
 *     .addTab("inventory", "Inventory")
 *     .addTab("stats", "Statistics", "stats-content")
 *     .addTab("settings", "Settings")
 *     .withSelectedTab("inventory")
 */
public class TabNavigationBuilder extends UIElementBuilder<TabNavigationBuilder>
        implements LayoutModeSupported<TabNavigationBuilder>, BackgroundSupported<TabNavigationBuilder> {

    public record Tab(String id, String label, String contentId, boolean selected, UIElementBuilder<?> buttonBuilder) {
        public Tab withSelected(boolean selected) {
            return new Tab(id, label, contentId, selected, buttonBuilder);
        }

        public Tab withContentId(String contentId) {
            return new Tab(id, label, contentId, selected, buttonBuilder);
        }
    }

    private final List<Tab> tabs = new ArrayList<>();
    private String selectedTabId;
    private String layoutMode = "Left";
    private HyUIPatchStyle background;
    private HyUIStyle selectedTabStyle;
    private HyUIStyle unselectedTabStyle;
    private int tabSpacing = 0;
    private int tabsVersion = 0;
    private int lastBuiltTabsVersion = -1;
    private final List<UIElementBuilder<?>> tabButtons = new ArrayList<>();
    public TabNavigationBuilder() {
        super(UIElements.GROUP, "Group");
    }

    public TabNavigationBuilder(Theme theme) {
        super(theme, UIElements.GROUP, "Group");
    }

    /**
     * Factory method to create a new TabNavigationBuilder.
     */
    public static TabNavigationBuilder tabNavigation() {
        return new TabNavigationBuilder();
    }

    /**
     * Adds a tab to the navigation.
     *
     * @param id    Unique identifier for the tab (used in events)
     * @param label Display text for the tab button
     */
    public TabNavigationBuilder addTab(String id, String label) {
        tabs.add(new Tab(id, label, null, false, null));
        markTabsDirty();
        return this;
    }

    /**
     * Adds a tab to the navigation with a linked content element ID.
     *
     * @param id        Unique identifier for the tab (used in events)
     * @param label     Display text for the tab button
     * @param contentId ID of the content element to show when selected
     */
    public TabNavigationBuilder addTab(String id, String label, String contentId) {
        tabs.add(new Tab(id, label, contentId, false, null));
        markTabsDirty();
        return this;
    }

    /**
     * Adds a tab to the navigation with a custom button builder.
     *
     * @param id            Unique identifier for the tab (used in events)
     * @param label         Display text for the tab button
     * @param buttonBuilder Custom button builder to extend for this tab
     */
    public TabNavigationBuilder addTab(String id, String label, UIElementBuilder<?> buttonBuilder) {
        tabs.add(new Tab(id, label, null, false, buttonBuilder));
        markTabsDirty();
        return this;
    }

    /**
     * Adds a tab to the navigation with a linked content element ID and custom button builder.
     *
     * @param id            Unique identifier for the tab (used in events)
     * @param label         Display text for the tab button
     * @param contentId     ID of the content element to show when selected
     * @param buttonBuilder Custom button builder to extend for this tab
     */
    public TabNavigationBuilder addTab(String id, String label, String contentId, UIElementBuilder<?> buttonBuilder) {
        tabs.add(new Tab(id, label, contentId, false, buttonBuilder));
        markTabsDirty();
        return this;
    }

    /**
     * Sets which tab is currently selected.
     */
    public TabNavigationBuilder withSelectedTab(String tabId) {
        this.selectedTabId = tabId;
        return this;
    }

    /**
     * Sets the style for selected tabs.
     */
    public TabNavigationBuilder withSelectedTabStyle(HyUIStyle style) {
        this.selectedTabStyle = style;
        return this;
    }

    /**
     * Sets the style for unselected tabs.
     */
    public TabNavigationBuilder withUnselectedTabStyle(HyUIStyle style) {
        this.unselectedTabStyle = style;
        return this;
    }

    /**
     * Sets spacing between tabs (in pixels).
     */
    public TabNavigationBuilder withTabSpacing(int spacing) {
        this.tabSpacing = spacing;
        return this;
    }

    /**
     * Gets all tabs.
     */
    public List<Tab> getTabs() {
        return new ArrayList<>(tabs);
    }

    /**
     * Gets all tabs.
     */
    public List<Tab> getAllTabs() {
        return getTabs();
    }

    /**
     * Gets a tab by its ID.
     */
    public Tab getTab(String tabId) {
        if (tabId == null) {
            return null;
        }
        for (Tab tab : tabs) {
            if (tab.id().equals(tabId)) {
                return tab;
            }
        }
        return null;
    }

    /**
     * Replaces a tab in the list by ID.
     */
    public TabNavigationBuilder updateTab(String tabId, Tab updatedTab) {
        if (tabId == null || updatedTab == null) {
            return this;
        }
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            if (tab.id().equals(tabId)) {
                tabs.set(i, updatedTab);
                if (tabId.equals(selectedTabId) && updatedTab.id() != null) {
                    selectedTabId = updatedTab.id();
                }
                markTabsDirty();
                return this;
            }
        }
        return this;
    }

    /**
     * Removes a tab by its ID.
     */
    public TabNavigationBuilder removeTab(String tabId) {
        if (tabId == null) {
            return this;
        }
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            if (tab.id().equals(tabId)) {
                tabs.remove(i);
                if (tabId.equals(selectedTabId)) {
                    selectedTabId = tabs.isEmpty() ? null : tabs.get(0).id();
                }
                markTabsDirty();
                return this;
            }
        }
        return this;
    }

    public boolean hasTab(String tabId) {
        if (tabId == null) {
            return false;
        }
        for (Tab tab : tabs) {
            if (tab.id().equals(tabId)) {
                return true;
            }
        }
        return false;
    }

    public void linkTabContent(String tabId, String contentId) {
        if (tabId == null || contentId == null) {
            return;
        }
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            if (tab.id().equals(tabId)) {
                if (tab.contentId() == null || tab.contentId().isBlank()) {
                    tabs.set(i, tab.withContentId(contentId));
                    markTabsDirty();
                }
                return;
            }
        }
    }

    /**
     * Gets the currently selected tab ID.
     */
    public String getSelectedTabId() {
        return selectedTabId;
    }

    @Override
    public TabNavigationBuilder withLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    @Override
    public String getLayoutMode() {
        return this.layoutMode;
    }

    @Override
    public TabNavigationBuilder withBackground(HyUIPatchStyle background) {
        this.background = background;
        return this;
    }

    @Override
    public HyUIPatchStyle getBackground() {
        return this.background;
    }

    @Override
    protected boolean supportsStyling() {
        return false;
    }

    @Override
    protected boolean preserveChildrenOnTemplateMerge() {
        return true;
    }


    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        applyLayoutMode(commands, selector);

        if ((selectedTabId == null || !hasTab(selectedTabId)) && !tabs.isEmpty()) {
            selectedTabId = tabs.get(0).id();
        }
        
        // TODO Proper hash check on objects.
        boolean tabButtonsMissing = tabsVersion > lastBuiltTabsVersion || (tabButtons.isEmpty() && children.isEmpty()) || tabButtons.size() != children.size();
        if (tabButtonsMissing) {
            clearTabButtons();
        } else {
            return;
        }

        // Only create tab buttons once, we're dealing with builders here, not raw set commands.
        lastBuiltTabsVersion = tabsVersion;

        // Create tab buttons as children
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            boolean isSelected = tab.id().equals(selectedTabId);

            UIElementBuilder<?> tabButton = tab.buttonBuilder() != null
                    ? tab.buttonBuilder()
                    : ButtonBuilder.secondaryTextButton();

            tabButton.withId(tab.id());
            applyTabButtonText(tabButton, tab.label());
            tabButton.addEventListenerWithContext(CustomUIEventBindingType.Activating, Void.class, (_, ctx) -> {
                applyTabSelection(ctx, tab.id());
                ctx.updatePage(true);
            });

            if (tab.buttonBuilder() == null) {
                tabButton.withFlexWeight(1);
            }

            // Apply selected/unselected styling
            applyTabButtonStyle(tabButton, isSelected);

            this.addChild(tabButton);
            tabButtons.add(tabButton);

            HyUIPlugin.getLog().logFinest("Added tab: " + tab.id() + " (selected: " + isSelected + ")");
        }
    }

    private void applyTabSelection(UIContext ctx, String tabId) {
        this.selectedTabId = tabId;
        for (Tab tab : tabs) {
            boolean isSelected = tab.id().equals(tabId);
            ctx.getById(tab.id(), ButtonBuilder.class).ifPresent(button -> {
                HyUIStyle style = isSelected
                        ? selectedTabStyle != null ? selectedTabStyle : defaultSelectedStyle()
                        : unselectedTabStyle != null ? unselectedTabStyle : defaultUnselectedStyle();
                button.withStyle(style);
            });
            String contentId = tab.contentId();
            if (contentId != null && !contentId.isBlank()) {
                ctx.getById(contentId, TabContentBuilder.class).ifPresent(content -> content.withVisible(isSelected));
            }
        }
    }

    public void applySelectionState(UIContext ctx) {
        if (ctx == null) {
            return;
        }
        String tabId = selectedTabId;
        if (tabId == null || tabId.isBlank() || !hasTab(tabId)) {
            if (!tabs.isEmpty()) {
                tabId = tabs.get(0).id();
                selectedTabId = tabId;
            }
        }
        if (tabId != null && !tabId.isBlank()) {
            applyTabSelection(ctx, tabId);
        }
    }

    /**
     * Creates a default style for selected tabs (primary button)
     */
    public static HyUIStyle defaultSelectedStyle() {
        return new HyUIStyle()
                .withStyleReference("Common.ui", "DefaultTextButtonStyle");
    }

    /**
     * Creates a default style for unselected tabs (secondary button).
     */
    public static HyUIStyle defaultUnselectedStyle() {
        return new HyUIStyle()
                .withStyleReference("Common.ui", "SecondaryTextButtonStyle");
    }

    private void applyTabButtonText(UIElementBuilder<?> button, String text) {
        if (button instanceof ButtonBuilder buttonBuilder) {
            buttonBuilder.withText(text);
        } else if (button instanceof CustomButtonBuilder customButton) {
            customButton.withText(text);
        }
    }

    private void applyTabButtonStyle(UIElementBuilder<?> button, boolean isSelected) {
        if (button instanceof ButtonBuilder buttonBuilder) {
            if (isSelected) {
                buttonBuilder.withStyle(selectedTabStyle != null ? selectedTabStyle : defaultSelectedStyle());
            } else {
                buttonBuilder.withStyle(unselectedTabStyle != null ? unselectedTabStyle : defaultUnselectedStyle());
            }
        }
    }

    private void markTabsDirty() {
        tabsVersion++;
    }

    private void clearTabButtons() {
        if (tabButtons.isEmpty()) {
            return;
        }
        children.removeAll(tabButtons);
        tabButtons.clear();
    }
}
