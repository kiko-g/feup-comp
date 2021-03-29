package pt.up.fe.comp.jmm.ast.examples;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

/**
 * Counts the occurrences of each node kind.
 * 
 * @author JBispo
 *
 */
public class ExamplePrintVariables extends PreorderJmmVisitor<Boolean, Boolean> {

    private final String varNameAttribute;
    private final String varLineAttribute;

    public ExamplePrintVariables(String varNodeKind, String varNameAttribute, String varLineAttribute) {
        this.varNameAttribute = varNameAttribute;
        this.varLineAttribute = varLineAttribute;

        addVisit(varNodeKind, this::printId); // Method reference
    }

    private Boolean printId(JmmNode node, Boolean dummy) {
        System.out.println(
                "Var '" + node.get(varNameAttribute) + "' in line " + node.get(varLineAttribute) + ", parent of "
                        + node.getParent().getKind());

        return true;
    }

}
