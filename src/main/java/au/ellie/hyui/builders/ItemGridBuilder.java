package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.elements.BackgroundSupported;
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.elements.ScrollbarStyleSupported;
import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Builder for the ItemGrid UI element.
 */
public class ItemGridBuilder extends UIElementBuilder<ItemGridBuilder> implements
        LayoutModeSupported<ItemGridBuilder>,
        BackgroundSupported<ItemGridBuilder>,
        ScrollbarStyleSupported<ItemGridBuilder> {
    private String layoutMode;
    private String backgroundMode;
    private HyUIPatchStyle background;
    private String scrollbarStyleReference;
    private String scrollbarStyleDocument;
    private Boolean renderItemQualityBackground;
    private Boolean areItemsDraggable;
    private Boolean keepScrollPosition;
    private Boolean showScrollbar;
    private Integer slotsPerRow;
    private final List<ItemGridSlot> slots = new ArrayList<>();

    public ItemGridBuilder() {
        super(UIElements.ITEM_GRID, "#HyUIItemGrid");
        withWrappingGroup(true);
        withUiFile("Pages/Elements/ItemGrid.ui");
    }

    /**
     * Creates an ItemGridBuilder instance.
     *
     * @return an ItemGridBuilder configured for creating an item grid.
     */
    public static ItemGridBuilder itemGrid() {
        return new ItemGridBuilder();
    }

    @Override
    public ItemGridBuilder withLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    @Override
    public String getLayoutMode() {
        return this.layoutMode;
    }

    public ItemGridBuilder withBackgroundMode(String backgroundMode) {
        this.backgroundMode = backgroundMode;
        return this;
    }

    @Override
    public ItemGridBuilder withBackground(HyUIPatchStyle background) {
        this.background = background;
        return this;
    }

    @Override
    public HyUIPatchStyle getBackground() {
        return this.background;
    }

    @Override
    public ItemGridBuilder withScrollbarStyle(String document, String styleReference) {
        this.scrollbarStyleDocument = document;
        this.scrollbarStyleReference = styleReference;
        return this;
    }

    @Override
    public String getScrollbarStyleReference() {
        return this.scrollbarStyleReference;
    }

    @Override
    public String getScrollbarStyleDocument() {
        return this.scrollbarStyleDocument;
    }

    public ItemGridBuilder withRenderItemQualityBackground(boolean renderItemQualityBackground) {
        this.renderItemQualityBackground = renderItemQualityBackground;
        return this;
    }

    public ItemGridBuilder withAreItemsDraggable(boolean areItemsDraggable) {
        this.areItemsDraggable = areItemsDraggable;
        return this;
    }

    public ItemGridBuilder withKeepScrollPosition(boolean keepScrollPosition) {
        this.keepScrollPosition = keepScrollPosition;
        return this;
    }

    public ItemGridBuilder withShowScrollbar(boolean showScrollbar) {
        this.showScrollbar = showScrollbar;
        return this;
    }

    public ItemGridBuilder withSlotsPerRow(int slotsPerRow) {
        this.slotsPerRow = slotsPerRow;
        return this;
    }

    public ItemGridBuilder withSlots(List<ItemGridSlot> slots) {
        this.slots.clear();
        if (slots != null) {
            this.slots.addAll(slots);
        }
        return this;
    }

    public ItemGridBuilder addSlot(ItemGridSlot slot) {
        if (slot != null) {
            this.slots.add(slot);
        }
        return this;
    }

    @Override
    protected boolean supportsStyling() {
        return true;
    }
    
    @Override
    protected boolean isStyleWhitelist() { return true; }

    protected Set<String> getSupportedStyleProperties() {
        return Set.of(
                "SlotSpacing",
                "SlotSize",
                "SlotIconSize",
                "SlotBackground",
                "QuantityPopupSlotOverlay",
                "BrokenSlotBackgroundOverlay",
                "BrokenSlotIconOverlay",
                "DefaultItemIcon",
                "DurabilityBar",
                "DurabilityBarBackground",
                "DurabilityBarAnchor",
                "DurabilityBarColorStart",
                "DurabilityBarColorEnd",
                "CursedIconPatch",
                "CursedIconAnchor",
                "ItemStackHoveredSound",
                "ItemStackActivateSound"
        );
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        applyLayoutMode(commands, selector);
        applyBackground(commands, selector);
        applyScrollbarStyle(commands, selector);

        if (backgroundMode != null) {
            HyUIPlugin.getLog().logInfo("Setting BackgroundMode: " + backgroundMode + " for " + selector);
            commands.set(selector + ".BackgroundMode", backgroundMode);
        }
        if (renderItemQualityBackground != null) {
            HyUIPlugin.getLog().logInfo("Setting RenderItemQualityBackground: " + renderItemQualityBackground + " for " + selector);
            commands.set(selector + ".RenderItemQualityBackground", renderItemQualityBackground);
        }
        if (areItemsDraggable != null) {
            HyUIPlugin.getLog().logInfo("Setting AreItemsDraggable: " + areItemsDraggable + " for " + selector);
            commands.set(selector + ".AreItemsDraggable", areItemsDraggable);
        }
        if (keepScrollPosition != null) {
            HyUIPlugin.getLog().logInfo("Setting KeepScrollPosition: " + keepScrollPosition + " for " + selector);
            commands.set(selector + ".KeepScrollPosition", keepScrollPosition);
        }
        if (showScrollbar != null) {
            HyUIPlugin.getLog().logInfo("Setting ShowScrollbar: " + showScrollbar + " for " + selector);
            commands.set(selector + ".ShowScrollbar", showScrollbar);
        }
        if (slotsPerRow != null) {
            HyUIPlugin.getLog().logInfo("Setting SlotsPerRow: " + slotsPerRow + " for " + selector);
            commands.set(selector + ".SlotsPerRow", slotsPerRow);
        }
        if (!slots.isEmpty()) {
            HyUIPlugin.getLog().logInfo("Setting Slots for " + selector);
            commands.set(selector + ".Slots", slots);
        }

        listeners.forEach(listener -> {
            CustomUIEventBindingType type = listener.type();
            if (type == CustomUIEventBindingType.SlotClicking
                    || type == CustomUIEventBindingType.SlotDoubleClicking
                    || type == CustomUIEventBindingType.SlotMouseEntered
                    || type == CustomUIEventBindingType.SlotMouseExited
                    || type == CustomUIEventBindingType.DragCancelled
                    || type == CustomUIEventBindingType.Dropped
                    || type == CustomUIEventBindingType.SlotMouseDragCompleted
                    || type == CustomUIEventBindingType.SlotMouseDragExited
                    || type == CustomUIEventBindingType.SlotClickReleaseWhileDragging
                    || type == CustomUIEventBindingType.SlotClickPressWhileDragging) {
                String eventId = getEffectiveId();
                // Untested: cannot find server-side examples for item-grid slot events.
                events.addEventBinding(type, selector,
                        EventData.of("Action", type.name())
                                .append("Target", eventId),
                        false);
            }
        });
    }
}
