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
    private Boolean clipChildren;

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

    /**
     * Sets whether the container should clip its children.
     *
     * @param clipChildren Whether to clip children.
     * @return the {@code ContainerBuilder} instance for method chaining
     */
    public ContainerBuilder withClipChildren(boolean clipChildren) {
        this.clipChildren = clipChildren;
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

        if (clipChildren != null) {
            commands.set(selector + ".ClipChildren", clipChildren);
        }

        if (titleText != null) {
            String titleSelector = selector + " #Title #HyUIContainerTitle";
            HyUIPlugin.getLog().logInfo("Setting Title Text: " + titleText + " for " + titleSelector);
            commands.set(titleSelector + ".Text", titleText);
        }
    }

    @Override
    protected void buildChildren(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector != null) {
            for (UIElementBuilder<?> child : children) {
                HyUIPlugin.getLog().logInfo("Building child element with parent selector: " + child.parentSelector);
                // We want to make sure children can be placed in #Title or #Content.
                // UIElementBuilder.inside() sets parentSelector.
                String childParent = child.parentSelector;

                // Store the original parent selector so we can restore it after building.
                // This prevents the selector from accumulating recursively on subsequent builds.
                String originalParent = childParent;

                if (childParent.equals("#Content")) {
                    child.inside(selector + " #Content").build(commands, events);
                } else if (childParent.equals("#Title")) {
                    child.inside(selector + " #Title").build(commands, events);
                } else if (childParent.startsWith("#")) {
                    // If it starts with #, assume it's a sub-element ID of the container
                    child.inside(selector + " " + childParent).build(commands, events);
                } else {
                    // Fallback
                    child.inside(selector + " " + childParent).build(commands, events);
                }

                // Restore the original parent selector
                child.inside(originalParent);
            }
        }
    }
}
