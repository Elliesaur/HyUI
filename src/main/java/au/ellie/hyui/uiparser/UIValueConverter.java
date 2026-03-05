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
import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

final class UIValueConverter {
    private static final Map<TypeType, Class<?>> TYPE_CLASS_MAP = new EnumMap<>(TypeType.class);
    private static final Map<Class<?>, TypeType> CLASS_TYPE_MAP = new HashMap<>();
    private static final Map<GetterKey, Method> GETTER_CACHE = new ConcurrentHashMap<>();
    private static final Map<GetterKey, Boolean> GETTER_MISS_CACHE = new ConcurrentHashMap<>();
    private static final Map<MethodKey, Method> SETTER_CACHE = new ConcurrentHashMap<>();
    private static final Map<MethodKey, Boolean> SETTER_MISS_CACHE = new ConcurrentHashMap<>();
    private static final Map<MethodKey, Method> WITH_METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<MethodKey, Boolean> WITH_METHOD_MISS_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Method>> GETTER_LIST_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Object>> ENUM_LOOKUP_CACHE = new ConcurrentHashMap<>();
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

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
        if (targetType == HyUIAnchor.class && isAnchorLike(value)) {
            return toAnchor(value);
        }
        if (targetType == HyUIPadding.class && isPaddingLike(value)) {
            return toPadding(value);
        }
        if (targetType == HyUIPatchStyle.class && isPatchStyleLike(value)) {
            return toPatchStyle(value);
        }
        if (targetType == HyUIStyle.class && isLabelStyleLike(value)) {
            return toLabelStyle(value);
        }
        if (targetType == au.ellie.hyui.types.TextTooltipStyle.class && isTextTooltipStyleLike(value)) {
            return toTextTooltipStyle(value);
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
        MethodKey key = new MethodKey(type, methodName, 1);
        Method cached = WITH_METHOD_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        if (WITH_METHOD_MISS_CACHE.containsKey(key)) {
            return null;
        }
        for (Method method : type.getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                continue;
            }
            WITH_METHOD_CACHE.put(key, method);
            return method;
        }
        WITH_METHOD_MISS_CACHE.put(key, Boolean.TRUE);
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

    private HyUIAnchor toAnchor(Object anchor) {
        if (anchor == null) {
            return null;
        }
        HyUIAnchor converted = new HyUIAnchor();
        Integer left = readInt(anchor, "getLeft");
        Integer right = readInt(anchor, "getRight");
        Integer top = readInt(anchor, "getTop");
        Integer bottom = readInt(anchor, "getBottom");
        Integer width = readInt(anchor, "getWidth");
        Integer height = readInt(anchor, "getHeight");
        Integer minWidth = readInt(anchor, "getMinWidth");
        Integer maxWidth = readInt(anchor, "getMaxWidth");
        Integer full = readInt(anchor, "getFull");
        Integer horizontal = readInt(anchor, "getHorizontal");
        Integer vertical = readInt(anchor, "getVertical");
        if (left != null) converted.setLeft(left);
        if (right != null) converted.setRight(right);
        if (top != null) converted.setTop(top);
        if (bottom != null) converted.setBottom(bottom);
        if (width != null) converted.setWidth(width);
        if (height != null) converted.setHeight(height);
        if (minWidth != null) converted.setMinWidth(minWidth);
        if (maxWidth != null) converted.setMaxWidth(maxWidth);
        if (full != null) converted.setFull(full);
        if (horizontal != null) converted.setHorizontal(horizontal);
        if (vertical != null) converted.setVertical(vertical);
        return converted;
    }

