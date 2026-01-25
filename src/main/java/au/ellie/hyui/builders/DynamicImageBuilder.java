package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.assets.DynamicImageAsset;
import au.ellie.hyui.elements.BackgroundSupported;
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.elements.ScrollbarStyleSupported;
import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

public class DynamicImageBuilder extends UIElementBuilder<DynamicImageBuilder> 
        implements BackgroundSupported<DynamicImageBuilder>, 
        ScrollbarStyleSupported<DynamicImageBuilder>, 
        LayoutModeSupported<DynamicImageBuilder> {
    private static final String DEFAULT_TEXTURE_PATH = "UI/Custom/Pages/Elements/DynamicImage1.png";

    private String layoutMode;
    private HyUIPatchStyle background;
    private String scrollbarStyleReference;
    private String scrollbarStyleDocument;
    private String imageUrl;
    private boolean imagePathAssigned;
    private Integer slotIndex;

    public DynamicImageBuilder() {
        super(UIElements.GROUP, "Group");
        this.background = new HyUIPatchStyle().setTexturePath(DEFAULT_TEXTURE_PATH);
    }

    public static DynamicImageBuilder dynamicImage() {
        return new DynamicImageBuilder();
    }

    public DynamicImageBuilder withImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImagePathAssigned(boolean imagePathAssigned) {
        this.imagePathAssigned = imagePathAssigned;
    }

    public DynamicImageBuilder withImagePath(String texturePath) {
        if (this.background == null) {
            this.background = new HyUIPatchStyle();
        }
        texturePath = texturePath.replace("UI/Custom/", "");
        this.background.setTexturePath(texturePath);
        this.imagePathAssigned = true;
        return this;
    }

    public boolean isImagePathAssigned() {
        return imagePathAssigned;
    }

    public void setSlotIndex(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public Integer getSlotIndex() {
        return slotIndex;
    }

    public void invalidateImage() {
        if (slotIndex != null) {
            DynamicImageAsset.releaseSlotIndex(slotIndex);
        }
        this.slotIndex = null;
        this.imagePathAssigned = false;
    }

    @Override
    public DynamicImageBuilder withLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    @Override
    public String getLayoutMode() {
        return this.layoutMode;
    }

    @Override
    public DynamicImageBuilder withBackground(HyUIPatchStyle background) {
        this.background = background;
        return this;
    }

    @Override
    public HyUIPatchStyle getBackground() {
        return this.background;
    }

    @Override
    public DynamicImageBuilder withScrollbarStyle(String document, String styleReference) {
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
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (imageUrl != null && !imageUrl.isBlank()) {
            HyUIPlugin.getLog().logInfo("Building dynamic image with URL: " + imageUrl);
        } else if (this.background != null) {
            HyUIPlugin.getLog().logInfo("Building dynamic image from path: " + this.background.getTexturePath());
        }
        applyLayoutMode(commands, selector);
        applyBackground(commands, selector);
        applyScrollbarStyle(commands, selector);
    }

    @Override
    protected boolean supportsStyling() {
        return false;
    }
}
