package au.ellie.hyui.uiparser;

import app.ultradev.hytaleuiparser.ast.NodeArray;
import app.ultradev.hytaleuiparser.ast.NodeColor;
import app.ultradev.hytaleuiparser.ast.NodeConstant;
import app.ultradev.hytaleuiparser.ast.NodeRefMember;
import app.ultradev.hytaleuiparser.ast.NodeType;
import app.ultradev.hytaleuiparser.ast.NodeVariable;
import app.ultradev.hytaleuiparser.ast.VariableReference;
import app.ultradev.hytaleuiparser.ast.VariableValue;
import app.ultradev.hytaleuiparser.asttools.VariableKt;
import app.ultradev.hytaleuiparser.generated.types.Anchor;
import app.ultradev.hytaleuiparser.generated.types.LabelStyle;
import app.ultradev.hytaleuiparser.generated.types.Padding;
import app.ultradev.hytaleuiparser.generated.types.PatchStyle;
import app.ultradev.hytaleuiparser.validation.types.TypeType;
import au.ellie.hyui.builders.HyUIAnchor;
import au.ellie.hyui.builders.HyUIPadding;
import au.ellie.hyui.builders.HyUIPatchStyle;
import au.ellie.hyui.builders.HyUIStyle;
import au.ellie.hyui.builders.Alignment;
import au.ellie.hyui.types.HyUIBsonSerializable;
import au.ellie.hyui.types.*;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;

final class UIValueConverter {
    private static final Map<TypeType, Class<?>> TYPE_CLASS_MAP = new EnumMap<>(TypeType.class);
    private static final Map<Class<?>, TypeType> CLASS_TYPE_MAP = new HashMap<>();

    static {
        TYPE_CLASS_MAP.put(TypeType.SoundStyle, SoundStyle.class);
        TYPE_CLASS_MAP.put(TypeType.SoundsStyle, SoundsStyle.class);
        TYPE_CLASS_MAP.put(TypeType.ButtonSounds, ButtonSounds.class);
        TYPE_CLASS_MAP.put(TypeType.ScrollbarStyle, ScrollbarStyle.class);
        TYPE_CLASS_MAP.put(TypeType.CheckBoxStyleState, CheckBoxStyleState.class);
        TYPE_CLASS_MAP.put(TypeType.CheckBoxStyle, CheckBoxStyle.class);
        TYPE_CLASS_MAP.put(TypeType.CheckedStyle, CheckedStyle.class);
        TYPE_CLASS_MAP.put(TypeType.ButtonStyleState, ButtonStyleState.class);
        TYPE_CLASS_MAP.put(TypeType.ButtonStyle, ButtonStyle.class);
        TYPE_CLASS_MAP.put(TypeType.ToggleButtonStyleState, ButtonStyleState.class);
        TYPE_CLASS_MAP.put(TypeType.ToggleButtonStyle, ButtonStyle.class);
        TYPE_CLASS_MAP.put(TypeType.TextButtonStyleState, TextButtonStyleState.class);
        TYPE_CLASS_MAP.put(TypeType.TextButtonStyle, TextButtonStyle.class);
        TYPE_CLASS_MAP.put(TypeType.SliderStyle, SliderStyle.class);
        TYPE_CLASS_MAP.put(TypeType.InputFieldStyle, InputFieldStyle.class);
        TYPE_CLASS_MAP.put(TypeType.TextTooltipStyle, TextTooltipStyle.class);
        TYPE_CLASS_MAP.put(TypeType.InputFieldIcon, InputFieldIcon.class);
        TYPE_CLASS_MAP.put(TypeType.InputFieldButtonStyle, InputFieldButtonStyle.class);
        TYPE_CLASS_MAP.put(TypeType.InputFieldDecorationStyleState, InputFieldDecorationStyleState.class);
        TYPE_CLASS_MAP.put(TypeType.InputFieldDecorationStyle, InputFieldDecorationStyle.class);
        TYPE_CLASS_MAP.put(TypeType.ColorPickerStyle, ColorPickerStyle.class);
        TYPE_CLASS_MAP.put(TypeType.ColorPickerDropdownBoxStateBackground, ColorPickerDropdownBoxStateBackground.class);
        TYPE_CLASS_MAP.put(TypeType.ColorPickerDropdownBoxStyle, ColorPickerDropdownBoxStyle.class);
        TYPE_CLASS_MAP.put(TypeType.SpriteFrame, SpriteFrame.class);
        TYPE_CLASS_MAP.put(TypeType.NumberFieldFormat, NumberFieldFormat.class);
        TYPE_CLASS_MAP.put(TypeType.ItemGridStyle, ItemGridStyle.class);
        TYPE_CLASS_MAP.put(TypeType.TabStateStyle, TabStateStyle.class);
        TYPE_CLASS_MAP.put(TypeType.TabStyle, TabStyle.class);
        TYPE_CLASS_MAP.put(TypeType.TabNavigationStyle, TabNavigationStyle.class);
        TYPE_CLASS_MAP.put(TypeType.DropdownBoxSounds, DropdownBoxSounds.class);
        TYPE_CLASS_MAP.put(TypeType.DropdownBoxStyle, DropdownBoxStyle.class);
        TYPE_CLASS_MAP.put(TypeType.PopupStyle, PopupStyle.class);
        TYPE_CLASS_MAP.put(TypeType.SubMenuItemStyleState, SubMenuItemStyleState.class);
        TYPE_CLASS_MAP.put(TypeType.SubMenuItemStyle, SubMenuItemStyle.class);
        TYPE_CLASS_MAP.put(TypeType.MenuItemStyle, MenuItemStyle.class);
        TYPE_CLASS_MAP.put(TypeType.BlockSelectorStyle, BlockSelectorStyle.class);
        TYPE_CLASS_MAP.put(TypeType.LabeledCheckBoxStyleState, LabeledCheckBoxStyleState.class);
        TYPE_CLASS_MAP.put(TypeType.LabeledCheckBoxStyle, LabeledCheckBoxStyle.class);
        TYPE_CLASS_MAP.forEach((key, value) -> CLASS_TYPE_MAP.put(value, key));
    }

