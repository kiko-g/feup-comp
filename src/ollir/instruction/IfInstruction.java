package ollir.instruction;

import java.util.List;
import java.util.stream.Collectors;

public class IfInstruction implements JmmInstruction {
    private static int ifCounter = 0;
    protected List<JmmInstruction> condition, ifBody, elseBody;
    protected JmmInstruction conditionInstruction;
    protected int ifNum;

    public IfInstruction(List<JmmInstruction> condition, List<JmmInstruction> ifBody, List<JmmInstruction> elseBody) {
        this.conditionInstruction = condition.remove(condition.size() - 1);
        this.condition = condition;

        this.ifBody = ifBody;
        this.elseBody = elseBody;
        this.ifNum = ifCounter++;
    }

    @Override
    public JmmInstruction getVariable() {
        return new NullInstruction();
    }

    @Override
    public String toString(String backspace) {
        return condition.stream().map(instruction -> instruction.toString(backspace)).collect(Collectors.joining()) +
            backspace + "if (" + conditionInstruction + ") goto True" + ifCounter + ";\n" +
            elseBody.stream().map(instruction -> instruction.toString(backspace + "\t")).collect(Collectors.joining()) +
            backspace + "\tgoto Endif" + ifCounter + ";\n" +
            backspace + "True" + ifCounter + ":\n" +
            ifBody.stream().map(instruction -> instruction.toString(backspace + "\t")).collect(Collectors.joining()) +
            backspace + "Endif" + ifCounter + ":\n";
    }
}
