# Element Validation

This document describes the property validation rules for `.ui` files and HyUI builders. 
It mirrors the Hytale UI Parser `ElementType` and `TypeType` definitions, and includes HyUI-only notes where applicable.

```
MIT License

Copyright (c) 2026 UltraDev

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Syntax Notes

- `$Var = "path/to/file.ui"` imports another UI file.
- `$Var.@TemplateName` references a template from an import.
- `@Name = "Value"` defines a named inline property; it can also be referenced as `$Var.@Name`.

## Restrictions with appendInline

- We cannot reference variables or imported files easily.
- We can reference variables within a `UICommandBuilder.set(...)` using `Value.ref(document, variableName)`.

## Validation in HyUI

- We do not validate supplied `.ui` files when using `.fromFile()` (yet).
- We silently fail when validating HYUIML, ensuring no crashes and excluding invalid properties/styles.
- The elements listed under "Elements (Validated)" are validated by the Hytale UI Parser.
- Elements and properties under "HyUI-only" are validated, and are still be supported by HyUI.

## Common Properties (All Elements)

All validated elements accept the following properties in addition to their element-specific properties.

| Property | Type | Notes |
| --- | --- | --- |
| `#Id` | String | Use `#Id` after the element type to assign an ID. |
| `Anchor` | Anchor | Positioning and sizing definition. |
| `Padding` | Padding | Inner spacing definition. |
| `Background` | PatchStyle | Patch style object. |
| `FlexWeight` | Integer | Flex layout weight. |
| `HitTestVisible` | Boolean | Controls whether the element can receive input. |
| `Visible` | Boolean | Controls visibility. |
| `LayoutMode` | LayoutMode | Layout mode for the element. |
| `TooltipText` | String | Tooltip text for the element. |
| `TextTooltipStyle` | TextTooltipStyle | Tooltip style definition. |
| `TooltipTextSpans` | String | Tooltip text span data. |

## Shared Button Properties

| Property | Type | Notes |
| --- | --- | --- |
| `Disabled` | Boolean | Disabled state for buttons. |

## Type Types

### Primitive Types

- `String`
- `Integer`
- `Boolean`
- `Float`
- `Double`
- `Color`

### Enum Types

- `LayoutMode`: `TopScrolling`, `MiddleCenter`, `Left`, `Right`, `Full`, `Middle`, `Bottom`, `BottomScrolling`, `CenterMiddle`, `Top`, `LeftCenterWrap`, `Center`
- `BarAlignment`: `Vertical`, `Horizontal`
- `Alignment`: `Top`, `Bottom`, `Left`, `Right`, `TopLeft`
- `Direction`: `Center`, `Start`, `End`
- `InfoDisplay`: `None`
- `Side`: `Left`, `Right`
- `ColorPickerFormat`: `Rgb`

### Object Types

- `Sound`
  - `SoundPath`: String
  - `MinPitch`: Float
  - `MaxPitch`: Float
  - `Volume`: Float
- `SoundsStyle`
  - `Activate`: Sound
  - `MouseHover`: Sound
  - `Close`: Sound
  - `Context`: Sound
- `Anchor`
  - `Left`: Float
  - `Right`: Float
  - `Top`: Float
  - `Bottom`: Float
  - `Width`: Float
  - `Height`: Float
  - `MinWidth`: Float
  - `MaxWidth`: Float
  - `Full`: Float
  - `Horizontal`: Float
  - `Vertical`: Float
- `Padding`
  - `Left`: Integer
  - `Right`: Integer
  - `Top`: Integer
  - `Bottom`: Integer
  - `Horizontal`: Integer
  - `Vertical`: Integer
  - `Full`: Integer
- `LabelStyle`
  - `FontSize`: Integer
  - `FontName`: String
  - `LetterSpacing`: Float
  - `TextColor`: Color
  - `RenderBold`: Boolean
  - `RenderUppercase`: Boolean
  - `RenderItalics`: Boolean
  - `Alignment`: Direction
  - `HorizontalAlignment`: Direction
  - `VerticalAlignment`: Direction
  - `OutlineColor`: Color
  - `Wrap`: Boolean
