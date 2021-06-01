package analysis.table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.*;
import java.util.stream.Collectors;

public class AnalysisTable implements SymbolTable {
    public final static String CLASS_SCOPE = "";
    public final static String MAIN_SCOPE = "main";
    public final static String PARAM_SEPARATOR = "^";
    private static AnalysisTable current;

    private final Map<String, Set<Symbol>> symbolTable = new HashMap<>();
    private final Map<Symbol, Set<Symbol>> methods = new HashMap<>();
    private final Set<String> imports = new HashSet<>();
    private String className;
    private String extension;

    public AnalysisTable() {
        current = this;
    }

    public static AnalysisTable getInstance() {
        return current;
    }

    public static String getMethodString(String methodName, List<Type> parameters) {
        StringBuilder builder = new StringBuilder();
        builder.append(methodName);

        for (Type parameter : parameters) {
            builder.append(PARAM_SEPARATOR).append(parameter);
        }

        return builder.toString();
    }

    public boolean hasConflict(String var, String scope) {
        for(Symbol param : this.getParameters(scope)) {
            if (param.getName().equals(var)) {
                return true;
            }
        }

        for(Symbol localVar : this.getLocalVariables(scope)) {
            if (localVar.getName().equals(var)) {
                return true;
            }
        }

        for(Symbol field : this.getFields()) {
            if (field.getName().equals(var)) {
                return true;
            }
        }

        return false;
    }

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

    public boolean hasMethod(String methodName) {
        for (Symbol method : this.methods.keySet()) {
            String knownMethod = method.getName();
            int paramsStartIndex = knownMethod.indexOf(PARAM_SEPARATOR);
            if (paramsStartIndex != -1) {
                knownMethod = knownMethod.substring(0, paramsStartIndex);
            }

            if (knownMethod.equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    public boolean addMethod(Symbol method) {
        if (this.methods.put(method, new HashSet<>()) != null) {
            return false;
        }

        return this.symbolTable.put(method.getName(), new HashSet<>()) == null;
    }

    @Override
    public List<Symbol> getFields() {
        return new ArrayList<>(this.symbolTable.get(CLASS_SCOPE));
    }

    @Override
    public Type getReturnType(String methodName) {
        for(Symbol method : this.methods.keySet()) {
            if(method.getName().equals(methodName)) {
                return method.getType();
            }
        }
        return null;
    }

    @Override
    public List<Symbol> getParameters(String methodName) {
        for (Symbol method : this.methods.keySet()) {
            if (method.getName().equals(methodName)) {
                return new ArrayList<>(this.methods.get(method));
            }
        }

        return null;
    }

    public boolean addParameter(String methodName, Symbol param) {
        for (Symbol method : this.methods.keySet()) {
            if (method.getName().equals(methodName)) {
                return this.methods.get(method).add(param);
            }
        }

        return false;
    }

    @Override
    public List<Symbol> getLocalVariables(String methodName) {
        return new ArrayList<>(this.symbolTable.get(methodName));
    }

    public Symbol getVariable(String scope, String name) {
        for (Symbol variable : this.symbolTable.getOrDefault(scope, new HashSet<>())) {
            if (variable.getName().equals(name)) {
                return variable;
            }
        }

        List<Symbol> parameters = this.getParameters(scope);
        parameters = parameters == null ? new ArrayList<>() : parameters;

        for (Symbol variable : parameters) {
            if (variable.getName().equals(name)) {
                return variable;
            }
        }

        return null;
    }

    public boolean addLocalVariable(String scope, Symbol symbol) {
        Set<Symbol> variables = this.symbolTable.get(scope);
        return variables != null && variables.add(symbol);
    }

    @Override
    public String toString() {
        return "AnalysisTable{\n" +
            "\timports=" + imports.stream().map(String::toString).collect(Collectors.joining(", ")) + "\n" +
            "\tclass='" + className + "'\n" +
            "\textends='" + extension + "'\n" +
            "\tfields={\n" + fieldsToString("\t\t") + "\t}\n" +
            "\tmethods={\n" + methodsToString("\t\t") + "\t}\n" +
            '}';
    }

    private String fieldsToString(String backspace) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Symbol field : this.symbolTable.get(CLASS_SCOPE)) {
            stringBuilder.append(backspace).append(field).append("\n");
        }

        return stringBuilder.toString();
    }

    private String methodsToString(String backspace) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Symbol, Set<Symbol>> entry : this.methods.entrySet()) {
            stringBuilder.append(backspace).append("method={\n");
            stringBuilder.append(backspace).append("\t").append(entry.getKey()).append("\n");

            stringBuilder.append(backspace).append("\tparameters={\n");
            for (Symbol parameter : entry.getValue()) {
                stringBuilder.append(backspace).append("\t\t").append(parameter).append("\n");
            }
            stringBuilder.append(backspace).append("\t}\n");

            stringBuilder.append(backspace).append("\tvariables={\n");
            for (Symbol variable : this.symbolTable.get(entry.getKey().getName())) {
                if (entry.getValue().contains(variable)) continue;
                stringBuilder.append(backspace).append("\t\t").append(variable).append("\n");
            }
            stringBuilder.append(backspace).append("\t}\n");
            stringBuilder.append(backspace).append("}\n");
        }

        return stringBuilder.toString();
    }
}
