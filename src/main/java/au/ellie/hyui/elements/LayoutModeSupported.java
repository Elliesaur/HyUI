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

package au.ellie.hyui.elements;

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import au.ellie.hyui.HyUIPlugin;

/**
 * Interface for UI elements that support LayoutMode.
 */
public interface LayoutModeSupported<T extends LayoutModeSupported<T>> {

    enum LayoutMode {
        TopScrolling,
        MiddleCenter,
        Left,
        Right,
        Full,
        Middle,
        Bottom,
        BottomScrolling,
        CenterMiddle,
        Top,
        LeftCenterWrap,
        RightCenterWrap,
        Center
    }
    
    /**
     * Sets the layout mode for the element.
     * 
     * @param layoutMode The layout mode to set (e.g., Left, Top, TopScrolling, Right, Full).
     * @return This builder instance for method chaining.
     */
    T withLayoutMode(String layoutMode);

    /**
     * Sets the layout mode for the element using the LayoutMode enum.
     *
     * @param layoutMode The layout mode enum value.
     * @return This builder instance for method chaining.
     */
    default T withLayoutMode(LayoutMode layoutMode) {
        return withLayoutMode(layoutMode.name());
    }

    /**
     * Sets the layout mode for the element using the types.LayoutMode enum.
     * This overload supports the new typed enum while maintaining backwards compatibility.
     *
     * @param layoutMode The layout mode enum value from au.ellie.hyui.types.LayoutMode.
     * @return This builder instance for method chaining.
     */
    default T withLayoutMode(au.ellie.hyui.types.LayoutMode layoutMode) {
        return withLayoutMode(layoutMode.name());
    }

    /**
     * Gets the current layout mode.
     * 
     * @return The layout mode, or null if not set.
     */
    String getLayoutMode();

    /**
     * Default implementation to apply the layout mode to the UICommandBuilder.
     * 
     * @param commands The UICommandBuilder to use.
     * @param selector The selector for the element.
     */
    default void applyLayoutMode(UICommandBuilder commands, String selector) {
        String mode = getLayoutMode();
        if (mode != null && selector != null) {
            HyUIPlugin.getLog().logFinest("Setting LayoutMode: " + mode + " for " + selector);
            commands.set(selector + ".LayoutMode", mode);
        }
    }
}
