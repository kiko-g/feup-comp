package pt.up.fe.comp.jmm.analysis.table;

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
}
