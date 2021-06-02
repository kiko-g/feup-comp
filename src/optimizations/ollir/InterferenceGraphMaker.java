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

            for (VarNode node : graph.keySet()){
                for (LivenessNode livenessNode : entry.getValue()) {
                    if (livenessNode.getUse().contains(node) || livenessNode.getIn().contains(node) || livenessNode.getOut().contains(node)) {
                        node.setUnused(false);
                    }
                }
            }

            methodGraph.put(entry.getKey(), graph);
        }

        return methodGraph;
    }

    private void updateGraph(Map<VarNode, Set<VarNode>> graph, LivenessNode node) {
        addNodes(graph, node.getDef());
        addNodes(graph, node.getUse());
        addNodes(graph, node.getOut());
        addNodes(graph, node.getIn());

        for (VarNode inNode : node.getIn()) {
            Set<VarNode> connections = graph.get(inNode);

            if (!node.getOut().contains(inNode)) {
                continue;
            }

            for (VarNode outNode : node.getOut()) {
                if (outNode.equals(inNode)) {
                    continue;
                }

                connections.add(outNode);
                graph.get(outNode).add(inNode);
            }
        }
    }

    private void addNodes(Map<VarNode, Set<VarNode>> graph, Set<VarNode> nodes) {
        for (VarNode node : nodes) {
            graph.putIfAbsent(node, new HashSet<>());
        }
    }
}
