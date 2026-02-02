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

package au.ellie.hyui.html.ast.item;

import java.util.List;
import java.util.Map;

public interface Node {

    // ---- Expression Nodes ----

    sealed interface ExpressionNode extends Node {
        /**
         * Represents plain text in the template
         */
        record TextNode(String content) implements Node {
        }

        /**
         * Represents a literal value (string, number, boolean)
         */
        record LiteralNode(Object value) implements ExpressionNode {
        }

        /**
         * Represents a variable reference
         */
        record VariableNode(String name) implements ExpressionNode {
        }

        /**
         * Represents accessing a property of an object
         */
        record PropertyAccessNode(ExpressionNode object, String property) implements ExpressionNode {
        }

        /**
         * Represents a binary operation between two expressions
         */
        record BinaryOpNode(ExpressionNode left, Token.Type operator, ExpressionNode right) implements ExpressionNode {
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

    }

    // ---- Control Flow Nodes ----

    interface BlockNode extends Node {
        /**
         * Represents an if control structure
         */
        record IfBlockNode(ExpressionNode condition, List<Node> thenBody, List<Node> elseBody) implements BlockNode {
        }

        /**
         * Represents an `each` control structure
         */
        record EachBlockNode(String itemName, ExpressionNode collection, List<Node> body) implements BlockNode {
        }
    }

    // ---- Attribute Value Nodes ----

    sealed interface AttributeValue {
        record Static(String value) implements AttributeValue {
        }

        record Dynamic(ExpressionNode expression) implements AttributeValue {
        }

        record Flag() implements AttributeValue {
        }
    }

    // ---- HTML Nodes ----

    record HtmlElementNode(
            String tagName,
            Map<String, AttributeValue> attributes,
            Map<String, AttributeValue> customAttributes,
            List<Node> children,
            boolean selfClosing
    ) implements Node {
    }
}

