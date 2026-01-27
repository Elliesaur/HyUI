package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.assets.DynamicImageAsset;
import au.ellie.hyui.elements.BackgroundSupported;
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.elements.ScrollbarStyleSupported;
import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private final Map<UUID, Integer> slotIndexes = new HashMap<>();
    private static final UUID DEFAULT_PLAYER_UUID = new UUID(0L, 0L);

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

    public DynamicImageBuilder withImagePath(String texturePath) {
        if (this.background == null) {
            this.background = new HyUIPatchStyle();
        }
        texturePath = texturePath.replace("UI/Custom/", "");
        this.background.setTexturePath(texturePath);
        this.imagePathAssigned = true;
        return this;
    }

    public boolean isImagePathAssigned(UUID playerUuid) {
        if (!imagePathAssigned) {
            return false;
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            return true;
        }
        return slotIndexes.containsKey(normalizePlayerUuid(playerUuid));
    }
    
    public void setSlotIndex(UUID playerUuid, int slotIndex) {
        slotIndexes.put(normalizePlayerUuid(playerUuid), slotIndex);
    }

    public void invalidateImage() {
        for (Map.Entry<UUID, Integer> entry : slotIndexes.entrySet()) {
            DynamicImageAsset.releaseSlotIndex(entry.getKey(), entry.getValue());
        }
        slotIndexes.clear();
        this.imagePathAssigned = false;
    }

    public void invalidateImage(UUID playerUuid) {
        releaseSlotForPlayer(playerUuid);
    }

    public void releaseSlotForPlayer(UUID playerUuid) {
        Integer slotIndex = slotIndexes.remove(normalizePlayerUuid(playerUuid));
        if (slotIndex != null) {
            DynamicImageAsset.releaseSlotIndex(playerUuid, slotIndex);
        }
    }

    @Override
    protected void applyTemplate(UIElementBuilder<?> template) {
        HyUIPatchStyle currentBackground = this.background;
        boolean currentImagePathAssigned = this.imagePathAssigned;
        Map<UUID, Integer> currentSlotIndexes = new HashMap<>(this.slotIndexes);

        super.applyTemplate(template);

        if (currentImagePathAssigned) {
            this.background = currentBackground;
            this.imagePathAssigned = true;
            this.slotIndexes.clear();
            this.slotIndexes.putAll(currentSlotIndexes);
        }
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

    private static UUID normalizePlayerUuid(UUID playerUuid) {
        return playerUuid != null ? playerUuid : DEFAULT_PLAYER_UUID;
    }
}
