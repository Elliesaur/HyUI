### HYUIML (HyUI Markup Language)

HYUIML is an HTML-like markup language for defining Hytale UIs using a familiar syntax. It is parsed by HyUI and converted into the fluent builder API calls under the hood.

#### Basic Usage

You can load HYUIML directly into a `PageBuilder`:

```java
String html = """
    <div class="page-overlay">
        <div class="container" data-hyui-title="Hello">
            <div class="container-contents">
                <p>Hello from HYUIML!</p>
            </div>
        </div>
    </div>
    """;
PageBuilder.pageForPlayer(playerRef)
    .fromHtml(html)
    .open(store);
```

#### Supported Tags and Mappings

| HTML Tag                  | HyUI Builder | Notes                                                                                                                    |
|---------------------------| --- |--------------------------------------------------------------------------------------------------------------------------|
| `<div>`                   | `GroupBuilder` | Use for layout and containers.                                                                                           |
| `<div class="tab-content">` | `TabContentBuilder` | Tab content container linked to a tab ID.                                                                                |
| `<p>`                     | `LabelBuilder` | Standard text labels.                                                                                                    |
| `<label>`                 | `LabelBuilder` | Similar to `<p>`, often used for form field descriptions.                                                                |
| `<button>`                | `ButtonBuilder` | Standard buttons. Use `class="back-button"`, `class="secondary-button"`, or `class="tertiary-button"` for themed variants. |
| `<input type="text">`     | `TextFieldBuilder` | Text input fields. Requires a `value` to set to track values on events.                                                  |
| `<input type="password">` | `TextFieldBuilder` | Masked password input fields. Requires a `value` to set to track values on events.                                       |
| `<input type="number">`   | `NumberFieldBuilder` | Numeric input fields. Requires a `value` to set to track values on events.                                               |
| `<input type="range">`    | `SliderBuilder` | Sliders. Requires a `value` to set to track values on events.                                                            |
| `<input type="checkbox">` | `CheckBoxBuilder` | Toggle switches.                                                    |
| `<input type="color">`    | `ColorPickerBuilder` | Color selectors.                                                                                                         |
| `<input type="reset">`    | `ButtonBuilder` | Specifically creates a `CancelTextButton`.                                                                               |
| `<progress>`              | `ProgressBarBuilder` | Displays a progress bar. Add `class="circular-progress"` to render a CircularProgressBar.                                |
| `<span class="item-icon">` | `ItemIconBuilder` | Displays an item icon. Use `data-hyui-item-id` for the item icon.                                                        |
| `<span class="item-slot">` | `ItemSlotBuilder` | Displays a full item slot. Use `data-hyui-item-id` for the item.                                                         |
| `<div class="item-grid">` | `ItemGridBuilder` | Displays an item grid container.                                                                                         |
| `<div class="item-grid-slot">` | `ItemGridSlot` | Adds a slot entry inside an item grid.                                                                                   |
| `<img>`                   | `ImageBuilder` | Displays an image. Use `src` for the path.                                                                               |
| `<img class="dynamic-image">` | `DynamicImageBuilder` | Downloads and streams a PNG at runtime (see Dynamic Images).                                                      |
| `<select>`                | `DropdownBoxBuilder` | Dropdown selection lists. Use `<option>` children for entries.                                                           |
| `<sprite>`                | `SpriteBuilder` | Displays an animated sprite.                                                                                             |
| `<nav class="tabs">`      | `TabNavigationBuilder` | Tab navigation bar.                                                                                                      |

#### Attributes

HYUIML supports several standard and custom attributes:

