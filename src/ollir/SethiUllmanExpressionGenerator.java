package ollir;

import analysis.AnalysisTableBuilder;
import ollir.instruction.*;
import ollir.instruction.complex.*;
import ollir.instruction.complex.binary.*;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

import java.util.ArrayList;
import java.util.List;

public class SethiUllmanExpressionGenerator extends AJmmVisitor<String, List<JmmInstruction>> {
    private final SymbolTable symbolTable;

    public SethiUllmanExpressionGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;

        addVisit("If",          this::visitIf);
        addVisit("While",       this::visitWhile);

        addVisit("Add",         (node, sns) -> this.generateTwoChildren(node, sns, new Operation(OperationType.ADD, new Type("int", false))));
        addVisit("Sub",         (node, sns) -> this.generateTwoChildren(node, sns, new Operation(OperationType.SUB, new Type("int", false))));
        addVisit("Mul",         (node, sns) -> this.generateTwoChildren(node, sns, new Operation(OperationType.MUL, new Type("int", false))));
        addVisit("Div",         (node, sns) -> this.generateTwoChildren(node, sns, new Operation(OperationType.DIV, new Type("int", false))));
        addVisit("LogicalAnd",  (node, sns) -> this.generateTwoChildren(node, sns, new Operation(OperationType.AND, new Type("bool", false))));
        addVisit("Less",        (node, sns) -> this.generateTwoChildren(node, sns, new Operation(OperationType.LESS_THAN, new Type("bool", false))));
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

        JmmInstruction sizeVar = getTerminalInstruction(sizeInstructions, sizeInstructions.get(sizeInstructions.size() - 1), scope);

        List<JmmInstruction> instructions = new ArrayList<>(sizeInstructions);
        instructions.add(new NewArrayInstruction(sizeVar));

