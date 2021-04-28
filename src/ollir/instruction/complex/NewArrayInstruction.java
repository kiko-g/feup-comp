package ollir.instruction.complex;

import ollir.instruction.JmmInstruction;
import ollir.instruction.TerminalInstruction;
import ollir.instruction.complex.ComplexInstruction;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class NewArrayInstruction extends ComplexInstruction {
    private final JmmInstruction size;
    private final TerminalInstruction terminalInstruction;

    public NewArrayInstruction(JmmInstruction size) {
        this.terminalInstruction = new TerminalInstruction(new Symbol(new Type("int", true), "t" + stackCounter++));

        this.size = size;
    }

    @Override
    public JmmInstruction getVariable() {
        return terminalInstruction;
    }

    public String toString() {
        return terminalInstruction + " :=.array.i32 new(array, " + size + ").array.i32";
    }

    @Override
    public String toString(String backspace) {
        return backspace + this + ";\n";
    }
}
