package ollir.instruction;

import pt.up.fe.comp.jmm.analysis.table.Symbol;

public class ConstantInstruction extends TerminalInstruction {
    public ConstantInstruction(Symbol terminal) {
        super(terminal);
    }
}
