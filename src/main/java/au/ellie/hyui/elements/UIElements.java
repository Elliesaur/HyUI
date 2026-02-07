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

package au.ellie.hyui.elements;

import java.util.Set;

public class UIElements {
    // Primitives
    public static final String GROUP = "Group";
    public static final String LABEL = "Label";
    public static final String COLOR_PICKER = "ColorPicker";
    public static final String BUTTON = "Button";
    public static final String TEXT_FIELD = "TextField";
    public static final String MULTILINE_TEXT_FIELD = "MultilineTextField";
    public static final String SLIDER = "Slider";
    public static final String PROGRESS_BAR = "ProgressBar";
    public static final String ITEM_ICON = "ItemIcon";
    public static final String DROPDOWN_BOX = "DropdownBox";
    public static final String SPRITE = "Sprite";
    public static final String ITEM_GRID = "ItemGrid";
    public static final String ITEM_SLOT = "ItemSlot";
    public static final String COMPACT_TEXT_FIELD = "CompactTextField";
    public static final String FLOAT_SLIDER = "FloatSlider";
    public static final String COLOR_PICKER_DROPDOWN_BOX = "ColorPickerDropdownBox";
    public static final String CIRCULAR_PROGRESS_BAR = "CircularProgressBar";
    public static final String ITEM_SLOT_BUTTON = "ItemSlotButton";
    public static final String SCENE_BLUR = "SceneBlur";
    public static final String BACKGROUND_IMAGE = "BackgroundImage";
    public static final String TOGGLE_BUTTON = "ToggleButton";
    public static final String ITEM_PREVIEW_COMPONENT = "ItemPreviewComponent";
    public static final String CHARACTER_PREVIEW_COMPONENT = "CharacterPreviewComponent";
    public static final String SLIDER_NUMBER_FIELD = "SliderNumberField";
    public static final String BLOCK_SELECTOR = "BlockSelector";
    public static final String REORDERABLE_LIST_GRIP = "ReorderableListGrip";
    public static final String REORDERABLE_LIST = "ReorderableList";
    public static final String TAB_BUTTON = "TabButton";
    public static final String FLOAT_SLIDER_NUMBER_FIELD = "FloatSliderNumberField";
    public static final String ACTION_BUTTON = "ActionButton";
    public static final String PANEL = "Panel";
    public static final String LABELED_CHECK_BOX = "LabeledCheckBox";
    public static final String PLAYER_PREVIEW_COMPONENT = "PlayerPreviewComponent";
    public static final String HOTKEY_LABEL = "HotkeyLabel";
    public static final String MENU_ITEM = "MenuItem";
    public static final String DYNAMIC_PANE_CONTAINER = "DynamicPaneContainer";
    public static final String DYNAMIC_PANE = "DynamicPane";
    public static final String NATIVE_TAB_BUTTON = "NativeTabButton";
    public static final String NATIVE_TAB_NAVIGATION = "NativeTabNavigation";
    public static final String NATIVE_TIMER_LABEL = "NativeTimerLabel";

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
            GROUP,
            LABEL,
            COLOR_PICKER,
            BUTTON,
            TEXT_FIELD,
            MULTILINE_TEXT_FIELD,
            COMPACT_TEXT_FIELD,
            "Input",
            "CheckBox",
            SLIDER,
            FLOAT_SLIDER,
            SLIDER_NUMBER_FIELD,
            FLOAT_SLIDER_NUMBER_FIELD,
            ITEM_SLOT,
            ITEM_SLOT_BUTTON,
            "Text",
            PROGRESS_BAR,
            CIRCULAR_PROGRESS_BAR,
            ITEM_ICON,
            SPRITE,
            ITEM_GRID,
            TIMER_LABEL,
            TAB_NAVIGATION,
            TAB_BUTTON,
            DROPDOWN_BOX,
            COLOR_PICKER_DROPDOWN_BOX,
            TOGGLE_BUTTON,
            ACTION_BUTTON,
            BLOCK_SELECTOR,
            REORDERABLE_LIST_GRIP,
            REORDERABLE_LIST,
            BACKGROUND_IMAGE,
            SCENE_BLUR,
            ITEM_PREVIEW_COMPONENT,
            CHARACTER_PREVIEW_COMPONENT,
            PLAYER_PREVIEW_COMPONENT,
            HOTKEY_LABEL,
            MENU_ITEM,
            PANEL,
            LABELED_CHECK_BOX,
            DYNAMIC_PANE_CONTAINER,
            DYNAMIC_PANE,
            NATIVE_TAB_BUTTON,
            NATIVE_TAB_NAVIGATION,
            NATIVE_TIMER_LABEL
    );

    public static final Set<String> MACRO_ELEMENTS = Set.of(
            PAGE_OVERLAY, CONTAINER, TEXT_BUTTON, SECONDARY_TEXT_BUTTON, TERTIARY_TEXT_BUTTON, CANCEL_TEXT_BUTTON, CHECK_BOX_WITH_LABEL, MACRO_TEXT_FIELD, MACRO_NUMBER_FIELD, BACK_BUTTON, ASSET_IMAGE, "Title", SLIDER
    );

    private UIElements() {}
}
