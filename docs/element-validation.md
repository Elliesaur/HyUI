# Element Validation

This document is the property checklist we validate against for HyUI `.ui` files and builders.

We do not validate possible client-side elements, properties, or styles.

## Syntax Notes

- `$Var = "path/to/file.ui"` imports another UI file.
- `$Var.@TemplateName` references a template from an import.
- `@Name = "Value"` defines a named inline property; it can also be referenced as `$Var.@Name`.

## Restrictions with appendInline

- We cannot reference variables, or imported files easily.
- We can however reference variables within a `UICommandBuilder.set(...)` using `Value.ref(document, variableName)`

## Validation in HyUI

- We do not validate supplied .ui files when using `.fromFile()` (yet).
- We silently fail when validating HYUIML, making sure there are no crashes and exclude invalid properties/styles.

## Common Properties (All Elements)

| Property | Type | Notes |
| --- | --- | --- |
| `#Id` | String | Use `#Id` after the element type to assign an ID. |
| `Anchor` | Anchor | Positioning and sizing definition. |
| `FlexWeight` | Number | Flex layout weight. |
| `Padding` | Padding | Inner spacing definition. |
| `Background` | PatchStyle/String | PatchStyle object or style reference. |
| `Visible` | Boolean | Controls visibility. |
| `TooltipTextSpans` | Message | Tooltip text span data. |
| `HitTestVisible` | Boolean | Controls whether the element can receive input. |

## Common Objects

### Layout Mode
* TopScrolling,
* MiddleCenter,
* Left,
* Right,
* Full,
* Middle,
* Bottom,
* BottomScrolling,
* CenterMiddle,
* Top,
* LeftCenterWrap,
* RightCenterWrap,
* Center

### Anchor
* Left
* Right
* Top
* Bottom
* Width
* Height
* MinWidth
* MaxWidth
* Full
* Horizontal
* Vertical

## Elements

### Container
- `LayoutMode` (see Layout Modes)
- `ScrollbarStyle` (Value reference)
- `ClipChildren` (Boolean)
- `#Content` and `#Title` are `Group` elements and follow group rules.
- The common `@Title` must use `@Text = "Value"` and must be first in the properties list.
- NO STYLE.

### PageOverlay
- `LayoutMode` (see Layout Modes)
- `ScrollbarStyle` (Value reference)
- `ClipChildren` (Boolean)
- NO STYLE.

### Group
- `LayoutMode` (see Layout Modes)
- `ScrollbarStyle` (Value reference)
- `ClipChildren` (Boolean)
- NO STYLE.

### ColorPicker

- `Value` (String, hex)
- Style:  `PaddingLeft` (Integer), `PaddingRight` (Integer), `PaddingTop` (Integer), `PaddingBottom` (Integer), `FontSize` (Integer), `RenderBold` (Boolean), `RenderItalics` (Boolean), `RenderUppercase` (Boolean), `FontName` (String: Default or Secondary), `Wrap` (Boolean), `LetterSpacing` (Integer)
  - Or a style reference.

### Label

- `Text` (String/Message)
- Style: `PaddingLeft` (Integer), `PaddingRight` (Integer), `PaddingTop` (Integer), `PaddingBottom` (Integer), `Alignment` (Integer), `HorizontalAlignment` (Integer), `VerticalAlignment` (Integer), `FontSize` (Integer), `TextColor` (Color), `RenderBold` (Boolean), `RenderItalics` (Boolean), `RenderUppercase` (Boolean), `FontName` (String: Default or Secondary), `Wrap` (Boolean), `LetterSpacing` (Integer), `OutlineColor` (Color)
  - Or a style reference.

### TimerLabel

- `Seconds` (Number)
- Style: `PaddingLeft` (Integer), `PaddingRight` (Integer), `PaddingTop` (Integer), `PaddingBottom` (Integer), `Alignment` (Integer), `HorizontalAlignment` (Integer), `VerticalAlignment` (Integer), `FontSize` (Integer), `TextColor` (Color), `RenderBold` (Boolean), `RenderItalics` (Boolean), `RenderUppercase` (Boolean), `FontName` (String: Default or Secondary), `Wrap` (Boolean), `LetterSpacing` (Integer), `OutlineColor` (Color)
  - Or a style reference.

### TextButton

- `Text` (String, not `@Text`)
- `Disabled` (Boolean)
- Style: `PaddingLeft` (Integer), `PaddingRight` (Integer), `PaddingTop` (Integer), `PaddingBottom` (Integer)
  - A raw `Button` may use `TextColor`.
  - Or a style reference.
- `LayoutMode` (see Layout Modes)
- `Overscroll` (Boolean)

### NumberField

- `Value` (Number)
- `Format` (String)
- `MaxDecimalPlaces` (Integer)
- Style: Unknown but excludes `TextColor`

### TextField

- `Value` (String)
- `PlaceholderText` (String)
- `MaxLength` (Integer)
- `MaxVisibleLines` (Integer)
- `ReadOnly` (Boolean)
- `Password` (Boolean)
- `PasswordChar` (String)
- `AutoGrow` (Boolean)
- Style: Unknown but excludes `TextColor`

### AssetImage
- `LayoutMode` (see Layout Modes)
- `AssetPath` (String)
- NO STYLE.

### ItemIcon
- `ItemId` (String, example: `Tool_Pickaxe_Crude`)
- NO STYLE.

### ItemSlot
- `ShowQualityBackground` (Boolean)
- `ShowQuantity` (Boolean)
- `ItemId` (String, example: `Tool_Pickaxe_Crude`)
- NO STYLE.

### ItemGrid
- `LayoutMode` (String)
- `BackgroundMode` (String)
- `ScrollbarStyle` (Value reference)
- `RenderItemQualityBackground` (Boolean)
- `AreItemsDraggable` (Boolean)
- `KeepScrollPosition` (Boolean)
- `ShowScrollbar` (Boolean)
- `SlotsPerRow` (Integer)
- `Slots` (List<ItemGridSlot>)
- `Style` (HyUIStyle with custom properties below)
  - `SlotSize` (Integer)
  - `SlotIconSize` (Integer) - often the same as the slot size
  - `SlotSpacing` (Integer)
  - `SlotBackground` (String, example: `Common/BlockSelectorSlotBackground`)

### Sprite
- `TexturePath` (String)
- `Frame` (Frame, as `(Width: Int, Height: Int, PerRow: Int, Count: Int)`)
- `FramesPerSecond` (Integer)
- Styles: `PaddingLeft` (Integer), `PaddingRight` (Integer), `PaddingTop` (Integer), `PaddingBottom` (Integer)

### CheckBoxWithLabel

- `@Text` (String) must be first and must be present.
- Styles: `PaddingLeft` (Integer), `PaddingRight` (Integer), `PaddingTop` (Integer), `PaddingBottom` (Integer)
  - Not confirmed, but `TextColor` is not allowed.
