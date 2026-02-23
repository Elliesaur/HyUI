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
 * Layout modes for child placement within a container.
 *
 * <ul>
 *     <li>Full: child elements fill the full size of the parent.</li>
 *     <li>Left: left-to-right, aligned left.</li>
 *     <li>Center: left-to-right, aligned center.</li>
 *     <li>Right: left-to-right, aligned right.</li>
 *     <li>Top: top-to-bottom, aligned top.</li>
 *     <li>Middle: top-to-bottom, aligned middle.</li>
 *     <li>Bottom: top-to-bottom, aligned bottom.</li>
 *     <li>LeftScrolling: left-to-right, aligned left, with scrolling enabled.</li>
 *     <li>RightScrolling: left-to-right, aligned right, with scrolling enabled.</li>
 *     <li>TopScrolling: top-to-bottom, aligned top, with scrolling enabled.</li>
 *     <li>BottomScrolling: top-to-bottom, aligned bottom, with scrolling enabled.</li>
 *     <li>CenterMiddle: left-to-right, centered horizontally and vertically.</li>
 *     <li>MiddleCenter: top-to-bottom, centered horizontally and vertically.</li>
 *     <li>LeftCenterWrap: left-to-right and top-to-bottom, centered horizontally.</li>
 * </ul>
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
