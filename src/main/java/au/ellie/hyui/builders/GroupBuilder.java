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

import au.ellie.hyui.elements.BackgroundSupported;
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.elements.ScrollbarStyleSupported;
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.theme.Theme;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

/**
 * Builder for creating group UI elements. 
 * Groups can be used to organize and layout other UI elements.
 * 
 * This directly translates to a {@code Group {}}
 */
public class GroupBuilder extends UIElementBuilder<GroupBuilder> implements 
        LayoutModeSupported<GroupBuilder>, 
        BackgroundSupported<GroupBuilder>, 
        ScrollbarStyleSupported<GroupBuilder> {
    private String layoutMode;
    private HyUIPatchStyle background;
    private String scrollbarStyleReference;
    private String scrollbarStyleDocument;

    public GroupBuilder() {
        super(UIElements.GROUP, "Group");
    }

    public GroupBuilder(Theme theme) {
        super(theme, UIElements.GROUP, "Group");
    }

    /**
     * Factory method to create a new instance of {@code GroupBuilder}.
     *
     * @return A new {@code GroupBuilder} instance for creating and customizing a group.
     */
    public static GroupBuilder group() {
        return new GroupBuilder();
    }
    
    /**
     * Sets the layout mode for the group.
     * 
     * @param layoutMode The layout mode to set.
     * @return This builder instance for method chaining.
     */
    @Override
    public GroupBuilder withLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }
    
    @Override
    public String getLayoutMode() {
        return this.layoutMode;
    }

    @Override
    public GroupBuilder withBackground(HyUIPatchStyle background) {
        this.background = background;
        return this;
    }

    @Override
    public HyUIPatchStyle getBackground() {
        return this.background;
    }

    @Override
    public GroupBuilder withScrollbarStyle(String document, String styleReference) {
        this.scrollbarStyleDocument = document;
        this.scrollbarStyleReference = styleReference;
        return this;
    }

    @Override
    public String getScrollbarStyleReference() {
        return this.scrollbarStyleReference;
    }

    @Override
    public String getScrollbarStyleDocument() {
        return this.scrollbarStyleDocument;
    }

    @Override
    protected boolean supportsStyling() {
        return false;
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        applyLayoutMode(commands, selector);
        applyScrollbarStyle(commands, selector);
    }
}
