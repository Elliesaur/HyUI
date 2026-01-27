### UI Elements

This page provides examples of common UI element combinations and configurations in HyUI.

### Dropdown Box Example

A `DropdownBox` allows players to select one or more options from a list.

#### HYUIML Example

```html
<select id="myDropdown" data-hyui-showlabel="true" value="Entry1">
    <option value="Entry1">First Entry</option>
    <option value="Entry2">Second Entry</option>
    <option value="Entry3" selected>Third Entry</option>
</select>
```
In this example, the third entry will be selected in the UI, this is due to the `selected` attribute being specified on an entry. If there is multiple `selected` items the last will be specified as the  value. The `selected` attribute overrides the `value` attribute on the `select` element.

> **Warning**: When setting the `value` attribute on a `<select>` tag, ensure it matches the `value` attribute of one of the `<option>` children.

#### Java Builder Example

```java
DropdownBoxBuilder.dropdownBox()
    .withId("myDropdown")
    .addEntry("Entry1", "First Entry")
    .addEntry("Entry2", "Second Entry")
    .withValue("Entry1") // Must match an entry name
    .addEventListener(CustomUIEventBindingType.ValueChanged, (val) -> {
        player.sendMessage(Message.raw("Selected: " + val));
    });
```

#### Dropdown Styling

The `DropdownBoxBuilder` supports additional secondary styles for detailed customization:

- `withEntryLabelStyle(HyUIStyle)`: Sets the style for the entry labels in the dropdown.
- `withSelectedEntryLabelStyle(HyUIStyle)`: Sets the style for the currently selected entry's label.
- `withPopupStyle(HyUIStyle)`: Sets the style for the popup menu container.

In HYUIML, these can be set via CSS:

```css
#myDropdown {
    hyui-entry-label-style: "Common.ui" "DefaultLabelStyle";
    hyui-selected-entry-label-style: "Common.ui" "SelectedLabelStyle";
    hyui-popup-style: "Common.ui" "DefaultPopupStyle";
}
```

> **Warning**: The value passed to `.withValue(String)` MUST exist within the entries added to the dropdown (via `.addEntry` or `.withEntries`). If it doesn't, the dropdown may fail to display correctly.

### Item Icon Button Example

It is often useful to combine a button with an item icon and labels to create interactive inventory-style elements. 
This example combines a button and item icon and labels within the button.

#### HYUIML Example

```html
<style>
    #IconButton {
        layout-mode: Left;
        padding: 6;
    }
    
    #Icon {
        anchor-width: 32;
        anchor-height: 32;
    }

    #ItemName {
        padding-left: 10;
        padding-right: 10;
        padding-top: 5;
        padding-bottom: 5;
        font-weight: bold;
        flex-weight: 1;
    }

    #ItemInfo {
        padding-left: 10;
        padding-right: 10;
        padding-top: 5;
        padding-bottom: 5;
        color: #ffffff;
    }
</style>

<button id="IconButton">
    <span id="Icon" class="item-icon" data-hyui-item-id="Tool_Pickaxe_Crude"></span>
    <p id="ItemName">Crude Pickaxe</p>
    <p id="ItemInfo">100/100</p>
</button>
```

#### Java Builder Example

```java
ButtonBuilder.textButton()
    .withId("IconButton")
    .withItemIcon(
        ItemIconBuilder.itemIcon()
            .withItemId("Tool_Pickaxe_Crude")
            .withAnchor(new HyUIAnchor().setWidth(32).setHeight(32))
    )
    .addChild(
        LabelBuilder.label()
            .withText("Crude Pickaxe")
            .withStyle(new HyUIStyle().setRenderBold(true))
    )
    .addChild(
        LabelBuilder.label()
            .withText("100/100")
    )
    .open(playerRef, store);
```

### Custom Button Example

Custom buttons let you override the background and (for text buttons) label styles on a per-state basis. This is the same pattern used in the HyUI showcase.

#### HYUIML Example

```html
<style>
    @ShowcaseHoveredLabel {
        font-weight: bold;
        color: #ffffff;
        font-size: 18;
    }
    @ShowcaseHoveredBackground {
        background-color: #0c0c0c;
    }
    @ShowcaseCustomBackground {
        background-image: url('Common/ShopTest.png');
        background-color: rgba(255, 0, 0, 0.25);
    }
</style>

<button class="custom-textbutton"
        data-hyui-default-label-style="@ShowcaseHoveredLabel"
        data-hyui-default-bg="@ShowcaseHoveredBackground"
        style="anchor-height: 30;">Custom Text</button>

<button class="custom-button"
        data-hyui-default-bg="@ShowcaseCustomBackground"
        style="anchor-width: 44; anchor-height: 44;"></button>
```

#### Notes

- `custom-textbutton` supports per-state label styling via `data-hyui-*-label-style` and background styling via `data-hyui-*-bg`.
- `custom-button` supports per-state background styling via `data-hyui-*-bg`.
- Each `data-hyui-*` style can be an inline declaration (`color: #fff; font-size: 18;`) or a `@Name` definition from a `<style>` block.
- For label styles, HyUI supports `color`, `font-size`, `font-weight`, `font-style`, `text-transform`, `letter-spacing`, `white-space`, `font-family`/`font-name`, outline color, and alignment.
- Use `data-hyui-disabled` or `data-hyui-overscroll` to control button state/behavior.

