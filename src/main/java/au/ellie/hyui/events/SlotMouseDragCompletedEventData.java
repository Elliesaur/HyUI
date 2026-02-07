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

package au.ellie.hyui.events;

/**
 * Payload passed to listeners registered for
 * {@link com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType#SlotMouseDragCompleted}.
 */
public final class SlotMouseDragCompletedEventData {
    private final Integer sourceItemGridIndex;
    private final Integer sourceSlotId;
    
    private final Integer slotIndex;
    private final Integer itemStackQuantity;
    private final Integer pressedMouseButton;
    private final String itemStackId;
    private final Integer sourceInventorySectionId;

    public SlotMouseDragCompletedEventData(Integer sourceItemGridIndex,
                                           Integer sourceSlotId,
                                           Integer itemStackQuantity,
                                           Integer pressedMouseButton,
                                           String itemStackId,
                                           Integer sourceInventorySectionId,
                                           Integer slotIndex) {
        this.sourceItemGridIndex = sourceItemGridIndex;
        this.sourceSlotId = sourceSlotId;
        this.itemStackQuantity = itemStackQuantity;
        this.pressedMouseButton = pressedMouseButton;
        this.itemStackId = itemStackId;
        this.sourceInventorySectionId = sourceInventorySectionId;
        this.slotIndex = slotIndex;
    }

    public Integer getSourceItemGridIndex() {
        return sourceItemGridIndex;
    }

    public Integer getSourceSlotId() {
        return sourceSlotId;
    }

    public Integer getItemStackQuantity() {
        return itemStackQuantity;
    }

    public Integer getPressedMouseButton() {
        return pressedMouseButton;
    }

    public String getItemStackId() {
        return itemStackId;
    }
    
    public Integer getSlotIndex() {
        return slotIndex;
    }

    public Integer getSourceInventorySectionId() {
        return sourceInventorySectionId;
    }

    public static SlotMouseDragCompletedEventData from(DynamicPageData data) {
        return new SlotMouseDragCompletedEventData(
                DynamicPageDataReader.getInt(data, "SourceItemGridIndex"),
                DynamicPageDataReader.getInt(data, "SourceSlotId"),
                DynamicPageDataReader.getInt(data, "ItemStackQuantity"),
                DynamicPageDataReader.getInt(data, "PressedMouseButton"),
                DynamicPageDataReader.getString(data, "ItemStackId"),
                DynamicPageDataReader.getInt(data, "SourceInventorySectionId"),
                DynamicPageDataReader.getInt(data, "SlotIndex")
        );
    }
}
