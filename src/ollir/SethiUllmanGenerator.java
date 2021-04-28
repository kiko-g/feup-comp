package ollir;

import ollir.instruction.*;
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

public class SethiUllmanGenerator extends AJmmVisitor<SethiUllmanGenerator.Scope, List<JmmInstruction>> {
    private final SymbolTable symbolTable;
    private int stackCounter;
    private int ifElseCounter = 0;
    private int loopCounter = 0;

    protected static class Scope {
        private final String scope;
        private final String previousScope;

        public Scope(String scope, String previousScope) {
            this.scope = scope;
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

        addVisit("Add",         (node, sns) -> this.generateTwoChildren(node, sns, "+.i32", "i32"));
        addVisit("Sub",         (node, sns) -> this.generateTwoChildren(node, sns, "-.i32", "i32"));
        addVisit("Mul",         (node, sns) -> this.generateTwoChildren(node, sns, "*.i32", "i32"));
        addVisit("Div",         (node, sns) -> this.generateTwoChildren(node, sns, "/.i32", "i32"));
        addVisit("LogicalAnd",  (node, sns) -> this.generateTwoChildren(node, sns, "&&.bool", "bool"));
        addVisit("Less",        (node, sns) -> this.generateTwoChildren(node, sns, "<.bool", "i32"));
        addVisit("Assign",      this::visitAssign);
        addVisit("ArrayAccess", this::visitArrayAccess);
        addVisit("Object",      this::visitObject);

        addVisit("Params",      this::visitAllChildren);
        addVisit("Dot",         this::visitDot);

        addVisit("Var",         this::visitVar);
        addVisit("IntegerVal",  this::visitIntegerVal);
        addVisit("Bool",        this::visitBool);
        addVisit("This",        this::visitThis);
/*
        addVisit("MethodCall",  this::visitMethodCall);
        addVisit("Not",         this::visitFirstChildren);
        addVisit("Size",        this::visitFirstChildren);
        addVisit("Index",       this::visitFirstChildren);
        addVisit("Return",      this::visitReturn);

        //TODO addVisit("Param",       this::visitFirstChildren);
        //TODO addVisit("Important",   this::visitFirstChildren);
        //TODO addVisit("IntArray",    this::visitFirstChildren);
*/
    }

    private List<JmmInstruction> visitDot(JmmNode node, Scope scope) {
        //TODO: invokevirtual(this, "compFac", aux1.i32).i32;
        //TODO: arraylength($1.A.array.i32).i32;
        List<JmmInstruction> instructions = new ArrayList<>();
        //DotMethodInstruction dotMethodInstruction = new DotMethodInstruction(node);

        return instructions;
    }

    private List<JmmInstruction> visitAllChildren(JmmNode node, Scope scope) {
        List<JmmNode> children = node.getChildren();
        List<JmmInstruction> instructions = new ArrayList<>();

        for(JmmNode child : children) {
            instructions.add(new ParametersInstruction(visit(child, scope)));
        }

        return instructions;
    }

    private List<JmmInstruction> visitVar(JmmNode node, Scope scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        instructions.add(node.getChildren() + ";\n");

        return instructions;
    }

    private List<JmmInstruction> generateTwoChildren(JmmNode node, Scope scope, String expressionSign, String paramType) {
        List<JmmInstruction> instructions = new ArrayList<>();
        List<JmmInstruction> instructionsLeft = this.visit(node.getChildren().get(0), scope);
        List<JmmInstruction> instructionsRight = this.visit(node.getChildren().get(1), scope);

        int originalStackCounter = this.stackCounter;
        String leftVar = node.getChildren().get(0), scope, paramType, instructions, instructionsLeft;
        String rightVar = getInstruction(node.getChildren().get(1), scope, paramType, instructions, instructionsRight);
        this.stackCounter = originalStackCounter;

        instructions.addAll(instructionsLeft);
        instructions.addAll(instructionsRight);
        instructions.add(leftVar + " " + expressionSign + " " + rightVar + ";\n");

        return instructions;
    }

    private List<JmmInstruction> visitThis(JmmNode node, Scope scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        instructions.add("this." + symbolTable.getClassName() + ";\n");

        return instructions;
    }

    private List<JmmInstruction> visitBool(JmmNode node, Scope scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        instructions.add(node.get("VALUE") + ".i32;\n");

        return instructions;
    }

