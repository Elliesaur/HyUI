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

package au.ellie.hyui.types;

import au.ellie.hyui.builders.*;

/**
 * Default style definitions derived from Common.ui and Sounds.ui.
 */
public final class DefaultStyles {
    private static final int DEFAULT_LABEL_HEIGHT = 20;
    private static final int PRIMARY_BUTTON_HEIGHT = 44;
    private static final int SMALL_BUTTON_HEIGHT = 32;
    private static final int BIG_BUTTON_HEIGHT = 48;
    private static final int BUTTON_BORDER_SIZE = 12;
    private static final int DEFAULT_BUTTON_MIN_WIDTH = 172;
    private static final int BUTTON_PADDING = 24;
    private static final String DISABLED_COLOR = "#797b7c";

    private DefaultStyles() {}

    public static HyUIPatchStyle lightBlackOverlayBackground() {
        return new HyUIPatchStyle().setColor("#000000D1");
    }

    public static HyUIPatchStyle darkBlackOverlayBackground() {
        return new HyUIPatchStyle().setColor("#000000e0");
    }

    public static HyUIPatchStyle panelBackground() {
        return patchWithBorder("Common/DropdownBox.png", 16);
    }

    public static HyUIPatchStyle inputBoxBackground() {
        return patchWithBorder("Common/InputBox.png", 16);
    }

    public static HyUIPatchStyle inputBoxHoveredBackground() {
        return patchWithBorder("Common/InputBoxHovered.png", 16);
    }

    public static HyUIPatchStyle inputBoxPressedBackground() {
        return patchWithBorder("Common/InputBoxPressed.png", 16);
    }

    public static HyUIPatchStyle inputBoxSelectedBackground() {
        return patchWithBorder("Common/InputBoxSelected.png", 16);
    }

    public static HyUIStyle defaultLabelStyle() {
        return new HyUIStyle().setVerticalAlignment(Alignment.Center);
    }

    public static InputFieldStyle defaultInputFieldStyle() {
        return new InputFieldStyle();
    }

    public static InputFieldStyle defaultInputFieldPlaceholderStyle() {
        return new InputFieldStyle().withTextColor("#6e7da1");
    }

    public static HyUIStyle primaryButtonLabelStyle() {
        return new HyUIStyle()
                .setFontSize(17)
                .setTextColor("#bfcdd5")
                .setRenderBold(true)
                .setRenderUppercase(true)
                .setHorizontalAlignment(Alignment.Center)
                .setVerticalAlignment(Alignment.Center);
    }

    public static HyUIStyle primaryButtonDisabledLabelStyle() {
        return new HyUIStyle()
                .setFontSize(17)
                .setTextColor(DISABLED_COLOR)
                .setRenderBold(true)
                .setRenderUppercase(true)
                .setHorizontalAlignment(Alignment.Center)
                .setVerticalAlignment(Alignment.Center);
    }

    public static HyUIStyle secondaryButtonLabelStyle() {
        return new HyUIStyle()
                .setFontSize(17)
                .setTextColor("#bdcbd3")
                .setRenderBold(true)
                .setRenderUppercase(true)
                .setHorizontalAlignment(Alignment.Center)
                .setVerticalAlignment(Alignment.Center);
    }

    public static HyUIStyle secondaryButtonDisabledLabelStyle() {
        return new HyUIStyle()
                .setFontSize(17)
                .setTextColor(DISABLED_COLOR)
                .setRenderBold(true)
                .setRenderUppercase(true)
                .setHorizontalAlignment(Alignment.Center)
                .setVerticalAlignment(Alignment.Center);
    }

    public static HyUIStyle smallSecondaryButtonLabelStyle() {
        return new HyUIStyle()
                .setFontSize(14)
                .setTextColor("#bdcbd3")
                .setRenderBold(true)
                .setRenderUppercase(true)
                .setHorizontalAlignment(Alignment.Center)
                .setVerticalAlignment(Alignment.Center);
    }

