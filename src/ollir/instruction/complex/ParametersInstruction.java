package ollir.instruction.complex;
import ollir.instruction.JmmInstruction;
import ollir.instruction.TerminalInstruction;
import ollir.instruction.complex.ComplexInstruction;

import java.util.List;
import java.util.stream.Collectors;

public class ParametersInstruction extends ComplexInstruction {
    private final List<JmmInstruction> instructions;
    private JmmInstruction terminalInstruction;

    public ParametersInstruction(List<JmmInstruction> instructions) {
        this.instructions = instructions;

        terminalInstruction = instructions.get(instructions.size()-1);

        if(terminalInstruction instanceof TerminalInstruction) {
            instructions.remove(terminalInstruction);
        }
        else {
            terminalInstruction = terminalInstruction.getVariable();
        }
    }

    @Override
    public JmmInstruction getVariable() {
        return terminalInstruction;
    }

    @Override
    public String toString(String backspace) {
        return instructions.stream().map(x -> x.toString(backspace)).collect(Collectors.joining());
    }
}
