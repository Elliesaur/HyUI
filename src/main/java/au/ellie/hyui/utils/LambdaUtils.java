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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Quick utility for detecting and calling multiple type of lambda/function objects,
 * including Java functional interfaces and Kotlin functions.
 */
public class LambdaUtils {

    /**
     * Check if an object is a callable function/lambda
     *
     * @param obj The object to check
     */
    public static boolean isFunction(Object obj) {
        if (obj == null)
            return false;

        // Check Kotlin functions
        if (obj.getClass().getName().startsWith("kotlin.jvm.functions.Function"))
            return true;

        // Check Java functional interfaces
        return obj instanceof Supplier || obj instanceof Function ||
                obj instanceof BiFunction || obj instanceof Consumer ||
                obj instanceof BiConsumer || obj instanceof Predicate ||
                obj instanceof Runnable || obj instanceof Callable;
    }

    /**
     * Call a function with the given arguments
     * Automatically detects the function type and calls it appropriately
     *
     * @param source The function object to call
     * @param args   Arguments to pass to the function
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object call(Object source, Object... args) {
        switch (source) {
            case Supplier supplier -> {
                return supplier.get();
            }
            case Function function when args.length >= 1 -> {
                return function.apply(args[0]);
            }
            case BiFunction biFunction when args.length >= 2 -> {
                return biFunction.apply(args[0], args[1]);
            }
            case Consumer consumer when args.length >= 1 -> {
                consumer.accept(args[0]);
                return null;
            }
            case BiConsumer biConsumer when args.length >= 2 -> {
                biConsumer.accept(args[0], args[1]);
                return null;
            }
            case Runnable runnable -> {
                runnable.run();
                return null;
            }
            case Callable callable -> {
                try {
                    return callable.call();
                } catch (Exception e) {
                    throw new RuntimeException("Error calling Callable", e);
                }
            }
            case Predicate predicate when args.length >= 1 -> {
                return predicate.test(args[0]);
            }
            default -> {
                // Continue to reflection fallback
            }
        }

        try {
            return callViaReflection(source, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call function", e);
        }
    }

    /**
     * Call a function using reflection (fallback method)
     *
     * @param function The function object to call
     * @param args     Arguments to pass to the function
     * @return The result of the function call
     */
    private static Object callViaReflection(Object function, Object... args) throws Exception {
        var clazz = function.getClass();

        // Try to find "invoke" method (Kotlin)
        try {
            var invokeMethod = clazz.getMethod("invoke");

            return invokeMethod.invoke(function, args);
        } catch (NoSuchMethodException e) {
            // Continue to SAM method search
        }

        // Find the single abstract method
        var sam = findSingleAbstractMethod(clazz);
        if (sam != null)
            return sam.invoke(function, args);

        throw new IllegalArgumentException("Cannot find callable method on " + clazz);
    }

    /**
     * Find the single abstract method (SAM) of a class, if it exists
     *
     * @param clazz The class to inspect
     * @return The single abstract method, or null if there are none or more than one
     */
    private static Method findSingleAbstractMethod(Class<?> clazz) {
        Method abstractMethod = null;
        for (var method : clazz.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers()) &&
                    !method.isDefault() &&
                    !method.getDeclaringClass().equals(Object.class)) {

                if (abstractMethod != null)
                    return null; // More than one abstract method

                abstractMethod = method;
            }
        }

        return abstractMethod;
    }
}