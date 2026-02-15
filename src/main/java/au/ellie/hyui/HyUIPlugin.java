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

package au.ellie.hyui;

import au.ellie.hyui.builders.HudBuilder;
import au.ellie.hyui.builders.LabelBuilder;
import au.ellie.hyui.commands.*;
import au.ellie.hyui.html.TemplateProcessor;
import au.ellie.hyui.utils.HyvatarUtils;
import au.ellie.hyui.utils.PngDownloadUtils;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.io.IOException;

public class HyUIPlugin extends JavaPlugin {

    private static HyUIPluginLogger instance;
    
    private static final boolean ADD_CMDS = false;
    
    public static HyUIPluginLogger getLog() {
        if (instance == null)
            instance = new HyUIPluginLogger();
        return instance;
    }

    public HyUIPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        if (instance == null)
            instance = new HyUIPluginLogger();
    }

    @Override
    protected void setup() {
        if (ADD_CMDS) {
            instance.logFinest("Setting up plugin " + this.getName());
            this.getCommandRegistry().registerCommand(new HyUITestGuiCommand());
            this.getCommandRegistry().registerCommand(new HyUIAddHudCommand());
            this.getCommandRegistry().registerCommand(new HyUIRemHudCommand());
            this.getCommandRegistry().registerCommand(new HyUIUpdateHudCommand());
            this.getCommandRegistry().registerCommand(new HyUIShowcaseCommand());
            this.getCommandRegistry().registerCommand(new HyUITemplateRuntimeCommand());
            this.getCommandRegistry().registerCommand(new HyUIBountyCommand());
            this.getCommandRegistry().registerCommand(new HyUITabsCommand());
            
            this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> {
                instance.logFinest("Player ready event triggered for " + event.getPlayer().getDisplayName());
                
                var player = event.getPlayer();
                if (player == null) return;

                Ref<EntityStore> ref = event.getPlayerRef();
                if (!ref.isValid()) return;

                Store<EntityStore> store = ref.getStore();
                World world = store.getExternalData().getWorld();
                world.execute(() -> {
                    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                    try {
                        PngDownloadUtils.prefetchPngForPlayer(playerRef, HyvatarUtils.buildRenderUrl(
                                player.getDisplayName(), 
                                HyvatarUtils.RenderType.HEAD, 
                                64, null, null), 18000);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    String html = "<hyvatar username='" + player.getDisplayName() + "' size='64'></hyvatar><div style='anchor-width: 400; anchor-height: 50;'><progress value='50' max='100' data-hyui-bar-texture-path='Common/ShopTest.png'></progress></div>";
                    var tp = new TemplateProcessor();
                    var hud = HudBuilder.detachedHud()
                            .fromTemplate(html, tp)
                            .withRefreshRate(1000)
                            .onRefresh((h) -> {
                                h.getById("text", LabelBuilder.class).ifPresent((builder) -> {
                                    builder.withText("Hello, World! " + System.currentTimeMillis());
                                });
                            })
                            .show(playerRef);
                });

            });
        }
        
    }
}
