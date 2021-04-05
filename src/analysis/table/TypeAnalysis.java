package analysis.table;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import report.Report;
import report.ReportType;
import report.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class TypeAnalysis extends AJmmVisitor<String, Type> {
    public final static Type INT = new Type("int", false);
    public final static Type BOOL = new Type("boolean", false);
    public final static Type THIS = new Type("this", false);
    public final static Type LENGTH = new Type("length", false);

    private final AnalysisTable symbolTable;
    private final List<Report> reports;


    public TypeAnalysis(AnalysisTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;

        addVisit("Method", this::visitMethod);
        addVisit("Main", this::visitMain);
        addVisit("Assign", this::visitEqual);
        addVisit("Dot", this::visitDot);
        addVisit("MethodCall", this::visitMethodCall);
        addVisit("Add", this::visitInt);
        addVisit("Sub", this::visitInt);
        addVisit("Mul", this::visitInt);
        addVisit("Div", this::visitInt);
        addVisit("LogicalAnd", this::visitBool);
        addVisit("Less", this::visitIntReturnBool);
        addVisit("Not", this::visitSingleBool);
        addVisit("ArrayAccess", this::visitArray);
//        addVisit("Object", this::visitObject);
        addVisit("IntArray", this::returnInt);
        addVisit("Index", this::visitSingleInt);
        addVisit("Size", this::visitSingleInt);
        addVisit("Var", this::visitVar);
        addVisit("IntegerVal", this::returnInt);
        addVisit("Bool", this::returnBool);
        addVisit("Length", this::returnLength);
        addVisit("This", this::returnThis);
        setDefaultVisit(this::defaultVisit);
    }

    private Type visitMethod(JmmNode node, String scope) {
        JmmNode returnType = node.getChildren().get(0);
        JmmNode name = node.getChildren().get(1);

        for (JmmNode child : node.getChildren()){
            visit(child, name.get("VALUE"));
        }

        return getType(returnType);
    }

    private Type visitMain(JmmNode node, String scope) {
        for (JmmNode child : node.getChildren()){
            visit(child, "main");
        }

        return null;
    }

    private Type visitEqual(JmmNode node, String scope) {
        JmmNode leftOperand = node.getChildren().get(0);
        JmmNode rightOperand = node.getChildren().get(1);

        Type leftType = visit(leftOperand, scope);
        Type rightType = visit(rightOperand, scope);

        if (!leftType.equals(rightType)) {
            this.reports.add(
                    new Report(
                            ReportType.ERROR,
                            Stage.SEMANTIC,
                            Integer.parseInt(node.get("LINE")),
                            Integer.parseInt(node.get("COLUMN")),
                            "Invalid type found. Expected \"" + leftType + "\", found \"" + rightType + "\""
                    )
            );
        }

        return leftType;
    }

    private Type visitDot(JmmNode node, String scope) {
        JmmNode leftOperand = node.getChildren().get(0);
        JmmNode rightOperand = node.getChildren().get(1);

        Type leftType = visit(leftOperand, scope);
        Type rightType = visit(rightOperand, leftType.equals(THIS) ? AnalysisTable.CLASS_SCOPE : scope);

        if (leftType.equals(INT) || leftType.equals(BOOL) || leftType.equals(LENGTH)) {
            this.reports.add(
                    new Report(
                            ReportType.ERROR,
                            Stage.SEMANTIC,
                            Integer.parseInt(node.get("LINE")),
                            Integer.parseInt(node.get("COLUMN")),
                            "Invalid type found. Expected \"<class>\" or \"this\", found \"" + leftType + "\""
                    )
            );
        }

        if (rightType.equals(LENGTH) && !leftType.isArray()) {
            this.reports.add(
                    new Report(
                            ReportType.ERROR,
                            Stage.SEMANTIC,
                            Integer.parseInt(node.get("LINE")),
                            Integer.parseInt(node.get("COLUMN")),
                            "Invalid type found. Expected \"<any_type>[]\", found \"" + rightType + "\""
                    )
            );
        }

        return rightType;
    }

    private Type visitMethodCall(JmmNode node, String scope) {
        JmmNode methodNameNode = node.getChildren().get(0);
        JmmNode paramsNode = node.getChildren().get(1);

        Symbol methodSymbol = this.symbolTable.getMethodSymbol(methodNameNode.get("VALUE"));

        if (methodSymbol == null) {
            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("LINE")),
                    Integer.parseInt(node.get("COLUMN")),
                    "Could not find the method \"" + methodNameNode.get("VALUE") + "\", are you missing an import?"
                )
            );

            return null; // TODO: check if null always works as a return
        }

        List<Type> methodParameters = this.symbolTable.getParameters(methodSymbol.getName()).stream().map(Symbol::getType).collect(Collectors.toList());

        List<Type> givenParameters = paramsNode.getChildren().stream().map(this::visit).collect(Collectors.toList());

        if (methodParameters.size() != givenParameters.size()) {
            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("LINE")),
                    Integer.parseInt(node.get("COLUMN")),
                    "Invalid parameters given. Expected " + methodParameters.size() + " parameters: \""
                        + methodParameters.stream().map(Type::toString).collect(Collectors.joining(", "))
                        + "\"; found " + givenParameters.size() + " parameters: \""
                        + givenParameters.stream().map(Type::toString).collect(Collectors.joining(", ")) + "\""
                )
            );

            return methodSymbol.getType();
        }

        for (int i = 0; i < methodParameters.size(); i++) {
            Type expected = methodParameters.get(i);
            Type given = givenParameters.get(i);

            if (!expected.equals(given)) {
                this.reports.add(
                    new Report(
                        ReportType.ERROR,
                        Stage.SEMANTIC,
                        Integer.parseInt(node.get("LINE")),
                        Integer.parseInt(node.get("COLUMN")),
                        "Invalid type found. Expected \"" + expected + "\", found \"" + given + "\""
                    )
                );
            }
        }

        return methodSymbol.getType();
    }

    private Type visitArray(JmmNode node, String scope) {
        JmmNode leftOperand = node.getChildren().get(0);
        JmmNode rightOperand = node.getChildren().get(1);

        Type leftType = visit(leftOperand, scope);
        Type rightType = visit(rightOperand, scope);

        if (!leftType.isArray()) {
            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("LINE")),
                    Integer.parseInt(node.get("COLUMN")),
                    "Invalid type found. Expected \"<type>[]\", found \"" + leftType + "\""
                )
            );
        }

        if (!rightType.equals(INT)) {
            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("LINE")),
                    Integer.parseInt(node.get("COLUMN")),
                    "Invalid type found. Expected \"" + INT + "\", found \"" + rightType + "\""
                )
            );
        }

        return new Type(leftType.getName(), false);
    }

    private Type visitIntReturnBool(JmmNode node, String scope) {
        checkTwoTypes(node, scope, INT);

        return BOOL;
    }

    private Type visitBool(JmmNode node, String scope) {
        checkTwoTypes(node, scope, BOOL);

        return BOOL;
    }

    private Type visitSingleInt(JmmNode node, String scope) {
        checkSingleType(node, scope, BOOL);

        return INT;
    }

    private Type visitSingleBool(JmmNode node, String scope) {
        checkSingleType(node, scope, INT);

        return BOOL;
    }

    private Type visitInt(JmmNode node, String scope) {
        checkTwoTypes(node, scope, INT);

        return INT;
    }

    private Type visitVar(JmmNode node, String scope) {
        Symbol variable = this.symbolTable.getVariable(scope, node.get("VALUE"));

        if(variable == null) {
            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("LINE")),
                    Integer.parseInt(node.get("COLUMN")),
                    "Variable \"" + node.get("VALUE") + "\" has not been initialized in this scope"
                )
            );

            return null; // TODO: check if this can be null without further checks
        }

        return variable.getType();
    }

    private Type returnInt(JmmNode node, String scope) {
        return INT;
    }

    private Type returnBool(JmmNode node, String scope) {
        return BOOL;
    }

    private Type returnLength(JmmNode node, String scope) {
        return LENGTH;
    }

    private Type returnThis(JmmNode node, String scope) {
        return THIS;
    }

    private void checkTwoTypes(JmmNode node, String scope, Type type) {
        JmmNode leftOperand = node.getChildren().get(0);
        JmmNode rightOperand = node.getChildren().get(1);

        Type leftType = visit(leftOperand, scope);
        Type rightType = visit(rightOperand, scope);

        if (!leftType.equals(type) || !rightType.equals(type)) {
            Type wrongType = leftType.equals(type) ? rightType : leftType;

            this.reports.add(
                    new Report(
                            ReportType.ERROR,
                            Stage.SEMANTIC,
                            Integer.parseInt(node.get("LINE")),
                            Integer.parseInt(node.get("COLUMN")),
                            "Invalid type found on " + (wrongType == rightType ? "right" : "left")
                                    + " side operand. Expected \"" + type + "\", found \"" + wrongType + "\""
                    )
            );
        }
    }

    private void checkSingleType(JmmNode node, String scope, Type type) {
        Type childType = visit(node.getChildren().get(0));

        if (!childType.equals(INT)) {
            this.reports.add(
                    new Report(
                            ReportType.ERROR,
                            Stage.SEMANTIC,
                            Integer.parseInt(node.get("LINE")),
                            Integer.parseInt(node.get("COLUMN")),
                            "Invalid expression type found. Expected \"" + INT + "\", found \"" + childType + "\""
                    )
            );
        }
    }

    private Type getType(JmmNode node) {
        if (node.getKind().equals("Array")) {
            return new Type(node.getChildren().get(0).get("VALUE"), true);
        }

        return new Type(node.get("VALUE"), false);
    }

    private Type defaultVisit(JmmNode node, String scope) {
        Type type = null;
        for (JmmNode child : node.getChildren()) {
            type = visit(child, scope);
        }

        return type;
    }
}
