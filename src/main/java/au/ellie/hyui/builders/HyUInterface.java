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

import au.ellie.hyui.HyUIPluginLogger;
import au.ellie.hyui.elements.UIType;
import au.ellie.hyui.events.*;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TemplateProcessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.Asset;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import au.ellie.hyui.HyUIPlugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.UUID;

/**
 * Base implementation for HyUI pages and HUDs.
 * Handles template processing, element building, and event dispatch.
 */
public abstract class HyUInterface implements UIContext {

    protected String uiFile;
    protected List<UIElementBuilder<?>> elements;
    protected List<BiConsumer<UICommandBuilder, UIEventBuilder>> editCallbacks;
    protected Map<String, Object> elementValues = new HashMap<>();
    protected List<String> commandLog = new ArrayList<>();
    protected String templateHtml;
    protected TemplateProcessor templateProcessor;
    protected boolean hasBuilt;
    protected boolean runtimeTemplateUpdatesEnabled;
    protected final InterfaceBuilder<?> rootElementBuilder;
    protected final Set<String> dirtyValueIds = new HashSet<>();

    /**
     * Creates a new interface wrapper.
     *
     * @param uiFile UI file path (nullable)
     * @param elements top-level elements to build
     * @param editCallbacks edit callbacks to apply before building
     * @param templateHtml optional template HTML
     * @param templateProcessor template processor for runtime updates
     * @param runtimeTemplateUpdatesEnabled whether to reprocess templates at runtime
     * @param rootElementBuilder root builder that owns this interface
     */
    public HyUInterface(String uiFile,
                        List<UIElementBuilder<?>> elements,
                        List<BiConsumer<UICommandBuilder, UIEventBuilder>> editCallbacks,
                        String templateHtml,
                        TemplateProcessor templateProcessor,
                        boolean runtimeTemplateUpdatesEnabled,
                        InterfaceBuilder<?> rootElementBuilder) {
        this.uiFile = uiFile;
        this.elements = elements;
        this.editCallbacks = editCallbacks;
        this.templateHtml = templateHtml;
        this.templateProcessor = templateProcessor;
        this.runtimeTemplateUpdatesEnabled = runtimeTemplateUpdatesEnabled;
        this.rootElementBuilder = rootElementBuilder;
    }

    /**
     * @return a copy of the UI command log for debugging
     */
    @Override
    public List<String> getCommandLog() {
        return new ArrayList<>(commandLog);
    }

    /**
     * Retrieves the last known value for an element ID.
     *
     * @param id element id
     * @return optional value
     */
    @Override
    public Optional<Object> getValue(String id) {
        if (HyUIPluginLogger.IS_DEV) {
            HyUIPlugin.getLog().logFinest("Retrieving value for element: " + id);
            for (var s : elementValues.entrySet()) {
                HyUIPlugin.getLog().logFinest("Element: " + s.getKey() + ", Value: " + s.getValue());
            }
        }
        return Optional.ofNullable(elementValues.get(id));
    }

    /**
     * @return the active page if this interface is a page
     */
    @Override
    public Optional<HyUIPage> getPage() {
        return Optional.empty();
    }

    /**
     * Updates the page; implementations decide behavior.
     *
     * @param shouldClose whether to close/clear before rebuilding
     */
    @Override
    public void updatePage(boolean shouldClose) {}
    
