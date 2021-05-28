package optimizations;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class GraphPainter {
    private final Map<MethodNode, Map<VarNode, Set<VarNode>>> interferenceGraph;

    public static class GraphPainterException extends Exception {
        public GraphPainterException(String message) {
            super(message);
        }
    }

    public GraphPainter(Map<MethodNode, Map<VarNode, Set<VarNode>>> interferenceGraph) {
        this.interferenceGraph = interferenceGraph;
    }

    public void paint(int nColours) throws GraphPainterException {
        for (Map.Entry<MethodNode, Map<VarNode, Set<VarNode>>> methodEntry : this.interferenceGraph.entrySet()) {
            if (!paint(methodEntry.getKey(), methodEntry.getValue(), nColours)) {
                throw new GraphPainterException("There are not enough registers to allocate to local variables in method \"" + methodEntry.getKey().toString());
            }
        }
    }

    private boolean paint(MethodNode methodNode, Map<VarNode, Set<VarNode>> graph, int nColours) {
        Stack<VarNode> stack = new Stack<>();
        Set<Integer> coloursUsed = new HashSet<>();
        int stackSize = 0;

        while (stack.size() != graph.size()) {
            int tempNColours = nColours;
            graph.forEach((node, edges) -> {
                if (node.isDeleted() || edges.stream().filter(VarNode::isDeleted).count() >= tempNColours) {
                    return;
                }

                node.setDeleted(true);
                stack.push(node);
            });

            if(stackSize == stack.size()) {
                nColours++;
            }

            stackSize = stack.size();
        }

        while (!stack.isEmpty()) {
            int nodeColour = 0;
            VarNode node = stack.pop();
            node.setDeleted(false);

            Set<VarNode> edges = graph.get(node);

            edges.forEach(varNode -> {
                if (varNode.isDeleted()) {
                    return;
                };

                coloursUsed.add(varNode.getId());
            });

            //TODO: possibly increase nColours available
            if (coloursUsed.size() >= nColours) {
                //VarNode spillingNode = this.chooseSpillingNode();
                return false;
            }

            for (int i = 0; i < nColours; i++) {
                if(!coloursUsed.contains(i)) {
                    nodeColour = i;
                    break;
                }
            }

            node.setId(nodeColour);

            coloursUsed.clear();
        }

        return true;
    }

    /*private VarNode chooseSpillingNode() {

    }*/
}
