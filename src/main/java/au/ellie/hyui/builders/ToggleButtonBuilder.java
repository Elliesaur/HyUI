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
import au.ellie.hyui.events.UIEventListener;
import au.ellie.hyui.types.ButtonStyle;
import au.ellie.hyui.utils.ParseUtils;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBinding;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for ToggleButton UI elements.
 */
public class ToggleButtonBuilder extends UIElementBuilder<ToggleButtonBuilder> {
    private Boolean isChecked;

    public ToggleButtonBuilder() {
        super(UIElements.TOGGLE_BUTTON, "#HyUIToggleButton");
        withUiFile("Pages/Elements/ToggleButton.ui");
        withWrappingGroup(true);
        this.initialValue = false;
    }

    
    public static ToggleButtonBuilder toggleButton() {
        return new ToggleButtonBuilder();
    }

    public ToggleButtonBuilder withCheckedStyle(ButtonStyle style) {
        return withSecondaryStyle("CheckedStyle", style);
    }

    public ToggleButtonBuilder withIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
        this.initialValue = isChecked;
        return this;
    }

    /**
     * Adds an event listener for the ValueChanged event (toggle state change).
     */
    public ToggleButtonBuilder onValueChanged(Consumer<Boolean> callback) {
        return addEventListener(CustomUIEventBindingType.ValueChanged, Boolean.class, callback);
    }

    /**
     * Adds an event listener for the ValueChanged event with context.
     */
    public ToggleButtonBuilder onValueChanged(BiConsumer<Boolean, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.ValueChanged, Boolean.class, callback);
    }

    /**
     * Adds an event listener for the Activating event.
     */
    public ToggleButtonBuilder onActivating(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.Activating, Void.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the Activating event with context.
     */
    public ToggleButtonBuilder onActivating(BiConsumer<Void, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.Activating, Void.class, callback);
    }

    /**
     * Adds an event listener for the DoubleClicking event.
     */
    public ToggleButtonBuilder onDoubleClicking(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.DoubleClicking, Void.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the RightClicking event.
     */
    public ToggleButtonBuilder onRightClicking(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.RightClicking, Void.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the MouseEntered event.
     */
    public ToggleButtonBuilder onMouseEntered(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.MouseEntered, Void.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the MouseExited event.
     */
    public ToggleButtonBuilder onMouseExited(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.MouseExited, Void.class, v -> callback.run());
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
                Set.of("Background")
        );
    }

    @Override
    protected boolean usesRefValue() {
        return true;
    }

    @Override
    protected void applyRuntimeValue(Object value) {
        if (value != null) {
            var next = Boolean.parseBoolean(String.valueOf(value));
            this.isChecked = next;
            this.initialValue = next;
        }
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (isChecked != null) {
            HyUIPlugin.getLog().logFinest("Setting IsChecked: " + isChecked + " for " + selector);
            commands.set(selector + ".Value", isChecked);
        }

        if (listeners.isEmpty()) {
            // To handle data back to the .getValue, we need to add at least one listener.
            listeners.add(new UIEventListener<Boolean>(CustomUIEventBindingType.ValueChanged, (Boolean v, UIContext ctx) -> {}));
        }

        // Register event listeners
        listeners.forEach(listener -> {
            String eventId = getEffectiveId();
            if (listener.type() == CustomUIEventBindingType.ValueChanged) {
                HyUIPlugin.getLog().logFinest("Adding ValueChanged event binding for " + selector + " with eventId: " + eventId);
                events.addEventBinding(CustomUIEventBindingType.ValueChanged, selector,
                        EventData.of("@ValueBool", selector + ".Value")
                            .append("Target", eventId)
                            .append("Action", UIEventActions.VALUE_CHANGED), false);
            } else if (listener.type() == CustomUIEventBindingType.Activating) {
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
