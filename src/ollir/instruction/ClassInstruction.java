package ollir.instruction;

import java.util.List;
import java.util.stream.Collectors;

public class ClassInstruction implements JmmInstruction {
    private final String className;
    private final List<JmmInstruction> instructions;

    public ClassInstruction(String className, List<JmmInstruction> instructions) {
        this.className = className;
        this.instructions = instructions;
    }

    @Override
    public JmmInstruction getVariable() {
        return new NullInstruction();
    }

    @Override
    public String toString(String backspace) {
        return className + " {\n" +
            "\t.construct " + className + "().V {\n" +
            "\t\tinvokespecial(this, \"<init>\").V;\n" +
            "\t}\n" +
            instructions.stream().map(inst -> inst.toString("\t")).collect(Collectors.joining()) +
            "}\n";
    }
}
