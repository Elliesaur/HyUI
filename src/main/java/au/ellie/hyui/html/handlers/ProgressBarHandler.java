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

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.builders.ProgressBarBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import au.ellie.hyui.types.ProgressBarAlignment;
import au.ellie.hyui.types.ProgressBarDirection;
import au.ellie.hyui.utils.ParseUtils;
import org.jsoup.nodes.Element;

public class ProgressBarHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        return element.tagName().equalsIgnoreCase("progress");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        boolean isCircular = element.hasClass("circular-progress") || element.hasClass("circular");
        ProgressBarBuilder builder = isCircular
                ? ProgressBarBuilder.circularProgressBar()
                : ProgressBarBuilder.progressBar();

        if (element.hasAttr("value")) {
            ParseUtils.parseFloat(element.attr("value")).ifPresent(value -> {
                // Value in a progress bar is between 0.0 and 1.0. 50 on a value attribute for html would be 0.5.
                builder.withValue(value > 1.0f ? value / 100.0f : value);
            });
        }

        if (element.hasAttr("data-hyui-effect-width")) {
            ParseUtils.parseInt(element.attr("data-hyui-effect-width"))
                    .ifPresent(builder::withEffectWidth);
        }
        if (element.hasAttr("data-hyui-effect-height")) {
            ParseUtils.parseInt(element.attr("data-hyui-effect-height"))
                    .ifPresent(builder::withEffectHeight);
        }
        if (element.hasAttr("data-hyui-effect-offset")) {
            ParseUtils.parseInt(element.attr("data-hyui-effect-offset"))
                    .ifPresent(builder::withEffectOffset);
        }

        if (element.hasAttr("data-hyui-direction")) {
            String directionValue = element.attr("data-hyui-direction");
            try {
                builder.withDirection(ProgressBarDirection.valueOf(directionValue));
            } catch (IllegalArgumentException e) {
                // Fall back to string method for backwards compatibility
                builder.withDirection(directionValue);
            }
        }

        if (element.hasAttr("data-hyui-bar-texture-path")) {
            builder.withBarTexturePath(element.attr("data-hyui-bar-texture-path"));
        }
        if (element.hasAttr("data-hyui-effect-texture-path")) {
            builder.withEffectTexturePath(element.attr("data-hyui-effect-texture-path"));
        }

        if (element.hasAttr("data-hyui-mask-texture-path")) {
            builder.withMaskTexturePath(element.attr("data-hyui-mask-texture-path"));
        }

        if (element.hasAttr("data-hyui-color")) {
            builder.withColor(element.attr("data-hyui-color"));
        }

        if (element.hasAttr("data-hyui-alignment")) {
            String alignmentValue = element.attr("data-hyui-alignment");
            try {
                builder.withAlignment(ProgressBarAlignment.valueOf(alignmentValue));
            } catch (IllegalArgumentException e) {
                // Fall back to string method for backwards compatibility
                builder.withAlignment(alignmentValue);
            }
        }

        applyCommonAttributes(builder, element);
        return builder;
    }
}
