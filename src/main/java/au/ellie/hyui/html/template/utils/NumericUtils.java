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

package au.ellie.hyui.html.template.utils;

import javax.annotation.Nullable;

public class NumericUtils {

    // Epsilon value for comparing floating-point numbers thanks of how number
    //  are represented in computers, two floating-point numbers that are very close
    //  may not be exactly equal due to precision issues.
    private static final double EPSILON = 1e-9;

    /**
     * Convert an object to a Number if possible.
     * Supports Number and String types.
     *
     * @param value The object to convert
     * @return Number if conversion is successful, or null if it cannot be converted
     */
    public static Number toNumber(@Nullable Object value) {
        switch (value) {
            case Number num -> {
                return num;
            }

            case String str -> {
                try {
                    if (str.contains("."))
                        return Double.parseDouble(str);
                    else
                        return Long.parseLong(str);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }

            case null, default -> {
                return null;
            }
        }
    }

    /**
     * Convert a number to double.
     *
     * @return 0.0 if num is null
     */
    public static double toDouble(Number num) {
        if (num == null)
            return 0.0;

        return num.doubleValue();
    }

    /**
     * Convert a number to long.
     *
     * @return 0.0 if num is null
     */
    public static long toLong(Number num) {
        if (num == null)
            return 0L;

        return num.longValue();
    }

    /**
     * Compare two objects as numbers.
     * Supports Number and String types.
     *
     * @return A negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
     */
    public static int compare(Object left, Object right) {
        var leftNum = toNumber(left);
        var rightNum = toNumber(right);

        if (leftNum == null && rightNum == null) return 0;
        if (leftNum == null) return -1;
        if (rightNum == null) return 1;

        // if at least one of the two is a floating-point type, compare with epsilon
        if (isFloatingPoint(leftNum) || isFloatingPoint(rightNum))
            return compareWithEpsilon(toDouble(leftNum), toDouble(rightNum));

        // otherwise, compare as long
        return Long.compare(toLong(leftNum), toLong(rightNum));
    }

    /**
     * Check if two objects are numerically equal.
     *
     * @return true if both are null or if they are numerically equal (considering epsilon for floating-point), false otherwise
     */
    public static boolean equals(Object left, Object right) {
        return compare(left, right) == 0;
    }

    /**
     * Check if a number is a floating-point type (Double or Float).
     */
    private static boolean isFloatingPoint(Number num) {
        return num instanceof Double || num instanceof Float;
    }

    /**
     * Compare two doubles with an epsilon tolerance.
     */
    private static int compareWithEpsilon(double a, double b) {
        if (Math.abs(a - b) < EPSILON)
            return 0;

        return Double.compare(a, b);
    }
}