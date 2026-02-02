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

package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.events.DynamicPageData;
import au.ellie.hyui.events.PageRefreshResult;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.html.TemplateProcessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPage;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class HyUIPage extends InteractiveCustomUIPage<DynamicPageData> implements UIContext {
    private final HyUInterface delegate;
    private long refreshRateMs;
    private long lastRefreshTime;
    private Function<HyUIPage, PageRefreshResult> refreshListener;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> refreshTask;

    public HyUIPage(PlayerRef playerRef,
                    CustomPageLifetime lifetime,
                    String uiFile,
                    List<UIElementBuilder<?>> elements,
                    List<Consumer<UICommandBuilder>> editCallbacks,
                    String templateHtml,
                    TemplateProcessor templateProcessor,
                    boolean runtimeTemplateUpdatesEnabled) {
        super(playerRef, lifetime, DynamicPageData.CODEC);
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

    private void stopRefreshTask() {
        if (refreshTask != null) {
            refreshTask.cancel(false);
        }
    }

    private void checkRefreshes() {
        try {
            PlayerRef playerRef = this.playerRef;
            if (!playerRef.isValid()) {
                stopRefreshTask();
                return;
            }

            Ref<EntityStore> ref = playerRef.getReference();
            if (ref == null || !ref.isValid()) {
                return; // This might happen during world changes.
            }

            long now = System.currentTimeMillis();
            long rate = getRefreshRateMs();

            if (rate > 0 && now - lastRefreshTime >= rate) {
                PageRefreshResult result = triggerRefresh();
                if (result != PageRefreshResult.NONE) {
                    boolean shouldClear = result == PageRefreshResult.UPDATE_CLEAR;
                    ref.getStore().getExternalData().getWorld().execute(() -> updatePage(shouldClear));
                }
                lastRefreshTime = now;
            }
        } catch (Exception e) {
            HyUIPlugin.getLog().logFinest("Refresh task failed: " + e.getMessage());
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
        return Optional.of(this);
    }
    
    public void close() {
        stopRefreshTask();
        super.close();
        HyUIPlugin.getLog().logFinest("Page closed!");
        delegate.releaseDynamicImages(playerRef.getUuid());
    }
    
    @Override
    public void updatePage(boolean shouldClear) {
        Ref<EntityStore> ref = this.playerRef.getReference();
        if (ref != null) {
            Store<EntityStore> store = ref.getStore();
            Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());
            UICommandBuilder commandBuilder = new UICommandBuilder();
            UIEventBuilder eventBuilder = new UIEventBuilder();
            delegate.build(ref, commandBuilder, eventBuilder, ref.getStore(), !shouldClear);
            playerComponent.getPageManager().updateCustomPage(new CustomPage(this.getClass().getName(), false, shouldClear, this.lifetime, commandBuilder.getCommands(), eventBuilder.getEvents()));
        }
    }

    public long getRefreshRateMs() {
        return refreshRateMs;
    }

    public void setRefreshRateMs(long refreshRateMs) {
        this.refreshRateMs = refreshRateMs;
    }

    public void setRefreshListener(Function<HyUIPage, PageRefreshResult> refreshListener) {
        this.refreshListener = refreshListener;
    }

    /**
     * Triggers the refresh listener if it exists.
     */
    public PageRefreshResult triggerRefresh() {
        if (refreshListener != null) {
            PageRefreshResult result = refreshListener.apply(this);
            return result == null ? PageRefreshResult.NONE : result;
        }
        return PageRefreshResult.NONE;
    }

    @Override
    public <E extends UIElementBuilder<E>> Optional<E> getById(String id, Class<E> clazz) {
        return delegate.getById(id, clazz);
    }

    @Override
    public Optional<UIElementBuilder<?>> getByIdRaw(String id) {
        return delegate.getById(id);
    }

    /**
     * Reloads a dynamic image by its element ID. This will forcibly invalidate the image 
     * and re-download (cache still applies to all downloads for 15 seconds!).
     * 
     * @param dynamicImageElementId The ID of the dynamic image element.
     * @param shouldClearPage Whether to clear the page after reloading the image.
     */
    public void reloadImage(String dynamicImageElementId, boolean shouldClearPage) {
        reloadImage(dynamicImageElementId, shouldClearPage, true);
    }

    /**
     * Reloads a dynamic image by its element ID.
     *
     * @param dynamicImageElementId The ID of the dynamic image element.
     * @param shouldClearPage Whether to clear the page after reloading the image.
     * @param forceDownload Whether to force a re-download of the image.
     */
    public void reloadImage(String dynamicImageElementId, boolean shouldClearPage, boolean forceDownload) {
        Ref<EntityStore> ref = this.playerRef.getReference();
        if (ref == null || !ref.isValid()) {
            return;
        }
        getById(dynamicImageElementId, DynamicImageBuilder.class).ifPresent(dynamicImage -> {
            if (forceDownload) {
                dynamicImage.invalidateImage(playerRef.getUuid());
                InterfaceBuilder.sendDynamicImage(playerRef, dynamicImage);
                updatePage(shouldClearPage);
                return;
            }
            if (dynamicImage.isImagePathAssigned(playerRef.getUuid())) {
                updatePage(shouldClearPage);
            }
        });
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        stopRefreshTask();
        HyUIPlugin.getLog().logFinest("Page dismissed!");
        delegate.releaseDynamicImages(playerRef.getUuid());
    }
    
    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        startRefreshTask();
        delegate.build(ref, uiCommandBuilder, uiEventBuilder, store);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull DynamicPageData data) {
        super.handleDataEvent(ref, store, data);
        delegate.handleDataEventInternal(data, this);
    }

    /*@Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull String data) {
        super.handleDataEvent(ref, store, data);
        HyUIPlugin.getLog().logInfo("Handling data event: " + data);
    }*/
}
