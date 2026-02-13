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

import au.ellie.hyui.builders.HyUIPatchStyle;

/**
 * Interface for UI elements that support backgrounds.
 */
public interface BackgroundSupported<T extends BackgroundSupported<T>> {
    
    /**
     * Sets the background for the element.
     * 
     * @param background The PatchStyle to set as background.
     * @return This builder instance for method chaining.
     */
    T withBackground(HyUIPatchStyle background);

    /**
     * Gets the current background.
     * 
     * @return The PatchStyle, or null if not set.
     */
    HyUIPatchStyle getBackground();
}
