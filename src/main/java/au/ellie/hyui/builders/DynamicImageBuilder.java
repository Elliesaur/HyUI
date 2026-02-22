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
import au.ellie.hyui.elements.BackgroundSupported;
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.elements.ScrollbarStyleSupported;
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.types.ScrollbarStyle;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DynamicImageBuilder extends UIElementBuilder<DynamicImageBuilder> 
        implements ScrollbarStyleSupported<DynamicImageBuilder>, 
        LayoutModeSupported<DynamicImageBuilder> {
    private static final String DEFAULT_TEXTURE_PATH = "UI/Custom/Pages/Elements/DynamicImage1.png";

    private String layoutMode;
    private String scrollbarStyleReference;
    private String scrollbarStyleDocument;
    private ScrollbarStyle scrollbarStyle;
    private String imageUrl;
    private String imageFilePath;
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
        return withImageSource(imageUrl);
    }

    public DynamicImageBuilder withImageSource(String imageSource) {
        if (imageSource == null || imageSource.isBlank()) {
            this.imageUrl = imageSource;
            this.imageFilePath = null;
            return this;
        }
        String trimmed = imageSource.trim();
        if (isRemoteUrl(trimmed)) {
            this.imageUrl = trimmed;
            this.imageFilePath = null;
        } else {
            this.imageFilePath = normalizeFilePath(trimmed);
            this.imageUrl = null;
        }
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public DynamicImageBuilder withImageFilePath(String filePath) {
        this.imageFilePath = normalizeFilePath(filePath);
        this.imageUrl = null;
        return this;
    }

    public String getImageFilePath() {
        return imageFilePath;
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
        if ((imageUrl == null || imageUrl.isBlank()) && (imageFilePath == null || imageFilePath.isBlank())) {
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
    public DynamicImageBuilder withScrollbarStyle(String document, String styleReference) {
        this.scrollbarStyleDocument = document;
        this.scrollbarStyleReference = styleReference;
        this.scrollbarStyle = null;
        return this;
    }

    @Override
    public DynamicImageBuilder withScrollbarStyle(ScrollbarStyle style) {
        this.scrollbarStyle = style;
        this.scrollbarStyleDocument = null;
        this.scrollbarStyleReference = null;
        return this;
    }

    @Override
    public String getScrollbarStyleReference() {
        return this.scrollbarStyleReference;
    }

    @Override
    public ScrollbarStyle getScrollbarStyle() {
        return this.scrollbarStyle;
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
            HyUIPlugin.getLog().logFinest("Building dynamic image with URL: " + imageUrl);
        } else if (imageFilePath != null && !imageFilePath.isBlank()) {
            HyUIPlugin.getLog().logFinest("Building dynamic image from file: " + imageFilePath);
        } else if (this.background != null) {
            HyUIPlugin.getLog().logFinest("Building dynamic image from path: " + this.background.getTexturePath());
        }
        applyLayoutMode(commands, selector);
        applyScrollbarStyle(commands, selector);
    }

    @Override
    protected boolean supportsStyling() {
        return false;
    }

    @Override
    protected boolean isStyleWhitelist() {
        return true;
    }

    @Override
    protected Set<String> getSupportedStyleProperties() {
        return Set.of();
    }

    private static UUID normalizePlayerUuid(UUID playerUuid) {
        return playerUuid != null ? playerUuid : DEFAULT_PLAYER_UUID;
    }

    private static boolean isRemoteUrl(String source) {
        String lower = source.toLowerCase(Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private static String normalizeFilePath(String filePath) {
        if (filePath == null) {
            return null;
        }
        String trimmed = filePath.trim();
        if (trimmed.isBlank()) {
            return trimmed;
        }
        if (trimmed.startsWith("file:")) {
            trimmed = trimmed.substring("file:".length());
            while (trimmed.startsWith("/")) {
                trimmed = trimmed.substring(1);
            }
        }
        return trimmed;
    }
}
