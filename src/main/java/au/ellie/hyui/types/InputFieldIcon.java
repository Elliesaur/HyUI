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
 * InputFieldIcon type definition.
 */
public class InputFieldIcon implements HyUIBsonSerializable {
    private HyUIPatchStyle texture;
    private Integer width;
    private Integer height;
    private Integer offset;
    private String side;

    public InputFieldIcon withTexture(HyUIPatchStyle texture) {
        this.texture = texture;
        return this;
    }

    public InputFieldIcon withWidth(int width) {
        this.width = width;
        return this;
    }

    public InputFieldIcon withHeight(int height) {
        this.height = height;
        return this;
    }

    public InputFieldIcon withOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public InputFieldIcon withSide(String side) {
        this.side = side;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (texture != null) doc.set("Texture", texture.toBsonDocument());
        if (width != null) doc.set("Width", width);
        if (height != null) doc.set("Height", height);
        if (offset != null) doc.set("Offset", offset);
        if (side != null) doc.set("Side", side);
    }
}