    private HyUIPadding toPadding(Object padding) {
        if (padding == null) {
            return null;
        }
        HyUIPadding converted = new HyUIPadding();
        Integer full = readInt(padding, "getFull");
        Integer horizontal = readInt(padding, "getHorizontal");
        Integer vertical = readInt(padding, "getVertical");
        Integer left = readInt(padding, "getLeft");
        Integer right = readInt(padding, "getRight");
        Integer top = readInt(padding, "getTop");
        Integer bottom = readInt(padding, "getBottom");
        if (full != null) {
            converted.setFull(full);
            return converted;
        }
        if (horizontal != null || vertical != null) {
            int verticalValue = vertical != null ? vertical : 0;
            int horizontalValue = horizontal != null ? horizontal : 0;
            converted.setSymmetric(verticalValue, horizontalValue);
        }
        if (left != null) converted.setLeft(left);
        if (right != null) converted.setRight(right);
        if (top != null) converted.setTop(top);
        if (bottom != null) converted.setBottom(bottom);
        return converted;
    }

    private HyUIPatchStyle toPatchStyle(Object patchStyle) {
        if (patchStyle == null) {
            return null;
        }
        HyUIPatchStyle style = new HyUIPatchStyle();
        String texturePath = readString(patchStyle, "getTexturePath");
        Integer border = readInt(patchStyle, "getBorder");
        Integer horizontalBorder = readInt(patchStyle, "getHorizontalBorder");
        Integer verticalBorder = readInt(patchStyle, "getVerticalBorder");
        Color color = readColor(patchStyle, "getColor");
        if (texturePath != null) {
            style.setTexturePath(stripCustomUiPrefix(texturePath));
        }
        if (border != null) {
            style.setBorder(border);
        }
        if (horizontalBorder != null) {
            style.setHorizontalBorder(horizontalBorder);
        }
        if (verticalBorder != null) {
            style.setVerticalBorder(verticalBorder);
        }
        if (color != null) {
            style.setColor(colorToHex(color));
        }
        applyAreaFromObject(style, invokeGetter(patchStyle, "getArea"));
        return style;
    }

    private HyUIStyle toLabelStyle(Object labelStyle) {
        if (labelStyle == null) {
            return null;
        }
        HyUIStyle style = new HyUIStyle();
        Float fontSize = readFloat(labelStyle, "getFontSize");
        String fontName = readString(labelStyle, "getFontName");
        Float letterSpacing = readFloat(labelStyle, "getLetterSpacing");
        Color textColor = readColor(labelStyle, "getTextColor");
        Boolean renderBold = readBoolean(labelStyle, "getRenderBold");
        Boolean renderUppercase = readBoolean(labelStyle, "getRenderUppercase");
        Boolean renderItalics = readBoolean(labelStyle, "getRenderItalics");
        Object alignment = invokeGetter(labelStyle, "getAlignment");
        Object horizontalAlignment = invokeGetter(labelStyle, "getHorizontalAlignment");
        Object verticalAlignment = invokeGetter(labelStyle, "getVerticalAlignment");
        Color outlineColor = readColor(labelStyle, "getOutlineColor");
        Boolean wrap = readBoolean(labelStyle, "getWrap");
        if (fontSize != null) {
            style.setFontSize(fontSize);
        }
        if (fontName != null) {
            // TODO: Investigate why fontname isn't happy with being bson for ellie :(
            //style.setFontName(fontName);
        }
        if (letterSpacing != null) {
            style.setLetterSpacing(letterSpacing.intValue());
        }
        if (textColor != null) {
            style.setTextColor(colorToHex(textColor));
        }
        if (renderBold != null) {
            style.setRenderBold(renderBold);
        }
        if (renderUppercase != null) {
            style.setRenderUppercase(renderUppercase);
        }
        if (renderItalics != null) {
            style.setRenderItalics(renderItalics);
        }
        if (alignment != null) {
            style.setAlignment((Alignment) parseEnumValue(alignment, Alignment.class));
        }
        if (horizontalAlignment != null) {
            style.setHorizontalAlignment((Alignment) parseEnumValue(horizontalAlignment, Alignment.class));
        }
        if (verticalAlignment != null) {
            style.setVerticalAlignment((Alignment) parseEnumValue(verticalAlignment, Alignment.class));
        }
        if (outlineColor != null) {
            style.setOutlineColor(colorToHex(outlineColor));
        }
        if (wrap != null) {
            style.setWrap(wrap);
        }
        return style;
    }

