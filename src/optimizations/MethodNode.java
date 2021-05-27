package optimizations;

import org.specs.comp.ollir.Element;

import java.util.List;

public class MethodNode {
    private final String name;
    private final List<Element> paramList;

    public MethodNode(String name, List<Element> paramList) {
        this.name = name;
        this.paramList = paramList;
    }

    public String getName() {
        return name;
    }

    public List<Element> getParamList() {
        return paramList;
    }
}
