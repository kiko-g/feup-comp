package ollir.instruction;

import ollir.OllirUtils;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;
import java.util.stream.Collectors;

public class MethodInstruction implements JmmInstruction {
    private final String methodName;
    private final List<Symbol> params;
    private final List<JmmInstruction> instructions;
    private final Type returnType;

    public MethodInstruction(String methodName, List<Symbol> params, List<JmmInstruction> instructions, Type returnType) {
        this.methodName = methodName;
        this.params = params;
        this.instructions = instructions;
        this.returnType = returnType;
    }

    @Override
    public JmmInstruction getVariable() {
        return new NullInstruction();
    }

    @Override
    public String toString(String backspace) {
        return backspace + ".method public " + methodName + "(" +
            params.stream().map(symbol -> symbol.getName() + "." + OllirUtils.typeToOllir(symbol.getType())).collect(Collectors.joining(", ")) + ")." +
            OllirUtils.typeToOllir(returnType) + " {\n" +
            instructions.stream().map(inst -> inst.toString(backspace + "\t")).collect(Collectors.joining()) +
            backspace + "}\n";
    }
}
