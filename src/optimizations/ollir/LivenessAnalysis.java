package optimizations.ollir;

import optimizations.ollir.data.LivenessNode;
import optimizations.ollir.data.VarNode;
import org.specs.comp.ollir.*;

import java.util.*;
import java.util.function.Function;

public class LivenessAnalysis {

    private final ClassUnit ollirClass;

    public LivenessAnalysis(ClassUnit ollirClass) {
        this.ollirClass = ollirClass;
    }

    public Map<Method, List<LivenessNode>> analyse() {
        Map<Method, List<LivenessNode>> livenessAnalysis = new HashMap<>();

        for(Method method: this.ollirClass.getMethods()) {
            List<LivenessNode> nodes = new ArrayList<>();
            List<VarNode> vars = new ArrayList<>();

            // Set up vars list
            for (Map.Entry<String, Descriptor> entry : method.getVarTable().entrySet()) {
                VarNode node = new VarNode(entry.getKey(), entry.getValue());

                if (entry.getValue().getScope() != VarScope.LOCAL) {
                    node.setUnused(false);
                }

                vars.add(node);
            }

            // Set up liveness nodes list
            for (Instruction instruction : method.getInstructions()) {
                nodes.add(new LivenessNode(instruction));
            }

            // Set up uses, defs and succ of each liveness node
            for (LivenessNode node : nodes) {
                this.analyseNode(method, nodes, vars, node);
            }

            for (int i = 0; i < 3; i++) {
                for (int j = nodes.size() - 1; j >= 0; j--) {
                    this.update(nodes.get(j));
                }
            }

            livenessAnalysis.put(method, nodes);
        }

        return livenessAnalysis;
    }

    private void update(LivenessNode node) {
        Set<VarNode> aux = new HashSet<>();

        Set<VarNode> in = node.getIn();
        Set<VarNode> out = node.getOut();
        Set<VarNode> def = node.getDef();
        Set<VarNode> use = node.getUse();
        Set<LivenessNode> succ = node.getSucc();

        // Update out set of node -> out = U for(s € succ) in(s)
        out.clear();
        for (LivenessNode successor : succ) {
            out.addAll(successor.getIn());
        }

        // Update in set of node -> in = use U (out - def)
        in.clear();
        in.addAll(use);
        aux.addAll(out);
        aux.removeAll(def);

        in.addAll(aux);
    }

    private void analyseNode(Method method, List<LivenessNode> nodes, List<VarNode> vars, LivenessNode node) {
        this.analyseNode(method, nodes, vars, node, node.getInstruction());
    }

    private void analyseNode(Method method, List<LivenessNode> nodes, List<VarNode> vars, LivenessNode node, Instruction instr) {
        switch (instr.getInstType()) {
            case NOPER -> this.updateNode(method, vars, node, ((SingleOpInstruction) instr).getSingleOperand(), LivenessNode::getUse);
            case ASSIGN -> {
                this.updateNode(method, vars, node, ((AssignInstruction) instr).getDest(), LivenessNode::getDef);
                this.analyseNode(method, nodes, vars, node, ((AssignInstruction) instr).getRhs());
            }
            case BINARYOPER -> {
                this.updateNode(method, vars, node, ((BinaryOpInstruction) instr).getLeftOperand(), LivenessNode::getUse);
                this.updateNode(method, vars, node, ((BinaryOpInstruction) instr).getRightOperand(), LivenessNode::getUse);
            }
            case CALL -> {
                if (((CallInstruction) instr).getInvocationType() == CallType.invokevirtual ||
                    ((CallInstruction) instr).getInvocationType() == CallType.invokespecial ||
                    ((CallInstruction) instr).getInvocationType() == CallType.arraylength
                ) {
                    this.updateNode(method, vars, node, ((CallInstruction) instr).getFirstArg(), LivenessNode::getUse);
                }

                if (((CallInstruction) instr).getListOfOperands() != null) {
                    for (Element elem : ((CallInstruction) instr).getListOfOperands()) {
                        this.updateNode(method, vars, node, elem, LivenessNode::getUse);
                    }
                }
            }
            case GETFIELD -> {
                this.updateNode(method, vars, node, ((GetFieldInstruction) instr).getFirstOperand(), LivenessNode::getUse);
                this.updateNode(method, vars, node, ((GetFieldInstruction) instr).getSecondOperand(), LivenessNode::getUse);
            }
            case PUTFIELD -> {
                this.updateNode(method, vars, node, ((PutFieldInstruction) instr).getFirstOperand(), LivenessNode::getUse);
                this.updateNode(method, vars, node, ((PutFieldInstruction) instr).getSecondOperand(), LivenessNode::getDef);
                this.updateNode(method, vars, node, ((PutFieldInstruction) instr).getThirdOperand(), LivenessNode::getUse);
            }
            case RETURN -> this.updateNode(method, vars, node, ((ReturnInstruction) instr).getOperand(), LivenessNode::getUse);
            case BRANCH -> {
                this.updateNode(method, vars, node, ((CondBranchInstruction) instr).getLeftOperand(), LivenessNode::getUse);
                this.updateNode(method, vars, node, ((CondBranchInstruction) instr).getRightOperand(), LivenessNode::getUse);
                this.updateNodeSucc(method, nodes, node, ((CondBranchInstruction) instr).getLabel());
            }
            case GOTO -> {
                this.updateNodeSucc(method, nodes, node, ((GotoInstruction) instr).getLabel());
                return;
            }
        }

        int nextNodeIndex = nodes.indexOf(node) + 1;
        if (nextNodeIndex >= nodes.size()) {
            return;
        }

        node.getSucc().add(nodes.get(nextNodeIndex));
    }

    private void updateNodeSucc(Method method, List<LivenessNode> nodes, LivenessNode node, String label) {
        Instruction targetInstruction = null;
        for (Map.Entry<String, Instruction> entry : method.getLabels().entrySet()) {
            if (entry.getKey().equals(label)) {
                targetInstruction = entry.getValue();
                break;
            }
        }

        int nextNodeIndex = nodes.indexOf(new LivenessNode(targetInstruction));

        if (nextNodeIndex == -1) {
            return;
        }

        node.getSucc().add(nodes.get(nextNodeIndex));
    }

    private void updateNode(Method method, List<VarNode> vars, LivenessNode node, Element element, Function<LivenessNode, Set<VarNode>> accessor) {
        if(!(element instanceof Operand) ||
            element.getType().getTypeOfElement() == ElementType.THIS || element.getType().getTypeOfElement() == ElementType.CLASS ||
                (element.getType().getTypeOfElement() == ElementType.BOOLEAN &&
                (((Operand) element).getName().equals("true") || ((Operand) element).getName().equals("false")))) {
            return;
        }

        Operand operand = (Operand) element;
        if(operand instanceof ArrayOperand) {
            for(Element elem: ((ArrayOperand)operand).getIndexOperands()) {
                updateNode(method, vars, node, elem, LivenessNode::getUse);
            }

            accessor = LivenessNode::getUse;
        }

        Descriptor descriptor = method.getVarTable().get(operand.getName());
        if(descriptor.getScope() != VarScope.LOCAL) {
            return;
        }

        VarNode var = new VarNode(operand.getName(), descriptor);
        if(vars.contains(var)) {
            var = vars.get(vars.indexOf(var));
        }

        accessor.apply(node).add(var);
    }
}
