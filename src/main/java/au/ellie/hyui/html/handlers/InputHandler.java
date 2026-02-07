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
                builder = resolveSliderBuilder(element);
                applySliderAttributes(builder, element);
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
                ColorPickerBuilder colorBuilder = (ColorPickerBuilder) builder;
                if (element.hasAttr("value")) {
                    colorBuilder.withValue(element.attr("value"));
                }
                if (element.hasAttr("data-hyui-display-text-field")) {
                    colorBuilder.withDisplayTextField(Boolean.parseBoolean(element.attr("data-hyui-display-text-field")));
                }
                if (element.hasAttr("data-hyui-reset-transparency-when-changing-color")) {
                    colorBuilder.withResetTransparencyWhenChangingColor(
                        Boolean.parseBoolean(element.attr("data-hyui-reset-transparency-when-changing-color")));
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

    private UIElementBuilder<?> resolveSliderBuilder(Element element) {
        if (element.hasClass("float-slider-number-field")) {
            return FloatSliderNumberFieldBuilder.floatSliderNumberField();
        }
        if (element.hasClass("slider-number-field")) {
            return SliderNumberFieldBuilder.sliderNumberField();
        }
        if (element.hasClass("float-slider")) {
            return FloatSliderBuilder.floatSlider();
        }
        return SliderBuilder.gameSlider();
    }

    private void applySliderAttributes(UIElementBuilder<?> builder, Element element) {
        String value = element.hasAttr("value") ? element.attr("value") : element.attr("data-hyui-value");
        String min = element.hasAttr("min") ? element.attr("min") : element.attr("data-hyui-min");
        String max = element.hasAttr("max") ? element.attr("max") : element.attr("data-hyui-max");
        String step = element.hasAttr("step") ? element.attr("step") : element.attr("data-hyui-step");

        if (builder instanceof FloatSliderBuilder floatSliderBuilder) {
            applyFloatSliderValues(floatSliderBuilder, value, min, max, step);
            return;
        }
        if (builder instanceof FloatSliderNumberFieldBuilder floatSliderNumberFieldBuilder) {
            applyFloatSliderValues(floatSliderNumberFieldBuilder, value, min, max, step);
            applyNumberFieldAnchor(floatSliderNumberFieldBuilder, element);
            if (element.hasAttr("data-hyui-number-field-max-decimal-places")) {
                ParseUtils.parseInt(element.attr("data-hyui-number-field-max-decimal-places"))
                        .ifPresent(floatSliderNumberFieldBuilder::withNumberFieldMaxDecimalPlaces);
            }
            return;
        }
        if (builder instanceof SliderNumberFieldBuilder sliderNumberFieldBuilder) {
            applyIntSliderValues(sliderNumberFieldBuilder, value, min, max, step);
            applyNumberFieldAnchor(sliderNumberFieldBuilder, element);
            return;
        }
        if (builder instanceof SliderBuilder sliderBuilder) {
            applyIntSliderValues(sliderBuilder, value, min, max, step);
            if (element.hasAttr("readonly") || element.hasAttr("data-hyui-is-read-only")) {
                sliderBuilder.withIsReadOnly(true);
            }
        }
    }

    private void applyFloatSliderValues(FloatSliderBuilder builder, String value, String min, String max, String step) {
        if (value != null && !value.isBlank()) {
            ParseUtils.parseDouble(value).ifPresent(v -> builder.withValue(v.floatValue()));
        }
        if (min != null && !min.isBlank()) {
            ParseUtils.parseDouble(min).ifPresent(v -> builder.withMin(v.floatValue()));
        }
        if (max != null && !max.isBlank()) {
            ParseUtils.parseDouble(max).ifPresent(v -> builder.withMax(v.floatValue()));
        }
        if (step != null && !step.isBlank()) {
            ParseUtils.parseDouble(step).ifPresent(v -> builder.withStep(v.floatValue()));
        }
    }

    private void applyFloatSliderValues(FloatSliderNumberFieldBuilder builder, String value, String min, String max, String step) {
        if (value != null && !value.isBlank()) {
            ParseUtils.parseDouble(value).ifPresent(v -> builder.withValue(v.floatValue()));
        }
        if (min != null && !min.isBlank()) {
            ParseUtils.parseDouble(min).ifPresent(v -> builder.withMin(v.floatValue()));
        }
        if (max != null && !max.isBlank()) {
            ParseUtils.parseDouble(max).ifPresent(v -> builder.withMax(v.floatValue()));
        }
        if (step != null && !step.isBlank()) {
            ParseUtils.parseDouble(step).ifPresent(v -> builder.withStep(v.floatValue()));
        }
    }

    private void applySliderValues(SliderNumberFieldBuilder builder, String value, String min, String max, String step) {
        if (value != null && !value.isBlank()) {
            ParseUtils.parseInt(value).ifPresent(builder::withValue);
        }
        if (min != null && !min.isBlank()) {
            ParseUtils.parseInt(min).ifPresent(builder::withMin);
        }
        if (max != null && !max.isBlank()) {
            ParseUtils.parseInt(max).ifPresent(builder::withMax);
        }
        if (step != null && !step.isBlank()) {
            ParseUtils.parseInt(step).ifPresent(builder::withStep);
        }
    }

    private void applyIntSliderValues(SliderBuilder builder, String value, String min, String max, String step) {
        if (value != null && !value.isBlank()) {
            ParseUtils.parseInt(value).ifPresent(builder::withValue);
        }
        if (min != null && !min.isBlank()) {
            ParseUtils.parseInt(min).ifPresent(builder::withMin);
        }
        if (max != null && !max.isBlank()) {
            ParseUtils.parseInt(max).ifPresent(builder::withMax);
        }
        if (step != null && !step.isBlank()) {
            ParseUtils.parseInt(step).ifPresent(builder::withStep);
        }
    }
    
    private void applyIntSliderValues(SliderNumberFieldBuilder builder, String value, String min, String max, String step) {
        if (value != null && !value.isBlank()) {
            ParseUtils.parseInt(value).ifPresent(builder::withValue);
        }
        if (min != null && !min.isBlank()) {
            ParseUtils.parseInt(min).ifPresent(builder::withMin);
        }
        if (max != null && !max.isBlank()) {
            ParseUtils.parseInt(max).ifPresent(builder::withMax);
        }
        if (step != null && !step.isBlank()) {
            ParseUtils.parseInt(step).ifPresent(builder::withStep);
        }
    }
    
    private void applyNumberFieldAnchor(SliderNumberFieldBuilder builder, Element element) {
        HyUIAnchor anchor = parseAnchor(element, "data-hyui-number-field-anchor-");
        if (anchor != null) {
            builder.withNumberFieldContainerAnchor(anchor);
        }
    }

    private void applyNumberFieldAnchor(FloatSliderNumberFieldBuilder builder, Element element) {
        HyUIAnchor anchor = parseAnchor(element, "data-hyui-number-field-anchor-");
        if (anchor != null) {
            builder.withNumberFieldContainerAnchor(anchor);
        }
    }

    private HyUIAnchor parseAnchor(Element element, String prefix) {
        HyUIAnchor anchor = new HyUIAnchor();
        boolean found = false;
        found |= setAnchorValue(element, prefix + "left", value -> anchor.setLeft(value));
        found |= setAnchorValue(element, prefix + "right", value -> anchor.setRight(value));
        found |= setAnchorValue(element, prefix + "top", value -> anchor.setTop(value));
        found |= setAnchorValue(element, prefix + "bottom", value -> anchor.setBottom(value));
        found |= setAnchorValue(element, prefix + "width", value -> anchor.setWidth(value));
        found |= setAnchorValue(element, prefix + "height", value -> anchor.setHeight(value));
        found |= setAnchorValue(element, prefix + "full", value -> anchor.setFull(value));
        found |= setAnchorValue(element, prefix + "horizontal", value -> anchor.setHorizontal(value));
        found |= setAnchorValue(element, prefix + "vertical", value -> anchor.setVertical(value));
        return found ? anchor : null;
    }

    private boolean setAnchorValue(Element element, String attr, java.util.function.IntConsumer setter) {
        if (!element.hasAttr(attr)) {
            return false;
        }
        return ParseUtils.parseInt(element.attr(attr)).map(value -> {
            setter.accept(value);
            return true;
        }).orElse(false);
    }
}
