/*
 *     Copyright (C) 2026 EllieAU
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package au.ellie.hyui.commands;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.HyUIPluginLogger;
import au.ellie.hyui.builders.*;
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.events.PageRefreshResult;
import au.ellie.hyui.events.SlotMouseDragCompletedEventData;
import au.ellie.hyui.types.*;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hypixel.hytale.server.core.command.commands.player.inventory.InventorySeeCommand.MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD;

public class HyUITestGuiCommand extends AbstractAsyncCommand {

    public HyUITestGuiCommand() {
        super("t", "Opens the HyUI Test GUI");
        if (!HyUIPluginLogger.IS_DEV) {
            return;
        }
        this.setPermissionGroup(GameMode.Adventure);
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
        if (!HyUIPluginLogger.IS_DEV) {
            return CompletableFuture.completedFuture(null);
        }
        if (HyUIPluginLogger.IS_DEV) {
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
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }
    
    private void openReplicate(PlayerRef playerRef, Store<EntityStore> store) {
        if (!HyUIPluginLogger.IS_DEV) {
            return;
        }
        new PageBuilder(playerRef)
                .loadHtml("Pages/Replicate.html")
                .open(store);
    }
    
    private void openTestGuiMinimal(PlayerRef playerRef, Store<EntityStore> store) {
        if (!HyUIPluginLogger.IS_DEV) {
            return;
        }
        new PageBuilder(playerRef)
                .fromFile("Pages/EllieAU_HyUI_Placeholder.ui")
                .open(store);
    }
    private void openHtmlTestGui(PlayerRef playerRef, Store<EntityStore> store) {
        if (!HyUIPluginLogger.IS_DEV) {
            return;
        }

        // Resource file: Common/UI/Custom/Pages/HyUIHtmlTest.html
        /*html = """
                    <div class="page-overlay">
                        <div class="decorated-container" style="anchor-width: 800; anchor-height: 900;" id="myContainer" data-hyui-title="HyUIML Parser Test">
                        <div style="anchor-left: 1; layout-mode: left;">
                            <div style="layout-mode: top">
                                <button id="test">
                                    <span class="item-slot" id="itemslot" data-hyui-item-id="Tool_Pickaxe_Crude" data-hyui-show-quality-background="true"
                                    data-hyui-show-quantity="true" style="anchor-width: 64; anchor-height: 64;">
                                    </span>
                                </button>
                               <div style="background-color: #2a2a3e; padding: 10; anchor-width: 140; align-items: center; justify-content: center;">
                                   <hyvatar username="Elyra" render="full" size="256" rotate="45"></hyvatar>
                               </div>
                                <input type="number" value="42" min="-5" max="50" step="1.5" style="padding: 10; anchor-width: 50;"/>
                                <p>Please enter your desired Buy It Now price:</p>
                                <p style="color: #ff0000; visibility: hidden;" id="invalid-price">Price must be a positive number.</p>
                                <input id="price-input" type="number"/>
                                <button id="confirm-button" class="secondary-button" >Confirm</button>
                                <button id="confirm-button" class="small-secondary-button" >Confirm</button>
                                <button id="confirm-button" class="small-tertiary-button" >Confirm</button>
                                <button id="confirm-button" class="tertiary-button" >Confirm</button>
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
                    
                    """;*/
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
                .loadHtml("Pages/HyUIHtmlTest.html")
                /*.addEventListener("itemgrid", CustomUIEventBindingType.Dropped, (data, ctx) -> {
                    HyUIPlugin.getLog().logInfo("Item dropped on grid.");
                })
                .addEventListener("itemgrid", CustomUIEventBindingType.SlotClicking, (data, ctx) -> {
                    HyUIPlugin.getLog().logInfo("Slot clicked on grid.");
                })
                .addEventListener("itemslot", CustomUIEventBindingType.Dropped, (data, ctx) -> {
                    HyUIPlugin.getLog().logInfo("Slot dropped.");
                })
                .addEventListener("test", CustomUIEventBindingType.Activating, (_, context) -> {
                    var a = context.getValue("price-input", Double.class);
                    a.ifPresent(aDouble -> HyUIPlugin.getLog().logInfo("Price input is: " + aDouble));
                })*/
                .withLifetime(CustomPageLifetime.CanDismiss)
                .withRefreshRate(1000)
                .onRefresh((page) -> {
                    playerRef.sendMessage(Message.raw("HEY"));
                    int count = clicks.get();
                    if (count == 0) {
                        return PageRefreshResult.NONE;
                    }
                    return (count % 2 == 0) ? PageRefreshResult.UPDATE_CLEAR : PageRefreshResult.UPDATE;
                })
                .addEventListener("btn1", CustomUIEventBindingType.Activating, (data, ctx) -> {
                    playerRef.sendMessage(Message.raw("Button clicked via PageBuilder ID lookup!: " +
                    ctx.getValue("myInput", String.class).orElse("N/A")));
                    HyUIPlugin.getLog().logFinest("Clicked button.");
                    ctx.getById("label", LabelBuilder.class).ifPresent(lb -> { 
                        lb.withText("ClicksA: " + String.valueOf(clicks.incrementAndGet()));
                        HyUIPlugin.getLog().logFinest("Found label builder.");
                        ctx.updatePage(true);
                        /*for (String s : ctx.getCommandLog()) {
                            HyUIPlugin.getLog().logInfo(s);
                        }
                        HyUIPlugin.getLog().logInfo("Updated page.");*/
                    });
                    var val = ctx.getValue("myDropdown", String.class).orElse("N/A");
                    playerRef.sendMessage(Message.raw("Dropdown VALUE: " + val));
                    ctx.getById("myDropdown", DropdownBoxBuilder.class).ifPresent(lb -> {
                        HyUIPlugin.getLog().logFinest("Found Dropdown builder.");
                        var val2 = ctx.getValue("myDropdown", String.class).orElse("N/A");
                        playerRef.sendMessage(Message.raw("Dropdown VALUE: " + val2));
                        // SETTING VALUE
                        //lb.withValue("Entry3");
                        
                    });

                })
                .addEventListener("myInput", CustomUIEventBindingType.ValueChanged, String.class, (val) -> {
                    playerRef.sendMessage(Message.raw("Input changed to: " + val));
                })
                .addEventListener("myDropdown", CustomUIEventBindingType.ValueChanged, String.class, (val) -> {
                    playerRef.sendMessage(Message.raw("Dropdown changed to: " + val));
                });

        // Or ... if you don't like building in method chains or want something custom...
        /*builder.getById("myInput", TextFieldBuilder.class).ifPresent(input -> {
            input.addEventListener(CustomUIEventBindingType.ValueChanged, (val) -> {
                playerRef.sendMessage(Message.raw("Input changed to: " + val));
            });
        });*/
        builder.open(playerRef, store);
        for (String s : builder.getCommandLog()) {
            HyUIPlugin.getLog().logFinest(s);
        }
    }

    private void openHtmlTestGui2(PlayerRef playerRef, Store<EntityStore> store) {
        if (!HyUIPluginLogger.IS_DEV) {
            return;
        }
        
        PageBuilder builder = PageBuilder.detachedPage()
                .loadHtml("Pages/ItemGridTest.html")
                .addEventListener("itemgrid", CustomUIEventBindingType.SlotMouseDragCompleted, SlotMouseDragCompletedEventData.class, (data, ctx) -> {
                    playerRef.sendMessage(Message.raw("Mouse drag completed on item grid: " + data.getSlotIndex()));
                    playerRef.sendMessage(Message.raw("Mouse drag completed on item grid: " + data.getItemStackId()));
                })
                .addEventListener("test", CustomUIEventBindingType.Activating, (_, context) -> {
                    var a = context.getValue("price-input", Double.class);
                    a.ifPresent(aDouble -> HyUIPlugin.getLog().logFinest("Price input is: " + aDouble));
                })
                .withLifetime(CustomPageLifetime.CanDismiss);
        
        builder.getById("itemgrid", ItemGridBuilder.class).ifPresent(ig -> {
            ig.addSlot(new ItemGridSlot(new ItemStack("Ore_Gold", 25)));
            ig.addSlot(new ItemGridSlot(new ItemStack("Ore_Iron", 25)));
        });
        for (CustomUIEventBindingType typeName : CustomUIEventBindingType.values()) {
            builder.addEventListener("itemgrid", typeName, (data, ctx) -> {
                playerRef.sendMessage(Message.raw("Event triggered: " + typeName.name()));
            });
        }
        for (CustomUIEventBindingType typeName : CustomUIEventBindingType.values()) {
            builder.addEventListener("itemslot", typeName, (data, ctx) -> {
                playerRef.sendMessage(Message.raw("Event triggered: " + typeName.name()));
            });
        }
        builder.open(playerRef, store);
        for (String s : builder.getCommandLog()) {
            HyUIPlugin.getLog().logFinest(s);
        }
    }


    private void openTestGuiFromScratch(PlayerRef playerRef, Store<EntityStore> store) {
        if (!HyUIPluginLogger.IS_DEV) {
            return;
        }

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
        if (!HyUIPluginLogger.IS_DEV) {
            return;
        }

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
                                    HyUIPlugin.getLog().logFinest("Before build callback for FirstButton");
                                })
                                .withTooltipTextSpan(Message.raw("This button has a tooltip now!"))
                                .withStyle(new HyUIStyle().setTextColor("#00FF00").setFontSize(16))
                                .addEventListener(CustomUIEventBindingType.Activating, (ignored, ctx) -> {
                                    String text = ctx.getValue("MyTextField", String.class).orElse("N/A");
                                    Double num = ctx.getValue("ANum", Double.class).orElse(0.0);
                                    playerRef.sendMessage(Message.raw("Text Field: " + text + ", Num: " + num));
                                }))
                        /*.addChild(SliderBuilder.slider()
                                .withId("Hey")
                                .withMax(300)
                                .withMin(-50)
                                .withStep(10)
                                .withValue(51)
                                .withAnchor(new HyUIAnchor().setHeight(15).setWidth(100))
                                .withStyle(SliderStyle.defaultStyle())
                                .addEventListener(CustomUIEventBindingType.ValueChanged, (value, ctx) -> {
                                    HyUIPlugin.getLog().logFinest("Slider value changed to: " + value);
                                    String text = ctx.getValue("MyTextField", String.class).orElse("N/A");
                                    Integer num = ctx.getValue("Hey", Integer.class).orElse(0);
                                    playerRef.sendMessage(Message.raw("Text Field: " + text + ", Num: " + num));
                                }))
                        .addChild(ButtonBuilder.textButton()
                                .withId("SecondButton")
                                .withText("Text Button 2")
                                .editElementAfter((commandBuilder, elementSelector) -> {
                                    HyUIPlugin.getLog().logFinest("HEEEEEEEEEEY");
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
                        .addChild(TextFieldBuilder.multilineTextField()
                                .withId("MyMultilineTextField")
                                .withPlaceholderText("%client.feedback.field.description.placeholder")
                                .withStyle(new HyUIStyle().withStyleReference("Common.ui", "DefaultInputFieldStyle"))
                                .withPlaceholderStyle(new HyUIStyle().withStyleReference("Common.ui", "DefaultInputFieldPlaceholderStyle"))
                                .withBackground("Common.ui", "InputBoxBackground")
                                .withScrollbarStyle("Common.ui", "DefaultScrollbarStyle")
                                .withContentPadding(HyUIPadding.symmetric(8, 10))
                                .withMaxVisibleLines(8)
                                .withMaxLength(1000)
                                .withAnchor(new HyUIAnchor().setTop(5).setHeight(150))
                                .withAutoGrow(false)
                                .addEventListener(CustomUIEventBindingType.ValueChanged, (val) -> {
                                    playerRef.sendMessage(Message.raw("Multiline text updated: " + val));
                                }))
                        .addChild(new CheckBoxBuilder()
                                .withId("MyCheckBox")
                                .withValue(true)
                                .withStyle(CheckBoxStyle.defaultStyle())
                                .addEventListener(CustomUIEventBindingType.ValueChanged, (checked) -> {
                                    playerRef.sendMessage(Message.raw("CheckBox: " + checked));
                                })
                        )
                        .addChild(new ColorPickerBuilder()
                                .withValue("#aabbcc")
                                .withStyle(ColorPickerStyle.defaultStyle())
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
                        )*/
                        .addChild(ButtonBuilder.textButton()
                                .withText("Button with Icon"))
                                //.withItemIcon(ItemIconBuilder.itemIcon().withItemId("Items/IronSword.png")))
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
                        .addChild(ActionButtonBuilder.actionButton()
                                .withId("ActionButtonExample")
                                .withActionName("ExampleAction")
                                .withKeyBindingLabel("K")
                                .withAlignment(ActionButtonAlignment.Right))
                        .addChild(BlockSelectorBuilder.blockSelector()
                                .withId("BlockSelectorExample")
                                .withCapacity(8))
                        .addChild(ColorPickerDropdownBoxBuilder.colorPickerDropdownBox()
                                .withId("ColorPickerDropdownExample")
                                .withFormat(ColorFormat.Rgba)
                                .withDisplayTextField(true)
                                .withStyle(ColorPickerDropdownBoxStyle.defaultStyle()))
                        .addChild(FloatSliderBuilder.floatSlider()
                                .withId("FloatSliderExample")
                                .withMin(0)
                                .withMax(1)
                                .withStep(0.05f)
                                .withValue(0.3f)
                                .withStyle(SliderStyle.defaultStyle()))
                        .addChild(LabelBuilder.label()
                                .withText("Hey")
                                .withTooltipTextSpans(List.of(Message.raw("Lol").bold(true), Message.raw("No").bold(false)))
                                .addTextSpan(Message.raw("Hey3").bold(true).color(Color.RED))
                                .addTextSpan(Message.raw("Hey4").bold(true).color(Color.RED))
                                .addTextSpan(Message.raw("Hey5").bold(true).color(Color.YELLOW))
                        )
                        .addChild(FloatSliderNumberFieldBuilder.floatSliderNumberField()
                                .withId("FloatSliderNumberFieldExample")
                                .withMin(0)
                                .withMax(10)
                                .withStep(0.5f)
                                .withValue(2.5f)
                                .withSliderStyle(SliderStyle.defaultStyle())
                                .withNumberFieldStyle(InputFieldStyle.defaultStyle()))
                        .addChild(HotkeyLabelBuilder.hotkeyLabel()
                                .withId("HotkeyLabelExample")
                                .withInputBindingKey("F"))
                        .addChild(ItemSlotButtonBuilder.itemSlotButton()
                                .withId("ItemSlotButtonExample")
                                .withLayoutMode("Left"))
                        .addChild(LabeledCheckBoxBuilder.labeledCheckBox()
                                .withId("LabeledCheckBoxExample"))
                        .addChild(MenuItemBuilder.menuItem()
                                .withId("MenuItemExample")
                                .withText("Menu Item"))
                        .addChild(PanelBuilder.panel()
                                .withId("PanelExample")
                                .withBackground(new HyUIPatchStyle().setColor("#00000020"))
                                .withAnchor(new HyUIAnchor().setWidth(120).setHeight(40)))
                        .addChild(DynamicPaneContainerBuilder.dynamicPaneContainer()
                                .withId("DynamicPaneContainerExample")
                                .withLayoutMode(LayoutMode.Left)
                                .withAnchor(new HyUIAnchor().setWidth(260).setHeight(120))
                                .addPane(DynamicPaneBuilder.dynamicPane()
                                        .withId("DynamicPaneExampleLeft")
                                        .withMinSize(80)
                                        .withResizeAt(ResizeType.End)
                                        .withResizerSize(4)
                                        .withBackground(new HyUIPatchStyle().setColor("#00000020"))
                                        .withResizerBackground(new HyUIPatchStyle().setColor("#00000040"))
                                        .addChild(LabelBuilder.label()
                                                .withText("Pane A")))
                                .addPane(DynamicPaneBuilder.dynamicPane()
                                        .withId("DynamicPaneExampleRight")
                                        .withMinSize(80)
                                        .withBackground(new HyUIPatchStyle().setColor("#00000020"))
                                        .addChild(LabelBuilder.label()
                                                .withText("Pane B"))))
                        .addChild(NativeTabNavigationBuilder.nativeTabNavigation()
                                .withId("NativeTabNavigationExample")
                                .withSelectedTab("NativeTabOne")
                                .withStyle(DefaultStyles.textTopTabsStyle())
                                .withAllowUnselection(false)
                                .onSelectedTabChanged((a) -> {})
                                .withAnchor(new HyUIAnchor().setWidth(260).setHeight(36))
                                .addTab(new NativeTab().withId("NativeTabOne").withText("HELLO"))
                                .addTab(new NativeTab().withId("NativeTabTwo").withText("SECOND"))
                                
                                /*.addChild(NativeTabButtonBuilder.nativeTabButton()
                                        .withId("NativeTabOne")
                                        .withTabId("NativeTabOne")
                                        .withIcon(new HyUIPatchStyle().setTexturePath("Common/IconGeneralSettingsActivated.png"))
                                        .withIconSelected(new HyUIPatchStyle().setTexturePath("Common/IconGeneralSettingsActivated.png"))
                                        .withText("One"))
                                .addChild(NativeTabButtonBuilder.nativeTabButton()
                                        .withId("NativeTabTwo")
                                        .withTabId("NativeTabTwo")
                                        .withIcon(new HyUIPatchStyle().setTexturePath("Common/IconGeneralSettingsActivated.png"))
                                        .withIconSelected(new HyUIPatchStyle().setTexturePath("Common/IconGeneralSettingsActivated.png"))
                                        .withText("Two"))
                                .addChild(NativeTabButtonBuilder.nativeTabButton()
                                        .withId("NativeTabThree")
                                        .withTabId("NativeTabThree")
                                        .withIcon(new HyUIPatchStyle().setTexturePath("Common/IconGeneralSettingsActivated.png"))
                                        .withIconSelected(new HyUIPatchStyle().setTexturePath("Common/IconGeneralSettingsActivated.png"))
                                        .withText("Three")))*/
                        ).addChild(GroupBuilder.group()
                                .withId("NativeTabOne")
                                .addChild(LabelBuilder.label().withText("NativeTabOne")))
                        .addChild(GroupBuilder.group()
                                .withId("NativeTabTwo")
                                .addChild(LabelBuilder.label().withText("NativeTabTwo")))
                        .addChild(GroupBuilder.group()
                                .withId("NativeTabThree")
                                .addChild(LabelBuilder.label().withText("NativeTabThree")))
                        .addChild(GroupBuilder.group()
                                .withId("ScrollableContainerExample")
                                .withLayoutMode("Top")
                                .withScrollbarStyle("Common.ui", "DefaultScrollbarStyle")
                                .withAnchor(new HyUIAnchor().setWidth(180).setHeight(100))
                                .withContentHeight(240)
                                .withKeepScrollPosition(true)
                                .withBackground(new HyUIPatchStyle().setColor("#00000010"))
                                .addChild(LabelBuilder.label().withText("Scrollable Item 1"))
                                .addChild(LabelBuilder.label().withText("Scrollable Item 2"))
                                .addChild(LabelBuilder.label().withText("Scrollable Item 3"))
                                .addChild(LabelBuilder.label().withText("Scrollable Item 4"))
                                .addChild(LabelBuilder.label().withText("Scrollable Item 5")))
                        .addChild(ReorderableListBuilder.reorderableList()
                                .withId("ReorderableListExample")
                                .withAnchor(new HyUIAnchor().setWidth(180).setHeight(140))
                                .addChild(GroupBuilder.group()
                                        .withLayoutMode("Left")
                                        .withPadding(new HyUIPadding().setLeft(6).setRight(6).setTop(4).setBottom(4))
                                        .addChild(ReorderableListGripBuilder.reorderableListGrip()
                                                .withId("ReorderableListExampleGrip1"))
                                        .addChild(LabelBuilder.label()
                                                .withText("Item One")))
                                .addChild(GroupBuilder.group()
                                        .withLayoutMode("Left")
                                        .withPadding(new HyUIPadding().setLeft(6).setRight(6).setTop(4).setBottom(4))
                                        .addChild(ReorderableListGripBuilder.reorderableListGrip()
                                                .withId("ReorderableListExampleGrip2"))
                                        .addChild(LabelBuilder.label()
                                                .withText("Item Two")))
                                .addChild(GroupBuilder.group()
                                        .withLayoutMode("Left")
                                        .withPadding(new HyUIPadding().setLeft(6).setRight(6).setTop(4).setBottom(4))
                                        .addChild(ReorderableListGripBuilder.reorderableListGrip()
                                                .withId("ReorderableListExampleGrip3"))
                                        .addChild(LabelBuilder.label()
                                                .withText("Item Three"))))
                        .addChild(ReorderableListGripBuilder.reorderableListGrip()
                                .withId("ReorderableListGripExample"))
                        .addChild(SceneBlurBuilder.sceneBlur()
                                .withId("SceneBlurExample"))
                        .addChild(SliderNumberFieldBuilder.sliderNumberField()
                                .withId("SliderNumberFieldExample")
                                .withMin(0)
                                .withMax(100)
                                .withStep(5)
                                .withValue(25)
                                .withSliderStyle(SliderStyle.defaultStyle())
                                .withNumberFieldStyle(InputFieldStyle.defaultStyle()))
                        .addChild(ToggleButtonBuilder.toggleButton()
                                .withId("ToggleButtonExample")
                                .withStyle(ButtonStyle.primaryStyle())
                                .withCheckedStyle(ButtonStyle.primaryStyle()))
                        .addChild(PageOverlayBuilder.pageOverlay()
                                .withId("MyOverlay")
                                .addChild(new LabelBuilder()
                                        .withText("Overlay Content"))
                                .addChild(ButtonBuilder.backButton()))
                )
                .open(store);
    }
}
