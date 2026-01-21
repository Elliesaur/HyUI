package au.ellie.hyui.commands;

import au.ellie.hyui.builders.HyUIHud;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

public class HyUIRemHudCommand extends AbstractAsyncCommand {

    public HyUIRemHudCommand() {
        super("rem", "Removes the last added HTML HUD");
        this.setPermissionGroup(GameMode.Adventure);
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
        if (commandContext.sender() instanceof Player) {
            HyUIAddHudCommand.TEST.remove();
            
            if (HyUIAddHudCommand.HUD_INSTANCES.isEmpty()) {
                commandContext.sendMessage(Message.raw("No HUDs to remove!"));
                return CompletableFuture.completedFuture(null);
            }
            
            HyUIHud lastHud = HyUIAddHudCommand.HUD_INSTANCES.remove(HyUIAddHudCommand.HUD_INSTANCES.size() - 1);
            lastHud.remove();
            commandContext.sendMessage(Message.raw("Removed last HUD."));
        }
        return CompletableFuture.completedFuture(null);
    }
}
