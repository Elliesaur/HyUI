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

import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

/**
 * Builder for LabeledCheckBox UI elements.
 */
public class LabeledCheckBoxBuilder extends UIElementBuilder<LabeledCheckBoxBuilder> {
    public LabeledCheckBoxBuilder() {
        super(UIElements.LABELED_CHECK_BOX, "#HyUILabeledCheckBox");
        withUiFile("Pages/Elements/LabeledCheckBox.ui");
        withWrappingGroup(true);
    }

    public static LabeledCheckBoxBuilder labeledCheckBox() {
        return new LabeledCheckBoxBuilder();
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
    protected java.util.Set<String> getSupportedStyleProperties() {
        return java.util.Set.of();
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
    }
}
