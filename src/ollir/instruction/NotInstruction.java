package ollir.instruction;

import pt.up.fe.comp.jmm.analysis.table.Type;

public class NotInstruction extends BinaryOperationInstruction {
    // a.bool !.bool a.bool
    public NotInstruction(JmmInstruction rhs) {
        super(rhs, rhs, new Operation(OperationType.NOT, new Type("bool", false)));
    }
}
