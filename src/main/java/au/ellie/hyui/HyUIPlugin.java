package au.ellie.hyui;

import au.ellie.hyui.builders.HudBuilder;
import au.ellie.hyui.builders.LabelBuilder;
import au.ellie.hyui.commands.*;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class HyUIPlugin extends JavaPlugin {

    private static HyUIPluginLogger instance;
    
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
        if (HyUIPluginLogger.LOGGING_ENABLED) {
            instance.logInfo("Setting up plugin " + this.getName());
            this.getCommandRegistry().registerCommand(new HyUITestGuiCommand());
            this.getCommandRegistry().registerCommand(new HyUIAddHudCommand());
            this.getCommandRegistry().registerCommand(new HyUIRemHudCommand());
            this.getCommandRegistry().registerCommand(new HyUIUpdateHudCommand());
            this.getCommandRegistry().registerCommand(new HyUIShowcaseCommand());
            this.getCommandRegistry().registerCommand(new HyUITemplateRuntimeCommand());
            
            this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> {
                instance.logInfo("Player ready event triggered for " + event.getPlayer().getDisplayName());
                
                var player = event.getPlayer();
                if (player == null) return;

                Ref<EntityStore> ref = event.getPlayerRef();
                if (!ref.isValid()) return;

                Store<EntityStore> store = ref.getStore();
                World world = store.getExternalData().getWorld();
                world.execute(() -> {
                    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                    String html = "<hyvatar username='" + player.getDisplayName() + "' size='64'></hyvatar><div style='anchor-width: 400; anchor-height: 50;'><progress value='50' max='100' data-hyui-bar-texture-path='Common/ShopTest.png'></progress></div>";
                    var hud = HudBuilder.detachedHud()
                            .fromHtml(html)
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