    private au.ellie.hyui.types.TextTooltipStyle toTextTooltipStyle(Object tooltipStyle) {
        if (tooltipStyle == null) {
            return null;
        }
        au.ellie.hyui.types.TextTooltipStyle style = new au.ellie.hyui.types.TextTooltipStyle();
        Object background = invokeGetter(tooltipStyle, "getBackground");
        Integer maxWidth = readInt(tooltipStyle, "getMaxWidth");
        Object labelStyle = invokeGetter(tooltipStyle, "getLabelStyle");
        Integer padding = readInt(tooltipStyle, "getPadding");
        Object alignment = invokeGetter(tooltipStyle, "getAlignment");
        if (background != null) {
            style.withBackground(toPatchStyle(background));
        }
        if (maxWidth != null) {
            style.withMaxWidth(maxWidth);
        }
        if (labelStyle != null) {
            style.withLabelStyle(toLabelStyle(labelStyle));
        }
        if (padding != null) {
            // TODO: Fix me to be proper hyuipadding.
            style.withPadding(HyUIPadding.all(padding));
        }
        if (alignment != null) {
            style.withAlignment(alignment.toString());
        }
        return style;
    }

    private boolean isAnchorLike(Object value) {
        return hasGetter(value, "getLeft")
                || hasGetter(value, "getRight")
                || hasGetter(value, "getTop")
                || hasGetter(value, "getBottom");
    }

    private boolean isPaddingLike(Object value) {
        return hasGetter(value, "getFull")
                || hasGetter(value, "getHorizontal")
                || hasGetter(value, "getVertical");
    }

    private boolean isPatchStyleLike(Object value) {
        return hasGetter(value, "getTexturePath")
                || hasGetter(value, "getColor")
                || hasGetter(value, "getBorder");
    }

    private boolean isLabelStyleLike(Object value) {
        return hasGetter(value, "getFontSize")
                || hasGetter(value, "getTextColor")
                || hasGetter(value, "getAlignment");
    }

    private boolean isTextTooltipStyleLike(Object value) {
        return hasGetter(value, "getBackground")
                || hasGetter(value, "getLabelStyle")
                || hasGetter(value, "getMaxWidth");
    }

    private boolean hasGetter(Object value, String methodName) {
        if (value == null) {
            return false;
        }
        return getGetter(value.getClass(), methodName) != null;
    }