- `PatchStyle`
  - `TexturePath`: String
  - `VerticalBorder`: Integer
  - `HorizontalBorder`: Integer
  - `Border`: Integer
  - `Color`: Color
  - `LabelStyle`: LabelStyle
  - `Anchor`: Anchor
- `ScrollbarStyle`
  - `OnlyVisibleWhenHovered`: Boolean
  - `Spacing`: Integer
  - `Size`: Integer
  - `Background`: PatchStyle
  - `Handle`: PatchStyle
  - `HoveredHandle`: PatchStyle
  - `DraggedHandle`: PatchStyle
- `CheckedStyleInnerElement`
  - `DefaultBackground`: PatchStyle
  - `HoveredBackground`: PatchStyle
  - `PressedBackground`: PatchStyle
  - `DisabledBackground`: PatchStyle
  - `ChangedSound`: Sound
- `CheckBoxStyle`
  - `Checked`: CheckedStyleInnerElement
  - `Unchecked`: CheckedStyleInnerElement
- `CheckedStyle`
  - `Default`: CheckBoxStyle
  - `Hovered`: CheckBoxStyle
  - `Pressed`: CheckBoxStyle
  - `Sounds`: SoundsStyle
- `ButtonStyleElement`
  - `Background`: PatchStyle
  - `LabelStyle`: LabelStyle
  - `BindingLabelStyle`: LabelStyle
- `ButtonStyle`
  - `Default`: ButtonStyleElement
  - `Hovered`: ButtonStyleElement
  - `Pressed`: ButtonStyleElement
  - `Disabled`: ButtonStyleElement
  - `Sounds`: SoundsStyle
- `SliderStyle`
  - `Background`: PatchStyle
  - `Handle`: String
  - `HandleWidth`: Integer
  - `HandleHeight`: Integer
  - `Sounds`: SoundsStyle
- `InputFieldStyle`
  - `TextColor`: Color
  - `FontSize`: Integer
  - `RenderBold`: Boolean
  - `RenderItalics`: Boolean
  - `RenderUppercase`: Boolean
- `TextTooltipStyle`
  - `Background`: PatchStyle
  - `MaxWidth`: Integer
  - `LabelStyle`: LabelStyle
  - `Padding`: Integer
  - `Alignment`: Alignment
- `Icon`
  - `Texture`: PatchStyle
  - `Width`: Integer
  - `Height`: Integer
  - `Offset`: Integer
  - `HoveredTexture`: PatchStyle
  - `PressedTexture`: PatchStyle
  - `Side`: Side
- `TextFieldDecorationElement`
  - `Background`: PatchStyle
  - `Icon`: Icon
  - `ClearButtonStyle`: Icon
- `TextFieldDecoration`
  - `Default`: TextFieldDecorationElement
- `ColorPickerStyle`
  - `OpacitySelectorBackground`: String
  - `ButtonBackground`: String
  - `ButtonFill`: String
  - `TextFieldDecoration`: TextFieldDecoration
  - `TextFieldPadding`: Padding
  - `TextFieldHeight`: Integer
- `ColorPickerDropdownBoxBackgroundThing`
  - `Default`: String
- `ColorPickerDropdownBoxStyle`
  - `ColorPickerStyle`: ColorPickerStyle
  - `Background`: ColorPickerDropdownBoxBackgroundThing
  - `ArrowBackground`: ColorPickerDropdownBoxBackgroundThing
  - `Overlay`: ColorPickerDropdownBoxBackgroundThing
  - `PanelBackground`: PatchStyle
  - `PanelPadding`: Padding
  - `PanelOffset`: Integer
  - `ArrowAnchor`: Anchor
- `SpriteFrame`
  - `Width`: Integer
  - `Height`: Integer
  - `PerRow`: Integer
  - `Count`: Integer
