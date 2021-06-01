package ollir.instruction.complex;

import ollir.OllirUtils;
import ollir.instruction.JmmInstruction;
import ollir.instruction.NullInstruction;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class ReturnInstruction extends ComplexInstruction {
    private final JmmInstruction instruction;
    private final Type returnType;

    public ReturnInstruction(JmmInstruction instruction, Type returnType) {
        this.instruction = instruction;
        this.returnType = returnType;
    }

    @Override
    public JmmInstruction getVariable(String _s) {
        return new NullInstruction();
    }

    @Override
    public String toString() {
        return "ret." + OllirUtils.typeToOllir(returnType) + " " + instruction;
    }

    @Override
    public String toString(String backspace) {
        return backspace + this + ";\n";
    }
}

