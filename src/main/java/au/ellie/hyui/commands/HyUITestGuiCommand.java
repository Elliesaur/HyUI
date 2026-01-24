package au.ellie.hyui.commands;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.HyUIPluginLogger;
import au.ellie.hyui.builders.*;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
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
import java.util.concurrent.atomic.AtomicInteger;

import static com.hypixel.hytale.server.core.command.commands.player.inventory.InventorySeeCommand.MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD;

public class HyUITestGuiCommand extends AbstractAsyncCommand {

    public HyUITestGuiCommand() {
        super("t", "Opens the HyUI Test GUI");
        this.setPermissionGroup(GameMode.Adventure);
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
        if (HyUIPluginLogger.LOGGING_ENABLED) {
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
        if (HyUIPluginLogger.LOGGING_ENABLED) {

            String html = """
                    <style>
                        .page-overlay {
                            anchor: 150;
                            background-image: url('lizard.png');
                        }
                        .container 
                        { 
                            layout-mode: top;
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
                            background-color: #ff0000;
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
                                <p id="welcome-msg" style="padding: 100 200">Welcome to HyUIML parser!</p>
                                <p id="label">Clicks: 0</p>
                                <div class="input-row" style="padding-left: 50; padding-top: 20;">
                                     Free text here should be a label!
                                    <input type="text" id="myInput"/>
                                </div>
                                <input type="number" value="42" style="padding: 10;"/>
                                <input type="range" min="0" max="100" value="50" step="1"/>
                                <label data-hyui-tooltiptext="This is a checkbox!">
                                    Checkbox Label
                                </label>
                                <input type="checkbox" checked="true"/>
                                <input type="color" value="#ff0000" 
                                    style="anchor-width: 140; anchor-height: 120; anchor-left: 12"/>
                                <img src="lizard.png" style="anchor-width: 100; anchor-height: 100;"/>
                                <img src="lizard.png" width="100" height="50"/>
                                <progress value="0.7" style="anchor-width: 500; anchor-height: 50" 
                                    data-hyui-effect-width="500" 
                                    data-hyui-effect-height="50" 
                                    data-hyui-effect-offset="0"></progress>
                                <span class="item-icon" data-hyui-item-id="Tool_Pickaxe_Crude" style="anchor-width: 64; anchor-height: 64;"></span>
                               
                                <button id="btn1">Click Me!</button>
                                <select id="myDropdown" data-hyui-showlabel="true" value="Entry1">
                                    <option value="Entry1">First Entry</option>
                                    <option value="Entry2">Second Entry</option>
                                    <option value="Entry3">Third Entry</option>
                                </select>
                                <sprite src="Common/Spinner.png"
                                    data-hyui-frame-width="32"
                                    data-hyui-frame-height="32"
                                    data-hyui-frame-per-row="8"
                                    data-hyui-frame-count="72"
                                    data-hyui-fps="30"
                                    style="anchor-width: 32; anchor-height: 32; anchor-right: 1;"
                                >
                                </sprite>
                                <button id="btn2"><span class="item-icon" data-hyui-item-id="Tool_Pickaxe_Crude" style="anchor-width: 64; anchor-height: 64;"></span></button>
                                <input type="reset" value="Cancel Operation" class="cancel-btn" style="font-size: 25px"/>
                            </div>
                        </div>
                    </div>
                    """;
            html = """
                    <div class="page-overlay">
                        <div class="container" style="anchor-width: 800; anchor-height: 900;" id="myContainer" data-hyui-title="HyUIML Parser Test">
                        <div style="anchor-left: 1; layout-mode: left;">
                            <div style="layout-mode: top">
                                <span class="item-slot" id="itemslot" data-hyui-item-id="Tool_Pickaxe_Crude" data-hyui-show-quality-background="true"
                                data-hyui-show-quantity="true" style="anchor-width: 64; anchor-height: 64;"></span>
                                <input type="number" value="42" min="-5" max="50" step="1.5" style="padding: 10; anchor-width: 50;"/>
                                <p>Please enter your desired Buy It Now price:</p>
                                <p style="color: #ff0000; visibility: hidden;" id="invalid-price">Price must be a positive number.</p>
                                <input id="price-input" type="number"/>
                                <button id="confirm-button">Confirm</button>
                            </div>
                        </div>
                        <div style="layout-mode: right;">
                            <div class="item-grid" id="itemgrid" data-hyui-slots-per-row="6"
                                style="anchor-width: 400; anchor-height: 400;">
                                <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude"></div>
                                <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude"></div>
                                <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude"></div>
                                <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude"></div>
                                <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude"></div>
                                <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude"></div>
                                <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude"></div>
                                <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude"></div>
                                <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude"></div>
                                <div class="item-grid-slot" data-hyui-item-id="Tool_Pickaxe_Crude"></div>
                                <div class="item-grid-slot"></div>
                                <div class="item-grid-slot"></div>
                                <div class="item-grid-slot"></div>
                            </div></div>

                        </div>
                    </div>
                    
                    """;
/*            html = """
                    <style>
                                 #Button {
                                     layout-mode: Left;
                                     padding: 6;
                                 }
                    
                                 #Button:hover {
                                     background-color: #000000(0.2);
                                 }
                    
                                 #Icon {
                                     anchor-width: 32;
                                     anchor-height: 32;
                                 }
                    
                                 #Name {
                                     padding-left: 10;
                                     padding-right: 10;
                                     padding-top: 5;
                                     padding-bottom: 5;
                                     font-weight: bold;
                                     flex-weight: 1;
                                 }
                    
                                 #Durability {
                                     padding-left: 10;
                                     padding-right: 10;
                                     padding-top: 5;
                                     padding-bottom: 5;
                                     color: #ffffff;
                                 }
                    
                                 .separator {
                                     layout-mode: Full;
                                     anchor-height: 2;
                                     background-color: #ffffff(0.6);
                                 }
                                .container {
                                    layout-mode: top;
                                    anchor-left: 100;
                                    anchor-right: 100; 
                                    anchor-top: 50; 
                                    anchor-bottom: 50; 
                                }
                    
                             </style>
                            <div class="page-overlay" style="anchor: 150">
                            <div class="container">
                             <div class="container-contents">
                             <div>
                                 <button id="Button">
                                     <span id="Icon" class="item-icon" src="Tool_Pickaxe_Crude"></span>
                                     <p id="Name">Item Name</p>
                                     <p id="Durability">100/100</p>
                                     <div class="separator"></div>
                                 </button>
                             </div>
                             </div>
                            </div>
                    
                             </div>
                    """;*/
            // TODO: 
            // -- Support opacity on text color. 
            // -- Support :hover sub-style (Just Style: (Hovered: ...)).


            //HyUIHud hudInstance = HudBuilder.detachedHud()
            //        .fromHtml(html)
            //        .show(playerRef, store);
            AtomicInteger clicks = new AtomicInteger();
            PageBuilder builder = PageBuilder.detachedPage()
                    .fromHtml(html)
                    .addEventListener("itemgrid", CustomUIEventBindingType.Dropped, (data, ctx) -> {
                        HyUIPlugin.getLog().logInfo("Item dropped on grid.");
                    })
                    .addEventListener("itemgrid", CustomUIEventBindingType.SlotClicking, (data, ctx) -> {
                        HyUIPlugin.getLog().logInfo("Slot clicked on grid.");
                    })
                    .addEventListener("itemslot", CustomUIEventBindingType.Dropped, (data, ctx) -> {
                        HyUIPlugin.getLog().logInfo("Slot dropped.");
                    })
                    .addEventListener("confirm-button", CustomUIEventBindingType.Activating, (_, context) -> {
                        var a = context.getValue("price-input", Double.class);
                        a.ifPresent(aDouble -> HyUIPlugin.getLog().logInfo("Price input is: " + aDouble));
                    })
                    .withLifetime(CustomPageLifetime.CanDismiss);
                /*.addEventListener("btn1", CustomUIEventBindingType.Activating, (data, ctx) -> {
                    playerRef.sendMessage(Message.raw("Button clicked via PageBuilder ID lookup!: " +
                    ctx.getValue("myInput", String.class).orElse("N/A")));
                    HyUIPlugin.getLog().logInfo("Clicked button.");
                    ctx.getById("label", LabelBuilder.class).ifPresent(lb -> { 
                        lb.withText("ClicksA: " + String.valueOf(clicks.incrementAndGet()));
                        HyUIPlugin.getLog().logInfo("Found label builder.");
                        ctx.updatePage(true);
                        for (String s : ctx.getCommandLog()) {
                            HyUIPlugin.getLog().logInfo(s);
                        }
                        HyUIPlugin.getLog().logInfo("Updated page.");
                    });
                })
                .addEventListener("myInput", CustomUIEventBindingType.ValueChanged, String.class, (val) -> {
                    playerRef.sendMessage(Message.raw("Input changed to: " + val));
                })
                .addEventListener("myDropdown", CustomUIEventBindingType.ValueChanged, String.class, (val) -> {
                    playerRef.sendMessage(Message.raw("Dropdown changed to: " + val));
                });
*/
            // Or ... if you don't like building in method chains or want something custom...
        /*builder.getById("myInput", TextFieldBuilder.class).ifPresent(input -> {
            input.addEventListener(CustomUIEventBindingType.ValueChanged, (val) -> {
                playerRef.sendMessage(Message.raw("Input changed to: " + val));
            });
        });*/
            builder.open(playerRef, store);
            for (String s : builder.getCommandLog()) {
                HyUIPlugin.getLog().logInfo(s);
            }
        }
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
        if (HyUIPluginLogger.LOGGING_ENABLED) {

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
                                        HyUIPlugin.getLog().logInfo("Before build callback for FirstButton");
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
                                        HyUIPlugin.getLog().logInfo("Slider value changed to: " + value);
                                        String text = ctx.getValue("MyTextField", String.class).orElse("N/A");
                                        Integer num = ctx.getValue("Hey", Integer.class).orElse(0);
                                        playerRef.sendMessage(Message.raw("Text Field: " + text + ", Num: " + num));
                                    }))
                            .addChild(ButtonBuilder.textButton()
                                    .withId("SecondButton")
                                    .withText("Text Button 2")
                                    .editElementAfter((commandBuilder, elementSelector) -> {
                                        HyUIPlugin.getLog().logInfo("HEEEEEEEEEEY");
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
                            .addChild(ProgressBarBuilder.progressBar()
                                    .withId("MyProgressBar")
                                    .withValue(0.45f)
                                    .withOuterAnchor(new HyUIAnchor().setWidth(200).setHeight(12))
                            )
                            .addChild(ButtonBuilder.textButton()
                                    .withText("Button with Icon")
                                    .withItemIcon(ItemIconBuilder.itemIcon().withItemId("Items/IronSword.png")))
                            .addChild(ContainerBuilder.container()
                                    .withId("MyContainer")
                                    .withTitleText("Custom Title")
                                    .addChild(new LabelBuilder()
                                            .withText("Inside Content")
                                            .inside("#Content"))
                                    .addChild(new LabelBuilder()
                                            .withText("Inside Title")
                                            .inside("#Title")))
                            .addChild(LabelBuilder.label()
                                    .withText("Styled via Reference")
                                    .withStyle(new HyUIStyle().withStyleReference("Common.ui", "DefaultLabelStyle")))
                            .addChild(PageOverlayBuilder.pageOverlay()
                                    .withId("MyOverlay")
                                    .addChild(new LabelBuilder()
                                            .withText("Overlay Content"))
                                    .addChild(ButtonBuilder.backButton()))
                    )
                    .open(store);
        }
    }
}
