package optimizations.ollir.data;

import org.specs.comp.ollir.Descriptor;

import java.util.Objects;

public class VarNode {
    private final String variable;
    private final Descriptor descriptor;
    private int id = -1;
    private boolean deleted = false;
    private boolean unused = true;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isUnused() {
        return unused;
    }

    public void setUnused(boolean unused) {
        this.unused = unused;
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

    @Override
    public String toString() {
        return variable + " '" + id + '\'';
    }
}
