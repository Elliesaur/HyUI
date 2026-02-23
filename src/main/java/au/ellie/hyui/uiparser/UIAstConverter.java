package au.ellie.hyui.uiparser;

import app.ultradev.hytaleuiparser.ast.*;
import app.ultradev.hytaleuiparser.asttools.VariableKt;
import app.ultradev.hytaleuiparser.validation.ElementType;
import app.ultradev.hytaleuiparser.validation.types.TypeType;
import au.ellie.hyui.builders.*;
import au.ellie.hyui.types.NativeTab;
import au.ellie.hyui.types.HyUIBsonSerializable;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class UIAstConverter {
    private static final Map<MethodKey, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<MethodKey, Boolean> METHOD_MISS_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, List<Method>>> ONE_ARG_METHODS_CACHE = new ConcurrentHashMap<>();
    private final UIParseResult result;
    private final String documentPath;
    private final UIValueConverter valueConverter;

    UIAstConverter(UIParseResult result, String documentPath) {
        this.result = result;
        this.documentPath = documentPath;
        this.valueConverter = new UIValueConverter(result, documentPath);
    }

    List<UIElementBuilder<?>> convert(RootNode root) {
        List<UIElementBuilder<?>> builders = new ArrayList<>();
        for (NodeElement element : root.getElements()) {
            UIElementBuilder<?> builder = convertElement(element);
            if (builder != null) {
                builders.add(builder);
            }
        }
        return builders;
    }

    private UIElementBuilder<?> convertElement(NodeElement element) {
        String rawTypeName = extractTypeName(element);
        UIElementBuilder<?> builder = createBuilder(rawTypeName, element.getResolvedType());
        if (builder == null) {
            if (result != null) {
                result.addConversionWarning("Unsupported element type: " + rawTypeName);
            }
            return null;
        }

        if (element instanceof NodeElementWithSelector withSelector) {
            String selectorId = cleanSelector(withSelector.getSelector() != null ? withSelector.getSelector().getIdentifier() : null);
            if (selectorId != null && !selectorId.isBlank()) {
                builder.withId(selectorId);
            }
        }

        applyProperties(builder, element);
        applyChildren(builder, element);

        return builder;
    }

    private void applyChildren(UIElementBuilder<?> builder, NodeElement element) {
        if (builder instanceof NativeTabNavigationBuilder nativeTabs) {
            applyNativeTabChildren(nativeTabs, element);
            return;
        }
        if (builder instanceof DropdownBoxBuilder dropdownBox) {
            applyDropdownEntries(dropdownBox, element);
        }

        for (NodeElement child : element.getChildElements()) {
            if (builder instanceof DropdownBoxBuilder) {
                String rawType = extractTypeName(child);
                if ("DropdownEntry".equalsIgnoreCase(rawType)) {
                    continue;
                }
            }
            UIElementBuilder<?> childBuilder = convertElement(child);
            if (childBuilder != null) {
                builder.addChild(childBuilder);
            }
        }

        for (NodeSelectorElement selectorElement : element.getSelectorElements()) {
            applySelectorChildren(builder, selectorElement);
        }
    }

    private void applyNativeTabChildren(NativeTabNavigationBuilder builder, NodeElement element) {
        for (NodeElement child : element.getChildElements()) {
            String rawType = extractTypeName(child);
            if ("TabButton".equalsIgnoreCase(rawType)) {
                NativeTab tab = new NativeTab();
                Map<String, VariableValue> properties;
                try {
                    properties = child.resolveProperties();
                } catch (RuntimeException e) {
                    if (result != null) {
                        result.addConversionWarning("Failed to resolve TabButton properties: " + e.getMessage());
                    }
                    continue;
                }
                String tabId = VariableKt.valueAsString(properties.get("Id"));
                if ((tabId == null || tabId.isBlank()) && child instanceof NodeElementWithSelector withSelector) {
                    tabId = cleanSelector(withSelector.getSelector() != null ? withSelector.getSelector().getIdentifier() : null);
                }
                if (tabId != null) {
                    tab.withId(tabId);
                }
                String tabText = VariableKt.valueAsString(properties.get("Text"));
                if (tabText != null) {
                    tab.withText(tabText);
                }
                HyUIPatchStyle icon = (HyUIPatchStyle) valueConverter.convert(properties.get("Icon"), HyUIPatchStyle.class);
                if (icon != null) {
                    tab.withIcon(icon);
                }
                HyUIPatchStyle iconSelected = (HyUIPatchStyle) valueConverter.convert(properties.get("IconSelected"), HyUIPatchStyle.class);
                if (iconSelected != null) {
                    tab.withIconSelected(iconSelected);
                }
                HyUIAnchor anchor = (HyUIAnchor) valueConverter.convert(properties.get("IconAnchor"), HyUIAnchor.class);
                if (anchor != null) {
                    tab.withIconAnchor(anchor);
                }
                builder.addTab(tab);
                continue;
            }

            UIElementBuilder<?> childBuilder = convertElement(child);
            if (childBuilder != null) {
                builder.addChild(childBuilder);
            }
        }

        for (NodeSelectorElement selectorElement : element.getSelectorElements()) {
            applySelectorChildren(builder, selectorElement);
        }
    }

    private void applyDropdownEntries(DropdownBoxBuilder builder, NodeElement element) {
        for (NodeElement child : element.getChildElements()) {
            String rawType = extractTypeName(child);
            if (!"DropdownEntry".equalsIgnoreCase(rawType)) {
                continue;
            }
            Map<String, VariableValue> properties;
            try {
                properties = child.resolveProperties();
            } catch (RuntimeException e) {
                if (result != null) {
                    result.addConversionWarning("Failed to resolve DropdownEntry properties: " + e.getMessage());
                }
                continue;
            }
            String text = VariableKt.valueAsString(properties.get("Text"));
            String value = VariableKt.valueAsString(properties.get("Value"));
            if (value == null || value.isBlank()) {
                if (child instanceof NodeElementWithSelector withSelector) {
                    value = cleanSelector(withSelector.getSelector() != null ? withSelector.getSelector().getIdentifier() : null);
                }
                if (value == null || value.isBlank()) {
                    value = text;
                }
            }
            if (value == null) {
                continue;
            }
            String label = text != null ? text : value;
            builder.addEntry(new DropdownEntryInfo(LocalizableString.fromString(label), value));
        }
    }

    private void applySelectorChildren(UIElementBuilder<?> builder, NodeSelectorElement selectorElement) {
        String selector = selectorElement.getSelector() != null ? selectorElement.getSelector().getIdentifier() : null;
        String cleanedSelector = cleanSelector(selector);
        if (builder instanceof ContainerBuilder container) {
            for (NodeElement child : selectorElement.getChildElements()) {
                UIElementBuilder<?> childBuilder = convertElement(child);
                if (childBuilder == null) {
                    continue;
                }
                if ("Title".equalsIgnoreCase(cleanedSelector)) {
                    container.addTitleChild(childBuilder);
                } else if ("Content".equalsIgnoreCase(cleanedSelector)) {
                    container.addContentChild(childBuilder);
                } else {
                    container.addChild(childBuilder);
                    if (result != null) {
                        result.addConversionWarning("Unsupported selector " + selector + " for Container element in " + documentPath);
                    }
                }
            }
            return;
        }

        if (result != null) {
            result.addConversionWarning("Selector blocks are only supported for Container elements. Selector " + selector + " was ignored in " + documentPath);
        }
        for (NodeElement child : selectorElement.getChildElements()) {
            UIElementBuilder<?> childBuilder = convertElement(child);
            if (childBuilder != null) {
                builder.addChild(childBuilder);
            }
        }
    }

    private void applyProperties(UIElementBuilder<?> builder, NodeElement element) {
        NodeElement resolvedImpl = element.getResolvedVariableImplementation();
        if (resolvedImpl != null && resolvedImpl.getBody() != null) {
            resolvedImpl.getBody().setActiveScopeKey$Parser(element);
        }
        Map<String, NodeField> rawFields = collectRawFields(element);
        Map<String, VariableValue> resolved;
        try {
            resolved = element.resolveProperties();
        } catch (RuntimeException e) {
            if (result != null) {
                result.addConversionWarning("Failed to resolve properties for " + extractTypeName(element) + ": " + e.getMessage());
            }
            return;
        }

        ElementType resolvedType = element.getResolvedType();
        if (resolvedType == null) {
            resolvedType = inferElementType(extractTypeName(element));
        }
        if (resolvedType == null) {
            return;
        }

        Set<String> applied = applyResolvedProperties(builder, resolved, rawFields, resolvedType);
        applyRawFields(builder, rawFields, applied);
        applyVariableAssignments(builder, element.getLocalVariables(), applied);
    }

    private Set<String> applyResolvedProperties(UIElementBuilder<?> builder,
                                                Map<String, VariableValue> resolved,
                                                Map<String, NodeField> rawFields,
                                                ElementType resolvedType) {
        Set<String> applied = new HashSet<>();
        if (resolved == null || resolved.isEmpty()) {
            return applied;
        }
        for (Map.Entry<String, VariableValue> entry : resolved.entrySet()) {
            String propertyKey = entry.getKey();
            VariableValue resolvedValue = entry.getValue();
            if (propertyKey == null || resolvedValue == null) {
                continue;
            }
            VariableValue rawValue = null;
            NodeField rawField = rawFields.get(propertyKey);
            if (rawField != null) {
                rawValue = rawField.getValueAsVariableValue();
            }
            VariableValue adapterValue = rawValue != null ? rawValue : resolvedValue;
            TypeType propertyType = resolvedType != null ? resolvedType.getProperties().get(propertyKey) : null;
            if (applyPropertyChain(builder, propertyKey, new RawValueAdapter(adapterValue), rawValue != null ? rawValue : resolvedValue, propertyType)) {
                applied.add(propertyKey);
            }
        }
        return applied;
    }

    private ElementType inferElementType(String rawTypeName) {
        if (rawTypeName == null || rawTypeName.isBlank()) {
            return null;
        }
        for (ElementType candidate : ElementType.values()) {
            if (candidate.name().equalsIgnoreCase(rawTypeName)) {
                return candidate;
            }
        }
        return null;
    }

    private void applyRawFields(UIElementBuilder<?> builder, Map<String, NodeField> rawFields, Set<String> applied) {
        if (rawFields == null || rawFields.isEmpty()) {
            return;
        }
        for (Map.Entry<String, NodeField> entry : rawFields.entrySet()) {
            String propertyKey = entry.getKey();
            if (propertyKey == null || propertyKey.isBlank()) {
                continue;
            }
            if (applied != null && applied.contains(propertyKey)) {
                continue;
            }
            NodeField field = entry.getValue();
            if (field == null) {
                continue;
            }
            VariableValue rawValue = field.getValueAsVariableValue();
            if (rawValue == null) {
                continue;
            }
            if (applyPropertyChain(builder, propertyKey, new RawValueAdapter(rawValue), rawValue, null)) {
                if (applied != null) {
                    applied.add(propertyKey);
                }
            }
        }
    }

    private void applyVariableAssignments(UIElementBuilder<?> builder, List<NodeAssignVariable> variables, Set<String> applied) {
        if (variables == null || variables.isEmpty()) {
            return;
        }
        for (NodeAssignVariable assign : variables) {
            if (assign == null || assign.getVariable() == null) {
                continue;
            }
            String rawName = assign.getVariable().getIdentifier();
            String propertyKey = toPropertyKey(stripVariablePrefix(rawName));
            if (propertyKey == null || propertyKey.isBlank()) {
                continue;
            }
            if (applied != null && applied.contains(propertyKey)) {
                continue;
            }
            VariableValue rawValue = assign.getValueAsVariable();
            if (rawValue == null) {
                continue;
            }
            if (applyPropertyViaMethod(builder, propertyKey, new RawValueAdapter(rawValue))) {
                if (applied != null) {
                    applied.add(propertyKey);
                }
            }
        }
    }

    private boolean applySpecialProperty(UIElementBuilder<?> builder, String propertyKey, ValueAdapter value) {
        if ("TextSpans".equals(propertyKey)) {
            String text = (String) value.convertTo(String.class);
            if (text == null) {
                return true;
            }
            List<Message> spans = List.of(Message.raw(text));
            if (builder instanceof LabelBuilder label) {
                label.withTextSpans(spans);
            }
            return true;
        }
        if ("TooltipTextSpans".equals(propertyKey)) {
            String text = (String) value.convertTo(String.class);
            if (text != null) {
                builder.withTooltipTextSpan(Message.raw(text));
            }
            return true;
        }
        if ("TooltipText".equals(propertyKey)) {
            String text = (String) value.convertTo(String.class);
            if (text != null) {
                builder.withTooltipText(text);
            }
            return true;
        }
        return false;
    }

    private boolean applyPrimaryStyle(UIElementBuilder<?> builder,
                                      ValueAdapter value,
                                      VariableValue rawValue,
                                      TypeType propertyType) {
        if (propertyType != null && propertyType != TypeType.LabelStyle) {
            Object typed = rawValue != null ? valueConverter.convertTyped(rawValue, propertyType) : null;
            if (typed instanceof HyUIBsonSerializable hyuiStyle) {
                builder.withStyle(hyuiStyle);
                return true;
            }
            Object converted = value.convertTo(HyUIBsonSerializable.class);
            if (converted instanceof HyUIBsonSerializable hyuiStyle) {
                builder.withStyle(hyuiStyle);
                return true;
            }
            return false;
        }
        Object converted = value.convertTo(HyUIStyle.class);
        if (converted instanceof HyUIStyle style) {
            builder.withStyle(style);
            return true;
        }
        return false;
    }

    private boolean applySecondaryStyle(UIElementBuilder<?> builder,
                                        String property,
                                        ValueAdapter value,
                                        VariableValue rawValue,
                                        TypeType propertyType) {
        if (!property.contains("Style")) {
            return false;
        }
        if (propertyType != null && propertyType != TypeType.LabelStyle) {
            Object typed = rawValue != null ? valueConverter.convertTyped(rawValue, propertyType) : null;
            if (typed instanceof HyUIBsonSerializable hyuiStyle) {
                builder.withSecondaryStyle(property, hyuiStyle);
                return true;
            }
            Object converted = value.convertTo(HyUIBsonSerializable.class);
            if (converted instanceof HyUIBsonSerializable typedStyle) {
                builder.withSecondaryStyle(property, typedStyle);
                return true;
            }
            return false;
        }
        Object converted = value.convertTo(HyUIStyle.class);
        if (converted instanceof HyUIStyle style) {
            builder.withSecondaryStyle(property, style);
            return true;
        }
        return false;
    }

    private boolean applyPropertyViaMethod(UIElementBuilder<?> builder, String property, ValueAdapter value) {
        String methodName = "with" + property;
        if (applyPropertyViaNamedMethod(builder, methodName, value)) {
            return true;
        }
        if (property.startsWith("Is") && property.length() > 2) {
            if (applyPropertyViaNamedMethod(builder, "with" + property.substring(2), value)) {
                return true;
            }
        }
        if ("MouseWheelScrollBehavior".equals(property)) {
            return applyPropertyViaNamedMethod(builder, "withMouseWheelScrollBehaviour", value);
        }
        return false;
    }

    private boolean applyPropertyViaNamedMethod(UIElementBuilder<?> builder, String methodName, ValueAdapter value) {
        for (Method method : getOneArgMethods(builder.getClass(), methodName)) {
            Class<?> paramType = method.getParameterTypes()[0];
            Object converted = value.convertTo(paramType);
            if (converted == null) {
                continue;
            }
            try {
                method.invoke(builder, converted);
                return true;
            } catch (ReflectiveOperationException ignored) {
                // Continue
            }
        }
        return false;
    }

    private boolean applyStyleReference(UIElementBuilder<?> builder, String property, VariableValue rawValue) {
        if (!property.contains("Style")) {
            return false;
        }
        UIValueConverter.UiStyleReference reference = valueConverter.resolveStyleReference(rawValue);
        if (reference == null) {
            return false;
        }
        // Likely common.ui,
        // TODO: support more than common ui :(((
        // reference.document() - path to doc.
        
        if ("Style".equals(property)) {

            HyUIStyle style = new HyUIStyle().withStyleReference("Common.ui", reference.reference());
            builder.withStyle(style);
            return true;
        }

        Method twoArg = findMethod(builder.getClass(), "with" + property, String.class, String.class);
        if (twoArg != null) {
            try {
                twoArg.invoke(builder, "Common.ui", reference.reference());
                return true;
            } catch (ReflectiveOperationException ignored) {
                // Fall through
            }
        }

        Method oneArg = findMethod(builder.getClass(), "with" + property, String.class);
        if (oneArg != null) {
            try {
                oneArg.invoke(builder, reference.reference());
                return true;
            } catch (ReflectiveOperationException ignored) {
                // Fall through
            }
        }

        builder.withSecondaryStyle(property, new HyUIStyle().withStyleReference("Common.ui", reference.reference()));
        return true;
    }

    private boolean applyPropertyChain(UIElementBuilder<?> builder,
                                       String propertyKey,
                                       ValueAdapter adapter,
                                       VariableValue rawValue,
                                       TypeType propertyType) {
        if (applySpecialProperty(builder, propertyKey, adapter)) {
            return true;
        }
        if (applyStyleReference(builder, propertyKey, rawValue)) {
            return true;
        }
        if ("Style".equals(propertyKey) && applyPrimaryStyle(builder, adapter, rawValue, propertyType)) {
            return true;
        }
        if (applyPropertyViaMethod(builder, propertyKey, adapter)) {
            return true;
        }
        return applySecondaryStyle(builder, propertyKey, adapter, rawValue, propertyType);
    }

    private interface ValueAdapter {
        Object convertTo(Class<?> targetType);
    }

    private final class RawValueAdapter implements ValueAdapter {
        private final VariableValue value;

        private RawValueAdapter(VariableValue value) {
            this.value = value;
        }

        @Override
        public Object convertTo(Class<?> targetType) {
            return valueConverter.convert(value, targetType);
        }
    }

    private Method findMethod(Class<?> type, String name, Class<?>... params) {
        MethodKey key = new MethodKey(type, name, signatureFor(params));
        Method cached = METHOD_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        if (METHOD_MISS_CACHE.containsKey(key)) {
            return null;
        }
        for (Method method : type.getMethods()) {
            if (!method.getName().equals(name)) {
                continue;
            }
            if (params == null) {
                if (method.getParameterCount() == 1) {
                    METHOD_CACHE.put(key, method);
                    return method;
                }
                continue;
            }
            if (method.getParameterCount() != params.length) {
                continue;
            }
            boolean matches = true;
            for (int i = 0; i < params.length; i++) {
                if (!method.getParameterTypes()[i].equals(params[i])) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                METHOD_CACHE.put(key, method);
                return method;
            }
        }
        METHOD_MISS_CACHE.put(key, Boolean.TRUE);
        return null;
    }

    private String signatureFor(Class<?>... params) {
        if (params == null) {
            return "null";
        }
        return Arrays.toString(params);
    }

    private List<Method> getOneArgMethods(Class<?> type, String name) {
        Map<String, List<Method>> cached = ONE_ARG_METHODS_CACHE.get(type);
        if (cached == null) {
            Map<String, List<Method>> built = new HashMap<>();
            for (Method method : type.getMethods()) {
                if (method.getParameterCount() != 1) {
                    continue;
                }
                built.computeIfAbsent(method.getName(), key -> new ArrayList<>()).add(method);
            }
            ONE_ARG_METHODS_CACHE.put(type, built);
            cached = built;
        }
        List<Method> methods = cached.get(name);
        return methods != null ? methods : List.of();
    }

    private Map<String, NodeField> collectRawFields(NodeElement element) {
        Map<String, NodeField> fields = new LinkedHashMap<>();
        NodeElement implementation = element.getResolvedVariableImplementation();
        if (implementation != null) {
            for (NodeField field : implementation.getProperties()) {
                if (field.getIdentifier() == null) {
                    continue;
                }
                String name = field.getIdentifier().getIdentifier();
                fields.putIfAbsent(name, field);
            }
        }
        for (NodeField field : element.getProperties()) {
            if (field.getIdentifier() == null) {
                continue;
            }
            fields.put(field.getIdentifier().getIdentifier(), field);
        }
        return fields;
    }

    private String extractTypeName(NodeElement element) {
        if (element.getType() == null) {
            return "";
        }
        if (element.getType() instanceof NodeVariable nodeVariable) {
            return stripVariablePrefix(nodeVariable.getIdentifier());
        }
        if (element.getType() instanceof NodeRefMember refMember) {
            if (refMember.getMember() != null) {
                return stripVariablePrefix(refMember.getMember().getIdentifier());
            }
        }
        return element.getType().getText();
    }

    private UIElementBuilder<?> createBuilder(String rawTypeName, ElementType resolvedType) {
        if (rawTypeName != null) {
            String normalized = rawTypeName.replace("#", "");
            UIElementBuilder<?> macro = createMacroBuilder(normalized);
            if (macro != null) {
                return macro;
            }
        }

        ElementType type = resolvedType;
        if (type == null && rawTypeName != null) {
            for (ElementType candidate : ElementType.values()) {
                if (candidate.name().equalsIgnoreCase(rawTypeName)) {
                    type = candidate;
                    break;
                }
            }
        }
        if (type == null) {
            return null;
        }

        return switch (type) {
            case Group -> GroupBuilder.group();
            case TimerLabel -> NativeTimerLabelBuilder.nativeTimerLabel();
            case Label -> LabelBuilder.label();
            case TextButton -> ButtonBuilder.textButton();
            case Button -> ButtonBuilder.rawButton();
            case CheckBox -> new CheckBoxBuilder();
            case CheckBoxContainer -> GroupBuilder.group();
            case TextField -> TextFieldBuilder.textInput();
            case NumberField -> NumberFieldBuilder.numberInput();
            case DropdownEntry -> null;
            case DropdownBox -> DropdownBoxBuilder.dropdownBox();
            case Sprite -> SpriteBuilder.sprite();
            case CompactTextField -> TextFieldBuilder.compactTextField();
            case FloatSlider -> FloatSliderBuilder.floatSlider();
            case MultilineTextField -> TextFieldBuilder.multilineTextField();
            case ColorPickerDropdownBox -> ColorPickerDropdownBoxBuilder.colorPickerDropdownBox();
            case CircularProgressBar -> ProgressBarBuilder.circularProgressBar();
            case CodeEditor -> null;
            case ColorOptionGrid -> null;
            case ProgressBar -> ProgressBarBuilder.progressBar();
            case Slider -> SliderBuilder.slider();
            case ItemSlotButton -> ItemSlotButtonBuilder.itemSlotButton();
            case ItemSlot -> ItemSlotBuilder.itemSlot();
            case AssetImage -> ImageBuilder.image();
            case SceneBlur -> SceneBlurBuilder.sceneBlur();
            case ItemGrid -> ItemGridBuilder.itemGrid();
            case ItemIcon -> ItemIconBuilder.itemIcon();
            case ColorPicker -> new ColorPickerBuilder();
            case BackgroundImage -> null;
            case TabButton -> NativeTabButtonBuilder.nativeTabButton();
            case TabNavigation -> NativeTabNavigationBuilder.nativeTabNavigation();
            case ToggleButton -> ToggleButtonBuilder.toggleButton();
            case ItemPreviewComponent -> null;
            case CharacterPreviewComponent -> null;
            case SliderNumberField -> SliderNumberFieldBuilder.sliderNumberField();
            case BlockSelector -> BlockSelectorBuilder.blockSelector();
            case ReorderableListGrip -> ReorderableListGripBuilder.reorderableListGrip();
            case ReorderableList -> ReorderableListBuilder.reorderableList();
            case FloatSliderNumberField -> FloatSliderNumberFieldBuilder.floatSliderNumberField();
            case ActionButton -> ActionButtonBuilder.actionButton();
            case BackButton -> ButtonBuilder.backButton();
            case Panel -> PanelBuilder.panel();
            case LabeledCheckBox -> LabeledCheckBoxBuilder.labeledCheckBox();
            case PlayerPreviewComponent -> null;
            case HotkeyLabel -> HotkeyLabelBuilder.hotkeyLabel();
            case MenuItem -> MenuItemBuilder.menuItem();
            case DynamicPane -> DynamicPaneBuilder.dynamicPane();
            case DynamicPaneContainer -> DynamicPaneContainerBuilder.dynamicPaneContainer();
        };
    }

    private UIElementBuilder<?> createMacroBuilder(String rawTypeName) {
        if (rawTypeName == null) {
            return null;
        }
        String name = rawTypeName.trim();
        return switch (name) {
            case "Container" -> ContainerBuilder.container();
            case "DecoratedContainer" -> ContainerBuilder.decoratedContainer();
            case "PageOverlay" -> PageOverlayBuilder.pageOverlay();
            case "TextButton" -> ButtonBuilder.textButton();
            case "SecondaryTextButton" -> ButtonBuilder.secondaryTextButton();
            case "SmallSecondaryTextButton" -> ButtonBuilder.smallSecondaryTextButton();
            case "TertiaryTextButton" -> ButtonBuilder.tertiaryTextButton();
            case "SmallTertiaryTextButton" -> ButtonBuilder.smallTertiaryTextButton();
            case "CancelTextButton" -> ButtonBuilder.cancelTextButton();
            case "BackButton" -> ButtonBuilder.backButton();
            case "CustomButton" -> CustomButtonBuilder.customButton();
            case "CheckBoxWithLabel" -> new CheckBoxBuilder();
            case "AssetImage" -> ImageBuilder.image();
            case "Text" -> LabelBuilder.label();
            case "Input" -> TextFieldBuilder.textInput();
            //case "CustomTextButton" -> CustomButtonBuilder.customButton();
            default -> null;
        };
    }

    private String cleanSelector(String selector) {
        if (selector == null) {
            return null;
        }
        String cleaned = selector.trim();
        if (cleaned.startsWith("#") || cleaned.startsWith("@")) {
            cleaned = cleaned.substring(1);
        }
        return cleaned;
    }

    private String stripVariablePrefix(String identifier) {
        if (identifier == null) {
            return "";
        }
        if (identifier.startsWith("@") || identifier.startsWith("#")) {
            return identifier.substring(1);
        }
        return identifier;
    }

    private String toPropertyKey(String propertyName) {
        if (propertyName == null || propertyName.isBlank()) {
            return propertyName;
        }
        return Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    }

    private record MethodKey(Class<?> type, String name, String signature) {}

}
