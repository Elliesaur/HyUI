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
 * {@link com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType#SlotClickReleaseWhileDragging}.
 */
public final class SlotClickReleaseWhileDraggingEventData {
    private final Integer slotIndex;
    private final Integer clickMouseButton;
    private final Integer clickCount;

    public SlotClickReleaseWhileDraggingEventData(Integer slotIndex, Integer clickMouseButton, Integer clickCount) {
        this.slotIndex = slotIndex;
        this.clickMouseButton = clickMouseButton;
        this.clickCount = clickCount;
    }

    public Integer getSlotIndex() {
        return slotIndex;
    }

    public Integer getClickMouseButton() {
        return clickMouseButton;
    }

    public Integer getClickCount() {
        return clickCount;
    }

    public static SlotClickReleaseWhileDraggingEventData from(DynamicPageData data) {
        return new SlotClickReleaseWhileDraggingEventData(
                DynamicPageDataReader.getInt(data, "SlotIndex"),
                DynamicPageDataReader.getInt(data, "ClickMouseButton"),
                DynamicPageDataReader.getInt(data, "ClickCount")
        );
    }
}
