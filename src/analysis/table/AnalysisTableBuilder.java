package analysis.table;

import java.util.List;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import report.Report;

public class AnalysisTableBuilder extends AJmmVisitor<String, String> {
    private final AnalysisTable symbolTable;
    private final List<Report> reports;

    public AnalysisTableBuilder(AnalysisTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;

        addVisit("Import", this::visitImport);
        addVisit("Class", this::visitClass);
        addVisit("ClassVar", this::visitClassVar);
        addVisit("Array", this::visitArray);

        /*
        addVisit("Method", this::visitMethod);
        addVisit("Main", this::visitMain);
        */

        setDefaultVisit(this::defaultVisit);
    }

    public String visitImport(JmmNode node, String space) {
        StringBuilder importStmt = new StringBuilder();

        for (JmmNode child : node.getChildren()) {
            importStmt.append(child.get("VALUE")).append('.');
        }

        importStmt.deleteCharAt(importStmt.length() - 1);
        this.symbolTable.addImport(importStmt.toString());
        return "";
    }

    public String visitClass(JmmNode node, String space) {
        this.symbolTable.setClassName(node.getChildren().get(0).get("VALUE"));
        for(int i = 1; i < node.getChildren().size(); i++) {
            visit(node.getChildren().get(i));
        }

        return "";
    }

    public String visitClassVar(JmmNode node, String space) {
        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);

        if(firstChild.getKind().equals("Array")) {
            this.symbolTable.addLocalVariable(this.symbolTable.getClassName(), new Symbol(new Type(visit(firstChild),true), secondChild.get("VALUE")));
        } else {
            this.symbolTable.addLocalVariable(this.symbolTable.getClassName(), new Symbol(new Type(firstChild.get("VALUE"),false), secondChild.get("VALUE")));
        }

        return "";
    }

    public String visitArray(JmmNode node, String space) {
        return node.getChildren().get(0).get("VALUE");
    }

    /*public String visitMethod(JmmNode node, String space) {
        //this.symbolTable.addMethod(new Symbol());
        return defaultVisit(node, "");
    }

    public String visitMain(JmmNode node, String space) {
        this.symbolTable.addImport(node.getAttributes().get(0));
        return defaultVisit(node, "");
    }*/

    private String defaultVisit(JmmNode node, String space) {
        for (JmmNode child : node.getChildren()) {
           visit(child, space + " ");
        }

        return "";
    }
}
