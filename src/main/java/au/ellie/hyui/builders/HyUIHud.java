package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.html.TemplateProcessor;
import au.ellie.hyui.utils.MultiHudWrapper;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;
import java.util.ArrayList;
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

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> refreshTask;
    
    public HyUIHud(String name, PlayerRef playerRef, 
                   String uiFile,
                   List<UIElementBuilder<?>> elements,
                   List<Consumer<UICommandBuilder>> editCallbacks,
                   String templateHtml,
                   TemplateProcessor templateProcessor,
                   boolean runtimeTemplateUpdatesEnabled) {
        super(playerRef);
        this.name = name;
        this.delegate = new HyUInterface(uiFile, elements, editCallbacks, templateHtml, templateProcessor, runtimeTemplateUpdatesEnabled) {};
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
        if (!playerRef.isValid()) {
            HyUIPlugin.getLog().logInfo("Player is invalid, cancelling refresh task for HUD.");
            
            // Player is no longer valid, cancel task and cleanup.
            if (refreshTask != null) {
                HyUIPlugin.getLog().logInfo("Player is invalid, cancelling refresh task for HUD.");
                refreshTask.cancel(false);
            }
            return;
        }

        if (playerRef.getReference() == null) {
            return; // This might happen during world changes.
        }

        long now = System.currentTimeMillis();
        long rate = getRefreshRateMs();

        if (rate > 0) {
            if (now - lastRefreshTime >= rate) {
                triggerRefresh();
                refreshOrRerender(true, false);
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

    @Override
    public Optional<UIElementBuilder<?>> getByIdRaw(String id) {
        return delegate.getById(id);
    }

    /**
     * Updates the HUD with the provided builder.
     * The builder can be a completely new configuration.
     * 
     * @param updatedHudBuilder The builder containing updated HUD configuration.
     */
    public void update(HudBuilder updatedHudBuilder) {
        UICommandBuilder builder = configureFrom(updatedHudBuilder);
        refreshOrRerender(true, false);
    }

    /**
     * Remove the HUD from its parent multi-HUD. 
     * This will remove it from the screen for the player.
     * and stop refreshing it.
     * 
     * You can later associate it with another, or the same multi-HUD and show it.
     */
    public void remove() {
        var store = getStore();
        if (store == null) return;

        store.getExternalData().getWorld().execute(() -> {
            var player = getPlayer();
            if (player == null) return;

            MultiHudWrapper.hideCustomHud(player, getPlayerRef(), this.name);
        });
        HyUIPlugin.getLog().logInfo("HUD removed: " + this.name);
        refreshTask.cancel(false);
    }

    /**
     * Remove the HUD from its parent multi-HUD. This does NOT check thread access.
     * This will remove it from the screen for the player.
     * and stop refreshing it.
     *
     * You can later associate it with another, or the same multi-HUD and show it.
     */
    public void removeUnsafe() {
        var player = getPlayer();
        if (player == null) return;

        MultiHudWrapper.hideCustomHud(player, getPlayerRef(), this.name);
        HyUIPlugin.getLog().logInfo("HUD removed: " + this.name);
        refreshTask.cancel(false);
    }

    /**
     * Adds the HUD to its parent multi-HUD.
     * Begins refresh task.
     * 
     */
    public void add() {
        this.safeAdd();
        if (refreshTask != null && !refreshTask.isCancelled()) {
            refreshTask.cancel(false);
        }
        HyUIPlugin.getLog().logInfo("HUD added: " + this.name);
        startRefreshTask();
    }
    
    /**
     * Adds the HUD to its parent multi-HUD. This does NOT check thread access.
     * Begins refresh task.
     *
     */
    public void addUnsafe() {
        var player = getPlayer();
        if (player == null) return;

        MultiHudWrapper.setCustomHud(player, getPlayerRef(), this.name, this);
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
        setVisibilityOnFirstElement(false, false);
    }

    /**
     * Hides the UI from view of player. This does NOT check thread access.
     */
    public void hideUnsafe() {
        setVisibilityOnFirstElement(false, true);
    }
    
    /**
     * Shows the UI to the player if it has previously been hidden.
     */
    public void unhide() {
        setVisibilityOnFirstElement(true, false);
    }

    /**
     * Shows the UI to the player if it has previously been hidden. This does NOT check thread access.
     */
    public void unhideUnsafe() {
        setVisibilityOnFirstElement(true, true);
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

    public void refreshOrRerender(boolean shouldRerender, boolean unsafe) {
        if (!shouldRerender) {
            UICommandBuilder uiCommandBuilder = new UICommandBuilder();
            delegate.buildFromCommandBuilder(uiCommandBuilder, true);
            this.update(false, uiCommandBuilder);
        } else {
            // Re-render completely.
            if (!unsafe) {
                this.safeAdd();
            } else {
                var player = getPlayer();
                if (player == null) return;

                MultiHudWrapper.setCustomHud(player, getPlayerRef(), this.name, this);
            }

        }
    }
    
    @Override
    public List<String> getCommandLog() {
        return delegate.getCommandLog();
    }

    @Override
    public Optional<Object> getValue(String id) {
        return delegate.getValue(id);
    }

    @Override
    public Optional<HyUIPage> getPage() {
        return Optional.empty();
    }

    /**
     * Not implemented.
     * @param shouldClear Not implemented.
     */
    @Override
    public void updatePage(boolean shouldClear) {}
    
    private void setVisibilityOnFirstElement(boolean value, boolean unsafe) {
        for (UIElementBuilder<?> element : delegate.getElements()) {
            element.withVisible(value);
            break;
        }
       
        HyUIPlugin.getLog().logInfo("REDRAW: HUD SET VISIBILITY from single hud");
        this.refreshOrRerender(false, unsafe);
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

    private void safeAdd() {
        var store = getStore();
        if (store == null) return;

        store.getExternalData().getWorld().execute(() -> {
            var player = getPlayer();
            if (player == null) return;

            MultiHudWrapper.setCustomHud(player, getPlayerRef(), this.name, this);
        });
    }

    private Store<EntityStore> getStore() {
        var playerRef = getPlayerRef();
        if (!playerRef.isValid()) {
            return null;
        }
        var playerReference = playerRef.getReference();
        if (playerReference == null || !playerReference.isValid()) {
            return null;
        }
        return playerReference.getStore();
    }

    private Player getPlayer() {
        var playerRef = getPlayerRef();
        if (!playerRef.isValid()) {
            return null;
        }
        var playerRefRef = playerRef.getReference();
        if (playerRefRef == null || !playerRefRef.isValid()) {
            return null;
        }
        var store = playerRefRef.getStore();
        return store.getComponent(playerRefRef, Player.getComponentType());
    }

    /**
     * Reloads a dynamic image by its element ID. This will forcibly invalidate the image 
     * and re-download (cache still applies to all downloads for 15 seconds!).
     *
     * @param dynamicImageElementId The ID of the dynamic image element.
     */
    public void reloadImage(String dynamicImageElementId) {
        var playerRefInternal = getPlayerRef();
        Ref<EntityStore> ref = playerRefInternal.getReference();
        if (ref == null || !ref.isValid()) {
            return;
        }
        getById(dynamicImageElementId, DynamicImageBuilder.class).ifPresent(dynamicImage -> {
            dynamicImage.invalidateImage(playerRefInternal.getUuid());
            InterfaceBuilder.sendDynamicImage(playerRefInternal, dynamicImage);
            updatePage(true);
        });
    }
    
    // TODO: HUD release images.
}
