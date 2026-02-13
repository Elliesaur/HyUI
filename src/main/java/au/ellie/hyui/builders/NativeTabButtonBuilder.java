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
import au.ellie.hyui.events.MouseEventData;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventActions;
import au.ellie.hyui.types.LayoutMode;
import au.ellie.hyui.utils.PropertyBatcher;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for native Hytale TabButton UI elements.
 * This is the native Hytale TabButton, not the custom HyUI tab system.
 */
public class NativeTabButtonBuilder extends UIElementBuilder<NativeTabButtonBuilder> {
    private String text;
    private LayoutMode layoutMode;
    private HyUIPatchStyle icon;
    private HyUIPatchStyle iconSelected;
    private String tabId;
    private Boolean disabled;

    public NativeTabButtonBuilder() {
        super("TabButton", "#HyUINativeTabButton");
        withUiFile("Pages/Elements/NativeTabButton.ui");
        withWrappingGroup(true);
    }

    public static NativeTabButtonBuilder nativeTabButton() {
        return new NativeTabButtonBuilder();
    }

    public NativeTabButtonBuilder withText(String text) {
        this.text = text;
        return this;
    }

    public NativeTabButtonBuilder withLayoutMode(LayoutMode layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    public NativeTabButtonBuilder withIcon(HyUIPatchStyle icon) {
        this.icon = icon;
        return this;
    }

    public NativeTabButtonBuilder withIconSelected(HyUIPatchStyle iconSelected) {
        this.iconSelected = iconSelected;
        return this;
    }

    @Override
    public NativeTabButtonBuilder withId(String id) {
        return (NativeTabButtonBuilder) super.withId(id);
    }

    public NativeTabButtonBuilder withTabId(String tabId) {
        this.tabId = tabId;
        return this;
    }

    public NativeTabButtonBuilder withDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    /**
     * Adds an event listener for the Activating event.
     */
    public NativeTabButtonBuilder onActivating(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.Activating, Void.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the Activating event with context.
     */
    public NativeTabButtonBuilder onActivating(BiConsumer<Void, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.Activating, Void.class, callback);
    }

    /**
     * Adds an event listener for the DoubleClicking event.
     */
    public NativeTabButtonBuilder onDoubleClicking(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.DoubleClicking, MouseEventData.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the DoubleClicking event.
     */
    public NativeTabButtonBuilder onDoubleClicking(Consumer<MouseEventData> callback) {
        return addEventListener(CustomUIEventBindingType.DoubleClicking, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the DoubleClicking event with context.
     */
    public NativeTabButtonBuilder onDoubleClicking(BiConsumer<MouseEventData, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.DoubleClicking, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the RightClicking event.
     */
    public NativeTabButtonBuilder onRightClicking(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.RightClicking, MouseEventData.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the RightClicking event.
     */
    public NativeTabButtonBuilder onRightClicking(Consumer<MouseEventData> callback) {
        return addEventListener(CustomUIEventBindingType.RightClicking, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the RightClicking event with context.
     */
    public NativeTabButtonBuilder onRightClicking(BiConsumer<MouseEventData, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.RightClicking, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the MouseEntered event.
     */
    public NativeTabButtonBuilder onMouseEntered(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.MouseEntered, MouseEventData.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the MouseEntered event.
     */
    public NativeTabButtonBuilder onMouseEntered(Consumer<MouseEventData> callback) {
        return addEventListener(CustomUIEventBindingType.MouseEntered, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the MouseEntered event with context.
     */
    public NativeTabButtonBuilder onMouseEntered(BiConsumer<MouseEventData, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.MouseEntered, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the MouseExited event.
     */
    public NativeTabButtonBuilder onMouseExited(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.MouseExited, MouseEventData.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the MouseExited event.
     */
    public NativeTabButtonBuilder onMouseExited(Consumer<MouseEventData> callback) {
        return addEventListener(CustomUIEventBindingType.MouseExited, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the MouseExited event with context.
     */
    public NativeTabButtonBuilder onMouseExited(BiConsumer<MouseEventData, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.MouseExited, MouseEventData.class, callback);
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

        if (text != null) {
            HyUIPlugin.getLog().logFinest("Setting Text: " + text + " for " + selector);
            commands.set(selector + ".Text", text);
        }
        if (layoutMode != null) {
            HyUIPlugin.getLog().logFinest("Setting LayoutMode: " + layoutMode + " for " + selector);
            commands.set(selector + ".LayoutMode", layoutMode.name());
        }
        if (icon != null) {
            HyUIPlugin.getLog().logFinest("Setting Icon for " + selector);
            commands.setObject(selector + ".Icon", icon.getHytalePatchStyle());
        }
        if (iconSelected != null) {
            HyUIPlugin.getLog().logFinest("Setting IconSelected for " + selector);
            commands.setObject(selector + ".IconSelected", iconSelected.getHytalePatchStyle());
        }
        String resolvedTabId = tabId != null ? tabId : getId();
        if (resolvedTabId != null) {
            HyUIPlugin.getLog().logFinest("Setting Id: " + resolvedTabId + " for " + selector);
            commands.set(selector + ".Id", resolvedTabId);
        }
        if (disabled != null) {
            HyUIPlugin.getLog().logFinest("Setting Disabled: " + disabled + " for " + selector);
            commands.set(selector + ".Disabled", disabled);
        }

        // Register event listeners
        listeners.forEach(listener -> {
            String eventId = getEffectiveId();
            if (listener.type() == CustomUIEventBindingType.Activating) {
                HyUIPlugin.getLog().logFinest("Adding Activating event binding: " + eventId + " for " + selector);
                events.addEventBinding(CustomUIEventBindingType.Activating, selector,
                        EventData.of("Action", UIEventActions.BUTTON_CLICKED)
                            .append("Target", eventId), false);
            } else if (listener.type() == CustomUIEventBindingType.DoubleClicking) {
                HyUIPlugin.getLog().logFinest("Adding DoubleClicking event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.DoubleClicking, selector,
                        EventData.of("Action", UIEventActions.DOUBLE_CLICKING)
                            .append("Target", eventId), false);
            } else if (listener.type() == CustomUIEventBindingType.RightClicking) {
                HyUIPlugin.getLog().logFinest("Adding RightClicking event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.RightClicking, selector,
                        EventData.of("Action", UIEventActions.RIGHT_CLICKING)
                            .append("Target", eventId), false);
            } else if (listener.type() == CustomUIEventBindingType.MouseEntered) {
                HyUIPlugin.getLog().logFinest("Adding MouseEntered event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.MouseEntered, selector,
                        EventData.of("Action", UIEventActions.MOUSE_ENTERED)
                            .append("Target", eventId), false);
            } else if (listener.type() == CustomUIEventBindingType.MouseExited) {
                HyUIPlugin.getLog().logFinest("Adding MouseExited event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.MouseExited, selector,
                        EventData.of("Action", UIEventActions.MOUSE_EXITED)
                            .append("Target", eventId), false);
            }
        });
    }
}
