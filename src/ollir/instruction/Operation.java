package ollir.instruction;

import pt.up.fe.comp.jmm.analysis.table.Type;

public class Operation {
    private final OperationType operationType;
    private final Type resultType;

    public Operation(OperationType operationType, Type resultType) {
        this.operationType = operationType;
        this.resultType = resultType;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public Type getResultType() {
        return resultType;
    }

    @Override
    public String toString() {
        String operation = "";

        switch (operationType) {
            case ADD -> operation = "+";
            case SUB -> operation = "-";
            case MUL -> operation = "*";
            case DIV -> operation = "/";
            case AND -> operation = "&&";
            case LESS_THAN -> operation = "<";
            case EQUALS -> operation = ":=";
            case NOT -> operation = "!";
        }

        return operation + resultType;
    }
}
