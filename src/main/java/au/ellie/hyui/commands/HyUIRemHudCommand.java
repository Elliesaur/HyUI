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

import au.ellie.hyui.builders.HyUIHud;
import au.ellie.hyui.HyUIPluginLogger;
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
