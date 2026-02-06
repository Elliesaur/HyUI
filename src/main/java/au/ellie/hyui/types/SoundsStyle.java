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

import au.ellie.hyui.utils.BsonDocumentHelper;

/**
 * SoundsStyle type definition.
 */
public class SoundsStyle implements HyUIBsonSerializable {
    private SoundStyle activate;
    private SoundStyle mouseHover;
    private SoundStyle close;
    private SoundStyle context;

    public SoundsStyle withActivate(SoundStyle activate) {
        this.activate = activate;
        return this;
    }

    public SoundsStyle withMouseHover(SoundStyle mouseHover) {
        this.mouseHover = mouseHover;
        return this;
    }

    public SoundsStyle withClose(SoundStyle close) {
        this.close = close;
        return this;
    }

    public SoundsStyle withContext(SoundStyle context) {
        this.context = context;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (activate != null) doc.set("Activate", activate.toBsonDocument());
        if (mouseHover != null) doc.set("MouseHover", mouseHover.toBsonDocument());
        if (close != null) doc.set("Close", close.toBsonDocument());
        if (context != null) doc.set("Context", context.toBsonDocument());
    }
}