    public static HyUIStyle smallSecondaryButtonDisabledLabelStyle() {
        return new HyUIStyle()
                .setFontSize(14)
                .setTextColor(DISABLED_COLOR)
                .setRenderBold(true)
                .setRenderUppercase(true)
                .setHorizontalAlignment(Alignment.Center)
                .setVerticalAlignment(Alignment.Center);
    }

    public static HyUIPatchStyle primaryButtonDefaultBackground() {
        return patchWithVerticalHorizontalBorder("Common/Buttons/Primary.png", BUTTON_BORDER_SIZE, 80);
    }

    public static HyUIPatchStyle primaryButtonHoveredBackground() {
        return patchWithVerticalHorizontalBorder("Common/Buttons/Primary_Hovered.png", BUTTON_BORDER_SIZE, 80);
    }

    public static HyUIPatchStyle primaryButtonPressedBackground() {
        return patchWithVerticalHorizontalBorder("Common/Buttons/Primary_Pressed.png", BUTTON_BORDER_SIZE, 80);
    }

    public static HyUIPatchStyle primaryButtonDisabledBackground() {
        return patchWithVerticalHorizontalBorder("Common/Buttons/Disabled.png", BUTTON_BORDER_SIZE, 80);
    }

    public static HyUIPatchStyle primarySquareButtonDefaultBackground() {
        return patchWithBorder("Common/Buttons/Primary_Square.png", BUTTON_BORDER_SIZE);
    }

    public static HyUIPatchStyle primarySquareButtonHoveredBackground() {
        return patchWithBorder("Common/Buttons/Primary_Square_Hovered.png", BUTTON_BORDER_SIZE);
    }

    public static HyUIPatchStyle primarySquareButtonPressedBackground() {
        return patchWithBorder("Common/Buttons/Primary_Square_Pressed.png", BUTTON_BORDER_SIZE);
    }

    public static HyUIPatchStyle primarySquareButtonDisabledBackground() {
        return patchWithBorder("Common/Buttons/Disabled.png", BUTTON_BORDER_SIZE);
    }

    public static ButtonSounds buttonSounds() {
        return (ButtonSounds) new ButtonSounds()
                .withActivate(new SoundStyle()
                        .withSoundPath("Sounds/ButtonsLightActivate.ogg")
                        .withMinPitch(-0.2f)
                        .withMaxPitch(0.2f)
                        .withVolume(2))
                .withMouseHover(new SoundStyle()
                        .withSoundPath("Sounds/ButtonsLightHover.ogg")
                        .withVolume(6));
    }

    public static ButtonSounds buttonDestructiveSounds() {
        return (ButtonSounds) new ButtonSounds()
                .withActivate(new SoundStyle()
                        .withSoundPath("Sounds/ButtonsCancelActivate.ogg")
                        .withMinPitch(-0.4f)
                        .withMaxPitch(0.4f)
                        .withVolume(6))
                .withMouseHover(new SoundStyle()
                        .withSoundPath("Sounds/ButtonsLightHover.ogg")
                        .withVolume(6));
    }

    public static DropdownBoxSounds dropdownBoxSounds() {
        return new DropdownBoxSounds()
                .withActivate(new SoundStyle()
                        .withSoundPath("Sounds/TickActivate.ogg")
                        .withVolume(6))
                .withMouseHover(new SoundStyle()
                        .withSoundPath("Sounds/ButtonsLightHover.ogg")
                        .withVolume(6))
                .withClose(new SoundStyle()
                        .withSoundPath("Sounds/UntickActivate.ogg")
                        .withVolume(6));
    }

    public static ButtonStyle primaryButtonStyle() {
        return new ButtonStyle()
                .withDefault(new ButtonStyleState().withBackground(primaryButtonDefaultBackground()))
                .withHovered(new ButtonStyleState().withBackground(primaryButtonHoveredBackground()))
                .withPressed(new ButtonStyleState().withBackground(primaryButtonPressedBackground()))
                .withDisabled(new ButtonStyleState().withBackground(primaryButtonDisabledBackground()))
                .withSounds(buttonSounds());
    }

