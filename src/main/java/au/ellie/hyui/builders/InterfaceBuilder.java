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
import au.ellie.hyui.assets.DynamicImageAsset;
import au.ellie.hyui.elements.UIType;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TemplateProcessor;
import au.ellie.hyui.utils.HyvatarUtils;
import au.ellie.hyui.utils.PngDownloadUtils;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class InterfaceBuilder<T extends InterfaceBuilder<T>> {
    protected final Map<String, UIElementBuilder<?>> elementRegistry = new LinkedHashMap<>();
    protected final List<BiConsumer<UICommandBuilder, UIEventBuilder>> editCallbacks = new ArrayList<>();
    protected String uiFile;
    protected String templateHtml;
    protected TemplateProcessor templateProcessor;
    protected boolean runtimeTemplateUpdatesEnabled;
    protected boolean asyncImageLoadingEnabled;
    private static final ExecutorService DYNAMIC_IMAGE_EXECUTOR = Executors.newCachedThreadPool();

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    /**
     * Sets the UI file path for this interface.
     * Note: Event listeners will NOT work on elements defined in a .ui file.
     *
     * @param uiFile The path to the .ui file (e.g. "Pages/MyInterface.ui")
     * @return This builder instance for method chaining
     */
    public T fromFile(String uiFile) {
        this.uiFile = uiFile;
        this.templateHtml = null;
        this.templateProcessor = null;
        this.runtimeTemplateUpdatesEnabled = false;
        return self();
    }

    /**
     * Parses the provided HTML string into this interface with a specific style.
     *
     * @param html  The HTML string to parse
     * @param style The UIType style to apply (e.g. {@link UIType#HYWIND})
     * @return This builder instance for method chaining
     */
    public T fromHtml(String html, UIType style) {
        this.templateHtml = null;
        this.templateProcessor = null;
        this.runtimeTemplateUpdatesEnabled = false;
        html = addUIStyleToHtml(html, style);
        new HtmlParser().parseToInterface(this, html);
        return self();
    }
    
    /**
     * Parses the provided HTML template into this interface with variable substitution and a specific style.
     *
     * @param html     The HTML template with {{$variable}} placeholders
     * @param template The template processor with variables set
     * @param style    The UIType style to apply (e.g. {@link UIType#HYWIND})
     * @return This builder instance for method chaining
     */
    public T fromTemplate(String html, TemplateProcessor template, UIType style) {
        this.templateHtml = html;
        this.templateProcessor = template;
        HtmlParser parser = new HtmlParser();
        parser.setTemplateProcessor(template);
        html = addUIStyleToHtml(html, style);
        parser.parseToInterface(this, html);
        return self();
    }
    
    /**
     * Parses the provided HTML string into this interface.
     *
     * @param html The HTML string to parse
     * @return This builder instance for method chaining
     */
    public T fromHtml(String html) {
        return fromHtml(html, UIType.NONE);
    }
    
    /**
     * Parses the provided HTML template into this interface with variable substitution.
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
        return fromTemplate(html, template, UIType.NONE);
    }
    
    private String loadHtmlFromResources(String resourceFileName) {
        String normalized = resourceFileName.startsWith("/") ? resourceFileName.substring(1) : resourceFileName;
        List<Path> candidatePaths = List.of(
                Paths.get("src/main/resources").resolve(normalized),
                Paths.get("..", "src", "main", "resources").resolve(normalized),
                Paths.get("build/resources/main").resolve(normalized),
                Paths.get("..", "build", "resources", "main").resolve(normalized),
                Paths.get(normalized)
        );
        for (Path path : candidatePaths) {
            if (Files.isRegularFile(path)) {
                try {
                    return Files.readString(path, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load HTML from file: " + path, e);
                }
            }
        }
        try (InputStream inputStream = InterfaceBuilder.class.getResourceAsStream(resourceFileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceFileName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load HTML from resource: " + resourceFileName, e);
        }
    }

    private String addUIStyleToHtml(String html, UIType style) {
        String uiStyleFilePath = null;
        if (Objects.requireNonNull(style) == UIType.HYWIND) {
            uiStyleFilePath = "/Common/UI/Custom/Pages/Styles/hywind.html";
        } else {
            return html;
        }
        String contents = loadHtmlFromResources(uiStyleFilePath);
        return contents + "\n\n" + html;
    }
    
    /**
     * Loads an HTML file from resources under Common/UI/Custom and parses it into this interface.
     *
     * @param resourcePath Path relative to Common/UI/Custom (e.g. "Pages/Something.html")
     * @return This builder instance for method chaining
     */
    public T loadHtml(String resourcePath) {
        return loadHtml(resourcePath, UIType.NONE);
    }
    
    /**
     * Loads an HTML file from resources under Common/UI/Custom and parses it into this interface.
     *
     * @param resourcePath Path relative to Common/UI/Custom (e.g. "Pages/Something.html")
     * @param style        The UIType style to apply (e.g. {@link UIType#HYWIND})
     * @return This builder instance for method chaining
     */
    public T loadHtml(String resourcePath, UIType style) {
        String html = loadHtmlFromResources(resolveCustomResourcePath(resourcePath));
        return fromHtml(html, style);
    }
    
    /**
     * Loads an HTML template from resources under Common/UI/Custom with a template processor.
     *
     * @param resourcePath Path relative to Common/UI/Custom (e.g. "Pages/Something.html")
     * @param template     The template processor with variables set
     * @return This builder instance for method chaining
     */
    public T loadHtml(String resourcePath, TemplateProcessor template) {
        return loadHtml(resourcePath, template, UIType.NONE);
    }

    /**
     * Loads an HTML template from resources under Common/UI/Custom with a template processor and a specific style.
     *
     * @param resourcePath Path relative to Common/UI/Custom (e.g. "Pages/Something.html")
     * @param template     The template processor with variables set
     * @param style        The UIType style to apply (e.g. {@link UIType#HYWIND})
     * @return This builder instance for method chaining
     */
    public T loadHtml(String resourcePath, TemplateProcessor template, UIType style) {
        String html = loadHtmlFromResources(resolveCustomResourcePath(resourcePath));
        return fromTemplate(html, template, style);
    }
    
    /**
     * Loads an HTML template from resources under Common/UI/Custom with variable substitution.
     *
     * @param resourcePath Path relative to Common/UI/Custom (e.g. "Pages/Something.html")
     * @param variables    Map of variable names to values
     * @return This builder instance for method chaining
     */
    public T loadHtml(String resourcePath, Map<String, ?> variables) {
        String html = loadHtmlFromResources(resolveCustomResourcePath(resourcePath));
        return fromTemplate(html, variables);
    }

    /**
     * Loads an HTML template from resources under Common/UI/Custom with variables and a specific style.
     *
     * @param resourcePath Path relative to Common/UI/Custom (e.g. "Pages/Something.html")
     * @param variables    Map of variable names to values
     * @param style        The UIType style to apply (e.g. {@link UIType#HYWIND})
     * @return This builder instance for method chaining
     */
    public T loadHtml(String resourcePath, Map<String, ?> variables, UIType style) {
        String html = loadHtmlFromResources(resolveCustomResourcePath(resourcePath));
        return fromTemplate(html, variables, style);
    }
    
    public T enableRuntimeTemplateUpdates(boolean enabled) {
        this.runtimeTemplateUpdatesEnabled = enabled;
        return self();
    }

    public T enableAsyncImageLoading(boolean enabled) {
        this.asyncImageLoadingEnabled = enabled;
        return self();
    }

    /**
     * Parses the provided HTML template into this interface with variable substitution.
     *
     * @param html      The HTML template with {{$variable}} placeholders
     * @param variables Map of variable names to values
     * @return This builder instance for method chaining
     */
    public T fromTemplate(String html, Map<String, ?> variables) {
        return fromTemplate(html, variables, UIType.NONE);
    }
    
    /**
     * Parses the provided HTML template into this interface with variable substitution and a specific style.
     *
     * @param html      The HTML template with {{$variable}} placeholders
     * @param variables Map of variable names to values
     * @param style     The UIType style to apply (e.g. {@link UIType#HYWIND})
     * @return This builder instance for method chaining
     */
    public T fromTemplate(String html, Map<String, ?> variables, UIType style) {
        return fromTemplate(html, new TemplateProcessor().setVariables(variables), style);
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
        return this.editElement((uiCommandBuilder, _) -> callback.accept(uiCommandBuilder));
    }
    
    public T editElement(BiConsumer<UICommandBuilder, UIEventBuilder> callback) {
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

    protected void sendDynamicImageIfNeededAsync(PlayerRef pRef, Consumer<DynamicImageBuilder> onComplete) {
        if (pRef == null || !pRef.isValid()) {
            return;
        }
        UUID playerUuid = pRef.getUuid();
        for (UIElementBuilder<?> element : elementRegistry.values()) {
            if (element instanceof DynamicImageBuilder dImg) {
                if (dImg.isImagePathAssigned(playerUuid)) {
                    continue;
                }
                String url = dImg.getImageUrl();
                if (url == null || url.isBlank()) {
                    continue;
                }
                CompletableFuture.runAsync(() -> sendDynamicImage(pRef, dImg), DYNAMIC_IMAGE_EXECUTOR)
                        .thenRun(() -> {
                            if (onComplete != null) {
                                onComplete.accept(dImg);
                            }
                        });
            }
        }
    }

    static void sendDynamicImage(PlayerRef pRef, DynamicImageBuilder dynamicImage) {
        if (pRef == null || dynamicImage == null) {
            HyUIPlugin.getLog().logFinest("REFERENCE WAS INVALID");
            
            return;
        }
        UUID playerUuid = pRef.getUuid();
        String url = dynamicImage.getImageUrl();
        if (url == null || url.isBlank()) {
            HyUIPlugin.getLog().logFinest("URL WAS BLANK OR NULL");
            
            return;
        }
        try {
            HyUIPlugin.getLog().logFinest("Preparing dynamic image from URL: " + url);
            byte[] imageBytes;
            if (dynamicImage instanceof HyvatarImageBuilder hyvatar && !hyvatar.hasCustomImageUrl()) {
                imageBytes = HyvatarUtils.downloadRenderPng(
                        hyvatar.getUsername(),
                        hyvatar.getRenderType(),
                        hyvatar.getSize(),
                        hyvatar.getRotate(),
                        hyvatar.getCape(),
                        playerUuid
                );
            } else {
                imageBytes = PngDownloadUtils.downloadPng(url, playerUuid);
            }

            DynamicImageAsset asset = new DynamicImageAsset(imageBytes, playerUuid);
            DynamicImageAsset.sendToPlayer(pRef.getPacketHandler(), DynamicImageAsset.empty(asset.getSlotIndex()));
            dynamicImage.withImagePath(asset.getPath());
            dynamicImage.setSlotIndex(playerUuid, asset.getSlotIndex());

            DynamicImageAsset.sendToPlayer(pRef.getPacketHandler(), asset);
            HyUIPlugin.getLog().logFinest("Dynamic image sent using path: " + asset.getPath());
        } catch (IllegalStateException e) {
            HyUIPlugin.getLog().logFinest("Failed to allocate dynamic image slot: " + e.getMessage());
        } catch (IOException e) {
            HyUIPlugin.getLog().logFinest("Failed to download dynamic image: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            HyUIPlugin.getLog().logFinest("Dynamic image download interrupted: " + e.getMessage());
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