    private List<JmmInstruction> visitIntegerVal(JmmNode node, Scope scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        String ollirBool = "0.bool";
        if(node.get("VALUE").equals("true")) ollirBool = "1.bool";
        instructions.add(ollirBool + ";\n");

        return instructions;
    }

    private List<JmmInstruction> visitObject(JmmNode node, Scope scope) {
        //TODO:
        // t0.myClass :=.myClass new(myClass).myClass;
        // invokespecial(t0.myClass,"<init>").V;
        // t0.myClass
        List<JmmInstruction> instructions = new ArrayList<>();
        String ollirParamType = typeToOllirString(new Type(node.getChildren().get(0).get("VALUE"), false));
        instructions.add("t" + this.stackCounter + "." + ollirParamType + " :=." + ollirParamType + "new(" + ollirParamType + ")." + ollirParamType + ";\n");
        instructions.add("invokespecial(t" + this.stackCounter + "." + ollirParamType + ",\"<init>\").V;\n");
        instructions.add("t" + this.stackCounter + "." + ollirParamType + ";\n");

        return instructions;
    }

    private List<JmmInstruction> visitArrayAccess(JmmNode node, Scope scope) {
        //TODO: $1.A[i.i32].i32;
        List<JmmInstruction> instructions = new ArrayList<>();
        List<JmmInstruction> instructionsRight = this.visit(node.getChildren().get(1), scope);

        int originalStackCounter = this.stackCounter;
        String leftVar = getTerminalVar(node.getChildren().get(0), scope.scope, scope.previousScope).split("\\.array")[0];
        String rightVar = getInstruction(node.getChildren().get(1), scope, "", instructions, instructionsRight);
        this.stackCounter = originalStackCounter;

        instructions.addAll(instructionsRight);
        instructions.add(leftVar + "[" + rightVar + ".i32].i32;\n");

        return instructions;
    }

    private List<JmmInstruction> visitAssign(JmmNode node, Scope scope) {
        Type paramType = getVarType(node.getChildren().get(0), scope);
        String ollirParamType = typeToOllirString(paramType);

        return generateTwoChildren(node, scope, "=."+ollirParamType, ollirParamType);
    }

    private List<JmmInstruction> visitClass(JmmNode node, Scope _s) {
        List<JmmInstruction> instructions = new ArrayList<>();

        instructions.add(this.symbolTable.getClassName() + " {\n\n");

        for (Symbol field : this.symbolTable.getFields()) {
            instructions.add("\t.field private " + field.getName() + "." + typeToOllirString(field.getType()) + ";\n\n");
        }

        instructions.add("\t.construct " + this.symbolTable.getClassName() + "().V {\n" + "\t\tinvokespecial(this, \"<init>\").V;\n" + "\t}\n\n");

        instructions.addAll(this.defaultVisit(node, new Scope(AnalysisTable.CLASS_SCOPE, "\t", null)));

        instructions.add("}");

        return instructions;
    }

    private List<JmmInstruction> visitMethod(JmmNode node, Scope scopeNSpacing) {
        List<JmmInstruction> instructions = new ArrayList<>();
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
                .append(typeToOllirString(param.getType()));
        }

        methodBuilder.append(".")
            .append(typeToOllirString(getParamType(returnType)))
            .append("{\n");

        instructions.add(methodBuilder.toString());

        String scope = AnalysisTable.getMethodString(name.get("VALUE"), parameters.stream().map(Symbol::getType).collect(Collectors.toList()));

        for (JmmNode child : node.getChildren()) {
            instructions.addAll(visit(child, new Scope(scope, scopeNSpacing + "\t", scopeNSpacing.scope)));
        }

        instructions.add(scopeNSpacing.spacing + "}\n\n");

        return instructions;
    }

    private List<JmmInstruction> visitMain(JmmNode node, Scope scopeNSpacing) {
        List<JmmInstruction> instructions = new ArrayList<>();
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
                .append(typeToOllirString(param.getType()));
        }

        methodBuilder.append(").V{\n");

        instructions.add(methodBuilder.toString());

        String scope = AnalysisTable.getMethodString(AnalysisTable.MAIN_SCOPE, parameters.stream().map(Symbol::getType).collect(Collectors.toList()));

        for (JmmNode child : node.getChildren()) {
            instructions.addAll(visit(child, new Scope(scope, scopeNSpacing.spacing + "\t", scopeNSpacing.scope)));
        }

        instructions.add(scopeNSpacing.spacing + "}\n\n");

