package ollir.instruction;

public interface JmmInstruction {
    JmmInstruction getVariable(String scope);
    String toString(String backspace);
}
