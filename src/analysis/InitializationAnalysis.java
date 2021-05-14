package analysis;

import analysis.table.AnalysisTable;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import report.StyleReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InitializationAnalysis extends AJmmVisitor<InitializationAnalysis.ScopeNAssign, Symbol> {
    private final AnalysisTable symbolTable;
    private final List<Report> reports;

    private final Map<String, List<Symbol>> initialized = new HashMap<>();

    protected static class ScopeNAssign {
        private final String scope;
        private final boolean isAssignment;

        public ScopeNAssign(String scope, boolean isAssignment) {
            this.scope = scope;
            this.isAssignment = isAssignment;
        }
    }

    public InitializationAnalysis(AnalysisTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;

        addVisit("Method", this::visitMethod);
        addVisit("Main", this::visitMain);
        addVisit("Assign", this::visitAssign);
        addVisit("Var", this::visitVar);

        setDefaultVisit(this::defaultVisit);
    }

    public List<Report> getReports() {
        return reports;
    }

    private Symbol visitAssign(JmmNode node, ScopeNAssign scopeNAssign) {
        this.visit(node.getChildren().get(1), scopeNAssign);

        Symbol leftSymbol = this.visit(node.getChildren().get(0), new ScopeNAssign(scopeNAssign.scope, true));

        if (leftSymbol != null) {
            this.initialized.getOrDefault(scopeNAssign.scope, new ArrayList<>()).add(leftSymbol);
        }

        return null;
    }

    private Symbol visitVar(JmmNode node, ScopeNAssign scopeNAssign) {
        for (Symbol symbol : this.symbolTable.getParameters(scopeNAssign.scope)) {
            if (symbol.getName().equals(node.get("VALUE"))) {
                return symbol;
            }
        }

        for (Symbol symbol : this.symbolTable.getLocalVariables(scopeNAssign.scope)) {
            if (symbol.getName().equals(node.get("VALUE"))) {
                if(!scopeNAssign.isAssignment) {
                    checkInitialization(node, scopeNAssign.scope, symbol);
                }
                return symbol;
            }
        }

        for (Symbol symbol : this.symbolTable.getFields()) {
            if (symbol.getName().equals(node.get("VALUE"))) {
                return symbol;
            }
        }

        return new Symbol(new Type("void", false), "null");
    }

    private Symbol visitMethod(JmmNode node, ScopeNAssign _scope) {
        List<Symbol> methodSymbols = new ASTMethodGenerator().visit(node);
        Symbol methodSymbol = methodSymbols.remove(0);

        String methodScope = AnalysisTable.getMethodString(methodSymbol.getName(), methodSymbols.stream().map(Symbol::getType).collect(Collectors.toList()));

        this.initialized.put(methodScope, new ArrayList<>());

        for (int i = 2; i < node.getNumChildren(); i++){
            if (node.getChildren().get(i).getKind().equals("MethodParameters")) {
                continue;
            }

            visit(node.getChildren().get(i), new ScopeNAssign(methodScope, false));
        }

        return null;
    }

    private Symbol visitMain(JmmNode node, ScopeNAssign _scope) {
        List<Symbol> methodSymbols = new ASTMethodGenerator().visit(node);
        Symbol methodSymbol = methodSymbols.remove(0);

        String methodScope = AnalysisTable.getMethodString(methodSymbol.getName(), methodSymbols.stream().map(Symbol::getType).collect(Collectors.toList()));
        this.initialized.put(methodScope, new ArrayList<>());

        for (int i = 1; i < node.getNumChildren(); i++){
            if (node.getChildren().get(i).getKind().equals("MethodParameters")) {
                continue;
            }

            visit(node.getChildren().get(i), new ScopeNAssign(methodScope, false));
        }

        return null;
    }

    private Symbol defaultVisit(JmmNode node, ScopeNAssign scopeNAssign) {
        for (JmmNode child : node.getChildren()) {
            this.visit(child, scopeNAssign);
        }

        return null;
    }

    private void checkInitialization(JmmNode node, String scope, Symbol variable) {
        if (initialized.getOrDefault(scope, new ArrayList<>()).contains(variable)) {
            return;
        }

        reports.add(new StyleReport(
                ReportType.WARNING,
                Stage.SEMANTIC,
                Integer.parseInt(node.get("LINE")),
                Integer.parseInt(node.get("COLUMN")),
                "Variable \"" + variable.getName() + "\" may have not been initialized!")
        );
    }
}
