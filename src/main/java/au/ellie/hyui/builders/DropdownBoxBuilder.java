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
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventActions;
import au.ellie.hyui.theme.Theme;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for creating dropdown box UI elements.
 */
public class DropdownBoxBuilder extends UIElementBuilder<DropdownBoxBuilder> {
    private String value;
    private List<String> selectedValues;
    private Boolean allowUnselection;
    private Integer maxSelection;
    private Integer entryHeight;
    private Boolean showLabel;
    private String noItemsText;
    private String panelTitleText;
    private Boolean disabled;
    private Boolean isReadOnly;
    private Boolean showSearchInput;
    private String forcedLabel;
    private Boolean displayNonExistingValue;
    private List<DropdownEntryInfo> entries = new ArrayList<>();

    public DropdownBoxBuilder() {
        super(UIElements.DROPDOWN_BOX, "#HyUIDropdownBox");
        withWrappingGroup(true);
        withUiFile("Pages/Elements/DropdownBox.ui");
    }

    public DropdownBoxBuilder(Theme theme) {
        super(theme, UIElements.DROPDOWN_BOX, "#HyUIDropdownBox");
        withWrappingGroup(true);
        withUiFile("Pages/Elements/DropdownBox.ui");
    }

    public static DropdownBoxBuilder dropdownBox() {
        return new DropdownBoxBuilder(Theme.GAME_THEME);
    }

    /**
     * Sets the initial selected value for the dropdown box.
     * 
     * WARNING: The value must correspond to the "name" of one of the entries added via {@link #addEntry} or {@link #withEntries}.
     * If the value does not exist in the entries, the dropdown may exhibit unexpected behavior or fail to show the selection.
     * 
     * @param value The name of the entry to select.
     * @return This builder instance for method chaining.
     */
    public DropdownBoxBuilder withValue(String value) {
        this.value = value;
        this.initialValue = value;
        return this;
    }

    public DropdownBoxBuilder withAllowUnselection(boolean allowUnselection) {
        this.allowUnselection = allowUnselection;
        return this;
    }

    public DropdownBoxBuilder withMaxSelection(int maxSelection) {
        this.maxSelection = maxSelection;
        return this;
    }

    public DropdownBoxBuilder withEntryHeight(int entryHeight) {
        this.entryHeight = entryHeight;
        return this;
    }

