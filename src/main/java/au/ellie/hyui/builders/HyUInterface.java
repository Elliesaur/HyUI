package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPluginLogger;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventListener;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.events.UIEventActions;
import au.ellie.hyui.events.DynamicPageData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class HyUInterface implements UIContext {

    protected String uiFile;
    protected List<UIElementBuilder<?>> elements;
    protected List<Consumer<UICommandBuilder>> editCallbacks;
    protected Map<String, Object> elementValues = new HashMap<>();
    protected List<String> commandLog = new ArrayList<>();

    public HyUInterface(String uiFile, List<UIElementBuilder<?>> elements, List<Consumer<UICommandBuilder>> editCallbacks) {
        this.uiFile = uiFile;
        this.elements = elements;
        this.editCallbacks = editCallbacks;
    }

    @Override
    public List<String> getCommandLog() {
        return new ArrayList<>(commandLog);
    }

    @Override
    public Optional<Object> getValue(String id) {
        if (HyUIPluginLogger.LOGGING_ENABLED) {
            HyUIPlugin.getLog().logInfo("Retrieving value for element: " + id);
            for (var s : elementValues.entrySet()) {
                HyUIPlugin.getLog().logInfo("Element: " + s.getKey() + ", Value: " + s.getValue());
            }
        }
        return Optional.ofNullable(elementValues.get(id));
    }

    @Override
    public Optional<HyUIPage> getPage() {
        return Optional.empty();
    }

    @Override
    public void updatePage(boolean shouldClose) {}
    
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        HyUIPlugin.getLog().logInfo("Building HyUInterface" + (uiFile != null ? " from file: " + uiFile : ""));
        
        LoggingUICommandBuilder loggingBuilder = new LoggingUICommandBuilder();

        if (uiFile != null) {
            if (HyUIPluginLogger.LOGGING_ENABLED)
                loggingBuilder.append(uiFile);
            uiCommandBuilder.append(uiFile);
        }

        if (editCallbacks != null) {
            for (Consumer<UICommandBuilder> callback : editCallbacks) {
                if (HyUIPluginLogger.LOGGING_ENABLED)
                    callback.accept(loggingBuilder);
                callback.accept(uiCommandBuilder);
            }
        }

        elementValues.clear();
        for (UIElementBuilder<?> element : elements) {
            captureInitialValues(element);
            if (HyUIPluginLogger.LOGGING_ENABLED)
                element.build(loggingBuilder, new UIEventBuilder());
            element.build(uiCommandBuilder, uiEventBuilder);
        }

        this.commandLog = loggingBuilder.getCommandLog();
    }

    public void buildFromCommandBuilder(@Nonnull UICommandBuilder uiCommandBuilder) {
        HyUIPlugin.getLog().logInfo("Building HyUInterface " + (uiFile != null ? " from file: " + uiFile : ""));
        
        LoggingUICommandBuilder loggingBuilder = new LoggingUICommandBuilder();

        if (uiFile != null) {
            if (HyUIPluginLogger.LOGGING_ENABLED)
                loggingBuilder.append(uiFile);
            uiCommandBuilder.append(uiFile);
        }

        if (editCallbacks != null) {
            for (Consumer<UICommandBuilder> callback : editCallbacks) {
                if (HyUIPluginLogger.LOGGING_ENABLED)
                    callback.accept(loggingBuilder);
                callback.accept(uiCommandBuilder);
            }
        }

        elementValues.clear();
        for (UIElementBuilder<?> element : elements) {
            captureInitialValues(element);
            if (HyUIPluginLogger.LOGGING_ENABLED)
                element.build(loggingBuilder, null);
            element.build(uiCommandBuilder, null);
        }

        this.commandLog = loggingBuilder.getCommandLog();
    }

    protected void captureInitialValues(UIElementBuilder<?> element) {
        String id = element.getId();
        if (id != null && element.initialValue != null) {
            elementValues.put(id, element.initialValue);
        }
        for (UIElementBuilder<?> child : element.children) {
            captureInitialValues(child);
        }
    }

    protected void handleDataEventInternal(DynamicPageData data) {
        handleDataEventInternal(data, this);
    }

    protected void handleDataEventInternal(DynamicPageData data, UIContext context) {
        HyUIPlugin.getLog().logInfo("Received DataEvent: Action=" + data.action);
        data.values.forEach((key, value) -> {
            HyUIPlugin.getLog().logInfo("  Property: " + key + " = " + value);
        });

        for (UIElementBuilder<?> element : elements) {
            handleElementEvents(element, data, context);
        }
    }

    protected void handleElementEvents(UIElementBuilder<?> element, DynamicPageData data, UIContext context) {
        String internalId = element.getEffectiveId();
        String userId = element.getId();

        if (internalId != null) {
            String target = data.getValue("Target");

            for (UIEventListener<?> listener : element.getListeners()) {
                if (listener.type() == CustomUIEventBindingType.Activating && UIEventActions.BUTTON_CLICKED.equals(data.action)) {
                    if (internalId.equals(target)) {
                        ((UIEventListener<Void>) listener).callback().accept(null, context);
                    }
                } else if (listener.type() == CustomUIEventBindingType.ValueChanged) {
                    Object finalValue = null;

                    if (UIEventActions.VALUE_CHANGED.equals(data.action) && internalId.equals(target)) {
                        String rawValue;
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
                        ((UIEventListener<Object>) listener).callback().accept(finalValue, context);
                    }
                }
            }
        }

        for (UIElementBuilder<?> child : element.children) {
            handleElementEvents(child, data, context);
        }
    }

    public Optional<UIElementBuilder<?>> getById(String id) {
        for (UIElementBuilder<?> element : elements) {
            Optional<UIElementBuilder<?>> found = findByIdRecursive(element, id);
            if (found.isPresent()) return found;
        }
        return Optional.empty();
    }

    private Optional<UIElementBuilder<?>> findByIdRecursive(UIElementBuilder<?> element, String id) {
        if (id.equals(element.getId())) {
            return Optional.of(element);
        }
        for (UIElementBuilder<?> child : element.children) {
            Optional<UIElementBuilder<?>> found = findByIdRecursive(child, id);
            if (found.isPresent()) return found;
        }
        return Optional.empty();
    }

    public <E extends UIElementBuilder<E>> Optional<E> getById(String id, Class<E> clazz) {
        return getById(id).filter(clazz::isInstance).map(clazz::cast);
    }

    public String getUiFile() {
        return uiFile;
    }

    protected void setUiFile(String uiFile) {
        this.uiFile = uiFile;
    }

    public List<UIElementBuilder<?>> getElements() {
        return elements;
    }

    protected void setElements(List<UIElementBuilder<?>> elements) {
        this.elements = elements;
    }

    public List<Consumer<UICommandBuilder>> getEditCallbacks() {
        return editCallbacks;
    }

    protected void setEditCallbacks(List<Consumer<UICommandBuilder>> editCallbacks) {
        this.editCallbacks = editCallbacks;
    }

    public Map<String, Object> getElementValues() {
        return elementValues;
    }

    protected void setElementValues(Map<String, Object> elementValues) {
        this.elementValues = elementValues;
    }

}
