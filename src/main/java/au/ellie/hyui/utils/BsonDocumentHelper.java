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

package au.ellie.hyui.utils;

import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.server.core.Message;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.BsonValue;

/**
 * Helper class for building BsonDocument for UI styles.
 */
public final class BsonDocumentHelper {
    private final BsonDocument document = new BsonDocument();

    public BsonDocumentHelper set(String key, Message message) {
        document.put(key, Message.CODEC.encode(message, EmptyExtraInfo.EMPTY));
        return this;
    }
    
    public BsonDocumentHelper set(String key, String value) {
        document.put(key, new BsonString(value));
        return this;
    }

    public BsonDocumentHelper set(String key, int value) {
        document.put(key, new BsonInt32(value));
        return this;
    }

    public BsonDocumentHelper set(String key, double value) {
        document.put(key, new BsonDouble(value));
        return this;
    }

    public BsonDocumentHelper set(String key, boolean value) {
        document.put(key, new BsonBoolean(value));
        return this;
    }

    public BsonDocumentHelper set(String key, BsonValue value) {
        document.put(key, value);
        return this;
    }

    public BsonDocument getDocument() {
        return document;
    }
}
