package ollir.instruction.complex.binary;

import ollir.OllirUtils;
import ollir.instruction.*;
import ollir.instruction.complex.ComplexInstruction;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class FieldInstruction extends BinaryOperationInstruction {
    private final Symbol field;

    public FieldInstruction(Symbol field) {
        super(new NullInstruction(), new NullInstruction(), new Operation(OperationType.NONE, new Type("", false)));

        this.field = field;
    }

    @Override
    public JmmInstruction getVariable() {
        if (operation.getOperationType() == OperationType.EQUALS) {
            return lhs;
        }

        hasVariable = true;

        TerminalInstruction saveVariable = new TerminalInstruction(new Symbol(field.getType(), "t" + ComplexInstruction.stackCounter++));
        BinaryOperationInstruction newOperation = new FieldInstruction(this.field);

        lhs = saveVariable;
        rhs = newOperation;
        operation = new Operation(OperationType.EQUALS, field.getType());

        return lhs;
    }

    @Override
    public String toString() {
        return hasVariable ? super.toString() : "getfield(this, " + field.getName() + "." + OllirUtils.typeToOllir(field.getType()) + ")." + OllirUtils.typeToOllir(field.getType());
    }

    @Override
    public String toString(String backspace) {
        return backspace + this + ";\n";
    }
}
