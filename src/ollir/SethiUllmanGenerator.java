package ollir;

import ollir.instruction.*;
import analysis.AnalysisTableBuilder;
import analysis.table.AnalysisTable;
import ollir.instruction.complex.*;
import ollir.instruction.complex.binary.*;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SethiUllmanGenerator extends AJmmVisitor<String, List<JmmInstruction>> {
    private final SymbolTable symbolTable;

    public SethiUllmanGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;

        addVisit("Class",       this::visitClass);
        addVisit("Method",      this::visitMethod);
        addVisit("Main",        this::visitMain);

        addVisit("If",          this::visitIf);
        addVisit("While",       this::visitWhile);

        addVisit("Add",         (node, sns) -> this.generateTwoChildren(node, sns, new Operation(OperationType.ADD, new Type("int", false))));
        addVisit("Sub",         (node, sns) -> this.generateTwoChildren(node, sns, new Operation(OperationType.SUB, new Type("int", false))));
        addVisit("Mul",         (node, sns) -> this.generateTwoChildren(node, sns, new Operation(OperationType.MUL, new Type("int", false))));
        addVisit("Div",         (node, sns) -> this.generateTwoChildren(node, sns, new Operation(OperationType.DIV, new Type("int", false))));
        addVisit("LogicalAnd",  (node, sns) -> this.generateTwoChildren(node, sns, new Operation(OperationType.AND, new Type("bool", false))));
        addVisit("Less",        (node, sns) -> this.generateTwoChildren(node, sns, new Operation(OperationType.LESS_THAN, new Type("int", false))));
        addVisit("Assign",      this::visitAssign);
        addVisit("ArrayAccess", this::visitArrayAccess);
        addVisit("Object",      this::visitObject);
        addVisit("IntArray",    this::visitIntArray);
        addVisit("Not",         this::visitNot);

        addVisit("Params",      this::visitAllChildren);
        addVisit("Dot",         this::visitDot);

        addVisit("Var",         this::visitVar);
        addVisit("IntegerVal",  this::visitIntegerVal);
        addVisit("Bool",        this::visitBool);
        addVisit("This",        this::visitThis);
        addVisit("Return",      this::visitReturn);

        setDefaultVisit(this::defaultVisit);
    }

    private List<JmmInstruction> visitIntArray(JmmNode node, String scope) {
        List<JmmInstruction> sizeInstructions = this.visit(node.getChildren().get(0), scope);

        JmmInstruction sizeVar = getTerminalInstruction(sizeInstructions, sizeInstructions.get(sizeInstructions.size() - 1));

        List<JmmInstruction> instructions = new ArrayList<>(sizeInstructions);
        instructions.add(new NewArrayInstruction(sizeVar));

        return instructions;
    }

    private List<JmmInstruction> visitNot(JmmNode node, String scope) {
        List<JmmInstruction> notInstructions = this.visit(node.getChildren().get(0), scope);

        JmmInstruction notVar = getTerminalInstruction(notInstructions, notInstructions.get(notInstructions.size() - 1));

        List<JmmInstruction> instructions = new ArrayList<>(notInstructions);
        instructions.add(new NotInstruction(notVar));

        return instructions;
    }

    private List<JmmInstruction> visitReturn(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>(visit(node.getChildren().get(0), scope));
        JmmInstruction terminalVar = getTerminalInstruction(instructions, instructions.get(instructions.size() - 1));

        instructions.add(new ReturnInstruction(terminalVar, this.symbolTable.getReturnType(scope)));

        return instructions;
    }

    private List<JmmInstruction> visitDot(JmmNode node, String scope) {
        JmmNode leftOperand = node.getChildren().get(0);
        JmmNode rightOperand = node.getChildren().get(1);
        List<JmmInstruction> leftInstructions = visit(leftOperand, scope);
        JmmInstruction leftInstruction;

        List<JmmInstruction> instructions = new ArrayList<>();

        if (leftInstructions.size() != 0) {
            leftInstruction = getTerminalInstruction(leftInstructions, leftInstructions.get(leftInstructions.size() - 1));

            instructions.addAll(leftInstructions);

            if (rightOperand.getKind().equals("Length")) {
                instructions.add(new DotLengthInstruction(leftInstruction));
                return instructions;
            }

            String methodName = rightOperand.getChildren().get(0).get("VALUE");
            Type returnType = OllirUtils.ollirToType(rightOperand.get("RETURN"));

            List<JmmInstruction> paramTerminals = getParamInstructions(scope, rightOperand.getChildren().get(1), instructions);

            instructions.add(new DotMethodInstruction(leftInstruction, methodName, paramTerminals, returnType));
        } else {
            String methodName = rightOperand.getChildren().get(0).get("VALUE");
            Type returnType = OllirUtils.ollirToType(rightOperand.get("RETURN"));

            List<JmmInstruction> paramTerminals = getParamInstructions(scope, rightOperand.getChildren().get(1), instructions);

            instructions.add(new DotStaticMethodInstruction(leftOperand.get("VALUE"), methodName, paramTerminals, returnType));
        }

        return instructions;
    }

    private List<JmmInstruction> getParamInstructions(String scope, JmmNode paramNode, List<JmmInstruction> instructions) {
        List<JmmInstruction> paramTerminals = new ArrayList<>();

        for (JmmNode param : paramNode.getChildren()) {
            List<JmmInstruction> paramInstructions = visit(param, scope);
            paramTerminals.add(getTerminalInstruction(paramInstructions, paramInstructions.get(paramInstructions.size() - 1)));
            instructions.addAll(paramInstructions);
        }

        return paramTerminals;
    }

    private List<JmmInstruction> visitAllChildren(JmmNode node, String scope) {
        List<JmmNode> children = node.getChildren();
        List<JmmInstruction> instructions = new ArrayList<>();

        for(JmmNode child : children) {
            instructions.add(new ParametersInstruction(visit(child, scope)));
        }

        return instructions;
    }

    private List<JmmInstruction> visitVar(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>();

        List<Symbol> parameters = this.symbolTable.getParameters(scope);
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i).getName().equals(node.get("VALUE"))) {
                Symbol param = new Symbol(parameters.get(i).getType(), "$" + i + "." + parameters.get(i).getName());
                instructions.add(new TerminalInstruction(param));
                return instructions;
            }
        }

        for (Symbol symbol : symbolTable.getLocalVariables(scope)) {
            if (symbol.getName().equals(node.get("VALUE"))) {
                instructions.add(new TerminalInstruction(symbol));
                return instructions;
            }
        }

        for (Symbol symbol : symbolTable.getFields()) {
            if (symbol.getName().equals(node.get("VALUE"))) {
                instructions.add(new FieldInstruction(symbol));
                return instructions;
            }
        }

        return instructions;
    }

    private List<JmmInstruction> generateTwoChildren(JmmNode node, String scope, Operation operation) {
        List<JmmInstruction> instructions = new ArrayList<>();
        List<JmmInstruction> instructionsLeft = this.visit(node.getChildren().get(0), scope);
        List<JmmInstruction> instructionsRight = this.visit(node.getChildren().get(1), scope);

        JmmInstruction leftVar = getTerminalInstruction(instructionsLeft, instructionsLeft.get(instructionsLeft.size() - 1));
        JmmInstruction rightVar = getTerminalInstruction(instructionsRight, instructionsRight.get(instructionsRight.size() - 1));

        instructions.addAll(instructionsLeft);
        instructions.addAll(instructionsRight);
        instructions.add(new BinaryOperationInstruction(leftVar, rightVar, operation));

        return instructions;
    }

    private List<JmmInstruction> visitThis(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        instructions.add(new TerminalInstruction(new Symbol(new Type("", false), "this")));

        return instructions;
    }

    private List<JmmInstruction> visitBool(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        instructions.add(new TerminalInstruction(new Symbol(new Type("bool", false), node.get("VALUE"))));

        return instructions;
    }

    private List<JmmInstruction> visitIntegerVal(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        instructions.add(new TerminalInstruction(new Symbol(new Type("int", false), node.get("VALUE"))));

        return instructions;
    }

    private List<JmmInstruction> visitObject(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        String className = node.getChildren().get(0).get("VALUE");

        if (className.equals(symbolTable.getClassName())) {
            instructions.add(new NewObjectInstruction(className));
        } else {
            instructions.add(new NewObjectInstruction(getImport(className)));
        }

        return instructions;
    }

    private List<JmmInstruction> visitArrayAccess(JmmNode node, String scope) {
        List<JmmInstruction> instructionsLeft = this.visit(node.getChildren().get(0), scope);
        List<JmmInstruction> instructionsRight = this.visit(node.getChildren().get(1), scope);

        JmmInstruction leftVar = instructionsLeft.get(instructionsLeft.size() - 1);
        JmmInstruction rightVar = getTerminalInstruction(instructionsRight, instructionsRight.get(instructionsRight.size() - 1));

        List<JmmInstruction> instructions = new ArrayList<>(instructionsRight);
        instructions.add(new ArrayAccessInstruction(leftVar, rightVar));

        return instructions;
    }

    private List<JmmInstruction> visitAssign(JmmNode node, String scope) {
        Type paramType = getVarType(node.getChildren().get(0), scope);

        return generateTwoChildren(node, scope, new Operation(OperationType.EQUALS, paramType));
    }

    private List<JmmInstruction> visitClass(JmmNode node, String _s) {
        List<JmmInstruction> instructions = new ArrayList<>();

        instructions.add(new ClassInstruction(
                this.symbolTable.getClassName(),
                this.symbolTable.getFields(),
                this.defaultVisit(node, AnalysisTable.CLASS_SCOPE)
        ));

        return instructions;
    }

    private List<JmmInstruction> visitMethod(JmmNode node, String scope) {
        JmmNode returnType = node.getChildren().get(0);
        JmmNode name = node.getChildren().get(1);

        List<Symbol> parameters = new ArrayList<>();
        if(node.getChildren().size() >= 3 && node.getChildren().get(2).getKind().equals("MethodParameters")) {
            this.fillMethodParameters(node.getChildren().get(2), parameters);
        }

        String methodScope = AnalysisTable.getMethodString(name.get("VALUE"), parameters.stream().map(Symbol::getType).collect(Collectors.toList()));

        List<JmmInstruction> methodInstructions = getInstructions(node, methodScope);

        List<JmmInstruction> instructions = new ArrayList<>();
        instructions.add(new MethodInstruction(name.get("VALUE"), parameters, methodInstructions, getParamType(returnType)));

        return instructions;
    }

    private List<JmmInstruction> getInstructions(JmmNode node, String scope) {
        List<JmmInstruction> methodInstructions = new ArrayList<>();
        for (JmmNode child : node.getChildren()) {
            methodInstructions.addAll(visit(child, scope));
        }

        return methodInstructions;
    }

    private List<JmmInstruction> visitMain(JmmNode node, String scope) {
        JmmNode params = node.getChildren().get(0);

        List<Symbol> parameters = new ArrayList<>();

        this.fillMethodParameters(params, parameters);

        String mainScope = AnalysisTable.getMethodString(AnalysisTable.MAIN_SCOPE, parameters.stream().map(Symbol::getType).collect(Collectors.toList()));

        List<JmmInstruction> methodInstructions = getInstructions(node, mainScope);

        List<JmmInstruction> instructions = new ArrayList<>();
        instructions.add(new MainInstruction(parameters, methodInstructions));

        return instructions;
    }

    private List<JmmInstruction> visitIf(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        JmmNode condition = node.getChildren().get(0);
        JmmNode conditionTrue = node.getChildren().get(1);
        JmmNode conditionFalse = node.getChildren().get(2);

        List<JmmInstruction> conditionInstructions = visit(condition, scope);

        List<JmmInstruction> conditionTrueInstructions = visit(conditionTrue, scope);
        List<JmmInstruction> conditionFalseInstructions = visit(conditionFalse, scope);

        instructions.add(new IfInstruction(conditionInstructions, conditionTrueInstructions, conditionFalseInstructions));

        return instructions;
    }

    private List<JmmInstruction> visitWhile(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        JmmNode condition = node.getChildren().get(0);
        JmmNode loop = node.getChildren().get(1);

        List<JmmInstruction> conditionInstructions = visit(condition, scope);
        List<JmmInstruction> loopInstructions = visit(loop, scope);

        instructions.add(new WhileInstruction(conditionInstructions, loopInstructions));

        return instructions;
    }

    private String getImport(String className) {
        List<String> imports = this.symbolTable.getImports();

        for (String importStatement : imports) {
            if(this.getImportName(importStatement).equals(className)) {
                return importStatement;
            }
        }

        return "";
    }

    private String getImportName(String importStatement) {
        int delimiterIndex = importStatement.lastIndexOf(AnalysisTableBuilder.DELIMITER);

        if (delimiterIndex == -1) {
            return importStatement;
        }

        return importStatement.substring(delimiterIndex + 1);
    }

    private JmmInstruction getTerminalInstruction(List<JmmInstruction> instructions, JmmInstruction var) {
        if (var instanceof TerminalInstruction) {
            instructions.remove(var);
            return var;
        }

        return var.getVariable();
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

    private Type getVarType(JmmNode node, String scope) {
        JmmNode name = node;

        if(node.getNumChildren() > 0) {
            name = node.getChildren().get(0);
        }

        String varName = name.get("VALUE");

        Type varType = findTypeOfVar(this.symbolTable.getLocalVariables(scope), varName);
        if(varType != null) return varType;

        varType = findTypeOfVar(this.symbolTable.getParameters(scope), varName);
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

    private List<JmmInstruction> defaultVisit(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>();

        for (JmmNode child : node.getChildren()) {
            instructions.addAll(visit(child, scope));
        }

        return instructions;
    }
}
