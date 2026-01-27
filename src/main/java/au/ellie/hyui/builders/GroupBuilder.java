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
public class GroupBuilder extends UIElementBuilder<GroupBuilder> implements LayoutModeSupported<GroupBuilder>, BackgroundSupported<GroupBuilder>, ScrollbarStyleSupported<GroupBuilder> {
    private String layoutMode;
    private HyUIPatchStyle background;
    private String scrollbarStyleReference;
    private String scrollbarStyleDocument;
    private Boolean clipChildren;

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

    /**
     * Sets whether the group should clip its children.
     *
     * @param clipChildren Whether to clip children.
     * @return This builder instance for method chaining.
     */
    public GroupBuilder withClipChildren(boolean clipChildren) {
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

    @Override
    protected boolean supportsStyling() {
        return false;
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

    }
}
