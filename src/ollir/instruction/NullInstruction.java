package ollir.instruction;

public class NullInstruction implements JmmInstruction {
    @Override
    public JmmInstruction getVariable() {
        return this;
    }

    @Override
    public String toString(String backspace) {
        return "";
    }
}
