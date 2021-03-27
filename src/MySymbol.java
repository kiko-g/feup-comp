import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class MySymbol extends Symbol {
    public MySymbol(Type type, String name) {
        super(type, name);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MySymbol symbol = (MySymbol) o;
        return symbol.getName().equals(this.getName());
    }
}