*   `id`: Sets the element ID (accessible via `PageBuilder.getById` and for event listeners).
*   `class`: Used for CSS styling.
*   `value`: Sets the initial value for input elements.
*   `min`, `max`, `step`: Specific to sliders (`input type="range"`).
*   `checked`: Specific to checkboxes (`true` or `false`).
*   `format`: Number format string for `<input type="number">`.
*   `placeholder`: Hint text for text/password inputs.
*   `maxlength`: Maximum character limit for text/password inputs.
*   `readonly`: Makes text/password inputs read-only (`true` or `false`).
*   `width`, `height`: Specific to `<img>` tag, maps to `anchor-width` and `anchor-height`.
*   `data-hyui-title`: Specific to containers/overlays to set the header title.
*   `data-hyui-tooltiptext`: Adds a tooltip to the element.
*   `data-hyui-item-id`: In-game item ID for the icon to reflect.
*   `data-hyui-show-quality-background`: Specific to `<span class="item-slot">`, toggles item quality background.
*   `data-hyui-show-quantity`: Specific to `<span class="item-slot">`, toggles the item quantity display.
*   `data-hyui-max-decimal-places`: Specific to `<input type="number">`, sets the maximum decimal places.
*   `data-hyui-bar-texture-path`: Path to the progress bar's fill texture.
*   `data-hyui-effect-texture-path`: Path to the progress bar's effect texture.
*   `data-hyui-effect-width`, `data-hyui-effect-height`, `data-hyui-effect-offset`: Customizes the progress bar's effect appearance.
*   `data-hyui-direction`: Progress bar fill direction (`start` or `end`).
*   `data-hyui-alignment`: Progress bar orientation (`horizontal` or `vertical`).
*   `data-hyui-mask-texture-path`: Circular progress bar mask texture path.
*   `data-hyui-color`: Progress bar fill color (hex).
*   `data-hyui-allowunselection`: Specific to `<select>`, allows deselecting items.
*   `data-hyui-maxselection`: Specific to `<select>`, number of maximum selectable items.
*   `data-hyui-entryheight`: Specific to `<select>`, height of each dropdown entry.
*   `data-hyui-showlabel`: Specific to `<select>`, boolean to show or hide the label.
*   `data-hyui-frame-width`, `data-hyui-frame-height`, `data-hyui-frame-per-row`, `data-hyui-frame-count`: Specific to `<sprite>`, defines the sprite animation frames.
*   `data-hyui-fps`: Specific to `<sprite>`, sets the animation speed.
*   `data-hyui-background-mode`: Specific to `<div class="item-grid">`, sets the background mode.
*   `data-hyui-render-item-quality-background`: Specific to `<div class="item-grid">`, toggles item quality background rendering.
*   `data-hyui-are-items-draggable`: Specific to `<div class="item-grid">`, toggles item drag behavior.
*   `data-hyui-keep-scroll-position`: Specific to `<div class="item-grid">`, keeps scroll position.
*   `data-hyui-show-scrollbar`: Specific to `<div class="item-grid">`, toggles the scrollbar.
*   `data-hyui-slots-per-row`: Specific to `<div class="item-grid">`, sets slots per row.
*   `data-hyui-name`: Specific to `<div class="item-grid-slot">`, sets the slot label.
*   `data-hyui-description`: Specific to `<div class="item-grid-slot">`, sets the slot description.
*   `data-hyui-item-incompatible`: Specific to `<div class="item-grid-slot">`, toggles item incompatibility.
*   `data-hyui-item-uncraftable`: Specific to `<div class="item-grid-slot">`, toggles item uncraftable state.
*   `data-hyui-activatable`: Specific to `<div class="item-grid-slot">`, toggles slot activation.
*   `data-hyui-skip-item-quality-background`: Specific to `<div class="item-grid-slot">`, skips item quality background.
*   `data-hyui-slot-background`: Specific to `<div class="item-grid-slot">`, patch style reference for slot background.
*   `data-hyui-slot-overlay`: Specific to `<div class="item-grid-slot">`, patch style reference for slot overlay.
*   `data-hyui-slot-icon`: Specific to `<div class="item-grid-slot">`, patch style reference for slot icon.
*   `data-tabs`: Specific to `<nav class="tabs">`, defines tabs in `tabId:Label` or `tabId:Label:contentId` format.
*   `data-selected`: Specific to `<nav class="tabs">`, initial selected tab ID.
*   `data-tab-content`: Specific to `<button>` or `<a>` inside a tab nav, links a tab to a content ID.
*   `data-hyui-tab-id`: Specific to `<div class="tab-content">`, links this content block to a tab ID.
*   `data-hyui-tab-nav`: Optional for `<div class="tab-content">`, restricts it to a specific tab nav ID.

#### Styling with CSS

You can include a `<style>` block at the beginning of your HYUIML:

```html
<style>
    .header {
        color: #ff0000;
        font-weight: bold;
    }
    #my-button {
        flex-weight: 1;
    }
</style>
<div class="header">Title</div>
<button id="my-button">Click Me</button>
```

