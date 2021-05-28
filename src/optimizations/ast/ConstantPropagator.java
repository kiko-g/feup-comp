package optimizations.ast;

import analysis.ASTMethodGenerator;
import analysis.table.AnalysisTable;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConstantPropagator  extends AJmmVisitor<String, ConstantPropagator.Value> {
    private Map<String, Value> constants = new HashMap<>();

    protected static class Value {
        private final Type type;
        private final String value;

        public Value(Type type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    public ConstantPropagator() {
        addVisit("Method", this::visitMethod);
        addVisit("Main", this::visitMain);

        addVisit("Var", this::visitVar);
        addVisit("Assign", this::visitAssign);
        addVisit("IntegerVal", this::visitAssign);
        addVisit("Bool", this::visitAssign);

        setDefaultVisit(this::defaultVisit);
    }

    private Value visitVar(JmmNode node, String scope) {
        if (!constants.containsKey(node.get("VALUE"))) {
            return null;
        }

        int index;
        JmmNode parent = node.getParent();

        for (index = 0; index < parent.getNumChildren(); index++) {
            if (parent.getChildren().get(index) == node) {
                break;
            }
        }

        node.delete();

        Value constant = constants.get(node.get("VALUE"));

        JmmNode newNode = new JmmNodeImpl(this.valueToKind(constant));
        newNode.put("COLUMN", node.get("COLUMN"));
        newNode.put("LINE", node.get("LINE"));
        newNode.put("VALUE", constant.value);

        parent.add(node, index);

        return constant;
    }

    private String valueToKind(Value value) {
        return switch (value.type.getName()) {
            case "int" -> "IntegerVal";
            case "boolean" -> "Bool";
            default -> throw new IllegalStateException("Unexpected value: " + value.type.getName());
        };
    }

    private Value visitAssign(JmmNode node, String scope) {
        JmmNode leftNode = node.getChildren().get(0);
        Value rightValue = visit(node, scope);

        if (leftNode.getKind().equals("ArrayAccess")) {
            return null;
        }

        if (rightValue != null) {
            constants.put(leftNode.get("VALUE"), rightValue);
            node.delete();
        }

        return null;
    }

    private Value visitMain(JmmNode node, String scope) {
        List<Symbol> methodSymbols = new ASTMethodGenerator().visit(node);
        Symbol methodSymbol = methodSymbols.remove(0);

        String methodScope = AnalysisTable.getMethodString(methodSymbol.getName(), methodSymbols.stream().map(Symbol::getType).collect(Collectors.toList()));

        this.constants.clear();

        for (int i = 1; i < node.getNumChildren(); i++){
            if (node.getChildren().get(i).getKind().equals("MethodParameters")) {
                continue;
            }

            visit(node.getChildren().get(i), methodScope);
        }

        return null;
    }

    private Value visitMethod(JmmNode node, String e) {
        List<Symbol> methodSymbols = new ASTMethodGenerator().visit(node);
        Symbol methodSymbol = methodSymbols.remove(0);

        String methodScope = AnalysisTable.getMethodString(methodSymbol.getName(), methodSymbols.stream().map(Symbol::getType).collect(Collectors.toList()));

        this.constants.clear();

        for (int i = 2; i < node.getNumChildren(); i++){
            if (node.getChildren().get(i).getKind().equals("MethodParameters")) {
                continue;
            }

            visit(node.getChildren().get(i), methodScope);
        }

        return null;
    }

    private Value defaultVisit(JmmNode node, String scope) {
        for (JmmNode child : node.getChildren()) {
            visit(child, scope);
        }

        return null;
    }
}
