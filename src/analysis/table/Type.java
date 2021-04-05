package analysis.table;

import java.util.Objects;

public class Type {
    private final String name;
    private final boolean isArray;
    
    public Type(String name, boolean isArray) {
        this.name = name;
        this.isArray = isArray;
    }
  
    public String getName() {
        return name;
    }
    public boolean isArray() {
        return isArray;
    }

    @Override
    public String toString() {
        return name + (isArray ? "[]" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type type = (Type) o;
        return isArray == type.isArray && name.equals(type.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isArray);
    }
}