- `NumberFieldFormat`
  - `MaxDecimalPlaces`: Integer
  - `Step`: Float
  - `MinValue`: Float
  - `MaxValue`: Float
- `ItemGridStyle`
  - `SlotSize`: Integer
  - `SlotIconSize`: Integer
  - `SlotSpacing`: Integer
  - `SlotBackground`: PatchStyle
  - `QuantityPopupSlotOverlay`: String
  - `BrokenSlotBackgroundOverlay`: String
  - `BrokenSlotIconOverlay`: String
  - `DefaultItemIcon`: String
  - `DurabilityBar`: String
  - `DurabilityBarBackground`: String
  - `DurabilityBarAnchor`: Anchor
  - `DurabilityBarColorStart`: Color
  - `DurabilityBarColorEnd`: Color
  - `CursedIconPatch`: PatchStyle
  - `CursedIconAnchor`: Anchor
  - `ItemStackHoveredSound`: Sound
  - `ItemStackActivateSound`: Sound
- `TabNavigationStyleState`
  - `LabelStyle`: LabelStyle
  - `Padding`: Padding
  - `Background`: PatchStyle
  - `Overlay`: PatchStyle
  - `IconAnchor`: Anchor
  - `Anchor`: Anchor
  - `TooltipStyle`: TextTooltipStyle
  - `IconOpacity`: Float
  - `ContentMaskTexturePath`: String
- `TabNavigationStyleElement`
  - `Default`: TabNavigationStyleState
  - `Hovered`: TabNavigationStyleState
  - `Pressed`: TabNavigationStyleState
- `TabNavigationStyle`
  - `TabStyle`: TabNavigationStyleElement
  - `SelectedTabStyle`: TabNavigationStyleElement
  - `TabSounds`: SoundsStyle
- `DropdownBoxSounds`
  - `Activate`: Sound
  - `MouseHover`: Sound
  - `Close`: Sound
- `DropdownBoxStyle`
  - `DefaultBackground`: PatchStyle
  - `HoveredBackground`: PatchStyle
  - `PressedBackground`: PatchStyle
  - `DefaultArrowTexturePath`: String
  - `HoveredArrowTexturePath`: String
  - `PressedArrowTexturePath`: String
  - `ArrowWidth`: Integer
  - `ArrowHeight`: Integer
  - `LabelStyle`: LabelStyle
  - `EntryLabelStyle`: LabelStyle
  - `NoItemsLabelStyle`: LabelStyle
  - `SelectedEntryLabelStyle`: LabelStyle
  - `HorizontalPadding`: Integer
  - `PanelScrollbarStyle`: ScrollbarStyle
  - `PanelBackground`: PatchStyle
  - `PanelPadding`: Padding
  - `PanelWidth`: Integer
  - `PanelAlign`: Alignment
  - `PanelOffset`: Integer
  - `EntryHeight`: Integer
  - `EntryIconWidth`: Integer
  - `EntryIconHeight`: Integer
  - `EntriesInViewport`: Integer
  - `HorizontalEntryPadding`: Padding
  - `HoveredEntryBackground`: PatchStyle
  - `PressedEntryBackground`: PatchStyle
  - `Sounds`: DropdownBoxSounds
  - `EntrySounds`: SoundsStyle
  - `FocusOutlineSize`: Integer
  - `FocusOutlineColor`: Color
  - `PanelTitleLabelStyle`: LabelStyle
  - `SelectedEntryIconBackground`: PatchStyle
  - `IconTexturePath`: String
  - `IconWidth`: Integer
  - `IconHeight`: Integer
- `PopupStyle`
  - `Background`: Color
  - `ButtonPadding`: Padding
  - `Padding`: Padding
  - `TooltipStyle`: TextTooltipStyle
  - `ButtonStyle`: ButtonStyle
  - `SelectedButtonStyle`: ButtonStyle
- `MenuItemStyle`
  - `Default`: PatchStyle
  - `Hovered`: PatchStyle
