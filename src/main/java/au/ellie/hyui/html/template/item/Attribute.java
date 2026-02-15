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
import au.ellie.hyui.html.template.item.Node.BlockNode.ConditionalBlockNode;
import au.ellie.hyui.html.template.item.Node.BlockNode.ConditionalBlockNode.ConditionalBranch;
import au.ellie.hyui.html.template.item.Node.BlockNode.ForBlockNode;
import au.ellie.hyui.html.template.item.Node.ExpressionNode;
import au.ellie.hyui.utils.StringReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static au.ellie.hyui.utils.ObjectUtils.mutableListOf;

public interface Attribute {

    /**
     * Hold extracted loop/conditional attributes from HTML elements
     */
    record ControlAttribute(ExpressionNode collection, String itemName, String indexName) implements Attribute {
    }

    /**
     * Hold extracted condition attribute from HTML elements
     */
    record ConditionAttribute(String name, ExpressionNode condition) implements Attribute {
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
                    }).collect(Collectors.toCollection(ArrayList::new));
        }

        /**
         * Build a node with the parsed attributes and control flow attributes
         * Handles grouping of if/else-if/else chains
         *
         * @param base The base node to wrap with control flow nodes
         * @return The final node with all control flow nodes applied
         */
        public Node build(Node base) {
            Node result = base;

            for (var attr : sortedFlowAttributes()) {
                result = switch (attr) {
                    case ControlAttribute(ExpressionNode collection, String itemName, String indexName) ->
                            new ForBlockNode(itemName, indexName, collection, mutableListOf(result));
                    case ConditionAttribute(String name, ExpressionNode condition) -> {
                        var branch = new ConditionalBranch(condition, mutableListOf(result));
                        yield new ConditionalBlockNode(name, mutableListOf(branch));
                    }
                    default -> result;
                };
            }

            return result;
        }
    }

    /**
     * Parse inline attributes from evaluated expression content.
     *
     * @param content The evaluated content containing attributes
     * @param context The context map to add attributes to
     */
    static void inlineAttributes(String content, Map<String, Object> context) {
        var reader = new StringReader(content.trim());

        while (reader.hasNext()) {
            reader.skipWhitespace();
            if (!reader.hasNext())
                break;

            // Read attribute name (until whitespace or '=')
            var name = reader.readWhile(c -> !Character.isWhitespace(c) && c != '=');
            if (name.isEmpty())
                break;

            reader.skipWhitespace();

            // Switch between flag and key-value attribute
            if (reader.consume('=')) {
                reader.skipWhitespace();

                context.put(name, reader.readValue());
            } else
                context.put(name, true);
        }
    }
}