### Sprite Example

A `Sprite` displays an animated sequence of frames from a spritemap texture.

#### HYUIML Example

```html
<sprite src="Common/Spinner.png" 
        data-hyui-frame-width="32" 
        data-hyui-frame-height="32" 
        data-hyui-frame-per-row="8" 
        data-hyui-frame-count="72" 
        data-hyui-fps="30" 
        style="anchor-width: 32; anchor-height: 32;">
</sprite>
```

#### Java Builder Example

```java
SpriteBuilder.sprite()
    .withTexture("Common/Spinner.png")
    .withFrame(32, 32, 8, 72) // Width, Height, PerRow, Count
    .withFramesPerSecond(30)
    .withAnchor(new HyUIAnchor().setWidth(32).setHeight(32))
    .open(playerRef, store);
```

### Tab Navigation Example

Use `TabNavigationBuilder` for the tab bar and `TabContentBuilder` for tabbed content sections. Tab content is linked by tab ID and is auto-hidden unless selected, including across `updatePage()` rebuilds.

#### HYUIML Example

```html
<nav id="main-tabs" class="tabs"
     data-tabs="templates:Templates,timers:Timers,components:Components"
     data-selected="templates">
</nav>

<div id="templates-content" class="tab-content" data-hyui-tab-id="templates">
    <p>Template examples...</p>
</div>

<div id="timers-content" class="tab-content" data-hyui-tab-id="timers">
    <p>Timer examples...</p>
</div>
```

#### Java Builder Example

```java
TabNavigationBuilder tabs = TabNavigationBuilder.tabNavigation()
    .withId("main-tabs")
    .addTab("templates", "Templates")
    .addTab("timers", "Timers")
    .withSelectedTab("templates");

TabContentBuilder templates = TabContentBuilder.tabContent()
    .withId("templates-content")
    .withTabId("templates")
    .addChild(LabelBuilder.label().withText("Template examples..."));

TabContentBuilder timers = TabContentBuilder.tabContent()
    .withId("timers-content")
    .withTabId("timers")
    .addChild(LabelBuilder.label().withText("Timer examples..."));

PageBuilder.pageForPlayer(playerRef)
    .addElement(tabs)
    .addElement(templates)
    .addElement(timers)
    .open(store);
```

Note: `addElement(...)` only attaches elements to the root. To nest elements, use `.addChild(...)`
on the parent builder.

If you have multiple tab navs, set `data-hyui-tab-nav` (HYUIML) or `withTabNavigationId(...)` (Java) on the content to target a specific navigation ID.

### Dynamic Image Example

Dynamic images download a PNG at runtime and assign it to a dynamic image slot.

#### HYUIML Example

```html
<img id="player-head" class="dynamic-image" src="https://hyvatar.io/render/PlayerName" />
```

#### Java Builder Example

```java
DynamicImageBuilder.dynamicImage()
    .withId("player-head")
    .withImageUrl("https://hyvatar.io/render/PlayerName");
```

#### Update URL at Runtime

Use a text field + button and call `reloadImage(...)` to invalidate and re-download:

```java
builder.addEventListener("reload-button", CustomUIEventBindingType.Activating, (data, ctx) -> {
    ctx.getValue("image-url").ifPresent(value -> {
        String url = String.valueOf(value).trim();
        if (url.isBlank()) {
            return;
        }
        ctx.getById("player-head", DynamicImageBuilder.class)
                .ifPresent(img -> img.withImageUrl(url));
        ctx.getPage().ifPresent(page -> page.reloadImage("player-head"));
    });
});
```

Notes:
*   Dynamic images are limited to 10 per page, per player.
*   Downloaded PNGs are cached for 15 seconds.

### Hyvatar Image Example

Hyvatar images use the Hyvatar.io rendering service to stream player renders as dynamic images.

#### HYUIML Example

```html
<hyvatar id="player-head" username="PlayerName" render="head" size="256" rotate="45"></hyvatar>
```

#### Java Builder Example

```java
HyvatarImageBuilder.hyvatar()
    .withId("player-head")
    .withUsername("PlayerName")
    .withRenderType(HyvatarUtils.RenderType.HEAD)
    .withSize(256)
    .withRotate(45);
```

Notes:
*   Hyvatar images are dynamic images and follow the same slot limits and caching rules.
*   Thanks to Hyvatar.io for their fantastic work on the rendering service.

### Circular Progress Bar Example

A `CircularProgressBar` uses a mask texture and color to render a radial fill.

#### HYUIML Example

```html
<progress class="circular-progress"
          value="0.65"
          data-hyui-mask-texture-path="MaskTexture.png"
          data-hyui-color="#ffffff"
          style="anchor-width: 98; anchor-height: 98;">
</progress>
```

#### Java Builder Example

```java
ProgressBarBuilder.circularProgressBar()
    .withValue(0.65f)
    .withMaskTexturePath("MaskTexture.png")
    .withColor("#ffffff")
    .withAnchor(new HyUIAnchor().setWidth(98).setHeight(98));
```
