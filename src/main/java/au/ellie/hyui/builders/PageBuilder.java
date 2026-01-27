package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.html.HtmlParser;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A convenient builder to help you construct a custom Page for Hytale and open it.
 */
public class PageBuilder extends InterfaceBuilder<PageBuilder> {
    private final PlayerRef playerRef;
    private CustomPageLifetime lifetime = CustomPageLifetime.CanDismiss;
    private HyUIPage lastPage;

    /**
     * Constructs a new instance of the PageBuilder class.
     *
     * @param playerRef The reference to the player for whom the page is being built.
     */
    public PageBuilder(PlayerRef playerRef) {
        this.playerRef = playerRef;
        // No matter what happens, we need at least an empty UI file to begin with.
        fromFile("Pages/EllieAU_HyUI_Placeholder.ui");
    }
    
    /**
     * Constructs a new instance of the PageBuilder class without dependency on player.
     */
    public PageBuilder() {
        this.playerRef = null;
        fromFile("Pages/EllieAU_HyUI_Placeholder.ui");
    }

    /**
     * Factory method to create a new instance of the {@code PageBuilder} class.
     * This does not depend on any player.
     * You must use {@code PageBuilder.open(PlayerRef, Store<ECS>)} to open it.
     *
     * @return A new instance of the {@code PageBuilder}.
     */
    public static PageBuilder detachedPage() {
        return new PageBuilder();
    }

    /**
     * Factory method to create a new instance of the {@code PageBuilder} class.
     *
     * @return A new instance of the {@code PageBuilder}.
     */
    public static PageBuilder pageForPlayer(PlayerRef ref) {
        return new PageBuilder(ref);
    }

    /**
     * Sets the lifetime of the page to the specified CustomPageLifetime value.
     *
     * @param lifetime The lifecycle definition for the page, which determines
     *                 how the page behaves and when it is dismissed.
     * @return The current instance of the PageBuilder to allow for method chaining.
     */
    public PageBuilder withLifetime(CustomPageLifetime lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    /**
     * Opens a custom UI page for the associated player using the provided store.
     * This method retrieves the player's page manager and creates a new instance
     * of the HyUIPage based on the specified parameters and fields defined in the
     * class, then instructs the page manager to open the page.
     *
     * @param store The store containing the entity data required to configure and display the page.
     * @return The created HyUIPage instance.
     */
    public HyUIPage open(Store<EntityStore> store) {
        assert playerRef != null : "Player reference cannot be null. Use override for open(Store<ECS>) if reusing this builder.";
        Player playerComponent = store.getComponent(playerRef.getReference(), Player.getComponentType());
        sendDynamicImageIfNeeded(playerRef);
        PageManager pageManager = playerComponent.getPageManager();
        this.lastPage = new HyUIPage(playerRef, lifetime, uiFile, getTopLevelElements(), editCallbacks, templateHtml, templateProcessor, runtimeTemplateUpdatesEnabled);
        pageManager.openCustomPage(playerRef.getReference(), store, this.lastPage);
        return this.lastPage;
    }

    /**
     * Opens a custom UI page for the associated player using the provided store and player reference.
     * This method retrieves the player's page manager and creates a new instance
     * of the HyUIPage based on the specified parameters and fields defined in the
     * class, then instructs the page manager to open the page.
     *
     * @param playerRefParam The player reference for whom the page is being opened.
     * @param store The store containing the entity data required to configure and display the page.
     * @return The created HyUIPage instance.
     */
    public HyUIPage open(@Nonnull PlayerRef playerRefParam, Store<EntityStore> store) {
        Player playerComponent = store.getComponent(playerRefParam.getReference(), Player.getComponentType());
        sendDynamicImageIfNeeded(playerRefParam);
        PageManager pageManager = playerComponent.getPageManager();
        this.lastPage = new HyUIPage(playerRefParam, lifetime, uiFile, getTopLevelElements(), editCallbacks, templateHtml, templateProcessor, runtimeTemplateUpdatesEnabled);
        pageManager.openCustomPage(playerRefParam.getReference(), store, this.lastPage);
        return this.lastPage;
    }

    /**
     * Retrieves the list of logged UI commands from the last opened page.
     * @return A list of strings representing the logged commands, or an empty list if no page has been opened.
     */
    public List<String> getCommandLog() {
        if (lastPage == null) {
            return List.of();
        }
        return lastPage.getCommandLog();
    }

    /**
     * Reloads a dynamic image by its element ID on the last opened page.
     *
     * @param id The ID of the dynamic image element.
     * @param shouldClearPage Whether to clear the page after reloading the image.
     */
    public void reloadImage(String id, boolean shouldClearPage) {
        if (lastPage != null) {
            lastPage.reloadImage(id, shouldClearPage);
        }
    }
}
