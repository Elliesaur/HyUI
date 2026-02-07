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

import au.ellie.hyui.builders.HyUIAnchor;
import au.ellie.hyui.builders.HyUIPatchStyle;
import au.ellie.hyui.utils.BsonDocumentHelper;

/**
 * TabNavigationStyle type definition.
 */
public class TabNavigationStyle implements HyUIBsonSerializable {
    private TabStyle tabStyle;
    private TabStyle selectedTabStyle;
    private ButtonSounds tabSounds;
    private HyUIAnchor separatorAnchor;
    private HyUIPatchStyle separatorBackground;

    public TabNavigationStyle withTabStyle(TabStyle tabStyle) {
        this.tabStyle = tabStyle;
        return this;
    }

    public TabNavigationStyle withSelectedTabStyle(TabStyle selectedTabStyle) {
        this.selectedTabStyle = selectedTabStyle;
        return this;
    }

    public TabNavigationStyle withTabSounds(ButtonSounds tabSounds) {
        this.tabSounds = tabSounds;
        return this;
    }

    public TabNavigationStyle withSeparatorAnchor(HyUIAnchor separatorAnchor) {
        this.separatorAnchor = separatorAnchor;
        return this;
    }

    public TabNavigationStyle withSeparatorBackground(HyUIPatchStyle separatorBackground) {
        this.separatorBackground = separatorBackground;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (tabStyle != null) doc.set("TabStyle", tabStyle.toBsonDocument());
        if (selectedTabStyle != null) doc.set("SelectedTabStyle", selectedTabStyle.toBsonDocument());
        if (tabSounds != null) doc.set("TabSounds", tabSounds.toBsonDocument());
        if (separatorAnchor != null) doc.set("SeparatorAnchor", separatorAnchor.toBsonDocument());
        if (separatorBackground != null) doc.set("SeparatorBackground", separatorBackground.toBsonDocument());
    }
}
