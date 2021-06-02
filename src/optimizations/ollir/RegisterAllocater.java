package optimizations.ollir;

import optimizations.ollir.data.LivenessNode;
import optimizations.ollir.data.VarNode;
import org.specs.comp.ollir.*;

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
                if (node.isUnused()) {
                    this.deleteVar(method, node);
                }

                int id = node.getId();
                node.getDescriptor().setVirtualReg(id + offset);
            });
        });
    }

    private void deleteVar(Method method, VarNode node) {
        method.getVarTable().remove(node.getVariable());
        List<Instruction> instructions = method.getInstructions();

        for (int i = 0; i < instructions.size(); i++) {
            if (!(instructions.get(i) instanceof AssignInstruction)) {
                continue;
            }

            AssignInstruction instruction = (AssignInstruction) instructions.get(i);

            if (!(instruction.getDest() instanceof Operand)) {
                continue;
            }

            Operand operand = (Operand) instruction.getDest();

            if(!operand.getName().equals(node.getVariable())) {
                continue;
            }

            Instruction replaceInstruction = instruction.getRhs();

            instructions.remove(i);
            instructions.add(i, replaceInstruction);
        }
    }
}
