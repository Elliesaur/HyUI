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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for creating dropdown box UI elements.
 */
public class DropdownBoxBuilder extends UIElementBuilder<DropdownBoxBuilder> {
    private String value;
    private Boolean allowUnselection;
    private Integer maxSelection;
    private Integer entryHeight;
    private Boolean showLabel;
    private java.util.List<DropdownEntryInfo> entries = new java.util.ArrayList<>();

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
    protected boolean usesRefValue() {
        return true;
    }

    @Override
    protected boolean supportsStyling() {
        return true;
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (value != null) {
            commands.set(selector + ".Value", value);
        }
        if (allowUnselection != null) {
            commands.set(selector + ".AllowUnselection", allowUnselection);
        }
        if (maxSelection != null) {
            commands.set(selector + ".MaxSelection", maxSelection);
        }
        if (entryHeight != null) {
            commands.set(selector + ".EntryHeight", entryHeight);
        }
        if (showLabel != null) {
            commands.set(selector + ".ShowLabel", showLabel);
        }
        if (!entries.isEmpty()) {
            commands.set(selector + ".Entries", entries);
        }

        // TODO: Verify this actually works.
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
