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

package au.ellie.hyui.html.ast.utils;

import javax.annotation.Nullable;

public class NumericUtils {
    private static final double EPSILON = 1e-9;

    /**
     * Convertit une valeur en Number si possible
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
     * Convertit un Number en double
     */
    public static double toDouble(Number num) {
        if (num == null)
            return 0.0;

        return num.doubleValue();
    }

    /**
     * Convertit un Number en long
     */
    public static long toLong(Number num) {
        if (num == null)
            return 0L;

        return num.longValue();
    }

    /**
     * Compare deux nombres avec epsilon pour les doubles
     */
    public static int compare(Object left, Object right) {
        Number leftNum = toNumber(left);
        Number rightNum = toNumber(right);

        if (leftNum == null && rightNum == null) return 0;
        if (leftNum == null) return -1;
        if (rightNum == null) return 1;

        // Si au moins un des deux est un double/float, on compare en double avec epsilon
        if (isFloatingPoint(leftNum) || isFloatingPoint(rightNum)) {
            return compareWithEpsilon(toDouble(leftNum), toDouble(rightNum));
        }

        // Sinon, comparaison en long
        return Long.compare(toLong(leftNum), toLong(rightNum));
    }

    /**
     * Vérifie l'égalité entre deux nombres avec epsilon pour les doubles
     */
    public static boolean equals(Object left, Object right) {
        Number leftNum = toNumber(left);
        Number rightNum = toNumber(right);

        if (leftNum == null && rightNum == null) return true;
        if (leftNum == null || rightNum == null) return false;

        // Si au moins un des deux est un double/float, on compare avec epsilon
        if (isFloatingPoint(leftNum) || isFloatingPoint(rightNum)) {
            return Math.abs(toDouble(leftNum) - toDouble(rightNum)) < EPSILON;
        }

        // Sinon, comparaison exacte en long
        return toLong(leftNum) == toLong(rightNum);
    }

    /**
     * Vérifie si un Number est un type à virgule flottante
     */
    private static boolean isFloatingPoint(Number num) {
        return num instanceof Double || num instanceof Float;
    }

    /**
     * Compare deux doubles avec epsilon
     */
    private static int compareWithEpsilon(double a, double b) {
        if (Math.abs(a - b) < EPSILON) return 0;
        return Double.compare(a, b);
    }
}