package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.events.DynamicPageData;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventActions;
import au.ellie.hyui.events.UIEventListener;
import au.ellie.hyui.html.TemplateProcessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPage;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

public class HyUIPage extends InteractiveCustomUIPage<DynamicPageData> implements UIContext {
    private final HyUInterface delegate;

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
        super.close();
        HyUIPlugin.getLog().logInfo("Page closed!");
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
        Ref<EntityStore> ref = this.playerRef.getReference();
        if (ref == null || !ref.isValid()) {
            return;
        }
        getById(dynamicImageElementId, DynamicImageBuilder.class).ifPresent(dynamicImage -> {
            dynamicImage.invalidateImage(playerRef.getUuid());
            InterfaceBuilder.sendDynamicImage(playerRef, dynamicImage);
            updatePage(shouldClearPage);
        });
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        HyUIPlugin.getLog().logInfo("Page dismissed!");
        delegate.releaseDynamicImages(playerRef.getUuid());
    }
    
    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        delegate.build(ref, uiCommandBuilder, uiEventBuilder, store);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull DynamicPageData data) {
        super.handleDataEvent(ref, store, data);
        delegate.handleDataEventInternal(data, this);
    }
}