##### Supported CSS Properties:
*   `color`: Hex colors (e.g., `#FFFFFF`).
*   `font-size`: Numeric value.
*   `font-weight`: `bold` or `normal`.
*   `text-transform`: `uppercase` or `none`.
*   `hyui-style-reference`: Applies a style reference from a UI document (e.g., `hyui-style-reference: "Common.ui" "DefaultLabelStyle"`). Note that if this is used, other styling properties in the same block may be ignored.
*   `hyui-entry-label-style`: Style reference for dropdown entry labels.
*   `hyui-selected-entry-label-style`: Style reference for the selected dropdown entry label.
*   `hyui-popup-style`: Style reference for the dropdown popup menu.
*   `hyui-number-field-style`: Style reference for the number field.
*   `hyui-checked-style`: Style reference for checkboxes when checked.
*   `hyui-unchecked-style`: Style reference for checkboxes when unchecked.
*   `text-align`: `top`, `bottom`, `left`, `right`, `center`, `topscrolling`, `bottomscrolling`, `middlecenter`, `centermiddle`, `leftcenterwrap`, `rightcenterwrap`, `full`, `middle`, `middlecenter`. (Note: Maps to `LayoutMode` for `<div>`).
*   `layout-mode`, `layout`: Alternative names for `text-align` specifically for setting the `LayoutMode` on a `<div>`.
*   `vertical-align`: `top`, `bottom`, `center`.
*   `horizontal-align`: `left`, `right`, `center`.
*   `align`: Combines horizontal and vertical alignment (e.g., `center`).
*   `visibility`: `hidden` or `shown` (directly translates to `withVisible(bool)`).
*   `display`: `none` or `block` (alternative to `visibility`, also translates to `withVisible(bool)`).
*   `flex-weight`: Numeric weight for layout. From v0.5.0 onwards, this applies to the wrapping group (all elements except `Group` and `Label`), which can change layout behavior vs earlier versions.
*   `anchor-*`: Maps to Hytale anchors (e.g., `anchor-left`, `anchor-top`, `anchor-width`, `anchor-height`).
*   `background-image`: URL to an image (e.g., `url('lizard.png')` or `lizard.png`) with optional border values: `background-image: url('lizard.png') 4 6` (horizontal, vertical) or `background-image: url('lizard.png') 4` (border).
*   `background-color`: Hex color (e.g., `#ff0000` or `#ff0000(0.5)`) or `rgb(...)`/`rgba(...)` (converted to hex). Supports optional border values: `background-color: #ff0000 4 6` (horizontal, vertical) or `background-color: rgba(255, 0, 0, 0.5) 4` (border).

#### Custom Style Properties

Some elements support additional style properties that are not exposed via the standard CSS mapping. Use `data-hyui-style` to set arbitrary style keys directly on the element's `HyUIStyle`:

```html
<div class="item-grid" data-hyui-style="SlotSpacing: 6"></div>
```

Multiple properties can be specified in the same attribute:

```html
<div class="item-grid" data-hyui-style="SlotSpacing: 6; SlotSize: 64"></div>
```

These map to `HyUIStyle.set(key, value)` and are applied alongside any existing CSS-derived styles.

#### Image Assets

All image paths (in `src` for `<img>` or `url()` for `background-image`) are relative to your mod's `Common/UI/Custom` folder. 

**Important**: Hytale requires image assets to have a name ending in `@2x.png` for high-resolution support. 
For example, if you use `<img src="lizard.png"/>`, you must have a file named `lizard@2x.png` located in `src/main/resources/Common/UI/Custom/lizard@2x.png`.

#### Dynamic Images

Use `class="dynamic-image"` on `<img>` to download a PNG at runtime:

```html
<img class="dynamic-image" src="https://hyvatar.io/render/Elyra" />
```

Notes:
*   Dynamic images are limited to 10 per page.
*   Downloaded PNGs are cached for 15 seconds.

#### Special Layout Classes

HYUIML provides several special classes for `<div>` elements that map to Hytale's common layout macros:

*   **`.page-overlay`**: Wraps its children in a Hytale `PageOverlay`. This is typically used as the root element of your UI to ensure it fills the screen and handles background dimming.
*   **`.container`**: Maps to a Hytale `Container`.
    *   Use the `data-hyui-title` attribute on this `div` to set the container's header title.
    *   **`.container-title`**: A special child `div` of `.container`. Any elements inside this will be placed in the container's **#Title** area (alongside the main title).
    *   **`.container-contents`**: A special child `div` of `.container`. Any elements inside this will be placed in the container's main **#Content** area.
    *   *Note: If you don't use these specific child classes, elements added directly to a `.container` will be placed in the main `#Content` area by default.*
