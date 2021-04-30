package ollir.instruction;

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