- `BlockSelectorStyle`
  - `ItemGridStyle`: ItemGridStyle
  - `SlotDropIcon`: PatchStyle
  - `SlotDeleteIcon`: PatchStyle
  - `SlotHoverOverlay`: PatchStyle
- `LabeledCheckBoxStyleElement`
  - `DefaultBackground`: PatchStyle
  - `HoveredBackground`: PatchStyle
  - `PressedBackground`: PatchStyle
  - `Text`: String
  - `DefaultLabelStyle`: LabelStyle
  - `HoveredLabelStyle`: LabelStyle
  - `PressedLabelStyle`: LabelStyle
  - `ChangedSound`: Sound
- `LabeledCheckBoxStyle`
  - `Checked`: LabeledCheckBoxStyleElement
  - `Unchecked`: LabeledCheckBoxStyleElement

## Elements (Validated)

Only the element-specific properties are listed below. All validated elements also accept the Common Properties table above.

### Group

- `ScrollbarStyle`: ScrollbarStyle
- `MaskTexturePath`: String
- `AutoScrollDown`: Boolean
- `KeepScrollPosition`: Boolean
-  Untested `ClipChildren`: Boolean

### TimerLabel

- `Style`: LabelStyle
- `Seconds`: Integer

### Label

- `Style`: LabelStyle
- `Text`: String
- `MaskTexturePath`: String
- `TextSpans`: String
- HyUI-only: `Text` may be a Message value.

### TextButton

- `Disabled`: Boolean
- `Text`: String
- `MaskTexturePath`: String
- `Style`: ButtonStyle
- Untested `Overscroll`: Boolean

### Button

- `Disabled`: Boolean
- `MaskTexturePath`: String
- `Style`: ButtonStyle

### CheckBox

- `Style`: CheckBoxStyle
- `Value`: Boolean

### TextField

- `Style`: InputFieldStyle
- `PlaceholderStyle`: InputFieldStyle
- `PlaceholderText`: String
- `MaxLength`: Integer
- `IsReadOnly`: Boolean
- `PasswordChar`: String
- `Decoration`: TextFieldDecoration
- Legacy/HyUI-only: `Value` (String), `MaxVisibleLines` (Integer), `ReadOnly` (Boolean), `Password` (Boolean), `AutoGrow` (Boolean)

### NumberField

- `Format`: NumberFieldFormat
- `Value`: Double
- `Min`: Double
- `Max`: Double
- `Step`: Double
- `Style`: InputFieldStyle
- `PlaceholderStyle`: InputFieldStyle

### DropdownBox

- `Style`: DropdownBoxStyle
- `NoItemsText`: String
- `PanelTitleText`: String
- `MaxSelection`: Integer
- `ShowLabel`: Boolean

### Sprite

- `TexturePath`: String
- `Frame`: SpriteFrame
- `FramesPerSecond`: Integer

### CompactTextField

- TODO

### BackButton

- No element-specific properties.

### FloatSlider

- `Style`: SliderStyle
- `Min`: Float
- `Max`: Float
- `Step`: Float
- `Value`: Float

### MultilineTextField

- `Style`: InputFieldStyle
- `PlaceholderStyle`: InputFieldStyle
- `PlaceholderText`: String
- `MaxVisibleLines`: Integer
- `MaxLength`: Integer
- `AutoGrow`: Boolean
- `ContentPadding`: Padding

### ColorPickerDropdownBox

- `Style`: ColorPickerDropdownBoxStyle
- `Format`: ColorPickerFormat
- `DisplayTextField`: Boolean

### CircularProgressBar

- `Value`: Float
- `MaskTexturePath`: String
- `Color`: Color

### ProgressBar

- `Value`: Float
- `Bar`: PatchStyle
- `BarTexturePath`: String
- `EffectTexturePath`: String
- `EffectWidth`: Integer
- `EffectHeight`: Integer
- `EffectOffset`: Integer
- `Direction`: Direction
- `Alignment`: BarAlignment

