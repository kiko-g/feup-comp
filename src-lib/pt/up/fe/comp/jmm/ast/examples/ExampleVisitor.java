package pt.up.fe.comp.jmm.ast.examples;

import java.util.stream.Collectors;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

public class ExampleVisitor extends AJmmVisitor<String, String> {

    public ExampleVisitor() {
        addVisit("Identifier", this::dealWithIdentifier);
        setDefaultVisit(this::defaultVisit);
    }

    public String dealWithIdentifier(JmmNode node, String space) {
        if (node.get("id").equals("this")) {
            return space + "THIS_ACCESS\n";
        }
        return defaultVisit(node, space);
    }

    private String defaultVisit(JmmNode node, String space) {
        String content = space + node.getKind();
        String attrs = node.getAttributes()
                .stream()
                .filter(a -> !a.equals("line"))
                .map(a -> a + "=" + node.get(a))
                .collect(Collectors.joining(", ", "[", "]"));

        content += ((attrs.length() > 2) ? attrs : "") + "\n";
        for (JmmNode child : node.getChildren()) {
            content += visit(child, space + " ");
        }
        return content;
    }

}
