package analysis.table;

import java.util.List;
import java.util.Objects;

public class Method {
    private final Symbol methodSymbol;
    private final List<Type> parameters;

    public Method(Symbol methodSymbol, List<Type> parameters) {
        this.methodSymbol = methodSymbol;
        this.parameters = parameters;
    }

    public Symbol getMethodSymbol() {
        return methodSymbol;
    }

    public List<Type> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Method method = (Method) o;
        return methodSymbol.equals(method.methodSymbol) && parameters.equals(method.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodSymbol, parameters);
    }
}
