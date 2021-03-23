package pt.up.fe.comp.jmm.analysis.table;

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
}
