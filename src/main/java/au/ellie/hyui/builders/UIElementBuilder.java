package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.theme.Theme;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventListener;
import au.ellie.hyui.utils.BsonDocumentHelper;
import au.ellie.hyui.utils.PropertyBatcher;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A builder class for constructing UI elements with a hierarchical structure and configurable 
 * properties. The {@code UIElementBuilder} class provides an API for specifying attributes such 
 * as styles, visibility, children, tooltips, custom callbacks, and more. This class is intended 
 * to be extended and further customized.
 */
public abstract class UIElementBuilder<T extends UIElementBuilder<T>> {
    protected final Theme theme;
    protected String elementPath;
    protected String uiFilePath;
    protected String id;
    protected String userId;
    protected String style;
    protected HyUIStyle hyUIStyle;
    protected final List<UIEventListener<?>> listeners = new ArrayList<>();
    protected final List<UIElementBuilder<?>> children = new ArrayList<>();
    protected Object initialValue;
    protected String parentSelector = "#Content";
    protected String typeSelector;
    protected boolean wrapInGroup = false;
    protected HyUIAnchor anchor;
    protected HyUIPadding padding;
    protected Boolean visible;
    protected Message tooltipTextSpan;
    protected Boolean hitTestVisible;
    protected Integer flexWeight;
    protected final List<BiConsumer<UICommandBuilder, String>> editAfterCallbacks = new ArrayList<>();
    protected final List<BiConsumer<UICommandBuilder, String>> editBeforeCallbacks = new ArrayList<>();
    protected final Map<String, HyUIStyle> secondaryStyles = new HashMap<>();

    private static int idCounter = 0;

    public UIElementBuilder(String elementPath, String typeSelector) {
        this(Theme.RAW, elementPath, typeSelector);
    }

    public UIElementBuilder(Theme theme, String elementPath, String typeSelector) {
        this.theme = theme != null ? theme : Theme.RAW;
        this.elementPath = this.theme.format(elementPath);
        this.typeSelector = typeSelector;
        this.id = generateUniqueId();
        this.userId = this.id;
    }

    protected abstract void onBuild(UICommandBuilder commands, UIEventBuilder events);

    protected boolean supportsStyling() {
        return false;
    }
    
    public T withUiFile(String uiFilePath) {
        this.uiFilePath = uiFilePath;
        return (T) this;
    }

    public T addChild(UIElementBuilder<?> child) {
        this.children.add(child);
        return (T) this;
    }
    
    public String getEffectiveId() {
        return id;
    }

    public String getElementPath() {
        return elementPath;
    }

    public String getId() {
        return userId;
    }

    public HyUIStyle getHyUIStyle() {
        return hyUIStyle;
    }

    /**
     * @return true if the element uses the @Value (or data-type equiv.) (RefValue) property for its value in events.
     */
    protected boolean usesRefValue() {
        return false;
    }

    /**
     * @return the list of event listeners associated with this element
     */
    public List<UIEventListener<?>> getListeners() {
        return listeners;
    }

    /**
     * Parses the raw value received from a UI event into the appropriate type for this element.
     * Defaults to returning the raw value as a string.
     * 
     * @param rawValue The raw string value from the UI event.
     * @return The parsed value object, or null if parsing fails or is not supported.
     */
    protected Object parseValue(String rawValue) {
        return rawValue;
    }

    public static void resetIdCounter() {
        idCounter = 0;
    }

    @SuppressWarnings("unchecked")
    public T withSecondaryStyle(String property, HyUIStyle style) {
        if (style != null) {
            this.secondaryStyles.put(property, style);
        }
        return (T) this;
    }

    /**
     * @param id the id to set for the element, without leading #.
     * @return the builder instance for method chaining
     */
    @SuppressWarnings("unchecked")
    public T withId(String id) {
        if (id != null) {
            this.userId = id;
            this.id = sanitizeId(id);
        }
        return (T) this;
    }

