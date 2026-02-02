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

import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;

public class HyUIAnchor {
    private Integer left = null;
    private Integer right = null;
    private Integer top = null;
    private Integer bottom = null;
    private Integer height = null;
    private Integer full = null;
    private Integer horizontal = null;
    private Integer vertical = null;
    private Integer width = null;
    private Integer minWidth = null;
    private Integer maxWidth = null;
    public HyUIAnchor() {
    }

    public HyUIAnchor(Integer left, Integer right, Integer top, Integer bottom, Integer height, Integer full, Integer horizontal, Integer vertical, Integer width, Integer minWidth, Integer maxWidth) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.height = height;
        this.full = full;
        this.horizontal = horizontal;
        this.vertical = vertical;
        this.width = width;
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
    }

    public HyUIAnchor setLeft(Integer left) {
        this.left = left;
        return this;
    }

    public HyUIAnchor setRight(Integer right) {
        this.right = right;
        return this;
    }

    public HyUIAnchor setTop(Integer top) {
        this.top = top;
        return this;
    }

    public HyUIAnchor setBottom(Integer bottom) {
        this.bottom = bottom;
        return this;
    }

    public HyUIAnchor setHeight(Integer height) {
        this.height = height;
        return this;
    }

    public Integer getHeight() {
        return height;
    }

    public HyUIAnchor setFull(Integer full) {
        this.full = full;
        return this;
    }

    public HyUIAnchor setHorizontal(Integer horizontal) {
        this.horizontal = horizontal;
        return this;
    }

    public HyUIAnchor setVertical(Integer vertical) {
        this.vertical = vertical;
        return this;
    }

    public HyUIAnchor setWidth(Integer width) {
        this.width = width;
        return this;
    }

    public HyUIAnchor setMinWidth(Integer minWidth) {
        this.minWidth = minWidth;
        return this;
    }

    public HyUIAnchor setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public Anchor toHytaleAnchor() {
        Anchor anchor = new Anchor();
        if (left != null) anchor.setLeft(Value.of(left));
        if (right != null) anchor.setRight(Value.of(right));
        if (top != null) anchor.setTop(Value.of(top));
        if (bottom != null) anchor.setBottom(Value.of(bottom));
        if (height != null) anchor.setHeight(Value.of(height));
        if (full != null) anchor.setFull(Value.of(full));
        if (horizontal != null) anchor.setHorizontal(Value.of(horizontal));
        if (vertical != null) anchor.setVertical(Value.of(vertical));
        if (width != null) anchor.setWidth(Value.of(width));
        if (minWidth != null) anchor.setMinWidth(Value.of(minWidth));
        if (maxWidth != null) anchor.setMaxWidth(Value.of(maxWidth));
        return anchor;
    }
}
