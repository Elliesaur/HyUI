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
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.Set;


/**
 * Builder for ColorPickerDropdownBox UI elements.
 */
public class ColorPickerDropdownBoxBuilder extends UIElementBuilder<ColorPickerDropdownBoxBuilder> {
    private ColorFormat format;
    private Boolean displayTextField;

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
        return Set.of(
                "ColorPickerStyle",
                "Background",
                "ArrowBackground",
                "Overlay",
                "PanelBackground",
                "PanelPadding",
                "PanelWidth",
                "PanelOffset",
                "ArrowAnchor",
                "Sounds"
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
    }
}