    /**
     * Builds the UI into command/event builders.
     */
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder,
                      @Nonnull Store<EntityStore> store) {
        build(ref, uiCommandBuilder, uiEventBuilder, store, false);
    }

    /**
     * Builds the UI into command/event builders with optional update-only mode.
     *
     * @param updateOnly if true, only update existing elements
     */
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder,
                      @Nonnull Store<EntityStore> store,
                      boolean updateOnly) {
        
        HyUIPlugin.getLog().logFinest("REBUILD: HyUInterface build updateOnly=" + updateOnly);
        HyUIPlugin.getLog().logFinest("Building HyUInterface" + (uiFile != null ? " from file: " + uiFile : ""));

        //LoggingUICommandBuilder loggingBuilder = new LoggingUICommandBuilder();

        refreshTemplate(this);

        if (!updateOnly && uiFile != null) {
            //if (HyUIPluginLogger.IS_DEV)
            //    loggingBuilder.append(uiFile);
            uiCommandBuilder.append(uiFile);
        }

        if (editCallbacks != null) {
            for (BiConsumer<UICommandBuilder, UIEventBuilder> callback : editCallbacks) {
                //if (HyUIPluginLogger.IS_DEV)
                //    callback.accept(loggingBuilder);
                callback.accept(uiCommandBuilder, uiEventBuilder);
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
            /*if (HyUIPluginLogger.IS_DEV) {
                if (updateOnly) {
                    element.buildUpdates(loggingBuilder, new UIEventBuilder());
                } else {
                    element.build(loggingBuilder, new UIEventBuilder());
                }
            }*/
            if (updateOnly) {
                element.buildUpdates(uiCommandBuilder, uiEventBuilder);
            } else {
                element.build(uiCommandBuilder, uiEventBuilder);
            }
        }

        if (!updateOnly) {
            // This entire process below happens because we need to update the template to mimic the page.
            // and we capture the real values of the elements after they've been built.
            refreshTemplate(this);
            for (UIElementBuilder<?> element : elements) {
                /*if (HyUIPluginLogger.IS_DEV) {
                    element.buildUpdates(loggingBuilder, new UIEventBuilder());
                }*/
                // TODO: THIS IS CALLED WHICH REBUILDS THE ENTIRE UI.
                element.buildUpdates(uiCommandBuilder, uiEventBuilder);
            }
        }

        //this.commandLog = loggingBuilder.getCommandLog();
        this.hasBuilt = true;
    }

    /**
     * Builds using an existing {@link UICommandBuilder}.
     */
    public void buildFromCommandBuilder(@Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder) {
        buildFromCommandBuilder(uiCommandBuilder, false, uiEventBuilder);
    }

    /**
     * Builds using an existing {@link UICommandBuilder}, with optional update-only mode.
     *
     * @param updateOnly if true, only update existing elements
     */
    public void buildFromCommandBuilder(@Nonnull UICommandBuilder uiCommandBuilder, boolean updateOnly, @Nonnull UIEventBuilder uiEventBuilder) {
        HyUIPlugin.getLog().logFinest("REBUILD: HyUInterface buildFromCommandBuilder updateOnly=" + updateOnly);
        HyUIPlugin.getLog().logFinest("Building HyUInterface " + (uiFile != null ? " from file: " + uiFile : ""));

        //LoggingUICommandBuilder loggingBuilder = new LoggingUICommandBuilder();

        refreshTemplate(this);

        if (!updateOnly && uiFile != null) {
            /*if (HyUIPluginLogger.IS_DEV)
                loggingBuilder.append(uiFile);*/
            uiCommandBuilder.append(uiFile);
        }

        if (editCallbacks != null) {
            for (BiConsumer<UICommandBuilder, UIEventBuilder> callback : editCallbacks) {
               /* if (HyUIPluginLogger.IS_DEV)
                    callback.accept(loggingBuilder);*/
                callback.accept(uiCommandBuilder, uiEventBuilder);
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
            /*if (HyUIPluginLogger.IS_DEV) {
                if (updateOnly) {
                    element.buildUpdates(loggingBuilder, null);
                } else {
                    element.build(loggingBuilder, null);
                }
            }*/
            if (updateOnly) {
                element.buildUpdates(uiCommandBuilder, null);
            } else {
                element.build(uiCommandBuilder, null);
            }
        }

        if (!updateOnly) {
            refreshTemplate(this);
            for (UIElementBuilder<?> element : elements) {
                /*if (HyUIPluginLogger.IS_DEV) {
                    element.buildUpdates(loggingBuilder, null);
                }*/
                element.buildUpdates(uiCommandBuilder, null);
            }
        }

        //this.commandLog = loggingBuilder.getCommandLog();
        this.hasBuilt = true;
    }

    /**
     * Captures initial values from an element tree into the value cache.
     */
    protected void captureInitialValues(UIElementBuilder<?> element) {
        String id = element.getId();
        if (id != null && element.initialValue != null) {
            elementValues.put(id, element.initialValue);
        }
        for (UIElementBuilder<?> child : element.children) {
            captureInitialValues(child);
        }
    }

    /**
     * Dispatches a data event to all elements using this interface as context.
     */
    protected void handleDataEventInternal(DynamicPageData data) {
        handleDataEventInternal(data, this);
    }

    /**
     * Dispatches a data event to all elements using the provided context.
     */
    protected void handleDataEventInternal(DynamicPageData data, UIContext context) {
        HyUIPlugin.getLog().logFinest("Received DataEvent: Action=" + data.action);
        data.values.forEach((key, value) -> {
            HyUIPlugin.getLog().logFinest("  Property: " + key + " = " + value);
        });

        for (UIElementBuilder<?> element : elements) {
            handleElementEvents(element, data, context);
        }
    }

    /**
     * Routes event callbacks for a single element and its children.
     */
    protected void handleElementEvents(UIElementBuilder<?> element, DynamicPageData data, UIContext context) {
        String internalId = element.getEffectiveId();
        String userId = element.getId();

        if (internalId != null) {
            String target = data.getValue("Target");
            Optional<CustomUIEventBindingType> actionType = resolveActionType(data.action);

            List<UIEventListener<?>> listeners = new ArrayList<>(element.getListeners());
            for (UIEventListener<?> listener : listeners) {
                if (!internalId.equals(target)) {
                    continue;
                }
                if (actionType.isEmpty() || listener.type() != actionType.get()) {
                    continue;
                }

                if (listener.type() == CustomUIEventBindingType.Activating || 
                        listener.type() == CustomUIEventBindingType.Dismissing || 
                        listener.type() == CustomUIEventBindingType.Validating) {
                    ((UIEventListener<Void>) listener).callback().accept(null, context);
                    continue;
                }
                if (isSlotEventRelated(listener.type()) || 
                        listener.type() == CustomUIEventBindingType.SelectedTabChanged ||
                        listener.type() == CustomUIEventBindingType.MouseButtonReleased ||
                        listener.type() == CustomUIEventBindingType.MouseEntered ||
                        listener.type() == CustomUIEventBindingType.MouseExited ||
                        listener.type() == CustomUIEventBindingType.DoubleClicking ||
                        listener.type() == CustomUIEventBindingType.RightClicking
                ) {
                    Object payload = buildEventPayload(listener.type(), data);
                    ((UIEventListener<Object>) listener).callback().accept(payload, context);
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

        List<UIElementBuilder<?>> children = new ArrayList<>(element.children);
        for (UIElementBuilder<?> child : children) {
            handleElementEvents(child, data, context);
        }
    }

    private boolean isSlotEventRelated(CustomUIEventBindingType type) {
        return type == CustomUIEventBindingType.SlotClicking
                || type == CustomUIEventBindingType.SlotDoubleClicking
                || type == CustomUIEventBindingType.SlotMouseEntered
                || type == CustomUIEventBindingType.SlotMouseExited
                || type == CustomUIEventBindingType.DragCancelled
                || type == CustomUIEventBindingType.Dropped
                || type == CustomUIEventBindingType.SlotMouseDragCompleted
                || type == CustomUIEventBindingType.SlotMouseDragExited
                || type == CustomUIEventBindingType.SlotClickReleaseWhileDragging
                || type == CustomUIEventBindingType.SlotClickPressWhileDragging;
    }

    private Object buildEventPayload(CustomUIEventBindingType type, DynamicPageData data) {
        return switch (type) {
            case SlotClicking -> SlotClickingEventData.from(data);
            case SlotDoubleClicking -> SlotDoubleClickingEventData.from(data);
            case SlotMouseEntered -> SlotMouseEnteredEventData.from(data);
            case SlotMouseExited -> SlotMouseExitedEventData.from(data);
            case DragCancelled -> new DragCancelledEventData();
            case Dropped -> DroppedEventData.from(data);
            case SlotMouseDragCompleted -> SlotMouseDragCompletedEventData.from(data);
            case SlotMouseDragExited -> SlotMouseDragExitedEventData.from(data);
            case SlotClickReleaseWhileDragging -> SlotClickReleaseWhileDraggingEventData.from(data);
            case SlotClickPressWhileDragging -> SlotClickPressWhileDraggingEventData.from(data);
            case SelectedTabChanged -> SelectedTabChangedEventData.from(data);
            // Only RightClicking and DoubleClicking has event data, but we wrap it in the same event data.
            case MouseButtonReleased, MouseEntered, MouseExited, DoubleClicking, RightClicking -> MouseEventData.from(data);
            default -> null;
        };
    }

    private Optional<CustomUIEventBindingType> resolveActionType(String action) {
        if (action == null || action.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(CustomUIEventBindingType.valueOf(action));
        } catch (IllegalArgumentException e) {
            if ("ButtonClicked".equals(action)) {
                return Optional.of(CustomUIEventBindingType.Activating);
            }
            return Optional.empty();
        }
    }

    /**
     * Looks up a UI element by its user-defined id.
     *
     * @param id element id
     * @return optional builder
     */
    public Optional<UIElementBuilder<?>> getById(String id) {
        for (UIElementBuilder<?> element : elements) {
            Optional<UIElementBuilder<?>> found = findByIdRecursive(element, id);
            if (found.isPresent()) return found;
        }
        return Optional.empty();
    }

    /**
     * Raw lookup of an element by id.
     */
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

    /**
     * Looks up and casts an element by id.
     */
    public <E extends UIElementBuilder<E>> Optional<E> getById(String id, Class<E> clazz) {
        return getById(id).filter(clazz::isInstance).map(clazz::cast);
    }

    /**
     * @return the UI file path, if any
     */
    public String getUiFile() {
        return uiFile;
    }

    /**
     * Sets the UI file path for this interface.
     */
    protected void setUiFile(String uiFile) {
        this.uiFile = uiFile;
    }

    /**
     * @return top-level elements managed by this interface
     */
    public List<UIElementBuilder<?>> getElements() {
        return elements;
    }

    /**
     * Replaces the current top-level element list.
     */
    protected void setElements(List<UIElementBuilder<?>> elements) {
        this.elements = elements;
    }

    /**
     * @return edit callbacks applied before building
     */
    public List<BiConsumer<UICommandBuilder, UIEventBuilder>> getEditCallbacks() {
        return editCallbacks;
    }

    /**
     * Replaces the current edit callbacks list.
     */
    protected void setEditCallbacks(List<BiConsumer<UICommandBuilder, UIEventBuilder>> editCallbacks) {
        this.editCallbacks = editCallbacks;
    }

    /**
     * @return cached element values by id
     */
    public Map<String, Object> getElementValues() {
        return elementValues;
    }

    /**
     * Replaces the current element value cache.
     */
    protected void setElementValues(Map<String, Object> elementValues) {
        this.elementValues = elementValues;
    }

    /**
     * Resets the built state for this interface.
     */
    protected void resetBuildState() {
        this.hasBuilt = false;
    }

    /**
     * Releases any dynamic image slots assigned for this player.
     */
    public void releaseDynamicImages(UUID playerUuid) {
        getElements().forEach(element -> releaseDynamicImagesRecursive(element, playerUuid));
    }

    private void releaseDynamicImagesRecursive(UIElementBuilder<?> element, UUID playerUuid) {
        if (element instanceof DynamicImageBuilder) {
            HyUIPlugin.getLog().logFinest("Releasing image: " + element.getEffectiveId());
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
        HyUIPlugin.getLog().logFinest("REBUILD: Template refresh");
        HtmlParser parser = new HtmlParser();
        String processedHtml = templateProcessor.process(templateHtml, context);
        List<UIElementBuilder<?>> updatedElements = parser.parse(processedHtml);
        
        this.elements = mergeElementLists(this.elements, updatedElements);
        applyRuntimeValues(this.elements, context);
        reapplyTabSelections(this.elements, context);
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
                if (!(current instanceof TabNavigationBuilder)) {
                    List<UIElementBuilder<?>> mergedChildren = mergeElementLists(current.children, updated.children);
                    current.children.clear();
                    current.children.addAll(mergedChildren);
                }
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

    private void reapplyTabSelections(List<UIElementBuilder<?>> elements, UIContext context) {
        if (elements == null || context == null) {
            return;
        }
        for (UIElementBuilder<?> element : elements) {
            if (element instanceof TabNavigationBuilder navigation) {
                navigation.applySelectionState(context);
            }
            if (!element.children.isEmpty()) {
                reapplyTabSelections(element.children, context);
            }
        }
    }

    /**
     * Reopens this interface when a referenced asset changes.
     *
     * @return the rebuilt builder, or null if not applicable
     */
    public InterfaceBuilder<?> reopenFromAsset(Player player, PlayerRef ref, Store<EntityStore> store, Asset asset) {
        if (rootElementBuilder instanceof PageBuilder pageBuilder) {
            // TODO: EndsWith or some parsing?
            if (uiFile != null && asset.name.contains(uiFile)) {
                pageBuilder.elementRegistry.clear();
                if (pageBuilder.parsedUIFile)
                    pageBuilder.fromUIFile(uiFile);
                else
                    pageBuilder.fromFile(uiFile);
                pageBuilder.open(ref, store);
                return pageBuilder;
            }
            // Generally the resource html path is more specific than the asset name.
            if (pageBuilder.htmlFilePath.contains(asset.name)) {
                var normalizedHtmlPath = pageBuilder.htmlFilePath.replace("/Common/UI/Custom/", "");
                var style = pageBuilder.uiStyleFilePath != null ? 
                        pageBuilder.uiStyleFilePath.contains("hywind") ? UIType.HYWIND : UIType.NONE
                        : UIType.NONE;
                
                // We need to "reload" the changed asset.
                // Other information such as events is still captured on the page builder.
                pageBuilder.elementRegistry.clear();
                if (pageBuilder.templateProcessor != null) {
                    pageBuilder.loadHtml(
                            normalizedHtmlPath,
                            pageBuilder.templateProcessor, style
                            
                    );
                } else {
                    pageBuilder.loadHtml(normalizedHtmlPath, 
                            style);
                }
                // We CANNOT "reload" html from inline stuff, so we are stuck here with just opening the page.
                // Users of this mod should opt to store their HTML in files if they use it.
                pageBuilder.open(ref, store);
                return pageBuilder;
            }
        } else if (rootElementBuilder instanceof HudBuilder hudBuilder) {
            if (uiFile != null && asset.name.contains(uiFile)) {
                hudBuilder.elementRegistry.clear();
                if (hudBuilder.parsedUIFile)
                    hudBuilder.fromUIFile(uiFile);
                else
                    hudBuilder.fromFile(uiFile);
                // DO NOT ever show.
                return hudBuilder;
            }
            // Generally the resource html path is more specific than the asset name.
            if (hudBuilder.htmlFilePath.contains(asset.name)) {
                var normalizedHtmlPath = hudBuilder.htmlFilePath.replace("/Common/UI/Custom/", "");
                var style = hudBuilder.uiStyleFilePath != null ?
                        hudBuilder.uiStyleFilePath.contains("hywind") ? UIType.HYWIND : UIType.NONE
                        : UIType.NONE;

                // We need to "reload" the changed asset.
                // Other information such as events is still captured on the page builder.
                hudBuilder.elementRegistry.clear();
                if (hudBuilder.templateProcessor != null) {
                    hudBuilder.loadHtml(
                            normalizedHtmlPath,
                            hudBuilder.templateProcessor, style

                    );
                } else {
                    hudBuilder.loadHtml(normalizedHtmlPath,
                            style);
                }
                // DO NOT ever show.
                return hudBuilder;
            }
        }
        return null;
    }

    /**
     * Determines whether an asset change should trigger a reopen.
     */
    public boolean willReopenFromAsset(Player player, PlayerRef playerRef, Store<EntityStore> store, Asset asset) {
        if (rootElementBuilder instanceof PageBuilder pageBuilder) {
            // TODO: EndsWith or some parsing?
            if (uiFile != null && asset.name.contains(uiFile)) {
                return true;
            }
            // Generally the resource html path is more specific than the asset name.
            if (pageBuilder.htmlFilePath.contains(asset.name)) {
                return true;
            }
        } else if (rootElementBuilder instanceof HudBuilder hudBuilder) {
            if (uiFile != null && asset.name.contains(uiFile)) {
                return true;
            }
            // Generally the resource html path is more specific than the asset name.
            if (hudBuilder.htmlFilePath.contains(asset.name)) {
                return true;
            }
        }
        return false;
    }
}
