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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SlotSupplier implements Supplier<String> {
    private final Function<Node, String> handler;
    private List<Node> nodes;

    public SlotSupplier(Function<Node, String> handler) {
        this.handler = handler;
    }

    public void add(Node node) {
        if (nodes == null)
            nodes = new ArrayList<>();

        nodes.add(node);
    }

    @Override
    public String get() {
        var result = new StringBuilder();
        for (Node node : nodes)
            result.append(handler.apply(node));

        return result.toString().trim();
    }
}
