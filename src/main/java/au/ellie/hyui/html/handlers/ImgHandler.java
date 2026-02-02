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

import au.ellie.hyui.builders.DynamicImageBuilder;
import au.ellie.hyui.builders.ImageBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import org.jsoup.nodes.Element;

public class ImgHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        return element.tagName().equalsIgnoreCase("img");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        UIElementBuilder<?> builder;

        boolean isDynamicImage = element.hasAttr("class") && element.attr("class").contains("dynamic-image");
        if (isDynamicImage) {
            DynamicImageBuilder dynamicImage = DynamicImageBuilder.dynamicImage();
            if (element.hasAttr("src")) {
                dynamicImage.withImageUrl(element.attr("src"));
            }
            builder = dynamicImage;
        } else {
            ImageBuilder image = ImageBuilder.image();
            if (element.hasAttr("src")) {
                image.withImage(element.attr("src"));
            }
            builder = image;
        }
        
        applyCommonAttributes(builder, element);
        
        return builder;
    }
}
