package ollir.instruction;

import ollir.instruction.complex.binary.BinaryOperationInstruction;
import ollir.instruction.complex.binary.DotMethodInstruction;
import ollir.instruction.complex.binary.DotStaticMethodInstruction;
import ollir.instruction.complex.binary.NotInstruction;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WhileInstruction implements JmmInstruction {
    private static int loopCounter = 0;
    protected List<JmmInstruction> condition, invertedCondition, whileBody;
    protected JmmInstruction conditionInstruction;
    protected JmmInstruction invertedConditionInstruction;
    protected int loopNum;

    public WhileInstruction(List<JmmInstruction> condition, List<JmmInstruction> whileBody, String scope) {
        this.conditionInstruction = condition.remove(condition.size() - 1);
        this.condition = condition;

        if (this.conditionInstruction instanceof DotMethodInstruction || this.conditionInstruction instanceof DotStaticMethodInstruction) {
            condition.add(this.conditionInstruction);
            this.conditionInstruction = this.conditionInstruction.getVariable(scope);
        }

        this.invertedCondition = new ArrayList<>(condition);

        if (this.conditionInstruction instanceof TerminalInstruction) {
            this.conditionInstruction = new BinaryOperationInstruction(this.conditionInstruction, new TerminalInstruction(new Symbol(new Type("boolean", false), "false")), new Operation(OperationType.NOT_EQUAL, new Type("bool", false)));
        }

        if (this.conditionInstruction instanceof BinaryOperationInstruction) {
            BinaryOperationInstruction ifCondition = (BinaryOperationInstruction) this.conditionInstruction;
            this.invertedConditionInstruction = new BinaryOperationInstruction(ifCondition);
            if (ifCondition.getOperation() != null) {
                switch (ifCondition.getOperation().getOperationType()) {
                    case AND -> {
                        JmmInstruction newLeftInstruction = new NotInstruction(ifCondition.getLhs());
                        condition.add(newLeftInstruction);
                        JmmInstruction newRightInstruction = new NotInstruction(ifCondition.getRhs());
                        condition.add(newRightInstruction);
                        this.conditionInstruction = new BinaryOperationInstruction(newLeftInstruction.getVariable(scope), newRightInstruction.getVariable(scope), ifCondition.getOperation().inverseOperation());
                    }
                    case NOT -> {
                        JmmInstruction instruction = ifCondition.getLhs().getVariable(scope);
                        this.conditionInstruction = new BinaryOperationInstruction(instruction, instruction, new Operation(OperationType.AND, new Type("boolean", false)));
                    }
                    case LESS_THAN, NOT_EQUAL -> ifCondition.setOperation(ifCondition.getOperation().inverseOperation());
                }
            }
        }

        this.whileBody = whileBody;
        this.loopNum = loopCounter++;
    }

    @Override
    public JmmInstruction getVariable(String _s) {
        return new NullInstruction();
    }

    @Override
    public String toString(String backspace) {
        return condition.stream().map(instruction -> instruction.toString(backspace)).collect(Collectors.joining()) +
            backspace + "if (" + conditionInstruction + ") goto EndWhile" + this.loopNum + ";\n" +
            backspace + "While" + this.loopNum + ":\n" +
            whileBody.stream().map(instruction -> instruction.toString(backspace + "\t")).collect(Collectors.joining()) +
            invertedCondition.stream().map(instruction -> instruction.toString(backspace + "\t")).collect(Collectors.joining()) +
            backspace + "\tif (" + invertedConditionInstruction + ") goto While" + this.loopNum + ";\n" +
            backspace + "EndWhile" + this.loopNum + ":\n";
    }
}
