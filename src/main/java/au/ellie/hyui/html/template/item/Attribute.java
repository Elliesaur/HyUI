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

package au.ellie.hyui.html.template.item;

import au.ellie.hyui.html.template.item.Node.AttributeValueNode;
import au.ellie.hyui.html.template.item.Node.BlockNode.EachBlockNode;
import au.ellie.hyui.html.template.item.Node.BlockNode.IfBlockNode;
import au.ellie.hyui.html.template.item.Node.ExpressionNode;

import java.util.List;

public interface Attribute {

    /**
     * Hold extracted loop/conditional attributes from HTML elements
     */
    record ControlAttribute(ExpressionNode collection, String itemName) implements Attribute {
    }

    /**
     * Hold extracted condition attribute from HTML elements
     */
    record ConditionAttribute(ExpressionNode condition) implements Attribute {
    }

    /**
     * Record to hold parsed attributes along with control flow attributes
     */
    record ParsedAttributes(
            List<AttributeValueNode> attributes,
            List<Attribute> flows
    ) {

        /**
         * Sort the control flow attributes
         *
         * @return A sorted list of control flow attributes
         */
        public List<Attribute> sortedFlowAttributes() {
            return flows.stream()
                    .sorted((a, b) -> {
                        if (a instanceof ControlAttribute && b instanceof ConditionAttribute)
                            return -1;
                        if (a instanceof ConditionAttribute && b instanceof ControlAttribute)
                            return 1;
                        return 0;
                    }).toList();
        }

        /**
         * Build a node with the parsed attributes and control flow attributes
         *
         * @param base The base node to wrap with control flow nodes
         * @return The final node with all control flow nodes applied
         */
        public Node build(Node base) {
            Node result = base;

            for (var attr : sortedFlowAttributes()) {
                if (attr instanceof ControlAttribute(ExpressionNode collection, String itemName))
                    result = new EachBlockNode(itemName, collection, List.of(result));
                else if (attr instanceof ConditionAttribute(ExpressionNode condition))
                    result = new IfBlockNode(condition, List.of(result), List.of());
            }

            return result;
        }
    }
}
