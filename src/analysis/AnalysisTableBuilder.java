package analysis;

import analysis.table.AnalysisTable;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnalysisTableBuilder extends AJmmVisitor<String, String> {
    public static final String DELIMITER = "/";
    private final AnalysisTable symbolTable = new AnalysisTable();
    private final List<Report> reports;

    public AnalysisTableBuilder(List<Report> reports) {
        this.reports = reports;

        addVisit("Import", this::visitImport);
        addVisit("ImportName", this::visitValue);
        addVisit("Class", this::visitClass);
        addVisit("Extension", this::visitExtension);
        addVisit("VarDecl", this::visitVarDecl);
        addVisit("Method", this::visitMethod);
        addVisit("Main", this::visitMain);

        setDefaultVisit(this::defaultVisit);
    }

    public String visitImport(JmmNode node, String scope) {
        List<String> imports = new ArrayList<>();

        for (JmmNode child : node.getChildren()) {
            imports.add(visit(child));
        }

        String importClass = String.join(DELIMITER, imports);
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

        return "";
    }

    public String visitClass(JmmNode node, String scope) {
        this.symbolTable.setClassName(node.getChildren().get(0).get("VALUE"));
        return defaultVisit(node, AnalysisTable.CLASS_SCOPE);
    }

    private String visitExtension(JmmNode node, String scope) {
        this.symbolTable.setSuper(node.get("VALUE"));
        return "";
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

        return "";
    }

    public String visitMethod(JmmNode node, String scope) {
        JmmNode returnType = node.getChildren().get(0);
        JmmNode name = node.getChildren().get(1);

        List<Symbol> parameters = new ArrayList<>();

        if(node.getChildren().size() >= 3 && node.getChildren().get(2).getKind().equals("MethodParameters")) {
            this.fillMethodParameters(node.getChildren().get(2), name.get("VALUE"), parameters);
        }

        Symbol method = new Symbol(
            getType(returnType),
            AnalysisTable.getMethodString(name.get("VALUE"), parameters.stream().map(Symbol::getType).collect(Collectors.toList()))
        );

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

        for (Symbol parameter : parameters) {
            this.symbolTable.addParameter(method.getName(), parameter);
            this.symbolTable.addLocalVariable(method.getName(), parameter);
        }

        return defaultVisit(node, method.getName());
    }

    public String visitMain(JmmNode node, String scope) {
        JmmNode params = node.getChildren().get(0);

        List<Symbol> parameters = new ArrayList<>();

        this.fillMethodParameters(params, AnalysisTable.MAIN_SCOPE, parameters);

        Symbol method = new Symbol(
            new Type("void", false),
            AnalysisTable.getMethodString(AnalysisTable.MAIN_SCOPE, parameters.stream().map(Symbol::getType).collect(Collectors.toList()))
        );

        if (!this.symbolTable.addMethod(method)) {
            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    "Redeclaration of method \"main\""
                )
            );
        }

        for (Symbol parameter : parameters) {
            this.symbolTable.addParameter(method.getName(), parameter);
            this.symbolTable.addLocalVariable(method.getName(), parameter);
        }

        return defaultVisit(node, method.getName());
    }

    public String visitValue(JmmNode node, String scope) {
        return node.get("VALUE");
    }

    private void fillMethodParameters(JmmNode node, String method, List<Symbol> parameters) {
        for(JmmNode child: node.getChildren()) {
            JmmNode firstChild = child.getChildren().get(0);
            JmmNode secondChild = child.getChildren().get(1);

            Symbol param = new Symbol(getType(firstChild), secondChild.get("VALUE"));

            if (parameters.contains(param)) {
                this.reports.add(
                    new Report(
                        ReportType.ERROR,
                        Stage.SEMANTIC,
                        Integer.parseInt(secondChild.get("LINE")),
                        Integer.parseInt(secondChild.get("COLUMN")),
                        "Redeclaration of parameter \"" + secondChild.get("VALUE") + "\" in function: \"" + method + "\""
                    )
                );
            } else {
                parameters.add(param);
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
