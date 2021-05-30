package optimizations.ollir;

import optimizations.ollir.data.LivenessNode;
import optimizations.ollir.data.VarNode;
import org.specs.comp.ollir.Method;

import java.util.*;

public class InterferenceGraphMaker {
    public Map<Method, Map<VarNode, Set<VarNode>>> create(Map<Method, List<LivenessNode>> livenessAnalysis) {
        Map<Method, Map<VarNode, Set<VarNode>>> methodGraph = new HashMap<>();
        
        for(Map.Entry<Method, List<LivenessNode>> entry: livenessAnalysis.entrySet()) {
            Map<VarNode, Set<VarNode>> graph = new HashMap<>();

            for (LivenessNode node : entry.getValue()) {
                this.updateGraph(graph, node);
            }

            methodGraph.put(entry.getKey(), graph);
        }

        return methodGraph;
    }

    private void updateGraph(Map<VarNode, Set<VarNode>> graph, LivenessNode node) {
        addNodes(graph, node.getUse());
        addNodes(graph, node.getDef());

        for (VarNode outNode : node.getOut()) {
            Set<VarNode> connections = graph.get(outNode);

            for (VarNode inNode : node.getIn()) {
                if (!inNode.equals(outNode)) {
                    continue;
                }

                for (VarNode inNodeAdd : node.getIn()) {
                    if (inNodeAdd.equals(outNode)) {
                        continue;
                    }

                    connections.add(inNodeAdd);
                    graph.get(inNodeAdd).add(outNode);
                }
            }
        }
    }

    private void addNodes(Map<VarNode, Set<VarNode>> graph, Set<VarNode> nodes) {
        for (VarNode node : nodes) {
            graph.putIfAbsent(node, new HashSet<>());
        }
    }
}
