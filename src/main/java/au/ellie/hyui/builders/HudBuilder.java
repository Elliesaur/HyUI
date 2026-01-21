package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class HudBuilder extends InterfaceBuilder<HudBuilder> {
    private final PlayerRef playerRef;
    private long refreshRateMs = 0;
    private Consumer<HyUIHud> refreshListener;
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
    public HyUIHud show(Store<EntityStore> store) {
        assert playerRef != null : "Player reference cannot be null.";
        return show(playerRef, store);
    }

    /**
     * Shows the HUD for the specified player using the provided entity store.
     * This also adds and manages multiple HUDs, there is no need to check the active HUD.
     * 
     * @param playerRefParam The player reference for whom the HUD should be shown.
     * @param store The entity store containing player data.
     * @return The created HyUIHud instance.
     */
    public HyUIHud show(@Nonnull PlayerRef playerRefParam, Store<EntityStore> store) {
        Player playerComponent = store.getComponent(playerRefParam.getReference(), Player.getComponentType());
        String name = "HYUIHUD" + System.currentTimeMillis();
        
        var hyUIHud = new HyUIHud(name, playerRefParam, store, uiFile, getTopLevelElements(), editCallbacks);
        hyUIHud.setRefreshRateMs(refreshRateMs);
        hyUIHud.setRefreshListener(refreshListener);
        HyUIPlugin.getLog().logInfo("Adding to a MultiHud: " + name);

        // Show it.
        hyUIHud.add();
        
        return hyUIHud;
    }
    
    /**
     * You can update an existing HUD with this builder.
     * @param hudRef The HyUIHud instance to update.
     */
    public void updateExisting(@Nonnull HyUIHud hudRef) {
        hudRef.update(this);
    }
}
