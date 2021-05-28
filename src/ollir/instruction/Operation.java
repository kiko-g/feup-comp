package ollir.instruction;

import ollir.OllirUtils;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class Operation {
    private final OperationType operationType;
    private final Type resultType;

    public Operation(OperationType operationType, Type resultType) {
        this.operationType = operationType;
        this.resultType = resultType;
    }

    public Operation(Operation operation) {
        this.operationType = operation.operationType;
        this.resultType = new Type(operation.resultType.getName(), operation.resultType.isArray());
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public Type getResultType() {
        return resultType;
    }

    public Operation inverseOperation() {
        return switch (operationType) {
            case ADD -> new Operation(OperationType.SUB, this.resultType);
            case SUB -> new Operation(OperationType.ADD, this.resultType);
            case MUL -> new Operation(OperationType.DIV, this.resultType);
            case DIV -> new Operation(OperationType.MUL, this.resultType);
            case AND -> new Operation(OperationType.OR, this.resultType);
            case LESS_THAN -> new Operation(OperationType.GREATER_OR_EQUAL, this.resultType);
            case GREATER_OR_EQUAL -> new Operation(OperationType.LESS_THAN, this.resultType);
            case NOT, NOT_EQUAL -> new Operation(OperationType.IS_EQUAL, this.resultType);
            case IS_EQUAL -> new Operation(OperationType.NOT_EQUAL, this.resultType);
            default -> this;
        };
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
            case OR -> operation = "||";
            case LESS_THAN -> operation = "<";
            case GREATER_OR_EQUAL -> operation = ">=";
            case EQUALS -> operation = ":=";
            case NOT -> operation = "!";
            case IS_EQUAL -> operation = "==";
            case NOT_EQUAL -> operation = "!=";
        }

        return operation + "." + OllirUtils.typeToOllir(resultType);
    }
}
