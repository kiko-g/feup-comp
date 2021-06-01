package ollir.instruction.complex.binary;

import ollir.instruction.*;
import ollir.instruction.complex.ComplexInstruction;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class DotLengthInstruction extends BinaryOperationInstruction {
    public DotLengthInstruction(JmmInstruction array) {
        super(array, new NullInstruction(), new Operation(OperationType.DOT, new Type("int", false)));
    }

    @Override
    public JmmInstruction getVariable(String scope) {
        if (operation.getOperationType() == OperationType.EQUALS) {
            return lhs;
        }

        hasVariable = true;

        TerminalInstruction saveVariable = new TerminalInstruction(new Symbol(operation.getResultType(), ComplexInstruction.getAuxVar(scope)));
        BinaryOperationInstruction newOperation = new DotLengthInstruction(lhs);

        lhs = saveVariable;
        rhs = newOperation;
        operation = new Operation(OperationType.EQUALS, operation.getResultType());

        return lhs;
    }

    @Override
    public String toString() {
        return hasVariable ? super.toString() : "arraylength(" + lhs.toString() + ").i32";
    }

    @Override
    public String toString(String backspace) {
        return backspace + this + ";\n";
    }
}
