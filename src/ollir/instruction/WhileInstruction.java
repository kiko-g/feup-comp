package ollir.instruction;

import ollir.instruction.complex.binary.BinaryOperationInstruction;
import ollir.instruction.complex.binary.DotMethodInstruction;
import ollir.instruction.complex.binary.DotStaticMethodInstruction;
import ollir.instruction.complex.binary.NotInstruction;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;
import java.util.stream.Collectors;

public class WhileInstruction implements JmmInstruction {
    private static int loopCounter = 0;
    protected List<JmmInstruction> condition, whileBody;
    protected JmmInstruction conditionInstruction;
    protected int loopNum;

    public WhileInstruction(List<JmmInstruction> condition, List<JmmInstruction> whileBody) {
        this.conditionInstruction = condition.remove(condition.size() - 1);
        this.condition = condition;

        if (this.conditionInstruction instanceof DotMethodInstruction || this.conditionInstruction instanceof DotStaticMethodInstruction) {
            condition.add(this.conditionInstruction);
            this.conditionInstruction = this.conditionInstruction.getVariable();
        }

        if (this.conditionInstruction instanceof TerminalInstruction) {
            this.conditionInstruction = new BinaryOperationInstruction(this.conditionInstruction, this.conditionInstruction, new Operation(OperationType.IS_EQUAL, new Type("bool", false)));
        }

        if (this.conditionInstruction instanceof BinaryOperationInstruction) {
            BinaryOperationInstruction ifCondition = (BinaryOperationInstruction) this.conditionInstruction;
            if (ifCondition.getOperation() != null) {
                if(ifCondition.getOperation().getOperationType() != OperationType.LESS_THAN) {
                    JmmInstruction newLeftInstruction = new NotInstruction(ifCondition.getLhs());
                    condition.add(newLeftInstruction);
                    JmmInstruction newRightInstruction = new NotInstruction(ifCondition.getRhs());
                    condition.add(newRightInstruction);
                    this.conditionInstruction = new BinaryOperationInstruction(newLeftInstruction.getVariable(), newRightInstruction.getVariable(), ifCondition.getOperation().inverseOperation());
                } else {
                    ifCondition.setOperation(ifCondition.getOperation().inverseOperation());
                }
            }
        }

        this.whileBody = whileBody;
        this.loopNum = loopCounter++;
    }

    @Override
    public JmmInstruction getVariable() {
        return new NullInstruction();
    }

    @Override
    public String toString(String backspace) {
        return backspace + "While" + this.loopNum + ":\n" +
            condition.stream().map(instruction -> instruction.toString(backspace + "\t")).collect(Collectors.joining()) +
            backspace + "\tif (" + conditionInstruction + ") goto EndWhile" + this.loopNum + ";\n" +
            whileBody.stream().map(instruction -> instruction.toString(backspace + "\t")).collect(Collectors.joining()) +
            backspace + "\tgoto While" + this.loopNum + ";\n" +
            backspace + "EndWhile" + this.loopNum + ":\n";
    }
}
