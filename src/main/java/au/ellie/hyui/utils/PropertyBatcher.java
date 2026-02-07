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

import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommandType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Utility for applying set() calls in bulk to Hytale UI elements using BSON-based property setting.
 */
public final class PropertyBatcher {

    private static final Method INTERNAL_SETTER;
    
    static {
        try {
            INTERNAL_SETTER = UICommandBuilder.class.getDeclaredMethod("setBsonValue", String.class, BsonValue.class);
            INTERNAL_SETTER.setAccessible(true);
            
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Hytale UI internals incompatible with this version of PropertyBatcher", e);
        }
    }

    /**
     * Starts a batch of style property additions.
     * @return A new BsonDocumentHelper to collect properties.
     */
    public static BsonDocumentHelper beginSet() {
        return new BsonDocumentHelper();
    }

    /**
     * Submits a collected batch of properties to the specified UI element.
     */
    public static void endSet(String targetSelector, BsonDocumentHelper helper, UICommandBuilder builder) {
        // Make sure to not set an empty doc.
        if (helper.getDocument().isEmpty()) {
            return;
        }
        endSet(targetSelector, helper.getDocument(), builder);
    }

    private PropertyBatcher() {}

    public static void endSet(String targetSelector, BsonDocument bsonDocument, UICommandBuilder builder) {
        try {
            INTERNAL_SETTER.invoke(builder, targetSelector, bsonDocument);
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply BSON styles to " + targetSelector, e);
        }
    }

    public static void setBsonValue(String targetSelector, BsonValue value, UICommandBuilder builder) {
        if (value == null) {
            return;
        }
        try {
            INTERNAL_SETTER.invoke(builder, targetSelector, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply BSON value to " + targetSelector, e);
        }
    }
    
    public static void addArrayCommand(String selector, List<Message> messages, UICommandBuilder builder) {
        if (messages == null) {
            return;
        }
        try {
            var fieldVal = UICommandBuilder.class.getDeclaredField("commands");
            fieldVal.setAccessible(true);
            
            ObjectArrayList<CustomUICommand> arrayList = (ObjectArrayList<CustomUICommand>) fieldVal.get(builder);
            
            BsonDocument wrapper = new BsonDocument();
            for (int i = 0; i < messages.size(); i++) {
                var item = messages.get(i);
                wrapper.put(String.valueOf(i), Message.CODEC.encode(item));
            }
            arrayList.add(
                    new CustomUICommand(
                            CustomUICommandType.Set, selector, wrapper.toJson(), null)
                    );
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply BSON value to " + selector, e);
        }
    }
}
