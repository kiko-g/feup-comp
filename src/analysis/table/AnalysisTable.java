package analysis.table;

import analysis.table.Symbol;
import analysis.table.SymbolTable;
import analysis.table.Type;

import java.util.*;
import java.util.stream.Collectors;

public class AnalysisTable implements SymbolTable {
    public final static String CLASS_SCOPE = "";
    public final static String MAIN_SCOPE = "main";

    private final Map<String, Set<Symbol>> symbolTable = new HashMap<>();
    private final Map<Symbol, Set<Symbol>> methods = new HashMap<>();
    private final Set<String> imports = new HashSet<>();
    private String className;
    private String extension;

    @Override
    public List<String> getImports() {
        return new ArrayList<>(this.imports);
    }

    public boolean addImport(String name) {
        return this.imports.add(name);
    }

    @Override
    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
        this.symbolTable.put(CLASS_SCOPE, new HashSet<>());
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

    public Symbol getMethodSymbol(String method) {
        for (Symbol methodSymbol : methods.keySet()) {
            if (methodSymbol.getName().equals(method)) {
                return methodSymbol;
            }
        }

        return null;
    }

    public boolean addMethod(Symbol method) {
        if (this.methods.put(method, new HashSet<>()) != null) {
            return false;
        }

        return this.symbolTable.put(method.getName(), new HashSet<>()) == null;
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
        return this.methods.get(method).add(param);
    }

    @Override
    public List<Symbol> getLocalVariables(String methodName) {
        return new ArrayList<>(this.symbolTable.get(methodName));
    }

    public Symbol getVariable(String scope, String name) {
        for (Symbol variable : this.symbolTable.get(scope)) {
            if (variable.getName().equals(name)) {
                return variable;
            }
        }

        return null;
    }

    public boolean addLocalVariable(String scope, Symbol symbol) {
        return this.symbolTable.get(scope).add(symbol);
    }
}
