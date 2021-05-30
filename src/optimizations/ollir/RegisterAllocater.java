package optimizations.ollir;

import optimizations.ollir.data.LivenessNode;
import optimizations.ollir.data.VarNode;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegisterAllocater {
    public void allocate(ClassUnit classUnit, int numRegisters) throws GraphPainterException {
        Map<Method, List<LivenessNode>> livenessAnalysis = new LivenessAnalysis(classUnit).analyse();
        Map<Method, Map<VarNode, Set<VarNode>>> graph = new InterferenceGraphMaker().create(livenessAnalysis);

        new GraphPainter(graph).paint(numRegisters);

        graph.forEach((method, interferenceGraph) -> {
            int offset = method.getParams().size() + (method.isStaticMethod() ? 0 : 1);
            interferenceGraph.forEach((node, edges) -> {
                int id = node.getId();
                node.getDescriptor().setVirtualReg(id + offset);
            });
        });
    }
}
