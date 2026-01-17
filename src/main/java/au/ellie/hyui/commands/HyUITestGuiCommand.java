package au.ellie.hyui.commands;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.builders.*;
import au.ellie.hyui.html.HtmlParser;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
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
                        openHtmlTestGui(playerRef, store);
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
    private void openTestGuiMinimal(PlayerRef playerRef, Store<EntityStore> store) {
        new PageBuilder(playerRef)
                .fromFile("Pages/EllieAU_HyUI_Placeholder.ui")
                .open(store);
    }
    private void openHtmlTestGui(PlayerRef playerRef, Store<EntityStore> store) {
        String html = """
            <style>
                .page-overlay { 
                    anchor: 150; 
                }
                .container 
                { 
                    anchor-left: 100; 
                    anchor-right: 100; 
                    anchor-top: 50; 
                    anchor-bottom: 50; 
                }
                .title-text 
                { 
                    // This is a comment
                    color: #ff0000; 
                    font-weight: bold; 
                    text-transform: uppercase; 
                }
                #welcome-msg { 
                    /* Multi-line
                       comment */
                    font-size: 20; 
                    color: #00ff00; 
                }
                .input-row { text-align: top; flex-weight: 1; color: #ff0000; }
                input[type="text"] { flex-weight: 1; }
                .cancel-btn { flex-weight: 2; }
            </style>
            <div class="page-overlay">
                <div class="container" id="myContainer" data-hyui-title="HyUIML Parser Test">
                    <div class="container-title">
                        <p class="title-text">
                            Text Here That Isnt The Title
                        </p>
                    </div>
                    <div class="container-contents">
                        <p id="welcome-msg">Welcome to HyUIML parser!</p>
                        <div class="input-row">
                             Free text here should be a label!
                            <input type="text" id="myInput"/>
                        </div>
                        <input type="number" value="42"/>
                        <input type="range" min="0" max="100" value="50" step="1"/>
                        <label data-hyui-tooltiptext="This is a checkbox!">
                            Checkbox Label
                        </label>
                        <input type="checkbox" checked="true"/>
                        <input type="color" value="#ff0000" 
                            style="anchor-width: 140; anchor-height: 120; anchor-left: 12"/>
                        <button id="btn1">Click Me!</button>
                        <input type="reset" value="Cancel Operation" class="cancel-btn"/>
                    </div>
                </div>
            </div>
            """;
        PageBuilder builder = PageBuilder.detachedPage()
                .fromHtml(html)
                .withLifetime(CustomPageLifetime.CanDismiss)
                .addEventListener("btn1", CustomUIEventBindingType.Activating, (data, ctx) -> {
                    playerRef.sendMessage(Message.raw("Button clicked via PageBuilder ID lookup!: " + 
                    ctx.getValue("myInput", String.class).orElse("N/A")));
                })
                .addEventListener("myInput", CustomUIEventBindingType.ValueChanged, String.class, (val) -> {
                    playerRef.sendMessage(Message.raw("Input changed to: " + val));
                });
        
        // Or ... if you don't like building in method chains or want something custom...
        builder.getById("myInput", TextFieldBuilder.class).ifPresent(input -> {
            input.addEventListener(CustomUIEventBindingType.ValueChanged, (val) -> {
                playerRef.sendMessage(Message.raw("Input changed to: " + val));
            });
        });
        builder.open(playerRef, store);
    }

    private void openTestGuiFromScratch(PlayerRef playerRef, Store<EntityStore> store) {
        
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
                                .addEventListener(CustomUIEventBindingType.Activating, (ignored, ctx) -> {
                                    String text = ctx.getValue("MyTextField", String.class).orElse("N/A");
                                    Double num = ctx.getValue("ANum", Double.class).orElse(0.0);
                                    playerRef.sendMessage(Message.raw("Text Field: " + text + ", Num: " + num));
                                }))
                        .addChild(SliderBuilder.slider()
                                .withId("Hey")
                                .withMax(300)
                                .withMin(-50)
                                .withStep(10)
                                .withValue(51)
                                .addEventListener(CustomUIEventBindingType.ValueChanged, (value, ctx) -> {
                                    HyUIPlugin.getInstance().logInfo("Slider value changed to: " + value);
                                    String text = ctx.getValue("MyTextField", String.class).orElse("N/A");
                                    Integer num = ctx.getValue("Hey", Integer.class).orElse(0);
                                    playerRef.sendMessage(Message.raw("Text Field: " + text + ", Num: " + num));
                                }))
                        .addChild(ButtonBuilder.textButton()
                                .withId("SecondButton")
                                .withText("Text Button 2")
                                .editElementAfter((commandBuilder, elementSelector) -> {
                                    HyUIPlugin.getInstance().logInfo("HEEEEEEEEEEY");
                                    commandBuilder.set(elementSelector + ".Text", "Heyyy");
                                })
                                .addEventListener(CustomUIEventBindingType.Activating, (ignored) -> {
                                    playerRef.sendMessage(Message.raw("Text Button 2 clicked!"));
                                }))
                        .addChild(TextFieldBuilder.textInput()
                                .withId("MyTextField")
                                .withValue("Test Value")
                                .addEventListener(CustomUIEventBindingType.ValueChanged, (val) -> {
                                    playerRef.sendMessage(Message.raw("Text Field changed to: " + val));
                                }))
                        .addChild(new CheckBoxBuilder()
                                .withId("MyCheckBox")
                                .withValue(true)
                                .addEventListener(CustomUIEventBindingType.ValueChanged, (checked) -> {
                                    playerRef.sendMessage(Message.raw("CheckBox: " + checked));
                                })
                        )
                        .addChild(new ColorPickerBuilder()
                                .withValue("#aabbcc")
                                .addEventListener(CustomUIEventBindingType.ValueChanged, (val) -> {
                                    playerRef.sendMessage(Message.raw("Color Picker changed to: " + val));
                                })
                        )
                        .addChild(NumberFieldBuilder.numberInput()
                                .withValue(25)
                                .withId("ANum")
                                .addEventListener(CustomUIEventBindingType.ValueChanged, (val) -> {
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
                        .addChild(ContainerBuilder.container()
                                .withId("MyContainer")
                                .withTitleText("Custom Title")
                                .addChild(new LabelBuilder()
                                        .withText("Inside Content")
                                        .inside("#Content"))
                                .addChild(new LabelBuilder()
                                        .withText("Inside Title")
                                        .inside("#Title")))
                        .addChild(PageOverlayBuilder.pageOverlay()
                                .withId("MyOverlay")
                                .addChild(new LabelBuilder()
                                        .withText("Overlay Content"))
                                .addChild(ButtonBuilder.backButton()))
                )
                .open(store);
    }
}
