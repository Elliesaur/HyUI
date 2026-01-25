package au.ellie.hyui.commands;

import au.ellie.hyui.builders.HudBuilder;
import au.ellie.hyui.builders.HyUIHud;
import au.ellie.hyui.builders.LabelBuilder;
import au.ellie.hyui.HyUIPluginLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.hypixel.hytale.server.core.command.commands.player.inventory.InventorySeeCommand.MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD;

public class HyUIAddHudCommand extends AbstractAsyncCommand {

    public static final List<HyUIHud> HUD_INSTANCES = new ArrayList<>();

    public static HyUIHud TEST;

    public HyUIAddHudCommand() {
        super("add", "Adds a new HTML HUD");
        if (!HyUIPluginLogger.LOGGING_ENABLED) {
            return;
        }
        this.setPermissionGroup(GameMode.Adventure);
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
        if (!HyUIPluginLogger.LOGGING_ENABLED) {
            return CompletableFuture.completedFuture(null);
        }
        var sender = commandContext.sender();
        if (sender instanceof Player player) {
            Ref<EntityStore> ref = player.getReference();
            if (ref != null && ref.isValid()) {
                Store<EntityStore> store = ref.getStore();
                World world = store.getExternalData().getWorld();
                return CompletableFuture.runAsync(() -> {
                    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                    if (playerRef != null) {
                        addHud(playerRef, store);
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

    private void addHud(PlayerRef playerRef, Store<EntityStore> store) {
        if (!HyUIPluginLogger.LOGGING_ENABLED) {
            return;
        }
        String html = """
            <div id="Test" style=" background-color: #000000; anchor-width: 280; anchor-height: 240; anchor-right: 1; anchor-top: 150">
                <div style="layout-mode: top">
                    <label>
                        HUD Instance #""" + (HUD_INSTANCES.size() + 1) + """
                    </label>
                    <label id="Hello">Initial Text</label>
                </div>
            </div>
            """;

        if (TEST == null) {

            /*HyUIHud hud = HudBuilder.detachedHud()
                    .fromFile("Pages/replicate.ui")
                    .editElement(uiCommandBuilder -> {
                        uiCommandBuilder.set("#SecondaryTitle.Text", "Say Cheeze");
                        uiCommandBuilder.set("#PrimaryTitle.Text", String.valueOf(System.currentTimeMillis()));
                    })
                    .withRefreshRate(1000)
                    .onRefresh((h) -> {
                    })
                    .show(playerRef, store);

            TEST = hud;*/
        }
        var hud2 = HudBuilder.detachedHud()
                .fromHtml(html)
                .withRefreshRate(1000)
                .onRefresh((h) -> {
                    h.getById("Hello", LabelBuilder.class).ifPresent((builder) -> {
                        builder.withText("Hello, World! " + System.currentTimeMillis());
                    });
                    //playerRef.sendMessage(Message.raw("HUD Refreshed!"));
                })
                .show(playerRef, store);
        HUD_INSTANCES.add(hud2);
    }
}
