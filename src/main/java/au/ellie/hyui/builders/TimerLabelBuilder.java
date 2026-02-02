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
import au.ellie.hyui.elements.BackgroundSupported;
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.theme.Theme;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Builder for creating timer label UI elements.
 * TimerLabels display formatted time values (countdown, elapsed time, etc.)
 *
 * This is a specialized Label that provides time formatting utilities.
 * The actual timer logic should be handled externally (e.g., via a TickingSystem).
 */
public class TimerLabelBuilder extends UIElementBuilder<TimerLabelBuilder> implements BackgroundSupported<TimerLabelBuilder> {

    public enum TimerFormat {
        /** Display as HH:MM:SS (e.g., "01:23:45") */
        HMS,
        /** Display as MM:SS (e.g., "23:45") */
        MS,
        /** Display as seconds only (e.g., "45s") */
        SECONDS,
        /** Display as human readable (e.g., "1h 23m 45s") */
        HUMAN_READABLE,
        /** Display as milliseconds (e.g., "1234ms") */
        MILLISECONDS
    }

    private String text;
    private long timeValueMs = 0;
    private TimerFormat format = TimerFormat.MS;
    private String prefix = "";
    private String suffix = "";
    private HyUIPatchStyle background;

    public TimerLabelBuilder() {
        super(UIElements.LABEL, "Label");
    }

    public TimerLabelBuilder(Theme theme) {
        super(theme, UIElements.LABEL, "Label");
    }

    /**
     * Factory method to create a new TimerLabelBuilder.
     */
    public static TimerLabelBuilder timerLabel() {
        return new TimerLabelBuilder();
    }

    /**
     * Sets the time value to display in milliseconds.
     */
    public TimerLabelBuilder withTimeMs(long milliseconds) {
        this.timeValueMs = milliseconds;
        this.text = null; // Clear manual text
        return this;
    }

    /**
     * Sets the time value to display in seconds.
     */
    public TimerLabelBuilder withTimeSeconds(long seconds) {
        return withTimeMs(seconds * 1000);
    }

    /**
     * Sets the time value from a duration.
     */
    public TimerLabelBuilder withTime(long value, TimeUnit unit) {
        return withTimeMs(unit.toMillis(value));
    }

    /**
     * Sets the display format for the timer.
     */
    public TimerLabelBuilder withFormat(TimerFormat format) {
        this.format = format;
        return this;
    }

    /**
     * Sets a prefix to display before the time (e.g., "Time: ").
     */
    public TimerLabelBuilder withPrefix(String prefix) {
        this.prefix = prefix != null ? prefix : "";
        return this;
    }

    /**
     * Sets a suffix to display after the time (e.g., " remaining").
     */
    public TimerLabelBuilder withSuffix(String suffix) {
        this.suffix = suffix != null ? suffix : "";
        return this;
    }

    /**
     * Sets raw text, bypassing time formatting.
     */
    public TimerLabelBuilder withText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public TimerLabelBuilder withBackground(HyUIPatchStyle background) {
        this.background = background;
        return this;
    }

    @Override
    public HyUIPatchStyle getBackground() {
        return this.background;
    }

    /**
     * Formats the time value according to the current format setting.
     */
    public String formatTime(long timeMs) {
        long totalSeconds = timeMs / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        long ms = timeMs % 1000;

        return switch (format) {
            case HMS -> String.format("%02d:%02d:%02d", hours, minutes, seconds);
            case MS -> String.format("%02d:%02d", minutes, seconds);
            case SECONDS -> seconds + "s";
            case MILLISECONDS -> timeMs + "ms";
            case HUMAN_READABLE -> {
                StringBuilder sb = new StringBuilder();
                if (hours > 0) sb.append(hours).append("h ");
                if (minutes > 0 || hours > 0) sb.append(minutes).append("m ");
                sb.append(seconds).append("s");
                yield sb.toString().trim();
            }
        };
    }

    /**
     * Gets the current display text (formatted time or raw text).
     */
    public String getDisplayText() {
        if (text != null) {
            return text;
        }
        return prefix + formatTime(timeValueMs) + suffix;
    }

    public long getTimeValueMs() {
        return timeValueMs;
    }

    public TimerFormat getFormat() {
        return format;
    }

    @Override
    protected boolean supportsStyling() {
        return true;
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        applyBackground(commands, selector);

        String displayText = getDisplayText();
        HyUIPlugin.getLog().logFinest("Setting Timer Text: " + displayText + " for " + selector);
        commands.set(selector + ".Text", displayText);

        if (hyUIStyle == null && style != null) {
            HyUIPlugin.getLog().logFinest("Setting Raw Style: " + style + " for " + selector);
            commands.set(selector + ".Style", style);
        }
    }
}
