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
 * {@link com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType#SlotClickPressWhileDragging}.
 */
public final class SlotClickPressWhileDraggingEventData {
    private final Integer slotIndex;
    private final String dragItemStackId;
    private final Integer dragItemStackQuantity;
    private final Integer dragSourceInventorySectionId;
    private final Integer dragSourceItemGridIndex;
    private final Integer dragSourceSlotId;
    private final Integer dragPressedMouseButton;
    private final Integer clickMouseButton;
    private final Integer clickCount;

    public SlotClickPressWhileDraggingEventData(Integer slotIndex,
                                                String dragItemStackId,
                                                Integer dragItemStackQuantity,
                                                Integer dragSourceInventorySectionId,
                                                Integer dragSourceItemGridIndex,
                                                Integer dragSourceSlotId,
                                                Integer dragPressedMouseButton,
                                                Integer clickMouseButton,
                                                Integer clickCount) {
        this.slotIndex = slotIndex;
        this.dragItemStackId = dragItemStackId;
        this.dragItemStackQuantity = dragItemStackQuantity;
        this.dragSourceInventorySectionId = dragSourceInventorySectionId;
        this.dragSourceItemGridIndex = dragSourceItemGridIndex;
        this.dragSourceSlotId = dragSourceSlotId;
        this.dragPressedMouseButton = dragPressedMouseButton;
        this.clickMouseButton = clickMouseButton;
        this.clickCount = clickCount;
    }

    public Integer getSlotIndex() {
        return slotIndex;
    }

    public String getDragItemStackId() {
        return dragItemStackId;
    }

    public Integer getDragItemStackQuantity() {
        return dragItemStackQuantity;
    }

    public Integer getDragSourceInventorySectionId() {
        return dragSourceInventorySectionId;
    }

    public Integer getDragSourceItemGridIndex() {
        return dragSourceItemGridIndex;
    }

    public Integer getDragSourceSlotId() {
        return dragSourceSlotId;
    }

    public Integer getDragPressedMouseButton() {
        return dragPressedMouseButton;
    }

    public Integer getClickMouseButton() {
        return clickMouseButton;
    }

    public Integer getClickCount() {
        return clickCount;
    }

    public static SlotClickPressWhileDraggingEventData from(DynamicPageData data) {
        return new SlotClickPressWhileDraggingEventData(
                DynamicPageDataReader.getInt(data, "SlotIndex"),
                DynamicPageDataReader.getString(data, "DragItemStackId"),
                DynamicPageDataReader.getInt(data, "DragItemStackQuantity"),
                DynamicPageDataReader.getInt(data, "DragSourceInventorySectionId"),
                DynamicPageDataReader.getInt(data, "DragSourceItemGridIndex"),
                DynamicPageDataReader.getInt(data, "DragSourceSlotId"),
                DynamicPageDataReader.getInt(data, "DragPressedMouseButton"),
                DynamicPageDataReader.getInt(data, "ClickMouseButton"),
                DynamicPageDataReader.getInt(data, "ClickCount")
        );
    }
}
