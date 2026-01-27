package au.ellie.hyui.commands;

import au.ellie.hyui.builders.HyUIHud;
import au.ellie.hyui.builders.LabelBuilder;
import au.ellie.hyui.HyUIPluginLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

public class HyUIUpdateHudCommand extends AbstractAsyncCommand {

    public HyUIUpdateHudCommand() {
        super("update", "Updates the label in the HUD");
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
                    updateHuds();
                }, world);
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    private void updateHuds() {
        if (!HyUIPluginLogger.LOGGING_ENABLED) {
            return;
        }
        long millis = System.currentTimeMillis();
        for (HyUIHud hud : HyUIAddHudCommand.HUD_INSTANCES) {
            hud.getById("Hello", LabelBuilder.class).ifPresent(label -> {
                label.withText(String.valueOf(millis));
            });
        }
    }
}
