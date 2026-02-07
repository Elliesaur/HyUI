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
import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import java.util.Set;

/**
 * Builder for TabButton UI elements.
 */
public class TabButtonBuilder extends UIElementBuilder<TabButtonBuilder> {
    private String tabId;
    private HyUIPatchStyle icon;
    private HyUIPatchStyle iconSelected;

    public TabButtonBuilder() {
        super(UIElements.TAB_BUTTON, "#HyUITabButton");
        withWrappingGroup(true);
    }

    public static TabButtonBuilder tabButton() {
        return new TabButtonBuilder();
    }

    public TabButtonBuilder withTabId(String tabId) {
        this.tabId = tabId;
        return this;
    }

    public TabButtonBuilder withIcon(HyUIPatchStyle icon) {
        this.icon = icon;
        return this;
    }

    public TabButtonBuilder withIconSelected(HyUIPatchStyle iconSelected) {
        this.iconSelected = iconSelected;
        return this;
    }

    @Override
    protected boolean supportsStyling() {
        return true;
    }

    @Override
    protected boolean isStyleWhitelist() {
        return true;
    }

    @Override
    protected Set<String> getSupportedStyleProperties() {
        return StylePropertySets.merge(
                StylePropertySets.ANCHOR,
                StylePropertySets.PADDING,
                StylePropertySets.PATCH_STYLE,
                StylePropertySets.SOUND_STYLE,
                StylePropertySets.LABEL_STYLE,
                Set.of("Background")
        );
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (tabId != null) {
            HyUIPlugin.getLog().logFinest("Setting Id: " + tabId + " for " + selector);
            commands.set(selector + ".Id", tabId);
        }
        if (icon != null) {
            HyUIPlugin.getLog().logFinest("Setting Icon for " + selector);
            commands.setObject(selector + ".Icon", icon.getHytalePatchStyle());
        }
        if (iconSelected != null) {
            HyUIPlugin.getLog().logFinest("Setting IconSelected for " + selector);
            commands.setObject(selector + ".IconSelected", iconSelected.getHytalePatchStyle());
        }
    }
}
