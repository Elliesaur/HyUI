package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPluginLogger;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventListener;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TemplateProcessor;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.UUID;

public abstract class HyUInterface implements UIContext {

    protected String uiFile;
    protected List<UIElementBuilder<?>> elements;
    protected List<Consumer<UICommandBuilder>> editCallbacks;
    protected Map<String, Object> elementValues = new HashMap<>();
    protected List<String> commandLog = new ArrayList<>();
    protected String templateHtml;
    protected TemplateProcessor templateProcessor;
    private boolean hasBuilt;
    private boolean runtimeTemplateUpdatesEnabled;
    private final Set<String> dirtyValueIds = new HashSet<>();

    public HyUInterface(String uiFile,
                        List<UIElementBuilder<?>> elements,
                        List<Consumer<UICommandBuilder>> editCallbacks,
                        String templateHtml,
                        TemplateProcessor templateProcessor,
                        boolean runtimeTemplateUpdatesEnabled) {
        this.uiFile = uiFile;
        this.elements = elements;
        this.editCallbacks = editCallbacks;
        this.templateHtml = templateHtml;
        this.templateProcessor = templateProcessor;
        this.runtimeTemplateUpdatesEnabled = runtimeTemplateUpdatesEnabled;
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
    
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder,
                      @Nonnull Store<EntityStore> store) {
        build(ref, uiCommandBuilder, uiEventBuilder, store, false);
    }

    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder,
                      @Nonnull Store<EntityStore> store,
                      boolean updateOnly) {
        HyUIPlugin.getLog().logInfo("REBUILD: HyUInterface build updateOnly=" + updateOnly);
        HyUIPlugin.getLog().logInfo("Building HyUInterface" + (uiFile != null ? " from file: " + uiFile : ""));

        LoggingUICommandBuilder loggingBuilder = new LoggingUICommandBuilder();

        refreshTemplate(this);

        if (!updateOnly && uiFile != null) {
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

        if (!updateOnly) {
            elementValues.clear();
            dirtyValueIds.clear();
        }
        for (UIElementBuilder<?> element : elements) {
            if (!updateOnly) {
                captureInitialValues(element);
            }
            if (HyUIPluginLogger.LOGGING_ENABLED) {
                if (updateOnly) {
                    element.buildUpdates(loggingBuilder, new UIEventBuilder());
                } else {
                    element.build(loggingBuilder, new UIEventBuilder());
                }
            }
            if (updateOnly) {
                element.buildUpdates(uiCommandBuilder, uiEventBuilder);
            } else {
                element.build(uiCommandBuilder, uiEventBuilder);
            }
        }

        if (!updateOnly) {
            refreshTemplate(this);
            for (UIElementBuilder<?> element : elements) {
                if (HyUIPluginLogger.LOGGING_ENABLED) {
                    element.buildUpdates(loggingBuilder, new UIEventBuilder());
                }
                element.buildUpdates(uiCommandBuilder, uiEventBuilder);
            }
        }

        this.commandLog = loggingBuilder.getCommandLog();
        this.hasBuilt = true;
    }

    public void buildFromCommandBuilder(@Nonnull UICommandBuilder uiCommandBuilder) {
        buildFromCommandBuilder(uiCommandBuilder, false);
    }

    public void buildFromCommandBuilder(@Nonnull UICommandBuilder uiCommandBuilder, boolean updateOnly) {
        HyUIPlugin.getLog().logInfo("REBUILD: HyUInterface buildFromCommandBuilder updateOnly=" + updateOnly);
        HyUIPlugin.getLog().logInfo("Building HyUInterface " + (uiFile != null ? " from file: " + uiFile : ""));

        LoggingUICommandBuilder loggingBuilder = new LoggingUICommandBuilder();

        refreshTemplate(this);

        if (!updateOnly && uiFile != null) {
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

        if (!updateOnly) {
            elementValues.clear();
            dirtyValueIds.clear();
        }
        for (UIElementBuilder<?> element : elements) {
            if (!updateOnly) {
                captureInitialValues(element);
            }
            if (HyUIPluginLogger.LOGGING_ENABLED) {
                if (updateOnly) {
                    element.buildUpdates(loggingBuilder, null);
                } else {
                    element.build(loggingBuilder, null);
                }
            }
            if (updateOnly) {
                element.buildUpdates(uiCommandBuilder, null);
            } else {
                element.build(uiCommandBuilder, null);
            }
        }

        if (!updateOnly) {
            refreshTemplate(this);
            for (UIElementBuilder<?> element : elements) {
                if (HyUIPluginLogger.LOGGING_ENABLED) {
                    element.buildUpdates(loggingBuilder, null);
                }
                element.buildUpdates(uiCommandBuilder, null);
            }
        }

        this.commandLog = loggingBuilder.getCommandLog();
        this.hasBuilt = true;
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

            List<UIEventListener<?>> listeners = new ArrayList<>(element.getListeners());
            for (UIEventListener<?> listener : listeners) {
                if (listener.type() == CustomUIEventBindingType.Activating && UIEventActions.BUTTON_CLICKED.equals(data.action)) {
                    if (internalId.equals(target)) {
                        ((UIEventListener<Void>) listener).callback().accept(null, context);
                    }
                } else if (listener.type() == CustomUIEventBindingType.ValueChanged
                        || listener.type() == CustomUIEventBindingType.FocusLost
                        || listener.type() == CustomUIEventBindingType.FocusGained) {
                    if (!internalId.equals(target)) {
                        continue;
                    }

                    boolean shouldHandle = switch (listener.type()) {
                        case CustomUIEventBindingType.ValueChanged -> UIEventActions.VALUE_CHANGED.equals(data.action);
                        case CustomUIEventBindingType.FocusLost -> UIEventActions.FOCUS_LOST.equals(data.action);
                        case CustomUIEventBindingType.FocusGained -> UIEventActions.FOCUS_GAINED.equals(data.action);
                        default -> false;
                    };

                    if (!shouldHandle) {
                        continue;
                    }

                    String rawValue = element.usesRefValue() ? data.getValue("RefValue") : data.getValue("Value");
                    Object finalValue = rawValue != null ? element.parseValue(rawValue) : null;

                    // TODO: Seems like a bit of a hackaround to deal with the multiple events firing.
                    if (finalValue != null && userId != null && listener.type() != CustomUIEventBindingType.FocusGained) {
                        //Object previous = elementValues.get(userId);
                        //if (!Objects.equals(previous, finalValue)) {
                            elementValues.put(userId, finalValue);
                            dirtyValueIds.add(userId);
                        //}
                    }

                    if (finalValue != null) {
                        ((UIEventListener<Object>) listener).callback().accept(finalValue, context);
                    }
                }
            }
        }

        List<UIElementBuilder<?>> children = new ArrayList<>(element.children);
        for (UIElementBuilder<?> child : children) {
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

    @Override
    public Optional<UIElementBuilder<?>> getByIdRaw(String id) {
        return getById(id);
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

    protected void resetBuildState() {
        this.hasBuilt = false;
    }

    public void releaseDynamicImages(UUID playerUuid) {
        getElements().forEach(element -> releaseDynamicImagesRecursive(element, playerUuid));
    }

    private void releaseDynamicImagesRecursive(UIElementBuilder<?> element, UUID playerUuid) {
        if (element instanceof DynamicImageBuilder) {
            HyUIPlugin.getLog().logInfo("Releasing image: " + element.getEffectiveId());
            ((DynamicImageBuilder) element).releaseSlotForPlayer(playerUuid);
        }
        for (UIElementBuilder<?> child : element.children) {
            releaseDynamicImagesRecursive(child, playerUuid);
        }
    }

    private void refreshTemplate(UIContext context) {
        if (!runtimeTemplateUpdatesEnabled || templateHtml == null || templateProcessor == null) {
            return;
        }
        if (hasBuilt && dirtyValueIds.isEmpty()) {
            //return;
        }
        HyUIPlugin.getLog().logInfo("REBUILD: Template refresh");
        HtmlParser parser = new HtmlParser();
        String processedHtml = templateProcessor.process(templateHtml, context);
        List<UIElementBuilder<?>> updatedElements = parser.parse(processedHtml);
        
        this.elements = mergeElementLists(this.elements, updatedElements);
        applyRuntimeValues(this.elements, context);
        if (hasBuilt) {
            //dirtyValueIds.clear();
        }
    }

    private List<UIElementBuilder<?>> mergeElementLists(List<UIElementBuilder<?>> currentElements,
                                                       List<UIElementBuilder<?>> updatedElements) {
/*
        for (var e : updatedElements) {
            HyUIPlugin.getLog().logInfo("UPDATED ELEMENT: \n\n" + e);
        }
        for (var e : currentElements) {
            HyUIPlugin.getLog().logInfo("CURRENT ELEMENT: \n\n" + e);
        }
*/

        Map<String, UIElementBuilder<?>> currentById = new HashMap<>();
        List<UIElementBuilder<?>> currentNoId = new ArrayList<>();
        for (UIElementBuilder<?> element : currentElements) {
            String id = getStableId(element);
            if (id != null && !id.isBlank()) {
                currentById.put(id, element);
            } else {
                currentNoId.add(element);
            }
        }
/*

        HyUIPlugin.getLog().logInfo("Current elements with stable IDs:");
        for (Map.Entry<String, UIElementBuilder<?>> entry : currentById.entrySet()) {
            HyUIPlugin.getLog().logInfo("  ID: " + entry.getKey() + ", Element: " + entry.getValue().getClass().getSimpleName());
        }
        HyUIPlugin.getLog().logInfo("Current elements without stable IDs:");
        for (UIElementBuilder<?> element : currentNoId) {
            HyUIPlugin.getLog().logInfo("  Element: " + element.getClass().getSimpleName() + ", EffectiveId: " + element.getEffectiveId() + ", Id: " + element.getId());
        }
*/

        List<UIElementBuilder<?>> merged = new ArrayList<>();
        List<UIElementBuilder<?>> unusedNoId = new ArrayList<>(currentNoId);

        for (UIElementBuilder<?> updated : updatedElements) {
            //HyUIPlugin.getLog().logInfo("Processing updated element with ID: " + getStableId(updated));
            UIElementBuilder<?> current = null;
            String id = getStableId(updated);
            if (id != null && !id.isBlank()) {
                current = currentById.get(id);
            } else {
                UIElementBuilder<?> result = null;
                for (int i = 0; i < unusedNoId.size(); i++) {
                    UIElementBuilder<?> candidate = unusedNoId.get(i);
                    if (candidate.getClass().equals(updated.getClass())) {
                        unusedNoId.remove(i);
                        result = candidate;
                        break;
                    }
                }
                current = result;
            }

            if (current != null && current.getClass().equals(updated.getClass())) {
                current.applyTemplate(updated);
                List<UIElementBuilder<?>> mergedChildren = mergeElementLists(current.children, updated.children);
                current.children.clear();
                current.children.addAll(mergedChildren);
                merged.add(current);
            } else {
                merged.add(updated);
            }
        }

        return merged;
    }

    private String getStableId(UIElementBuilder<?> element) {
        if (element == null) {
            return null;
        }

        String userId = element.getId();
        String effectiveId = element.getEffectiveId();

        if (userId == null || userId.isBlank() || effectiveId == null || effectiveId.isBlank() || userId.equals(effectiveId)) {
            return null;
        }

        return userId;
    }

    private void applyRuntimeValues(List<UIElementBuilder<?>> elements, UIContext context) {
        if (elements == null || context == null) {
            return;
        }
        for (UIElementBuilder<?> element : elements) {
            String id = element.getId();
            if (id != null) {
                if (dirtyValueIds.contains(id)) {
                    context.getValue(id).ifPresent(element::applyRuntimeValue);
                } else if (element.initialValue != null) {
                    elementValues.put(id, element.initialValue);
                }
            }
            if (!element.children.isEmpty()) {
                applyRuntimeValues(element.children, context);
            }
        }
    }
}
