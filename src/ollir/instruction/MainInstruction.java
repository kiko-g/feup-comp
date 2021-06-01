package ollir.instruction;

import ollir.OllirUtils;
import pt.up.fe.comp.jmm.analysis.table.Symbol;

import java.util.List;
import java.util.stream.Collectors;

public class MainInstruction implements JmmInstruction {
    private final List<Symbol> params;
    private final List<JmmInstruction> instructions;

    public MainInstruction(List<Symbol> params, List<JmmInstruction> instructions) {
        this.params = params;
        this.instructions = instructions;
    }

    @Override
    public JmmInstruction getVariable(String _s) {
        return new NullInstruction();
    }

    @Override
    public String toString(String backspace) {
        return backspace + ".method public static main(" +
            params.stream().map(symbol -> symbol.getName() + "." + OllirUtils.typeToOllir(symbol.getType())).collect(Collectors.joining(", ")) + ").V {\n" +
            instructions.stream().map(inst -> inst.toString(backspace + "\t")).collect(Collectors.joining()) +
            backspace + "\tret.V;\n" +
            backspace + "}\n";
    }
}