*   **`.item-grid`**: Maps to an `ItemGrid` element. This is not a container; it renders a grid of item slots.
*   **`.item-grid-slot`**: Adds a slot entry to the nearest `.item-grid`. This does not render on its own.
*   **`.tab-content`**: Marks a div as tab content. Use `data-hyui-tab-id` to link it to a tab.

Example usage:

```html
<div class="page-overlay">
    <div class="container" data-hyui-title="My Settings">
        <div class="container-title">
            <button id="help-btn">?</button>
        </div>
        <div class="container-contents">
            <p>Settings content goes here...</p>
        </div>
    </div>
</div>
```

#### Tabs (Tab Navigation + Content)

Use a `<nav class="tabs">` element for the tab bar, and `<div class="tab-content">` blocks for the content. The tab content blocks are registered at build time and auto-hidden unless their tab is selected. This stays consistent across `updatePage()` rebuilds.

Basic example using `data-tabs`:

```html
<nav id="main-tabs" class="tabs"
     data-tabs="templates:Templates, timers:Timers, components:Components"
     data-selected="templates">
</nav>

<div id="templates-content" class="tab-content" data-hyui-tab-id="templates">
    <p>Template examples...</p>
</div>

<div id="timers-content" class="tab-content" data-hyui-tab-id="timers">
    <p>Timer examples...</p>
</div>

<div id="components-content" class="tab-content" data-hyui-tab-id="components">
    <p>Component examples...</p>
</div>
```

Linking content directly in the tab list:

```html
<nav class="tabs"
     data-tabs="templates:Templates:templates-content,timers:Timers:timers-content"
     data-selected="templates">
</nav>
```

Using explicit buttons:

```html
<nav class="tabs" data-selected="templates">
    <button data-tab="templates" data-tab-content="templates-content">Templates</button>
    <button data-tab="timers" data-tab-content="timers-content">Timers</button>
</nav>
```

Multiple tab navigations on one page:

```html
<nav id="left-tabs" class="tabs" data-tabs="a:A,b:B" data-selected="a"></nav>
<nav id="right-tabs" class="tabs" data-tabs="x:X,y:Y" data-selected="x"></nav>

<div class="tab-content" data-hyui-tab-id="a" data-hyui-tab-nav="left-tabs">...</div>
<div class="tab-content" data-hyui-tab-id="x" data-hyui-tab-nav="right-tabs">...</div>
```

#### Event Handling

Events for elements defined in HYUIML are handled via the `PageBuilder` (or `HudBuilder`) using the IDs provided in the markup. Note that this is the primary way to add interaction to HYUIML elements, whereas elements loaded from raw `.ui` files via `.fromFile` do not support `.addEventListener`.

```java
builder.addEventListener("my-button", CustomUIEventBindingType.Activating, (ignored, ctx) -> {
    playerRef.sendMessage(Message.raw("Button clicked!"));
});
```

#### Important Limitations & Gotchas

While HYUIML looks like HTML, it is **not a full browser engine**. It is a lightweight bridge to Hytale's UI system.

1.  **Strict ID Sanitization**: Internally, Hytale only permits alphanumeric IDs. HyUI handles this by sanitizing your IDs (e.g., `my-button` becomes something like `HYUUIDmybutton0`). Always use your original ID (`my-button`) when calling `getById` or `addEventListener` in Java.
2.  **Limited CSS**: Only the properties listed above are supported. Traditional CSS layout (floats, flexbox, grid, positions) is **not fully supported**. From v0.5.0 onwards, partial flexbox support exists (e.g., `flex-direction`, `align-items`, `justify-content` mapping to layout/alignment), but layout is still primarily controlled by `Group` layout modes and `flex-weight`.
3.  **No Scripting**: `<script>` tags are ignored. All logic must be handled in Java.
4.  **Nesting Rules**: While most elements can be nested, some Hytale macros (like specialized buttons) might behave unexpectedly if wrapped in too many layers.
5.  **Comments**: Standard HTML comments `<!-- comment -->` are supported. In CSS, both `/* */` and `//` are supported.
