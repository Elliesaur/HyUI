package au.ellie.hyui.html.handlers;

import au.ellie.hyui.builders.*;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import au.ellie.hyui.utils.ParseUtils;
import org.jsoup.nodes.Element;

public class InputHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        return element.tagName().equalsIgnoreCase("input");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        String type = element.attr("type").toLowerCase();
        UIElementBuilder<?> builder = null;

        switch (type) {
            case "text":
                builder = TextFieldBuilder.textInput();
                applyTextFieldAttributes((TextFieldBuilder) builder, element);
                break;
            case "password":
                builder = TextFieldBuilder.textInput().withPassword(true);
                applyTextFieldAttributes((TextFieldBuilder) builder, element);
                break;
            case "number":
                builder = NumberFieldBuilder.numberInput();
                NumberFieldBuilder numBuilder = (NumberFieldBuilder) builder;
                if (element.hasAttr("value")) {
                    ParseUtils.parseDouble(element.attr("value"))
                            .ifPresent(numBuilder::withValue);
                }
                if (element.hasAttr("format")) {
                    numBuilder.withFormat(element.attr("format"));
                }
                if (element.hasAttr("data-hyui-max-decimal-places")) {
                    ParseUtils.parseDouble(element.attr("data-hyui-max-decimal-places"))
                            .ifPresent(numBuilder::withMaxDecimalPlaces);
                }
                if (element.hasAttr("min")) {
                    ParseUtils.parseDouble(element.attr("min"))
                            .ifPresent(numBuilder::withMinValue);
                }
                if (element.hasAttr("max")) {
                    ParseUtils.parseDouble(element.attr("max"))
                            .ifPresent(numBuilder::withMaxValue);
                }
                if (element.hasAttr("step")) {
                    ParseUtils.parseDouble(element.attr("step"))
                            .ifPresent(numBuilder::withStep);
                }
                if (element.hasAttr("data-hyui-min")) {
                    ParseUtils.parseDouble(element.attr("data-hyui-min"))
                            .ifPresent(numBuilder::withMinValue);
                }
                if (element.hasAttr("data-hyui-max")) {
                    ParseUtils.parseDouble(element.attr("data-hyui-max"))
                            .ifPresent(numBuilder::withMaxValue);
                }
                if (element.hasAttr("data-hyui-step")) {
                    ParseUtils.parseDouble(element.attr("data-hyui-step"))
                            .ifPresent(numBuilder::withStep);
                }
                break;
            case "range":
                builder = SliderBuilder.gameSlider();
                SliderBuilder sliderBuilder = (SliderBuilder) builder;
                if (element.hasAttr("value")) {
                    ParseUtils.parseInt(element.attr("value"))
                            .ifPresent(sliderBuilder::withValue);
                }
                if (element.hasAttr("min")) {
                    ParseUtils.parseInt(element.attr("min"))
                            .ifPresent(sliderBuilder::withMin);
                }
                if (element.hasAttr("max")) {
                    ParseUtils.parseInt(element.attr("max"))
                            .ifPresent(sliderBuilder::withMax);
                }
                if (element.hasAttr("step")) {
                    ParseUtils.parseInt(element.attr("step"))
                            .ifPresent(sliderBuilder::withStep);
                }

                // Support data-hyui-* attributes for slider as well
                if (element.hasAttr("data-hyui-min")) {
                    ParseUtils.parseInt(element.attr("data-hyui-min"))
                            .ifPresent(sliderBuilder::withMin);
                }
                if (element.hasAttr("data-hyui-max")) {
                    ParseUtils.parseInt(element.attr("data-hyui-max"))
                            .ifPresent(sliderBuilder::withMax);
                }
                if (element.hasAttr("data-hyui-step")) {
                    ParseUtils.parseInt(element.attr("data-hyui-step"))
                            .ifPresent(sliderBuilder::withStep);
                }
                break;
            case "checkbox":
                builder = new CheckBoxBuilder();
                if (element.hasAttr("checked")) {
                    ((CheckBoxBuilder) builder).withValue(true);
                } else if (element.hasAttr("value")) {
                    ((CheckBoxBuilder) builder).withValue(Boolean.parseBoolean(element.attr("value")));
                }
                break;
            case "color":
                builder = new ColorPickerBuilder();
                if (element.hasAttr("value")) {
                    ((ColorPickerBuilder) builder).withValue(element.attr("value"));
                }
                break;
            case "submit":
            case "reset":
                return new ButtonHandler().handle(element, parser);
        }

        if (builder != null) {
            applyCommonAttributes(builder, element);
        }

        return builder;
    }

    private void applyTextFieldAttributes(TextFieldBuilder builder, Element element) {
        if (element.hasAttr("value")) {
            builder.withValue(element.attr("value"));
        }
        if (element.hasAttr("placeholder")) {
            builder.withPlaceholderText(element.attr("placeholder"));
        }
        if (element.hasAttr("maxlength")) {
            ParseUtils.parseInt(element.attr("maxlength"))
                    .ifPresent(builder::withMaxLength);
        }
        if (element.hasAttr("readonly")) {
            builder.withReadOnly(true);
        }
    }
}
