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
/*
        addVisit("MethodCall",  this::visitMethodCall);

        addVisit("Return",      this::visitReturn);
        addVisit("Not",         this::visitFirstChildren);
        addVisit("Index",       this::visitFirstChildren);
        addVisit("Size",        this::visitFirstChildren);
        addVisit("Param",       this::visitFirstChildren);
        addVisit("Important",   this::visitFirstChildren);
        addVisit("IntArray",    this::visitFirstChildren);
*/
        addVisit("Var",         this::visitVar);
        addVisit("IntegerVal",  this::visitIntegerVal);
        addVisit("Bool",        this::visitBool);
        addVisit("This",        this::visitThis);
    }

    private List<String> visitDot(JmmNode node, ScopeNSpacing scopeNSpacing) {
        //TODO: invokevirtual(this, "compFac", aux1.i32).i32;
        //TODO: arraylength($1.A.array.i32).i32;
        List<String> instructions = new ArrayList<>();

        return instructions;
    }

    private List<String> visitAllChildren(JmmNode node, ScopeNSpacing scopeNSpacing) {
        List<String> instructions = new ArrayList<>();

        int i = 0;
        StringBuilder instructionBuilder = new StringBuilder();
        List<JmmNode> children = node.getChildren();
        int numChildren = children.size();

        for(JmmNode child : children) {
            String ollirVariable = typeToOllirString(new Type(child.getChildren().get(0).get("VALUE"), false)); //TODO determine if array
            if(i == numChildren - 1){
                instructionBuilder.append(ollirVariable).append(", ");
            }
            else {
                instructionBuilder.append(ollirVariable);
            }
        }
        instructions.add(scopeNSpacing.spacing + instructionBuilder.toString());

        return instructions;
    }

    private List<String> visitVar(JmmNode node, ScopeNSpacing scopeNSpacing) {
        List<String> instructions = new ArrayList<>();
        //TODO
        instructions.add(scopeNSpacing.spacing + node.getChildren() + ";\n");

        return instructions;
    }

    private List<String> generateTwoChildren(JmmNode node, ScopeNSpacing scopeNSpacing, String expressionSign, String paramType) {
        List<String> instructions = new ArrayList<>();
        List<String> instructionsLeft = this.visit(node.getChildren().get(0), scopeNSpacing);
        List<String> instructionsRight = this.visit(node.getChildren().get(1), scopeNSpacing);

        int originalStackCounter = this.stackCounter;
        String leftVar = getInstruction(node.getChildren().get(0), scopeNSpacing, paramType, instructions, instructionsLeft);
        String rightVar = getInstruction(node.getChildren().get(1), scopeNSpacing, paramType, instructions, instructionsRight);
        this.stackCounter = originalStackCounter;

        instructions.addAll(instructionsLeft);
        instructions.addAll(instructionsRight);
        instructions.add(scopeNSpacing.spacing + leftVar + " " + expressionSign + " " + rightVar + ";\n");

        return instructions;
    }

    private List<String> visitThis(JmmNode node, ScopeNSpacing scopeNSpacing) {
        List<String> instructions = new ArrayList<>();
        instructions.add(scopeNSpacing.spacing + "this." + symbolTable.getClassName() + ";\n");

        return instructions;
    }

    private List<String> visitBool(JmmNode node, ScopeNSpacing scopeNSpacing) {
        List<String> instructions = new ArrayList<>();
        instructions.add(node.get("VALUE") + ".i32;\n");

        return instructions;
    }

    private List<String> visitIntegerVal(JmmNode node, ScopeNSpacing scopeNSpacing) {
        List<String> instructions = new ArrayList<>();
        String ollirBool = "0.bool";
        if(node.get("VALUE").equals("true")) ollirBool = "1.bool";
        instructions.add(ollirBool + ";\n");

        return instructions;
    }

    private List<String> visitObject(JmmNode node, ScopeNSpacing scopeNSpacing) {
        //TODO:
        // t0.myClass :=.myClass new(myClass).myClass;
        // invokespecial(t0.myClass,"<init>").V;
        // t0.myClass
        List<String> instructions = new ArrayList<>();
        String ollirParamType = typeToOllirString(new Type(node.getChildren().get(0).get("VALUE"), false));
        instructions.add(scopeNSpacing.spacing + "t" + this.stackCounter + "." + ollirParamType + " :=." + ollirParamType + "new(" + ollirParamType + ")." + ollirParamType + ";\n");
        instructions.add(scopeNSpacing.spacing + "invokespecial(t" + this.stackCounter + "." + ollirParamType + ",\"<init>\").V;\n");
        instructions.add(scopeNSpacing.spacing + "t" + this.stackCounter + "." + ollirParamType + ";\n");

        return instructions;
    }

    private List<String> visitArrayAccess(JmmNode node, ScopeNSpacing scopeNSpacing) {
        //TODO: $1.A[i.i32].i32;
        List<String> instructions = new ArrayList<>();
        List<String> instructionsRight = this.visit(node.getChildren().get(1), scopeNSpacing);

        int originalStackCounter = this.stackCounter;
        String leftVar = getTerminalVar(node.getChildren().get(0), scopeNSpacing.scope, scopeNSpacing.previousScope).split("\\.array")[0];
        String rightVar = getInstruction(node.getChildren().get(1), scopeNSpacing, "", instructions, instructionsRight);
        this.stackCounter = originalStackCounter;

        instructions.addAll(instructionsRight);
        instructions.add(scopeNSpacing.spacing + leftVar + "[" + rightVar + ".i32].i32;\n");

        return instructions;
    }

    private List<String> visitAssign(JmmNode node, ScopeNSpacing scopeNSpacing) {
        Type paramType = getVarType(node.getChildren().get(0), scopeNSpacing);
        String ollirParamType = typeToOllirString(paramType);

        return generateTwoChildren(node, scopeNSpacing, "=."+ollirParamType, ollirParamType);
    }

    private List<String> visitClass(JmmNode node, ScopeNSpacing _s) {
        List<String> instructions = new ArrayList<>();

        instructions.add(this.symbolTable.getClassName() + " {\n\n");

        for (Symbol field : this.symbolTable.getFields()) {
            instructions.add("\t.field private " + field.getName() + "." + typeToOllirString(field.getType()) + ";\n\n");
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
                .append(typeToOllirString(param.getType()));
        }

        methodBuilder.append(".")
            .append(typeToOllirString(getParamType(returnType)))
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
                .append(typeToOllirString(param.getType()));
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

        String conditionInstruction = cleanStringEnding(conditionInstructions.remove(conditionInstructions.size() - 1));

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

        String conditionInstruction = cleanStringEnding(conditionInstructions.remove(conditionInstructions.size() - 1));

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

    private String getInstruction(JmmNode node, ScopeNSpacing scopeNSpacing, String paramType, List<String> instructions, List<String> instructionsRight) {
        if(instructionsRight.size() == 0) {
            return getTerminalVar(node, scopeNSpacing.scope, scopeNSpacing.previousScope);
        }

        String instr = cleanStringEnding(instructionsRight.remove(instructionsRight.size() - 1));

        String var = "t" + this.stackCounter;
        instructions.add(var + "." + paramType + " :=." + paramType + " " + instr);
        this.stackCounter++;

        return var;
    }


    private String getTerminalVar(JmmNode node, String scope, String previousScope) {


        return "";
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

            parameters.add(new Symbol(getParamType(firstChild), secondChild.get("VALUE")));
        }
    }

    private Type getParamType(JmmNode node) {
        if (node.getKind().equals("Array")) {
            return new Type(node.getChildren().get(0).get("VALUE"), true);
        }

        return new Type(node.get("VALUE"), false);
    }

    private Type getVarType(JmmNode node, ScopeNSpacing scopeNSpacing) {
        JmmNode name = node;

        if(node.getNumChildren() > 0) {
            name = node.getChildren().get(0);
        }

        String varName = name.get("VALUE");

        Type varType = findTypeOfVar(this.symbolTable.getLocalVariables(scopeNSpacing.scope), varName);
        if(varType != null) return varType;

        varType = findTypeOfVar(this.symbolTable.getParameters(scopeNSpacing.scope), varName);
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

    private List<String> defaultVisit(JmmNode node, ScopeNSpacing scope) {
        List<String> instructions = new ArrayList<>();

        for (JmmNode child : node.getChildren()) {
            instructions.addAll(visit(child, scope));
        }

        return instructions;
    }

    private String cleanStringEnding(String instr) {
        return instr.replace("\t", "").replace("\n", "").replace(";", "");
    }
}
