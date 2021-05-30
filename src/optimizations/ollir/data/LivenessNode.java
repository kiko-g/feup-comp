package optimizations.ollir.data;

import org.specs.comp.ollir.Instruction;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class LivenessNode {
    private final Instruction instruction;
    private final Set<VarNode> in = new HashSet<>();
    private final Set<VarNode> out = new HashSet<>();
    private final Set<VarNode> def = new HashSet<>();
    private final Set<VarNode> use = new HashSet<>();
    private final Set<LivenessNode> succ = new HashSet<>();

    public LivenessNode(Instruction instruction) {
        this.instruction = instruction;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public Set<VarNode> getIn() {
        return in;
    }

    public Set<VarNode> getOut() {
        return out;
    }

    public Set<VarNode> getDef() {
        return def;
    }

    public Set<VarNode> getUse() {
        return use;
    }

    public Set<LivenessNode> getSucc() {
        return succ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LivenessNode that = (LivenessNode) o;
        return instruction.equals(that.instruction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instruction);
    }
}
