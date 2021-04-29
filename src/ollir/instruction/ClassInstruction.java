package ollir.instruction;

import ollir.OllirUtils;
import pt.up.fe.comp.jmm.analysis.table.Symbol;

import java.util.List;
import java.util.stream.Collectors;

public class ClassInstruction implements JmmInstruction {
    private final String className;
    private final List<JmmInstruction> instructions;
    private final List<Symbol> fields;

    public ClassInstruction(String className, List<Symbol> fields, List<JmmInstruction> instructions) {
        this.className = className;
        this.instructions = instructions;
        this.fields = fields;
    }

    @Override
    public JmmInstruction getVariable() {
        return new NullInstruction();
    }

    @Override
    public String toString(String backspace) {
        return className + " {\n" +
            fields.stream().map(this::symbolToField).collect(Collectors.joining()) + "\n" +
            "\t.construct " + className + "().V {\n" +
            "\t\tinvokespecial(this, \"<init>\").V;\n" +
            "\t}\n\n" +
            instructions.stream().map(inst -> inst.toString("\t")).collect(Collectors.joining("\n")) +
            "}\n";
    }

    private String symbolToField(Symbol symbol) {
        return "\t.field private " + symbol.getName() + "." + OllirUtils.typeToOllir(symbol.getType()) + ";\n";
    }
}
