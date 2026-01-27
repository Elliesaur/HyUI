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
    public static final String PROGRESS_BAR = "ProgressBar";
    public static final String ITEM_ICON = "ItemIcon";
    public static final String DROPDOWN_BOX = "DropdownBox";
    public static final String SPRITE = "Sprite";
    public static final String ITEM_GRID = "ItemGrid";
    public static final String ITEM_SLOT = "ItemSlot";

    // Macros (Common.ui)
    public static final String PAGE_OVERLAY = "PageOverlay";
    public static final String CONTAINER = "Container";
    public static final String TEXT_BUTTON = "TextButton";
    public static final String CUSTOM_TEXT_BUTTON = "CustomTextButton";
    public static final String SECONDARY_TEXT_BUTTON = "SecondaryTextButton";
    public static final String SMALL_SECONDARY_TEXT_BUTTON = "SmallSecondaryTextButton";
    public static final String TERTIARY_TEXT_BUTTON = "TertiaryTextButton";
    public static final String SMALL_TERTIARY_TEXT_BUTTON = "SmallTertiaryTextButton";
    public static final String CANCEL_TEXT_BUTTON = "CancelTextButton";
    public static final String CHECK_BOX_WITH_LABEL = "CheckBoxWithLabel";
    public static final String MACRO_TEXT_FIELD = "TextField";
    public static final String MACRO_NUMBER_FIELD = "NumberField";
    public static final String BACK_BUTTON = "BackButton";
    public static final String RAW_BUTTON = "Button";
    public static final String CUSTOM_BUTTON = "CustomButton";
    public static final String ASSET_IMAGE = "AssetImage";
    public static final String ITEM_ICON_MACRO = "ItemIcon";
    public static final String TIMER_LABEL = "TimerLabel";
    public static final String TAB_NAVIGATION = "TabNavigation";
    public static final Set<String> NORMAL_ELEMENTS = Set.of(
            GROUP, LABEL, COLOR_PICKER, BUTTON, TEXT_FIELD, "Input", "CheckBox", "Slider", ITEM_SLOT, "Text", PROGRESS_BAR, ITEM_ICON, SPRITE, ITEM_GRID, TIMER_LABEL, TAB_NAVIGATION
    );

    public static final Set<String> MACRO_ELEMENTS = Set.of(
            PAGE_OVERLAY, CONTAINER, TEXT_BUTTON, SECONDARY_TEXT_BUTTON, TERTIARY_TEXT_BUTTON, CANCEL_TEXT_BUTTON, CHECK_BOX_WITH_LABEL, MACRO_TEXT_FIELD, MACRO_NUMBER_FIELD, BACK_BUTTON, ASSET_IMAGE, "Title", SLIDER
    );

    private UIElements() {}
}
