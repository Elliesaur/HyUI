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

/**
 * Represents the padding for a UI element.
 */
public class HyUIPadding {
    private Integer left;
    private Integer top;
    private Integer right;
    private Integer bottom;

    public HyUIPadding() {}

    public static HyUIPadding all(int value) {
        return new HyUIPadding(value, value, value, value);
    }

    public static HyUIPadding symmetric(int vertical, int horizontal) {
        return new HyUIPadding(horizontal, vertical, horizontal, vertical);
    }

    public HyUIPadding(Integer left, Integer top, Integer right, Integer bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public Integer getLeft() {
        return left;
    }

    public HyUIPadding setLeft(Integer left) {
        this.left = left;
        return this;
    }

    public Integer getTop() {
        return top;
    }

    public HyUIPadding setTop(Integer top) {
        this.top = top;
        return this;
    }

    public Integer getRight() {
        return right;
    }

    public HyUIPadding setRight(Integer right) {
        this.right = right;
        return this;
    }

    public Integer getBottom() {
        return bottom;
    }

    public HyUIPadding setBottom(Integer bottom) {
        this.bottom = bottom;
        return this;
    }

    public HyUIPadding setFull(int value) {
        this.left = value;
        this.top = value;
        this.right = value;
        this.bottom = value;
        return this;
    }

    public HyUIPadding setSymmetric(int vertical, int horizontal) {
        this.left = horizontal;
        this.right = horizontal;
        this.top = vertical;
        this.bottom = vertical;
        return this;
    }
}
