package au.ellie.hyui.commands;

import au.ellie.hyui.HyUIPluginLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

import static com.hypixel.hytale.server.core.command.commands.player.inventory.InventorySeeCommand.MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD;

public abstract class AbstractDevCommand extends AbstractAsyncCommand {
    public AbstractDevCommand(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
        if (!HyUIPluginLogger.IS_DEV)
            return;

        this.setPermissionGroup(GameMode.Adventure);
    }

    @Override
    protected CompletableFuture<Void> executeAsync(@NonNullDecl CommandContext commandContext) {
        if (!HyUIPluginLogger.IS_DEV)
            return CompletableFuture.completedFuture(null);

        var sender = commandContext.sender();
        if (!(sender instanceof Player player))
            return CompletableFuture.completedFuture(null);

        var ref = player.getReference();
        if (ref != null && ref.isValid()) {
            commandContext.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
            return CompletableFuture.completedFuture(null);
        }

        return executeDev(player, ref, commandContext);
    }

    @NonNullDecl
    abstract CompletableFuture<Void> executeDev(Player player, Ref<EntityStore> ref, CommandContext commandContext);
}
