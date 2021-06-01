package ollir.instruction;

import ollir.OllirUtils;
import pt.up.fe.comp.jmm.analysis.table.Symbol;

public class PutFieldInstruction implements JmmInstruction {
    private final Symbol field;
    private final JmmInstruction rhs;

    public PutFieldInstruction(Symbol field, JmmInstruction rhs) {
        this.field = field;
        this.rhs = rhs;
    }

    @Override
    public JmmInstruction getVariable(String _s) {
        return new NullInstruction();
    }

    @Override
    public String toString(String backspace) {
        return backspace + this + ";\n";
    }

    public String toString() {
        return "putfield(this, " + field.getName() + "." + OllirUtils.typeToOllir(field.getType()) + ", " + rhs + ").V";
    }
}
