package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

/**
 * Builder for the ItemSlot UI element.
 */
public class ItemSlotBuilder extends UIElementBuilder<ItemSlotBuilder> {
    private String itemId;
    private Boolean showQualityBackground;
    private Boolean showQuantity;

    public ItemSlotBuilder() {
        super(UIElements.ITEM_SLOT, "#HyUIItemSlot");
        withWrappingGroup(true);
        withUiFile("Pages/Elements/ItemSlot.ui");
    }

    /**
     * Creates an ItemSlotBuilder instance.
     *
     * @return an ItemSlotBuilder configured for creating an item slot.
     */
    public static ItemSlotBuilder itemSlot() {
        return new ItemSlotBuilder();
    }

    /**
     * Sets the item ID for the item slot.
     *
     * @param itemId the item ID (e.g. Tool_Pickaxe_Crude).
     * @return the current instance for method chaining
     */
    public ItemSlotBuilder withItemId(String itemId) {
        this.itemId = itemId;
        return this;
    }

    /**
     * Sets whether the slot should render item quality background.
     *
     * @param showQualityBackground whether to show the item quality background
     * @return the current instance for method chaining
     */
    public ItemSlotBuilder withShowQualityBackground(boolean showQualityBackground) {
        this.showQualityBackground = showQualityBackground;
        return this;
    }

    /**
     * Sets whether the slot should render the item quantity.
     *
     * @param showQuantity whether to show the item quantity
     * @return the current instance for method chaining
     */
    public ItemSlotBuilder withShowQuantity(boolean showQuantity) {
        this.showQuantity = showQuantity;
        return this;
    }

    @Override
    protected boolean supportsStyling() {
        return false;
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (showQualityBackground != null) {
            HyUIPlugin.getLog().logInfo("Setting ShowQualityBackground: " + showQualityBackground + " for " + selector);
            commands.set(selector + ".ShowQualityBackground", showQualityBackground);
        }
        if (showQuantity != null) {
            HyUIPlugin.getLog().logInfo("Setting ShowQuantity: " + showQuantity + " for " + selector);
            commands.set(selector + ".ShowQuantity", showQuantity);
        }
        if (itemId != null) {
            HyUIPlugin.getLog().logInfo("Setting ItemId on " + selector + " to " + itemId);
            commands.set(selector + ".ItemId", itemId);
        }

        // Sadface.
/*        listeners.forEach(listener -> {
            if (listener.type() == CustomUIEventBindingType.Dropped) {
                String eventId = getEffectiveId();
                events.addEventBinding(CustomUIEventBindingType.Dropped, selector,
                        EventData.of("Action", CustomUIEventBindingType.Dropped.name())
                                .append("Target", eventId),
                        false);
            }
        });*/
    }
}
