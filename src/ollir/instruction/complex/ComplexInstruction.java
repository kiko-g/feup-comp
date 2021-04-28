package ollir.instruction.complex;

import ollir.instruction.JmmInstruction;

public abstract class ComplexInstruction implements JmmInstruction {
    protected static int stackCounter = 0;

    public static int getStackCounter() {
        return stackCounter;
    }
}
