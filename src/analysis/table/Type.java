package analysis.table;

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
}
