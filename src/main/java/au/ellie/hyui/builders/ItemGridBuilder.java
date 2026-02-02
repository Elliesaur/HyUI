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
import au.ellie.hyui.elements.ScrollbarStyleSupported;
import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
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
    private static final Field ITEM_STACK_FIELD;
    private static final boolean ITEM_STACK_FIELD_AVAILABLE;

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

    /**
     * Retrieves an unmodifiable list of slots in the item grid.
     * @return An unmodifiable list of slots
     */
    public List<ItemGridSlot> getSlots() {
        return Collections.unmodifiableList(this.slots);
    }
    
    public ItemGridBuilder updateSlot(ItemGridSlot updatedSlot, Integer index) {
        if (updatedSlot == null || index == null || index < 0 || index >= this.slots.size()) {
            return this;
        }
        this.slots.set(index, updatedSlot);
        return this;
    }
    
    public ItemGridBuilder removeSlot(Integer index) {
        if (index == null || index < 0 || index >= this.slots.size()) {
            return this;
        }
        this.slots.remove(index.intValue());
        return this;
    }
    
    public ItemGridSlot getSlot(Integer index) {
        if (index == null || index < 0 || index >= this.slots.size()) {
            return null;
        }
        return this.slots.get(index);
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
            HyUIPlugin.getLog().logFinest("Setting BackgroundMode: " + backgroundMode + " for " + selector);
            commands.set(selector + ".BackgroundMode", backgroundMode);
        }
        if (renderItemQualityBackground != null) {
            HyUIPlugin.getLog().logFinest("Setting RenderItemQualityBackground: " + renderItemQualityBackground + " for " + selector);
            commands.set(selector + ".RenderItemQualityBackground", renderItemQualityBackground);
        }
        if (areItemsDraggable != null) {
            HyUIPlugin.getLog().logFinest("Setting AreItemsDraggable: " + areItemsDraggable + " for " + selector);
            commands.set(selector + ".AreItemsDraggable", areItemsDraggable);
            if (areItemsDraggable) {
                setAllSlotsActivatable();
            }
        }
        if (keepScrollPosition != null) {
            HyUIPlugin.getLog().logFinest("Setting KeepScrollPosition: " + keepScrollPosition + " for " + selector);
            commands.set(selector + ".KeepScrollPosition", keepScrollPosition);
        }
        if (showScrollbar != null) {
            HyUIPlugin.getLog().logFinest("Setting ShowScrollbar: " + showScrollbar + " for " + selector);
            commands.set(selector + ".ShowScrollbar", showScrollbar);
        }
        if (slotsPerRow != null) {
            HyUIPlugin.getLog().logFinest("Setting SlotsPerRow: " + slotsPerRow + " for " + selector);
            commands.set(selector + ".SlotsPerRow", slotsPerRow);
        }
        if (!slots.isEmpty()) {
            HyUIPlugin.getLog().logFinest("Setting Slots for " + selector);
            commands.set(selector + ".Slots", slots);
        }
        
        listeners.forEach(listener -> {
            CustomUIEventBindingType type = listener.type();
            if (type == CustomUIEventBindingType.Activating 
                    || type == CustomUIEventBindingType.RightClicking
                    || type == CustomUIEventBindingType.DoubleClicking
                    || type == CustomUIEventBindingType.MouseEntered
                    || type == CustomUIEventBindingType.MouseExited
                    || type == CustomUIEventBindingType.MouseButtonReleased
                    || type == CustomUIEventBindingType.ValueChanged
                    || type == CustomUIEventBindingType.ElementReordered
                    || type == CustomUIEventBindingType.Validating
                    || type == CustomUIEventBindingType.Dismissing
                    || type == CustomUIEventBindingType.FocusGained
                    || type == CustomUIEventBindingType.FocusLost
                    || type == CustomUIEventBindingType.KeyDown
                    || type == CustomUIEventBindingType.SelectedTabChanged
                )
                return;
            String eventId = getEffectiveId();
            HyUIPlugin.getLog().logFinest("Adding " + type.name());
            events.addEventBinding(type, selector,
                    EventData.of("Action", type.name())
                            .append("Target", eventId),
                    false);
        });
    }

    static {
        Field itemStackField = null;
        boolean available = false;
        try {
            itemStackField = ItemGridSlot.class.getDeclaredField("itemStack");
            itemStackField.setAccessible(true);
            available = true;
        } catch (NoSuchFieldException e) {
            HyUIPlugin.getLog().logFinest("ItemGridSlot.itemStack field not found; empty slots will still be activatable.");
        }
        ITEM_STACK_FIELD = itemStackField;
        ITEM_STACK_FIELD_AVAILABLE = available;
    }

    private void setAllSlotsActivatable() {
        for (var slot : slots) {
            if (!slot.isActivatable()) {
                slot.setActivatable(true);
            }
        }
    }

    // Might need it one day.
    public static ItemStack getItemStack(ItemGridSlot slot) {
        if (!ITEM_STACK_FIELD_AVAILABLE) {
            return null;
        }
        try {
            return (ItemStack)ITEM_STACK_FIELD.get(slot);
        } catch (IllegalAccessException e) {
            HyUIPlugin.getLog().logFinest("Unable to access ItemGridSlot.itemStack.");
            return null;
        }
    }
}
