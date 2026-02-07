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
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventActions;
import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import java.util.Set;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for ItemSlotButton UI elements.
 */
public class ItemSlotButtonBuilder extends UIElementBuilder<ItemSlotButtonBuilder> 
        implements LayoutModeSupported<ItemSlotButtonBuilder> {
    private String itemId;
    private String layoutMode;

    public ItemSlotButtonBuilder() {
        super(UIElements.ITEM_SLOT_BUTTON, "#HyUIItemSlotButton");
        withUiFile("Pages/Elements/ItemSlotButton.ui");
        withWrappingGroup(true);
    }

    public static ItemSlotButtonBuilder itemSlotButton() {
        return new ItemSlotButtonBuilder();
    }

    public ItemSlotButtonBuilder withLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    @Override
    public String getLayoutMode() {
        return this.layoutMode;
    }

    public ItemSlotButtonBuilder addEventListener(CustomUIEventBindingType type, Consumer<Void> callback) {
        return addEventListener(type, Void.class, callback);
    }

    public ItemSlotButtonBuilder addEventListener(CustomUIEventBindingType type, BiConsumer<Void, UIContext> callback) {
        return addEventListenerWithContext(type, Void.class, callback);
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

        applyLayoutMode(commands, selector);
        
        if (layoutMode != null) {
            HyUIPlugin.getLog().logFinest("Setting LayoutMode: " + layoutMode + " for " + selector);
            commands.set(selector + ".LayoutMode", layoutMode);
        }

        listeners.forEach(listener -> {
            if (listener.type() == CustomUIEventBindingType.Activating) {
                String eventId = getEffectiveId();
                HyUIPlugin.getLog().logFinest("Adding Activating event binding: " + eventId + " for " + selector);
                events.addEventBinding(CustomUIEventBindingType.Activating, selector,
                        EventData.of("Action", UIEventActions.BUTTON_CLICKED)
                                .append("Target", eventId),
                        false);
            }
        });
    }
}
