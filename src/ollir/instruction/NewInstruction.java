package ollir.instruction;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class NewInstruction extends BinaryOperationInstruction {
    private final String className;

    public NewInstruction(String className) {
        super(
            new TerminalInstruction(new Symbol(new Type(className, false), "t" + stackCounter++)),
            new NullInstruction(),
            new Operation(OperationType.DOT, new Type(className, false))
        );

        this.className = className;
    }

    @Override
    public JmmInstruction getVariable() {
        return lhs;
    }

    public String toStringNew() {
        return lhs + " :=." + className + "new(" + className + ")." + className;
    }

    public String toStringSpecial() {
        return "invokespecial" + lhs + ",\"<init>\").V";
    }

    @Override
    public String toString(String backspace) {
        return backspace + toStringNew() + ";\n" +
            backspace + toStringSpecial() + ";\n";
    }
}
