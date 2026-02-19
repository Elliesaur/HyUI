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

import au.ellie.hyui.builders.LabelBuilder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

public class HyUIUpdateHudCommand extends AbstractDevCommand {

    public HyUIUpdateHudCommand() {
        super("update", "Updates the label in the HUD");
    }

    @NonNullDecl
    protected CompletableFuture<Void> executeDev(Player player, Ref<EntityStore> ref, CommandContext commandContext) {
        var store = ref.getStore();
        var world = store.getExternalData().getWorld();

        return CompletableFuture.runAsync(this::updateHuds, world);
    }

    private void updateHuds() {
        var millis = System.currentTimeMillis();

        for (var hud : HyUIAddHudCommand.HUD_INSTANCES) {
            hud.getById("Hello", LabelBuilder.class).ifPresent(label -> {
                label.withText(String.valueOf(millis));
            });

            hud.refreshOrRerender(false, false);
        }
    }
}
