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
public class PageBuilder {
    private final PlayerRef playerRef;
    private CustomPageLifetime lifetime = CustomPageLifetime.CanDismiss;
    private final Map<String, UIElementBuilder<?>> elementRegistry = new LinkedHashMap<>();
    private final List<Consumer<UICommandBuilder>> editCallbacks = new ArrayList<>();
    private String uiFile;

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
     * Specifies the (html) HYUIML contents to parse to HyUI PageBuilder.
     * 
     * @param html
     * @return
     */
    public PageBuilder fromHtml(String html) {
        new HtmlParser().parseToPage(this, html);
        return this;
    }
    /**
     * Adds a UI element to the page being built.
     *
     * @param element The UIElementBuilder instance representing the UI element to add.
     * @return The current instance of the PageBuilder to allow for method chaining.
     */
    public PageBuilder addElement(UIElementBuilder<?> element) {
        // Elements are always added to the #HyUIRoot group in the placeholder.
        element.inside("#HyUIRoot");
        registerElement(element);
        return this;
    }

    private void registerElement(UIElementBuilder<?> element) {
        if (element.getId() != null) {
            this.elementRegistry.put(element.getId(), element);
        }
        for (UIElementBuilder<?> child : element.children) {
            registerElement(child);
        }
    }

    /**
     * Retrieves an element builder by its ID.
     *
     * @param id    The ID of the element to retrieve.
     * @param clazz The class of the builder to cast to.
     * @param <T>   The type of the builder.
     * @return An Optional containing the builder if found and of the correct type, otherwise empty.
     */
    public <T extends UIElementBuilder<T>> Optional<T> getById(String id, Class<T> clazz) {
        UIElementBuilder<?> builder = elementRegistry.get(id);
        if (builder != null && clazz.isInstance(builder)) {
            return Optional.of(clazz.cast(builder));
        }
        return Optional.empty();
    }

    /**
     * Adds an event listener to an element by its ID.
     * If no ID is found it will throw an IllegalArgumentException.
     * 
     * @param id       The ID of the element.
     * @param type     The event type.
     * @param valueClass The class of the value expected in the callback.
     * @param callback The callback function.
     * @param <V>      The type of the value.
     * @return The current PageBuilder instance.
     */
    public <V> PageBuilder addEventListener(String id, CustomUIEventBindingType type, Class<V> valueClass, Consumer<V> callback) {
        UIElementBuilder<?> element = elementRegistry.get(id);
        if (element == null) {
            throw new IllegalArgumentException("No element found with ID '" + id + "'. Please check your UI builder for elements with this ID to see if you misspelt the name.");
        }
        element.addEventListener(type, valueClass, callback);
        return this;
    }

    /**
     * Adds an event listener to an element by its ID with a default Object type.
     * If no ID is found it will throw an IllegalArgumentException.
     * 
     * @param id       The ID of the element.
     * @param type     The event type.
     * @param callback The callback function.
     * @return The current PageBuilder instance.
     */
    public PageBuilder addEventListener(String id, CustomUIEventBindingType type, Consumer<Object> callback) {
        return addEventListener(id, type, Object.class, callback);
    }

    /**
     * Adds an event listener with context to an element by its ID.
     * If no ID is found it will throw an IllegalArgumentException.
     * 
     * @param id       The ID of the element.
     * @param type     The event type.
     * @param valueClass The class of the value expected in the callback.
     * @param callback The callback function.
     * @param <V>      The type of the value.
     * @return The current PageBuilder instance.
     */
    public <V> PageBuilder addEventListener(String id, CustomUIEventBindingType type, Class<V> valueClass, BiConsumer<V, UIContext> callback) {
        UIElementBuilder<?> element = elementRegistry.get(id);
        if (element == null) {
            throw new IllegalArgumentException("No element found with ID '" + id + "'. Please check your UI builder for elements with this ID to see if you misspelt the name.");
        }
        element.addEventListenerWithContext(type, valueClass, callback);
        return this;
    }

    /**
     * Adds an event listener with context and default Object type to an element by its ID.
     * If no ID is found it will throw an IllegalArgumentException.
     * 
     * @param id       The ID of the element.
     * @param type     The event type.
     * @param callback The callback function.
     * @return The current PageBuilder instance.
     */
    public PageBuilder addEventListener(String id, CustomUIEventBindingType type, BiConsumer<Object, UIContext> callback) {
        return addEventListener(id, type, Object.class, callback);
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

    private List<UIElementBuilder<?>> getTopLevelElements() {
        List<UIElementBuilder<?>> topLevel = new ArrayList<>();
        for (UIElementBuilder<?> element : elementRegistry.values()) {
            if ("#HyUIRoot".equals(element.parentSelector)) {
                topLevel.add(element);
            }
        }
        return topLevel;
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
        pageManager.openCustomPage(playerRef.getReference(), store, new HyUIPage(playerRef, lifetime, uiFile, getTopLevelElements(), editCallbacks));
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
        pageManager.openCustomPage(playerRefParam.getReference(), store, new HyUIPage(playerRefParam, lifetime, uiFile, getTopLevelElements(), editCallbacks));
    }
    
}
