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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class ObjectUtils {

    /**
     * Convert an object to a boolean value.
     *
     * @param value The object to convert
     * @return The boolean value
     */
    public static boolean toBoolean(Object value) {
        return switch (value) {
            case null -> false;
            case Boolean b -> b;
            case Number n -> n.doubleValue() != 0;
            case String s -> !s.isEmpty();
            case Collection<?> c -> !c.isEmpty();
            case Map<?, ?> m -> !m.isEmpty();
            default -> true;
        };
    }

    /**
     * Convert an object to an iterable or throw an exception.
     *
     * @param value The object to convert
     * @return The iterable
     */
    public static Iterable<?> toIterable(Object value) {
        if (value instanceof Iterable<?> iterable)
            return iterable;

        if (value instanceof Map<?, ?> map)
            return map.entrySet();

        if (value.getClass().isArray())
            return Arrays.asList((Object[]) value);

        throw new RuntimeException("Cannot iterate over " + value.getClass());
    }

    /**
     * Evaluate if needle is in haystack.
     *
     * @param needle   Object to search for
     * @param haystack Object to search in
     * @return True if needle is in haystack, false otherwise
     */
    public static boolean containedIn(Object needle, Object haystack) {
        return switch (haystack) {
            case Collection<?> collection -> collection.contains(needle);
            case Map<?, ?> map -> map.containsKey(needle);
            case String str when needle != null -> str.contains(needle.toString());
            case null, default -> false;
        };
    }
}