    public static TextButtonStyle primaryTextButtonStyle() {
        return new TextButtonStyle()
                .withDefault((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(primaryButtonDefaultBackground())
                        .withLabelStyle(primaryButtonLabelStyle()))
                .withHovered((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(primaryButtonHoveredBackground())
                        .withLabelStyle(primaryButtonLabelStyle()))
                .withPressed((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(primaryButtonPressedBackground())
                        .withLabelStyle(primaryButtonLabelStyle()))
                .withDisabled((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(primaryButtonDisabledBackground())
                        .withLabelStyle(primaryButtonDisabledLabelStyle()))
                .withSounds(buttonSounds());
    }

    public static ButtonStyle primarySquareButtonStyle() {
        return new ButtonStyle()
                .withDefault(new ButtonStyleState().withBackground(primarySquareButtonDefaultBackground()))
                .withHovered(new ButtonStyleState().withBackground(primarySquareButtonHoveredBackground()))
                .withPressed(new ButtonStyleState().withBackground(primarySquareButtonPressedBackground()))
                .withDisabled(new ButtonStyleState().withBackground(primarySquareButtonDisabledBackground()))
                .withSounds(buttonSounds());
    }

    public static TextButtonStyle destructiveTextButtonStyle() {
        return new TextButtonStyle()
                .withDefault((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(patchWithBorder("Common/Buttons/Destructive.png", BUTTON_BORDER_SIZE))
                        .withLabelStyle(primaryButtonLabelStyle()))
                .withHovered((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(patchWithBorder("Common/Buttons/Destructive_Hovered.png", BUTTON_BORDER_SIZE))
                        .withLabelStyle(primaryButtonLabelStyle()))
                .withPressed((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(patchWithBorder("Common/Buttons/Destructive_Pressed.png", BUTTON_BORDER_SIZE))
                        .withLabelStyle(primaryButtonLabelStyle()))
                .withDisabled((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(patchWithBorder("Common/Buttons/Disabled.png", BUTTON_BORDER_SIZE))
                        .withLabelStyle(primaryButtonLabelStyle()))
                .withSounds(buttonDestructiveSounds());
    }

    public static ButtonStyle destructiveButtonStyle() {
        return new ButtonStyle()
                .withDefault(new ButtonStyleState().withBackground(patchWithBorder("Common/Buttons/Destructive.png", BUTTON_BORDER_SIZE)))
                .withHovered(new ButtonStyleState().withBackground(patchWithBorder("Common/Buttons/Destructive_Hovered.png", BUTTON_BORDER_SIZE)))
                .withPressed(new ButtonStyleState().withBackground(patchWithBorder("Common/Buttons/Destructive_Pressed.png", BUTTON_BORDER_SIZE)))
                .withDisabled(new ButtonStyleState().withBackground(patchWithBorder("Common/Buttons/Disabled.png", BUTTON_BORDER_SIZE)))
                .withSounds(buttonSounds());
    }

    public static ButtonStyle secondaryButtonStyle() {
        return new ButtonStyle()
                .withDefault(new ButtonStyleState().withBackground(patchWithBorder("Common/Buttons/Secondary.png", BUTTON_BORDER_SIZE)))
                .withHovered(new ButtonStyleState().withBackground(patchWithBorder("Common/Buttons/Secondary_Hovered.png", BUTTON_BORDER_SIZE)))
                .withPressed(new ButtonStyleState().withBackground(patchWithBorder("Common/Buttons/Secondary_Pressed.png", BUTTON_BORDER_SIZE)))
                .withDisabled(new ButtonStyleState().withBackground(patchWithBorder("Common/Buttons/Disabled.png", BUTTON_BORDER_SIZE)))
                .withSounds(buttonSounds());
    }

    public static TextButtonStyle secondaryTextButtonStyle() {
        return new TextButtonStyle()
                .withDefault((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(patchWithBorder("Common/Buttons/Secondary.png", BUTTON_BORDER_SIZE))
                        .withLabelStyle(secondaryButtonLabelStyle()))
                .withHovered((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(patchWithBorder("Common/Buttons/Secondary_Hovered.png", BUTTON_BORDER_SIZE))
                        .withLabelStyle(secondaryButtonLabelStyle()))
                .withPressed((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(patchWithBorder("Common/Buttons/Secondary_Pressed.png", BUTTON_BORDER_SIZE))
                        .withLabelStyle(secondaryButtonLabelStyle()))
                .withDisabled((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(patchWithBorder("Common/Buttons/Disabled.png", BUTTON_BORDER_SIZE))
                        .withLabelStyle(secondaryButtonDisabledLabelStyle()));
    }

    public static TextButtonStyle smallSecondaryTextButtonStyle() {
        return new TextButtonStyle()
                .withDefault((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(patchWithBorder("Common/Buttons/Secondary.png", BUTTON_BORDER_SIZE))
                        .withLabelStyle(smallSecondaryButtonLabelStyle()))
                .withHovered((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(patchWithBorder("Common/Buttons/Secondary_Hovered.png", BUTTON_BORDER_SIZE))
                        .withLabelStyle(smallSecondaryButtonLabelStyle()))
                .withPressed((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(patchWithBorder("Common/Buttons/Secondary_Pressed.png", BUTTON_BORDER_SIZE))
                        .withLabelStyle(smallSecondaryButtonLabelStyle()))
                .withDisabled((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(patchWithBorder("Common/Buttons/Disabled.png", BUTTON_BORDER_SIZE))
                        .withLabelStyle(smallSecondaryButtonDisabledLabelStyle()));
    }

    public static ButtonStyle tertiaryButtonStyle() {
        return new ButtonStyle()
                .withDefault(new ButtonStyleState().withBackground(patchWithBorder("Common/Buttons/Tertiary.png", BUTTON_BORDER_SIZE)))
                .withHovered(new ButtonStyleState().withBackground(patchWithBorder("Common/Buttons/Tertiary_Hovered.png", BUTTON_BORDER_SIZE)))
                .withPressed(new ButtonStyleState().withBackground(patchWithBorder("Common/Buttons/Tertiary_Pressed.png", BUTTON_BORDER_SIZE)))
                .withDisabled(new ButtonStyleState().withBackground(patchWithBorder("Common/Buttons/Disabled.png", BUTTON_BORDER_SIZE)))
                .withSounds(buttonSounds());
    }

    public static TextButtonStyle tertiaryTextButtonStyle() {
        return new TextButtonStyle()
                .withDefault((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(patchWithBorder("Common/Buttons/Tertiary.png", BUTTON_BORDER_SIZE))
                        .withLabelStyle(secondaryButtonLabelStyle()))
                .withHovered((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(patchWithBorder("Common/Buttons/Tertiary_Hovered.png", BUTTON_BORDER_SIZE))
                        .withLabelStyle(secondaryButtonLabelStyle()))
                .withPressed((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(patchWithBorder("Common/Buttons/Tertiary_Pressed.png", BUTTON_BORDER_SIZE))
                        .withLabelStyle(secondaryButtonLabelStyle()))
                .withDisabled((TextButtonStyleState) new TextButtonStyleState()
                        .withBackground(patchWithBorder("Common/Buttons/Disabled.png", BUTTON_BORDER_SIZE))
                        .withLabelStyle(secondaryButtonLabelStyle()));
    }

    public static CheckBoxStyle defaultCheckBoxStyle() {
        return new CheckBoxStyle()
                .withUnchecked(new CheckBoxStyleState()
                        .withDefaultBackground(new HyUIPatchStyle().setColor("#00000000"))
                        .withHoveredBackground(new HyUIPatchStyle().setColor("#00000000"))
                        .withPressedBackground(new HyUIPatchStyle().setColor("#00000000"))
                        .withDisabledBackground(new HyUIPatchStyle().setColor("#424242"))
                        .withChangedSound(new SoundStyle()
                                .withSoundPath("Sounds/UntickActivate.ogg")
                                .withVolume(6)))
                .withChecked(new CheckBoxStyleState()
                        .withDefaultBackground(new HyUIPatchStyle().setTexturePath("Common/Checkmark.png"))
                        .withHoveredBackground(new HyUIPatchStyle().setTexturePath("Common/Checkmark.png"))
                        .withPressedBackground(new HyUIPatchStyle().setTexturePath("Common/Checkmark.png"))
                        .withChangedSound(new SoundStyle()
                                .withSoundPath("Sounds/TickActivate.ogg")
                                .withVolume(6)));
    }

    public static HyUIStyle defaultDropdownBoxLabelStyle() {
        return new HyUIStyle()
                .setTextColor("#96a9be")
                .setRenderUppercase(true)
                .setVerticalAlignment(Alignment.Center)
                .setFontSize(13);
    }

    public static HyUIStyle defaultDropdownBoxEntryLabelStyle() {
        return new HyUIStyle()
                .setTextColor("#b7cedd")
                .setRenderUppercase(true)
                .setVerticalAlignment(Alignment.Center)
                .setFontSize(13);
    }

    public static DropdownBoxStyle defaultDropdownBoxStyle() {
        return new DropdownBoxStyle()
                .withDefaultBackground(patchWithBorder("Common/Dropdown.png", 16))
                .withHoveredBackground(patchWithBorder("Common/DropdownHovered.png", 16))
                .withPressedBackground(patchWithBorder("Common/DropdownPressed.png", 16))
                .withDefaultArrowTexturePath("Common/DefaultDropdownCaret.png")
                .withHoveredArrowTexturePath("Common/DefaultDropdownCaret.png")
                .withPressedArrowTexturePath("Common/PressedDropdownCaret.png")
                .withArrowWidth(13)
                .withArrowHeight(18)
                .withLabelStyle(defaultDropdownBoxLabelStyle())
                .withEntryLabelStyle(defaultDropdownBoxEntryLabelStyle())
                .withNoItemsLabelStyle(new HyUIStyle()
                        .setTextColor("#b7cedd80")
                        .setRenderUppercase(true)
                        .setVerticalAlignment(Alignment.Center)
                        .setFontSize(13))
                .withSelectedEntryLabelStyle(new HyUIStyle()
                        .setTextColor("#b7cedd")
                        .setRenderUppercase(true)
                        .setVerticalAlignment(Alignment.Center)
                        .setFontSize(13)
                        .setRenderBold(true))
                .withHorizontalPadding(8)
                .withPanelScrollbarStyle(ScrollbarStyle.defaultStyle())
                .withPanelBackground(patchWithBorder("Common/DropdownBox.png", 16))
                .withPanelPadding(6)
                .withPanelAlign("Right")
                .withPanelOffset(7)
                .withEntryHeight(31)
                .withEntriesInViewport(10)
                .withHorizontalEntryPadding(7)
                .withHoveredEntryBackground(new HyUIPatchStyle().setColor("#0a0f17"))
                .withPressedEntryBackground(new HyUIPatchStyle().setColor("#0f1621"))
                .withSounds(dropdownBoxSounds())
                .withEntrySounds(buttonSounds())
                .withFocusOutlineSize(1)
                .withFocusOutlineColor("#ffffff66");
    }

    public static DropdownBoxStyle titledDropdownBoxStyle() {
        return defaultDropdownBoxStyle()
                .withDefaultBackground(patchWithBorder("Common/DropdownAlt.png", 16))
                .withHoveredBackground(patchWithBorder("Common/DropdownAltHovered.png", 16))
                .withPressedBackground(patchWithBorder("Common/DropdownAltHovered.png", 16))
                .withDefaultArrowTexturePath("Common/AltDropdownCaretDown.png")
                .withHoveredArrowTexturePath("Common/AltDropdownCaretDown.png")
                .withPressedArrowTexturePath("Common/AltDropdownCaretUp.png")
                .withArrowWidth(12)
                .withArrowHeight(7)
                .withPanelTitleLabelStyle(new HyUIStyle()
                        .setTextColor("#96a9be")
                        .setRenderBold(true)
                        .setRenderUppercase(true)
                        .setVerticalAlignment(Alignment.Center)
                        .setFontSize(13))
                .withPanelPadding(29)
                .withPanelAlign("Bottom")
                .withPanelBackground(patchWithBorder("Common/TitledDropdownBox.png", 57))
                .withHoveredEntryBackground(new HyUIPatchStyle().setColor("#252e48"))
                .withPressedEntryBackground(new HyUIPatchStyle().setColor("#232b44"))
                .withSounds(dropdownBoxSounds());
    }

    public static FileDropdownBoxStyle defaultFileDropdownBoxStyle() {
        return new FileDropdownBoxStyle()
                .withDefaultBackground(patchWithBorder("Common/Dropdown.png", 16))
                .withHoveredBackground(patchWithBorder("Common/DropdownHovered.png", 16))
                .withPressedBackground(patchWithBorder("Common/DropdownPressed.png", 16))
                .withDefaultArrowTexturePath("Common/DefaultDropdownCaret.png")
                .withHoveredArrowTexturePath("Common/DefaultDropdownCaret.png")
                .withPressedArrowTexturePath("Common/PressedDropdownCaret.png")
                .withArrowWidth(9)
                .withArrowHeight(18)
                .withLabelStyle(new HyUIStyle()
                        .setTextColor("#96a9be")
                        .setRenderBold(true)
                        .setVerticalAlignment(Alignment.Center)
                        .setFontSize(18))
                .withHorizontalPadding(14)
                .withPanelAlign("Bottom")
                .withPanelOffset(7)
                .withSounds(dropdownBoxSounds());
    }

    public static PopupMenuLayerStyle defaultPopupMenuLayerStyle() {
        return new PopupMenuLayerStyle()
                .withBackground(patchWithBorder("Common/Popup.png", 16))
                .withPadding(2)
                .withBaseHeight(5)
                .withMaxWidth(200)
                .withTitleStyle(new HyUIStyle()
                        .setRenderBold(true)
                        .setRenderUppercase(true)
                        .setFontSize(13)
                        .setTextColor("#ccb588"))
                .withTitleBackground(new HyUIPatchStyle().setTexturePath("Common/PopupTitle.png"))
                .withRowHeight(25)
                .withItemLabelStyle(new HyUIStyle()
                        .setRenderBold(true)
                        .setRenderUppercase(true)
                        .setFontSize(11)
                        .setTextColor("#96a9beCC"))
                .withItemPadding(HyUIPadding.symmetric(5, 8))
                .withItemBackground(new HyUIPatchStyle().setTexturePath("Common/PopupItem.png"))
                .withItemIconSize(16)
                .withHoveredItemBackground(new HyUIPatchStyle().setTexturePath("Common/HoveredPopupItem.png"))
                .withPressedItemBackground(new HyUIPatchStyle().setTexturePath("Common/PressedPopupItem.png"))
                .withItemSounds(buttonSounds());
    }

    public static HyUIStyle popupTitleStyle() {
        return new HyUIStyle()
                .setFontSize(38)
                .setLetterSpacing(2)
                .setRenderUppercase(true)
                .setRenderBold(true)
                .setHorizontalAlignment(Alignment.Center)
                .setVerticalAlignment(Alignment.Center);
    }

    public static ColorPickerStyle defaultColorPickerStyle() {
        return new ColorPickerStyle()
                .withOpacitySelectorBackground("Common/ColorPickerOpacitySelectorBackground.png")
                .withButtonBackground("Common/ColorPickerButton.png")
                .withButtonFill("Common/ColorPickerFill.png")
                .withTextFieldDecoration(new InputFieldDecorationStyle()
                        .withDefault(new InputFieldDecorationStyleState()
                                .withBackground(new HyUIPatchStyle().setColor("#00000080"))))
                .withTextFieldPadding(new HyUIPadding().setLeft(7))
                .withTextFieldHeight(32);
    }

    public static ColorPickerDropdownBoxStyle defaultColorPickerDropdownBoxStyle() {
        return new ColorPickerDropdownBoxStyle()
                .withColorPickerStyle(defaultColorPickerStyle())
                .withBackground(new ColorPickerDropdownBoxStateBackground()
                        .withDefault(new HyUIPatchStyle().setTexturePath("Common/ColorPickerDropdownBoxBackground.png")))
                .withArrowBackground(new ColorPickerDropdownBoxStateBackground()
                        .withDefault(new HyUIPatchStyle().setTexturePath("Common/ColorPickerDropdownBoxArrow.png")))
                .withOverlay(new ColorPickerDropdownBoxStateBackground()
                        .withDefault(new HyUIPatchStyle().setTexturePath("Common/ColorPickerDropdownBoxOverlay.png")))
                .withPanelBackground(patchWithBorder("Common/DropdownBox.png", 16))
                .withPanelPadding(HyUIPadding.all(15))
                .withPanelOffset(10)
                .withArrowAnchor(new HyUIAnchor()
                        .setWidth(11)
                        .setHeight(7)
                        .setRight(3)
                        .setBottom(3));
    }

    public static HyUIPatchStyle defaultTooltipBackground() {
        // TODO: Verify images in client are available on server to send as set events...
        //  Should be OK.
        return patchWithBorder("InGame/Tooltips/ItemTooltipDefault.png", 24);
    }

    public static TextTooltipStyle buttonTextTooltipStyle() {
        return new TextTooltipStyle()
                .withBackground(defaultTooltipBackground())
                .withMaxWidth(400)
                .withLabelStyle(new HyUIStyle()
                        .setWrap(true)
                        .setFontSize(16))
                .withPadding(24);
    }

    public static TabNavigationStyle iconOnlyTopTabsStyle() {
        TabStyle tabStyle = new TabStyle()
                .withDefault(iconOnlyTopTabState(-14, "../Custom/Pages/Assets/TabOverlay.png"))
                .withHovered(iconOnlyTopTabState(-5, "../Custom/Pages/Assets/TabOverlay.png"))
                .withPressed(iconOnlyTopTabState(-8, "../Custom/Pages/Assets/TabOverlay.png"));

        TabStyle selectedStyle = new TabStyle()
                .withDefault(iconOnlyTopTabState(0, "../Custom/Pages/Assets/TabSelectedOverlay.png"));

        return new TabNavigationStyle()
                .withTabStyle(tabStyle)
                .withSelectedTabStyle(selectedStyle)
                .withTabSounds(defaultTabNavigationSounds());
    }

    public static TabNavigationStyle textTopTabsStyle() {
        TabStyle tabStyle = new TabStyle()
                .withDefault(textTopTabState(-14, "Pages/Assets/TabSelectedOverlay.png", null))
                .withHovered(textTopTabState(-5, "Pages/Assets/TabSelectedOverlay.png", null))
                .withPressed(textTopTabState(-8, "Pages/Assets/TabSelectedOverlay.png", null));

        HyUIStyle selectedLabelStyle = new HyUIStyle()
                .setFontSize(19)
                .setTextColor("#f0de7bff")
                .setRenderBold(true);

        TabStyle selectedStyle = new TabStyle()
                .withDefault(textTopTabState(0, "Pages/Assets/TabSelectedOverlay.png", selectedLabelStyle));

        return new TabNavigationStyle()
                .withTabStyle(tabStyle)
                .withSelectedTabStyle(selectedStyle)
                .withTabSounds(defaultTabNavigationSounds());
    }

    public static TabNavigationStyle headerTabsStyle() {
        TabStyle tabStyle = new TabStyle()
                .withDefault(headerTabState(0.4f, null, null))
                .withHovered(headerTabState(0.6f, null, null))
                .withPressed(headerTabState(0.8f, null, null));

        TabStyle selectedStyle = new TabStyle()
                .withDefault(headerTabState(1f, patchWithBorder("Pages/Assets/HeaderTabSelectedBackground.png", 7), 
                        "Pages/Assets/SelectedIconGradient.png"));

        return new TabNavigationStyle()
                .withTabStyle(tabStyle)
                .withSelectedTabStyle(selectedStyle)
                .withTabSounds(creativeTabSounds());
    }

    private static TabStateStyle iconOnlyTopTabState(int bottom, String overlayTexture) {
        return new TabStateStyle()
                .withBackground(new HyUIPatchStyle().setTexturePath("Pages/Assets/Tab.png"))
                .withOverlay(new HyUIPatchStyle().setTexturePath(overlayTexture))
                .withIconAnchor(new HyUIAnchor().setWidth(44).setHeight(44))
                .withLabelStyle(new HyUIStyle().setFontSize(19))
                .withAnchor(new HyUIAnchor().setWidth(82).setHeight(62).setRight(5).setBottom(bottom));
    }

    private static TabStateStyle textTopTabState(int bottom, String overlayTexture, HyUIStyle labelStyle) {
        HyUIStyle resolvedLabelStyle = labelStyle != null ? labelStyle : new HyUIStyle().setFontSize(19);
        return new TabStateStyle()
                .withBackground(new HyUIPatchStyle().setTexturePath("Pages/Assets/Tab.png").setBorder(3))
                .withOverlay(new HyUIPatchStyle().setTexturePath(overlayTexture))
                .withIconAnchor(new HyUIAnchor().setWidth(44).setHeight(44))
                .withLabelStyle(resolvedLabelStyle)
                .withPadding(new HyUIPadding().setLeft(20).setRight(20).setTop(14))
                .withAnchor(new HyUIAnchor().setHeight(62).setRight(5).setBottom(bottom));
    }

    private static TabStateStyle headerTabState(float iconOpacity, HyUIPatchStyle background, String contentMaskTexturePath) {
        TabStateStyle state = new TabStateStyle()
                .withAnchor(new HyUIAnchor().setWidth(37).setHeight(30).setLeft(4))
                .withIconAnchor(new HyUIAnchor().setWidth(22).setHeight(22))
                .withPadding(new HyUIPadding().setHorizontal(8))
                //.withTooltipStyle(buttonTextTooltipStyle())
                .withIconOpacity(iconOpacity);
        if (background != null) {
            state.withBackground(background);
        }
        if (contentMaskTexturePath != null) {
            state.withContentMaskTexturePath(contentMaskTexturePath);
        }
        return state;
    }

    private static ButtonSounds defaultTabNavigationSounds() {
        return (ButtonSounds) new ButtonSounds()
                .withActivate(new SoundStyle()
                        .withSoundPath("Sounds/DefaultTabActivate.ogg")
                        .withVolume(-2)
                        .withMinPitch(-0.2f)
                        .withMaxPitch(0.2f))
                .withMouseHover(new SoundStyle()
                        .withSoundPath("Sounds/ButtonsLightHover.ogg")
                        .withVolume(6));
    }

    private static ButtonSounds creativeTabSounds() {
        return (ButtonSounds) new ButtonSounds()
                .withActivate(new SoundStyle()
                        .withSoundPath("Sounds/TickActivate.ogg")
                        .withVolume(6));
    }

    private static HyUIPatchStyle patchWithBorder(String texturePath, int border) {
        return new HyUIPatchStyle()
                .setTexturePath(texturePath)
                .setBorder(border);
    }

    private static HyUIPatchStyle patchWithVerticalHorizontalBorder(String texturePath, int verticalBorder, int horizontalBorder) {
        return new HyUIPatchStyle()
                .setTexturePath(texturePath)
                .setVerticalBorder(verticalBorder)
                .setHorizontalBorder(horizontalBorder);
    }
}
