package ollir.instruction.complex.binary;

import ollir.OllirUtils;
import ollir.instruction.*;
import ollir.instruction.complex.ComplexInstruction;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;
import java.util.stream.Collectors;

public class DotMethodInstruction extends BinaryOperationInstruction {
    private final String methodName;
    private final List<JmmInstruction> params;

    public DotMethodInstruction(JmmInstruction obj, String methodName, List<JmmInstruction> params, Type returnType) {
        super(obj, new NullInstruction(), new Operation(OperationType.DOT, returnType));
        this.methodName = methodName;
        this.params = params;
    }

    @Override
    public JmmInstruction getVariable(String scope) {
        if(operation.getResultType().getName().equals("void")) {
            return new NullInstruction();
        }

        if (operation.getOperationType() == OperationType.EQUALS) {
            return lhs;
        }

        hasVariable = true;

        TerminalInstruction saveVariable = new TerminalInstruction(new Symbol(operation.getResultType(), ComplexInstruction.getAuxVar(scope)));
        BinaryOperationInstruction newOperation = new DotMethodInstruction(lhs, methodName, params, operation.getResultType());

        lhs = saveVariable;
        rhs = newOperation;
        operation = new Operation(OperationType.EQUALS, operation.getResultType());

        return lhs;
    }

    @Override
    public String toString() {
        return hasVariable ? super.toString() : "invokevirtual(" + lhs.toString() + ", \"" + methodName + "\"" +
            (params.size() > 0 ? ", " : "") +
            params.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")." +
            OllirUtils.typeToOllir(operation.getResultType());
    }

    @Override
    public String toString(String backspace) {
        return backspace + this + ";\n";
    }
}