    private final UIParseResult result;
    private final String documentPath;

    UIValueConverter(UIParseResult result, String documentPath) {
        this.result = result;
        this.documentPath = documentPath;
    }

    UiStyleReference resolveStyleReference(VariableValue value) {
        if (!(value instanceof VariableReference reference)) {
            return null;
        }

        if (reference instanceof NodeRefMember refMember) {
            String document = null;
            if (refMember.getReference() != null && refMember.getReference().getResolvedAssignment() != null) {
                document = refMember.getReference().getResolvedAssignment().getResolvedFilePath();
            }
            if (document == null || document.isBlank()) {
                document = documentPath;
            }
            String referenceName = stripVariablePrefix(refMember.getMember() != null ? refMember.getMember().getIdentifier() : null);
            if (referenceName == null || referenceName.isBlank()) {
                return null;
            }
            return new UiStyleReference(document, referenceName);
        }

        if (reference instanceof NodeVariable nodeVariable) {
            String referenceName = stripVariablePrefix(nodeVariable.getIdentifier());
            if (referenceName == null || referenceName.isBlank()) {
                return null;
            }
            return new UiStyleReference(documentPath, referenceName);
        }

        return null;
    }

    Object convert(VariableValue value, TypeType type, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        VariableValue resolved = value instanceof VariableReference
                ? ((VariableReference) value).deepResolve()
                : value;
        if (resolved == null) {
            return null;
        }
        try {
            if (targetType == String.class) {
                return toString(resolved, type);
            }
            if (targetType == int.class || targetType == Integer.class) {
                return VariableKt.valueAsInt32(resolved);
            }
            if (targetType == float.class || targetType == Float.class) {
                return VariableKt.valueAsFloat(resolved);
            }
            if (targetType == boolean.class || targetType == Boolean.class) {
                return VariableKt.valueAsBoolean(resolved);
            }
            if (targetType.isEnum()) {
                String enumValue = toString(resolved, type);
                return parseEnum(enumValue, targetType);
            }
            if (targetType == HyUIAnchor.class) {
                return toAnchor(resolved);
            }
            if (targetType == HyUIPadding.class) {
                return toPadding(resolved);
            }
            if (targetType == HyUIPatchStyle.class) {
                return toPatchStyle(resolved, type);
            }
            if (targetType == HyUIStyle.class) {
                return toLabelStyle(resolved);
            }
            if (List.class.isAssignableFrom(targetType)) {
                return toList(resolved, type);
            }
            if (HyUIBsonSerializable.class.isAssignableFrom(targetType)) {
                return toTypedObject(resolved, type, targetType);
            }
            return null;
        } catch (RuntimeException e) {
            if (result != null) {
                result.addConversionWarning("Failed to convert value for type " + type + ": " + e.getMessage());
            }
            return null;
        }
    }

