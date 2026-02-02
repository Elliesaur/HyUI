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

    public static final List<String> VALUE_ACTIONS = List.of(VALUE_CHANGED, FOCUS_LOST, FOCUS_GAINED);
    
    private UIEventActions() {}
}
