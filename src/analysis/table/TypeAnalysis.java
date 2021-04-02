package analysis.table;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import report.Report;
import report.ReportType;
import report.Stage;

import java.util.List;

public class TypeAnalysis extends AJmmVisitor<String, Type> {
    private final AnalysisTable symbolTable;
    private final List<Report> reports;
    public final static Type INT = new Type("int", false);


    public TypeAnalysis(AnalysisTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;

        addVisit("Add", this::visitInt);
        addVisit("Sub", this::visitInt);
        addVisit("Mul", this::visitInt);
        addVisit("Div", this::visitInt);
        addVisit("Method", this::visitMethod);
//        addVisit("Method", this::visitMain);
//        addVisit("LogicalAnd", this::visitBool);
//        addVisit("Less", this::visitIntReturnBool);
//        addVisit("Not", this::visitSingleBool);
//        addVisit("ArrayAccess", this::visitArray);
//        addVisit("Dot", this::visitClass);
//        addVisit("Length", this::visitArray);
//        addVisit("MethodCall", this::visitAdd);
//        addVisit("Index", this::visitSingleInt);
//        addVisit("Assign", this::visitEqual);
//        addVisit("Var", this::visitVar);
//        addVisit("IntegerVal", this::visitVar);
//        addVisit("Bool", this::visitVar);
//        addVisit("This", this::visitVar);
//        addVisit("New", this::visitVar);
//        addVisit("Important", this::visitVar);
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

    private Type visitInt(JmmNode node, String scope) {
        JmmNode leftOperand = node.getChildren().get(0);
        JmmNode rightOperand = node.getChildren().get(1);

        Type leftType = visit(leftOperand);
        Type rightType = visit(rightOperand);

        if (!leftType.equals(INT) || !rightType.equals(INT)) {
            Type wrongType = leftType.equals(INT) ? rightType : leftType;

            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("LINE")),
                    Integer.parseInt(node.get("COLUMN")),
                    "Invalid type on " + (wrongType == rightType ? "right" : "left")
                        + " side operand. Expected \"int\", found \"" + wrongType + "\""
                )
            );
        }

        return INT;
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
