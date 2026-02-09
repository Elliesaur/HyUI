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

import au.ellie.hyui.html.template.item.Node;

public class EvaluationException extends RuntimeException {

    /**
     * The node in cause at the time of the exception
     */
    public final Node node;

    public EvaluationException(String message, Node node) {
        super(message);
        this.node = node;
    }

    /**
     * Exception thrown when a component is not found in the context during evaluation
     */
    public static class ComponentNotFoundException extends EvaluationException {

        /**
         * The tag of the component that was found
         */
        public final String tag;

        public ComponentNotFoundException(String message, Node node, String tag) {
            super(message, node);
            this.tag = tag;
        }
    }
}
