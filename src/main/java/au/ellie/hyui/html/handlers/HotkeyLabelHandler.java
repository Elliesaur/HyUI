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

import au.ellie.hyui.builders.HotkeyLabelBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import org.jsoup.nodes.Element;

public class HotkeyLabelHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        String tag = element.tagName().toLowerCase();
        return tag.equals("hotkey-label") || tag.equals("hyui-hotkey-label");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        HotkeyLabelBuilder builder = HotkeyLabelBuilder.hotkeyLabel();

        if (element.hasAttr("data-hyui-input-binding-key")) {
            builder.withInputBindingKey(element.attr("data-hyui-input-binding-key"));
        } else if (element.hasAttr("input-binding-key")) {
            builder.withInputBindingKey(element.attr("input-binding-key"));
        }
        if (element.hasAttr("data-hyui-input-binding-key-prefix")) {
            builder.withInputBindingKeyPrefix(element.attr("data-hyui-input-binding-key-prefix"));
        } else if (element.hasAttr("input-binding-key-prefix")) {
            builder.withInputBindingKeyPrefix(element.attr("input-binding-key-prefix"));
        }

        applyCommonAttributes(builder, element);
        return builder;
    }
}
