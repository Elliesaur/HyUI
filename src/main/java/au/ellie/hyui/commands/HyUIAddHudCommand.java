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

import au.ellie.hyui.builders.HudBuilder;
import au.ellie.hyui.builders.HyUIHud;
import au.ellie.hyui.builders.LabelBuilder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HyUIAddHudCommand extends AbstractDevCommand {

    public static final List<HyUIHud> HUD_INSTANCES = new ArrayList<>();

    public static HyUIHud TEST;

    public HyUIAddHudCommand() {
        super("add", "Adds a new HTML HUD");
    }

    @NonNullDecl
    protected CompletableFuture<Void> executeDev(Player player, Ref<EntityStore> ref, CommandContext commandContext) {
        var store = ref.getStore();
        var world = store.getExternalData().getWorld();

        return CompletableFuture.runAsync(() -> {
            var playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef != null)
                addHud(playerRef, store);
        }, world);
    }

    private void addHud(PlayerRef playerRef, Store<EntityStore> store) {
        var html = """
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
                .withRefreshRate(5000)
                .onRefresh((h) -> {
                    h.getById("Hello", LabelBuilder.class).ifPresent((builder) -> {
                        builder.withText("Hello, World! " + System.currentTimeMillis());
                    });
                    //playerRef.sendMessage(Message.raw("HUD Refreshed!"));
                }).show(playerRef);

        hud2.getById("Hello", LabelBuilder.class).ifPresent((builder) ->
                builder.withText("Hello, BAD! " + System.currentTimeMillis())
        );

        HUD_INSTANCES.add(hud2);
    }
}
