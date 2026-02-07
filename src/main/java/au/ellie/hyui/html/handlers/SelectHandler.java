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

import au.ellie.hyui.builders.DropdownBoxBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import au.ellie.hyui.utils.ParseUtils;
import org.jsoup.nodes.Element;

public class SelectHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        return element.tagName().equalsIgnoreCase("select");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        DropdownBoxBuilder builder = DropdownBoxBuilder.dropdownBox();

        if (element.hasAttr("value")) {
            builder.withValue(element.attr("value"));
        }

        if (element.hasAttr("data-hyui-allowunselection")) {
            builder.withAllowUnselection(Boolean.parseBoolean(element.attr("data-hyui-allowunselection")));
        }

        if (element.hasAttr("data-hyui-maxselection")) {
            ParseUtils.parseInt(element.attr("data-hyui-maxselection"))
                    .ifPresent(builder::withMaxSelection);
        }

        if (element.hasAttr("data-hyui-entryheight")) {
            ParseUtils.parseInt(element.attr("data-hyui-entryheight"))
                    .ifPresent(builder::withEntryHeight);
        }

        if (element.hasAttr("data-hyui-showlabel")) {
            builder.withShowLabel(Boolean.parseBoolean(element.attr("data-hyui-showlabel")));
        }

        if (element.hasAttr("disabled") || element.hasAttr("data-hyui-disabled")) {
            builder.withDisabled(true);
        }

        if (element.hasAttr("data-hyui-is-read-only")) {
            builder.withIsReadOnly(Boolean.parseBoolean(element.attr("data-hyui-is-read-only")));
        }

        if (element.hasAttr("data-hyui-show-search-input")) {
            builder.withShowSearchInput(Boolean.parseBoolean(element.attr("data-hyui-show-search-input")));
        }

        if (element.hasAttr("data-hyui-forced-label")) {
            builder.withForcedLabel(element.attr("data-hyui-forced-label"));
        }

        if (element.hasAttr("data-hyui-display-non-existing-value")) {
            builder.withDisplayNonExistingValue(Boolean.parseBoolean(element.attr("data-hyui-display-non-existing-value")));
        }

        for (Element option : element.select("option")) {
            String val = option.hasAttr("value") ? option.attr("value") : option.text();

            String label = option.text();
            builder.addEntry(val, label);

            // We override if any options have selected.
            if (option.hasAttr("selected")) {
                builder.withValue(val);
            }
        }

        applyCommonAttributes(builder, element);
        return builder;
    }
}
