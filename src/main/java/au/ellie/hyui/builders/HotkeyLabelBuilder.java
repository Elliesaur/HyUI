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

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

/**
 * Builder for HotkeyLabel UI elements.
 */
public class HotkeyLabelBuilder extends UIElementBuilder<HotkeyLabelBuilder> {
    private String inputBindingKey;
    private String inputBindingKeyPrefix;

    public HotkeyLabelBuilder() {
        super(UIElements.HOTKEY_LABEL, "#HyUIHotkeyLabel");
        withUiFile("Pages/Elements/HotkeyLabel.ui");
        withWrappingGroup(true);
    }

    public static HotkeyLabelBuilder hotkeyLabel() {
        return new HotkeyLabelBuilder();
    }

    public HotkeyLabelBuilder withInputBindingKey(String inputBindingKey) {
        this.inputBindingKey = inputBindingKey;
        return this;
    }

    public HotkeyLabelBuilder withInputBindingKeyPrefix(String inputBindingKeyPrefix) {
        this.inputBindingKeyPrefix = inputBindingKeyPrefix;
        return this;
    }

    @Override
    protected boolean supportsStyling() {
        return false;
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (inputBindingKey != null) {
            HyUIPlugin.getLog().logFinest("Setting InputBindingKey: " + inputBindingKey + " for " + selector);
            commands.set(selector + ".InputBindingKey", inputBindingKey);
        }
        if (inputBindingKeyPrefix != null) {
            HyUIPlugin.getLog().logFinest("Setting InputBindingKeyPrefix: " + inputBindingKeyPrefix + " for " + selector);
            commands.set(selector + ".InputBindingKeyPrefix", inputBindingKeyPrefix);
        }
    }
}
