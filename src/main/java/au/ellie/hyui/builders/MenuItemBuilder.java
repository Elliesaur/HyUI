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
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.events.MouseEventData;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventActions;
import au.ellie.hyui.types.MenuItemStyle;
import au.ellie.hyui.types.PopupStyle;
import au.ellie.hyui.types.TextTooltipStyle;
import au.ellie.hyui.utils.BsonDocumentHelper;
import au.ellie.hyui.utils.PropertyBatcher;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for MenuItem UI elements.
 */
public class MenuItemBuilder extends UIElementBuilder<MenuItemBuilder> {
    private String text;
    private String textTooltipStyleReference;
    private String textTooltipStyleDocument;
    private TextTooltipStyle textTooltipStyle;
    private String popupStyleReference;
    private String popupStyleDocument;
    private PopupStyle popupStyle;
    private HyUIPatchStyle icon;
    private HyUIAnchor iconAnchor;

    public MenuItemBuilder() {
        super(UIElements.MENU_ITEM, "#HyUIMenuItem");
        withUiFile("Pages/Elements/MenuItem.ui");
        withWrappingGroup(true);
    }

    public static MenuItemBuilder menuItem() {
        return new MenuItemBuilder();
    }

    public MenuItemBuilder withText(String text) {
        this.text = text;
        return this;
    }

    public MenuItemBuilder withTextTooltipStyle(String reference) {
        return withTextTooltipStyle("Common.ui", reference);
    }

    public MenuItemBuilder withTextTooltipStyle(String document, String reference) {
        this.textTooltipStyleDocument = document;
        this.textTooltipStyleReference = reference;
        return this;
    }

    public MenuItemBuilder withTextTooltipStyle(TextTooltipStyle textTooltipStyle) {
        this.textTooltipStyle = textTooltipStyle;
        return this;
    }

    public MenuItemBuilder withPopupStyle(String reference) {
        return withPopupStyle("Common.ui", reference);
    }

    public MenuItemBuilder withPopupStyle(String document, String reference) {
        this.popupStyleDocument = document;
        this.popupStyleReference = reference;
        return this;
    }

    public MenuItemBuilder withPopupStyle(PopupStyle popupStyle) {
        this.popupStyle = popupStyle;
        return this;
    }

    public MenuItemBuilder withSelectedStyle(MenuItemStyle style) {
        return withSecondaryStyle("SelectedStyle", style);
    }

    public MenuItemBuilder withIcon(HyUIPatchStyle icon) {
        this.icon = icon;
        return this;
    }

    public MenuItemBuilder withIconAnchor(HyUIAnchor iconAnchor) {
        this.iconAnchor = iconAnchor;
        return this;
    }

