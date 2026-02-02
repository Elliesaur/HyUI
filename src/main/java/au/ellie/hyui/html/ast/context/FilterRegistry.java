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

package au.ellie.hyui.html.ast.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FilterRegistry {

    private final Map<String, Filter> filters = new HashMap<>();

    public FilterRegistry() {
        register("uppercase", value -> value == null ? null : value.toString().toUpperCase(), "upper");
        register("lowercase", value -> value == null ? null : value.toString().toLowerCase(), "lower");
        register("capitalize", value -> {
            if (value == null)
                return null;

            String str = value.toString();
            if (str.isEmpty())
                return str;

            return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
        });
        register("trim", value -> value == null ? null : value.toString().trim());
        register("length", value -> switch (value) {
            case null -> 0;
            case String s -> s.length();
            case Collection<?> c -> c.size();
            case Map<?, ?> m -> m.size();
            default -> value.toString().length();
        });
        register("number", value -> {
            try {
                double num = Double.parseDouble(value.toString());
                if (num == (long) num)
                    return String.format(Locale.ENGLISH, "%,d", (long) num);

                return String.format("%,.2f", num);
            } catch (NumberFormatException e) {
                return value;
            }
        });
        register("percent", value -> {
            try {
                double num = Double.parseDouble(value.toString());

                return String.format(Locale.ENGLISH, "%.0f%%", num * 100);
            } catch (NumberFormatException e) {
                return value;
            }
        });
    }

    /**
     * Register a new filter.
     *
     * @param name   The name of the filter.
     * @param filter The filter implementation.
     */
    public void register(String name, Filter filter, String... aliases) {
        filters.put(name, filter);

        for (String alias : aliases)
            filters.put(alias, filter);
    }

    /**
     * Get a filter by name.
     *
     * @param name The name of the filter.
     * @return The filter implementation.
     * @throws RuntimeException If the filter is not found.
     */
    public Filter get(String name) {
        Filter filter = filters.get(name);
        if (filter == null)
            throw new RuntimeException("Unknown filter: " + name);

        return filter;
    }

    @FunctionalInterface
    public interface Filter {
        Object apply(Object value);
    }
}