    public DropdownBoxBuilder withShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
        return this;
    }

    public DropdownBoxBuilder withNoItemsText(String noItemsText) {
        this.noItemsText = noItemsText;
        return this;
    }

    public DropdownBoxBuilder withPanelTitleText(String panelTitleText) {
        this.panelTitleText = panelTitleText;
        return this;
    }

    public DropdownBoxBuilder withSelectedValues(List<String> selectedValues) {
        this.selectedValues = selectedValues;
        return this;
    }

    public DropdownBoxBuilder withDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public DropdownBoxBuilder withIsReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
        return this;
    }

    public DropdownBoxBuilder withShowSearchInput(boolean showSearchInput) {
        this.showSearchInput = showSearchInput;
        return this;
    }

    public DropdownBoxBuilder withForcedLabel(String forcedLabel) {
        this.forcedLabel = forcedLabel;
        return this;
    }

    public DropdownBoxBuilder withDisplayNonExistingValue(boolean displayNonExistingValue) {
        this.displayNonExistingValue = displayNonExistingValue;
        return this;
    }

    public DropdownBoxBuilder withEntryLabelStyle(HyUIStyle style) {
        return withSecondaryStyle("EntryLabelStyle", style);
    }

    public DropdownBoxBuilder withSelectedEntryLabelStyle(HyUIStyle style) {
        return withSecondaryStyle("SelectedEntryLabelStyle", style);
    }

    public DropdownBoxBuilder withPopupStyle(HyUIStyle style) {
        return withSecondaryStyle("PopupStyle", style);
    }

    public DropdownBoxBuilder withEntries(java.util.List<DropdownEntryInfo> entries) {
        this.entries = new java.util.ArrayList<>(entries);
        return this;
    }

    public DropdownBoxBuilder addEntry(DropdownEntryInfo entry) {
        this.entries.add(entry);
        return this;
    }

    public DropdownBoxBuilder addEntry(String name, String label) {
        this.entries.add(new DropdownEntryInfo(LocalizableString.fromString(label), name));
        return this;
    }

    public DropdownBoxBuilder addEventListener(CustomUIEventBindingType type, Consumer<String> callback) {
        return addEventListener(type, String.class, callback);
    }

    public DropdownBoxBuilder addEventListener(CustomUIEventBindingType type, BiConsumer<String, UIContext> callback) {
        return addEventListenerWithContext(type, String.class, callback);
    }

    @Override
    protected void applyRuntimeValue(Object value) {
        if (value != null) {
            String next = String.valueOf(value);
            this.value = next;
            this.initialValue = next;
        }
    }

    @Override
    protected boolean usesRefValue() {
        return true;
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
    protected java.util.Set<String> getSupportedStyleProperties() {
        return StylePropertySets.merge(
                StylePropertySets.ANCHOR,
                StylePropertySets.PADDING,
                StylePropertySets.PATCH_STYLE,
                StylePropertySets.LABEL_STYLE,
                StylePropertySets.INPUT_FIELD_STYLE,
                StylePropertySets.INPUT_FIELD_ICON,
                StylePropertySets.INPUT_FIELD_BUTTON,
                StylePropertySets.SOUND_STYLE,
                StylePropertySets.SCROLLBAR_STYLE,
                Set.of(
                        "DefaultBackground",
                        "HoveredBackground",
                        "PressedBackground",
                        "DisabledBackground",
                        "DefaultArrowTexturePath",
                        "HoveredArrowTexturePath",
                        "PressedArrowTexturePath",
                        "DisabledArrowTexturePath",
                        "ArrowWidth",
                        "ArrowHeight",
                        "HorizontalPadding",
                        "PanelBackground",
                        "PanelPadding",
                        "PanelWidth",
                        "PanelAlign",
                        "PanelOffset",
                        "EntryHeight",
                        "EntryIconWidth",
                        "EntryIconHeight",
                        "EntriesInViewport",
                        "HorizontalEntryPadding",
                        "EntryIconBackground",
                        "SelectedEntryIconBackground",
                        "HoveredEntryBackground",
                        "PressedEntryBackground",
                        "FocusOutlineSize",
                        "FocusOutlineColor",
                        "IconTexturePath",
                        "IconWidth",
                        "IconHeight",
                        "PlaceholderText",
                        "Background"
                )
        );
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (value != null) {
            HyUIPlugin.getLog().logFinest("Setting Value: " + value + " for " + selector);
            commands.set(selector + ".Value", value);
        }
        if (allowUnselection != null) {
            HyUIPlugin.getLog().logFinest("Setting AllowUnselection: " + allowUnselection + " for " + selector);
            commands.set(selector + ".AllowUnselection", allowUnselection);
        }
        if (maxSelection != null) {
            HyUIPlugin.getLog().logFinest("Setting MaxSelection: " + maxSelection + " for " + selector);
            commands.set(selector + ".MaxSelection", maxSelection);
        }
        if (entryHeight != null) {
            HyUIPlugin.getLog().logFinest("Setting EntryHeight: " + entryHeight + " for " + selector);
            commands.set(selector + ".EntryHeight", entryHeight);
        }
        if (showLabel != null) {
            HyUIPlugin.getLog().logFinest("Setting ShowLabel: " + showLabel + " for " + selector);
            commands.set(selector + ".ShowLabel", showLabel);
        }
        if (noItemsText != null) {
            HyUIPlugin.getLog().logFinest("Setting NoItemsText: " + noItemsText + " for " + selector);
            commands.set(selector + ".NoItemsText", noItemsText);
        }
        if (panelTitleText != null) {
            HyUIPlugin.getLog().logFinest("Setting PanelTitleText: " + panelTitleText + " for " + selector);
            commands.set(selector + ".PanelTitleText", panelTitleText);
        }
        if (selectedValues != null) {
            HyUIPlugin.getLog().logFinest("Setting SelectedValues: " + selectedValues + " for " + selector);
            commands.set(selector + ".SelectedValues", selectedValues);
        }
        if (disabled != null) {
            HyUIPlugin.getLog().logFinest("Setting Disabled: " + disabled + " for " + selector);
            commands.set(selector + ".Disabled", disabled);
        }
        if (isReadOnly != null) {
            HyUIPlugin.getLog().logFinest("Setting IsReadOnly: " + isReadOnly + " for " + selector);
            commands.set(selector + ".IsReadOnly", isReadOnly);
        }
        if (showSearchInput != null) {
            HyUIPlugin.getLog().logFinest("Setting ShowSearchInput: " + showSearchInput + " for " + selector);
            commands.set(selector + ".ShowSearchInput", showSearchInput);
        }
        if (forcedLabel != null) {
            HyUIPlugin.getLog().logFinest("Setting ForcedLabel: " + forcedLabel + " for " + selector);
            commands.set(selector + ".ForcedLabel", forcedLabel);
        }
        if (displayNonExistingValue != null) {
            HyUIPlugin.getLog().logFinest("Setting DisplayNonExistingValue: " + displayNonExistingValue + " for " + selector);
            commands.set(selector + ".DisplayNonExistingValue", displayNonExistingValue);
        }
        if (!entries.isEmpty()) {
            HyUIPlugin.getLog().logFinest("Setting Entries for " + selector);
            commands.set(selector + ".Entries", entries);
        }
        if (listeners.isEmpty()) {
            // To handle data back to the .getValue, we need to add at least one listener.
            addEventListener(CustomUIEventBindingType.ValueChanged, (_, _) -> {});
        }
        listeners.forEach(listener -> {
            if (listener.type() == CustomUIEventBindingType.ValueChanged) {
                String eventId = getEffectiveId();
                events.addEventBinding(CustomUIEventBindingType.ValueChanged, selector,
                        EventData.of("@Value", selector + ".Value")
                                .append("Target", eventId)
                                .append("Action", UIEventActions.VALUE_CHANGED),
                        false);
            }
        });
    }
}
