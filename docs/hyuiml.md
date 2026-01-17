### HYUIML (HyUI Markup Language)

HYUIML is an HTML-like markup language for defining Hytale UIs using a familiar syntax. It is parsed by HyUI and converted into the fluent builder API calls under the hood.

#### Basic Usage

You can load HYUIML directly into a `PageBuilder`:

```java
String html = "<div><p>Hello from HYUIML!</p></div>";
new PageBuilder(playerRef)
    .fromHtml(html)
    .open(store);
```

#### Supported Tags and Mappings

| HTML Tag | HyUI Builder | Notes |
| --- | --- | --- |
| `<div>` | `GroupBuilder` | Use for layout and containers. |
| `<p>` | `LabelBuilder` | Standard text labels. |
| `<label>` | `LabelBuilder` | Similar to `<p>`, often used for form field descriptions. |
| `<button>` | `ButtonBuilder` | Standard buttons. |
| `<input type="text">` | `TextFieldBuilder` | Text input fields. |
| `<input type="number">` | `NumberFieldBuilder` | Numeric input fields. |
| `<input type="range">` | `SliderBuilder` | Sliders. |
| `<input type="checkbox">` | `CheckBoxBuilder` | Toggle switches. |
| `<input type="color">` | `ColorPickerBuilder` | Color selectors. |
| `<input type="reset">` | `ButtonBuilder` | Specifically creates a `CancelTextButton`. |

#### Attributes

HYUIML supports several standard and custom attributes:

*   `id`: Sets the element ID (accessible via `PageBuilder.getById` and for event listeners).
*   `class`: Used for CSS styling.
*   `value`: Sets the initial value for input elements.
*   `min`, `max`, `step`: Specific to sliders (`input type="range"`).
*   `checked`: Specific to checkboxes (`true` or `false`).
*   `data-hyui-title`: Specific to containers/overlays to set the header title.
*   `data-hyui-tooltiptext`: Adds a tooltip to the element.

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
*   `text-align`: `top`, `bottom`, `left`, `right`, `center`.
*   `flex-weight`: Numeric weight for layout.
*   `anchor-*`: Maps to Hytale anchors (e.g., `anchor-left`, `anchor-top`, `anchor-width`, `anchor-height`).

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

Events for elements defined in HYUIML are handled via the `PageBuilder` using the IDs provided in the markup:

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
