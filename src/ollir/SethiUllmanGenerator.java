package ollir;

import analysis.ASTMethodGenerator;
import analysis.table.AnalysisTable;
import ollir.instruction.ClassInstruction;
import ollir.instruction.JmmInstruction;
import ollir.instruction.MainInstruction;
import ollir.instruction.MethodInstruction;
import ollir.instruction.complex.ComplexInstruction;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SethiUllmanGenerator extends AJmmVisitor<String, List<JmmInstruction>> {
    private final SymbolTable symbolTable;
    private final SethiUllmanExpressionGenerator expressionGenerator;

    public SethiUllmanGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.expressionGenerator = new SethiUllmanExpressionGenerator(symbolTable);

        ComplexInstruction.setStackCounter(0);

        addVisit("Class",       this::visitClass);
        addVisit("Method",      this::visitMethod);
        addVisit("Main",        this::visitMain);

        setDefaultVisit(this::defaultVisit);
    }

    private List<JmmInstruction> visitClass(JmmNode node, String _s) {
        List<JmmInstruction> instructions = new ArrayList<>();

        instructions.add(new ClassInstruction(
                this.symbolTable.getClassName(),
                this.symbolTable.getSuper(),
                this.symbolTable.getFields(),
                this.defaultVisit(node, AnalysisTable.CLASS_SCOPE)
        ));

        return instructions;
    }

    private List<JmmInstruction> visitMethod(JmmNode node, String scope) {
        List<Symbol> methodSymbols = new ASTMethodGenerator().visit(node, scope);
        Symbol methodSymbol = methodSymbols.remove(0);

        String methodScope = AnalysisTable.getMethodString(methodSymbol.getName(), methodSymbols.stream().map(Symbol::getType).collect(Collectors.toList()));

        List<JmmInstruction> methodInstructions = getInstructions(node, methodScope);

        return new ArrayList<>(Collections.singleton(new MethodInstruction(methodSymbol, methodSymbols, methodInstructions)));
    }

    private List<JmmInstruction> visitMain(JmmNode node, String scope) {
        List<Symbol> methodSymbols = new ASTMethodGenerator().visit(node, scope);
        methodSymbols.remove(0);

        String mainScope = AnalysisTable.getMethodString(AnalysisTable.MAIN_SCOPE, methodSymbols.stream().map(Symbol::getType).collect(Collectors.toList()));

        List<JmmInstruction> methodInstructions = getInstructions(node, mainScope);

        return new ArrayList<>(Collections.singleton(new MainInstruction(methodSymbols, methodInstructions)));
    }

    private List<JmmInstruction> getInstructions(JmmNode node, String scope) {
        List<JmmInstruction> methodInstructions = new ArrayList<>();
        for (JmmNode child : node.getChildren()) {
            methodInstructions.addAll(this.expressionGenerator.visit(child, scope));
        }

        return methodInstructions;
    }

    private List<JmmInstruction> defaultVisit(JmmNode node, String scope) {
        List<JmmInstruction> instructions = new ArrayList<>();

        for (JmmNode child : node.getChildren()) {
            instructions.addAll(visit(child, scope));
        }

        return instructions;
    }

}
