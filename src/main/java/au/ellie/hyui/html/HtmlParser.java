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

package au.ellie.hyui.html;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.builders.LabelBuilder;
import au.ellie.hyui.builders.InterfaceBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.handlers.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.List;

/**
 * A modular parser that converts HTML/XML-like language to HyUI builders.
 */
public class HtmlParser {
    private final List<TagHandler> handlers = new ArrayList<>();
    private TemplateProcessor templateProcessor;
    
    public HtmlParser() {
        // Register default handlers
        registerHandler(new ItemGridHandler());
        registerHandler(new TabContentHandler());
        registerHandler(new DivHandler());
        registerHandler(new LabelHandler());
        registerHandler(new InputHandler());
        registerHandler(new TextAreaHandler());
        registerHandler(new ButtonHandler());
        registerHandler(new ImgHandler());
        registerHandler(new HyvatarHandler());
        registerHandler(new ProgressBarHandler());
        registerHandler(new ItemIconHandler());
        registerHandler(new ItemSlotHandler());
        registerHandler(new SelectHandler());
        registerHandler(new SpriteHandler());
        registerHandler(new TimerHandler());
        registerHandler(new TabNavigationHandler());
    }

    /**
     * Registers a new tag handler.
     *
     * @param handler The handler to register.
     */
    public void registerHandler(TagHandler handler) {
        handlers.add(handler);
    }

    /**
     * Sets the template processor for variable interpolation and component inclusion.
     *
     * @param processor The template processor to use.
     */
    public void setTemplateProcessor(TemplateProcessor processor) {
        this.templateProcessor = processor;
    }

    /**
     * Gets the current template processor.
     *
     * @return The template processor, or null if not set.
     */
    public TemplateProcessor getTemplateProcessor() {
        return templateProcessor;
    }
    
    /**
     * Parses the HTML string and adds elements to the InterfaceBuilder.
     *
     * @param builder The InterfaceBuilder to add elements to.
     * @param html    The HTML string to parse.
     */
    public void parseToInterface(InterfaceBuilder<?> builder, String html) {
        List<UIElementBuilder<?>> elements = parse(html);
        for (UIElementBuilder<?> element : elements) {
            builder.addElement(element);
        }
    }

    /**
     * Parses the HTML string into a list of UIElementBuilders.
     *
     * @param html The HTML string to parse.
     * @return A list of UIElementBuilders.
     */
    public List<UIElementBuilder<?>> parse(String html) {
        // Apply template processing if a processor is set
        String processedHtml = html;
        if (templateProcessor != null) {
            processedHtml = templateProcessor.process(html);
            HyUIPlugin.getLog().logFinest("Processed template: " + processedHtml);
        }
        Document doc = Jsoup.parseBodyFragment(processedHtml);
        new CssPreprocessor().process(doc);
        HyUIPlugin.getLog().logFinest("Document elements after preprocessing: " + doc.body().html());
        return parseChildren(doc.body());
    }

    /**
     * Parses the children of a Jsoup element.
     *
     * @param parent The parent Jsoup element.
     * @return A list of UIElementBuilders.
     */
    public List<UIElementBuilder<?>> parseChildren(Element parent) {
        List<UIElementBuilder<?>> builders = new ArrayList<>();
        for (Node child : parent.childNodes()) {
            HyUIPlugin.getLog().logFinest("Parsing child node: " + child.nodeName());
            
            if (child instanceof Element) {
                HyUIPlugin.getLog().logFinest("Parsing ELEMENT node: " + child.nodeName());
                
                UIElementBuilder<?> builder = handleElement((Element) child);
                if (builder != null) {
                    HyUIPlugin.getLog().logFinest("Parsed element: " + builder.getClass().getSimpleName());
                    builders.add(builder);
                }
            } else if (child instanceof TextNode) {
                String text = ((TextNode) child).text().trim();
                if (!text.isEmpty()) {
                    builders.add(LabelBuilder.label().withText(text));
                }
            }
        }
        return builders;
    }

    public UIElementBuilder<?> handleElement(Element element) {
        for (TagHandler handler : handlers) {
            if (handler.canHandle(element)) {
                return handler.handle(element, this);
            }
        }
        return null;
    }
}
