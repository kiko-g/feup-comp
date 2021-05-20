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
import java.util.List;
import java.util.stream.Collectors;

public class AnalysisTableBuilder extends AJmmVisitor<String, String> {
    public static final String DELIMITER = "/";
    private final AnalysisTable symbolTable = new AnalysisTable();
    private final List<Report> reports;
    private final List<NameChange> varChanges = new ArrayList<>();
    private final List<NameChange> fieldChanges = new ArrayList<>();

    protected static class JmmNodeSymbol {
        private final JmmNode node;
        private final Symbol symbol;

        public JmmNodeSymbol(JmmNode node, Symbol symbol) {
            this.node = node;
            this.symbol = symbol;
        }
    }

    protected static class NameChange {
        private final String original;
        private final String name;

        public NameChange(String original, String name) {
            this.original = original;
            this.name = name;
        }
    }

    public AnalysisTableBuilder(List<Report> reports) {
        this.reports = reports;

        addVisit("Import", this::visitImport);
        addVisit("ImportName", this::visitValue);
        addVisit("Class", this::visitClass);
        addVisit("Extension", this::visitExtension);
        addVisit("VarDecl", this::visitVarDecl);
        addVisit("Method", this::visitMethod);
        addVisit("Main", this::visitMain);
        addVisit("Var", this::visitVar);

        setDefaultVisit(this::defaultVisit);
    }

    private String visitVar(JmmNode node, String scope) {
        for (NameChange nameChange : varChanges) {
            if (nameChange.original.equals(node.get("VALUE"))) {
                node.put("ORIGINAL_VALUE", nameChange.original);
                node.put("VALUE", nameChange.name);
                return "";
            }
        }

        for (NameChange nameChange : fieldChanges) {
            if (nameChange.original.equals(node.get("VALUE"))) {
                node.put("ORIGINAL_VALUE", nameChange.original);
                node.put("VALUE", nameChange.name);
                return "";
            }
        }

        return "";
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

        String varName = dealWithReservedKeywordVar(secondChild.get("VALUE"), scope);
        secondChild.put("ORIGINAL_VALUE", secondChild.get("VALUE"));
        secondChild.put("VALUE", varName);

        if (scope.equals(AnalysisTable.CLASS_SCOPE)) {
            fieldChanges.add(new NameChange(secondChild.get("ORIGINAL_VALUE"), varName));
        } else {
            varChanges.add(new NameChange(secondChild.get("ORIGINAL_VALUE"), varName));
        }

        if (!this.symbolTable.addLocalVariable(scope, new Symbol(getType(firstChild), varName))) {
            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(secondChild.get("LINE")),
                    Integer.parseInt(secondChild.get("COLUMN")),
                    "Redeclaration of variable \"" + secondChild.get("ORIGINAL_VALUE") + "\""
                )
            );
        }

        return "";
    }

    public String visitMethod(JmmNode node, String scope) {
        JmmNode returnType = node.getChildren().get(0);
        JmmNode name = node.getChildren().get(1);

        varChanges.clear();
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
                new StyleReport(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    "Redeclaration of method \"main\""
                )
            );
        }

        for (Symbol parameter : parameters) {
            this.symbolTable.addParameter(method.getName(), parameter);
        }

        return defaultVisit(node, method.getName());
    }

    public String visitValue(JmmNode node, String scope) {
        return node.get("VALUE");
    }

    private void fillMethodParameters(JmmNode node, String method, List<Symbol> parameters) {
        List<JmmNodeSymbol> foundParameters = new ArrayList<>();

        for(JmmNode child: node.getChildren()) {
            JmmNode firstChild = child.getChildren().get(0);
            JmmNode secondChild = child.getChildren().get(1);

            Symbol param = new Symbol(getType(firstChild), secondChild.get("VALUE"));
            foundParameters.add(new JmmNodeSymbol(secondChild, param));
        }

        for (int i = 0; i < foundParameters.size(); i++) {
            JmmNodeSymbol param = foundParameters.get(i);

            String paramName = dealWithReservedKeywordVar(param.symbol.getName(), i, foundParameters);
            Symbol paramSymbol = new Symbol(param.symbol.getType(), paramName);

            param.node.put("ORIGINAL_VALUE", param.node.get("VALUE"));
            param.node.put("VALUE", paramName);
            varChanges.add(new NameChange(param.node.get("ORIGINAL_VALUE"), paramName));

            if (parameters.contains(paramSymbol)) {
                this.reports.add(
                    new Report(
                        ReportType.ERROR,
                        Stage.SEMANTIC,
                        Integer.parseInt(param.node.get("LINE")),
                        Integer.parseInt(param.node.get("COLUMN")),
                        "Redeclaration of parameter \"" + param.node.get("ORIGINAL_VALUE") + "\" in function: \"" + method + "\""
                    )
                );
            } else {
                parameters.add(paramSymbol);
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

    private String dealWithReservedKeywordVar(String name, String scope) {
        String finalVarName = name.replaceAll("\\$", "");
        boolean canConflictWithVars = false;

        while(nameHasConflict(name, finalVarName, canConflictWithVars, scope)) {
            finalVarName = "_" + finalVarName;
            canConflictWithVars = true;
        }

        return finalVarName;
    }

    private boolean nameHasConflict(String originalName, String varName, boolean canConflictWithVars, String scope) {
        List<Symbol> parameters = this.symbolTable.getParameters(scope);
        List<Symbol> localVariables = this.symbolTable.getLocalVariables(scope);
        List<Symbol> fields = this.symbolTable.getFields();

        if(canConflictWithVars && varName.equals(originalName)) {
            for(Symbol param : parameters) {
                if (param.getName().equals(varName)) {
                    return true;
                }
            }

            for(Symbol localVar : localVariables) {
                if (localVar.getName().equals(varName)) {
                    return true;
                }
            }

            for(Symbol field : fields) {
                if (field.getName().equals(varName)) {
                    return true;
                }
            }
        }

        return checkReservedWord(varName);
    }

    private boolean checkReservedWord(String varName) {
        return varName.startsWith("array") || varName.startsWith("i32") || varName.startsWith("ret") ||
            varName.startsWith("bool") || varName.startsWith("field") || varName.startsWith("method") ||
            varName.startsWith("void");
    }

    private String dealWithReservedKeywordVar(String name, int currentIndex, List<JmmNodeSymbol> params) {
        String finalVarName = name.replaceAll("\\$", "");
        boolean canConflictWithVars = false;

        while(nameHasConflict(finalVarName, currentIndex, canConflictWithVars, params)) {
            finalVarName = "_" + finalVarName;
            canConflictWithVars = true;
        }

        return finalVarName;
    }

    private boolean nameHasConflict(String varName, int currentIndex, boolean canConflictWithVars, List<JmmNodeSymbol> params) {
        if (canConflictWithVars) {
            for (int i = 0; i < params.size(); i++) {
                if (currentIndex == i) {
                    continue;
                }

                if (params.get(i).symbol.getName().equals(varName)) {
                    return true;
                }
            }
        }

        return checkReservedWord(varName);
    }

    public AnalysisTable getSymbolTable() {
        return this.symbolTable;
    }

    public List<Report> getReports() {
        return this.reports;
    }
}
