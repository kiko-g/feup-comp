package analysis.table;

import java.util.List;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import report.Report;

public class AnalysisTableBuilder extends AJmmVisitor<String, String> {
    private final AnalysisTable symbolTable;
    private final List<Report> reports;
    public final static String CLASS_SCOPE = "";

    public AnalysisTableBuilder(AnalysisTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;

        addVisit("Import", this::visitImport);
        addVisit("Class", this::visitClass);
        addVisit("VarDecl", this::visitVarDecl);
        addVisit("Method", this::visitMethod);
        addVisit("Main", this::visitMain);

        setDefaultVisit(this::defaultVisit);
    }

    public String visitImport(JmmNode node, String scope) {
        StringBuilder importStmt = new StringBuilder();

        for (JmmNode child : node.getChildren()) {
            importStmt.append(child.get("VALUE")).append('.');
        }

        importStmt.deleteCharAt(importStmt.length() - 1);
        this.symbolTable.addImport(importStmt.toString());

        return defaultVisit(node, "");
    }

    public String visitClass(JmmNode node, String scope) {
        this.symbolTable.setClassName(node.getChildren().get(0).get("VALUE"), CLASS_SCOPE);
        return defaultVisit(node, CLASS_SCOPE);
    }

    public String visitVarDecl(JmmNode node, String scope) {
        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);
        this.symbolTable.addLocalVariable(scope, new Symbol(getType(firstChild), secondChild.get("VALUE")));
        return defaultVisit(node, scope);
    }

    public String visitMethod(JmmNode node, String scope) {
        JmmNode returnType = node.getChildren().get(0);
        JmmNode name = node.getChildren().get(1);

        System.out.println(name);
        Symbol method = new Symbol(getType(returnType), name.get("VALUE"));
        this.symbolTable.addMethod(method);

        if(node.getChildren().size() >= 3) {
            this.fillMethodParameters(node.getChildren().get(2), method);
        }
        
        return defaultVisit(node, method.getName());
    }

    public String visitMain(JmmNode node, String scope) {
        JmmNode params = node.getChildren().get(0);
        Symbol method = new Symbol(new Type("void", false), "main");

        this.symbolTable.addMethod(method);
        this.fillMethodParameters(params, method);

        return defaultVisit(node, method.getName());
    }

    private void fillMethodParameters(JmmNode node, Symbol method) {
        for(JmmNode child: node.getChildren()) {
            JmmNode firstChild = child.getChildren().get(0);
            JmmNode secondChild = child.getChildren().get(1);
            this.symbolTable.addParameter(method, new Symbol(getType(firstChild), secondChild.get("VALUE")));
        }
    }

    private Type getType(JmmNode node) {
        if (node.getKind().equals("Array")) {
            return new Type(node.getChildren().get(0).get("VALUE"), true);
        }

        return new Type(node.get("VALUE"), false);
    }

    private String defaultVisit(JmmNode node, String scope) {
        for (JmmNode child : node.getChildren()) {
           visit(child, scope);
        }

        return "";
    }

    @Override
    public String toString() {
        return "AnalysisTableBuilder{" +
                "symbolTable=" + symbolTable +
                ", reports=" + reports +
                '}';
    }
}
