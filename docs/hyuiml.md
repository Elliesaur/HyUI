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

| HTML Tag                  | HyUI Builder | Notes                                                             |
|---------------------------| --- |-------------------------------------------------------------------|
| `<div>`                   | `GroupBuilder` | Use for layout and containers.                                    |
| `<p>`                     | `LabelBuilder` | Standard text labels.                                             |
| `<label>`                 | `LabelBuilder` | Similar to `<p>`, often used for form field descriptions.         |
| `<button>`                | `ButtonBuilder` | Standard buttons. Use `class="back-button"` for a back button.    |
| `<input type="text">`     | `TextFieldBuilder` | Text input fields.                                                |
| `<input type="password">` | `TextFieldBuilder` | Masked password input fields.                                     |
| `<input type="number">`   | `NumberFieldBuilder` | Numeric input fields.                                             |
| `<input type="range">`    | `SliderBuilder` | Sliders.                                                          |
| `<input type="checkbox">` | `CheckBoxBuilder` | Toggle switches.                                                  |
| `<input type="color">`    | `ColorPickerBuilder` | Color selectors.                                                  |
| `<input type="reset">`    | `ButtonBuilder` | Specifically creates a `CancelTextButton`.                        |
| `<progress>`              | `ProgressBarBuilder` | Displays a progress bar.                                          |
| `<span class="item-icon">` | `ItemIconBuilder` | Displays an item icon. Use `data-hyui-item-id` for the item icon. |
| `<img>`                   | `ImageBuilder` | Displays an image. Use `src` for the path.                        |
| `<select>`                | `DropdownBoxBuilder` | Dropdown selection lists. Use `<option>` children for entries.   |

#### Attributes

HYUIML supports several standard and custom attributes:

*   `id`: Sets the element ID (accessible via `PageBuilder.getById` and for event listeners).
*   `class`: Used for CSS styling.
*   `value`: Sets the initial value for input elements.
*   `min`, `max`, `step`: Specific to sliders (`input type="range"`).
*   `checked`: Specific to checkboxes (`true` or `false`).
*   `placeholder`: Hint text for text/password inputs.
*   `maxlength`: Maximum character limit for text/password inputs.
*   `readonly`: Makes text/password inputs read-only (`true` or `false`).
*   `width`, `height`: Specific to `<img>` tag, maps to `anchor-width` and `anchor-height`.
*   `data-hyui-title`: Specific to containers/overlays to set the header title.
*   `data-hyui-tooltiptext`: Adds a tooltip to the element.
*   `data-hyui-item-id`: In-game item ID for the icon to reflect.
*   `data-hyui-bar-texture-path`: Path to the progress bar's fill texture.
*   `data-hyui-effect-texture-path`: Path to the progress bar's effect texture.
*   `data-hyui-effect-width`, `data-hyui-effect-height`, `data-hyui-effect-offset`: Customizes the progress bar's effect appearance.
*   `data-hyui-direction`: Progress bar fill direction (`start` or `end`).
*   `data-hyui-alignment`: Progress bar orientation (`horizontal` or `vertical`).
*   `data-hyui-allowunselection`: Specific to `<select>`, allows deselecting items.
*   `data-hyui-maxselection`: Specific to `<select>`, number of maximum selectable items.
*   `data-hyui-entryheight`: Specific to `<select>`, height of each dropdown entry.
*   `data-hyui-showlabel`: Specific to `<select>`, boolean to show or hide the label.

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
*   `text-align`: `top`, `bottom`, `left`, `right`, `center`, `topscrolling`, `bottomscrolling`, `middlecenter`, `centermiddle`, `leftcenterwrap`, `rightcenterwrap`, `full`, `middle`, `middlecenter`. (Note: Maps to `LayoutMode` for `<div>`).
*   `layout-mode`, `layout`: Alternative names for `text-align` specifically for setting the `LayoutMode` on a `<div>`.
*   `vertical-align`: `top`, `bottom`, `center`.
*   `horizontal-align`: `left`, `right`, `center`.
*   `align`: Combines horizontal and vertical alignment (e.g., `center`).
*   `visibility`: `hidden` or `shown` (directly translates to `withVisible(bool)`).
*   `display`: `none` or `block` (alternative to `visibility`, also translates to `withVisible(bool)`).
*   `flex-weight`: Numeric weight for layout.
*   `anchor-*`: Maps to Hytale anchors (e.g., `anchor-left`, `anchor-top`, `anchor-width`, `anchor-height`).
*   `background-image`: URL to an image (e.g., `url('lizard.png')`).
*   `background-color`: Hex color (e.g., `#ff0000`) or with opacity (e.g., `#ff0000(0.5)`).
*   `border`: Standard Hytale border support.

> **Note on Backgrounds**: Due to Hytale limitations, you currently cannot use `background-image`, `background-color`, and background opacity together in a single element's style. 

#### Image Assets

All image paths (in `src` for `<img>` or `url()` for `background-image`) are relative to your mod's `Common/UI/Custom` folder. 

**Important**: Hytale requires image assets to have a name ending in `@2x.png` for high-resolution support. 
For example, if you use `<img src="lizard.png"/>`, you must have a file named `lizard@2x.png` located in `src/main/resources/Common/UI/Custom/lizard@2x.png`.

#### Special Layout Classes

HYUIML provides several special classes for `<div>` elements that map to Hytale's common layout macros:

*   **`.page-overlay`**: Wraps its children in a Hytale `PageOverlay`. This is typically used as the root element of your UI to ensure it fills the screen and handles background dimming.
*   **`.container`**: Maps to a Hytale `Container`.
    *   Use the `data-hyui-title` attribute on this `div` to set the container's header title.
    *   **`.container-title`**: A special child `div` of `.container`. Any elements inside this will be placed in the container's **#Title** area (alongside the main title).
    *   **`.container-contents`**: A special child `div` of `.container`. Any elements inside this will be placed in the container's main **#Content** area.
    *   *Note: If you don't use these specific child classes, elements added directly to a `.container` will be placed in the main `#Content` area by default.*

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
2.  **Limited CSS**: Only the properties listed above are supported. Traditional CSS layout (floats, flexbox, grid, positions) is **not supported**. Layout is primarily controlled by `Group` layout modes and `flex-weight`.
3.  **No Scripting**: `<script>` tags are ignored. All logic must be handled in Java.
4.  **Nesting Rules**: While most elements can be nested, some Hytale macros (like specialized buttons) might behave unexpectedly if wrapped in too many layers.
5.  **Comments**: Standard HTML comments `<!-- comment -->` are supported. In CSS, both `/* */` and `//` are supported.
