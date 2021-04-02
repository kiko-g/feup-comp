package analysis.table;

import analysis.table.Symbol;
import analysis.table.SymbolTable;
import analysis.table.Type;

import java.util.*;
import java.util.stream.Collectors;

public class AnalysisTable implements SymbolTable {
    private Map<String, Set<Symbol>> symbolTable = new HashMap<>();
    private Map<Symbol, Set<Symbol>> methods = new HashMap<>();
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

    public void setClassName(String className, String classScope) {
        this.className = className;
        this.symbolTable.put(classScope, new HashSet<>());
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

    public void addMethod(Symbol method) {
        this.methods.put(method, new HashSet<>());
        this.symbolTable.put(method.getName(), new HashSet<>());
    }

    @Override
    public List<Symbol> getFields() {
        return new ArrayList<>(this.symbolTable.get(this.className));
    }

    @Override
    public Type getReturnType(String methodName) {
        for(Symbol symbol: this.methods.keySet()) {
            if(symbol.getName().equals(methodName)) {
                return symbol.getType();
            }
        }
        return null;
    }

    @Override
    public List<Symbol> getParameters(String methodName) {
        for(Symbol symbol: this.methods.keySet()) {
            if(symbol.getName().equals(methodName)) {
                return new ArrayList<>(this.methods.get(symbol));
            }
        }
        return null;
    }

    public boolean addParameter(Symbol method, Symbol param) {
        if(!this.methods.containsKey(method)) {
            return false;
        }

        return this.methods.get(method).add(param);
    }

    @Override
    public List<Symbol> getLocalVariables(String methodName) {
        return new ArrayList<>(this.symbolTable.get(methodName));
    }

    public boolean addLocalVariable(String scope, Symbol symbol) {
        return this.symbolTable.get(scope).add(symbol);
    }

    @Override
    public String toString() {
        return "AnalysisTable{" +
                "symbolTable=" + symbolTable +
                ", methods=" + methods +
                ", imports=" + imports +
                ", className='" + className + '\'' +
                ", extension='" + extension + '\'' +
                '}';
    }
}
