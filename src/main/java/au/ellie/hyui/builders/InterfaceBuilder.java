package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.assets.DynamicImageAsset;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TemplateProcessor;
import au.ellie.hyui.utils.HyvatarUtils;
import au.ellie.hyui.utils.PngDownloadUtils;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class InterfaceBuilder<T extends InterfaceBuilder<T>> {
    protected final Map<String, UIElementBuilder<?>> elementRegistry = new LinkedHashMap<>();
    protected final List<Consumer<UICommandBuilder>> editCallbacks = new ArrayList<>();
    protected String uiFile;
    protected String templateHtml;
    protected TemplateProcessor templateProcessor;
    protected boolean runtimeTemplateUpdatesEnabled;

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    public T fromFile(String uiFile) {
        this.uiFile = uiFile;
        this.templateHtml = null;
        this.templateProcessor = null;
        this.runtimeTemplateUpdatesEnabled = false;
        return self();
    }

    public T fromHtml(String html) {
        this.templateHtml = null;
        this.templateProcessor = null;
        this.runtimeTemplateUpdatesEnabled = false;
        new HtmlParser().parseToInterface(this, html);
        return self();
    }

    /**
     * Loads and processes an HTML template with variable substitution.
     *
     * <p>Example usage:</p>
     * <pre>
     * builder.fromTemplate("""
     *     &lt;p&gt;Hello, {{$playerName}}!&lt;/p&gt;
     *     &lt;p&gt;Score: {{$score|0}}&lt;/p&gt;
     *     """, new TemplateProcessor()
     *         .setVariable("playerName", player.getName())
     *         .setVariable("score", 1500));
     * </pre>
     *
     * @param html     The HTML template with {{$variable}} placeholders
     * @param template The template processor with variables set
     * @return This builder instance for method chaining
     */
    public T fromTemplate(String html, TemplateProcessor template) {
        this.templateHtml = html;
        this.templateProcessor = template;
        HtmlParser parser = new HtmlParser();
        parser.setTemplateProcessor(template);
        parser.parseToInterface(this, html);
        return self();
    }
    
    private String loadHtmlFromResources(String resourceFileName) {
        try (InputStream inputStream = InterfaceBuilder.class.getResourceAsStream(resourceFileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceFileName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load HTML from resource: " + resourceFileName, e);
        }
    }

    /**
     * Loads an HTML file from resources under Common/UI/Custom and parses it into this interface.
     *
     * @param resourcePath Path relative to Common/UI/Custom (e.g. "Pages/Something.html")
     * @return This builder instance for method chaining
     */
    public T loadHtml(String resourcePath) {
        String html = loadHtmlFromResources(resolveCustomResourcePath(resourcePath));
        return fromHtml(html);
    }

    /**
     * Loads an HTML template from resources under Common/UI/Custom with a template processor.
     *
     * @param resourcePath Path relative to Common/UI/Custom (e.g. "Pages/Something.html")
     * @param template     The template processor with variables set
     * @return This builder instance for method chaining
     */
    public T loadHtml(String resourcePath, TemplateProcessor template) {
        String html = loadHtmlFromResources(resolveCustomResourcePath(resourcePath));
        return fromTemplate(html, template);
    }

    /**
     * Loads an HTML template from resources under Common/UI/Custom with variables.
     *
     * @param resourcePath Path relative to Common/UI/Custom (e.g. "Pages/Something.html")
     * @param variables    Map of variable names to values
     * @return This builder instance for method chaining
     */
    public T loadHtml(String resourcePath, Map<String, ?> variables) {
        String html = loadHtmlFromResources(resolveCustomResourcePath(resourcePath));
        return fromTemplate(html, variables);
    }

    public T enableRuntimeTemplateUpdates(boolean enabled) {
        this.runtimeTemplateUpdatesEnabled = enabled;
        return self();
    }

    /**
     * Loads and processes an HTML template with a map of variables.
     *
     * @param html      The HTML template with {{$variable}} placeholders
     * @param variables Map of variable names to values
     * @return This builder instance for method chaining
     */
    public T fromTemplate(String html, Map<String, ?> variables) {
        return fromTemplate(html, new TemplateProcessor().setVariables(variables));
    }

    private String resolveCustomResourcePath(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("Resource path cannot be null or blank.");
        }
        String trimmed = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        return "/Common/UI/Custom/" + trimmed;
    }

    /**
     * Add an element inside the root node (#HyUIRoot) of the interface.
     * @param element The element to add to the root node.
     * @return Self, for chaining.
     */
    public T addElement(UIElementBuilder<?> element) {
        element.inside("#HyUIRoot");
        registerElement(element);
        return self();
    }

    protected void registerElement(UIElementBuilder<?> element) {
        if (element.getId() != null) {
            this.elementRegistry.put(element.getId(), element);
        }
        for (UIElementBuilder<?> child : element.children) {
            registerElement(child);
        }
        linkTabElements(element);
    }

    private void linkTabElements(UIElementBuilder<?> element) {
        if (element instanceof TabContentBuilder content) {
            linkTabContentToNavigation(content);
        } else if (element instanceof TabNavigationBuilder navigation) {
            for (UIElementBuilder<?> element1 : elementRegistry.values()) {
                if (element1 instanceof TabContentBuilder content) {
                    if (!isMatchingNavigation(content, navigation)) {
                        continue;
                    }
                    String contentId = content.getId();
                    if (contentId == null || contentId.isBlank()) {
                        continue;
                    }
                    navigation.linkTabContent(content.getTabId(), contentId);
                    applyInitialTabVisibility(content, navigation);
                }
            }
        }
    }

    private void linkTabContentToNavigation(TabContentBuilder content) {
        if (content.getTabId() == null || content.getTabId().isBlank()) {
            return;
        }
        String contentId = content.getId();
        if (contentId == null || contentId.isBlank()) {
            return;
        }
        for (UIElementBuilder<?> element : elementRegistry.values()) {
            if (element instanceof TabNavigationBuilder navigation) {
                if (!isMatchingNavigation(content, navigation)) {
                    continue;
                }
                navigation.linkTabContent(content.getTabId(), contentId);
                applyInitialTabVisibility(content, navigation);
                return;
            }
        }
    }

    private boolean isMatchingNavigation(TabContentBuilder content, TabNavigationBuilder navigation) {
        if (!navigation.hasTab(content.getTabId())) {
            return false;
        }
        String navId = content.getTabNavigationId();
        return navId == null || navId.isBlank() || navId.equals(navigation.getId());
    }

    private void applyInitialTabVisibility(TabContentBuilder content, TabNavigationBuilder navigation) {
        String selectedTabId = navigation.getSelectedTabId();
        if (selectedTabId == null || selectedTabId.isBlank()) {
            var tabs = navigation.getTabs();
            if (!tabs.isEmpty()) {
                selectedTabId = tabs.get(0).id();
                navigation.withSelectedTab(selectedTabId);
            }
        }
        if (selectedTabId != null && !selectedTabId.isBlank()) {
            content.withVisible(selectedTabId.equals(content.getTabId()));
        }
    }

    public <E extends UIElementBuilder<E>> Optional<E> getById(String id, Class<E> clazz) {
        UIElementBuilder<?> builder = elementRegistry.get(id);
        if (builder != null && clazz.isInstance(builder)) {
            return Optional.of(clazz.cast(builder));
        }
        return Optional.empty();
    }

    /**
     * Adds an event listener to an element by its ID.
     * Note: This only works for elements created using the builder API or via HYUIML (.fromHtml).
     * It does not work for elements defined in a .ui file loaded via .fromFile.
     *
     * @param id         The ID of the element.
     * @param type       The type of event to listen for.
     * @param valueClass The class of the value associated with the event.
     * @param callback   The callback to execute when the event occurs.
     * @param <V>        The type of the value.
     * @return This builder instance for method chaining.
     */
    public <V> T addEventListener(String id, CustomUIEventBindingType type, Class<V> valueClass, Consumer<V> callback) {
        UIElementBuilder<?> element = elementRegistry.get(id);
        if (element == null) {
            throw new IllegalArgumentException("No element found with ID '" + id + "'.");
        }
        element.addEventListener(type, valueClass, callback);
        return self();
    }

    public T addEventListener(String id, CustomUIEventBindingType type, Consumer<Object> callback) {
        return addEventListener(id, type, Object.class, callback);
    }

    /**
     * Adds an event listener with access to the UI context.
     * Note: This only works for elements created using the builder API or via HYUIML (.fromHtml).
     *
     * @param id         The ID of the element.
     * @param type       The type of event to listen for.
     * @param valueClass The class of the value associated with the event.
     * @param callback   The callback to execute when the event occurs.
     * @param <V>        The type of the value.
     * @return This builder instance for method chaining.
     */
    public <V> T addEventListener(String id, CustomUIEventBindingType type, Class<V> valueClass, BiConsumer<V, UIContext> callback) {
        UIElementBuilder<?> element = elementRegistry.get(id);
        if (element == null) {
            throw new IllegalArgumentException("No element found with ID '" + id + "'.");
        }
        element.addEventListenerWithContext(type, valueClass, callback);
        return self();
    }

    public T addEventListener(String id, CustomUIEventBindingType type, BiConsumer<Object, UIContext> callback) {
        return addEventListener(id, type, Object.class, callback);
    }

    public T editElement(Consumer<UICommandBuilder> callback) {
        this.editCallbacks.add(callback);
        return self();
    }

    protected void sendDynamicImageIfNeeded(PlayerRef pRef) {
        if (pRef == null || !pRef.isValid()) {
            return;
        }
        UUID playerUuid = pRef.getUuid();
        for (UIElementBuilder<?> element : elementRegistry.values()) {
            if (element instanceof DynamicImageBuilder dImg) {
                if (dImg.isImagePathAssigned(playerUuid)) {
                    continue;
                }
                sendDynamicImage(pRef, dImg);
            }
        }
    }

    static void sendDynamicImage(PlayerRef pRef, DynamicImageBuilder dynamicImage) {
        if (pRef == null || dynamicImage == null) {
            HyUIPlugin.getLog().logInfo("REFERENCE WAS INVALID");
            
            return;
        }
        UUID playerUuid = pRef.getUuid();
        String url = dynamicImage.getImageUrl();
        if (url == null || url.isBlank()) {
            HyUIPlugin.getLog().logInfo("URL WAS BLANK OR NULL");
            
            return;
        }
        try {
            HyUIPlugin.getLog().logInfo("Preparing dynamic image from URL: " + url);
            byte[] imageBytes;
            if (dynamicImage instanceof HyvatarImageBuilder hyvatar && !hyvatar.hasCustomImageUrl()) {
                imageBytes = HyvatarUtils.downloadRenderPng(
                        hyvatar.getUsername(),
                        hyvatar.getRenderType(),
                        hyvatar.getSize(),
                        hyvatar.getRotate(),
                        hyvatar.getCape()
                );
            } else {
                imageBytes = PngDownloadUtils.downloadPng(url);
            }

            DynamicImageAsset asset = new DynamicImageAsset(imageBytes, playerUuid);
            DynamicImageAsset.sendToPlayer(pRef.getPacketHandler(), DynamicImageAsset.empty(asset.getSlotIndex()));
            dynamicImage.withImagePath(asset.getPath());
            dynamicImage.setSlotIndex(playerUuid, asset.getSlotIndex());

            DynamicImageAsset.sendToPlayer(pRef.getPacketHandler(), asset);
            HyUIPlugin.getLog().logInfo("Dynamic image sent using path: " + asset.getPath());
        } catch (IllegalStateException e) {
            HyUIPlugin.getLog().logInfo("Failed to allocate dynamic image slot: " + e.getMessage());
        } catch (IOException e) {
            HyUIPlugin.getLog().logInfo("Failed to download dynamic image: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            HyUIPlugin.getLog().logInfo("Dynamic image download interrupted: " + e.getMessage());
        }
    }

    /**
     * Retrieves the top-level elements of the interface, which are elements with the parent selector "#HyUIRoot".
     * @return A list of top-level UIElementBuilder instances for use in other builders.
     */
    public List<UIElementBuilder<?>> getTopLevelElements() {
        List<UIElementBuilder<?>> topLevel = new ArrayList<>();
        for (UIElementBuilder<?> element : elementRegistry.values()) {
            if ("#HyUIRoot".equals(element.parentSelector)) {
                topLevel.add(element);
            }
        }
        return topLevel;
    }

    /**
     * Get all elements in the element registry for this builder.
     * 
     * @return a list of all elements, irrespective of top-level.
     */
    public List<UIElementBuilder<?>> getElements() {
        return elementRegistry.values().stream().toList();        
    }

    public String getTemplateHtml() {
        return templateHtml;
    }

    public TemplateProcessor getTemplateProcessor() {
        return templateProcessor;
    }

    public boolean isRuntimeTemplateUpdatesEnabled() {
        return runtimeTemplateUpdatesEnabled;
    }
}
