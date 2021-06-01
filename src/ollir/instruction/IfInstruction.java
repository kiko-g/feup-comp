package ollir.instruction;

import ollir.instruction.complex.binary.BinaryOperationInstruction;
import ollir.instruction.complex.binary.DotMethodInstruction;
import ollir.instruction.complex.binary.DotStaticMethodInstruction;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;
import java.util.stream.Collectors;

public class IfInstruction implements JmmInstruction {
    private static int ifCounter = 0;
    protected List<JmmInstruction> condition, ifBody, elseBody;
    protected JmmInstruction conditionInstruction;
    protected int ifNum;

    public IfInstruction(List<JmmInstruction> condition, List<JmmInstruction> ifBody, List<JmmInstruction> elseBody, String scope) {
        this.conditionInstruction = condition.remove(condition.size() - 1);
        this.condition = condition;

        if (this.conditionInstruction instanceof DotMethodInstruction || this.conditionInstruction instanceof DotStaticMethodInstruction) {
            condition.add(this.conditionInstruction);
            this.conditionInstruction = this.conditionInstruction.getVariable(scope);
        }

        if (this.conditionInstruction instanceof TerminalInstruction) {
            this.conditionInstruction = new BinaryOperationInstruction(this.conditionInstruction, new TerminalInstruction(new Symbol(new Type("boolean", false), "false")), new Operation(OperationType.NOT_EQUAL, new Type("bool", false)));
        }

        this.ifBody = ifBody;
        this.elseBody = elseBody;
        this.ifNum = ifCounter++;
    }

    @Override
    public JmmInstruction getVariable(String _s) {
        return new NullInstruction();
    }

    @Override
    public String toString(String backspace) {
        return condition.stream().map(instruction -> instruction.toString(backspace)).collect(Collectors.joining()) +
            backspace + "if (" + conditionInstruction + ") goto True" + ifNum + ";\n" +
            elseBody.stream().map(instruction -> instruction.toString(backspace + "\t")).collect(Collectors.joining()) +
            backspace + "\tgoto Endif" + ifNum + ";\n" +
            backspace + "True" + ifNum + ":\n" +
            ifBody.stream().map(instruction -> instruction.toString(backspace + "\t")).collect(Collectors.joining()) +
            backspace + "Endif" + ifNum + ":\n";
    }
}
