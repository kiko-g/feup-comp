package ollir.instruction.complex;

import analysis.table.AnalysisTable;
import ollir.instruction.JmmInstruction;

public abstract class ComplexInstruction implements JmmInstruction {
    protected static int stackCounter = 0;

    public static int getStackCounter() {
        return stackCounter;
    }

    public static void setStackCounter(int stackCounter) {
        ComplexInstruction.stackCounter = stackCounter;
    }

    protected static String getAuxVar(String scope) {
        String aux = "t" + ComplexInstruction.stackCounter++;

        while (AnalysisTable.getInstance() != null && AnalysisTable.getInstance().hasConflict(aux, scope)) {
            aux = "t" + ComplexInstruction.stackCounter++;
        }

        return aux;
    }
}
