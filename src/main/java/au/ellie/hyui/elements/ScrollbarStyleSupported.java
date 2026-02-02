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

import au.ellie.hyui.HyUIPlugin;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;

/**
 * Interface for UI elements that support custom scrollbar styles.
 */
public interface ScrollbarStyleSupported<T extends ScrollbarStyleSupported<T>> {

    /**
     * Sets the scrollbar style for the element as a reference string using Common.ui.
     *
     * @param styleReference The reference string (e.g., "ScrollbarStyleReferenceHere").
     * @return This builder instance for method chaining.
     */
    default T withScrollbarStyle(String styleReference) {
        return withScrollbarStyle("Common.ui", styleReference);
    }

    /**
     * Sets the scrollbar style for the element as a reference string from a specific UI document.
     *
     * @param document The document path (e.g., "Common.ui").
     * @param styleReference The reference string (e.g., "ScrollbarStyleReferenceHere").
     * @return This builder instance for method chaining.
     */
    T withScrollbarStyle(String document, String styleReference);

    /**
     * Gets the current scrollbar style reference.
     *
     * @return The reference string, or null if not set.
     */
    String getScrollbarStyleReference();

    /**
     * Gets the current scrollbar style document.
     *
     * @return The document path, or null if not set.
     */
    String getScrollbarStyleDocument();

    /**
     * Default implementation to apply the scrollbar style to the UICommandBuilder.
     * 
     * @param commands The UICommandBuilder to use.
     * @param selector The selector for the element.
     */
    default void applyScrollbarStyle(UICommandBuilder commands, String selector) {
        String reference = getScrollbarStyleReference();
        String document = getScrollbarStyleDocument();
        if (reference != null && document != null && selector != null) {
            HyUIPlugin.getLog().logFinest("Setting ScrollbarStyle reference for " + selector + " from " + document + ": " + reference);
            commands.set(selector + ".ScrollbarStyle", Value.ref(document, reference));
        }
    }
}
