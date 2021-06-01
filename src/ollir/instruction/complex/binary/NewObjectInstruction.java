package ollir.instruction.complex.binary;

import ollir.instruction.JmmInstruction;
import ollir.instruction.Operation;
import ollir.instruction.OperationType;
import ollir.instruction.TerminalInstruction;
import ollir.instruction.complex.ComplexInstruction;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class NewObjectInstruction extends BinaryOperationInstruction {
    private final String className;

    public NewObjectInstruction(String className, String scope) {
        super(new TerminalInstruction(new Symbol(new Type(className, false), ComplexInstruction.getAuxVar(scope))),
            null,
            new Operation(OperationType.NONE, new Type(className, false)));

        this.className = className;
    }

    public void setLhs(JmmInstruction lhs) {
        this.lhs = lhs;
    }

    @Override
    public JmmInstruction getVariable(String _s) {
        return this.lhs;
    }

    public String toStringNew() {
        return lhs + " :=." + className + " new(" + className + ")." + className;
    }

    public String toStringSpecial() {
        return "invokespecial(" + lhs + ",\"<init>\").V";
    }

    @Override
    public String toString(String backspace) {
        return backspace + toStringNew() + ";\n" +
            backspace + toStringSpecial() + ";\n";
    }
}
