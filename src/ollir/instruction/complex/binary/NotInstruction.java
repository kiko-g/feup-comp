package ollir.instruction.complex.binary;

import ollir.instruction.JmmInstruction;
import ollir.instruction.Operation;
import ollir.instruction.OperationType;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class NotInstruction extends BinaryOperationInstruction {
    public NotInstruction(JmmInstruction rhs) {
        super(rhs, rhs, new Operation(OperationType.NOT, new Type("bool", false)));
    }
}
