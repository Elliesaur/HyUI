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

package au.ellie.hyui.types;

import au.ellie.hyui.builders.HyUIPatchStyle;
import au.ellie.hyui.utils.BsonDocumentHelper;

/**
 * SliderStyle type definition.
 */
public class SliderStyle implements HyUIBsonSerializable {
    private HyUIPatchStyle background;
    private String handle;
    private Integer handleWidth;
    private Integer handleHeight;
    private ButtonSounds sounds;

    public SliderStyle withBackground(HyUIPatchStyle background) {
        this.background = background;
        return this;
    }

    public SliderStyle withHandle(String handle) {
        this.handle = handle;
        return this;
    }

    public SliderStyle withHandleWidth(int handleWidth) {
        this.handleWidth = handleWidth;
        return this;
    }

    public SliderStyle withHandleHeight(int handleHeight) {
        this.handleHeight = handleHeight;
        return this;
    }

    public SliderStyle withSounds(ButtonSounds sounds) {
        this.sounds = sounds;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (background != null) doc.set("Background", background.toBsonDocument());
        if (handle != null) doc.set("Handle", handle);
        if (handleWidth != null) doc.set("HandleWidth", handleWidth);
        if (handleHeight != null) doc.set("HandleHeight", handleHeight);
        if (sounds != null) doc.set("Sounds", sounds.toBsonDocument());
    }
    
    public static SliderStyle defaultStyle() {
        return new SliderStyle()
                .withBackground(new HyUIPatchStyle()
                        .setTexturePath("Common/SliderBackground.png")
                        .setBorder(2))
                .withHandle("Common/SliderHandle.png")
                .withHandleHeight(16)
                .withHandleWidth(16)
                .withSounds((ButtonSounds) new ButtonSounds()
                        .withActivate(new SoundStyle()
                                .withSoundPath("Sounds/UntickActivate.ogg")
                                .withVolume(9f))
                        .withMouseHover(new SoundStyle()
                                .withSoundPath("Sounds/ButtonsLightHover.ogg")
                                .withVolume(6f)));
    }
}
