package optimizations;

import org.specs.comp.ollir.*;

import java.util.*;

public class LivenessAnalysis {
    private ClassUnit ollirClass;

    public LivenessAnalysis(ClassUnit ollirClass) {
        this.ollirClass = ollirClass;
    }

    public Map<MethodNode, Map<VarNode, Set<VarNode>>> analyze() {
        Map<MethodNode, Map<VarNode, Set<VarNode>>> methodGraph = new HashMap<>();
        
        for(Method method: this.ollirClass.getMethods()) {
            Map<VarNode, VarLifeTime> lifetimes = new HashMap<>();

            for (int i = 0; i < method.getInstructions().size(); i++) {
                this.checkInstruction(method, lifetimes, method.getInstructions().get(i), i);
            }

            methodGraph.put(new MethodNode(method.getMethodName(), method.getParams()),
                this.createInterferenceGraph(method, lifetimes));
        }

        return methodGraph;
    }

    private void checkInstruction(Method method, Map<VarNode, VarLifeTime> lifetimes, Instruction instr, int index) {
        switch (instr.getInstType()) {
            case NOPER -> this.checkSingleOp(method, lifetimes, (SingleOpInstruction) instr, index);
            case ASSIGN -> this.checkAssignOp(method, lifetimes, (AssignInstruction) instr, index);
            case BINARYOPER -> this.checkBinaryOp(method, lifetimes, (BinaryOpInstruction) instr, index);
            case CALL -> this.checkCallOp(method, lifetimes, (CallInstruction) instr, index);
            case GETFIELD -> this.checkGetFieldOp(method, lifetimes, (GetFieldInstruction) instr, index);
            case PUTFIELD -> this.checkPutFieldOp(method, lifetimes, (PutFieldInstruction) instr, index);
            case RETURN -> this.checkReturnOp(method, lifetimes, (ReturnInstruction) instr, index);
            case BRANCH -> this.checkBranchOp(method, lifetimes, (CondBranchInstruction) instr, index);
        }
    }

    private void checkSingleOp(Method method, Map<VarNode, VarLifeTime> lifetimes, SingleOpInstruction instr, int index) {
        this.calculateLifeTime(method, lifetimes, instr.getSingleOperand(), index);
    }

    private void checkAssignOp(Method method, Map<VarNode, VarLifeTime> lifetimes, AssignInstruction instr, int index) {
        this.calculateLifeTime(method, lifetimes, instr.getDest(), index);
        this.checkInstruction(method, lifetimes, instr.getRhs(), index);
    }

    private void checkBinaryOp(Method method, Map<VarNode, VarLifeTime> lifetimes, BinaryOpInstruction instr, int index) {
        this.calculateLifeTime(method, lifetimes, instr.getLeftOperand(), index);
        this.calculateLifeTime(method, lifetimes, instr.getRightOperand(), index);
    }

    private void checkCallOp(Method method, Map<VarNode, VarLifeTime> lifetimes, CallInstruction instr, int index) {
        this.calculateLifeTime(method, lifetimes, instr.getFirstArg(), index);
        this.calculateLifeTime(method, lifetimes, instr.getSecondArg(), index);

        for(Element elem: instr.getListOfOperands()) {
            this.calculateLifeTime(method, lifetimes, elem, index);
        }
    }

    private void checkGetFieldOp(Method method, Map<VarNode, VarLifeTime> lifetimes, GetFieldInstruction instr, int index) {
        this.calculateLifeTime(method, lifetimes, instr.getFirstOperand(), index);
        this.calculateLifeTime(method, lifetimes, instr.getSecondOperand(), index);
    }

    private void checkPutFieldOp(Method method, Map<VarNode, VarLifeTime> lifetimes, PutFieldInstruction instr, int index) {
        this.calculateLifeTime(method, lifetimes, instr.getFirstOperand(), index);
        this.calculateLifeTime(method, lifetimes, instr.getSecondOperand(), index);
        this.calculateLifeTime(method, lifetimes, instr.getThirdOperand(), index);
    }

    private void checkReturnOp(Method method, Map<VarNode, VarLifeTime> lifetimes, ReturnInstruction instr, int index) {
        this.calculateLifeTime(method, lifetimes, instr.getOperand(), index);
    }

    private void checkBranchOp(Method method, Map<VarNode, VarLifeTime> lifetimes, CondBranchInstruction instr, int index) {
        this.calculateLifeTime(method, lifetimes, instr.getLeftOperand(), index);
        this.calculateLifeTime(method, lifetimes, instr.getRightOperand(), index);
    }

    private void calculateLifeTime(Method method, Map<VarNode, VarLifeTime> lifetimes, Element element, int index) {
        if(!(element instanceof Operand)) {
            return;
        }

        Operand operand = (Operand) element;
        if(operand instanceof ArrayOperand) {
            for(Element elem: ((ArrayOperand)operand).getIndexOperands()) {
                calculateLifeTime(method, lifetimes, elem, index);
            }

            return;
        }

        Descriptor descriptor = method.getVarTable().get(operand.getName());
        if(descriptor.getScope() != VarScope.LOCAL) {
            return;
        }

        VarNode node = new VarNode(operand.getName(), descriptor);
        if(lifetimes.containsKey(node)) {
            lifetimes.get(node).setEnd(index);
            return;
        }

        lifetimes.put(node, new VarLifeTime(index, index));
    }

    private Map<VarNode, Set<VarNode>> createInterferenceGraph(Method method, Map<VarNode, VarLifeTime> lifetimes) {
        Map<VarNode, Set<VarNode>> nodes = new HashMap<>();

        var varTable = method.getVarTable();
        varTable.forEach((var, descriptor) -> {
            nodes.put(new VarNode(var, descriptor), new HashSet<>());
        });

        List<Map.Entry<VarNode, VarLifeTime>> nodesObjs = new ArrayList<>(lifetimes.entrySet());
        for (int i = 0; i < nodesObjs.size(); i++) {
            Map.Entry<VarNode, VarLifeTime> entry1 = nodesObjs.get(i);

            for (int j = i + 1; j < nodesObjs.size(); j++) {
                Map.Entry<VarNode, VarLifeTime> entry2 = nodesObjs.get(j);
                VarLifeTime lifetime1 = entry1.getValue();
                VarLifeTime lifetime2 = entry2.getValue();

                if(lifetime1.getStart() <= lifetime2.getEnd() && lifetime1.getEnd() >= lifetime2.getStart()) {
                    nodes.get(entry1.getKey()).add(entry2.getKey());
                    nodes.get(entry2.getKey()).add(entry1.getKey());
                }
            }
        }


        return nodes;
    }
}
