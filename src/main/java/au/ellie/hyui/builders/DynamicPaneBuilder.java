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
import au.ellie.hyui.events.MouseEventData;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventActions;
import au.ellie.hyui.types.LayoutMode;
import au.ellie.hyui.types.ResizeType;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for DynamicPane UI elements.
 * Must be a child of DynamicPaneContainer.
 */
public class DynamicPaneBuilder extends UIElementBuilder<DynamicPaneBuilder> {
    private LayoutMode layoutMode;
    private Integer minSize;
    private ResizeType resizeAt;
    private Integer resizerSize;
    private HyUIPatchStyle resizerBackground;

    public DynamicPaneBuilder() {
        super("DynamicPane", "#HyUIDynamicPane");
    }

    public static DynamicPaneBuilder dynamicPane() {
        return new DynamicPaneBuilder();
    }

    public DynamicPaneBuilder withLayoutMode(LayoutMode layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    public DynamicPaneBuilder withMinSize(int minSize) {
        this.minSize = minSize;
        return this;
    }

    public DynamicPaneBuilder withResizeAt(ResizeType resizeAt) {
        this.resizeAt = resizeAt;
        return this;
    }

    public DynamicPaneBuilder withResizerSize(int resizerSize) {
        this.resizerSize = resizerSize;
        return this;
    }

    public DynamicPaneBuilder withResizerBackground(HyUIPatchStyle resizerBackground) {
        this.resizerBackground = resizerBackground;
        return this;
    }

    /**
     * Adds an event listener for the MouseButtonReleased event.
     */
    public DynamicPaneBuilder onMouseButtonReleased(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.MouseButtonReleased, MouseEventData.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the MouseButtonReleased event.
     */
    public DynamicPaneBuilder onMouseButtonReleased(Consumer<MouseEventData> callback) {
        return addEventListener(CustomUIEventBindingType.MouseButtonReleased, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the MouseButtonReleased event with context.
     */
    public DynamicPaneBuilder onMouseButtonReleased(BiConsumer<MouseEventData, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.MouseButtonReleased, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the Validating event.
     */
    public DynamicPaneBuilder onValidating(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.Validating, Void.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the Dismissing event.
     */
    public DynamicPaneBuilder onDismissing(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.Dismissing, Void.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the Scrolled event.
     */
    public DynamicPaneBuilder onScrolled(Consumer<Float> callback) {
        return addEventListener(CustomUIEventBindingType.ValueChanged, Float.class, callback);
    }

    /**
     * Adds an event listener for the Scrolled event with context.
     */
    public DynamicPaneBuilder onScrolled(BiConsumer<Float, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.ValueChanged, Float.class, callback);
    }

    @Override
    protected boolean supportsStyling() {
        return false;
    }

    @Override
    protected boolean isStyleWhitelist() {
        return true;
    }

    @Override
    protected Set<String> getSupportedStyleProperties() {
        return Set.of();
    }

    @Override
    protected boolean hasCustomInlineContent() {
        return true;
    }

    @Override
    protected String generateCustomInlineContent() {
        return "DynamicPane #" + getEffectiveId() + " { LayoutMode: Top; }";
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (layoutMode != null) {
            HyUIPlugin.getLog().logFinest("Setting LayoutMode: " + layoutMode + " for " + selector);
            commands.set(selector + ".LayoutMode", layoutMode.name());
        }
        if (minSize != null) {
            HyUIPlugin.getLog().logFinest("Setting MinSize: " + minSize + " for " + selector);
            commands.set(selector + ".MinSize", minSize);
        }
        if (resizeAt != null) {
            HyUIPlugin.getLog().logFinest("Setting ResizeAt: " + resizeAt + " for " + selector);
            commands.set(selector + ".ResizeAt", resizeAt.name());
        }
        if (resizerSize != null) {
            HyUIPlugin.getLog().logFinest("Setting ResizerSize: " + resizerSize + " for " + selector);
            commands.set(selector + ".ResizerSize", resizerSize);
        }
        if (resizerBackground != null) {
            HyUIPlugin.getLog().logFinest("Setting ResizerBackground for " + selector);
            commands.setObject(selector + ".ResizerBackground", resizerBackground.getHytalePatchStyle());
        }

        // Register event listeners
        listeners.forEach(listener -> {
            String eventId = getEffectiveId();
            if (listener.type() == CustomUIEventBindingType.MouseButtonReleased) {
                HyUIPlugin.getLog().logFinest("Adding MouseButtonReleased event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.MouseButtonReleased, selector,
                        EventData.of("Action", UIEventActions.MOUSE_BUTTON_RELEASED)
                            .append("Target", eventId), false);
            } else if (listener.type() == CustomUIEventBindingType.Validating) {
                HyUIPlugin.getLog().logFinest("Adding Validating event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.Validating, selector,
                        EventData.of("Action", UIEventActions.VALIDATING)
                            .append("Target", eventId), false);
            } else if (listener.type() == CustomUIEventBindingType.Dismissing) {
                HyUIPlugin.getLog().logFinest("Adding Dismissing event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.Dismissing, selector,
                        EventData.of("Action", UIEventActions.DISMISSING)
                            .append("Target", eventId), false);
            } else if (listener.type() == CustomUIEventBindingType.ValueChanged) {
                // Scrolled event uses ValueChanged type
                HyUIPlugin.getLog().logFinest("Adding Scrolled event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.ValueChanged, selector,
                        EventData.of("Action", UIEventActions.SCROLLED)
                            .append("Target", eventId), false);
            }
        });
    }
}
