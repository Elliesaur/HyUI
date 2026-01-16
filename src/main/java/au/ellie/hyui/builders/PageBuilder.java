package au.ellie.hyui.builders;

import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A convenient builder to help you construct a custom Page for Hytale and open it.
 */
public class PageBuilder {
    private final PlayerRef playerRef;
    private CustomPageLifetime lifetime = CustomPageLifetime.CanDismiss;
    private final List<UIElementBuilder<?>> elements = new ArrayList<>();
    private final List<Consumer<UICommandBuilder>> editCallbacks = new ArrayList<>();
    private String uiFile;

    /**
     * Constructs a new instance of the PageBuilder class.
     *
     * @param playerRef The reference to the player for whom the page is being built.
     */
    public PageBuilder(PlayerRef playerRef) {
        this.playerRef = playerRef;
    }
    
    /**
     * Constructs a new instance of the PageBuilder class without dependency on player.
     */
    public PageBuilder() {
        this.playerRef = null;
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
     * Specifies the path to a UI file that will be used as a placeholder/base by the page.
     *
     * @param uiFile The file path of the UI layout to load as placeholder/base.
     * @return The current instance of the PageBuilder to allow for method chaining.
     */
    public PageBuilder fromFile(String uiFile) {
        this.uiFile = uiFile;
        return this;
    }

    /**
     * Adds a UI element to the page being built.
     *
     * @param element The UIElementBuilder instance representing the UI element to add.
     * @return The current instance of the PageBuilder to allow for method chaining.
     */
    public PageBuilder addElement(UIElementBuilder<?> element) {
        this.elements.add(element);
        return this;
    }

    /**
     * Adds a callback function that can be used to modify an existing placeholder UI element
     * via a provided {@link UICommandBuilder}.
     * 
     * You should use editBeforeElement/editAfterElement on builders if you want to edit
     * the HyUI elements before or after creation.
     *
     * @param callback A Consumer that accepts a {@code UICommandBuilder} instance
     *                 and defines the modifications to be made to the UI element.
     * @return The current instance of the PageBuilder to allow for method chaining.
     */
    public PageBuilder editElement(Consumer<UICommandBuilder> callback) {
        this.editCallbacks.add(callback);
        return this;
    }

    /**
     * Opens a custom UI page for the associated player using the provided store.
     * This method retrieves the player's page manager and creates a new instance
     * of the HyUIPage based on the specified parameters and fields defined in the
     * class, then instructs the page manager to open the page.
     *
     * @param store The store containing the entity data required to configure and display the page.
     */
    public void open(Store<EntityStore> store) {
        assert playerRef != null : "Player reference cannot be null. Use override for open(Store<ECS>) if reusing this builder.";
        Player playerComponent = store.getComponent(playerRef.getReference(), Player.getComponentType());
        PageManager pageManager = playerComponent.getPageManager();
        pageManager.openCustomPage(playerRef.getReference(), store, new HyUIPage(playerRef, lifetime, uiFile, elements, editCallbacks));
    }

    /**
     * Opens a custom UI page for the associated player using the provided store and player reference.
     * This method retrieves the player's page manager and creates a new instance
     * of the HyUIPage based on the specified parameters and fields defined in the
     * class, then instructs the page manager to open the page.
     *
     * @param playerRefParam The player reference for whom the page is being opened.
     * @param store The store containing the entity data required to configure and display the page.
     */
    public void open(@Nonnull PlayerRef playerRefParam, Store<EntityStore> store) {
        Player playerComponent = store.getComponent(playerRefParam.getReference(), Player.getComponentType());
        PageManager pageManager = playerComponent.getPageManager();
        pageManager.openCustomPage(playerRefParam.getReference(), store, new HyUIPage(playerRefParam, lifetime, uiFile, elements, editCallbacks));
    }
}
