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
import au.ellie.hyui.builders.HyUIPage;
import au.ellie.hyui.builders.LabelBuilder;
import au.ellie.hyui.builders.PageBuilder;
import au.ellie.hyui.commands.*;
import au.ellie.hyui.html.TemplateProcessor;
import au.ellie.hyui.uiparser.UIParseOptions;
import au.ellie.hyui.uiparser.UnifiedAssetSource;
import au.ellie.hyui.utils.HyvatarUtils;
import au.ellie.hyui.utils.MultiHudWrapper;
import au.ellie.hyui.utils.PngDownloadUtils;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.Asset;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.interface_.CustomPage;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.setup.AssetInitialize;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.handlers.game.GamePacketHandler;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import app.ultradev.hytaleuiparser.source.AssetSource;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

public class HyUIPlugin extends JavaPlugin {

    private static HyUIPluginLogger instance;
    
    private static final boolean ADD_CMDS = true;
    private static final ConcurrentMap<PlayerRef, Deque<Asset>> PENDING_ASSETS =
            new ConcurrentHashMap<>();
    private static final ConcurrentMap<PlayerRef, Boolean> REBUILD_SCHEDULED =
            new ConcurrentHashMap<>();
    
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
        // Intercept: AssetFinalize, RequestCommonAssetsRebuild, AssetPart, AssetInitialize
        PacketAdapters.registerOutbound((PacketHandler handler, Packet packet) -> {
            var packetName = packet.getClass().getSimpleName();
            if (!(handler instanceof GamePacketHandler h)) {
                return;
            }
            switch (packetName) {
                case "RequestCommonAssetsRebuild": {
                    var pRef = h.getPlayerRef();
                    scheduleReopenAfterRebuild(pRef);
                    break;
                }
                case "AssetInitialize": {
                    var p = (AssetInitialize) packet;
                    var pRef = h.getPlayerRef();
                    enqueueAsset(pRef, p.asset);
                    break;
                }
            }

        });
        PacketAdapters.registerOutbound((PacketHandler handler, Packet packet) -> {
            var packetName = packet.getClass().getSimpleName();
            if (packetName.equals("CustomPage")) {
                var uiCmd = (CustomPage) packet;
                var sb = new StringBuilder();
                for (CustomUICommand command : uiCmd.commands) {
                    sb.append("UICommand(");
                    sb.append("UICommand.Type." + command.type.name());
                    sb.append(", ");
                    sb.append(command.selector == null ? "null" : "\"" + command.selector + "\"");
                    sb.append(", ");
                    sb.append(command.data == null ? "null" : "\"" + command.data.replace("\"", "\\\"") + "\"");
                    sb.append(", ");
                    sb.append(command.text == null ? "null" : "\"" + command.text.replace("\"", "\\\"") + "\"");
                    sb.append(")\n");
                }
                HytaleLogger.forEnclosingClass().atInfo().log(sb.toString());
            }

        });
        UIParseOptions.setDefaultAssetSource(buildAssetSource());

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
            
            var pb = PageBuilder.detachedPage().fromUIFile("Pages/ComplexTest.ui");
            
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

                    String html = "Pages/HudTest.html";
                    var tp = new TemplateProcessor();
                    tp.setVariable("playerName", player.getDisplayName());
                    var hud = HudBuilder.detachedHud()
                            .loadHtml(html, tp)
                            .withRefreshRate(1000)
                            .onRefresh((h) -> {
                                h.getById("text", LabelBuilder.class).ifPresent((builder) -> {
                                    builder.withText("Hello, World! " + System.currentTimeMillis());
                                });
                            })
                            .show(playerRef);
                    pb.open(playerRef, store);
                });

            });
        }
        
    }

    private static void enqueueAsset(PlayerRef playerRef, Asset asset) {
        if (playerRef == null || asset == null) {
            return;
        }
        PENDING_ASSETS.computeIfAbsent(playerRef, ref -> new ConcurrentLinkedDeque<>()).push(asset);
    }

    private static void scheduleReopenAfterRebuild(PlayerRef playerRef) {
        if (playerRef == null || !playerRef.isValid() 
                || playerRef.getReference() == null 
                || !playerRef.getReference().isValid()) {
            return;
        }
        if (REBUILD_SCHEDULED.putIfAbsent(playerRef, Boolean.TRUE) != null) {
            return;
        }
        var store = playerRef.getReference().getStore();
        var world = store.getExternalData().getWorld();
        world.execute(() -> {
            try {
                var assets = drainAssets(playerRef);
                if (assets.isEmpty()) {
                    return;
                }
                var player = store.getComponent(playerRef.getReference(), Player.getComponentType());
                PageManager manager = player.getPageManager();
                var currentPage = manager.getCustomPage();
                if (currentPage instanceof HyUIPage page) {
                    for (var asset : assets) {
                        page.reopenFromAsset(player, playerRef, store, asset);
                    }
                }
                for (var hud : MultiHudWrapper.getHuds(player, playerRef)) {
                    for (var asset : assets) {
                        hud.reopenFromAsset(player, playerRef, store, asset);
                    }
                }
            } finally {
                REBUILD_SCHEDULED.remove(playerRef);
            }
        });
    }

    private AssetSource buildAssetSource() {
        UnifiedAssetSource source = new UnifiedAssetSource();
        for (Path assetPath : Options.getOptionSet().valuesOf(Options.ASSET_DIRECTORY)) {
            source.addPath(assetPath);
        }
        return source.listUIFiles().isEmpty() ? null : source;
    }

    private static Set<Asset> drainAssets(PlayerRef playerRef) {
        Deque<Asset> stack = PENDING_ASSETS.get(playerRef);
        if (stack == null) {
            return Set.of();
        }
        Set<Asset> drained = new HashSet<>();
        Asset asset;
        while ((asset = stack.pollFirst()) != null) {
            drained.add(asset);
        }
        if (stack.isEmpty()) {
            PENDING_ASSETS.remove(playerRef, stack);
        }
        return drained;
    }
}
