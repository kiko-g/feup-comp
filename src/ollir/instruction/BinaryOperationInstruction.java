package ollir.instruction;

import pt.up.fe.comp.jmm.analysis.table.Symbol;

public class BinaryOperationInstruction extends ComplexInstruction {
    protected JmmInstruction rhs, lhs;
    protected Operation operation;

    public BinaryOperationInstruction(JmmInstruction lhs, JmmInstruction rhs, Operation operation) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.operation = operation;
    }

    protected BinaryOperationInstruction(BinaryOperationInstruction instruction) {
        this.lhs = instruction.lhs;
        this.rhs = instruction.rhs;
        this.operation = instruction.operation;
    }

    @Override
    public JmmInstruction getVariable() {
        if (operation.getOperationType() == OperationType.EQUALS) {
            return lhs;
        }

        TerminalInstruction saveVariable = new TerminalInstruction(new Symbol(operation.getResultType(), "t" + ComplexInstruction.stackCounter++));
        BinaryOperationInstruction newOperation = new BinaryOperationInstruction(this);

        lhs = saveVariable;
        rhs = newOperation;
        operation = new Operation(OperationType.EQUALS, operation.getResultType());

        return lhs;
    }

    @Override
    public String toString(String backspace) {
        return backspace + toString() + ";\n";
    }

    @Override
    public String toString() {
        return lhs.toString() + operation.toString() + rhs.toString();
    }
}
