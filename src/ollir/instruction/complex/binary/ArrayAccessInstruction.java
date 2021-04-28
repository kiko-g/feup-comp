package ollir.instruction.complex.binary;

import ollir.instruction.JmmInstruction;
import ollir.instruction.Operation;
import ollir.instruction.OperationType;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class ArrayAccessInstruction extends BinaryOperationInstruction {
    public ArrayAccessInstruction(JmmInstruction lhs, JmmInstruction rhs) {
        super(lhs, rhs, new Operation(OperationType.ARRAY_ACCESS, new Type("int", true)));
    }

    @Override
    public String toString() {
        return hasVariable ? super.toString() : lhs + "[" + rhs + "].i32";
    }
}
