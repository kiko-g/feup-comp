package ollir.instruction;

import ollir.OllirUtils;
import pt.up.fe.comp.jmm.analysis.table.Symbol;

public class TerminalInstruction implements JmmInstruction {
    private final Symbol terminal;

    public TerminalInstruction(Symbol terminal) {
        this.terminal = terminal;
    }

    public Symbol getTerminal() {
        return terminal;
    }

    @Override
    public JmmInstruction getVariable() {
        return this;
    }

    @Override
    public String toString(String backspace) {
        return backspace + this + ";\n";
    }

    @Override
    public String toString() {
        if(terminal.getName().equals("this")) return "this";
        return terminal.getName() + "." + OllirUtils.typeToOllir(terminal.getType());
    }
}
