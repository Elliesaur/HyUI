## ActionButtonAlignment

### Fields

- Left
- Right

## CodeEditorLanguage

### Fields

- Text
- Json

## ColorFormat

### Fields

- Rgb
- Rgba
- RgbShort

## DropdownBoxAlign

### Fields

- Top
- Bottom
- Left
- Right

## InputFieldButtonSide

### Fields

- Left
- Right

## InputFieldIconSide

### Fields

- Left
- Right

## ItemGridInfoDisplayMode

### Fields

- Tooltip
- Adjacent
- None

## LabelAlignment

### Fields

- Start
- Center
- End

## LayoutMode

### Fields

- Full
- Left
- Center
- Right
- Top
- Middle
- Bottom
- LeftScrolling
- RightScrolling
- TopScrolling
- BottomScrolling
- CenterMiddle
- MiddleCenter
- LeftCenterWrap

## MouseWheelScrollBehaviourType

### Fields

- Default
- VerticalOnly
- HorizontalOnly

## ProgressBarAlignment

### Fields

- Vertical
- Horizontal

## ProgressBarDirection

### Fields

- Start
- End

## ResizeType

### Fields

- None
- Start
- End

## TimerDirection

### Fields

- CountDown
- CountUp

## TooltipAlignment

### Fields

- TopLeft
- TopRight
- BottomLeft
- BottomRight

## Anchor

### Fields

- Left: Integer
- Right: Integer
- Top: Integer
- Bottom: Integer
- Height: Integer
- Full: Integer
- Horizontal: Integer
- Vertical: Integer
- Width: Integer
- MinWidth: Integer
- MaxWidth: Integer

## BlockSelectorStyle

### Fields

- ItemGridStyle: ItemGridStyle
- SlotDropIcon: PatchStyle / String
- SlotDeleteIcon: PatchStyle / String
- SlotHoverOverlay: PatchStyle / String

## ButtonSounds

### Fields

- Activate: SoundStyle
- Context: SoundStyle
- MouseHover: SoundStyle

## ButtonStyle

### Fields

- Default: ButtonStyleState
- Hovered: ButtonStyleState
- Pressed: ButtonStyleState
- Disabled: ButtonStyleState
- Sounds: ButtonSounds

## ButtonStyleState

### Fields

- Background: PatchStyle / String

## CheckBoxStyle

### Fields

- Checked: CheckBoxStyleState
- Unchecked: CheckBoxStyleState

## CheckBoxStyleState

### Fields

- DefaultBackground: PatchStyle / String
- HoveredBackground: PatchStyle / String
- PressedBackground: PatchStyle / String
- DisabledBackground: PatchStyle / String
- ChangedSound: SoundStyle
- HoveredSound: SoundStyle

## ClientItemStack

### Fields

- Id: String
- Quantity: Integer
- Durability: Double
- MaxDurability: Double
- OverrideDroppedItemAnimation: Boolean
- Metadata: ClientItemMetadata

## ColorOptionGridStyle

### Fields

- OptionSize: Integer
- OptionSpacingHorizontal: Integer
- OptionSpacingVertical: Integer
- HighlightSize: Integer
- HighlightOffsetLeft: Integer
- HighlightOffsetTop: Integer
- HighlightBackground: PatchStyle / String
- MaskTexturePath: UI Path (String)
- FrameBackground: PatchStyle / String
- Sounds: ButtonSounds

## ColorPickerDropdownBoxStateBackground

### Fields

- Default: PatchStyle / String
- Hovered: PatchStyle / String
- Pressed: PatchStyle / String

## ColorPickerDropdownBoxStyle

### Fields

- Background: ColorPickerDropdownBoxStateBackground
- ColorPickerStyle: ColorPickerStyle
- Overlay: ColorPickerDropdownBoxStateBackground
- ArrowBackground: ColorPickerDropdownBoxStateBackground
- ArrowAnchor: Anchor
- Sounds: ButtonSounds
- PanelBackground: PatchStyle / String
- PanelWidth: Integer
- PanelHeight: Integer
- PanelPadding: Padding
- PanelOffset: Integer

## ColorPickerStyle

### Fields

- ButtonBackground: PatchStyle / String
- ButtonFill: PatchStyle / String
- OpacitySelectorBackground: PatchStyle / String
- TextFieldDecoration: InputFieldDecorationStyle
- TextFieldInputStyle: InputFieldStyle
- TextFieldPadding: Padding
- TextFieldHeight: Integer

## DropdownBoxSearchInputStyle

### Fields

- Background: PatchStyle / String
- Icon: InputFieldIcon
- Style: InputFieldStyle
- PlaceholderStyle: InputFieldStyle
- Anchor: Anchor
- Padding: Padding
- PlaceholderText: String
- ClearButtonStyle: InputFieldButtonStyle

## DropdownBoxSounds

### Fields

