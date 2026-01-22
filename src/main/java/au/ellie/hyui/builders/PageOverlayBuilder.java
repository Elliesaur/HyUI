package au.ellie.hyui.builders;

import au.ellie.hyui.elements.BackgroundSupported;
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.elements.ScrollbarStyleSupported;
import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

/**
 * Builder for the PageOverlay UI element.
 */
public class PageOverlayBuilder extends UIElementBuilder<PageOverlayBuilder> implements LayoutModeSupported<PageOverlayBuilder>, BackgroundSupported<PageOverlayBuilder>, ScrollbarStyleSupported<PageOverlayBuilder> {
    private String layoutMode;
    private HyUIPatchStyle background;
    private String scrollbarStyleReference;
    private String scrollbarStyleDocument;

    public PageOverlayBuilder() {
        super(UIElements.PAGE_OVERLAY, "#HyUIPageOverlay");
        withWrappingGroup(true);
        withUiFile("Pages/Elements/PageOverlay.ui");
    }

    /**
     * Creates a PageOverlayBuilder instance for a page overlay element.
     *
     * @return a PageOverlayBuilder configured for creating a page overlay with predefined settings.
     */
    public static PageOverlayBuilder pageOverlay() {
        return new PageOverlayBuilder();
    }

    @Override
    public PageOverlayBuilder withLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    @Override
    public String getLayoutMode() {
        return this.layoutMode;
    }

    @Override
    public PageOverlayBuilder withBackground(HyUIPatchStyle background) {
        this.background = background;
        return this;
    }

    @Override
    public HyUIPatchStyle getBackground() {
        return this.background;
    }

    @Override
    public PageOverlayBuilder withScrollbarStyle(String document, String styleReference) {
        this.scrollbarStyleDocument = document;
        this.scrollbarStyleReference = styleReference;
        return this;
    }

    @Override
    public String getScrollbarStyleReference() {
        return this.scrollbarStyleReference;
    }

    @Override
    public String getScrollbarStyleDocument() {
        return this.scrollbarStyleDocument;
    }

    @Override
    protected boolean supportsStyling() {
        return false;
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        applyLayoutMode(commands, selector);
        applyBackground(commands, selector);
        applyScrollbarStyle(commands, selector);
    }
}
