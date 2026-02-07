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
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for ReorderableListGrip UI elements.
 */
public class ReorderableListGripBuilder extends UIElementBuilder<ReorderableListGripBuilder> {
    public ReorderableListGripBuilder() {
        super(UIElements.REORDERABLE_LIST_GRIP, "#HyUIReorderableListGrip");
        withUiFile("Pages/Elements/ReorderableListGrip.ui");
        withWrappingGroup(true);
    }

    public static ReorderableListGripBuilder reorderableListGrip() {
        return new ReorderableListGripBuilder();
    }

    /**
     * Adds an event listener for the Validating event.
     */
    public ReorderableListGripBuilder onValidating(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.Validating, Void.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the Dismissing event.
     */
    public ReorderableListGripBuilder onDismissing(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.Dismissing, Void.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the Scrolled event.
     */
    public ReorderableListGripBuilder onScrolled(Consumer<Float> callback) {
        return addEventListener(CustomUIEventBindingType.ValueChanged, Float.class, callback);
    }

    /**
     * Adds an event listener for the Scrolled event with context.
     */
    public ReorderableListGripBuilder onScrolled(BiConsumer<Float, UIContext> callback) {
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
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        // Register event listeners
        listeners.forEach(listener -> {
            String eventId = getEffectiveId();
            if (listener.type() == CustomUIEventBindingType.Validating) {
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
