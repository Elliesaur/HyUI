package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.events.DynamicPageData;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventActions;
import au.ellie.hyui.events.UIEventListener;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class HyUIPage extends InteractiveCustomUIPage<DynamicPageData> implements UIContext {
    private final String uiFile;
    private final List<UIElementBuilder<?>> elements;
    private final List<Consumer<UICommandBuilder>> editCallbacks;
    private final Map<String, Object> elementValues = new HashMap<>();

    /**
     * Creates a new HyUIPage.
     *
     * @param playerRef     The player this page is for.
     * @param lifetime      The lifetime of the page.
     * @param baseUiFile    The base UI file to use (e.g. "Pages/Placeholder.ui").
     * @param elements      The elements to add to the page.
     */
    public HyUIPage(PlayerRef playerRef, CustomPageLifetime lifetime, String baseUiFile, List<UIElementBuilder<?>> elements) {
        this(playerRef, lifetime, baseUiFile, elements, null);
    }

    public HyUIPage(PlayerRef playerRef, CustomPageLifetime lifetime, String uiFile, List<UIElementBuilder<?>> elements, List<Consumer<UICommandBuilder>> editCallbacks) {
        super(playerRef, lifetime, DynamicPageData.CODEC);
        this.uiFile = uiFile;
        this.elements = elements;
        this.editCallbacks = editCallbacks;
    }

    @Override
    public Optional<Object> getValue(String id) {
        return Optional.ofNullable(elementValues.get(id));
    }

    /**
     * NOTE: Do not call this method, Hytale will call this method for you.
     * To open a UI see documentation.
     * 
     * Builds the HyUI page by appending the designated UI file and processing the 
     * elements and callbacks associated with the page.
     *
     * @param ref             The reference to the entity store associated with the page.
     * @param uiCommandBuilder The builder used to construct the UI commands for the page.
     * @param uiEventBuilder   The builder used to construct the UI events for the page.
     * @param store            The store containing the entity data required for the page.
     */
    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        HyUIPlugin.getInstance().logInfo("Building HyUIPage" + (uiFile != null ? " from file: " + uiFile : ""));
        if (uiFile != null) {
            uiCommandBuilder.append(uiFile);
        }

        if (editCallbacks != null) {
            for (Consumer<UICommandBuilder> callback : editCallbacks) {
                callback.accept(uiCommandBuilder);
            }
        }

        elementValues.clear();
        for (UIElementBuilder<?> element : elements) {
            captureInitialValues(element);
            element.build(uiCommandBuilder, uiEventBuilder);
        }
    }

    private void captureInitialValues(UIElementBuilder<?> element) {
        String id = element.getId();
        if (id != null && element.initialValue != null) {
            elementValues.put(id, element.initialValue);
        }
        for (UIElementBuilder<?> child : element.children) {
            captureInitialValues(child);
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull DynamicPageData data) {
        super.handleDataEvent(ref, store, data);

        HyUIPlugin.getInstance().logInfo("Received DataEvent: Action=" + data.action);
        data.values.forEach((key, value) -> {
            HyUIPlugin.getInstance().logInfo("  Property: " + key + " = " + value);
        });

        for (UIElementBuilder<?> element : elements) {
            handleElementEvents(element, data);
        }
    }

    private void handleElementEvents(UIElementBuilder<?> element, DynamicPageData data) {
        String internalId = element.getEffectiveId();
        String userId = element.getId();

        if (internalId != null) {
            String target = data.getValue("Target");

            for (UIEventListener<?> listener : element.getListeners()) {
                if (listener.type() == CustomUIEventBindingType.Activating && UIEventActions.BUTTON_CLICKED.equals(data.action)) {
                    if (internalId.equals(target)) {
                        ((UIEventListener<Void>) listener).callback().accept(null, this);
                    }
                } else if (listener.type() == CustomUIEventBindingType.ValueChanged) {
                    Object finalValue = null;

                    if (UIEventActions.VALUE_CHANGED.equals(data.action) && internalId.equals(target)) {
                        String rawValue;
                        // If it's a value-changed action, use @Value (RefValue) for specific elements
                        if (element.usesRefValue()) {
                            rawValue = data.getValue("RefValue");
                        } else {
                            rawValue = data.getValue("Value");
                        }

                        if (rawValue != null) {
                            finalValue = element.parseValue(rawValue);
                        }

                        if (finalValue != null && userId != null) {
                            elementValues.put(userId, finalValue);
                        }
                    }

                    if (finalValue != null) {
                        ((UIEventListener<Object>) listener).callback().accept(finalValue, this);
                    }
                }
            }
        }

        for (UIElementBuilder<?> child : element.children) {
            handleElementEvents(child, data);
        }
    }
}
