package ollir.instruction.complex.binary;

import ollir.instruction.JmmInstruction;
import ollir.instruction.Operation;
import ollir.instruction.OperationType;
import ollir.instruction.TerminalInstruction;
import ollir.instruction.complex.ComplexInstruction;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class NewArrayInstruction extends BinaryOperationInstruction {
    private final JmmInstruction size;

    public NewArrayInstruction(JmmInstruction size) {
        super(null, size, new Operation(OperationType.NONE, new Type("int", true)));

        this.size = size;
    }

    @Override
    public JmmInstruction getVariable(String scope) {
        if (operation.getOperationType() == OperationType.EQUALS) {
            return lhs;
        }

        hasVariable = true;

        TerminalInstruction saveVariable = new TerminalInstruction(new Symbol(operation.getResultType(), ComplexInstruction.getAuxVar(scope)));
        BinaryOperationInstruction newOperation = new NewArrayInstruction(size);

        lhs = saveVariable;
        rhs = newOperation;
        operation = new Operation(OperationType.EQUALS, operation.getResultType());

        return lhs;
    }

    @Override
    public String toString() {
        return hasVariable ? super.toString() : "new(array, " + size + ").array.i32";
    }

    @Override
    public String toString(String backspace) {
        return backspace + this + ";\n";
    }
}
