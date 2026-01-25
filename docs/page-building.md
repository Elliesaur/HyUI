### Page Building

Pages in HyUI are full-screen user interfaces that typically handle player interaction while the game is paused or the cursor is active. They are managed via the `PageBuilder`.

#### Content Sources

There are multiple ways to define the content of your page:

##### 1. Loading from UI Files
You can load an existing Hytale `.ui` file as the base for your page. HyUI often uses a "Placeholder" UI that provides a standard container and title.

> **Note**: Elements defined within the `.ui` file cannot have event listeners attached to them via `.addEventListener`. This method is only for elements created via the Java Builder API or HYUIML. To interact with elements in a `.ui` file, use `.editElement` to send raw UI commands.

```java
PageBuilder.pageForPlayer(playerRef)
    .fromFile("Pages/MyPage.ui")
    .open(store);
```

##### 2. Loading from HYUIML (HTML)
You can define your page using HTML-like syntax, which is often easier for complex layouts. Elements defined in HYUIML **do** support `.addEventListener` using the `id` specified in the HTML.

When using HYUIML for a page, it is recommended to use the standard structural classes:
- `.page-overlay`: Ensures the UI fills the screen and handles background dimming.
- `.container`: Provides the standard Hytale window frame.
- `.container-contents`: The main area for your content.
- `.decorated-container`: Uses the decorated container UI file for a framed variant of the standard container.

```java
String html = """
    <div class="page-overlay">
        <div class="decorated-container" data-hyui-title="My Custom Page">
            <div class="container-contents">
                <p>Welcome to my custom page built with HYUIML!</p>
                <button id="hi-btn">Say Hi</button>
            </div>
        </div>
    </div>
    """;

PageBuilder.pageForPlayer(playerRef)
    .fromHtml(html)
    .addEventListener("hi-btn", CustomUIEventBindingType.Activating, (ignored, ctx) -> {
        playerRef.sendMessage(Message.raw("Hello from the UI!"));

        // Use UIContext to access the page or element values
        ctx.getPage().ifPresent(page -> {
            // Do something with the page
        });
    })
    .open(store);
```

#### Event Listeners and UIContext

When you register an event listener, the callback receives a `UIContext` as the second argument. This context provides a way to interact with the current state of the UI without needing to keep a direct reference to the page.

Key methods in `UIContext`:
- `getPage()`: Returns an `Optional<HyUIPage>`. This allows you to access the page instance if the event originated from a page.
- `getValue(String id)`: Retrieves the current value of an element by its ID.
- `getValue(String id, Class<T> type)`: Retrieves the current value of an element and casts it to the specified type.

```java
.addEventListener("my-button", CustomUIEventBindingType.Activating, (ignored, ctx) -> {
    // Accessing the page
    ctx.getPage().ifPresent(page -> page.close());

    // Accessing values from other elements
    Optional<String> username = ctx.getValue("username-input", String.class);
});
```

##### 3. Manual Building
You can manually add elements using builders for fine-grained control.

```java
PageBuilder.detachedPage()
    .withLifetime(CustomPageLifetime.CanDismiss)
    .addElement(PageOverlayBuilder.pageOverlay()
        .withId("MyOverlay")
        .addChild(ContainerBuilder.container()
            .withTitleText("Custom UI from scratch")
            .addContentChild(
                LabelBuilder.label()
                    .withText("Overlay Content")
            )
        )
    )
    .addElement(ButtonBuilder.backButton())
    .open(playerRef, store);
```

#### Detached Pages

Just like HUDs, you can prepare a page configuration before you have a player reference.

```java
PageBuilder builder = PageBuilder.detachedPage()
    .fromHtml("<p>Hello World</p>")
    .withLifetime(CustomPageLifetime.CanDismiss);

// Later, open it for a specific player
builder.open(playerRef, store);
```

#### Page Lifetime

You can control how the page behaves when the player tries to close it or when other UIs open.

```java
PageBuilder.pageForPlayer(playerRef)
    .withLifetime(CustomPageLifetime.CanDismiss) // Standard dismissable page
    .open(store);
```

#### Updating Page Values

You can update elements on an open page by retrieving their builders from the `UIContext`, modifying them, and calling `updatePage()`.

```java
PageBuilder.detachedPage()
    .fromHtml("""
        <p id="label">Clicks: 0</p>
        <button id="btn">Click Me!</button>
    """)
    .addEventListener("btn", CustomUIEventBindingType.Activating, (data, ctx) -> {
        // Increment a counter (e.g., from an AtomicInteger)
        int newClicks = clicks.incrementAndGet();

        // Get the builder for the label
        ctx.getById("label", LabelBuilder.class).ifPresent(lb -> { 
            // Update the builder state
            lb.withText("Clicks: " + newClicks);

            // Send the update to the client
            ctx.updatePage(true); 
        });
    })
    .open(playerRef, store);
```

> **Important**: When calling `ctx.updatePage(true)`, the page is rebuilt on the client. Due to a known issue in Hytale, `Slider` elements (created via `SliderBuilder` or `<input type="range">`) may lose their custom styles during this update.

#### Modifying Base Elements

If you load from a file, you can use `.editElement` to modify components defined in the `.ui` file before your dynamic elements are added.

```java
PageBuilder.pageForPlayer(playerRef)
    .fromFile("Pages/EllieAU_HyUI_Placeholder.ui")
    .editElement(commands -> {
        commands.set("#HyUITitle.Text", "Dynamic Title");
    })
    .open(store);
```

#### Complete Implementation Example

For a full example of a command that opens a variety of different page types, see the `HyUITestGuiCommand.java` file in the source code.
