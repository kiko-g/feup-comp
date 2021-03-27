import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.*;
import java.util.stream.Collectors;

public class AnalysisTable implements SymbolTable {
    private Map<String, Set<MySymbol>> symbolTable = new HashMap<>();
    private Map<MySymbol, Set<MySymbol>> methods = new HashMap<>();
    private List<String> imports = new ArrayList<>();
    private String className;
    private String extension;

    @Override
    public List<String> getImports() {
        return this.imports;
    }

    public void addImport(String name) {
        this.imports.add(name);
    }

    @Override
    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String getSuper() {
        return this.extension;
    }

    public void setSuper(String extension) {
        this.extension = extension;
    }

    @Override
    public List<String> getMethods() {
        return this.methods.keySet().stream().map(Symbol::getName).collect(Collectors.toList());
    }

    public void addMethod(MySymbol method) {
        this.methods.put(method, new HashSet<>());
    }

    @Override
    public List<Symbol> getFields() {
        return new ArrayList<>(this.symbolTable.get(this.className));
    }

    @Override
    public Type getReturnType(String methodName) {
        for(MySymbol symbol: this.methods.keySet()) {
            if(symbol.getName().equals(methodName)) {
                return symbol.getType();
            }
        }
        return null;
    }

    @Override
    public List<Symbol> getParameters(String methodName) {
        for(MySymbol symbol: this.methods.keySet()) {
            if(symbol.getName().equals(methodName)) {
                return new ArrayList<>(this.methods.get(symbol));
            }
        }
        return null;
    }

    @Override
    public List<Symbol> getLocalVariables(String methodName) {
        return new ArrayList<>(this.symbolTable.get(methodName));
    }
}
