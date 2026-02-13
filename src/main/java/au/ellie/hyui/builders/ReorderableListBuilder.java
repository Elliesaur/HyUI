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

import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.elements.ScrollbarStyleSupported;
import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.Set;

/**
 * Builder for ReorderableList UI elements.
 */
public class ReorderableListBuilder extends UIElementBuilder<ReorderableListBuilder>
        implements LayoutModeSupported<ReorderableListBuilder>, ScrollbarStyleSupported<ReorderableListBuilder> {
    private String layoutMode;
    private String scrollbarStyleReference;
    private String scrollbarStyleDocument;
    private HyUIAnchor dropIndicatorAnchor;
    private HyUIPatchStyle dropIndicatorBackground;

    public ReorderableListBuilder() {
        super(UIElements.REORDERABLE_LIST, "#HyUIReorderableList");
        withUiFile("Pages/Elements/ReorderableList.ui");
        withWrappingGroup(true);
    }

    public static ReorderableListBuilder reorderableList() {
        return new ReorderableListBuilder();
    }

    @Override
    public ReorderableListBuilder withLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    @Override
    public String getLayoutMode() {
        return layoutMode;
    }

    @Override
    public ReorderableListBuilder withScrollbarStyle(String document, String styleReference) {
        this.scrollbarStyleDocument = document;
        this.scrollbarStyleReference = styleReference;
        return this;
    }

    @Override
    public String getScrollbarStyleReference() {
        return scrollbarStyleReference;
    }

    @Override
    public String getScrollbarStyleDocument() {
        return scrollbarStyleDocument;
    }

    public ReorderableListBuilder withDropIndicatorAnchor(HyUIAnchor anchor) {
        this.dropIndicatorAnchor = anchor;
        return this;
    }

    public ReorderableListBuilder withDropIndicatorBackground(HyUIPatchStyle background) {
        this.dropIndicatorBackground = background;
        return this;
    }

    public ReorderableListBuilder withDropIndicatorBackground(String value) {
        this.dropIndicatorBackground = new HyUIPatchStyle().setTexturePath(value);
        return this;
    }

    @Override
    protected boolean isStyleWhitelist() {
        return true;
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) {
            return;
        }

        applyLayoutMode(commands, selector);
        applyScrollbarStyle(commands, selector);

        if (dropIndicatorAnchor != null) {
            commands.setObject(selector + ".DropIndicatorAnchor", dropIndicatorAnchor.toHytaleAnchor());
        }
        if (dropIndicatorBackground != null) {
            commands.setObject(selector + ".DropIndicatorBackground", dropIndicatorBackground.getHytalePatchStyle());
        }
    }
}
