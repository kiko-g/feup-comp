package ollir.instruction;

public interface JmmInstruction {
    JmmInstruction getVariable();
    String toString(String backspace);
}
