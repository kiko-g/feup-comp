package analysis.table;

import java.util.Objects;

public class Symbol {
    private final Type type;
    private final String name;
    
    public Symbol(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }
  
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symbol symbol = (Symbol) o;
        return symbol.getName().equals(this.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
