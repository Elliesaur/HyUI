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
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

/**
 * Builder for the Sprite UI element.
 * A sprite is an animated image that uses a spritemap.
 * 
 * UNUSED. Buggy.
 */
public class SpriteBuilder extends UIElementBuilder<SpriteBuilder> implements LayoutModeSupported<SpriteBuilder> {
    private String texturePath;
    private Frame frame;
    private Integer framesPerSecond;
    private String layoutMode;

    public static class Frame {
        private final int width;
        private final int height;
        private final int perRow;
        private final int count;

        public Frame(int width, int height, int perRow, int count) {
            this.width = width;
            this.height = height;
            this.perRow = perRow;
            this.count = count;
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public int getPerRow() { return perRow; }
        public int getCount() { return count; }
    }

    public SpriteBuilder() {
        super(UIElements.SPRITE, "#HyUISprite");
        withWrappingGroup(true);
    }

    /**
     * Factory method to create a SpriteBuilder instance.
     *
     * @return a SpriteBuilder configured for creating a sprite element.
     */
    public static SpriteBuilder sprite() {
        return new SpriteBuilder();
    }

    /**
     * Sets the texture path for the sprite.
     *
     * @param texturePath the path to the spritemap texture.
     * @return this SpriteBuilder instance.
     */
    public SpriteBuilder withTexture(String texturePath) {
        this.texturePath = texturePath;
        return this;
    }

    /**
     * Sets the frame information for the sprite.
     *
     * @param width  Width of a single frame.
     * @param height Height of a single frame.
     * @param perRow Number of frames per row in the spritemap.
     * @param count  Total number of frames.
     * @return this SpriteBuilder instance.
     */
    public SpriteBuilder withFrame(int width, int height, int perRow, int count) {
        this.frame = new Frame(width, height, perRow, count);
        return this;
    }

    /**
     * Sets the animation speed in frames per second.
     *
     * @param fps the number of frames to play per second.
     * @return this SpriteBuilder instance.
     */
    public SpriteBuilder withFramesPerSecond(int fps) {
        this.framesPerSecond = fps;
        return this;
    }

    @Override
    public SpriteBuilder withLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    @Override
    public String getLayoutMode() {
        return this.layoutMode;
    }

    // TODO: Technically it supports padding.
    @Override
    protected boolean supportsStyling() {
        return false;
    }

    @Override
    protected boolean hasCustomInlineContent() {
        return true;
    }

    @Override
    protected String generateCustomInlineContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("Sprite #HyUISprite").append(" { ");
        if (texturePath != null) {
            sb.append("TexturePath: \"").append(texturePath).append("\"; ");
        }
        if (frame != null) {
            sb.append("Frame: (Width: ").append(frame.getWidth())
              .append(", Height: ").append(frame.getHeight())
              .append(", PerRow: ").append(frame.getPerRow())
              .append(", Count: ").append(frame.getCount()).append("); ");
        }
        if (framesPerSecond != null) {
            sb.append("FramesPerSecond: ").append(framesPerSecond).append("; ");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        String wrappingGroupSelector = "#" + getWrappingGroupId();
        applyLayoutMode(commands, wrappingGroupSelector);
    }
}