    private String sanitizeId(String id) {
        if (id == null) return null;
        StringBuilder sb = new StringBuilder();
        sb.append("HYUUID");
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            }
        }
        sb.append(idCounter++);
        return sb.toString();
    }

    /**
     * Deprecated. For removal.
     * @param style the style to apply to the element
     * @return the builder instance for method chaining
     */
    @Deprecated(forRemoval = true)
    @SuppressWarnings("unchecked")
    public T withStyle(String style) {
        this.style = style;
        return (T) this;
    }

    /**
     * Applies the specified style to the current UI element if styling is supported.
     * 
     * @param style the {@code HyUIStyle} instance to be applied to the UI element
     * @return the builder instance of type {@code T} for method chaining
     */
    @SuppressWarnings("unchecked")
    public T withStyle(HyUIStyle style) {
        if (supportsStyling()) {
            this.hyUIStyle = style;
        }
        return (T) this;
    }

    /**
     * Sets the parent selector for the UI element being built.
     * The parent selector specifies the selector of the parent element 
     * in which this element will be nested.
     *
     * @param parentSelector the selector of the parent element
     * @return the builder instance of type {@code T} for method chaining
     */
    @SuppressWarnings("unchecked")
    public T inside(String parentSelector) {
        this.parentSelector = parentSelector;
        return (T) this;
    }

    /**
     * Configures whether the element should be wrapped in a grouping element.
     * This does not need to be accessed by a mod author.
     *
     * @param wrapInGroup a boolean indicating whether the element should be wrapped in a grouping element
     * @return the builder instance of type {@code T} for method chaining
     */
    @SuppressWarnings("unchecked")
    protected T withWrappingGroup(boolean wrapInGroup) {
        this.wrapInGroup = wrapInGroup;
        return (T) this;
    }

    /**
     * Sets the anchor configuration for the UI element being built.
     * The anchor specifies the positional and sizing constraints for the element.
     *
     * @param anchor the {@code HyUIAnchor} instance containing the anchor configuration
     * @return the builder instance of type {@code T} for method chaining
     */
    @SuppressWarnings("unchecked")
    public T withAnchor(HyUIAnchor anchor) {
        this.anchor = anchor;
        return (T) this;
    }

    public HyUIAnchor getAnchor() {
        return anchor;
    }

    /**
     * Sets the padding for the UI element.
     *
     * @param padding the {@code HyUIPadding} instance containing the padding configuration
     * @return the builder instance of type {@code T} for method chaining
     */
    @SuppressWarnings("unchecked")
    public T withPadding(HyUIPadding padding) {
        this.padding = padding;
        return (T) this;
    }

    /**
     * Configures the visibility of the UI element.
     *
     * @param visible a boolean indicating whether the element should be visible
     * @return the builder instance of type {@code T} for method chaining
     */
    @SuppressWarnings("unchecked")
    public T withVisible(boolean visible) {
        this.visible = visible;
        return (T) this;
    }

    /**
     * Configures the tooltip text span for the UI element.
     * This is displayed in-game on mouse hover.
     *
     * @param message the message to be displayed as tooltip
     * @return the builder instance of type {@code T} for method chaining
     */
    @SuppressWarnings("unchecked")
    public T withTooltipTextSpan(Message message) {
        this.tooltipTextSpan = message;
        return (T) this;
    }

    /**
     * Configures the tooltip text for the UI element using a raw string.
     *
     * @param tooltipText the tooltip text to display
     * @return the builder instance of type {@code T} for method chaining
     */
    @SuppressWarnings("unchecked")
    public T withTooltipText(String tooltipText) {
        if (tooltipText != null) {
            this.tooltipTextSpan = Message.raw(tooltipText);
        }
        return (T) this;
    }

    /**
     * Configures whether the element should receive hit-test interactions.
     *
     * @param hitTestVisible whether the element is hit-test visible
     * @return the builder instance of type {@code T} for method chaining
     */
    @SuppressWarnings("unchecked")
    public T withHitTestVisible(boolean hitTestVisible) {
        this.hitTestVisible = hitTestVisible;
        return (T) this;
    }

    /**
     * Sets the flex weight for the UI element.
     * Flex weight determines how the element is sized relative to its siblings
     * within a flex container.
     *
     * @param weight the flex weight value
     * @return the builder instance of type {@code T} for method chaining
     */
    @SuppressWarnings("unchecked")
    public T withFlexWeight(int weight) {
        this.flexWeight = weight;
        return (T) this;
    }

    /**
     * Registers a callback to modify the UI element after its initial configuration.
     * This method adds the provided callback to a list of "edit after" callbacks,
     * which will be executed after the element is built or modified.
     *
     * @param callback a {@code BiConsumer} that accepts a {@code UICommandBuilder} instance
     *                 and a {@code String} as parameters. The {@code UICommandBuilder}
     *                 is used to modify the UI commands, and the {@code String} represents
     *                 the element's path or identifier.
     * @return the current builder instance of type {@code T} for method chaining
     */
    @SuppressWarnings("unchecked")
    public T editElementAfter(BiConsumer<UICommandBuilder, String> callback) {
        this.editAfterCallbacks.add(callback);
        return (T) this;
    }

    /**
     * Registers a callback to modify the UI element before its initial configuration.
     * This method adds the provided callback to a list of "edit before" callbacks,
     * which will be executed before the element is built or modified.
     *
     * @param callback a {@code BiConsumer} that accepts a {@code UICommandBuilder} instance
     *                 and a {@code String} as parameters. The {@code UICommandBuilder}
     *                 is used to modify the UI commands, and the {@code String} represents 
     *                 the element's path or identifier.
     * @return the current builder instance of type {@code T} for method chaining
     */
    @SuppressWarnings("unchecked")
    public T editElementBefore(BiConsumer<UICommandBuilder, String> callback) {
        this.editBeforeCallbacks.add(callback);
        return (T) this;
    }

    /**
     * Handles the building process of a UI element, optionally wrapping it in a group if configured.
     * This method modifies the structure and commands for the UI element being constructed.
     *
     * @param commands an instance of {@code UICommandBuilder} used to construct UI commands
     * @param events   an instance of {@code UIEventBuilder} used to register and handle UI events
     */
    protected void build(UICommandBuilder commands, UIEventBuilder events) {
        
        if (wrapInGroup && parentSelector != null) {
            String wrappingGroupId = getWrappingGroupId();
            HyUIPlugin.getLog().logInfo("Creating wrapping group: #" + wrappingGroupId + " for element: " + (typeSelector != null ? typeSelector : elementPath));
            
            StringBuilder inlineMarkup = new StringBuilder();
            inlineMarkup.append("Group #").append(wrappingGroupId).append(" { ");
            
            if (padding != null) {
                StringBuilder paddingMarkup = new StringBuilder();
                if (padding.getLeft() != null) paddingMarkup.append("Left: ").append(padding.getLeft());
                if (padding.getTop() != null) {
                    if (paddingMarkup.length() > 0) paddingMarkup.append(", ");
                    paddingMarkup.append("Top: ").append(padding.getTop());
                }
                if (padding.getRight() != null) {
                    if (paddingMarkup.length() > 0) paddingMarkup.append(", ");
                    paddingMarkup.append("Right: ").append(padding.getRight());
                }
                if (padding.getBottom() != null) {
                    if (paddingMarkup.length() > 0) paddingMarkup.append(", ");
                    paddingMarkup.append("Bottom: ").append(padding.getBottom());
                }
                if (paddingMarkup.length() > 0) {
                    inlineMarkup.append("Padding: (").append(paddingMarkup).append("); ");
                }
            }

            inlineMarkup.append("}");
            commands.appendInline(parentSelector, inlineMarkup.toString());
            
            // The inner element should be inside the wrapping group
            String originalParent = parentSelector;
            parentSelector = "#" + wrappingGroupId;
            executeBuild(commands, events);
            parentSelector = originalParent;
        } else {
            executeBuild(commands, events);
        }
    }

    protected boolean hasCustomInlineContent() {
        return false;
    }

    protected String generateCustomInlineContent() {
        return null;
    }

    protected void buildBase(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        HyUIPlugin.getLog().logInfo("Building element: " + (typeSelector != null ? typeSelector : elementPath) + " with ID: " + id + " at selector: " + selector);

        if (parentSelector != null) {
            String path = getAppendPath();
            if (path != null && path.endsWith(".ui")) {
                HyUIPlugin.getLog().logInfo("Appending UI file: " + path + " to " + parentSelector);
                commands.append(parentSelector, path);
                
                // If it's a file but NOT wrapped, we need to set the ID of the root element in that file
                // if it's not already correct.
                if (!wrapInGroup && !id.equals(typeSelector != null ? typeSelector.replace("#", "") : "")) {
                     // We might need to rename the element we just appended.
                     // Let's assume for now that if it's not wrapped, it's a singleton or handled by user.
                }
            } else if (hasCustomInlineContent()) {
                String inline = generateCustomInlineContent();
                HyUIPlugin.getLog().logInfo("Appending custom inline: " + inline + " to " + parentSelector);
                commands.appendInline(parentSelector, inline);
            } else {
                String inline = generateBasicInlineMarkup();
                HyUIPlugin.getLog().logInfo("Appending inline: " + inline + " to " + parentSelector);
                commands.appendInline(parentSelector, inline);
            }
            
            if (anchor != null) {
                HyUIPlugin.getLog().logInfo("Setting Anchor for " + selector);
                commands.setObject(selector + ".Anchor", anchor.toHytaleAnchor());
            }

            if (padding != null && !wrapInGroup) {
                HyUIPlugin.getLog().logInfo("Setting Padding for " + selector);
                if (padding.getLeft() != null) commands.set(selector + ".Padding.Left", padding.getLeft());
                if (padding.getTop() != null) commands.set(selector + ".Padding.Top", padding.getTop());
                if (padding.getRight() != null) commands.set(selector + ".Padding.Right", padding.getRight());
                if (padding.getBottom() != null) commands.set(selector + ".Padding.Bottom", padding.getBottom());
            }

            if (visible != null) {
                HyUIPlugin.getLog().logInfo("Setting Visible: " + visible + " for " + selector);
                commands.set(selector + ".Visible", visible);
            }

            if (tooltipTextSpan != null) {
                HyUIPlugin.getLog().logInfo("Setting TooltipTextSpans for " + selector);
                commands.set(selector + ".TooltipTextSpans", tooltipTextSpan);
            }

            if (hitTestVisible != null) {
                HyUIPlugin.getLog().logInfo("Setting HitTestVisible: " + hitTestVisible + " for " + selector);
                commands.set(selector + ".HitTestVisible", hitTestVisible);
            }

            if (flexWeight != null) {
                String flexSelector = wrapInGroup ? "#" + getWrappingGroupId() : selector;
                HyUIPlugin.getLog().logInfo("Setting FlexWeight: " + flexWeight + " for " + flexSelector);
                commands.set(flexSelector + ".FlexWeight", flexWeight);
            }

            if (hyUIStyle != null) {
                BsonDocumentHelper doc = PropertyBatcher.beginSet();
                applyStyle(commands, selector + ".Style", hyUIStyle, doc);
                PropertyBatcher.endSet(selector + ".Style", doc, commands);
                applyRawStyleProperties(commands, selector + ".Style", hyUIStyle);
                hyUIStyle.getStates().forEach((state, nestedStyle) -> {
                    BsonDocumentHelper innerDoc = PropertyBatcher.beginSet();
                    applyStyle(commands, selector + ".Style." + state, nestedStyle, innerDoc);
                    PropertyBatcher.endSet(selector + ".Style." + state, doc, commands);
                    applyRawStyleProperties(commands, selector + ".Style." + state, nestedStyle);
                });
            }

            secondaryStyles.forEach((property, style) -> {
                BsonDocumentHelper doc = PropertyBatcher.beginSet();
                applyStyle(commands, selector + "." + property, style, doc);
                PropertyBatcher.endSet(selector + "." + property, doc, commands);
                applyRawStyleProperties(commands, selector + "." + property, style);
                style.getStates().forEach((state, nestedStyle) -> {
                    BsonDocumentHelper innerDoc = PropertyBatcher.beginSet();
                    applyStyle(commands, selector + "." + property + "." + state, nestedStyle, innerDoc);
                    PropertyBatcher.endSet(selector + "." + property + "." + state, doc, commands);
                    applyRawStyleProperties(commands, selector + "." + property + "." + state, nestedStyle);
                });
            });
        }
    }

    protected Set<String> getUnsupportedStyleProperties() {
        return Set.of();
    }

    protected boolean isStyleWhitelist() {
        return false;
    }

    protected Set<String> getSupportedStyleProperties() {
        return Set.of();
    }

    @SuppressWarnings("unchecked")
    public <V> T addEventListener(CustomUIEventBindingType type, Class<V> valueClass, Consumer<V> callback) {
        return addEventListenerInternal(type, callback);
    }

    @SuppressWarnings("unchecked")
    public <V> T addEventListenerWithContext(CustomUIEventBindingType type, Class<V> valueClass, BiConsumer<V, UIContext> callback) {
        return addEventListenerInternal(type, callback);
    }

    @SuppressWarnings("unchecked")
    protected <V> T addEventListenerInternal(CustomUIEventBindingType type, Consumer<V> callback) {
        this.listeners.add(new UIEventListener<>(type, (val, ctx) -> ((Consumer<Object>) callback).accept(val)));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    protected <V> T addEventListenerInternal(CustomUIEventBindingType type, BiConsumer<V, UIContext> callback) {
        this.listeners.add(new UIEventListener<>(type, callback));
        return (T) this;
    }

    protected String getAppendPath() {
        if (uiFilePath != null) {
            return uiFilePath;
        }
        return elementPath;
    }

    protected String generateBasicInlineMarkup() {
        StringBuilder sb = new StringBuilder();
        sb.append(elementPath);
        if (id != null && !wrapInGroup) {
            sb.append(" #").append(id);
        }
        
        sb.append(" {");
        sb.append("}");
        
        return sb.toString();
    }

    protected String getSelector() {
        if (wrapInGroup) {
            return "#" + id + " " + typeSelector;
        }
        return "#" + id;
    }

    /**
     * Applies the provided style settings to the given command builder while handling unsupported properties.
     *
     * @param commands The UICommandBuilder used to set style properties.
     * @param prefix A string used as a prefix for property keys when applying the styles.
     * @param style An instance of HyUIStyle containing the properties to be applied to the command builder.
     */
    protected void applyStyle(UICommandBuilder commands, String prefix, HyUIStyle style, BsonDocumentHelper doc) {
        if (style.getStyleReference() != null) {
            HyUIPlugin.getLog().logInfo("Applying style reference: " + style.getStyleDocument() + " -> " + style.getStyleReference() + " to " + prefix);
            commands.set(prefix, com.hypixel.hytale.server.core.ui.Value.ref(style.getStyleDocument(), style.getStyleReference()));
            return;
        }

        boolean whitelist = isStyleWhitelist();
        Set<String> supported = whitelist ? getSupportedStyleProperties() : Set.of();
        Set<String> unsupported = whitelist ? Set.of() : getUnsupportedStyleProperties();
        java.util.function.Predicate<String> isAllowed = property -> {
            if (whitelist) {
                return supported.contains(property);
            }
            return !unsupported.contains(property);
        };
        
        if (style.getFontSize() != null && isAllowed.test("FontSize")) {
            HyUIPlugin.getLog().logInfo("Setting Style FontSize: " + style.getFontSize() + " for " + prefix);
            doc.set("FontSize", style.getFontSize().doubleValue());
        }
        if (style.getRenderBold() != null && isAllowed.test("RenderBold")) {
            HyUIPlugin.getLog().logInfo("Setting Style RenderBold: " + style.getRenderBold() + " for " + prefix);
            doc.set("RenderBold", style.getRenderBold());
        }
        if (style.getRenderUppercase() != null && isAllowed.test("RenderUppercase")) {
            HyUIPlugin.getLog().logInfo("Setting Style RenderUppercase: " + style.getRenderUppercase() + " for " + prefix);
            doc.set("RenderUppercase", style.getRenderUppercase());
        }
        if (style.getRenderItalics() != null && isAllowed.test("RenderItalics")) {
            HyUIPlugin.getLog().logInfo("Setting Style RenderItalics: " + style.getRenderItalics() + " for " + prefix);
            doc.set("RenderItalics", style.getRenderItalics());
        }
        if (style.getTextColor() != null && isAllowed.test("TextColor")) {
            HyUIPlugin.getLog().logInfo("Setting Style TextColor: " + style.getTextColor() + " for " + prefix);
            doc.set("TextColor", style.getTextColor());
        }
        if (style.getLetterSpacing() != null && isAllowed.test("LetterSpacing")) {
            HyUIPlugin.getLog().logInfo("Setting Style LetterSpacing: " + style.getLetterSpacing() + " for " + prefix);
            doc.set("LetterSpacing", style.getLetterSpacing());
        }
        if (style.getWrap() != null && isAllowed.test("Wrap")) {
            HyUIPlugin.getLog().logInfo("Setting Style Wrap: " + style.getWrap() + " for " + prefix);
            doc.set("Wrap", style.getWrap());
        }
        if (style.getFontName() != null && isAllowed.test("FontName")) {
            HyUIPlugin.getLog().logInfo("Setting Style FontName: " + style.getFontName() + " for " + prefix);
            doc.set("FontName", style.getFontName());
        }
        if (style.getOutlineColor() != null && isAllowed.test("OutlineColor")) {
            HyUIPlugin.getLog().logInfo("Setting Style OutlineColor: " + style.getOutlineColor() + " for " + prefix);
            doc.set("OutlineColor", style.getOutlineColor());
        }
        if (style.getHorizontalAlignment() != null && isAllowed.test("HorizontalAlignment")) {
            HyUIPlugin.getLog().logInfo("Setting Style HorizontalAlignment: " + style.getHorizontalAlignment() + " for " + prefix);
            doc.set("HorizontalAlignment", style.getHorizontalAlignment().name());
        }
        if (style.getVerticalAlignment() != null && isAllowed.test("VerticalAlignment")) {
            HyUIPlugin.getLog().logInfo("Setting Style VerticalAlignment: " + style.getVerticalAlignment() + " for " + prefix);
            doc.set("VerticalAlignment", style.getVerticalAlignment().name());
        }
        if (style.getAlignment() != null && isAllowed.test("Alignment")) {
            HyUIPlugin.getLog().logInfo("Setting Style Alignment: " + style.getAlignment() + " for " + prefix);
            doc.set("Alignment", style.getAlignment().name());
        }
    }
    
    protected void applyRawStyleProperties(UICommandBuilder commands, String prefix, HyUIStyle style) {
        boolean whitelist = isStyleWhitelist();
        Set<String> supported = whitelist ? getSupportedStyleProperties() : Set.of();
        Set<String> unsupported = whitelist ? Set.of() : getUnsupportedStyleProperties();
        java.util.function.Predicate<String> isAllowed = property -> {
            if (whitelist) {
                return supported.contains(property);
            }
            return !unsupported.contains(property);
        };
        style.getRawProperties().forEach((key, value) -> {
            if (!isAllowed.test(key)) {
                return;
            }
            var fullPrefix = prefix + "." + key;
            HyUIPlugin.getLog().logInfo("Setting Style Raw Property: " + key + "=" + value + " for " + prefix);
            switch (value) {
                case String s -> commands.set(fullPrefix, s);
                case Boolean b -> commands.set(fullPrefix, b);
                case Double v -> commands.set(fullPrefix, v);
                case Integer i -> commands.set(fullPrefix, i);
                case Float v -> commands.set(fullPrefix, v);
                case null, default -> commands.set(fullPrefix, String.valueOf(value));
            }
        });
    }
    protected String getWrappingGroupId() {
        return id;
    }

    /**
     * Builds the child elements of the current UI element and registers their commands
     * and events within the provided builders. If the current element has a selector, 
     * each child is nested inside that selector during the build process.
     *
     * @param commands an instance of {@code UICommandBuilder} used for constructing 
     *                 UI commands associated with the child elements
     * @param events   an instance of {@code UIEventBuilder} used for setting up event 
     *                 handling for the child elements
     */
    protected void buildChildren(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector != null) {
            for (UIElementBuilder<?> child : children) {
                String originalParent = child.parentSelector;
                child.inside(selector).build(commands, events);
                child.inside(originalParent);
            }
        }
    }

    private void executeBuild(UICommandBuilder commands, UIEventBuilder events) {
        buildBase(commands, events);

        String selector = getSelector();
        for (BiConsumer<UICommandBuilder, String> callback : editBeforeCallbacks) {
            callback.accept(commands, selector);
        }

        onBuild(commands, events);
        buildChildren(commands, events);

        for (BiConsumer<UICommandBuilder, String> callback : editAfterCallbacks) {
            callback.accept(commands, selector);
        }
    }

    private String generateUniqueId() {
        String base = elementPath;
        if (base != null) {
            // Get last part of the path
            int lastSlash = base.lastIndexOf('/');
            if (lastSlash != -1) {
                base = base.substring(lastSlash + 1);
            }
            // Replace .ui with blank
            base = base.replace(".ui", "")
                    .replace("$C.@", "");
        } else {
            base = "Element";
        }
        return sanitizeId(base);
    }
}