    private Object invokeGetter(Object value, String methodName) {
        if (value == null) {
            return null;
        }
        Method getter = getGetter(value.getClass(), methodName);
        if (getter == null) {
            return null;
        }
        try {
            return getter.invoke(value);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private String readString(Object value, String methodName) {
        Object result = invokeGetter(value, methodName);
        if (result == null) {
            return null;
        }
        return result.toString();
    }

    private Integer readInt(Object value, String methodName) {
        Object result = invokeGetter(value, methodName);
        if (result instanceof Number number) {
            return number.intValue();
        }
        if (result instanceof String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Float readFloat(Object value, String methodName) {
        Object result = invokeGetter(value, methodName);
        if (result instanceof Number number) {
            return number.floatValue();
        }
        if (result instanceof String text) {
            try {
                return Float.parseFloat(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Boolean readBoolean(Object value, String methodName) {
        Object result = invokeGetter(value, methodName);
        if (result instanceof Boolean bool) {
            return bool;
        }
        if (result instanceof String text) {
            return Boolean.parseBoolean(text);
        }
        return null;
    }

    private Color readColor(Object value, String methodName) {
        Object result = invokeGetter(value, methodName);
        if (result instanceof Color color) {
            return color;
        }
        return null;
    }

    private void applyAreaFromObject(HyUIPatchStyle style, Object areaValue) {
        if (areaValue == null) {
            return;
        }
        Integer left = readInt(areaValue, "getLeft");
        Integer top = readInt(areaValue, "getTop");
        Integer right = readInt(areaValue, "getRight");
        Integer bottom = readInt(areaValue, "getBottom");
        if (left != null) style.setAreaX(left);
        if (top != null) style.setAreaY(top);
        if (right != null) style.setAreaWidth(right);
        if (bottom != null) style.setAreaHeight(bottom);
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
            for (Method getter : getReadableGetters(source.getClass())) {
                String getterName = getter.getName();
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
        Method withMethod = findCachedSetter(targetType, withName);
        if (withMethod != null) {
            return withMethod;
        }
        return findCachedSetter(targetType, setName);
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
        Map<String, Object> lookup = ENUM_LOOKUP_CACHE.computeIfAbsent(targetType, UIValueConverter::buildEnumLookup);
        return lookup.get(normalized);
    }

    private static String normalizeEnumValue(String value) {
        int length = value.length();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char ch = value.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                builder.append((char) (ch + 32));
            } else if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    private String colorToHex(Color color) {
        if (color == null) {
            return null;
        }
        char[] out = new char[9];
        out[0] = '#';
        writeHexByte(out, 1, color.getRed());
        writeHexByte(out, 3, color.getGreen());
        writeHexByte(out, 5, color.getBlue());
        writeHexByte(out, 7, color.getAlpha());
        return new String(out);
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

    private static Map<String, Object> buildEnumLookup(Class<?> enumType) {
        Map<String, Object> lookup = new HashMap<>();
        Object[] constants = enumType.getEnumConstants();
        if (constants == null) {
            return lookup;
        }
        for (Object constant : constants) {
            String name = ((Enum<?>) constant).name();
            lookup.putIfAbsent(normalizeEnumValue(name), constant);
        }
        return lookup;
    }

    private Method getGetter(Class<?> type, String methodName) {
        GetterKey key = new GetterKey(type, methodName);
        Method cached = GETTER_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        if (GETTER_MISS_CACHE.containsKey(key)) {
            return null;
        }
        try {
            Method method = type.getMethod(methodName);
            GETTER_CACHE.put(key, method);
            return method;
        } catch (NoSuchMethodException ignored) {
            GETTER_MISS_CACHE.put(key, Boolean.TRUE);
            return null;
        }
    }

    private Method findCachedSetter(Class<?> targetType, String name) {
        MethodKey key = new MethodKey(targetType, name, 1);
        Method cached = SETTER_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        if (SETTER_MISS_CACHE.containsKey(key)) {
            return null;
        }
        for (Method method : targetType.getMethods()) {
            if (!method.getName().equals(name)) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                continue;
            }
            SETTER_CACHE.put(key, method);
            return method;
        }
        SETTER_MISS_CACHE.put(key, Boolean.TRUE);
        return null;
    }

    private List<Method> getReadableGetters(Class<?> type) {
        List<Method> cached = GETTER_LIST_CACHE.get(type);
        if (cached != null) {
            return cached;
        }
        List<Method> methods = new ArrayList<>();
        for (Method method : type.getMethods()) {
            if (method.getParameterCount() != 0) {
                continue;
            }
            String name = method.getName();
            if ("getClass".equals(name) || (!name.startsWith("get") && !name.startsWith("is"))) {
                continue;
            }
            methods.add(method);
        }
        GETTER_LIST_CACHE.put(type, methods);
        return methods;
    }

    private void writeHexByte(char[] out, int index, int value) {
        out[index] = HEX_CHARS[(value >> 4) & 0xF];
        out[index + 1] = HEX_CHARS[value & 0xF];
    }

    record UiStyleReference(String document, String reference) {}

    private record GetterKey(Class<?> type, String name) {}

    private record MethodKey(Class<?> type, String name, int paramCount) {}
}
