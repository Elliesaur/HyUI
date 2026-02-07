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
 * Builder for creating ActionButton UI elements.
 */
public class ActionButtonBuilder extends UIElementBuilder<ActionButtonBuilder> {
    private Boolean disabled;
    private String keyBindingLabel;
    private Alignment alignment;
    private String actionName;

    public ActionButtonBuilder() {
        super(UIElements.ACTION_BUTTON, "#HyUIActionButton");
        withUiFile("Pages/Elements/ActionButton.ui");
        withWrappingGroup(true);
    }

    public static ActionButtonBuilder actionButton() {
        return new ActionButtonBuilder();
    }

    public ActionButtonBuilder withDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public ActionButtonBuilder withKeyBindingLabel(String keyBindingLabel) {
        this.keyBindingLabel = keyBindingLabel;
        return this;
    }

    public ActionButtonBuilder withAlignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public ActionButtonBuilder withActionName(String actionName) {
        this.actionName = actionName;
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

        if (disabled != null) {
            HyUIPlugin.getLog().logFinest("Setting Disabled: " + disabled + " for " + selector);
            commands.set(selector + ".Disabled", disabled);
        }
        if (keyBindingLabel != null) {
            HyUIPlugin.getLog().logFinest("Setting KeyBindingLabel: " + keyBindingLabel + " for " + selector);
            commands.set(selector + ".KeyBindingLabel", keyBindingLabel);
        }
        if (alignment != null) {
            HyUIPlugin.getLog().logFinest("Setting Alignment: " + alignment + " for " + selector);
            commands.set(selector + ".Alignment", alignment.name());
        }
        if (actionName != null) {
            HyUIPlugin.getLog().logFinest("Setting ActionName: " + actionName + " for " + selector);
            commands.set(selector + ".ActionName", actionName);
        }
    }
}