        return instructions;
    }

    private List<JmmInstruction> visitIf(JmmNode node, Scope scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        JmmNode condition = node.getChildren().get(0);
        JmmNode conditionTrue = node.getChildren().get(1);
        JmmNode conditionFalse = node.getChildren().get(2);

        int ifCounter = this.ifElseCounter;
        this.ifElseCounter++;

        List<JmmInstruction> conditionInstructions = visit(condition, scope);

        Scope conditionsScopeNSpacing = new Scope(scope.scope, "\t", scope.previousScope);
        List<JmmInstruction> conditionTrueInstructions = visit(conditionTrue, conditionsScopeNSpacing);
        List<JmmInstruction> conditionFalseInstructions = visit(conditionFalse, conditionsScopeNSpacing);

        String conditionInstruction = cleanStringEnding(conditionInstructions.remove(conditionInstructions.size() - 1));

        instructions.add("if (" + conditionInstruction + ") goto True" + ifCounter + ";\n");
        instructions.addAll(conditionFalseInstructions);
        instructions.add("goto Endif" + ifCounter + ";\n");
        instructions.add("True" + ifCounter + ":\n");
        instructions.addAll(conditionTrueInstructions);
        instructions.add("Endif" + ifCounter + ":\n");

        return instructions;
    }

    private List<JmmInstruction> visitWhile(JmmNode node, Scope scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        JmmNode condition = node.getChildren().get(0);
        JmmNode loop = node.getChildren().get(1);

        int loopCounter = this.loopCounter;
        this.loopCounter++;

        List<JmmInstruction> conditionInstructions = visit(condition, scope);

        Scope conditionsScopeNSpacing = new Scope(scope.scope, "\t", scope.previousScope);
        List<JmmInstruction> loopInstructions = visit(loop, conditionsScopeNSpacing);

        String conditionInstruction = cleanStringEnding(conditionInstructions.remove(conditionInstructions.size() - 1));

        instructions.add("While" + loopCounter + ":");
        instructions.addAll(conditionInstructions);
        instructions.add("if (" + conditionInstruction + ") goto Loop" + loopCounter + ";\n");
        instructions.add("goto EndWhile" + loopCounter + ":\n");
        instructions.add("Loop" + loopCounter + ":\n");
        instructions.addAll(loopInstructions);
        instructions.add("goto While" + loopCounter + ";\n");
        instructions.add("EndWhile" + loopCounter + ":\n");

        return instructions;
    }

    private String typeToOllirString(Type type) {
        StringBuilder builder = new StringBuilder();

        if (type.isArray()) {
            builder.append("array.");
        }

        switch (type.getName()) {
            case "int" -> builder.append("i32");
            case "boolean" -> builder.append("bool");
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


    private String getTerminalVar(JmmNode node, String scope, String previousScope) {


        return "";
    }

    private String getImport(String className) {
        List<JmmInstruction> imports = this.symbolTable.getImports();

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

            parameters.add(new Symbol(getParamType(firstChild), secondChild.get("VALUE")));
        }
    }

    private Type getParamType(JmmNode node) {
        if (node.getKind().equals("Array")) {
            return new Type(node.getChildren().get(0).get("VALUE"), true);
        }

        return new Type(node.get("VALUE"), false);
    }

    private Type getVarType(JmmNode node, Scope scope) {
        JmmNode name = node;

        if(node.getNumChildren() > 0) {
            name = node.getChildren().get(0);
        }

        String varName = name.get("VALUE");

        Type varType = findTypeOfVar(this.symbolTable.getLocalVariables(scope.scope), varName);
        if(varType != null) return varType;

        varType = findTypeOfVar(this.symbolTable.getParameters(scope.scope), varName);
        if(varType != null) return varType;

        varType = findTypeOfVar(this.symbolTable.getFields(), varName);
        return varType;
    }

    private Type findTypeOfVar(List<Symbol> symbols, String varName) {
        for(Symbol symbol : symbols) {
            if(symbol.getName().equals(varName)) {
                return symbol.getType();
            }
        }

        return null;
    }

    private List<JmmInstruction> defaultVisit(JmmNode node, Scope scope) {
        List<JmmInstruction> instructions = new ArrayList<>();

        for (JmmNode child : node.getChildren()) {
            instructions.addAll(visit(child, scope));
        }

        return instructions;
    }

    private String cleanStringEnding(String instr) {
        return instr.replace("\t", "").replace("\n", "").replace(";", "");
    }
}
