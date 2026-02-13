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
import au.ellie.hyui.events.SelectedTabChangedEventData;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventActions;
import au.ellie.hyui.types.NativeTab;
import au.ellie.hyui.utils.PropertyBatcher;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import org.bson.BsonArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for native Hytale TabNavigation UI elements.
 * This is the native Hytale TabNavigation, not the custom HyUI tab system.
 */
public class NativeTabNavigationBuilder extends UIElementBuilder<NativeTabNavigationBuilder> {
    private String selectedTab;
    private Boolean allowUnselection;
    private final List<NativeTab> tabs = new ArrayList<>();

    public NativeTabNavigationBuilder() {
        super("TabNavigation", "#HyUINativeTabNavigation");
        withUiFile("Pages/Elements/NativeTabNavigation.ui");
        withWrappingGroup(true);
    }

    public static NativeTabNavigationBuilder nativeTabNavigation() {
        return new NativeTabNavigationBuilder();
    }

    public NativeTabNavigationBuilder withSelectedTab(String selectedTab) {
        this.selectedTab = selectedTab;
        return this;
    }

    public NativeTabNavigationBuilder withAllowUnselection(boolean allowUnselection) {
        this.allowUnselection = allowUnselection;
        return this;
    }

    public NativeTabNavigationBuilder addTab(NativeTab tab) {
        if (tab != null) {
            this.tabs.add(tab);
        }
        return this;
    }

    public NativeTabNavigationBuilder withTabs(List<NativeTab> tabs) {
        this.tabs.clear();
        if (tabs != null) {
            this.tabs.addAll(tabs);
        }
        return this;
    }

    /**
     * Adds an event listener for the SelectedTabChanged event.
     */
    public NativeTabNavigationBuilder onSelectedTabChanged(Consumer<SelectedTabChangedEventData> callback) {
        return addEventListener(CustomUIEventBindingType.SelectedTabChanged, SelectedTabChangedEventData.class, callback);
    }

    /**
     * Adds an event listener for the SelectedTabChanged event with context.
     */
    public NativeTabNavigationBuilder onSelectedTabChanged(BiConsumer<SelectedTabChangedEventData, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.SelectedTabChanged, SelectedTabChangedEventData.class, callback);
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
                StylePropertySets.LABEL_STYLE,
                StylePropertySets.SOUND_STYLE,
                StylePropertySets.TEXT_TOOLTIP_STYLE,
                Set.of(
                        "TabStyle",
                        "SelectedTabStyle",
                        "TabSounds",
                        "SeparatorAnchor",
                        "SeparatorBackground",
                        "Background",
                        "Overlay",
                        "IconAnchor",
                        "LabelStyle",
                        "Padding",
                        "Anchor",
                        "TooltipStyle",
                        "IconOpacity",
                        "FlexWeight",
                        "ContentMaskTexturePath",
                        "Default",
                        "Hovered",
                        "Pressed",
                        "Activate",
                        "MouseHover",
                        "Alignment",
                        "MaxWidth"
                )
        );
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (selectedTab != null) {
            HyUIPlugin.getLog().logFinest("Setting SelectedTab: " + selectedTab + " for " + selector);
            commands.set(selector + ".SelectedTab", selectedTab);
        }
        if (allowUnselection != null) {
            HyUIPlugin.getLog().logFinest("Setting AllowUnselection: " + allowUnselection + " for " + selector);
            commands.set(selector + ".AllowUnselection", allowUnselection);
        }

        if (!tabs.isEmpty()) {
            BsonArray tabArray = new BsonArray();
            for (NativeTab tab : tabs) {
                if (tab != null) {
                    tabArray.add(tab.toBsonDocument());
                }
            }
            PropertyBatcher.setBsonValue(selector + ".Tabs", tabArray, commands);
        }

        // Register event listeners
        listeners.forEach(listener -> {
            String eventId = getEffectiveId();
            if (listener.type() == CustomUIEventBindingType.SelectedTabChanged) {
                HyUIPlugin.getLog().logFinest("Adding SelectedTabChanged event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.SelectedTabChanged, selector,
                        EventData.of("Action", UIEventActions.SELECTED_TAB_CHANGED)
                            .append("Target", eventId), false);
            }
        });
    }
}
