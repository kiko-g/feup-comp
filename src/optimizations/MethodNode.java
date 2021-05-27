package optimizations;

import org.specs.comp.ollir.*;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public String toString() {
        return name + '(' +
            paramList.stream().map(element ->
                this.typeToString(element.getType()) + " " +
                    ((Operand) element).getName()).collect(Collectors.joining(", ")) + ')';
    }

    private String typeToString(Type type) {
        return switch (type.getTypeOfElement()){
            case INT32 -> "int";
            case STRING -> "String";
            case BOOLEAN -> "boolean";
            case OBJECTREF, THIS, VOID -> "";
            case CLASS -> ((ClassType) type).getName();
            case ARRAYREF -> ((ArrayType) type).getTypeOfElements() == ElementType.INT32 ? "int[]" : "String[]";
        };
    }
}
