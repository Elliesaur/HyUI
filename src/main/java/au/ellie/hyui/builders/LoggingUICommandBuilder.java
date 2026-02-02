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

import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper for UICommandBuilder that logs all .set() calls.
 */
public class LoggingUICommandBuilder extends UICommandBuilder {
    private final List<String> commandLog = new ArrayList<>();

    @Override
    public UICommandBuilder set(String path, String value) {
        commandLog.add("set(" + path + ", " + value + ")");
        return super.set(path, value);
    }

    @Override
    public UICommandBuilder set(String path, boolean value) {
        commandLog.add("set(" + path + ", " + value + ")");
        return super.set(path, value);
    }

    @Override
    public UICommandBuilder set(String path, int value) {
        commandLog.add("set(" + path + ", " + value + ")");
        return super.set(path, value);
    }

    @Override
    public UICommandBuilder set(String path, float value) {
        commandLog.add("set(" + path + ", " + value + ")");
        return super.set(path, value);
    }

    @Override
    public UICommandBuilder set(String path, double value) {
        commandLog.add("set(" + path + ", " + value + ")");
        return super.set(path, value);
    }

    @Override
    public UICommandBuilder set(String path, com.hypixel.hytale.server.core.Message value) {
        commandLog.add("set(" + path + ", " + value + ")");
        return super.set(path, value);
    }

    @Override
    public UICommandBuilder set(String path, Value value) {
        commandLog.add("set(" + path + ", " + value.toString() + ")");
        return super.set(path, value);
    }

    public List<String> getCommandLog() {
        return new ArrayList<>(commandLog);
    }
}
