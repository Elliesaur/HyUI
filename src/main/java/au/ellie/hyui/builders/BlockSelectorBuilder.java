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
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.types.BlockSelectorStyle;
import au.ellie.hyui.utils.PropertyBatcher;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.Set;

/**
 * Builder for BlockSelector UI elements.
 */
public class BlockSelectorBuilder extends UIElementBuilder<BlockSelectorBuilder> {
    private Integer capacity;
    private String value;
    private BlockSelectorStyle blockSelectorStyle;

    public BlockSelectorBuilder() {
        super(UIElements.BLOCK_SELECTOR, "#HyUIBlockSelector");
        withUiFile("Pages/Elements/BlockSelector.ui");
        withWrappingGroup(true);
    }

    public static BlockSelectorBuilder blockSelector() {
        return new BlockSelectorBuilder();
    }

    public BlockSelectorBuilder withCapacity(Integer capacity) {
        this.capacity = capacity;
        return this;
    }

    public BlockSelectorBuilder withValue(String value) {
        this.value = value;
        return this;
    }

    public BlockSelectorBuilder withStyle(BlockSelectorStyle blockSelectorStyle) {
        this.blockSelectorStyle = blockSelectorStyle;
        return this;
    }

    @Override
    protected boolean supportsStyling() {
        return true;
    }

    @Override
    protected boolean isStyleWhitelist() {
        return true;
    }

    @Override
    protected Set<String> getSupportedStyleProperties() {
        return StylePropertySets.merge(
                StylePropertySets.ANCHOR,
                StylePropertySets.PADDING,
                StylePropertySets.PATCH_STYLE,
                StylePropertySets.SOUND_STYLE,
                Set.of(
                        "SlotDropIcon",
                        "SlotDeleteIcon",
                        "SlotHoverOverlay",
                        "SlotSize",
                        "SlotIconSize",
                        "SlotSpacing",
                        "DurabilityBarBackground",
                        "DurabilityBar",
                        "DurabilityBarColorStart",
                        "DurabilityBarColorEnd",
                        "CursedIconPatch",
                        "SlotBackground",
                        "QuantityPopupSlotOverlay",
                        "BrokenSlotBackgroundOverlay",
                        "BrokenSlotIconOverlay",
                        "DefaultItemIcon"
                )
        );
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (capacity != null) {
            HyUIPlugin.getLog().logFinest("Setting Capacity: " + capacity + " for " + selector);
            commands.set(selector + ".Capacity", capacity);
        }
        if (value != null) {
            HyUIPlugin.getLog().logFinest("Setting Value: " + value + " for " + selector);
            commands.set(selector + ".Value", value);
        }
        if (blockSelectorStyle != null) {
            HyUIPlugin.getLog().logFinest("Setting Style: " + blockSelectorStyle + " for " + selector);
            PropertyBatcher.endSet(selector + ".Style", filterStyleDocument(blockSelectorStyle.toBsonDocument()), commands);
        }
    }
}
