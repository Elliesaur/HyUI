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
