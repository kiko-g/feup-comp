package optimizations;

import org.specs.comp.ollir.ClassUnit;

import java.util.Map;
import java.util.Set;

public class RegisterAllocater {
    public void allocate(Map<MethodNode, Map<VarNode, Set<VarNode>>> graph) {
        graph.forEach((method, interferenceGraph) -> {
            int offset = method.getParamList().size() + (method.isStatic() ? 0 : 1);
            interferenceGraph.forEach((node, edges) -> {
                int id = node.getId();
                node.getDescriptor().setVirtualReg(id + offset);
            });
        });
    }
}