    Object convert(Object value, Class<?> targetType) {
        if (value == null || targetType == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return value;
        }
        if (targetType == String.class) {
            return stringify(value);
        }
        if (targetType == int.class || targetType == Integer.class) {
            return toInt(value);
        }
        if (targetType == float.class || targetType == Float.class) {
            return toFloat(value);
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return toBoolean(value);
        }
        if (targetType.isEnum()) {
            return parseEnumValue(value, targetType);
        }
        if (targetType == HyUIAnchor.class && value instanceof Anchor anchor) {
            return toAnchor(anchor);
        }
        if (targetType == HyUIPadding.class && value instanceof Padding padding) {
            return toPadding(padding);
        }
        if (targetType == HyUIPatchStyle.class && value instanceof PatchStyle patchStyle) {
            return toPatchStyle(patchStyle);
        }
        if (targetType == HyUIStyle.class && value instanceof LabelStyle labelStyle) {
            return toLabelStyle(labelStyle);
        }
        if (targetType == au.ellie.hyui.types.TextTooltipStyle.class
                && value instanceof app.ultradev.hytaleuiparser.generated.types.TextTooltipStyle tooltipStyle) {
            return toTextTooltipStyle(tooltipStyle);
        }
        if (HyUIBsonSerializable.class.isAssignableFrom(targetType)) {
            return toHyuiTypedValue(value, targetType);
        }
        if (targetType == Object.class) {
            return toHyuiTypedValue(value, targetType);
        }
        return null;
    }

    Object convert(VariableValue value, Class<?> targetType) {
        if (value == null || targetType == null) {
            return null;
        }
        VariableValue resolved = value instanceof VariableReference
                ? ((VariableReference) value).deepResolve()
                : value;
        if (resolved == null) {
            return null;
        }
        try {
            if (targetType == String.class) {
                if (resolved instanceof NodeColor) {
                    return colorToHex(VariableKt.valueAsColor(resolved));
                }
                return VariableKt.valueAsString(resolved);
            }
            if (targetType == int.class || targetType == Integer.class) {
                return VariableKt.valueAsInt32(resolved);
            }
            if (targetType == float.class || targetType == Float.class) {
                return VariableKt.valueAsFloat(resolved);
            }
            if (targetType == boolean.class || targetType == Boolean.class) {
                return VariableKt.valueAsBoolean(resolved);
            }
            if (targetType.isEnum()) {
                String enumValue = VariableKt.valueAsEnum(resolved);
                if (enumValue == null) {
                    enumValue = VariableKt.valueAsString(resolved);
                }
                return parseEnum(enumValue, targetType);
            }
            if (targetType == HyUIAnchor.class) {
                return toAnchor(resolved);
            }
            if (targetType == HyUIPadding.class) {
                return toPadding(resolved);
            }
            if (targetType == HyUIPatchStyle.class) {
                return toPatchStyle(resolved, TypeType.PatchStyle);
            }
            if (targetType == HyUIStyle.class) {
                return toLabelStyle(resolved);
            }
            if (HyUIBsonSerializable.class.isAssignableFrom(targetType) && targetType != HyUIBsonSerializable.class) {
                TypeType type = CLASS_TYPE_MAP.get(targetType);
                if (type == null) {
                    return null;
                }
                return toTypedObject(resolved, type, targetType);
            }
            return null;
        } catch (RuntimeException e) {
            if (result != null) {
                result.addConversionWarning("Failed to convert raw value for " + targetType.getSimpleName() + ": " + e.getMessage());
            }
            return null;
        }
    }

