package ollir.instruction;

import ollir.OllirUtils;
import pt.up.fe.comp.jmm.analysis.table.Symbol;

import java.util.List;
import java.util.stream.Collectors;

public class MethodInstruction implements JmmInstruction {
    private final Symbol methodSymbol;
    private final List<Symbol> params;
    private final List<JmmInstruction> instructions;

    public MethodInstruction(Symbol methodSymbol, List<Symbol> params, List<JmmInstruction> instructions) {
        this.methodSymbol = methodSymbol;
        this.params = params;
        this.instructions = instructions;
    }

    @Override
    public JmmInstruction getVariable(String _s) {
        return new NullInstruction();
    }

    @Override
    public String toString(String backspace) {
        return backspace + ".method public " + methodSymbol.getName() + "(" +
            params.stream().map(symbol -> symbol.getName() + "." + OllirUtils.typeToOllir(symbol.getType())).collect(Collectors.joining(", ")) + ")." +
            OllirUtils.typeToOllir(methodSymbol.getType()) + " {\n" +
            instructions.stream().map(inst -> inst.toString(backspace + "\t")).collect(Collectors.joining()) +
            (methodSymbol.getType().getName().equals("void") ? backspace + "\tret.V;\n" : "") +
            backspace + "}\n";
    }
}
