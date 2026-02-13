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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class StylePropertySets {
    static final Set<String> ANCHOR = Set.of(
            "Left",
            "Right",
            "Top",
            "Bottom",
            "Height",
            "Full",
            "Horizontal",
            "Vertical",
            "Width",
            "MinWidth",
            "MaxWidth"
    );

    static final Set<String> PADDING = Set.of(
            "Left",
            "Right",
            "Top",
            "Bottom",
            "Full",
            "Horizontal",
            "Vertical"
    );

    static final Set<String> PATCH_STYLE = Set.of(
            "Color",
            "HorizontalBorder",
            "VerticalBorder",
            "TexturePath",
            "Border"
    );

    static final Set<String> LABEL_STYLE = Set.of(
            "HorizontalAlignment",
            "VerticalAlignment",
            "Wrap",
            "FontName",
            "FontSize",
            "TextColor",
            "OutlineColor",
            "LetterSpacing",
            "RenderUppercase",
            "RenderBold",
            "RenderItalics",
            "RenderUnderlined",
            "Alignment"
    );

    static final Set<String> INPUT_FIELD_STYLE = Set.of(
            "FontName",
            "FontSize",
            "TextColor",
            "RenderUppercase",
            "RenderBold",
            "RenderItalics"
    );

    static final Set<String> INPUT_FIELD_ICON = Set.of(
            "Texture",
            "Width",
            "Height",
            "Offset",
            "Side"
    );

    static final Set<String> INPUT_FIELD_BUTTON = Set.of(
            "Texture",
            "HoveredTexture",
            "PressedTexture",
            "Width",
            "Height",
            "Offset",
            "Side"
    );

    static final Set<String> INPUT_FIELD_DECORATION_STATE = Set.of(
            "OutlineSize",
            "Background",
            "OutlineColor",
            "Icon",
            "ClearButtonStyle",
            "ToggleVisibilityButtonStyle"
    );

    static final Set<String> SOUND_STYLE = Set.of(
            "SoundPath",
            "Volume",
            "MinPitch",
            "MaxPitch",
            "StopExistingPlayback"
    );

    static final Set<String> SCROLLBAR_STYLE = Set.of(
            "Size",
            "Spacing",
            "OnlyVisibleWhenHovered",
            "Background",
            "Handle",
            "HoveredHandle",
            "DraggedHandle"
    );

    static final Set<String> TEXT_TOOLTIP_STYLE = Set.of(
            "MaxWidth",
            "Alignment",
            "Background"
    );

    static Set<String> merge(Set<String>... sets) {
        HashSet<String> merged = new HashSet<>();
        for (Set<String> set : sets) {
            if (set != null) {
                merged.addAll(set);
            }
        }
        return Collections.unmodifiableSet(merged);
    }

    private StylePropertySets() {}
}
