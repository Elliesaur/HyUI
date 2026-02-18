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

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

public class HyUIRemHudCommand extends AbstractDevCommand {

    public HyUIRemHudCommand() {
        super("rem", "Removes the last added HTML HUD");
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeDev(Player player, Ref<EntityStore> ref, CommandContext commandContext) {
        HyUIAddHudCommand.TEST.remove();

        if (HyUIAddHudCommand.HUD_INSTANCES.isEmpty())
            commandContext.sendMessage(Message.raw("No HUDs to remove!"));
        else {
            commandContext.sendMessage(Message.raw("Removed last HUD."));
            HyUIAddHudCommand.HUD_INSTANCES.removeLast().remove();
        }

        return CompletableFuture.completedFuture(null);
    }
}
