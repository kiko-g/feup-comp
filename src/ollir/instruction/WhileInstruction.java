package ollir.instruction;

import ollir.instruction.complex.binary.BinaryOperationInstruction;
import ollir.instruction.complex.binary.DotMethodInstruction;
import ollir.instruction.complex.binary.DotStaticMethodInstruction;
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
            this.conditionInstruction = new BinaryOperationInstruction(this.conditionInstruction, this.conditionInstruction, new Operation(OperationType.AND, new Type("bool", false)));
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
            backspace + "\tif (" + conditionInstruction + ") goto Loop" + this.loopNum + ";\n" +
            backspace + "\tgoto EndWhile" + this.loopNum + ";\n" +
            backspace + "Loop" + this.loopNum + ":\n" +
            whileBody.stream().map(instruction -> instruction.toString(backspace + "\t")).collect(Collectors.joining()) +
            backspace + "\tgoto While" + this.loopNum + ";\n" +
            backspace + "EndWhile" + this.loopNum + ":\n";
    }
}