- Activate: SoundStyle
- MouseHover: SoundStyle
- Close: SoundStyle

## DropdownBoxStyle

### Fields

- DefaultBackground: PatchStyle / String
- HoveredBackground: PatchStyle / String
- PressedBackground: PatchStyle / String
- DisabledBackground: PatchStyle / String
- IconTexturePath: UI Path (String)
- IconWidth: Integer
- IconHeight: Integer
- DefaultArrowTexturePath: UI Path (String)
- HoveredArrowTexturePath: UI Path (String)
- PressedArrowTexturePath: UI Path (String)
- DisabledArrowTexturePath: UI Path (String)
- ArrowWidth: Integer
- ArrowHeight: Integer
- HorizontalPadding: Integer
- LabelStyle: LabelStyle
- DisabledLabelStyle: LabelStyle
- Sounds: DropdownBoxSounds
- PanelTitleLabelStyle: LabelStyle
- EntryHeight: Integer
- EntriesInViewport: Integer
- HorizontalEntryPadding: Integer
- EntryIconHeight: Integer
- EntryIconWidth: Integer
- EntryIconBackground: PatchStyle / String
- SelectedEntryIconBackground: PatchStyle / String
- EntryLabelStyle: LabelStyle
- SelectedEntryLabelStyle: LabelStyle
- NoItemsLabelStyle: LabelStyle
- HoveredEntryBackground: PatchStyle / String
- PressedEntryBackground: PatchStyle / String
- FocusOutlineSize: Integer
- FocusOutlineColor: Color
- EntrySounds: ButtonSounds
- PanelScrollbarStyle: ScrollbarStyle
- PanelBackground: PatchStyle / String
- PanelPadding: Integer
- PanelAlign: DropdownBoxAlign
- PanelOffset: Integer
- PanelWidth: Integer
- SearchInputStyle: DropdownBoxSearchInputStyle

## InputFieldButtonStyle

### Fields

- Texture: PatchStyle / String
- HoveredTexture: PatchStyle / String
- PressedTexture: PatchStyle / String
- Width: Integer
- Height: Integer
- Offset: Integer
- Side: InputFieldButtonSide

## InputFieldDecorationStyle

### Fields

- Default: InputFieldDecorationStyleState
- Focused: InputFieldDecorationStyleState

## InputFieldDecorationStyleState

### Fields

- OutlineSize: Integer
- Background: PatchStyle / String
- OutlineColor: Color
- Icon: InputFieldIcon
- ClearButtonStyle: InputFieldButtonStyle
- ToggleVisibilityButtonStyle: InputFieldButtonStyle

## InputFieldIcon

### Fields

- Texture: PatchStyle / String
- Width: Integer
- Height: Integer
- Offset: Integer
- Side: InputFieldIconSide

## InputFieldStyle

### Fields

- FontName: Font Name (String)
- FontSize: Float
- TextColor: Color
- RenderUppercase: Boolean
- RenderBold: Boolean
- RenderItalics: Boolean

## ItemGridSlot

### Fields

- ItemStack: ClientItemStack
- Background: PatchStyle / String
- ExtraOverlays: List<PatchStyle>
- Overlay: PatchStyle / String
- Icon: PatchStyle / String
- IsItemIncompatible: Boolean
- Name: String
- Description: String
- InventorySlotIndex: Integer
- SkipItemQualityBackground: Boolean
- IsActivatable: Boolean
- IsItemUncraftable: Boolean

## ItemGridStyle

### Fields

- SlotSize: Integer
- SlotIconSize: Integer
- SlotSpacing: Integer
- DurabilityBarBackground: PatchStyle / String
- DurabilityBar: UI Path (String)
- DurabilityBarAnchor: Anchor
- DurabilityBarColorStart: Color
- DurabilityBarColorEnd: Color
- CursedIconPatch: PatchStyle / String
- CursedIconAnchor: Anchor
- SlotBackground: PatchStyle / String
- QuantityPopupSlotOverlay: PatchStyle / String
- BrokenSlotBackgroundOverlay: PatchStyle / String
- BrokenSlotIconOverlay: PatchStyle / String
- DefaultItemIcon: PatchStyle / String
- ItemStackActivateSound: SoundStyle
- ItemStackHoveredSound: SoundStyle

## LabeledCheckBoxStyle

### Fields

- Checked: LabeledCheckBoxStyleState
- Unchecked: LabeledCheckBoxStyleState

## LabeledCheckBoxStyleState

### Fields

- DefaultLabelStyle: LabelStyle
- HoveredLabelStyle: LabelStyle
- PressedLabelStyle: LabelStyle
- DisabledLabelStyle: LabelStyle
- Text: String
- DefaultBackground: PatchStyle / String
- HoveredBackground: PatchStyle / String
- PressedBackground: PatchStyle / String
- DisabledBackground: PatchStyle / String
- ChangedSound: SoundStyle
- HoveredSound: SoundStyle

