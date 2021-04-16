package ollir;

import analysis.AnalysisTableBuilder;
import analysis.table.AnalysisTable;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SethiUllmanGenerator extends AJmmVisitor<SethiUllmanGenerator.ScopeNSpacing, List<String>> {
    private final SymbolTable symbolTable;
    private int stackCounter;
    private int ifElseCounter = 0;
    private int loopCounter = 0;

    protected static class ScopeNSpacing {
        private final String scope;
        private final String previousScope;
        private final String spacing;

        public ScopeNSpacing(String scope, String spacing, String previousScope) {
            this.scope = scope;
            this.spacing = spacing;
            this.previousScope = previousScope;
        }
    }

    public SethiUllmanGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;

        addVisit("Class",       this::visitClass);
        addVisit("Method",      this::visitMethod);
        addVisit("Main",        this::visitMain);

        addVisit("If",          this::visitIf);
        addVisit("While",       this::visitWhile);

        addVisit("Add",         this::visitAdd);
/*
        addVisit("Params",      this::visitAllChildren);

        addVisit("Assign",      this::visitTwoChildren);
        addVisit("Dot",         this::visitTwoChildren);
        addVisit("Sub",         this::visitTwoChildren);
        addVisit("Mul",         this::visitTwoChildren);
        addVisit("Div",         this::visitTwoChildren);
        addVisit("LogicalAnd",  this::visitTwoChildren);
        addVisit("Less",        this::visitTwoChildren);
        addVisit("ArrayAccess", this::visitTwoChildren);

        addVisit("MethodCall",  this::visitMethodCall);

        addVisit("Return",      this::visitReturn);
        addVisit("Not",         this::visitFirstChildren);
        addVisit("Index",       this::visitFirstChildren);
        addVisit("Size",        this::visitFirstChildren);
        addVisit("Param",       this::visitFirstChildren);
        addVisit("Important",   this::visitFirstChildren);
        addVisit("New",         this::visitFirstChildren);
        addVisit("IntArray",    this::visitFirstChildren);

        addVisit("Object",      this::visitTerminal);
        addVisit("Var",         this::visitTerminal);
        addVisit("IntegerVal",  this::visitTerminal);
        addVisit("Bool",        this::visitTerminal);
        addVisit("Length",      this::visitTerminal);
        addVisit("This",        this::visitTerminal);
*/

    }

    private List<String> visitAdd(JmmNode node, ScopeNSpacing scopeNSpacing) {
        List<String> instructions = new ArrayList<>();
        List<String> instructionsLeft = this.visit(node.getChildren().get(0), scopeNSpacing);
        List<String> instructionsRight = this.visit(node.getChildren().get(1), scopeNSpacing);

        String leftVar, rightVar;

        if (instructionsRight.size() == 0) {
            rightVar = getTerminalVar(node.getChildren().get(1), scopeNSpacing.scope, scopeNSpacing.previousScope);
        } else {
            rightVar = "t" + this.stackCounter + ".i32";
            this.stackCounter--;
        }
        if (instructionsLeft.size() == 0) {
            leftVar = getTerminalVar(node.getChildren().get(0), scopeNSpacing.scope, scopeNSpacing.previousScope);
        } else {
            leftVar = "t" + this.stackCounter + ".i32";
            this.stackCounter--;
        }

        instructions.addAll(instructionsLeft);
        instructions.addAll(instructionsRight);

        this.stackCounter++;

        instructions.add(scopeNSpacing.spacing + "t" + this.stackCounter + ".i32 := " + leftVar + " +.i32 " + rightVar + ";\n");

        return instructions;
    }

    private String getTerminalVar(JmmNode node, String scope, String previousScope) {


        return "";
    }

    private List<String> visitClass(JmmNode node, ScopeNSpacing _s) {
        List<String> instructions = new ArrayList<>();

        instructions.add(this.symbolTable.getClassName() + " {\n\n");

        for (Symbol field : this.symbolTable.getFields()) {
            instructions.add("\t.field private " + field.getName() + "." + typeToString(field.getType()) + ";\n\n");
        }

        instructions.add("\t.construct " + this.symbolTable.getClassName() + "().V {\n" + "\t\tinvokespecial(this, \"<init>\").V;\n" + "\t}\n\n");

        instructions.addAll(this.defaultVisit(node, new ScopeNSpacing(AnalysisTable.CLASS_SCOPE, "\t", null)));

        instructions.add("}");

        return instructions;
    }

    private List<String> visitMethod(JmmNode node, ScopeNSpacing scopeNSpacing) {
        List<String> instructions = new ArrayList<>();
        this.stackCounter = 0;

        JmmNode returnType = node.getChildren().get(0);
        JmmNode name = node.getChildren().get(1);

        List<Symbol> parameters = new ArrayList<>();

        if(node.getChildren().size() >= 3 && node.getChildren().get(2).getKind().equals("MethodParameters")) {
            this.fillMethodParameters(node.getChildren().get(2), parameters);
        }

        StringBuilder methodBuilder = new StringBuilder();
        methodBuilder.append(scopeNSpacing.spacing)
            .append(".method public ")
            .append(name.get("VALUE"))
            .append("(");

        for (Symbol param : parameters) {
            methodBuilder.append(param.getName())
                .append(".")
                .append(typeToString(param.getType()));
        }

        methodBuilder.append(".")
            .append(typeToString(getType(returnType)))
            .append("{\n");

        instructions.add(methodBuilder.toString());

        String scope = AnalysisTable.getMethodString(name.get("VALUE"), parameters.stream().map(Symbol::getType).collect(Collectors.toList()));

        for (JmmNode child : node.getChildren()) {
            instructions.addAll(visit(child, new ScopeNSpacing(scope, scopeNSpacing + "\t", scopeNSpacing.scope)));
        }

        instructions.add(scopeNSpacing.spacing + "}\n\n");

        return instructions;
    }

    private List<String> visitMain(JmmNode node, ScopeNSpacing scopeNSpacing) {
        List<String> instructions = new ArrayList<>();
        this.stackCounter = 0;

        JmmNode params = node.getChildren().get(0);

        List<Symbol> parameters = new ArrayList<>();

        this.fillMethodParameters(params, parameters);

        StringBuilder methodBuilder = new StringBuilder();
        methodBuilder.append(scopeNSpacing.spacing)
            .append(".method public static ")
            .append(AnalysisTable.MAIN_SCOPE)
            .append("(");

        for (Symbol param : parameters) {
            methodBuilder.append(param.getName())
                .append(".")
                .append(typeToString(param.getType()));
        }

        methodBuilder.append(").V{\n");

        instructions.add(methodBuilder.toString());

        String scope = AnalysisTable.getMethodString(AnalysisTable.MAIN_SCOPE, parameters.stream().map(Symbol::getType).collect(Collectors.toList()));

        for (JmmNode child : node.getChildren()) {
            instructions.addAll(visit(child, new ScopeNSpacing(scope, scopeNSpacing.spacing + "\t", scopeNSpacing.scope)));
        }

        instructions.add(scopeNSpacing.spacing + "}\n\n");

        return instructions;
    }

    private List<String> visitIf(JmmNode node, ScopeNSpacing scopeNSpacing) {
        List<String> instructions = new ArrayList<>();
        JmmNode condition = node.getChildren().get(0);
        JmmNode conditionTrue = node.getChildren().get(1);
        JmmNode conditionFalse = node.getChildren().get(2);

        int ifCounter = this.ifElseCounter;
        this.ifElseCounter++;

        List<String> conditionInstructions = visit(condition, scopeNSpacing);

        ScopeNSpacing conditionsScopeNSpacing = new ScopeNSpacing(scopeNSpacing.scope, scopeNSpacing.spacing + "\t", scopeNSpacing.previousScope);
        List<String> conditionTrueInstructions = visit(conditionTrue, conditionsScopeNSpacing);
        List<String> conditionFalseInstructions = visit(conditionFalse, conditionsScopeNSpacing);

        String conditionInstruction = conditionInstructions.remove(conditionInstructions.size() - 1);
        conditionInstruction = conditionInstruction.replace("\t", "");
        conditionInstruction = conditionInstruction.replace("\n", "");

        instructions.add(scopeNSpacing.spacing + "if (" + conditionInstruction + ") goto True" + ifCounter + ";\n");
        instructions.addAll(conditionFalseInstructions);
        instructions.add(scopeNSpacing.spacing + "goto Endif" + ifCounter + ";\n");
        instructions.add(scopeNSpacing.spacing + "True" + ifCounter + ":\n");
        instructions.addAll(conditionTrueInstructions);
        instructions.add(scopeNSpacing.spacing + "Endif" + ifCounter + ":\n");

        return instructions;
    }

    private List<String> visitWhile(JmmNode node, ScopeNSpacing scopeNSpacing) {
        List<String> instructions = new ArrayList<>();
        JmmNode condition = node.getChildren().get(0);
        JmmNode loop = node.getChildren().get(1);

        int loopCounter = this.loopCounter;
        this.loopCounter++;

        List<String> conditionInstructions = visit(condition, scopeNSpacing);

        ScopeNSpacing conditionsScopeNSpacing = new ScopeNSpacing(scopeNSpacing.scope, scopeNSpacing.spacing + "\t", scopeNSpacing.previousScope);
        List<String> loopInstructions = visit(loop, conditionsScopeNSpacing);

        String conditionInstruction = conditionInstructions.remove(conditionInstructions.size() - 1);
        conditionInstruction = conditionInstruction.replace("\t", "");
        conditionInstruction = conditionInstruction.replace("\n", "");

        instructions.add(scopeNSpacing.spacing + "While" + loopCounter + ":");
        instructions.addAll(conditionInstructions);
        instructions.add(scopeNSpacing.spacing + "if (" + conditionInstruction + ") goto Loop" + loopCounter + ";\n");
        instructions.add(scopeNSpacing.spacing + "goto EndWhile" + loopCounter + ":\n");
        instructions.add(scopeNSpacing.spacing + "Loop" + loopCounter + ":\n");
        instructions.addAll(loopInstructions);
        instructions.add(scopeNSpacing.spacing + "goto While" + loopCounter + ";\n");
        instructions.add(scopeNSpacing.spacing + "EndWhile" + loopCounter + ":\n");

        return instructions;
    }

    private String typeToString(Type type) {
        StringBuilder builder = new StringBuilder();

        if (type.isArray()) {
            builder.append("array.");
        }

        switch (type.getName()) {
            case "int" -> builder.append("i32");
            case "boolean" -> builder.append("boolean");
            default -> {
                if (type.getName().equals(this.symbolTable.getClassName())) {
                    builder.append(this.symbolTable.getClassName());
                } else {
                    builder.append(this.getImport(type.getName()));
                }
            }
        }

        return builder.toString();
    }

    private String getImport(String className) {
        List<String> imports = this.symbolTable.getImports();

        for (String importStatement : imports) {
            if(this.getImportName(importStatement).equals(className)) {
                return importStatement;
            }
        }

        return null;
    }

    private String getImportName(String importStatement) {
        int delimiterIndex = importStatement.lastIndexOf(AnalysisTableBuilder.DELIMITER);

        if (delimiterIndex == -1) {
            return importStatement;
        }

        return importStatement.substring(delimiterIndex + 1);
    }

    private void fillMethodParameters(JmmNode node, List<Symbol> parameters) {
        for(JmmNode child: node.getChildren()) {
            JmmNode firstChild = child.getChildren().get(0);
            JmmNode secondChild = child.getChildren().get(1);

            parameters.add(new Symbol(getType(firstChild), secondChild.get("VALUE")));
        }
    }

    private Type getType(JmmNode node) {
        if (node.getKind().equals("Array")) {
            return new Type(node.getChildren().get(0).get("VALUE"), true);
        }

        return new Type(node.get("VALUE"), false);
    }

    private List<String> defaultVisit(JmmNode node, ScopeNSpacing scope) {
        List<String> instructions = new ArrayList<>();

        for (JmmNode child : node.getChildren()) {
            instructions.addAll(visit(child, scope));
        }

        return instructions;
    }

}
