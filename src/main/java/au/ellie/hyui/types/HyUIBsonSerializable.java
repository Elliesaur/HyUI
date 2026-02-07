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
import org.bson.BsonDocument;

/**
 * Represents a type that can serialize itself into a BSON document.
 */
public interface HyUIBsonSerializable {
    void applyTo(BsonDocumentHelper doc);

    default BsonDocument toBsonDocument() {
        BsonDocumentHelper doc = new BsonDocumentHelper();
        applyTo(doc);
        return doc.getDocument();
    }
}
