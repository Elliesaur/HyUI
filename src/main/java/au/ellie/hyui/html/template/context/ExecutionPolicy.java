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

/**
 * Execution policy for lazily evaluated variables.
 */
public enum ExecutionPolicy {
    /**
     * No caching.
     * The value with be evaluated on every request.
     */
    DYNAMIC,

    /**
     * Cache the value after the first evaluation.
     */
    CACHED,

    /**
     * Evaluate the value only once, deleting it afterward.
     * This is useful for one-time action.
     */
    EPHEMERAL,

    /**
     * Evaluate the value until the first null result,
     * then delete it from the stack.
     */
    NON_NULL
}