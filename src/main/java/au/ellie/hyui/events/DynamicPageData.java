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

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.HashMap;
import java.util.Map;

public class DynamicPageData {
    public static final BuilderCodec<DynamicPageData> CODEC = BuilderCodec.builder(DynamicPageData.class, DynamicPageData::new)
            .addField(new KeyedCodec<>("Action", Codec.STRING), (data, s) -> data.action = s, data -> data.action)
            .addField(new KeyedCodec<>("@Value", Codec.STRING), (data, s) -> data.values.put("RefValue", s), data -> data.values.get("RefValue"))
            .addField(new KeyedCodec<>("@ValueBool", Codec.BOOLEAN), (data, s) -> data.values.put("RefValue", String.valueOf(s)), data -> Boolean.parseBoolean(data.values.get("RefValue")))
            .addField(new KeyedCodec<>("@ValueInt", Codec.INTEGER), (data, s) -> data.values.put("RefValue", String.valueOf(s)), data -> Integer.parseInt(data.values.get("RefValue")))
            .addField(new KeyedCodec<>("@ValueFloat", Codec.FLOAT), (data, s) -> data.values.put("RefValue", String.valueOf(s)), data -> Float.parseFloat(data.values.get("RefValue")))
            .addField(new KeyedCodec<>("@ValueDouble", Codec.DOUBLE), (data, s) -> data.values.put("RefValue", String.valueOf(s)), data -> Double.parseDouble(data.values.get("RefValue")))
            .addField(new KeyedCodec<>("Value", Codec.STRING), (data, s) -> data.values.put("Value", s), data -> data.values.get("Value"))
            .addField(new KeyedCodec<>("Target", Codec.STRING), (data, s) -> data.values.put("Target", s), data -> data.values.get("Target"))
            // Used for slot events: SlotMouseEntered, SlotMouseExited, SlotDoubleClicking, SlotClicking, SlotClickPressWhileDragging
            .addField(new KeyedCodec<>("SlotIndex", Codec.INTEGER), (data, s) -> data.values.put("SlotIndex", String.valueOf(s)), data -> Integer.parseInt(data.values.get("SlotIndex")))
            // Used for: Dropped, SlotMouseDragCompleted.
            .addField(new KeyedCodec<>("SourceItemGridIndex", Codec.INTEGER), (data, s) -> data.values.put("SourceItemGridIndex", String.valueOf(s)), data -> Integer.parseInt(data.values.get("SourceItemGridIndex")))
            // Used for: Dropped, SlotMouseDragCompleted.
            .addField(new KeyedCodec<>("SourceSlotId", Codec.INTEGER), (data, s) -> data.values.put("SourceSlotId", String.valueOf(s)), data -> Integer.parseInt(data.values.get("SourceSlotId")))
            // Used for: Dropped, SlotMouseDragCompleted.
            .addField(new KeyedCodec<>("ItemStackQuantity", Codec.INTEGER), (data, s) -> data.values.put("ItemStackQuantity", String.valueOf(s)), data -> Integer.parseInt(data.values.get("ItemStackQuantity")))
            // Used for: Dropped, SlotMouseDragCompleted.
            .addField(new KeyedCodec<>("PressedMouseButton", Codec.INTEGER), (data, s) -> data.values.put("PressedMouseButton", String.valueOf(s)), data -> Integer.parseInt(data.values.get("PressedMouseButton")))
            // Used for: SlotMouseDragExited.
            .addField(new KeyedCodec<>("MouseOverIndex", Codec.INTEGER), (data, s) -> data.values.put("MouseOverIndex", String.valueOf(s)), data -> Integer.parseInt(data.values.get("MouseOverIndex")))
            // Used for: Dropped, SlotMouseDragCompleted.
            .addField(new KeyedCodec<>("ItemStackId", Codec.STRING), (data, s) -> data.values.put("ItemStackId", s), data -> data.values.get("ItemStackId"))
            // Seems null always, maybe used in multiple item grid situations where you're transferring from container to container?
            .addField(new KeyedCodec<>("SourceInventorySectionId", Codec.INTEGER), (data, s) -> data.values.put("SourceInventorySectionId", String.valueOf(s)), data -> Integer.parseInt(data.values.get("SourceInventorySectionId")))
            // Used for: SlotClickPressWhileDragging
            .addField(new KeyedCodec<>("DragItemStackId", Codec.STRING), (data, s) -> data.values.put("DragItemStackId", s), data -> data.values.get("DragItemStackId"))
            .addField(new KeyedCodec<>("DragItemStackQuantity", Codec.INTEGER), (data, s) -> data.values.put("DragItemStackQuantity", String.valueOf(s)), data -> Integer.parseInt(data.values.get("DragItemStackQuantity")))
            .addField(new KeyedCodec<>("DragSourceInventorySectionId", Codec.INTEGER), (data, s) -> data.values.put("DragSourceInventorySectionId", String.valueOf(s)), data -> Integer.parseInt(data.values.get("DragSourceInventorySectionId")))
            .addField(new KeyedCodec<>("DragSourceItemGridIndex", Codec.INTEGER), (data, s) -> data.values.put("DragSourceItemGridIndex", String.valueOf(s)), data -> Integer.parseInt(data.values.get("DragSourceItemGridIndex")))
            .addField(new KeyedCodec<>("DragSourceSlotId", Codec.INTEGER), (data, s) -> data.values.put("DragSourceSlotId", String.valueOf(s)), data -> Integer.parseInt(data.values.get("DragSourceSlotId")))
            .addField(new KeyedCodec<>("DragPressedMouseButton", Codec.INTEGER), (data, s) -> data.values.put("DragPressedMouseButton", String.valueOf(s)), data -> Integer.parseInt(data.values.get("DragPressedMouseButton")))
            .addField(new KeyedCodec<>("ClickMouseButton", Codec.INTEGER), (data, s) -> data.values.put("ClickMouseButton", String.valueOf(s)), data -> Integer.parseInt(data.values.get("ClickMouseButton")))
            .addField(new KeyedCodec<>("ClickCount", Codec.INTEGER), (data, s) -> data.values.put("ClickCount", String.valueOf(s)), data -> Integer.parseInt(data.values.get("ClickCount")))
            // Used for: SelectedTabChanged
            .addField(new KeyedCodec<>("SelectedTab", Codec.STRING), (data, s) -> data.values.put("SelectedTab", String.valueOf(s)), data -> data.values.get("SelectedTab"))
            .build();

    public String action;
    public final Map<String, String> values = new HashMap<>();

    public String getValue(String key) {
        return values.get(key);
    }
}
