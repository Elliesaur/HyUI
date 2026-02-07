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

import au.ellie.hyui.builders.HyUIAnchor;
import au.ellie.hyui.builders.HyUIPatchStyle;
import au.ellie.hyui.utils.BsonDocumentHelper;

/**
 * ItemGridStyle type definition.
 */
public class ItemGridStyle implements HyUIBsonSerializable {
    private Integer slotSize;
    private Integer slotIconSize;
    private Integer slotSpacing;
    private HyUIPatchStyle slotBackground;
    private HyUIPatchStyle quantityPopupSlotOverlay;
    private HyUIPatchStyle brokenSlotBackgroundOverlay;
    private HyUIPatchStyle brokenSlotIconOverlay;
    private HyUIPatchStyle defaultItemIcon;
    private String durabilityBar;
    private HyUIPatchStyle durabilityBarBackground;
    private HyUIAnchor durabilityBarAnchor;
    private String durabilityBarColorStart;
    private String durabilityBarColorEnd;
    private HyUIPatchStyle cursedIconPatch;
    private HyUIAnchor cursedIconAnchor;
    private SoundStyle itemStackHoveredSound;
    private SoundStyle itemStackActivateSound;

    public ItemGridStyle withSlotSize(int slotSize) {
        this.slotSize = slotSize;
        return this;
    }

    public ItemGridStyle withSlotIconSize(int slotIconSize) {
        this.slotIconSize = slotIconSize;
        return this;
    }

    public ItemGridStyle withSlotSpacing(int slotSpacing) {
        this.slotSpacing = slotSpacing;
        return this;
    }

    public ItemGridStyle withSlotBackground(HyUIPatchStyle slotBackground) {
        this.slotBackground = slotBackground;
        return this;
    }

    public ItemGridStyle withQuantityPopupSlotOverlay(HyUIPatchStyle quantityPopupSlotOverlay) {
        this.quantityPopupSlotOverlay = quantityPopupSlotOverlay;
        return this;
    }

    public ItemGridStyle withBrokenSlotBackgroundOverlay(HyUIPatchStyle brokenSlotBackgroundOverlay) {
        this.brokenSlotBackgroundOverlay = brokenSlotBackgroundOverlay;
        return this;
    }

    public ItemGridStyle withBrokenSlotIconOverlay(HyUIPatchStyle brokenSlotIconOverlay) {
        this.brokenSlotIconOverlay = brokenSlotIconOverlay;
        return this;
    }

    public ItemGridStyle withDefaultItemIcon(HyUIPatchStyle defaultItemIcon) {
        this.defaultItemIcon = defaultItemIcon;
        return this;
    }

    public ItemGridStyle withDurabilityBar(String durabilityBar) {
        this.durabilityBar = durabilityBar;
        return this;
    }

    public ItemGridStyle withDurabilityBarBackground(HyUIPatchStyle durabilityBarBackground) {
        this.durabilityBarBackground = durabilityBarBackground;
        return this;
    }

    public ItemGridStyle withDurabilityBarAnchor(HyUIAnchor durabilityBarAnchor) {
        this.durabilityBarAnchor = durabilityBarAnchor;
        return this;
    }

    public ItemGridStyle withDurabilityBarColorStart(String durabilityBarColorStart) {
        this.durabilityBarColorStart = durabilityBarColorStart;
        return this;
    }

    public ItemGridStyle withDurabilityBarColorEnd(String durabilityBarColorEnd) {
        this.durabilityBarColorEnd = durabilityBarColorEnd;
        return this;
    }

    public ItemGridStyle withCursedIconPatch(HyUIPatchStyle cursedIconPatch) {
        this.cursedIconPatch = cursedIconPatch;
        return this;
    }

    public ItemGridStyle withCursedIconAnchor(HyUIAnchor cursedIconAnchor) {
        this.cursedIconAnchor = cursedIconAnchor;
        return this;
    }

    public ItemGridStyle withItemStackHoveredSound(SoundStyle itemStackHoveredSound) {
        this.itemStackHoveredSound = itemStackHoveredSound;
        return this;
    }

    public ItemGridStyle withItemStackActivateSound(SoundStyle itemStackActivateSound) {
        this.itemStackActivateSound = itemStackActivateSound;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (slotSize != null) doc.set("SlotSize", slotSize);
        if (slotIconSize != null) doc.set("SlotIconSize", slotIconSize);
        if (slotSpacing != null) doc.set("SlotSpacing", slotSpacing);
        if (slotBackground != null) doc.set("SlotBackground", slotBackground.toBsonDocument());
        if (quantityPopupSlotOverlay != null) doc.set("QuantityPopupSlotOverlay", quantityPopupSlotOverlay.toBsonDocument());
        if (brokenSlotBackgroundOverlay != null) doc.set("BrokenSlotBackgroundOverlay", brokenSlotBackgroundOverlay.toBsonDocument());
        if (brokenSlotIconOverlay != null) doc.set("BrokenSlotIconOverlay", brokenSlotIconOverlay.toBsonDocument());
        if (defaultItemIcon != null) doc.set("DefaultItemIcon", defaultItemIcon.toBsonDocument());
        if (durabilityBar != null) doc.set("DurabilityBar", durabilityBar);
        if (durabilityBarBackground != null) doc.set("DurabilityBarBackground", durabilityBarBackground.toBsonDocument());
        if (durabilityBarAnchor != null) doc.set("DurabilityBarAnchor", durabilityBarAnchor.toBsonDocument());
        if (durabilityBarColorStart != null) doc.set("DurabilityBarColorStart", durabilityBarColorStart);
        if (durabilityBarColorEnd != null) doc.set("DurabilityBarColorEnd", durabilityBarColorEnd);
        if (cursedIconPatch != null) doc.set("CursedIconPatch", cursedIconPatch.toBsonDocument());
        if (cursedIconAnchor != null) doc.set("CursedIconAnchor", cursedIconAnchor.toBsonDocument());
        if (itemStackHoveredSound != null) doc.set("ItemStackHoveredSound", itemStackHoveredSound.toBsonDocument());
        if (itemStackActivateSound != null) doc.set("ItemStackActivateSound", itemStackActivateSound.toBsonDocument());
    }
}
