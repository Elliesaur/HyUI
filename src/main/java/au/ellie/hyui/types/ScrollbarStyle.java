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
 * ScrollbarStyle type definition.
 */
public class ScrollbarStyle implements HyUIBsonSerializable {
    private Boolean onlyVisibleWhenHovered;
    private Integer spacing;
    private Integer size;
    private HyUIPatchStyle background;
    private HyUIPatchStyle handle;
    private HyUIPatchStyle hoveredHandle;
    private HyUIPatchStyle draggedHandle;

    public ScrollbarStyle withOnlyVisibleWhenHovered(boolean onlyVisibleWhenHovered) {
        this.onlyVisibleWhenHovered = onlyVisibleWhenHovered;
        return this;
    }

    public ScrollbarStyle withSpacing(int spacing) {
        this.spacing = spacing;
        return this;
    }

    public ScrollbarStyle withSize(int size) {
        this.size = size;
        return this;
    }

    public ScrollbarStyle withBackground(HyUIPatchStyle background) {
        this.background = background;
        return this;
    }

    public ScrollbarStyle withHandle(HyUIPatchStyle handle) {
        this.handle = handle;
        return this;
    }

    public ScrollbarStyle withHoveredHandle(HyUIPatchStyle hoveredHandle) {
        this.hoveredHandle = hoveredHandle;
        return this;
    }

    public ScrollbarStyle withDraggedHandle(HyUIPatchStyle draggedHandle) {
        this.draggedHandle = draggedHandle;
        return this;
    }

    public static ScrollbarStyle defaultStyle() {
        return new ScrollbarStyle()
                .withSpacing(6)
                .withSize(6)
                .withBackground(new HyUIPatchStyle()
                        .setTexturePath("Common/Scrollbar.png")
                        .setBorder(3))
                .withHandle(new HyUIPatchStyle()
                        .setTexturePath("Common/ScrollbarHandle.png")
                        .setBorder(3))
                .withHoveredHandle(new HyUIPatchStyle()
                        .setTexturePath("Common/ScrollbarHandleHovered.png")
                        .setBorder(3))
                .withDraggedHandle(new HyUIPatchStyle()
                        .setTexturePath("Common/ScrollbarHandleDragged.png")
                        .setBorder(3));
    }

    public static ScrollbarStyle defaultExtraSpacingStyle() {
        return defaultStyle().withSpacing(12);
    }

    public static ScrollbarStyle translucentStyle() {
        return new ScrollbarStyle()
                .withSpacing(6)
                .withSize(6)
                .withOnlyVisibleWhenHovered(true)
                .withHandle(new HyUIPatchStyle()
                        .setTexturePath("Common/ScrollbarHandle.png")
                        .setBorder(3));
    }

    public static ScrollbarStyle defaultPlaceholderStyle() {
        return new ScrollbarStyle()
                .withSpacing(12)
                .withSize(10);
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (onlyVisibleWhenHovered != null) doc.set("OnlyVisibleWhenHovered", onlyVisibleWhenHovered);
        if (spacing != null) doc.set("Spacing", spacing);
        if (size != null) doc.set("Size", size);
        if (background != null) doc.set("Background", background.toBsonDocument());
        if (handle != null) doc.set("Handle", handle.toBsonDocument());
        if (hoveredHandle != null) doc.set("HoveredHandle", hoveredHandle.toBsonDocument());
        if (draggedHandle != null) doc.set("DraggedHandle", draggedHandle.toBsonDocument());
    }
}
