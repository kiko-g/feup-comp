package optimizations;

import org.specs.comp.ollir.Descriptor;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        VarNode varNode = (VarNode) o;
        return Objects.equals(variable, varNode.variable) && Objects.equals(descriptor, varNode.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable);
    }
}
