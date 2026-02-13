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

/**
 * Name	Description
 * Full	Child elements will fill the full size of its parent
 * Left	Child elements will be laid out from left to right, aligned to the left of their parent
 * Center	Child elements will be laid out from left to right, aligned to the center of their parent
 * Right	Child elements will be laid out from left to right, aligned to the right of their parent
 * Top	Child elements will be laid out from top to bottom, aligned to the top of their parent
 * Middle	Child elements will be laid out from top to bottom, aligned to the middle of their parent
 * Bottom	Child elements will be laid out from top to bottom, aligned to the bottom of their parent
 * LeftScrolling	Child elements will be laid out from left to right, aligned to the left of their parent. Enables scrolling
 * RightScrolling	Child elements will be laid out from left to right, aligned to the right of their parent. Enables scrolling
 * TopScrolling	Child elements will be laid out from top to bottom, aligned to the top of their parent. Enables scrolling
 * BottomScrolling	Child elements will be laid out from top to bottom, aligned to the bottom of their parent. Enables scrolling
 * CenterMiddle	Child elements will be laid out from left to right – vertically and horizontally centered
 * MiddleCenter	Child elements will be laid out from top to bottom – vertically and horizontally centered
 * LeftCenterWrap	Child elements will be laid out from left to right and top to bottom - horizontally centered
 */
public enum LayoutMode {
    Full,
    Left,
    Center,
    Right,
    Top,
    Middle,
    Bottom,
    LeftScrolling,
    RightScrolling,
    TopScrolling,
    BottomScrolling,
    CenterMiddle,
    MiddleCenter,
    LeftCenterWrap
}
