package ollir;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;

public class SethiUllmanLabeler extends PostorderJmmVisitor<Integer, Integer> {
    public static final String LABEL = "LABEL";

    public SethiUllmanLabeler() {
        addVisit("Params",      this::visitAllChildren);

        addVisit("Assign",      this::visitTwoChildren);
        addVisit("Dot",         this::visitTwoChildren);
        addVisit("Add",         this::visitTwoChildren);
        addVisit("Sub",         this::visitTwoChildren);
        addVisit("Mul",         this::visitTwoChildren);
        addVisit("Div",         this::visitTwoChildren);
        addVisit("LogicalAnd",  this::visitTwoChildren);
        addVisit("Less",        this::visitTwoChildren);
        addVisit("ArrayAccess", this::visitTwoChildren);

        addVisit("MethodCall",  this::visitSecondChildren);

        addVisit("Return",      this::visitFirstChildren);
        addVisit("If",          this::visitFirstChildren);
        addVisit("While",       this::visitFirstChildren);
        addVisit("Not",         this::visitFirstChildren);
        addVisit("Index",       this::visitFirstChildren);
        addVisit("Size",        this::visitFirstChildren);
        addVisit("Param",       this::visitFirstChildren);
        addVisit("Important",   this::visitFirstChildren);
        addVisit("New",         this::visitFirstChildren);
        addVisit("IntArray",    this::visitFirstChildren);

        addVisit("Object",      this::visitTerminal);
        addVisit("Var",         this::visitTerminal);
        addVisit("IntegerVal",  this::visitTerminal);
        addVisit("Bool",        this::visitTerminal);
        addVisit("Length",      this::visitTerminal);
        addVisit("This",        this::visitTerminal);

        setDefaultVisit(this::defaultVisit);
    }

    private Integer defaultVisit(JmmNode node, Integer _int) {
        setLabel(node, 0);

        return 0;
    }

    private Integer visitAllChildren(JmmNode node, Integer _int) {
        int numChildren = node.getNumChildren();
        int needed = numChildren;

        for (JmmNode child : node.getChildren()) {
            needed = Math.max(needed, numChildren + this.getLabel(child) - 1);
        }

        this.setLabel(node, needed);

        return needed;
    }

    private Integer visitSecondChildren(JmmNode node, Integer _int) {
        int label = this.getLabel(node.getChildren().get(1));
        this.setLabel(node, label);

        return label;
    }

    private Integer visitTwoChildren(JmmNode node, Integer _int) {
        int leftChildLabel = this.getLabel(node.getChildren().get(0));
        int rightChildLabel = this.getLabel(node.getChildren().get(1));

        int label = leftChildLabel == rightChildLabel ?
                leftChildLabel + 1 :
                Math.max(leftChildLabel, rightChildLabel);

        this.setLabel(node, label);

        return label;
    }

    private Integer visitFirstChildren(JmmNode node, Integer _int) {
        int label = this.getLabel(node.getChildren().get(0));
        this.setLabel(node, label);

        return label;
    }

    private Integer visitTerminal(JmmNode node, Integer _int) {
        this.setLabel(node, 1);

        return 1;
    }

    private int getLabel(JmmNode node) {
        return Integer.parseInt(node.get(LABEL));
    }

    private void setLabel(JmmNode node, int value) {
        node.put(LABEL, Integer.toString(value));
    }
}