### Slider

- `Style`: SliderStyle
- `Min`: Integer
- `Max`: Integer
- `Step`: Float
- `Value`: Integer

### ItemSlotButton

- `Style`: ButtonStyle
- `ItemId`: String

### ItemSlot

- `ShowQualityBackground`: Boolean
- `ShowQuantity`: Boolean
- `ItemId`: String

### AssetImage

- `LayoutMode`: LayoutMode
- `AssetPath`: String

### SceneBlur

- No element-specific properties. This element does not allow children.

### ItemGrid

- `Style`: ItemGridStyle
- `SlotsPerRow`: Integer
- `RenderItemQualityBackground`: Boolean
- `AreItemsDraggable`: Boolean
- `KeepScrollPosition`: Boolean
- `ShowScrollbar`: Boolean
- `ScrollbarStyle`: ScrollbarStyle
- `InfoDisplay`: InfoDisplay
- Legacy/HyUI-only: `BackgroundMode` (String), `Slots` (List<ItemGridSlot>)

### ItemIcon

- `ItemId`: String

### ColorPicker

- `Style`: ColorPickerStyle
- `Format`: ColorPickerFormat
- Legacy/HyUI-only: `Value` (String, hex)

### BackgroundImage

- `Image`: String
- `ImageUW`: String

### TabNavigation

- `Style`: TabNavigationStyle
- `SelectedTab`: String
- `AllowUnselection`: Boolean

### ToggleButton

- `Style`: ButtonStyle
- `CheckedStyle`: ButtonStyle

### ItemPreviewComponent

- `ItemScale`: Float

### CharacterPreviewComponent

- No element-specific properties.

### SliderNumberField

- `SliderStyle`: SliderStyle
- `Style`: InputFieldStyle
- `NumberFieldContainerAnchor`: Anchor
- `NumberFieldStyle`: InputFieldStyle
- `Min`: Double
- `Max`: Double
- `Step`: Double
- `Value`: Double

### BlockSelector

- `Capacity`: Integer
- `Style`: BlockSelectorStyle

### ReorderableListGrip

- No element-specific properties.

### TabButton

- `Id`: String
- `Icon`: String
- `IconSelected`: String

### FloatSliderNumberField

- `SliderStyle`: SliderStyle
- `Style`: InputFieldStyle
- `NumberFieldContainerAnchor`: Anchor
- `NumberFieldStyle`: InputFieldStyle
- `NumberFieldMaxDecimalPlaces`: Integer
- `Min`: Float
- `Max`: Float
- `Step`: Float
- `Value`: Float

### ActionButton

- `Disabled`: Boolean
- `KeyBindingLabel`: String
- `Alignment`: Alignment
- `ActionName`: String

### Panel

- TODO

### LabeledCheckBox

- `Style`: LabeledCheckBoxStyle

### PlayerPreviewComponent

- `Scale`: Float

### HotkeyLabel

- `InputBindingKey`: String
- `InputBindingKeyPrefix`: String

### MenuItem

- `Text`: String
- `TextTooltipStyle`: TextTooltipStyle
- `PopupStyle`: PopupStyle
- `Style`: MenuItemStyle
- `SelectedStyle`: MenuItemStyle
- `Icon`: PatchStyle
- `IconAnchor`: Anchor

## Common Elements from the Common.ui

### Container

- `LayoutMode`: LayoutMode
- `ScrollbarStyle`: ScrollbarStyle
- `ClipChildren`: Boolean
- `#Content` and `#Title` are `Group` elements and follow group rules.
- The common `@Title` must use `@Text = "Value"` and must be first in the properties list.

### PageOverlay

- `LayoutMode`: LayoutMode
- `ScrollbarStyle`: ScrollbarStyle
- `ClipChildren`: Boolean

### CheckBoxWithLabel

- `@Text`: String, must be first and must be present.
- Styles: `PaddingLeft`, `PaddingRight`, `PaddingTop`, `PaddingBottom` (Integer)
