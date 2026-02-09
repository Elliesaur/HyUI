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

/**
 * Utility class for string manipulation.
 * <p>
 * Used instead of Hytale's StringUtils to support space
 * and dot as word separators in capitalizeAll.
 */
public class StringUtils {

    /**
     * Capitalize the first letter of the string.
     *
     * @param str The string to capitalize
     */
    public static String capitalize(String str) {
        if (str == null || str.isEmpty())
            return str;

        return capitalizeUnsafe(str);
    }

    /**
     * Capitalize the first letter of each word in the string.
     *
     * @param str The string to capitalize
     */
    public static String capitalizeAll(String str) {
        if (str == null || str.isEmpty())
            return str;

        String[] words = str.split("[\\s\\.]+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty())
                continue;

            result.append(capitalizeUnsafe(word)).append(" ");
        }

        return result.toString().trim();
    }

    /**
     * Capitalize the first letter of the string
     * without checking for null or empty.
     *
     * @param str The string to capitalize
     */
    private static String capitalizeUnsafe(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
