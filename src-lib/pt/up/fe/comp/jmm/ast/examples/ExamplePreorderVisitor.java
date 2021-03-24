package pt.up.fe.comp.jmm.ast.examples;

import java.util.List;
import java.util.stream.Collectors;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.specs.util.utilities.StringLines;

/**
 * Counts the occurences of each node kind.
 * 
 * @author JBispo
 *
 */
public class ExamplePreorderVisitor extends PreorderJmmVisitor<String, String> {

    private final String identifierAttribute;

    public ExamplePreorderVisitor(String identifierType, String identifierAttribute) {
        super(ExamplePreorderVisitor::reduce);

        this.identifierAttribute = identifierAttribute;

        addVisit(identifierType, this::dealWithIdentifier);
        setDefaultVisit(this::defaultVisit);
    }

    public String dealWithIdentifier(JmmNode node, String space) {
        if (node.get(identifierAttribute).equals("this")) {
            return space + "THIS_ACCESS";
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

        content += ((attrs.length() > 2) ? attrs : "");

        return content;
    }

    private static String reduce(String nodeResult, List<String> childrenResults) {
        var content = new StringBuilder();

        content.append(nodeResult).append("\n");

        for (var childResult : childrenResults) {
            var childContent = StringLines.getLines(childResult).stream()
                    .map(line -> " " + line + "\n")
                    .collect(Collectors.joining());

            content.append(childContent);
        }

        return content.toString();
    }

}
