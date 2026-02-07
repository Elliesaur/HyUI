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

    @Override
    protected boolean supportsStyling() {
        return false;
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
    }
}
