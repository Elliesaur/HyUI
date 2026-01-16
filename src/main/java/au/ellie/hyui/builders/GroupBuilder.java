package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.theme.Theme;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import java.util.Collections;
import java.util.Set;

/**
 * Builder for creating group UI elements. 
 * Groups can be used to organize and layout other UI elements.
 * 
 * This directly translates to a {@code Group {}}
 */
public class GroupBuilder extends UIElementBuilder<GroupBuilder> {
    private String layoutMode;

    public GroupBuilder() {
        super(UIElements.GROUP);
    }

    public GroupBuilder(Theme theme) {
        super(theme, UIElements.GROUP);
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
     * Choose from: Left, Top, TopScrolling, Right, Full.
     * 
     * 
     * @param layoutMode The layout mode to set.
     * @return This builder instance for method chaining.
     */
    public GroupBuilder withLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    @Override
    protected boolean supportsStyling() {
        return false;
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (layoutMode != null) {
            HyUIPlugin.getInstance().logInfo("Setting LayoutMode: " + layoutMode + " for " + selector);
            commands.set(selector + ".LayoutMode", layoutMode);
        }

        if (hyUIStyle == null && style != null) {
            HyUIPlugin.getInstance().logInfo("Setting Style: " + style + " for " + selector);
            commands.set(selector + ".Style", style);
        }
    }
}
