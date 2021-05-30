package optimizations.ollir;

import optimizations.ollir.data.VarNode;
import org.specs.comp.ollir.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public class GraphPainter {
    private final Map<Method, Map<VarNode, Set<VarNode>>> interferenceGraph;

    public GraphPainter(Map<Method, Map<VarNode, Set<VarNode>>> interferenceGraph) {
        this.interferenceGraph = interferenceGraph;
    }

    public void paint(int nColours) throws GraphPainterException {
        for (Map.Entry<Method, Map<VarNode, Set<VarNode>>> methodEntry : this.interferenceGraph.entrySet()) {
            if (!paint(methodEntry.getValue(), nColours)) {
                throw new GraphPainterException("There are not enough registers to allocate to local variables in method \"" + this.methodToString(methodEntry.getKey()));
            }
        }
    }

    private boolean paint(Map<VarNode, Set<VarNode>> graph, int nColours) {
        Stack<VarNode> stack = new Stack<>();
        Set<Integer> coloursUsed = new HashSet<>();
        int stackSize = 0;

        while (stack.size() != graph.size()) {
            graph.forEach((node, edges) -> {
                if (node.isDeleted() || edges.stream().filter(VarNode::isDeleted).count() >= nColours) {
                    return;
                }

                node.setDeleted(true);
                stack.push(node);
            });

            if(stackSize == stack.size()) {
                return false;
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
                }

                coloursUsed.add(varNode.getId());
            });

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

    public String methodToString(Method method) {
        return method.getMethodName() + '(' +
            method.getParams().stream().map(element ->
                this.typeToString(element.getType()) + " " +
                    ((Operand) element).getName()).collect(Collectors.joining(", ")) + ')';
    }

    private String typeToString(Type type) {
        return switch (type.getTypeOfElement()){
            case INT32 -> "int";
            case STRING -> "String";
            case BOOLEAN -> "boolean";
            case OBJECTREF, THIS, VOID -> "";
            case CLASS -> ((ClassType) type).getName();
            case ARRAYREF -> ((ArrayType) type).getTypeOfElements() == ElementType.INT32 ? "int[]" : "String[]";
        };
    }
}
