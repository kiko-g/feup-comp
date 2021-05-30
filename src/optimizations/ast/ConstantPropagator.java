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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConstantPropagator  extends AJmmVisitor<String, ConstantPropagator.DeletedNSymbol> {
    private final static Type INT = new Type("int", false);
    private final static Type BOOL = new Type("boolean", false);

    protected static class DeletedNSymbol {
        private final Symbol symbol;
        private final boolean deleted;

        public DeletedNSymbol(Symbol symbol, boolean deleted) {
            this.symbol = symbol;
            this.deleted = deleted;
        }
    }

    private final Map<String, Symbol> constants = new HashMap<>();

    public ConstantPropagator() {
        addVisit("Method", this::visitMethod);
        addVisit("Main", this::visitMain);

        addVisit("While", (node, scope) -> {this.constants.clear(); return this.defaultVisit(node, scope);});
        addVisit("If", (node, scope) -> {this.constants.clear(); return this.defaultVisit(node, scope);});

        // Constant Propagation
        addVisit("Var", this::visitVar);
        addVisit("Assign", this::visitAssign);
        addVisit("IntegerVal", (node, scope) -> new DeletedNSymbol(new Symbol(INT, node.get("VALUE")), false));
        addVisit("Bool", (node, scope) -> new DeletedNSymbol(new Symbol(BOOL, node.get("VALUE")), false));

        // Constant Folding
        addVisit("LogicalAnd", (node, scope) -> visitOperation(node, scope, BOOL, BOOL, Boolean::parseBoolean, (lhs, rhs) -> lhs && rhs));
        addVisit("Less", (node, scope) -> visitOperation(node, scope, INT, BOOL, Integer::parseInt, (lhs, rhs) -> lhs < rhs));
        addVisit("Add", (node, scope) -> visitOperation(node, scope, INT, INT, Integer::parseInt, Integer::sum));
        addVisit("Sub", (node, scope) -> visitOperation(node, scope, INT, INT, Integer::parseInt, (lhs, rhs) -> lhs - rhs));
        addVisit("Mul", (node, scope) -> visitOperation(node, scope, INT, INT, Integer::parseInt, (lhs, rhs) -> lhs - rhs));
        addVisit("Div", (node, scope) -> visitOperation(node, scope, INT, INT, Integer::parseInt, (lhs, rhs) -> lhs - rhs));
        addVisit("Not", this::visitNot);

        setDefaultVisit(this::defaultVisit);
    }

    private <T, R> DeletedNSymbol visitOperation(JmmNode node, String scope, Type paramType, Type returnType, Function<String, T> parser, BiFunction<T, T, R> operator) {
        DeletedNSymbol leftNode = visit(node.getChildren().get(0), scope);
        DeletedNSymbol rightNode = visit(node.getChildren().get(1), scope);

        if (isNotConstant(leftNode, paramType) || isNotConstant(rightNode, paramType)) {
            return null;
        }

        T lhsVal = parser.apply(leftNode.symbol.getName());
        T rhsVal = parser.apply(rightNode.symbol.getName());

        Symbol symbol = new Symbol(returnType, String.valueOf(operator.apply(lhsVal, rhsVal)));

        this.replaceNode(node, symbol);

        return new DeletedNSymbol(symbol, false);
    }

    private DeletedNSymbol visitNot(JmmNode node, String scope) {
        DeletedNSymbol value = visit(node.getChildren().get(0), scope);

        if (isNotConstant(value, BOOL)) {
            return null;
        }

        boolean notValue = Boolean.parseBoolean(value.symbol.getName());

        Symbol symbol = new Symbol(BOOL, String.valueOf(!notValue));

        this.replaceNode(node, symbol);

        return new DeletedNSymbol(symbol, false);
    }

    private boolean isNotConstant(DeletedNSymbol value, Type type) {
        return value == null || !value.symbol.getType().equals(type);
    }

    private DeletedNSymbol visitVar(JmmNode node, String scope) {
        if (!constants.containsKey(node.get("VALUE"))) {
            return null;
        }

        Symbol constant = constants.get(node.get("VALUE"));

        replaceNode(node, constant);

        return new DeletedNSymbol(constant, false);
    }

    private void replaceNode(JmmNode node, Symbol symbol) {
        int index;
        JmmNode parent = node.getParent();

        for (index = 0; index < parent.getNumChildren(); index++) {
            if (parent.getChildren().get(index) == node) {
                break;
            }
        }

        node.delete();


        JmmNode newNode = new JmmNodeImpl(this.valueToKind(symbol.getType()));
        newNode.put("COLUMN", node.get("COLUMN"));
        newNode.put("LINE", node.get("LINE"));
        newNode.put("VALUE", symbol.getName());

        parent.add(newNode, index);
    }

    private String valueToKind(Type type) {
        return switch (type.getName()) {
            case "int" -> "IntegerVal";
            case "boolean" -> "Bool";
            default -> throw new IllegalStateException("Unexpected value: " + type.getName());
        };
    }

    private DeletedNSymbol visitAssign(JmmNode node, String scope) {
        JmmNode leftNode = node.getChildren().get(0);
        DeletedNSymbol rightValue = visit(node.getChildren().get(1), scope);

        if (leftNode.getKind().equals("ArrayAccess")) {
            return new DeletedNSymbol(null, false);
        }

        if (rightValue != null && rightValue.symbol != null) {
            constants.put(leftNode.get("VALUE"), rightValue.symbol);
        } else {
            constants.remove(leftNode.get("VALUE"));
        }

        return new DeletedNSymbol(null, false);
    }

    private DeletedNSymbol visitMain(JmmNode node, String scope) {
        String methodScope = initializeMethod(node);
        DeletedNSymbol deletedNSymbol;
        for (int i = 1; i < node.getNumChildren(); i++){
            if (node.getChildren().get(i).getKind().equals("MethodParameters")) {
                continue;
            }

            deletedNSymbol = visit(node.getChildren().get(i), methodScope);

            if (deletedNSymbol != null && deletedNSymbol.deleted) {
                i--;
            }
        }

        return null;
    }

    private DeletedNSymbol visitMethod(JmmNode node, String e) {
        String methodScope = initializeMethod(node);
        DeletedNSymbol deletedNSymbol;
        for (int i = 2; i < node.getNumChildren(); i++){
            if (node.getChildren().get(i).getKind().equals("MethodParameters")) {
                continue;
            }

            deletedNSymbol = visit(node.getChildren().get(i), methodScope);

            if (deletedNSymbol != null && deletedNSymbol.deleted) {
                i--;
            }
        }

        return null;
    }

    private String initializeMethod(JmmNode node) {
        List<Symbol> methodSymbols = new ASTMethodGenerator().visit(node);
        Symbol methodSymbol = methodSymbols.remove(0);

        this.constants.clear();

        return AnalysisTable.getMethodString(methodSymbol.getName(), methodSymbols.stream().map(Symbol::getType).collect(Collectors.toList()));
    }

    private DeletedNSymbol defaultVisit(JmmNode node, String scope) {
        DeletedNSymbol deletedNSymbol;
        for (int i = 0; i < node.getNumChildren(); i++) {
            deletedNSymbol = visit(node.getChildren().get(i), scope);

            if (deletedNSymbol != null && deletedNSymbol.deleted) {
                i--;
            }
        }

        return null;
    }
}
