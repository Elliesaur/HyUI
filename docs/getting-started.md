### Getting Started with HyUI

HyUI is a fluent, builder-based library for creating and managing custom user interfaces in Hytale. This guide will walk you through the basics of building a page using the `PageBuilder` and various element builders.

#### 0. Installation

To use HyUI in your Hytale project, you can either include the JAR file directly or use Cursemaven if you are using Gradle.

**Using the JAR file:**
1. Download the latest `HyUI-0.X.0.jar` (replace X with version) from the CurseForge page (see [README](../README.md)).
2. Place the JAR in your project's `libs` folder.
3. Add the following to your `build.gradle` (replace X with version):
```gradle
dependencies {
    implementation files('libs/HyUI-0.X.0.jar')
}
```

**Using Cursemaven (Gradle):**
You can use [Cursemaven](https://www.cursemaven.com/) to easily add it as a dependency:

```gradle
repositories {
    maven {
        url "https://www.cursemaven.com"
    }
}

dependencies {
    // Replace <project-id> and <file-id> with the actual IDs from CurseForge
    implementation "curse.maven:hyui-<project-id>:<file-id>"
}
```

#### 1. Creating a Page

To start building a UI, use the `PageBuilder`. You can load a base `.ui` file and then add dynamic elements to it.

##### What is a Placeholder UI file?
A placeholder UI file (e.g., `Placeholder.ui`) is a base UI file that typically contains:
1.  A **PageOverlay** component to ensure the UI behaves like a full page.
2.  A **Container** (often named `#Content`) where HyUI will inject your dynamic elements.

Example `Placeholder.ui` structure located in Common.UI.Custom.Pages:
```ui
$C = "../Common.ui";

@CustomStyle = TextButtonStyle(
              Default: (Background: $C.@DefaultSquareButtonDefaultBackground, LabelStyle: $C.@DefaultButtonLabelStyle),
              Hovered: (Background: $C.@DefaultSquareButtonHoveredBackground, LabelStyle: $C.@DefaultButtonLabelStyle),
              Pressed: (Background: $C.@DefaultSquareButtonPressedBackground, LabelStyle: $C.@DefaultButtonLabelStyle),
              Disabled: (Background: $C.@DefaultSquareButtonDisabledBackground, LabelStyle: $C.@DefaultButtonDisabledLabelStyle),
              Sounds: $C.@ButtonSounds,
            );

$C.@PageOverlay {
  $C.@Container #HyUIContainer {
    Anchor: (Width: 600, Height: 260);

    #Title {
      Group {
        $C.@Title #HyUITitle {
          @Text = "HyUI";
        }
      }
    }

    #Content {
      LayoutMode: Left;
    }
  }
}
```

##### Using Callbacks for Page Customization
You can use `editElement` callbacks on the `PageBuilder` to modify properties of the base UI file, such as its title.

```java
new PageBuilder(playerRef)
    .fromFile("Pages/MyBasePage.ui")
    .editElement(commands -> {
        commands.set("#Overlay.Title", "My Custom Title");
    })
    .open(store);
```

#### 2. Adding Elements

You can add elements like groups, buttons, and labels using the `addElement` method. Elements can be nested using the `addChild` method.

```java
new PageBuilder(playerRef)
    .fromFile("Pages/MyBasePage.ui")
    .addElement(new GroupBuilder()
        .withId("ParentGroup")
        .withLayoutMode("Top")
        .inside("#Content") // Target a selector in the base .ui file
        .addChild(ButtonBuilder.textButton()
            .withId("MyButton")
            .withText("Click Me!")
            .addEventListener(CustomUIEventBindingType.Activating, (ignored, ctx) -> {
                playerRef.sendMessage(Message.raw("Button clicked!"));
            }))
    )
    .open(store);
```

#### 3. Common Element Builders

HyUI provides several specialized builders for common UI components:

*   **GroupBuilder**: A container for other elements. Use `.withLayoutMode("Top"|"Bottom"|...)`.
*   **ButtonBuilder**: Create buttons. Use `ButtonBuilder.textButton()` for a standard game-themed button.
*   **LabelBuilder**: Display text. Use `.withText("Hello")`.
*   **TextFieldBuilder**: Text input fields. Use `TextFieldBuilder.textInput()`.
*   **NumberFieldBuilder**: Numeric input fields. Use `NumberFieldBuilder.numberInput()`.
*   **CheckBoxBuilder**: Boolean toggle. Use `.withValue(true)`.
*   **ColorPickerBuilder**: Hex color selector.

#### 4. Positioning with Anchors

Every element can be positioned using `HyUIAnchor`.

```java
.addChild(new LabelBuilder()
    .withText("Anchored Label")
    .withAnchor(new HyUIAnchor()
        .setTop(10)
        .setLeft(10)
        .setWidth(100)
        .setHeight(30))
)
```

#### 5. Styling Elements

Use `HyUIStyle` to customize the appearance of elements that support styling (Labels, Buttons, etc.).

```java
.withStyle(new HyUIStyle()
    .setFontSize(20)
    .setTextColor("#FF0000")
    .setRenderBold(true)
    .setDisabledStyle(new HyUIStyle().setTextColor("#888888")))
```

#### 6. Advanced Customization (Escape Hatches)

If the builder doesn't support a specific property, you can use `editElement` methods to access the raw `UICommandBuilder`.

*   **`PageBuilder.editElement`**: Modify the base page before elements are added.
*   **`UIElementBuilder.editElementBefore`**: Modify an element before its specific properties are set.
*   **`UIElementBuilder.editElementAfter`**: Modify an element after it's fully built by the specialized builder.

```java
.addChild(ButtonBuilder.textButton()
    .editElementAfter((commandBuilder, selector) -> {
        commandBuilder.set(selector + ".CustomProperty", "Value");
    }))
```

#### 7. Visibility and Tooltips

You can easily control visibility and add rich-text tooltips to any element.

```java
.withVisible(true)
.withTooltipTextSpan(Message.raw("This is a tooltip!"))
```

#### 8. Full Example (Command Implementation)

The following example shows how to implement a command that opens a HyUI page. This includes the full `HyUITestGuiCommand.java` file.

```java
package au.ellie.hyui.commands;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.builders.*;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

import static com.hypixel.hytale.server.core.command.commands.player.inventory.InventorySeeCommand.MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD;

public class HyUITestGuiCommand extends AbstractAsyncCommand {

    public HyUITestGuiCommand() {
        super("test", "Opens the HyUI Test GUI");
        this.setPermissionGroup(GameMode.Adventure);
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
        var sender = commandContext.sender();
        if (sender instanceof Player player) {
            player.getWorldMapTracker().tick(0);
            Ref<EntityStore> ref = player.getReference();
            if (ref != null && ref.isValid()) {
                Store<EntityStore> store = ref.getStore();
                World world = store.getExternalData().getWorld();
                return CompletableFuture.runAsync(() -> {
                    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                    if (playerRef != null) {
                        openTestGui(playerRef, store);
                    }
                }, world);
            } else {
                commandContext.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
                return CompletableFuture.completedFuture(null);
            }
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    private void openTestGui(PlayerRef playerRef, Store<EntityStore> store) {
        new PageBuilder(playerRef)
                .fromFile("Pages/EllieAU_HyUI_Placeholder.ui")
                .editElement((commandBuilder) -> {
                    //commandBuilder.set("#Selector", "ValueHere");
                })
                .editElement((commandBuilder) -> {
                    //commandBuilder.set("#Selector2", "ValueHere");
                })
                .addElement(new GroupBuilder()
                        .withId("ParentGroup")
                        .withLayoutMode("Top")
                        .inside("#Content")
                        .addChild(ButtonBuilder.textButton()
                                .withId("FirstButton")
                                .withText("Text Button 1")
                                .editElementBefore((commandBuilder, elementSelector) -> {
                                    HyUIPlugin.getInstance().logInfo("Before build callback for FirstButton");
                                })
                                .withTooltipTextSpan(Message.raw("This button has a tooltip now!"))
                                .withStyle(new HyUIStyle().setTextColor("#00FF00").setFontSize(16))
                                .addEventListener(CustomUIEventBindingType.Activating, (ignored) -> {
                                    playerRef.sendMessage(Message.raw("Text Button 1 clicked!"));
                                }))
                        .addChild(ButtonBuilder.textButton()
                                .withId("SecondButton")
                                .withText("Text Button 2")
                                .editElementAfter((commandBuilder, elementSelector) -> {
                                    HyUIPlugin.getInstance().logInfo("HEEEEEEEEEEY");
                                    commandBuilder.set(elementSelector + ".Text", "Heyyy");
                                })
                                .addEventListener(CustomUIEventBindingType.Activating, (ignored, ctx) -> {
                                    playerRef.sendMessage(Message.raw("Text Button 2 clicked!"));
                                }))
                        .addChild(TextFieldBuilder.textInput()
                                //.withId("MyTextField")
                                .withValue("Test Value")
                                .addEventListener(CustomUIEventBindingType.ValueChanged, (val, ctx) -> {
                                    playerRef.sendMessage(Message.raw("Text Field changed to: " + val));
                                }))
                        .addChild(new CheckBoxBuilder()
                                .withId("MyCheckBox")
                                .withValue(true)
                                .addEventListener(CustomUIEventBindingType.ValueChanged, (checked, ctx) -> {
                                    playerRef.sendMessage(Message.raw("CheckBox: " + checked));
                                })
                        )
                        .addChild(new ColorPickerBuilder()
                                .withValue("#aabbcc")
                                .addEventListener(CustomUIEventBindingType.ValueChanged, (val, ctx) -> {
                                    playerRef.sendMessage(Message.raw("Color Picker changed to: " + val));
                                })
                        )
                        .addChild(NumberFieldBuilder.numberInput()
                                .withValue(25)
                                .withId("ANum")
                                .addEventListener(CustomUIEventBindingType.ValueChanged, (val, ctx) -> {
                                    playerRef.sendMessage(Message.raw("Number Field changed to: " + val));
                                })
                        )
                        .addChild(new LabelBuilder()
                                .withId("MyLabel")
                                .withText("Hello World")
                                .withTooltipTextSpan(Message.raw("This is a tooltip"))
                                .withAnchor(new HyUIAnchor().setTop(10).setLeft(10).setWidth(100).setHeight(30))
                                .withVisible(true)
                                .withStyle(new HyUIStyle()
                                        .setFontSize(20)
                                        .setTextColor("#FF0000")
                                        .setRenderBold(true)
                                        //.set("CustomProperty", "ValueHere")
                                        //.setDisabledStyle(new HyUIStyle().setTextColor("#888888")))
                                ))

                )
                .open(store);
    }
}
```

#### 9. Interacting with Elements at Runtime

Mod authors can access the current values of other elements on the page within event listeners using the `UIContext` parameter.

```java
.addChild(ButtonBuilder.textButton()
    .withId("SubmitButton")
    .withText("Submit")
    .addEventListener(CustomUIEventBindingType.Activating, (ignored, ctx) -> {
        // Access value by ID set via .withId()
        String username = ctx.getValue("UsernameField", String.class).orElse("");
        Boolean rememberMe = ctx.getValue("RememberCheckBox", Boolean.class).orElse(false);
        
        playerRef.sendMessage(Message.raw("Logging in as: " + username + " (Remember: " + rememberMe + ")"));
    }))
```

The `UIContext` tracks values of elements that support them (TextField, NumberField, CheckBox, ColorPicker) automatically as they change in the UI. Values are initialized with whatever was set via `.withValue()` at build time.

#### 10. Building with HYUIML (HTML/CSS)

For those who prefer a more declarative approach, HyUI supports a subset of HTML and CSS called **HYUIML**. This allows you to define your UI structure and styling in a single markup string.

```java
String html = """
    <style>
        .my-label { color: #00FF00; font-weight: bold; }
    </style>
    <div>
        <p class="my-label">Welcome to HYUIML!</p>
        <button id="my-button">Click Me</button>
    </div>
    """;

new PageBuilder(playerRef)
    .fromHtml(html)
    .addEventListener("my-button", CustomUIEventBindingType.Activating, (ignored, ctx) -> {
        playerRef.sendMessage(Message.raw("Button clicked via HYUIML!"));
    })
    .open(store);
```

**Note:** HYUIML is **not** a full web engine. It has significant limitations:
*   **CSS Support:** Only basic properties like `color`, `font-size`, `font-weight`, and Hytale-specific `anchor-*` properties are supported.
*   **Layout:** Standard CSS layout (flex, grid, absolute positioning) is not supported. Use Hytale's layout modes and `flex-weight`.
*   **ID Sanitization:** Internal IDs are strictly alphanumeric. Use your original IDs in Java code; HyUI handles the mapping automatically.

For a full list of supported tags, attributes, and CSS properties, see the [HYUIML Documentation](hyuiml.md).
