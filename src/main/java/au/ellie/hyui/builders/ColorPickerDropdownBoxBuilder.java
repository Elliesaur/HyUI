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
import au.ellie.hyui.types.ColorFormat;
import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * Builder for ColorPickerDropdownBox UI elements.
 */
public class ColorPickerDropdownBoxBuilder extends UIElementBuilder<ColorPickerDropdownBoxBuilder> {
    private ColorFormat format;
    private Boolean displayTextField;
    private Boolean resetTransparencyWhenChangingColor;

    public ColorPickerDropdownBoxBuilder() {
        super(UIElements.COLOR_PICKER_DROPDOWN_BOX, "#HyUIColorPickerDropdownBox");
        withUiFile("Pages/Elements/ColorPickerDropdownBox.ui");
        withWrappingGroup(true);
    }

    public static ColorPickerDropdownBoxBuilder colorPickerDropdownBox() {
        return new ColorPickerDropdownBoxBuilder();
    }

    public ColorPickerDropdownBoxBuilder withFormat(ColorFormat format) {
        this.format = format;
        return this;
    }

    public ColorPickerDropdownBoxBuilder withDisplayTextField(boolean displayTextField) {
        this.displayTextField = displayTextField;
        return this;
    }

    public ColorPickerDropdownBoxBuilder withResetTransparencyWhenChangingColor(boolean resetTransparencyWhenChangingColor) {
        this.resetTransparencyWhenChangingColor = resetTransparencyWhenChangingColor;
        return this;
    }

    /**
     * Adds an event listener for the RightClicking event.
     */
    public ColorPickerDropdownBoxBuilder onRightClicking(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.RightClicking, MouseEventData.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the RightClicking event.
     */
    public ColorPickerDropdownBoxBuilder onRightClicking(Consumer<MouseEventData> callback) {
        return addEventListener(CustomUIEventBindingType.RightClicking, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the RightClicking event with context.
     */
    public ColorPickerDropdownBoxBuilder onRightClicking(BiConsumer<MouseEventData, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.RightClicking, MouseEventData.class, callback);
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
                StylePropertySets.INPUT_FIELD_STYLE,
                StylePropertySets.INPUT_FIELD_ICON,
                StylePropertySets.INPUT_FIELD_BUTTON,
                StylePropertySets.INPUT_FIELD_DECORATION_STATE,
                Set.of(
                        "Default",
                        "Hovered",
                        "Pressed",
                        "PanelBackground",
                        "PanelWidth",
                        "PanelHeight",
                        "PanelOffset",
                        "OpacitySelectorBackground",
                        "ButtonBackground",
                        "ButtonFill",
                        "TextFieldHeight"
                )
        );
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (format != null) {
            HyUIPlugin.getLog().logFinest("Setting Format: " + format + " for " + selector);
            commands.set(selector + ".Format", format.name());
        }
        if (displayTextField != null) {
            HyUIPlugin.getLog().logFinest("Setting DisplayTextField: " + displayTextField + " for " + selector);
            commands.set(selector + ".DisplayTextField", displayTextField);
        }
        if (resetTransparencyWhenChangingColor != null) {
            HyUIPlugin.getLog().logFinest("Setting ResetTransparencyWhenChangingColor: " + resetTransparencyWhenChangingColor + " for " + selector);
            commands.set(selector + ".ResetTransparencyWhenChangingColor", resetTransparencyWhenChangingColor);
        }

        listeners.forEach(listener -> {
            if (listener.type() == CustomUIEventBindingType.RightClicking) {
                String eventId = getEffectiveId();
                HyUIPlugin.getLog().logFinest("Adding RightClicking event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.RightClicking, selector,
                        EventData.of("Action", UIEventActions.RIGHT_CLICKING)
                            .append("Target", eventId),
                        false);
            }
        });
    }
}
