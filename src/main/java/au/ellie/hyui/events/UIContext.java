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

package au.ellie.hyui.events;

import au.ellie.hyui.builders.HyUIHud;
import au.ellie.hyui.builders.HyUIPage;
import au.ellie.hyui.builders.LabelBuilder;
import au.ellie.hyui.builders.UIElementBuilder;

import java.util.List;
import java.util.Optional;

/**
 * Provides access to the current state of UI elements on the page.
 */
public interface UIContext {
    /**
     * Retrieves the list of logged UI commands.
     * @return A list of strings representing the logged commands.
     */
    List<String> getCommandLog();
    /**
     * Retrieves the current value of an element by its ID.
     *
     * @param id The ID of the element (set via .withId).
     * @return An Optional containing the value, or empty if the ID is not found or has no value.
     */
    Optional<Object> getValue(String id);

    /**
     * Retrieves the current value of an element by its ID, cast to the specified type.
     *
     * @param id   The ID of the element.
     * @param type The class of the type to cast to.
     * @param <T>  The expected type of the value.
     * @return An Optional containing the cast value, or empty if not found or if casting fails.
     */
    default <T> Optional<T> getValue(String id, Class<T> type) {
        return getValue(id).filter(type::isInstance).map(type::cast);
    }

    /**
     * Retrieves the current value of an element by its ID, cast to the specified type.
     *
     * @param id   The ID of the element.
     * @param type The class of the type to cast to.
     * @param <T>  The expected type of the value.
     * @return An Optional containing the cast value, or empty if not found or if casting fails.
     */
    default <T> Optional<T> getValueAs(String id, Class<T> type) {
        return getValue(id).filter(type::isInstance).map(type::cast);
    }

    /**
     * @return The page associated with this context, or empty if not a page.
     */
    Optional<HyUIPage> getPage();

    /**
     * Updates the page associated with this context, rebuilding it if necessary.
     * Does not update HUDs.
     * @param shouldClear Whether to clear the page before rebuilding.
     */
    void updatePage(boolean shouldClear);

    /**
     * Retrieves the builder for a particular element, cast to the specified builder.
     *
     * @param id   The ID of the element.
     * @param clazz The class of the type to cast to.
     * @param <E>  The expected type of the value.
     * @return An Optional containing the builder, or empty if not found or if casting fails.
     */
    <E extends UIElementBuilder<E>> Optional<E> getById(String id, Class<E> clazz);

    /**
     * Retrieves the builder for a particular element without self-typed constraints.
     *
     * @param id The ID of the element.
     * @return An Optional containing the builder, or empty if not found.
     */
    Optional<UIElementBuilder<?>> getByIdRaw(String id);

    /**
     * Retrieves the builder for a particular element, cast to the specified builder type.
     * This is useful for builders that extend a different self-typed base.
     *
     * @param id The ID of the element.
     * @param clazz The class of the type to cast to.
     * @param <E> The expected type of the builder.
     * @return An Optional containing the builder, or empty if not found or if casting fails.
     */
    default <E extends UIElementBuilder<?>> Optional<E> getByIdAs(String id, Class<E> clazz) {
        Optional<UIElementBuilder<?>> builder = getByIdRaw(id);
        if (builder.isPresent() && clazz.isInstance(builder.get())) {
            return Optional.of(clazz.cast(builder.get()));
        }
        return Optional.empty();
    }
}
