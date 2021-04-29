package ollir.instruction.complex;

import ollir.instruction.JmmInstruction;
import ollir.instruction.TerminalInstruction;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class NewObjectInstruction extends ComplexInstruction {
    private final String className;
    private final TerminalInstruction terminalInstruction;

    public NewObjectInstruction(String className) {
        this.terminalInstruction = new TerminalInstruction(new Symbol(new Type(className, false), "t" + stackCounter++));

        this.className = className;
    }

    @Override
    public JmmInstruction getVariable() {
        return terminalInstruction;
    }

    public String toStringNew() {
        return terminalInstruction + " :=." + className + " new(" + className + ")." + className;
    }

    public String toStringSpecial() {
        return "invokespecial(" + terminalInstruction + ",\"<init>\").V";
    }

    @Override
    public String toString(String backspace) {
        return backspace + toStringNew() + ";\n" +
            backspace + toStringSpecial() + ";\n";
    }
}
