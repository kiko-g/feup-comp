package ollir.instruction.complex.binary;

import ollir.OllirUtils;
import ollir.instruction.*;
import ollir.instruction.complex.ComplexInstruction;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class ArrayAccessInstruction extends BinaryOperationInstruction {
    private final Symbol array;

    public ArrayAccessInstruction(Symbol array, JmmInstruction rhs) {
        super(new NullInstruction(), rhs, new Operation(OperationType.ARRAY_ACCESS, new Type(array.getType().getName(), false)));

        this.array = array;
    }

    @Override
    public JmmInstruction getVariable(String scope) {
        if (operation.getOperationType() == OperationType.EQUALS) {
            return lhs;
        }

        hasVariable = true;

        TerminalInstruction saveVariable = new TerminalInstruction(new Symbol(operation.getResultType(), ComplexInstruction.getAuxVar(scope)));
        BinaryOperationInstruction newOperation = new ArrayAccessInstruction(array, rhs);

        lhs = saveVariable;
        rhs = newOperation;
        operation = new Operation(OperationType.EQUALS, operation.getResultType());

        return lhs;
    }

    @Override
    public String toString() {
        return hasVariable ? super.toString() : array.getName() + "[" + rhs + "]." + OllirUtils.typeToOllir(operation.getResultType());
    }
}
