package analysis;

import java.util.List;

import analysis.table.AnalysisTable;
import analysis.table.Symbol;
import analysis.table.Type;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import report.Report;
import report.ReportType;
import report.Stage;

public class AnalysisTableBuilder extends AJmmVisitor<String, String> {
    private final AnalysisTable symbolTable = new AnalysisTable();
    private final List<Report> reports;

    public AnalysisTableBuilder(List<Report> reports) { ;
        this.reports = reports;

        addVisit("Import", this::visitImport);
        addVisit("ImportName", this::visitValue);
        addVisit("Class", this::visitClass);
        addVisit("VarDecl", this::visitVarDecl);
        addVisit("Method", this::visitMethod);
        addVisit("Main", this::visitMain);

        setDefaultVisit(this::defaultVisit);
    }

    public String visitImport(JmmNode node, String scope) {
        String importClass = "";

        for (JmmNode child : node.getChildren()) {
            importClass = visit(child);
        }

        if (!this.symbolTable.addImport(importClass)) {
            JmmNode lastChild = node.getChildren().get(node.getNumChildren() - 1);

            this.reports.add(
                new Report(
                    ReportType.WARNING,
                    Stage.SEMANTIC,
                    Integer.parseInt(lastChild.get("LINE")),
                    Integer.parseInt(lastChild.get("COLUMN")),
                    "Duplicated import " + importClass + "\""
                )
            );
        }

        return defaultVisit(node, "");
    }

    public String visitClass(JmmNode node, String scope) {
        this.symbolTable.setClassName(node.getChildren().get(0).get("VALUE"));
        return defaultVisit(node, AnalysisTable.CLASS_SCOPE);
    }

    public String visitVarDecl(JmmNode node, String scope) {
        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);

        if (!this.symbolTable.addLocalVariable(scope, new Symbol(getType(firstChild), secondChild.get("VALUE")))) {
            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(secondChild.get("LINE")),
                    Integer.parseInt(secondChild.get("COLUMN")),
                    "Redeclaration of variable \"" + secondChild.get("VALUE") + "\""
                )
            );
        }

        return defaultVisit(node, scope);
    }

    public String visitMethod(JmmNode node, String scope) {
        JmmNode returnType = node.getChildren().get(0);
        JmmNode name = node.getChildren().get(1);

        Symbol method = new Symbol(getType(returnType), name.get("VALUE"));

        if (!this.symbolTable.addMethod(method)) {
            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(name.get("LINE")),
                    Integer.parseInt(name.get("COLUMN")),
                    "Redeclaration of method \"" + name.get("VALUE") + "\""
                )
            );
        }

        if(node.getChildren().size() >= 3 && node.getChildren().get(2).getKind().equals("MethodParameters")) {
            this.fillMethodParameters(node.getChildren().get(2), method);
        }
        
        return defaultVisit(node, method.getName());
    }

    public String visitMain(JmmNode node, String scope) {
        JmmNode params = node.getChildren().get(0);
        Symbol method = new Symbol(new Type("void", false), AnalysisTable.MAIN_SCOPE);

        if (!this.symbolTable.addMethod(method)) {
            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    "Redeclaration of method \"main\""
                )
            );

            return scope;
        }

        this.fillMethodParameters(params, method);

        return defaultVisit(node, method.getName());
    }

    public String visitValue(JmmNode node, String scope) {
        return node.get("VALUE");
    }

    private void fillMethodParameters(JmmNode node, Symbol method) {
        for(JmmNode child: node.getChildren()) {
            JmmNode firstChild = child.getChildren().get(0);
            JmmNode secondChild = child.getChildren().get(1);

            Symbol param = new Symbol(getType(firstChild), secondChild.get("VALUE"));

            if (!this.symbolTable.addParameter(method, param)) {
                this.reports.add(
                    new Report(
                        ReportType.ERROR,
                        Stage.SEMANTIC,
                        Integer.parseInt(secondChild.get("LINE")),
                        Integer.parseInt(secondChild.get("COLUMN")),
                        "Redeclaration of parameter \"" + secondChild.get("VALUE") + "\" in function: \"" + method.getName() + "\""
                    )
                );
            } else {
                this.symbolTable.addLocalVariable(method.getName(), param);
            }
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

    public AnalysisTable getSymbolTable() {
        return this.symbolTable;
    }

    public List<Report> getReports() {
        return this.reports;
    }
}
