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

import au.ellie.hyui.builders.SceneBlurBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import org.jsoup.nodes.Element;

public class SceneBlurHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        String tag = element.tagName().toLowerCase();
        return tag.equals("scene-blur") || tag.equals("hyui-scene-blur");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        SceneBlurBuilder builder = SceneBlurBuilder.sceneBlur();
        applyCommonAttributes(builder, element);
        return builder;
    }
}
