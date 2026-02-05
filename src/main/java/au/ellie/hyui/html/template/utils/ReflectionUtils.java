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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for reflection-related operations.
 * Based on the work of Dr Heinz M. Kabutz
 */
public class ReflectionUtils {

    /**
     * Get a truly public method (i.e., a method that is public and declared in a public class or interface)
     * from the given class or its superclasses/interfaces, avoiding calling method on packages with restricted access.
     *
     * @param clazz      Class to inspect
     * @param name       Method name
     * @param paramTypes Parameter types
     * @return Optional containing the Method if found, or empty otherwise
     */
    public static Optional<Method> getPublicMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        if (clazz == null)
            return Optional.empty();

        List<Method> results = new ArrayList<>();
        findPublicMethods(clazz, results, name, paramTypes);

        return results.stream()
                .filter(method -> matches(method, name, paramTypes))
                .reduce((m1, m2) -> {
                    Class<?> r1 = m1.getReturnType();
                    Class<?> r2 = m2.getReturnType();

                    return r1 != r2 && r1.isAssignableFrom(r2) ? m2 : m1;
                });
    }

    /**
     * Recursively find truly public methods in the class hierarchy.
     *
     * @param clazz      Class to inspect
     * @param results    List to store found methods
     * @param name       Method name
     * @param paramTypes Parameter types
     */
    private static void findPublicMethods(Class<?> clazz, List<Method> results, String name, Class<?>... paramTypes) {
        if (clazz == null)
            return;

        Method[] methods = clazz.getMethods();
        for (Method method : methods)
            if (matches(method, name, paramTypes) && isPublic(method))
                results.add(method);

        for (Class<?> intf : clazz.getInterfaces())
            findPublicMethods(intf, results, name, paramTypes);

        findPublicMethods(clazz.getSuperclass(), results, name, paramTypes);
    }

    /**
     * Check if a method is truly public.
     *
     * @param method Method to check
     * @return True if the method is truly public, false otherwise
     */
    private static boolean isPublic(Method method) {
        return Modifier.isPublic(method.getModifiers()
                & method.getDeclaringClass().getModifiers());
    }

    /**
     * Check if a method matches the given name and parameter types.
     *
     * @param method     Method to check
     * @param name       Method name
     * @param paramTypes Parameter types
     * @return True if the method matches, false otherwise
     */
    private static boolean matches(Method method, String name, Class<?>... paramTypes) {
        return method.getName().equals(name)
                && Arrays.equals(method.getParameterTypes(), paramTypes);
    }
}