## LabelSpan

### Fields

- Text: String
- IsUppercase: Boolean
- IsBold: Boolean
- IsItalics: Boolean
- IsMonospace: Boolean
- IsUnderlined: Boolean
- Color: Color
- OutlineColor: Color
- Link: String
- Params: Dictionary`2

## LabelStyle

### Fields

- HorizontalAlignment: LabelAlignment
- VerticalAlignment: LabelAlignment
- Wrap: Boolean
- FontName: Font Name (String)
- FontSize: Float
- TextColor: Color
- OutlineColor: Color
- LetterSpacing: Float
- RenderUppercase: Boolean
- RenderBold: Boolean
- RenderItalics: Boolean
- RenderUnderlined: Boolean
- Alignment: LabelAlignment

## NumberFieldFormat

### Fields

- MaxDecimalPlaces: Integer
- Step: Decimal
- MinValue: Decimal
- MaxValue: Decimal
- DefaultValue: Decimal
- Suffix: String

## Padding

### Fields

- Left: Integer
- Right: Integer
- Top: Integer
- Bottom: Integer
- Full: Integer
- Horizontal: Integer
- Vertical: Integer

## PatchStyle

### Fields

- Area: Padding
- Color: Color
- Anchor: Anchor
- HorizontalBorder: Integer
- VerticalBorder: Integer
- TexturePath: UI Path (String)
- Border: Integer

## PopupStyle

### Fields

- Background: PatchStyle / String
- Padding: Padding
- Width: Integer
- ButtonPadding: Padding
- ButtonStyle: SubMenuItemStyle
- SelectedButtonStyle: SubMenuItemStyle
- TooltipStyle: TextTooltipStyle

## ScrollbarStyle

### Fields

- Size: Integer
- Spacing: Integer
- OnlyVisibleWhenHovered: Boolean
- Background: PatchStyle / String
- Handle: PatchStyle / String
- HoveredHandle: PatchStyle / String
- DraggedHandle: PatchStyle / String

## SliderStyle

### Fields

- Background: PatchStyle / String
- Fill: PatchStyle / String
- Handle: PatchStyle / String
- HandleWidth: Integer
- HandleHeight: Integer
- Sounds: ButtonSounds

## SoundStyle

### Fields

- SoundPath: UI Path (String)
- Volume: Float
- MinPitch: Float
- MaxPitch: Float
- StopExistingPlayback: Boolean

## SpriteFrame

### Fields

- Width: Integer
- Height: Integer
- PerRow: Integer
- Count: Integer

## SubMenuItemStyle

### Fields

- Default: SubMenuItemStyleState
- Hovered: SubMenuItemStyleState
- Pressed: SubMenuItemStyleState
- Disabled: SubMenuItemStyleState
- Sounds: ButtonSounds

## SubMenuItemStyleState

### Fields

- Background: PatchStyle / String
- LabelStyle: LabelStyle
- BindingLabelStyle: LabelStyle
- LabelMaskTexturePath: UI Path (String)

## Tab

### Fields

- Id: String
- Icon: PatchStyle / String
- IconSelected: PatchStyle / String
- IconAnchor: Anchor
- Text: String
- TooltipText: String

## TabNavigationStyle

### Fields

- TabStyle: TabStyle
- SelectedTabStyle: TabStyle
- SeparatorAnchor: Anchor
- SeparatorBackground: PatchStyle / String
- TabSounds: ButtonSounds

## TabStyle

### Fields

- Default: TabStyleState
- Hovered: TabStyleState
- Pressed: TabStyleState

## TabStyleState

### Fields

- Background: PatchStyle / String
- Overlay: PatchStyle / String
- Anchor: Anchor
- Padding: Padding
- IconAnchor: Anchor
- IconOpacity: Float
- LabelStyle: LabelStyle
- FlexWeight: Integer
- ContentMaskTexturePath: UI Path (String)
- TooltipStyle: TextTooltipStyle

## TextButtonStyle

### Fields

- Default: TextButtonStyleState
- Hovered: TextButtonStyleState
- Pressed: TextButtonStyleState
- Disabled: TextButtonStyleState
- Sounds: ButtonSounds

## TextButtonStyleState

### Fields

- Background: PatchStyle / String
- LabelStyle: LabelStyle
- LabelMaskTexturePath: UI Path (String)

## TextTooltipStyle

### Fields

- LabelStyle: LabelStyle
- Padding: Padding
- Background: PatchStyle / String
- MaxWidth: Integer
- Alignment: TooltipAlignment

## ToggleButtonStyle

### Fields

- Default: ToggleButtonStyleState
- Hovered: ToggleButtonStyleState
- Pressed: ToggleButtonStyleState
- Disabled: ToggleButtonStyleState
- Sounds: ButtonSounds

## ToggleButtonStyleState

### Fields

- Background: PatchStyle / String