    /**
     * Adds an event listener for the DoubleClicking event.
     */
    public MenuItemBuilder onDoubleClicking(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.DoubleClicking, MouseEventData.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the DoubleClicking event.
     */
    public MenuItemBuilder onDoubleClicking(Consumer<MouseEventData> callback) {
        return addEventListener(CustomUIEventBindingType.DoubleClicking, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the DoubleClicking event with context.
     */
    public MenuItemBuilder onDoubleClicking(BiConsumer<MouseEventData, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.DoubleClicking, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the RightClicking event.
     */
    public MenuItemBuilder onRightClicking(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.RightClicking, MouseEventData.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the RightClicking event.
     */
    public MenuItemBuilder onRightClicking(Consumer<MouseEventData> callback) {
        return addEventListener(CustomUIEventBindingType.RightClicking, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the RightClicking event with context.
     */
    public MenuItemBuilder onRightClicking(BiConsumer<MouseEventData, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.RightClicking, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the MouseEntered event.
     */
    public MenuItemBuilder onMouseEntered(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.MouseEntered, MouseEventData.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the MouseEntered event.
     */
    public MenuItemBuilder onMouseEntered(Consumer<MouseEventData> callback) {
        return addEventListener(CustomUIEventBindingType.MouseEntered, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the MouseEntered event with context.
     */
    public MenuItemBuilder onMouseEntered(BiConsumer<MouseEventData, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.MouseEntered, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the MouseExited event.
     */
    public MenuItemBuilder onMouseExited(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.MouseExited, MouseEventData.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the MouseExited event.
     */
    public MenuItemBuilder onMouseExited(Consumer<MouseEventData> callback) {
        return addEventListener(CustomUIEventBindingType.MouseExited, MouseEventData.class, callback);
    }

    /**
     * Adds an event listener for the MouseExited event with context.
     */
    public MenuItemBuilder onMouseExited(BiConsumer<MouseEventData, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.MouseExited, MouseEventData.class, callback);
    }

    @Override
    protected boolean supportsStyling() {
        return true;
    }

    @Override
    protected boolean isStyleWhitelist() {
        return true;
    }

    @Override
    protected Set<String> getSupportedStyleProperties() {
        return StylePropertySets.merge(
                StylePropertySets.ANCHOR,
                StylePropertySets.PADDING,
                StylePropertySets.PATCH_STYLE,
                StylePropertySets.SOUND_STYLE,
                StylePropertySets.LABEL_STYLE,
                Set.of(
                        "Background",
                        "LabelMaskTexturePath"
                )
        );
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (text != null) {
            HyUIPlugin.getLog().logFinest("Setting Text: " + text + " for " + selector);
            commands.set(selector + ".Text", text);
        }
        if (textTooltipStyleReference != null && textTooltipStyleDocument != null) {
            HyUIPlugin.getLog().logFinest("Setting TextTooltipStyle reference for " + selector);
            commands.set(selector + ".TextTooltipStyle", Value.ref(textTooltipStyleDocument, textTooltipStyleReference));
        } else if (textTooltipStyle != null) {
            BsonDocumentHelper tooltipDoc = PropertyBatcher.beginSet();
            textTooltipStyle.applyTo(tooltipDoc);
            PropertyBatcher.endSet(selector + ".TextTooltipStyle", tooltipDoc, commands);
        }
        if (popupStyleReference != null && popupStyleDocument != null) {
            HyUIPlugin.getLog().logFinest("Setting PopupStyle reference for " + selector);
            commands.set(selector + ".PopupStyle", Value.ref(popupStyleDocument, popupStyleReference));
        } else if (popupStyle != null) {
            BsonDocumentHelper popupDoc = PropertyBatcher.beginSet();
            popupStyle.applyTo(popupDoc);
            PropertyBatcher.endSet(selector + ".PopupStyle", popupDoc, commands);
        }
        if (icon != null) {
            HyUIPlugin.getLog().logFinest("Setting Icon for " + selector);
            commands.setObject(selector + ".Icon", icon.getHytalePatchStyle());
        }
        if (iconAnchor != null) {
            HyUIPlugin.getLog().logFinest("Setting IconAnchor for " + selector);
            commands.setObject(selector + ".IconAnchor", iconAnchor.toHytaleAnchor());
        }

        listeners.forEach(listener -> {
            String eventId = getEffectiveId();
            if (listener.type() == CustomUIEventBindingType.DoubleClicking) {
                HyUIPlugin.getLog().logFinest("Adding DoubleClicking event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.DoubleClicking, selector,
                        EventData.of("Action", UIEventActions.DOUBLE_CLICKING)
                            .append("Target", eventId), false);
            } else if (listener.type() == CustomUIEventBindingType.RightClicking) {
                HyUIPlugin.getLog().logFinest("Adding RightClicking event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.RightClicking, selector,
                        EventData.of("Action", UIEventActions.RIGHT_CLICKING)
                            .append("Target", eventId), false);
            } else if (listener.type() == CustomUIEventBindingType.MouseEntered) {
                HyUIPlugin.getLog().logFinest("Adding MouseEntered event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.MouseEntered, selector,
                        EventData.of("Action", UIEventActions.MOUSE_ENTERED)
                            .append("Target", eventId), false);
            } else if (listener.type() == CustomUIEventBindingType.MouseExited) {
                HyUIPlugin.getLog().logFinest("Adding MouseExited event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.MouseExited, selector,
                        EventData.of("Action", UIEventActions.MOUSE_EXITED)
                            .append("Target", eventId), false);
            }
        });
    }
}
