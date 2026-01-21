package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.utils.MultiHudWrapper;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A HUD for Hytale. 
 * It is important to store references to your existing HUDs to assist with updating elements.
 */
public class HyUIHud extends CustomUIHud implements UIContext {
    public String name;
    protected final HyUInterface delegate;
    private boolean isHidden;
    private long refreshRateMs;
    private long lastRefreshTime;
    private Consumer<HyUIHud> refreshListener;
    private final Store<EntityStore> store;

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> refreshTask;
    
    public HyUIHud(String name, PlayerRef playerRef, 
                   Store<EntityStore> store, 
                   String uiFile,
                   List<UIElementBuilder<?>> elements,
                   List<Consumer<UICommandBuilder>> editCallbacks) {
        super(playerRef);
        this.name = name;
        this.store = store;
        this.delegate = new HyUInterface(uiFile, elements, editCallbacks) {};
    }

    private void startRefreshTask() {
        if (refreshTask == null || refreshTask.isCancelled()) {
            refreshTask = scheduler.scheduleAtFixedRate(
                    this::checkRefreshes, 
                    100, 
                    100, 
                    TimeUnit.MILLISECONDS);
        }
    }
    
    private void checkRefreshes() {
        if (isHidden) {
            HyUIPlugin.getLog().logInfo("Hidden HUD. Not refreshing.");
            return;
        }
        
        PlayerRef playerRef = getPlayerRef();
        if (playerRef.getReference() == null) {
            HyUIPlugin.getLog().logInfo("Player is invalid, cancelling refresh task for HUD.");
            
            // Player is no longer valid, cancel task and cleanup.
            if (refreshTask != null) {
                HyUIPlugin.getLog().logInfo("Player is invalid, cancelling refresh task for HUD.");
                refreshTask.cancel(false);
            }
            return;
        }

        long now = System.currentTimeMillis();
        long rate = getRefreshRateMs();

        if (rate > 0) {
            if (now - lastRefreshTime >= rate) {
                triggerRefresh();
                refreshOrRerender(true);
                lastRefreshTime = now;
            }
        }
    }
    
    @Override
    public void build(UICommandBuilder uiCommandBuilder) {
        delegate.buildFromCommandBuilder(uiCommandBuilder);
    }

    /**
     * Retrieves an element builder by its ID.
     *
     * @param id The ID of the element.
     * @return An Optional containing the element builder, or empty if not found.
     */
    public Optional<UIElementBuilder<?>> getById(String id) {
        return delegate.getById(id);
    }

    /**
     * Retrieves an element builder by its ID, cast to the specified type.
     *
     * @param id    The ID of the element.
     * @param clazz The class of the type to cast to.
     * @param <E>   The expected type of the element builder.
     * @return An Optional containing the cast element builder, or empty if not found or if casting fails.
     */
    public <E extends UIElementBuilder<E>> Optional<E> getById(String id, Class<E> clazz) {
        return delegate.getById(id, clazz);
    }

    /**
     * Updates the HUD with the provided builder.
     * The builder can be a completely new configuration.
     * 
     * @param updatedHudBuilder The builder containing updated HUD configuration.
     */
    public void update(HudBuilder updatedHudBuilder) {
        UICommandBuilder builder = configureFrom(updatedHudBuilder);
        refreshOrRerender(true);
    }

    /**
     * Remove the HUD from its parent multi-HUD. 
     * This will remove it from the screen for the player.
     * and stop refreshing it.
     * 
     * You can later associate it with another, or the same multi-HUD and show it.
     */
    public void remove() {
        store.getExternalData().getWorld().execute(() -> {
            MultiHudWrapper.hideCustomHud(getPlayer(), getPlayerRef(), this.name);
        });
        HyUIPlugin.getLog().logInfo("HUD removed: " + this.name);
        refreshTask.cancel(false);
    }

    /**
     * Adds the HUD to its parent multi-HUD.
     * Begins refresh task.
     * 
     */
    public void add() {
        store.getExternalData().getWorld().execute(() -> {
            MultiHudWrapper.setCustomHud(getPlayer(), getPlayerRef(), this.name, this);
        });
        if (refreshTask != null && !refreshTask.isCancelled()) {
            refreshTask.cancel(false);
        }
        HyUIPlugin.getLog().logInfo("HUD added: " + this.name);
        startRefreshTask();
    }

    /**
     * Hides the UI from view of player.
     */
    public void hide() {
        setVisibilityOnFirstElement(false);
    }

    /**
     * Shows the UI to the player if it has previously been hidden.
     */
    public void unhide() {
        setVisibilityOnFirstElement(true);
    }

    public long getRefreshRateMs() {
        return refreshRateMs;
    }

    public void setRefreshRateMs(long refreshRateMs) {
        this.refreshRateMs = refreshRateMs;
    }

    public void setRefreshListener(Consumer<HyUIHud> refreshListener) {
        this.refreshListener = refreshListener;
    }

    /**
     * Triggers the refresh listener if it exists.
     */
    public void triggerRefresh() {
        if (refreshListener != null) {
            refreshListener.accept(this);
        }
    }

    public void refreshOrRerender(boolean shouldRerender) {
        if (!shouldRerender) {
            UICommandBuilder uiCommandBuilder = new UICommandBuilder();
            this.build(uiCommandBuilder);
            this.update(false, uiCommandBuilder);
        } else {
            // Re-render completely.
            store.getExternalData().getWorld().execute(() -> {
                MultiHudWrapper.setCustomHud(getPlayer(), getPlayerRef(), this.name, this);
            });
        }
    }
    
    @Override
    public Optional<Object> getValue(String id) {
        return delegate.getValue(id);
    }

    private void setVisibilityOnFirstElement(boolean value) {
        for (UIElementBuilder<?> element : delegate.getElements()) {
            element.withVisible(value);
            break;
        }
       
        HyUIPlugin.getLog().logInfo("REDRAW: HUD SET VISIBILITY from single hud");
        this.refreshOrRerender(false);
        // this.update(false, builder);
        isHidden = !isHidden;
    }

    private UICommandBuilder configureFrom(HudBuilder updatedHudBuilder) {
        UICommandBuilder builder = new UICommandBuilder();
        delegate.setEditCallbacks(updatedHudBuilder.editCallbacks);
        delegate.setElements(updatedHudBuilder.getTopLevelElements());
        delegate.setUiFile(updatedHudBuilder.uiFile);
        return builder;
    }
    
    @Nullable
    private Player getPlayer() {
        var playerRef = getPlayerRef();
        if (playerRef == null || !playerRef.isValid()) {
            return null;
        }
        var playerRefRef = playerRef.getReference();
        if (playerRefRef == null || !playerRefRef.isValid()) {
            return null;
        }
        return store.getComponent(playerRefRef, Player.getComponentType());
    }
}
