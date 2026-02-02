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
import au.ellie.hyui.elements.BackgroundSupported;
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.elements.ScrollbarStyleSupported;
import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

/**
 * Builder for the Container UI element.
 */
public class ContainerBuilder extends UIElementBuilder<ContainerBuilder> implements LayoutModeSupported<ContainerBuilder>, BackgroundSupported<ContainerBuilder>, ScrollbarStyleSupported<ContainerBuilder> {
    private String titleText;
    private String layoutMode;
    private HyUIPatchStyle background;
    private String scrollbarStyleReference;
    private String scrollbarStyleDocument;

    public ContainerBuilder() {
        super(UIElements.CONTAINER, "#HyUIContainer");
        withWrappingGroup(true);
        withUiFile("Pages/Elements/Container.ui");
    }

    /**
     * Creates a ContainerBuilder instance for a container element.
     *
     * @return a ContainerBuilder configured for creating a container with predefined settings.
     */
    public static ContainerBuilder container() {
        return new ContainerBuilder();
    }

    public static ContainerBuilder decoratedContainer() { 
        return new ContainerBuilder().withUiFile("Pages/Elements/DecoratedContainer.ui"); 
    }
    /**
     * Sets the text for the container's title.
     *
     * @param titleText the title text to set
     * @return the {@code ContainerBuilder} instance for method chaining
     */
    public ContainerBuilder withTitleText(String titleText) {
        this.titleText = titleText;
        return this;
    }

    @Override
    public ContainerBuilder withLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    @Override
    public String getLayoutMode() {
        return this.layoutMode;
    }

    @Override
    public ContainerBuilder withBackground(HyUIPatchStyle background) {
        this.background = background;
        return this;
    }

    @Override
    public HyUIPatchStyle getBackground() {
        return this.background;
    }

    @Override
    public ContainerBuilder withScrollbarStyle(String document, String styleReference) {
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

    /**
     * Add a child inside the #Content of the container.
     * @param child the element to add to the container's contents.
     * @return the {@code ContainerBuilder} instance for method chaining
     */
    public ContainerBuilder addContentChild(UIElementBuilder<?> child) {
        child.inside("#Content");
        this.children.add(child);
        return this;
    }
    
    /**
     * Add a child inside the #Title of the container.
     * @param child the element to add to the container's title.
     * @return the {@code ContainerBuilder} instance for method chaining
     */
    public ContainerBuilder addTitleChild(UIElementBuilder<?> child) {
        child.inside("#Title");
        this.children.add(child);
        return this;
    }
    
    @Override
    protected boolean supportsStyling() {
        return true;
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        applyLayoutMode(commands, selector);
        applyBackground(commands, selector);
        applyScrollbarStyle(commands, selector);
        
        if (titleText != null) {
            String titleSelector = selector + " #Title #HyUIContainerTitle";
            HyUIPlugin.getLog().logFinest("Setting Title Text: " + titleText + " for " + titleSelector);
            commands.set(titleSelector + ".Text", titleText);
        }
    }

    @Override
    protected void buildChildren(UICommandBuilder commands, UIEventBuilder events, boolean updateOnly) {
        String selector = getSelector();
        if (selector != null) {
            for (UIElementBuilder<?> child : children) {
                HyUIPlugin.getLog().logFinest("Building child element with parent selector: " + child.parentSelector);
                // We want to make sure children can be placed in #Title or #Content.
                // UIElementBuilder.inside() sets parentSelector.
                String childParent = child.parentSelector;

                // Store the original parent selector so we can restore it after building.
                // This prevents the selector from accumulating recursively on subsequent builds.
                String originalParent = childParent;

                if (childParent.equals("#Content")) {
                    child.inside(selector + " #Content").build(commands, events, updateOnly);
                } else if (childParent.equals("#Title")) {
                    child.inside(selector + " #Title").build(commands, events, updateOnly);
                } else if (childParent.startsWith("#")) {
                    // If it starts with #, assume it's a sub-element ID of the container
                    child.inside(selector + " " + childParent).build(commands, events, updateOnly);
                } else {
                    // Fallback
                    child.inside(selector + " " + childParent).build(commands, events, updateOnly);
                }

                // Restore the original parent selector
                child.inside(originalParent);
            }
        }
    }
}
