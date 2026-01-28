package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class HudBuilder extends InterfaceBuilder<HudBuilder> {
    private final PlayerRef playerRef;
    private long refreshRateMs = 0;
    private Consumer<HyUIHud> refreshListener;
    private HyUIHud lastHud;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public HudBuilder(PlayerRef playerRef) {
        this.playerRef = playerRef;
        fromFile("Pages/EllieAU_HyUI_Placeholder.ui");
    }

    public HudBuilder() {
        this.playerRef = null;
        fromFile("Pages/EllieAU_HyUI_Placeholder.ui");
    }

    /**
     * Sets the refresh rate for the HUD in milliseconds.
     * If set to 0 (default), the HUD will not refresh periodically.
     *
     * @param ms The refresh rate in milliseconds.
     * @return The HudBuilder instance.
     */
    public HudBuilder withRefreshRate(long ms) {
        this.refreshRateMs = ms;
        return this;
    }

    /**
     * Registers a callback to be triggered when the HUD is refreshed.
     *
     * @param listener The listener callback.
     * @return The HudBuilder instance.
     */
    public HudBuilder onRefresh(Consumer<HyUIHud> listener) {
        this.refreshListener = listener;
        return this;
    }

    /**
     * Factory method to create a detached HUD builder that does not reference a player.
     * @return  the created HudBuilder instance.
     */
    public static HudBuilder detachedHud() {
        return new HudBuilder();
    }

    /**
     * Factory method to create a HUD builder for a specific player.
     * 
     * @param ref The player reference for whom the HUD should be created.
     * @return  the created HudBuilder instance.
     */
    public static HudBuilder hudForPlayer(PlayerRef ref) {
        return new HudBuilder(ref);
    }

    /**
     * Shows the HUD for the already existing player reference.
     * The playerRef must already be set, using {@code HudBuilder.hudForPlayer(PlayerRef ref)}
     * 
     * @param store The entity store containing player data.
     * @return The created HyUIHud instance.
     */
    public HyUIHud show() {
        assert playerRef != null : "Player reference cannot be null.";
        return show(playerRef);
    }

    /**
     * Deprecated in favor of {@link HudBuilder#show()}
     */
    @Deprecated
    public HyUIHud show(Store<EntityStore> store) {
        return this.show();
    }

    /**
     * Shows the HUD for the specified player using the provided entity store.
     * This also adds and manages multiple HUDs, there is no need to check the active HUD.
     * 
     * @param playerRef The player reference for whom the HUD should be shown.
     * @return The created HyUIHud instance.
     */
    public HyUIHud show(@Nonnull PlayerRef playerRef) {
        String name = "HYUIHUD" + System.currentTimeMillis();
        
        this.lastHud = new HyUIHud(name, playerRef, uiFile, getTopLevelElements(), editCallbacks, templateHtml, templateProcessor, runtimeTemplateUpdatesEnabled);
        this.lastHud.setRefreshRateMs(refreshRateMs);
        this.lastHud.setRefreshListener(refreshListener);
        HyUIPlugin.getLog().logInfo("Adding to a MultiHud: " + name);

        // Show it.
        this.lastHud.add();
        
        return this.lastHud;
    }

    /**
     * Deprecated in favor of {@link HudBuilder#show(PlayerRef)}
     */
    @Deprecated
    public HyUIHud show(@Nonnull PlayerRef playerRef, Store<EntityStore> store) {
        return this.show(playerRef);
    }

    /**
     * You can update an existing HUD with this builder.
     * @param hudRef The HyUIHud instance to update.
     */
    public void updateExisting(@Nonnull HyUIHud hudRef) {
        this.lastHud = hudRef;
        hudRef.update(this);
    }

    /**
     * Retrieves the list of logged UI commands from the last shown HUD.
     * @return A list of strings representing the logged commands, or an empty list if no HUD has been shown.
     */
    public List<String> getCommandLog() {
        if (lastHud == null) {
            return List.of();
        }
        return lastHud.getCommandLog();
    }
}
