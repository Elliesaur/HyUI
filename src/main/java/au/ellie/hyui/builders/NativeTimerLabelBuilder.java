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
import au.ellie.hyui.types.TimerDirection;
import au.ellie.hyui.utils.PropertyBatcher;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import java.util.Set;

/**
 * Builder for native Hytale TimerLabel UI elements.
 * This is the native Hytale TimerLabel, not the custom HyUI timer system.
 */
public class NativeTimerLabelBuilder extends UIElementBuilder<NativeTimerLabelBuilder> {
    private Integer seconds;
    private String text;
    private TimerDirection direction;
    private Boolean paused;

    public NativeTimerLabelBuilder() {
        super("TimerLabel", "#HyUINativeTimerLabel");
        withUiFile("Pages/Elements/NativeTimerLabel.ui");
        withWrappingGroup(true);
    }

    public static NativeTimerLabelBuilder nativeTimerLabel() {
        return new NativeTimerLabelBuilder();
    }

    public NativeTimerLabelBuilder withSeconds(int seconds) {
        this.seconds = seconds;
        return this;
    }

    public NativeTimerLabelBuilder withText(String text) {
        this.text = text;
        return this;
    }

    public NativeTimerLabelBuilder withDirection(TimerDirection direction) {
        this.direction = direction;
        return this;
    }

    public NativeTimerLabelBuilder withPaused(boolean paused) {
        this.paused = paused;
        return this;
    }

    @Override
    protected boolean supportsStyling() {
        return true;
    }

    @Override
    protected boolean isStyleWhitelist() {
        return true;
    }

    @Override
    protected Set<String> getSupportedStyleProperties() {
        return Set.of(
                "HorizontalAlignment",
                "VerticalAlignment",
                "Wrap",
                "FontName",
                "FontSize",
                "TextColor",
                "OutlineColor",
                "LetterSpacing",
                "RenderUppercase",
                "RenderBold",
                "RenderItalics",
                "RenderUnderlined",
                "Alignment"
        );
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (seconds != null) {
            HyUIPlugin.getLog().logFinest("Setting Seconds: " + seconds + " for " + selector);
            commands.set(selector + ".Seconds", seconds);
        }
        if (text != null) {
            HyUIPlugin.getLog().logFinest("Setting Text: " + text + " for " + selector);
            commands.set(selector + ".Text", text);
        }
        if (direction != null) {
            HyUIPlugin.getLog().logFinest("Setting Direction: " + direction + " for " + selector);
            commands.set(selector + ".Direction", direction.name());
        }
        if (paused != null) {
            HyUIPlugin.getLog().logFinest("Setting Paused: " + paused + " for " + selector);
            commands.set(selector + ".Paused", paused);
        }

        if (hyUIStyle == null && typedStyle == null && style != null) {
            HyUIPlugin.getLog().logFinest("Setting Raw Style: " + style + " for " + selector);
            commands.set(selector + ".Style", style);
        } else if (hyUIStyle == null && typedStyle != null) {
            PropertyBatcher.endSet(selector + ".Style", filterStyleDocument(typedStyle.toBsonDocument()), commands);
        }
    }
}
