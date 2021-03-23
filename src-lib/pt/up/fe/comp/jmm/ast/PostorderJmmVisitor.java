/**
 * Copyright 2021 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package pt.up.fe.comp.jmm.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.specs.util.SpecsCheck;

/**
 * Visitor that automatically applies a postorder, bottom-up traversal (first the children, then the current node).
 * 
 * @author JBispo
 *
 */
public class PostorderJmmVisitor<D, R> extends AJmmVisitor<D, R> {

    private final BiFunction<R, List<R>, R> reduce;

    /**
     * 
     * @param reduce
     *            a reduce function, which returns a result based on the result of the current node and the results of
     *            its children
     */
    public PostorderJmmVisitor(BiFunction<R, List<R>, R> reduce) {
        this.reduce = reduce;
    }

    /**
     * No arguments constructor which just returns the result of the current node.
     */
    public PostorderJmmVisitor() {
        this((nodeResult, childrenResults) -> nodeResult);
    }

    @Override
    public R visit(JmmNode jmmNode, D data) {
        SpecsCheck.checkNotNull(jmmNode, () -> "Node should not be null");

        var visit = getVisit(jmmNode.getKind());

        List<R> childrenResults = new ArrayList<>();

        // Postorder: 1st visit each children
        for (var child : jmmNode.getChildren()) {
            childrenResults.add(visit.apply(child, data));
        }

        // Postorder: then, visit the node
        var nodeResult = visit.apply(jmmNode, data);

        return reduce.apply(nodeResult, childrenResults);
    }
}
