package ollir.instruction;

import ollir.OllirUtils;
import pt.up.fe.comp.jmm.analysis.table.Symbol;

public class TerminalInstruction implements JmmInstruction {
    private final Symbol rhs;

    public TerminalInstruction(Symbol rhs) {
        this.rhs = rhs;
    }

    @Override
    public JmmInstruction getVariable() {
        return this;
    }

    @Override
    public String toString(String backspace) {
        return backspace + toString() + ";\n";
    }

    @Override
    public String toString() {
        return rhs.getName() + "." + OllirUtils.typeToOllir(rhs.getType());
    }
}
