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
                
                )
                .open(store);
    }
}