        return instructions;
    }

    private List<JmmInstruction> visitNot(JmmNode node, String scope) {
        List<JmmInstruction> notInstructions = this.visit(node.getChildren().get(0), scope);

        JmmInstruction notVar = getTerminalInstruction(notInstructions, notInstructions.get(notInstructions.size() - 1), scope);

        List<JmmInstruction> instructions = new ArrayList<>(notInstructions);
        instructions.add(new NotInstruction(notVar));

        return instructions;
    }

    private List<JmmInstruction> visitReturn(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>(visit(node.getChildren().get(0), scope));
        JmmInstruction terminalVar = getTerminalInstruction(instructions, instructions.get(instructions.size() - 1), scope);

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
            leftInstruction = getTerminalInstruction(leftInstructions, leftInstructions.get(leftInstructions.size() - 1), scope);

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
            paramTerminals.add(getTerminalInstruction(paramInstructions, paramInstructions.get(paramInstructions.size() - 1), scope));
            instructions.addAll(paramInstructions);
        }

        return paramTerminals;
    }

    private List<JmmInstruction> visitAllChildren(JmmNode node, String scope) {
        List<JmmNode> children = node.getChildren();
        List<JmmInstruction> instructions = new ArrayList<>();

        for(JmmNode child : children) {
            instructions.add(new ParametersInstruction(visit(child, scope), scope));
        }

        return instructions;
    }

    private List<JmmInstruction> visitVar(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>();

        List<Symbol> parameters = this.symbolTable.getParameters(scope);
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i).getName().equals(node.get("VALUE"))) {
                instructions.add(new TerminalInstruction(new Symbol(parameters.get(i).getType(), "$" + i + "." + parameters.get(i).getName())));
                return instructions;
            }
        }

        for (Symbol symbol : symbolTable.getLocalVariables(scope)) {
            if (symbol.getName().equals(node.get("VALUE"))) {
                instructions.add(new TerminalInstruction(new Symbol(symbol.getType(), symbol.getName())));
                return instructions;
            }
        }

        for (Symbol symbol : symbolTable.getFields()) {
            if (symbol.getName().equals(node.get("VALUE"))) {
                instructions.add(new GetFieldInstruction(new Symbol(symbol.getType(), symbol.getName())));
                return instructions;
            }
        }

        return instructions;
    }

    private List<JmmInstruction> generateTwoChildren(JmmNode node, String scope, Operation operation) {
        List<JmmInstruction> instructions = new ArrayList<>();
        List<JmmInstruction> instructionsLeft = this.visit(node.getChildren().get(0), scope);
        List<JmmInstruction> instructionsRight = this.visit(node.getChildren().get(1), scope);

        JmmInstruction leftVar = getTerminalInstruction(instructionsLeft, instructionsLeft.get(instructionsLeft.size() - 1), scope);
        JmmInstruction rightVar = getTerminalInstruction(instructionsRight, instructionsRight.get(instructionsRight.size() - 1), scope);

        instructions.addAll(instructionsLeft);
        instructions.addAll(instructionsRight);
        instructions.add(new BinaryOperationInstruction(leftVar, rightVar, operation));

        return instructions;
    }

    private List<JmmInstruction> visitThis(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        instructions.add(new TerminalInstruction(new Symbol(new Type(this.symbolTable.getClassName(), false), "this")));

        return instructions;
    }

    private List<JmmInstruction> visitBool(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        instructions.add(new TerminalInstruction(new Symbol(new Type("bool", false), node.get("VALUE"))));

        return instructions;
    }

    private List<JmmInstruction> visitIntegerVal(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        instructions.add(new ConstantInstruction(new Symbol(new Type("int", false), node.get("VALUE"))));

        return instructions;
    }

    private List<JmmInstruction> visitObject(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        String className = node.getChildren().get(0).get("VALUE");

        if (className.equals(symbolTable.getClassName())) {
            instructions.add(new NewObjectInstruction(className, scope));
        } else {
            instructions.add(new NewObjectInstruction(getImport(className), scope));
        }

        return instructions;
    }

    private List<JmmInstruction> visitArrayAccess(JmmNode node, String scope) {
        List<JmmInstruction> instructionsLeft = this.visit(node.getChildren().get(0), scope);
        List<JmmInstruction> instructionsRight = this.visit(node.getChildren().get(1), scope);

        JmmInstruction leftVar = getTerminalInstruction(instructionsLeft, instructionsLeft.get(instructionsLeft.size() - 1), scope);
        JmmInstruction rightVar = getTerminalInstruction(instructionsRight, instructionsRight.get(instructionsRight.size() - 1), scope);

        if (rightVar instanceof ConstantInstruction) {
            ConstantInstruction rightConstant = (ConstantInstruction) rightVar;

            Symbol constantSymbol = rightConstant.getTerminal();
            constantSymbol = new Symbol(constantSymbol.getType(), "t" + ComplexInstruction.getStackCounter());

            ComplexInstruction.setStackCounter(ComplexInstruction.getStackCounter() + 1);

            JmmInstruction constantAssign = new BinaryOperationInstruction(new TerminalInstruction(constantSymbol), rightConstant, new Operation(OperationType.EQUALS, constantSymbol.getType()));

            instructionsRight.add(constantAssign);

            rightVar = constantAssign.getVariable(scope);
        }

        List<JmmInstruction> instructions = new ArrayList<>(instructionsLeft);
        instructions.addAll(instructionsRight);
        instructions.add(new ArrayAccessInstruction(((TerminalInstruction) leftVar).getTerminal(), rightVar));

        return instructions;
    }

    private List<JmmInstruction> visitAssign(JmmNode node, String scope) {
        Type paramType = getVarType(node.getChildren().get(0), scope);

        List<JmmInstruction> instructions = new ArrayList<>();
        List<JmmInstruction> instructionsLeft = this.visit(node.getChildren().get(0), scope);
        List<JmmInstruction> instructionsRight = this.visit(node.getChildren().get(1), scope);

        JmmInstruction rightVar = instructionsRight.get(instructionsRight.size() - 1);

        JmmInstruction leftVar = instructionsLeft.get(instructionsLeft.size() - 1);

        if (leftVar instanceof GetFieldInstruction) {
            rightVar = this.getTerminalInstruction(instructionsRight, rightVar, scope);
            instructionsLeft.remove(leftVar);
            instructions.addAll(instructionsRight);
            instructions.add(new PutFieldInstruction(((GetFieldInstruction) leftVar).getField(), rightVar));
            return instructions;
        }

        if (leftVar instanceof ArrayAccessInstruction) {
            instructionsLeft.remove(leftVar);
        } else {
            leftVar = getTerminalInstruction(instructionsLeft, leftVar, scope);
        }

        if (rightVar instanceof TerminalInstruction ||
                (rightVar instanceof BinaryOperationInstruction && ((BinaryOperationInstruction) rightVar).getOperation().getOperationType() != OperationType.EQUALS)) {
            instructionsRight.remove(rightVar);
        } else {
            rightVar = rightVar.getVariable(scope);
        }

        instructions.addAll(instructionsLeft);
        instructions.addAll(instructionsRight);

        if (rightVar instanceof NewObjectInstruction) {
            ((NewObjectInstruction) rightVar).setLhs(leftVar);
            instructions.add(rightVar);
            return instructions;
        }

        instructions.add(new BinaryOperationInstruction(leftVar, rightVar, new Operation(OperationType.EQUALS, paramType)));

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

        instructions.add(new IfInstruction(conditionInstructions, conditionTrueInstructions, conditionFalseInstructions, scope));

        return instructions;
    }

    private List<JmmInstruction> visitWhile(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>();
        JmmNode condition = node.getChildren().get(0);
        JmmNode loop = node.getChildren().get(1);

        List<JmmInstruction> conditionInstructions = visit(condition, scope);
        List<JmmInstruction> loopInstructions = visit(loop, scope);

        instructions.add(new WhileInstruction(conditionInstructions, loopInstructions, scope));

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

    private JmmInstruction getTerminalInstruction(List<JmmInstruction> instructions, JmmInstruction var, String scope) {
        if (var instanceof TerminalInstruction) {
            instructions.remove(var);
            return var;
        }

        return var.getVariable(scope);
    }

    private Type getVarType(JmmNode node, String scope) {
        JmmNode name = node;

        if(node.getKind().equals("ArrayAccess")) {
            return new Type("int", false);
        }

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
