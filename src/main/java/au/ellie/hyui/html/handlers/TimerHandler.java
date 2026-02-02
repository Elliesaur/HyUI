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

package au.ellie.hyui.html.handlers;

import au.ellie.hyui.builders.TimerLabelBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import au.ellie.hyui.utils.ParseUtils;
import org.jsoup.nodes.Element;

/**
 * Handler for timer elements in HYUIML.
 *
 * Supports:
 * - &lt;timer&gt; tag
 * - &lt;span class="timer"&gt; tag
 *
 * Attributes:
 * - value: Time value in milliseconds (default: 0)
 * - format: Display format (hms, ms, seconds, human, milliseconds)
 * - prefix: Text before the time
 * - suffix: Text after the time
 * - data-hyui-time-seconds: Time value in seconds
 */
public class TimerHandler implements TagHandler {

    @Override
    public boolean canHandle(Element element) {
        return element.tagName().equalsIgnoreCase("timer") ||
                (element.tagName().equalsIgnoreCase("span") && element.hasClass("timer"));
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        TimerLabelBuilder builder = TimerLabelBuilder.timerLabel();

        applyCommonAttributes(builder, element);

        // Parse time value
        if (element.hasAttr("value")) {
            ParseUtils.parseLong(element.attr("value"))
                    .ifPresent(builder::withTimeMs);
        }

        // Parse time in seconds
        if (element.hasAttr("data-hyui-time-seconds")) {
            ParseUtils.parseLong(element.attr("data-hyui-time-seconds"))
                    .ifPresent(builder::withTimeSeconds);
        }

        // Parse format
        if (element.hasAttr("format")) {
            String formatStr = element.attr("format").toLowerCase();
            TimerLabelBuilder.TimerFormat format = switch (formatStr) {
                case "hms" -> TimerLabelBuilder.TimerFormat.HMS;
                case "ms" -> TimerLabelBuilder.TimerFormat.MS;
                case "seconds", "s" -> TimerLabelBuilder.TimerFormat.SECONDS;
                case "human", "human-readable" -> TimerLabelBuilder.TimerFormat.HUMAN_READABLE;
                case "milliseconds", "ms-full" -> TimerLabelBuilder.TimerFormat.MILLISECONDS;
                default -> TimerLabelBuilder.TimerFormat.MS;
            };
            builder.withFormat(format);
        }

        // Parse prefix/suffix
        if (element.hasAttr("prefix")) {
            builder.withPrefix(element.attr("prefix"));
        }
        if (element.hasAttr("suffix")) {
            builder.withSuffix(element.attr("suffix"));
        }

        // If element has text content, use it as raw text (overrides time formatting)
        String text = element.ownText().trim();
        if (!text.isEmpty()) {
            builder.withText(text);
        }

        return builder;
    }
}
