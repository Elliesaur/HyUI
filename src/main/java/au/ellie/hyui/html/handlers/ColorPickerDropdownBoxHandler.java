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

import au.ellie.hyui.types.ColorFormat;
import au.ellie.hyui.builders.ColorPickerDropdownBoxBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import org.jsoup.nodes.Element;

public class ColorPickerDropdownBoxHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        String tag = element.tagName().toLowerCase();
        return tag.equals("color-picker-dropdown-box")
                || tag.equals("color-picker-dropdown")
                || tag.equals("hyui-color-picker-dropdown-box");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        ColorPickerDropdownBoxBuilder builder = ColorPickerDropdownBoxBuilder.colorPickerDropdownBox();

        String formatValue = null;
        if (element.hasAttr("data-hyui-format")) {
            formatValue = element.attr("data-hyui-format");
        } else if (element.hasAttr("format")) {
            formatValue = element.attr("format");
        }
        if (formatValue != null) {
            if (formatValue.equalsIgnoreCase("rgba")) {
                builder.withFormat(ColorFormat.Rgba);
            } else if (formatValue.equalsIgnoreCase("rgb")) {
                builder.withFormat(ColorFormat.Rgb);
            }
        }

        if (element.hasAttr("data-hyui-display-text-field")) {
            builder.withDisplayTextField(Boolean.parseBoolean(element.attr("data-hyui-display-text-field")));
        } else if (element.hasAttr("display-text-field")) {
            builder.withDisplayTextField(Boolean.parseBoolean(element.attr("display-text-field")));
        }

        if (element.hasAttr("data-hyui-reset-transparency-when-changing-color")) {
            builder.withResetTransparencyWhenChangingColor(
                Boolean.parseBoolean(element.attr("data-hyui-reset-transparency-when-changing-color")));
        } else if (element.hasAttr("reset-transparency-when-changing-color")) {
            builder.withResetTransparencyWhenChangingColor(
                Boolean.parseBoolean(element.attr("reset-transparency-when-changing-color")));
        }

        if (!element.hasClass("default-style")) {
            // Force default style and override after.
            element.addClass("default-style");
        }
        applyCommonAttributes(builder, element);
        return builder;
    }
}
