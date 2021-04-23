package ollir.instruction;

public abstract class ComplexInstruction implements JmmInstruction {
    protected static int stackCounter = 0;

    public static int getStackCounter() {
        return stackCounter;
    }
}
