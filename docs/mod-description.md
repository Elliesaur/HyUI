**HyUI** is a powerful, developer-friendly Java library designed to simplify the creation and management of custom User Interfaces for Hytale servers. **Now featuring HYUIML**, a declarative HTML/CSS-like syntax that allows you to build interfaces with familiar web-like markup. HyUI enables developers to construct complex, interactive, and high-performance UIs without the boilerplate of raw protocol handling by leveraging both this new markup and a fluent builder-based API.

Whether you are building a simple admin panel or a full-scale RPG menu system, HyUI provides the "escape hatches" and high-level abstractions needed to get the job done efficiently.

***

### Features

*   **Fluent Builder API:** Construct nested UI hierarchies (Groups, Buttons, Labels, etc.) using a clean, readable chain of methods.
*   **Dynamic Element Injection:** Load base `.ui` files and inject dynamic elements into specific selectors at runtime using the `inside("#Selector")` system.
*   **HYUIML (HTML/CSS):** Build interfaces using a familiar, declarative HTML-like syntax with CSS styling.
*   **Event Handling Simplified:** Bind server-side logic directly to UI events (like `Activating` or `ValueChanged`) using simple lambda expressions.
*   **Specialized Builders:** Includes ready-to-use builders for:
    *   **Buttons:** Standardized game-themed text buttons.
    *   **Input Fields:** Specialized builders for Text, Numbers, and Color Pickers.
    *   **Containers:** Flexible Group builders with various layout modes.
*   **Anchoring & Styling:** Robust support for `HyUIAnchor` for precise positioning and `HyUIStyle` for deep visual customization (colors, fonts, bold rendering, and disabled states).
*   **Rich Tooltips:** Easily attach `Message` based tooltips to any UI element.
*   **Multi-HUD Support:** Coexist with other mods using a smart HUD-chaining system that allows multiple HUD elements to be displayed simultaneously.
*   **Periodic UI Refresh:** Built-in support for batched, periodic HUD updates with low performance overhead.
*   **Advanced Logic (Escape Hatches):** Access raw `UICommandBuilder` instance at any point in the build process via `editElement` for properties not natively covered by the API.

***

### Quick Start

#### 1\. Installation (Gradle)

You can get started quickly by using the example project: [https://github.com/Elliesaur/Hytale-Example-UI-Project](https://github.com/Elliesaur/Hytale-Example-UI-Project)

Otherwise, add HyUI to your project via Cursemaven:

```
repositories {
    maven { url "https://www.cursemaven.com" }
}

dependencies {
    implementation "curse.maven:hyui-<project-id>:<file-id>"
}
```

#### 2\. Creating a Simple Page

Instantiate a `PageBuilder` to open a custom interface for a player:

```
new PageBuilder(playerRef)
    .fromFile("Pages/MyMenu.ui")
    .addElement(new GroupBuilder()
        .withId("MainContainer")
        .inside("#Content")
        .addChild(ButtonBuilder.textButton()
            .withText("Click Me!")
            .addEventListener(CustomUIEventBindingType.Activating, (ctx) -> {
                playerRef.sendMessage(Message.raw("You clicked the button!"));
            }))
    )
    .open(store);
```

#### 3\. Using HYUIML (HTML)

For a more declarative approach, use the `fromHtml` method:

```java
String html = """
    <div class="page-overlay">
        <div class="container" data-hyui-title="Settings">
            <p>Welcome to the menu!</p>
            <button id="myBtn">Click Me</button>
        </div>
    </div>
    """;

PageBuilder.detachedPage()
    .fromHtml(html)
    .addEventListener("myBtn", CustomUIEventBindingType.Activating, (ctx) -> {
        playerRef.sendMessage(Message.raw("Clicked!"));
    })
    .open(playerRef, store);
```

***

### Components

| Builder            |Purpose                                                                        |
| ------------------ |------------------------------------------------------------------------------ |
| <code>PageBuilder</code> |The entry point for UI creation; manages file loading and opening for players. |
| <code>HudBuilder</code> |The entry point for HUD creation; manages multi-HUD coexistence and periodic refreshes. |
| <code>GroupBuilder</code> |A container used to organize and layout child elements.                        |
| <code>ButtonBuilder</code> |For interactive buttons; supports <code>textButton()</code> for standard Hytale aesthetics. |
| <code>LabelBuilder</code> |For displaying dynamic text with style and anchor support.                     |
| <code>TextFieldBuilder</code> |Captures string input from the player.                                         |
| <code>ColorPickerBuilder</code> |Provides a Hex color selection interface.                                      |
| <code>SliderBuilder</code> |Provides support for number sliders.                                           |

***

### Documentation & Examples

A full implementation example, including a complete command class using `AbstractAsyncCommand`, can be found within the project repository docs folder.

Click the Source button on this page to go there!

**Requirements:**

*   Hytale Server added as a dependency
*   Java 25 (or current Hytale-compatible version)
*   jsoup is a dependency and included in the JAR under MIT license.