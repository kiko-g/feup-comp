package ollir.instruction;

import ollir.OllirUtils;
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
    public JmmInstruction getVariable() {
        if(operation.getResultType().getName().equals("void")) {
            return new NullInstruction();
        }

        if (operation.getOperationType() == OperationType.EQUALS) {
            return lhs;
        }

        TerminalInstruction saveVariable = new TerminalInstruction(new Symbol(operation.getResultType(), "t" + ComplexInstruction.stackCounter++));
        BinaryOperationInstruction newOperation = new DotMethodInstruction(lhs, methodName, params, operation.getResultType());

        lhs = saveVariable;
        rhs = newOperation;
        operation = new Operation(OperationType.EQUALS, operation.getResultType());

        return lhs;
    }

    @Override
    public String toString() {
        return "invokevirtual(" + lhs.toString() + ", \"" + methodName + "\", " +
            params.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")." +
            OllirUtils.typeToOllir(operation.getResultType());
    }

    @Override
    public String toString(String backspace) {
        return backspace + this + ";\n";
    }
}
