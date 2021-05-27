package optimizations;

import org.specs.comp.ollir.Descriptor;

public class VarNode {
    private final String variable;
    private final Descriptor descriptor;

    public VarNode(String variable, Descriptor descriptor) {
        this.variable = variable;
        this.descriptor = descriptor;
    }

    public String getVariable() {
        return variable;
    }

    public Descriptor getDescriptor() {
        return descriptor;
    }
}