    Object convertTyped(VariableValue value, TypeType type) {
        Class<?> targetType = TYPE_CLASS_MAP.get(type);
        if (targetType == null) {
            return null;
        }
        return convert(value, type, targetType);
    }

    Object convert(VariableValue value, TypeType type) {
        if (value == null) {
            return null;
        }
        VariableValue resolved = value instanceof VariableReference
                ? ((VariableReference) value).deepResolve()
                : value;
        if (resolved == null) {
            return null;
        }

        if (type == TypeType.Anchor) {
            return toAnchor(resolved);
        }
        if (type == TypeType.Padding) {
            return toPadding(resolved);
        }
        if (type == TypeType.PatchStyle) {
            return toPatchStyle(resolved, type);
        }
        if (type == TypeType.LabelStyle) {
            return toLabelStyle(resolved);
        }

        return null;
    }

    private Object toTypedObject(VariableValue value, TypeType type, Class<?> targetType) {
        if (!(value instanceof NodeType nodeType)) {
            return null;
        }
        try {
            Object instance = targetType.getDeclaredConstructor().newInstance();
            applyTypeProperties(instance, type, nodeType);
            return instance;
        } catch (ReflectiveOperationException e) {
            if (result != null) {
                result.addConversionWarning("Failed to instantiate type " + targetType.getSimpleName() + ": " + e.getMessage());
            }
            return null;
        }
    }

    private void applyTypeProperties(Object instance, TypeType type, NodeType nodeType) {
        Map<String, VariableValue> values = nodeType.resolveValue();
        for (Map.Entry<String, VariableValue> entry : values.entrySet()) {
            String property = entry.getKey();
            VariableValue value = entry.getValue();
            TypeType fieldType = type.getAllowedFields().get(property);
            if (fieldType == null) {
                continue;
            }
            Method method = findWithMethod(instance.getClass(), property);
            if (method == null) {
                continue;
            }
            Object converted = convert(value, fieldType, method.getParameterTypes()[0]);
            if (converted == null) {
                continue;
            }
            try {
                method.invoke(instance, converted);
            } catch (ReflectiveOperationException ignored) {
                // Skip invalid assignments
            }
        }
    }

