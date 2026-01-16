package au.ellie.hyui.elements;

import java.util.Set;

public class UIElements {
    // Primitives
    public static final String GROUP = "Group";
    public static final String LABEL = "Label";
    public static final String COLOR_PICKER = "ColorPicker";
    public static final String BUTTON = "Button";
    public static final String TEXT_FIELD = "TextField";
    public static final String SLIDER = "Slider";

    // Macros (Common.ui)
    public static final String PAGE_OVERLAY = "PageOverlay";
    public static final String CONTAINER = "Container";
    public static final String TEXT_BUTTON = "TextButton";
    public static final String CANCEL_TEXT_BUTTON = "CancelTextButton";
    public static final String CHECK_BOX_WITH_LABEL = "CheckBoxWithLabel";
    public static final String MACRO_TEXT_FIELD = "TextField";
    public static final String MACRO_NUMBER_FIELD = "NumberField";
    public static final String BACK_BUTTON = "BackButton";

    public static final Set<String> NORMAL_ELEMENTS = Set.of(
            GROUP, LABEL, COLOR_PICKER, BUTTON, TEXT_FIELD, "Input", "CheckBox", "Slider", "ItemSlot", "Text"
    );

    public static final Set<String> MACRO_ELEMENTS = Set.of(
            PAGE_OVERLAY, CONTAINER, TEXT_BUTTON, CANCEL_TEXT_BUTTON, CHECK_BOX_WITH_LABEL, MACRO_TEXT_FIELD, MACRO_NUMBER_FIELD, BACK_BUTTON, "Title", SLIDER
    );

    private UIElements() {}
}
