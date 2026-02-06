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

package au.ellie.hyui.types;

import au.ellie.hyui.builders.HyUIPatchStyle;
import au.ellie.hyui.utils.BsonDocumentHelper;

/**
 * BlockSelectorStyle type definition.
 */
public class BlockSelectorStyle implements HyUIBsonSerializable {
    private ItemGridStyle itemGridStyle;
    private HyUIPatchStyle slotDropIcon;
    private HyUIPatchStyle slotDeleteIcon;
    private HyUIPatchStyle slotHoverOverlay;

    public BlockSelectorStyle withItemGridStyle(ItemGridStyle itemGridStyle) {
        this.itemGridStyle = itemGridStyle;
        return this;
    }

    public BlockSelectorStyle withSlotDropIcon(HyUIPatchStyle slotDropIcon) {
        this.slotDropIcon = slotDropIcon;
        return this;
    }

    public BlockSelectorStyle withSlotDeleteIcon(HyUIPatchStyle slotDeleteIcon) {
        this.slotDeleteIcon = slotDeleteIcon;
        return this;
    }

    public BlockSelectorStyle withSlotHoverOverlay(HyUIPatchStyle slotHoverOverlay) {
        this.slotHoverOverlay = slotHoverOverlay;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (itemGridStyle != null) doc.set("ItemGridStyle", itemGridStyle.toBsonDocument());
        if (slotDropIcon != null) doc.set("SlotDropIcon", slotDropIcon.toBsonDocument());
        if (slotDeleteIcon != null) doc.set("SlotDeleteIcon", slotDeleteIcon.toBsonDocument());
        if (slotHoverOverlay != null) doc.set("SlotHoverOverlay", slotHoverOverlay.toBsonDocument());
    }
}