    private Method findWithMethod(Class<?> type, String property) {
        String methodName = "with" + property;
        for (Method method : type.getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                continue;
            }
            return method;
        }
        return null;
    }

    private Object toList(VariableValue value, TypeType type) {
        if (value instanceof NodeArray array) {
            List<Object> list = new ArrayList<>();
            for (Object entryObj : array.getEntries()) {
                if (!(entryObj instanceof VariableValue entry)) {
                    continue;
                }
                String converted = toString(entry instanceof VariableReference ? ((VariableReference) entry).deepResolve() : entry, type);
                if (converted != null) {
                    list.add(converted);
                }
            }
            return list;
        }
        List<Object> list = new ArrayList<>();
        String converted = toString(value, type);
        if (converted != null) {
            list.add(converted);
        }
        return list;
    }

    private HyUIAnchor toAnchor(VariableValue value) {
        if (!(value instanceof NodeType nodeType)) {
            return null;
        }
        Map<String, VariableValue> values = nodeType.resolveValue();
        HyUIAnchor anchor = new HyUIAnchor();
        applyAnchorPaddingValues(values, anchor, null);
        return anchor;
    }

    private HyUIPadding toPadding(VariableValue value) {
        if (!(value instanceof NodeType nodeType)) {
            if (value instanceof NodeConstant) {
                int full = VariableKt.valueAsInt32(value);
                return new HyUIPadding().setFull(full);
            }
            return null;
        }
        Map<String, VariableValue> values = nodeType.resolveValue();
        HyUIPadding padding = new HyUIPadding();
        applyAnchorPaddingValues(values, null, padding);
        return padding;
    }

    private void applyAnchorPaddingValues(Map<String, VariableValue> values, HyUIAnchor anchor, HyUIPadding padding) {
        for (Map.Entry<String, VariableValue> entry : values.entrySet()) {
            String key = entry.getKey();
            VariableValue value = entry.getValue();
            Integer intValue = value != null ? VariableKt.valueAsInt32(value) : null;
            if (intValue == null) {
                continue;
            }
            switch (key) {
                case "Left" -> {
                    if (anchor != null) anchor.setLeft(intValue);
                    if (padding != null) padding.setLeft(intValue);
                }
                case "Right" -> {
                    if (anchor != null) anchor.setRight(intValue);
                    if (padding != null) padding.setRight(intValue);
                }
                case "Top" -> {
                    if (anchor != null) anchor.setTop(intValue);
                    if (padding != null) padding.setTop(intValue);
                }
                case "Bottom" -> {
                    if (anchor != null) anchor.setBottom(intValue);
                    if (padding != null) padding.setBottom(intValue);
                }
                case "Width" -> {
                    if (anchor != null) anchor.setWidth(intValue);
                }
                case "Height" -> {
                    if (anchor != null) anchor.setHeight(intValue);
                }
                case "MinWidth" -> {
                    if (anchor != null) anchor.setMinWidth(intValue);
                }
                case "MaxWidth" -> {
                    if (anchor != null) anchor.setMaxWidth(intValue);
                }
                case "Full" -> {
                    if (anchor != null) anchor.setFull(intValue);
                    if (padding != null) padding.setFull(intValue);
                }
                case "Horizontal" -> {
                    if (anchor != null) anchor.setHorizontal(intValue);
                    if (padding != null) padding.setHorizontal(intValue);
                }
                case "Vertical" -> {
                    if (anchor != null) anchor.setVertical(intValue);
                    if (padding != null) padding.setVertical(intValue);
                }
                default -> {
                    // Ignore
                }
            }
        }
    }

    private HyUIPatchStyle toPatchStyle(VariableValue value, TypeType type) {
        if (value instanceof NodeColor) {
            return new HyUIPatchStyle().setColor(colorToHex(VariableKt.valueAsColor(value)));
        }
        if (value instanceof NodeConstant) {
            String constant = VariableKt.valueAsString(value);
            if (constant != null && constant.startsWith("#")) {
                return new HyUIPatchStyle().setColor(constant);
            }
            return new HyUIPatchStyle().setTexturePath(constant);
        }
        if (!(value instanceof NodeType nodeType)) {
            return null;
        }
        Map<String, VariableValue> values = nodeType.resolveValue();
        HyUIPatchStyle style = new HyUIPatchStyle();
        for (Map.Entry<String, VariableValue> entry : values.entrySet()) {
            String key = entry.getKey();
            VariableValue fieldValue = entry.getValue();
            if (fieldValue == null) {
                continue;
            }
            switch (key) {
                case "TexturePath" -> style.setTexturePath(stripCustomUiPrefix(VariableKt.valueAsString(fieldValue)));
                case "Color" -> style.setColor(colorToHex(VariableKt.valueAsColor(fieldValue)));
                case "Border" -> style.setBorder(VariableKt.valueAsInt32(fieldValue));
                case "HorizontalBorder" -> style.setHorizontalBorder(VariableKt.valueAsInt32(fieldValue));
                case "VerticalBorder" -> style.setVerticalBorder(VariableKt.valueAsInt32(fieldValue));
                case "Area" -> applyArea(style, fieldValue);
                default -> {
                    // Ignore
                }
            }
        }
        return style;
    }

    private void applyArea(HyUIPatchStyle style, VariableValue value) {
        if (!(value instanceof NodeType nodeType)) {
            return;
        }
        Map<String, VariableValue> values = nodeType.resolveValue();
        for (Map.Entry<String, VariableValue> entry : values.entrySet()) {
            String key = entry.getKey();
            VariableValue fieldValue = entry.getValue();
            if (fieldValue == null) {
                continue;
            }
            Integer intValue = VariableKt.valueAsInt32(fieldValue);
            switch (key) {
                case "Left" -> style.setAreaX(intValue);
                case "Top" -> style.setAreaY(intValue);
                case "Right" -> style.setAreaWidth(intValue);
                case "Bottom" -> style.setAreaHeight(intValue);
                default -> {
                    // Ignore
                }
            }
        }
    }

    private HyUIStyle toLabelStyle(VariableValue value) {
        if (!(value instanceof NodeType nodeType)) {
            return null;
        }
        HyUIStyle style = new HyUIStyle();
        Map<String, VariableValue> values = nodeType.resolveValue();
        for (Map.Entry<String, VariableValue> entry : values.entrySet()) {
            String key = entry.getKey();
            VariableValue fieldValue = entry.getValue();
            if (fieldValue == null) {
                continue;
            }
            switch (key) {
                case "FontSize" -> style.setFontSize(VariableKt.valueAsFloat(fieldValue));
                case "FontName" -> style.setFontName(VariableKt.valueAsString(fieldValue));
                case "LetterSpacing" -> style.setLetterSpacing(VariableKt.valueAsInt32(fieldValue));
                case "TextColor" -> style.setTextColor(colorToHex(VariableKt.valueAsColor(fieldValue)));
                case "RenderBold" -> style.setRenderBold(VariableKt.valueAsBoolean(fieldValue));
                case "RenderUppercase" -> style.setRenderUppercase(VariableKt.valueAsBoolean(fieldValue));
                case "RenderItalics" -> style.setRenderItalics(VariableKt.valueAsBoolean(fieldValue));
                case "Alignment" -> style.setAlignment(VariableKt.valueAsString(fieldValue));
                case "HorizontalAlignment" -> style.setHorizontalAlignment(VariableKt.valueAsString(fieldValue));
                case "VerticalAlignment" -> style.setVerticalAlignment(VariableKt.valueAsString(fieldValue));
                case "OutlineColor" -> style.setOutlineColor(colorToHex(VariableKt.valueAsColor(fieldValue)));
                case "Wrap" -> style.setWrap(VariableKt.valueAsBoolean(fieldValue));
                case "ShrinkTextToFit" -> style.setShrinkTextToFit(VariableKt.valueAsBoolean(fieldValue));
                case "MinShrinkTextToFitFontSize" -> style.setMinShrinkTextToFitFontSize(VariableKt.valueAsFloat(fieldValue));
                default -> {
                    // Ignore unsupported properties
                }
            }
        }
        return style;
    }

    private HyUIAnchor toAnchor(Anchor anchor) {
        if (anchor == null) {
            return null;
        }
        HyUIAnchor converted = new HyUIAnchor();
        if (anchor.getLeft() != null) converted.setLeft(anchor.getLeft());
        if (anchor.getRight() != null) converted.setRight(anchor.getRight());
        if (anchor.getTop() != null) converted.setTop(anchor.getTop());
        if (anchor.getBottom() != null) converted.setBottom(anchor.getBottom());
        if (anchor.getWidth() != null) converted.setWidth(anchor.getWidth());
        if (anchor.getHeight() != null) converted.setHeight(anchor.getHeight());
        if (anchor.getMinWidth() != null) converted.setMinWidth(anchor.getMinWidth());
        if (anchor.getMaxWidth() != null) converted.setMaxWidth(anchor.getMaxWidth());
        if (anchor.getFull() != null) converted.setFull(anchor.getFull());
        if (anchor.getHorizontal() != null) converted.setHorizontal(anchor.getHorizontal());
        if (anchor.getVertical() != null) converted.setVertical(anchor.getVertical());
        return converted;
    }

    private HyUIPadding toPadding(Padding padding) {
        if (padding == null) {
            return null;
        }
        HyUIPadding converted = new HyUIPadding();
        if (padding.getFull() != null) {
            converted.setFull(padding.getFull());
            return converted;
        }
        if (padding.getHorizontal() != null || padding.getVertical() != null) {
            int vertical = padding.getVertical() != null ? padding.getVertical() : 0;
            int horizontal = padding.getHorizontal() != null ? padding.getHorizontal() : 0;
            converted.setSymmetric(vertical, horizontal);
        }
        if (padding.getLeft() != null) converted.setLeft(padding.getLeft());
        if (padding.getRight() != null) converted.setRight(padding.getRight());
        if (padding.getTop() != null) converted.setTop(padding.getTop());
        if (padding.getBottom() != null) converted.setBottom(padding.getBottom());
        return converted;
    }

    private HyUIPatchStyle toPatchStyle(PatchStyle patchStyle) {
        if (patchStyle == null) {
            return null;
        }
        HyUIPatchStyle style = new HyUIPatchStyle();
        if (patchStyle.getTexturePath() != null) {
            style.setTexturePath(stripCustomUiPrefix(patchStyle.getTexturePath()));
        }
        if (patchStyle.getBorder() != null) {
            style.setBorder(patchStyle.getBorder());
        }
        if (patchStyle.getHorizontalBorder() != null) {
            style.setHorizontalBorder(patchStyle.getHorizontalBorder());
        }
        if (patchStyle.getVerticalBorder() != null) {
            style.setVerticalBorder(patchStyle.getVerticalBorder());
        }
        if (patchStyle.getColor() != null) {
            style.setColor(colorToHex(patchStyle.getColor()));
        }
        if (patchStyle.getArea() != null) {
            Padding area = patchStyle.getArea();
            if (area.getLeft() != null) style.setAreaX(area.getLeft());
            if (area.getTop() != null) style.setAreaY(area.getTop());
            if (area.getRight() != null) style.setAreaWidth(area.getRight());
            if (area.getBottom() != null) style.setAreaHeight(area.getBottom());
        }
        return style;
    }

    private HyUIStyle toLabelStyle(LabelStyle labelStyle) {
        if (labelStyle == null) {
            return null;
        }
        HyUIStyle style = new HyUIStyle();
        if (labelStyle.getFontSize() != null) {
            style.setFontSize(labelStyle.getFontSize());
        }
        if (labelStyle.getFontName() != null) {
            // TODO: Investigate why fontname isn't happy with being bson for ellie :(
            //style.setFontName(labelStyle.getFontName());
        }
        if (labelStyle.getLetterSpacing() != null) {
            style.setLetterSpacing(labelStyle.getLetterSpacing().intValue());
        }
        if (labelStyle.getTextColor() != null) {
            style.setTextColor(colorToHex(labelStyle.getTextColor()));
        }
        if (labelStyle.getRenderBold() != null) {
            style.setRenderBold(labelStyle.getRenderBold());
        }
        if (labelStyle.getRenderUppercase() != null) {
            style.setRenderUppercase(labelStyle.getRenderUppercase());
        }
        if (labelStyle.getRenderItalics() != null) {
            style.setRenderItalics(labelStyle.getRenderItalics());
        }
        if (labelStyle.getAlignment() != null) {
            style.setAlignment((Alignment) parseEnumValue(labelStyle.getAlignment(), Alignment.class));
        }
        if (labelStyle.getHorizontalAlignment() != null) {
            style.setHorizontalAlignment((Alignment) parseEnumValue(labelStyle.getHorizontalAlignment(), Alignment.class));
        }
        if (labelStyle.getVerticalAlignment() != null) {
            style.setVerticalAlignment((Alignment) parseEnumValue(labelStyle.getVerticalAlignment(), Alignment.class));
        }
        if (labelStyle.getOutlineColor() != null) {
            style.setOutlineColor(colorToHex(labelStyle.getOutlineColor()));
        }
        if (labelStyle.getWrap() != null) {
            style.setWrap(labelStyle.getWrap());
        }
        return style;
    }

    private au.ellie.hyui.types.TextTooltipStyle toTextTooltipStyle(app.ultradev.hytaleuiparser.generated.types.TextTooltipStyle tooltipStyle) {
        if (tooltipStyle == null) {
            return null;
        }
        au.ellie.hyui.types.TextTooltipStyle style = new au.ellie.hyui.types.TextTooltipStyle();
        if (tooltipStyle.getBackground() != null) {
            style.withBackground(toPatchStyle(tooltipStyle.getBackground()));
        }
        if (tooltipStyle.getMaxWidth() != null) {
            style.withMaxWidth(tooltipStyle.getMaxWidth());
        }
        if (tooltipStyle.getLabelStyle() != null) {
            style.withLabelStyle(toLabelStyle(tooltipStyle.getLabelStyle()));
        }
        if (tooltipStyle.getPadding() != null) {
            style.withPadding(tooltipStyle.getPadding());
        }
        if (tooltipStyle.getAlignment() != null) {
            style.withAlignment(tooltipStyle.getAlignment().toString());
        }
        return style;
    }

    private Object toHyuiTypedValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        Class<?> desiredType = targetType;
        if (desiredType == HyUIBsonSerializable.class || desiredType == Object.class) {
            desiredType = findHyuiTypeByName(value.getClass().getSimpleName());
        }
        if (desiredType == null) {
            return null;
        }
        if (desiredType.isInstance(value)) {
            return value;
        }
        return createAndApply(value, desiredType);
    }

    private Class<?> findHyuiTypeByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String className = "au.ellie.hyui.types." + name;
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private Object createAndApply(Object source, Class<?> targetType) {
        try {
            Object target = targetType.getDeclaredConstructor().newInstance();
            for (Method getter : source.getClass().getMethods()) {
                if (getter.getParameterCount() != 0) {
                    continue;
                }
                String getterName = getter.getName();
                if (getterName.equals("getClass") || (!getterName.startsWith("get") && !getterName.startsWith("is"))) {
                    continue;
                }
                String propertyName = propertyNameFromGetter(getterName);
                if (propertyName == null) {
                    continue;
                }
                Object value = getter.invoke(source);
                if (value == null) {
                    continue;
                }
                String propertyKey = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
                Method setter = findSetterMethod(targetType, propertyKey);
                if (setter == null) {
                    continue;
                }
                Object converted = convert(value, setter.getParameterTypes()[0]);
                if (converted == null) {
                    continue;
                }
                setter.invoke(target, converted);
            }
            return target;
        } catch (ReflectiveOperationException e) {
            if (result != null) {
                result.addConversionWarning("Failed to convert " + source.getClass().getSimpleName() + " to " + targetType.getSimpleName() + ": " + e.getMessage());
            }
            return null;
        }
    }

    private Method findSetterMethod(Class<?> targetType, String propertyKey) {
        String withName = "with" + propertyKey;
        String setName = "set" + propertyKey;
        for (Method method : targetType.getMethods()) {
            if (method.getParameterCount() != 1) {
                continue;
            }
            if (method.getName().equals(withName) || method.getName().equals(setName)) {
                return method;
            }
        }
        return null;
    }

    private String propertyNameFromGetter(String getterName) {
        if (getterName.startsWith("get")) {
            String base = getterName.substring(3);
            if (base.isEmpty()) {
                return null;
            }
            return Character.toLowerCase(base.charAt(0)) + base.substring(1);
        }
        if (getterName.startsWith("is")) {
            String base = getterName.substring(2);
            if (base.isEmpty()) {
                return null;
            }
            return Character.toLowerCase(base.charAt(0)) + base.substring(1);
        }
        return null;
    }

    private String stringify(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Color color) {
            return colorToHex(color);
        }
        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        return value.toString();
    }

    private Integer toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Float toFloat(Object value) {
        if (value instanceof Number number) {
            return number.floatValue();
        }
        if (value instanceof String text) {
            try {
                return Float.parseFloat(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            return Boolean.parseBoolean(text);
        }
        return null;
    }

    private Object parseEnumValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        String name = value instanceof Enum<?> enumValue ? enumValue.name() : value.toString();
        return parseEnum(name, targetType);
    }

    private String toString(VariableValue value, TypeType type) {
        if (type == TypeType.Color) {
            return colorToHex(VariableKt.valueAsColor(value));
        }
        if (type.isEnum()) {
            return VariableKt.valueAsEnum(value);
        }
        return VariableKt.valueAsString(value);
    }

    private Object parseEnum(String value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        String normalized = normalizeEnumValue(value);
        for (Object constant : targetType.getEnumConstants()) {
            String enumName = normalizeEnumValue(((Enum<?>) constant).name());
            if (enumName.equals(normalized)) {
                return constant;
            }
        }
        return null;
    }

    private String normalizeEnumValue(String value) {
        return value.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
    }

    private String colorToHex(Color color) {
        if (color == null) {
            return null;
        }
        return String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    private String stripVariablePrefix(String identifier) {
        if (identifier == null) {
            return null;
        }
        if (identifier.startsWith("@") || identifier.startsWith("#")) {
            return identifier.substring(1);
        }
        return identifier;
    }

    private String stripCustomUiPrefix(String path) {
        if (path == null) {
            return null;
        }
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        String prefix = "Common/UI/Custom/";
        if (normalized.startsWith(prefix)) {
            return normalized.substring(prefix.length());
        }
        return path;
    }

    record UiStyleReference(String document, String reference) {}
}
