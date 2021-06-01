package ollir.instruction.complex.binary;

import ollir.OllirUtils;
import ollir.instruction.*;
import ollir.instruction.complex.ComplexInstruction;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;
import java.util.stream.Collectors;

public class DotStaticMethodInstruction extends BinaryOperationInstruction {
    private final String methodName;
    private final List<JmmInstruction> params;
    private final String importClass;

    public DotStaticMethodInstruction(String importClass, String methodName, List<JmmInstruction> params, Type returnType) {
        super(new NullInstruction(), new NullInstruction(), new Operation(OperationType.DOT, returnType));
        this.importClass = importClass;
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
        BinaryOperationInstruction newOperation = new DotStaticMethodInstruction(importClass, methodName, params, operation.getResultType());

        lhs = saveVariable;
        rhs = newOperation;
        operation = new Operation(OperationType.EQUALS, operation.getResultType());

        return lhs;
    }

    @Override
    public String toString() {
        return hasVariable ? super.toString() : "invokestatic(" + importClass + ", \"" + methodName + "\"" +
            (params.size() > 0 ? ", " : "") +
            params.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")." +
            OllirUtils.typeToOllir(operation.getResultType());
    }

    @Override
    public String toString(String backspace) {
        return backspace + this + ";\n";
    }
}
