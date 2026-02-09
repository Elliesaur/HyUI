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

package au.ellie.hyui.html.template.context;

import au.ellie.hyui.utils.ParseUtils;
import au.ellie.hyui.utils.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FilterRegistry {

    private final Map<String, Filter> filters = new HashMap<>();

    public FilterRegistry() {
        register("uppercase", value -> value == null ? null : value.toString().toUpperCase(), "upper");
        register("lowercase", value -> value == null ? null : value.toString().toLowerCase(), "lower");
        register("capitalize", value -> StringUtils.capitalize(value.toString()));
        register("capitalizeAll", value -> StringUtils.capitalizeAll(value.toString()));
        register("trim", value -> value == null ? null : value.toString().trim());
        register("length", value -> switch (value) {
            case null -> 0;
            case String s -> s.length();
            case Collection<?> c -> c.size();
            case Map<?, ?> m -> m.size();
            default -> value.toString().length();
        });
        register("number", value -> {
            var num = ParseUtils.parseDouble(value.toString());
            if (num.isEmpty())
                return value;

            var numValue = num.get();
            if (numValue % 1 == 0)
                return String.format(Locale.ENGLISH, "%,d", numValue.longValue());

            return String.format("%,.2f", numValue);
        });
        register("percent", value -> {
            var num = ParseUtils.parseDouble(value.toString());
            if (num.isEmpty())
                return value;

            return String.format(Locale.ENGLISH, "%.0f%%", num.get() * 100);
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
