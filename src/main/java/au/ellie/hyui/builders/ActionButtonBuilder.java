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
import au.ellie.hyui.types.ActionButtonAlignment;
import au.ellie.hyui.types.ButtonStyle;
import au.ellie.hyui.types.LayoutMode;
import au.ellie.hyui.utils.PropertyBatcher;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Builder for creating ActionButton UI elements.
 */
public class ActionButtonBuilder extends UIElementBuilder<ActionButtonBuilder> {
    private Boolean disabled;
    private String keyBindingLabel;
    private String bindingModifier1Label;
    private String bindingModifier2Label;
    private Boolean isAvailable;
    private Boolean isHoldBinding;
    private ActionButtonAlignment alignment;
    private LayoutMode layoutMode;
    private ButtonStyle buttonStyle;
    private String actionName;

    public ActionButtonBuilder() {
        super(UIElements.ACTION_BUTTON, "#HyUIActionButton");
        withUiFile("Pages/Elements/ActionButton.ui");
        withWrappingGroup(true);
    }

    public static ActionButtonBuilder actionButton() {
        return new ActionButtonBuilder();
    }

    public ActionButtonBuilder withDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public ActionButtonBuilder withKeyBindingLabel(String keyBindingLabel) {
        this.keyBindingLabel = keyBindingLabel;
        return this;
    }

    public ActionButtonBuilder withBindingModifier1Label(String bindingModifier1Label) {
        this.bindingModifier1Label = bindingModifier1Label;
        return this;
    }

    public ActionButtonBuilder withBindingModifier2Label(String bindingModifier2Label) {
        this.bindingModifier2Label = bindingModifier2Label;
        return this;
    }

    public ActionButtonBuilder withIsAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
        return this;
    }

    public ActionButtonBuilder withIsHoldBinding(boolean isHoldBinding) {
        this.isHoldBinding = isHoldBinding;
        return this;
    }

    public ActionButtonBuilder withAlignment(ActionButtonAlignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public ActionButtonBuilder withLayoutMode(LayoutMode layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    public ActionButtonBuilder withStyle(ButtonStyle buttonStyle) {
        this.buttonStyle = buttonStyle;
        return this;
    }

    public ActionButtonBuilder withActionName(String actionName) {
        this.actionName = actionName;
        return this;
    }

    /**
     * Adds an event listener for the Activating event.
     */
    public ActionButtonBuilder onActivating(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.Activating, Void.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the Activating event with context.
     */
    public ActionButtonBuilder onActivating(BiConsumer<Void, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.Activating, Void.class, callback);
    }

    /**
     * Adds an event listener for the DoubleClicking event.
     */
    public ActionButtonBuilder onDoubleClicking(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.DoubleClicking, Void.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the RightClicking event.
     */
    public ActionButtonBuilder onRightClicking(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.RightClicking, Void.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the MouseEntered event.
     */
    public ActionButtonBuilder onMouseEntered(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.MouseEntered, Void.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the MouseExited event.
     */
    public ActionButtonBuilder onMouseExited(Runnable callback) {
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
                StylePropertySets.PATCH_STYLE,
                StylePropertySets.PADDING,
                StylePropertySets.ANCHOR,
                StylePropertySets.LABEL_STYLE,
                StylePropertySets.SOUND_STYLE,
                Set.of("Background")
        );
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (actionName != null) {
            HyUIPlugin.getLog().logFinest("Setting ActionName: " + actionName + " for " + selector);
            commands.set(selector + ".ActionName", actionName);
        }
        if (keyBindingLabel != null) {
            HyUIPlugin.getLog().logFinest("Setting KeyBindingLabel: " + keyBindingLabel + " for " + selector);
            commands.set(selector + ".KeyBindingLabel", keyBindingLabel);
        }
        if (bindingModifier1Label != null) {
            HyUIPlugin.getLog().logFinest("Setting BindingModifier1Label: " + bindingModifier1Label + " for " + selector);
            commands.set(selector + ".BindingModifier1Label", bindingModifier1Label);
        }
        if (bindingModifier2Label != null) {
            HyUIPlugin.getLog().logFinest("Setting BindingModifier2Label: " + bindingModifier2Label + " for " + selector);
            commands.set(selector + ".BindingModifier2Label", bindingModifier2Label);
        }
        if (isAvailable != null) {
            HyUIPlugin.getLog().logFinest("Setting IsAvailable: " + isAvailable + " for " + selector);
            commands.set(selector + ".IsAvailable", isAvailable);
        }
        if (isHoldBinding != null) {
            HyUIPlugin.getLog().logFinest("Setting IsHoldBinding: " + isHoldBinding + " for " + selector);
            commands.set(selector + ".IsHoldBinding", isHoldBinding);
        }
        if (alignment != null) {
            HyUIPlugin.getLog().logFinest("Setting Alignment: " + alignment + " for " + selector);
            commands.set(selector + ".Alignment", alignment.name());
        }
        if (layoutMode != null) {
            HyUIPlugin.getLog().logFinest("Setting LayoutMode: " + layoutMode + " for " + selector);
            commands.set(selector + ".LayoutMode", layoutMode.name());
        }
        if (disabled != null) {
            HyUIPlugin.getLog().logFinest("Setting Disabled: " + disabled + " for " + selector);
            commands.set(selector + ".Disabled", disabled);
        }
        if (buttonStyle != null) {
            HyUIPlugin.getLog().logFinest("Setting Style: " + buttonStyle + " for " + selector);
            PropertyBatcher.endSet(selector + ".Style", filterStyleDocument(buttonStyle.toBsonDocument()), commands);
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
