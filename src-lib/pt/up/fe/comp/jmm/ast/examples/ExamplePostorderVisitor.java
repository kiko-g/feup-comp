package pt.up.fe.comp.jmm.ast.examples;

import java.util.Map;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;

/**
 * Counts the occurrences of each node kind.
 * 
 * @author JBispo
 *
 */
public class ExamplePostorderVisitor extends PostorderJmmVisitor<Map<String, Integer>, Boolean> {

    public ExamplePostorderVisitor() {
        // setDefaultVisit(ExamplePostorderVisitor::defaultVisit); // This is equivalent to the line below
        setDefaultVisit((node, map) -> ExamplePostorderVisitor.defaultVisit(node, map));
    }

    private static Boolean defaultVisit(JmmNode node, Map<String, Integer> kindCount) {

        var currentCount = kindCount.get(node.getKind());

        if (currentCount == null) {
            currentCount = 0;
        }

        currentCount++;

        kindCount.put(node.getKind(), currentCount);

        return true;
    }

}
