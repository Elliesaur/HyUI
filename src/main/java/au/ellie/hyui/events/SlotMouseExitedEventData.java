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
 * {@link com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType#SlotMouseExited}.
 */
public final class SlotMouseExitedEventData {
    private final Integer slotIndex;

    public SlotMouseExitedEventData(Integer slotIndex) {
        this.slotIndex = slotIndex;
    }

    public Integer getSlotIndex() {
        return slotIndex;
    }

    public static SlotMouseExitedEventData from(DynamicPageData data) {
        return new SlotMouseExitedEventData(DynamicPageDataReader.getInt(data, "SlotIndex"));
    }
}
