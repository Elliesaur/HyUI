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

import java.util.List;

public class UIEventActions {
    public static final String BUTTON_CLICKED = "ButtonClicked";
    public static final String VALUE_CHANGED = "ValueChanged";
    public static final String FOCUS_LOST = "FocusLost";
    public static final String FOCUS_GAINED = "FocusGained";
    public static final String MOUSE_BUTTON_RELEASED = "MouseButtonReleased";
    public static final String DOUBLE_CLICKING = "DoubleClicking";
    public static final String RIGHT_CLICKING = "RightClicking";
    public static final String MOUSE_ENTERED = "MouseEntered";
    public static final String MOUSE_EXITED = "MouseExited";
    public static final String VALIDATING = "Validating";
    public static final String DISMISSING = "Dismissing";
    public static final String SCROLLED = "Scrolled";
    public static final String SELECTED_TAB_CHANGED = "SelectedTabChanged";
    public static final String SLOT_DOUBLE_CLICKING = "SlotDoubleClicking";
    public static final String DRAG_CANCELLED = "DragCancelled";
    public static final String SLOT_MOUSE_ENTERED = "SlotMouseEntered";
    public static final String SLOT_MOUSE_EXITED = "SlotMouseExited";

    public static final List<String> VALUE_ACTIONS = List.of(VALUE_CHANGED, FOCUS_LOST, FOCUS_GAINED);

    private UIEventActions() {}
}
