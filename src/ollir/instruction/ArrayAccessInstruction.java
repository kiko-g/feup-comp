package ollir.instruction;

import pt.up.fe.comp.jmm.analysis.table.Type;

public class ArrayAccessInstruction extends BinaryOperationInstruction {
    public ArrayAccessInstruction(JmmInstruction lhs, JmmInstruction rhs) {
        super(lhs, rhs, new Operation(OperationType.ARRAY_ACCESS, new Type("int", true)));
    }

    @Override
    public String toString() {
        return lhs + "[" + rhs + "].i32";
    }
}
