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

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Node {

    // ---- Expression Nodes ----

    sealed interface ExpressionNode extends Node {
        /**
         * Represents plain text in the template
         */
        record TextNode(String content) implements ExpressionNode {
        }

        /**
         * Represents a literal value (string, number, boolean)
         */
        record LiteralNode(Object value) implements ExpressionNode {
        }

        /**
         * Represents a variable reference
         */
        record VariableNode(String name, boolean negated) implements ExpressionNode {
        }

        /**
         * Represents accessing a property of an object
         */
        record PropertyAccessNode(ExpressionNode object, String property) implements ExpressionNode {
        }

        /**
         * Represents a binary operation between two expressions
         */
        record BinaryOpNode(ExpressionNode left, String operator, ExpressionNode right) implements ExpressionNode {
        }

        /**
         * Represents applying a filter to an expression
         */
        record PipeNode(ExpressionNode expression, String filterName) implements ExpressionNode {
        }

        /**
         * Represents a list of alternative expressions (like coalesce)
         */
        record DefaultNode(List<ExpressionNode> alternatives) implements ExpressionNode {
        }

        /**
         * Represents a comment block in the template
         */
        record CommentNode(String content) implements ExpressionNode {
        }
    }

    // ---- Control Flow Nodes ----

    interface BlockNode extends Node {
        /**
         * Represents an if / else-if / else control structure
         */
        class ConditionalBlockNode implements BlockNode {
            private final List<ConditionalBranch> branches;
            private final String name;

            private Map<String, Integer> tags;

            public ConditionalBlockNode(String name, List<ConditionalBranch> branches) {
                this.branches = branches;
                this.name = name;
            }

            public Map<String, Integer> getTags() {
                if (tags == null) {
                    tags = new HashMap<>();

                    for (var branch : branches) {
                        var local = getLocal(branch);

                        for (var entry : local.entrySet()) {
                            var tag = entry.getKey();
                            var count = entry.getValue();
                            if (tags.containsKey(tag) && tags.get(tag) >= count)
                                count = tags.get(tag);

                            tags.put(tag, count);
                        }
                    }
                }

                return tags;
            }

            @NonNullDecl
            private HashMap<String, Integer> getLocal(ConditionalBranch branch) {
                var local = new HashMap<String, Integer>();
                for (var node : branch.body) {
                    switch (node) {
                        case ComponentBlockNode c -> {
                            if (!c.tag.equals(Symbols.HTML_TAG_TEMPLATE))
                                local.put(c.tag, local.getOrDefault(c.tag, 0) + 1);
                        }
                        case SlotBlockNode s -> local.put(s.name, local.getOrDefault(s.name, 0) + 1);
                        default -> {
                            // Ignore other nodes
                        }
                    }
                }
                return local;
            }

            public String name() {
                return name;
            }

            public List<ConditionalBranch> branches() {
                return branches;
            }

            public record ConditionalBranch(ExpressionNode condition, List<Node> body) {
            }
        }

        /**
         * Represents an `each` control structure
         */
        record ForBlockNode(String itemName, String indexName, ExpressionNode collection,
                            List<Node> body) implements BlockNode {
        }

        /**
         * Represents an HTML element with attributes and children
         */
        record ComponentBlockNode(String tag, List<AttributeValueNode> attributes,
                                  List<Node> children) implements BlockNode {
        }

        /**
         * Represents an HTML slot element with attributes and children
         */
        record SlotBlockNode(String name, List<AttributeValueNode> attributes,
                             List<Node> children, boolean output) implements BlockNode {
        }
    }

    // ---- Component Nodes ----

    sealed interface AttributeValueNode extends Node {
        String getName();

        record MixedAttributeNode(String name, List<Object> parts) implements AttributeValueNode {
            public String getName() {
                return name;
            }
        }

        record DynamicAttributeNode(String name, ExpressionNode expression) implements AttributeValueNode {
            public String getName() {
                return name;
            }
        }

        record FlagAttributeNode(String name) implements AttributeValueNode {
            public String getName() {
                return name;
            }
        }

        record ExpressionAttributeNode(Node expressions) implements AttributeValueNode {
            public String getName() {
                return "<expression>";
            }
        }
    }

    // ---- Markers ----

    record MarkerNode(String content, Node inside) implements Node {
    }
}

