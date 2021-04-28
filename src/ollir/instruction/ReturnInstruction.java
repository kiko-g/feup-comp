package ollir.instruction;

import ollir.OllirUtils;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class ReturnInstruction extends ComplexInstruction {
    private final JmmInstruction instruction;
    private final Type returnType;

    public ReturnInstruction(JmmInstruction instruction, Type returnType) {
        this.instruction = instruction;
        this.returnType = returnType;
    }

    @Override
    public JmmInstruction getVariable() {
        return new NullInstruction();
    }

    @Override
    public String toString() {
        return "ret" + OllirUtils.typeToOllir(returnType) + " " + instruction;
    }

    @Override
    public String toString(String backspace) {
        return backspace + this + ";\n";
    }
}

