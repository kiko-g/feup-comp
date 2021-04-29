package ollir.instruction.complex.binary;

import ollir.instruction.JmmInstruction;
import ollir.instruction.NullInstruction;
import ollir.instruction.Operation;
import ollir.instruction.OperationType;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class ArrayAccessInstruction extends BinaryOperationInstruction {
    private final Symbol array;

    public ArrayAccessInstruction(Symbol array, JmmInstruction rhs) {
        super(new NullInstruction(), rhs, new Operation(OperationType.ARRAY_ACCESS, new Type("int", true)));

        this.array = array;
    }

    @Override
    public JmmInstruction getVariable() {
        return this;
    }

    @Override
    public String toString() {
        return array.getName() + "[" + rhs + "].i32";
    }
}
