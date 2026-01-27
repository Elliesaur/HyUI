package au.ellie.hyui.utils;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.HyUIPluginLogger;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Utility methods for safe parsing of attribute values with optional debug logging.
 */
public final class ParseUtils {
    /**
     * Parse a string to an integer, logging parse failures in debug mode.
     *
     * @param value   The string value to parse
     * @return Optional containing the parsed value, or empty if parsing failed
     */
    public static Optional<Integer> parseInt(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Parse a string to an integer with a default value.
     *
     * @param value        The string value to parse
     * @param defaultValue Value to return if parsing fails
     * @return The parsed value or defaultValue if parsing failed
     */
    public static int parseIntOrDefault(String value, int defaultValue) {
        return parseInt(value).orElse(defaultValue);
    }

    /**
     * Parse a string to an integer and apply it to a consumer if successful.
     * This is useful for builder patterns where you only want to call a setter on success.
     *
     * @param value    The string value to parse
     * @param consumer Consumer to apply the parsed value to
     * @return true if parsing succeeded and consumer was called, false otherwise
     */
    public static boolean parseIntAndApply(String value, Consumer<Integer> consumer) {
        return parseInt(value).map(v -> {
            consumer.accept(v);
            return true;
        }).orElse(false);
    }

    /**
     * Parse a string to a long, logging parse failures in debug mode.
     *
     * @param value   The string value to parse
     * @return Optional containing the parsed value, or empty if parsing failed
     */
    public static Optional<Long> parseLong(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Parse a string to a long with a default value.
     *
     * @param value        The string value to parse
     * @param defaultValue Value to return if parsing fails
     * @return The parsed value or defaultValue if parsing failed
     */
    public static long parseLongOrDefault(String value, long defaultValue) {
        return parseLong(value).orElse(defaultValue);
    }

    /**
     * Parse a string to a double, logging parse failures in debug mode.
     *
     * @param value   The string value to parse
     * @return Optional containing the parsed value, or empty if parsing failed
     */
    public static Optional<Double> parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Double.parseDouble(value.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Parse a string to a double with a default value.
     *
     * @param value        The string value to parse
     * @param defaultValue Value to return if parsing fails
     * @return The parsed value or defaultValue if parsing failed
     */
    public static double parseDoubleOrDefault(String value, double defaultValue) {
        return parseDouble(value).orElse(defaultValue);
    }

    /**
     * Parse a string to a float, logging parse failures in debug mode.
     *
     * @param value   The string value to parse
     * @return Optional containing the parsed value, or empty if parsing failed
     */
    public static Optional<Float> parseFloat(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Float.parseFloat(value.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Parse a string to a float with a default value.
     *
     * @param value        The string value to parse
     * @param defaultValue Value to return if parsing fails
     * @return The parsed value or defaultValue if parsing failed
     */
    public static float parseFloatOrDefault(String value, float defaultValue) {
        return parseFloat(value).orElse(defaultValue);
    }

    /**
     * Parse a string to an enum value, logging parse failures in debug mode.
     *
     * @param value     The string value to parse
     * @param enumClass The enum class to parse into
     * @param <E>       The enum type
     * @return Optional containing the parsed enum value, or empty if parsing failed
     */
    public static <E extends Enum<E>> Optional<E> parseEnum(String value, Class<E> enumClass) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Enum.valueOf(enumClass, value.trim()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Parse a string to an enum value with a default.
     *
     * @param value        The string value to parse
     * @param enumClass    The enum class to parse into
     * @param defaultValue Value to return if parsing fails
     * @param <E>          The enum type
     * @return The parsed enum value or defaultValue if parsing failed
     */
    public static <E extends Enum<E>> E parseEnumOrDefault(String value, Class<E> enumClass,
                                                           E defaultValue) {
        return parseEnum(value, enumClass).orElse(defaultValue);
    }
}
