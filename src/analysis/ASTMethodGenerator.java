package analysis;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ASTMethodGenerator extends AJmmVisitor<String, List<Symbol>> {
    public ASTMethodGenerator() {
        addVisit("MethodParam", this::visitMethodParameter);
        addVisit("Method", this::visitMethod);
        addVisit("Main", this::visitMain);

        addVisit("Int", this::visitType);
        addVisit("Boolean", this::visitType);
        addVisit("Array", this::visitType);
        addVisit("ClassName", this::visitType);
        addVisit("Name", this::visitName);

        setDefaultVisit(this::defaultVisit);
    }

    private List<Symbol> visitMethod(JmmNode node, String _scope) {
        Type type = this.visit(node.getChildren().get(0), _scope).get(0).getType();
        String name = this.visit(node.getChildren().get(1), _scope).get(0).getName();

        List<Symbol> symbols = new ArrayList<>(Collections.singleton(new Symbol(type, name)));

        if(node.getChildren().size() >= 3 && node.getChildren().get(2).getKind().equals("MethodParameters")) {
            symbols.addAll(this.visit(node.getChildren().get(2), _scope));
        }

        return symbols;
    }

    private List<Symbol> visitMain(JmmNode node, String _scope) {
        List<Symbol> symbols = new ArrayList<>(Collections.singleton(new Symbol(new Type("void", false), "main")));

        symbols.addAll(this.visit(node.getChildren().get(0), _scope));

        return symbols;
    }

    private List<Symbol> visitType(JmmNode node, String _scope) {
        Type type;
        boolean isArray = false;

        if ("Array".equals(node.getKind())) {
            isArray = true;
            node = node.getChildren().get(0);
        }

        type = new Type(node.get("VALUE"), isArray);

        List<Symbol> symbols = new ArrayList<>();
        symbols.add(new Symbol(type, ""));
        return symbols;
    }

    private List<Symbol> visitName(JmmNode node, String _scope) {
        return new ArrayList<>(Collections.singleton(new Symbol(null, node.get("VALUE"))));
    }

    private List<Symbol> visitMethodParameter(JmmNode node, String _scope) {
        Type type = this.visit(node.getChildren().get(0), _scope).get(0).getType();
        String name = this.visit(node.getChildren().get(1), _scope).get(0).getName();

        return new ArrayList<>(Collections.singleton(new Symbol(type, name)));
    }

    private List<Symbol> defaultVisit(JmmNode node, String scope) {
        List<Symbol> parameters = new ArrayList<>();

        for (JmmNode child : node.getChildren()) {
            parameters.addAll(visit(child, scope));
        }

        return parameters;
    }
}
