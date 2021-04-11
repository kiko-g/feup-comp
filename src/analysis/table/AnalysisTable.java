package analysis.table;

import java.util.*;
import java.util.stream.Collectors;

public class AnalysisTable implements SymbolTable {
    public final static String CLASS_SCOPE = "";
    public final static String MAIN_SCOPE = "main";

    private final Map<Method, Set<Symbol>> symbolTable = new HashMap<>();
    private final Map<Method, Set<Symbol>> methods = new HashMap<>();
    private final Set<String> imports = new HashSet<>();
    private String className;
    private String extension;
    private Method classMethod;

    public Method getClassMethod() {
        return classMethod;
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
        this.classMethod = new Method(new Symbol(new Type(className, false), CLASS_SCOPE), new ArrayList<>());
        this.symbolTable.put(classMethod, new HashSet<>());
    }

    @Override
    public String getSuper() {
        return this.extension;
    }

    public void setSuper(String extension) {
        this.extension = extension;
    }

    @Override
    public List<Method> getMethods() {
        return new ArrayList<>(this.methods.keySet());
    }

    public Method getMethod(String methodName, List<Type> parameters) {
        for (Method method : methods.keySet()) {
            if (method.getMethodSymbol().getName().equals(methodName) && parameters.size() == method.getParameters().size()) {
                boolean isEqual = true;
                for (int i = 0; i < parameters.size(); i++) {
                    isEqual &= parameters.get(i).equals(method.getParameters().get(i));
                }

                if (isEqual) {
                    return method;
                }
            }
        }

        return null;
    }

    public boolean addMethod(Method method) {
        if (this.methods.put(method, new HashSet<>()) != null) {
            return false;
        }

        return this.symbolTable.put(method, new HashSet<>()) == null;
    }

    @Override
    public List<Symbol> getFields() {
        return new ArrayList<>(this.symbolTable.get(this.className));
    }

    @Override
    public Type getReturnType(String methodName, List<Type> parameters) {
        for(Method method : this.methods.keySet()) {
            if(method.getMethodSymbol().getName().equals(methodName)) {
                return method.getMethodSymbol().getType();
            }
        }
        return null;
    }

    @Override
    public List<Symbol> getParameters(Method method) {
        Set<Symbol> parameters = this.methods.get(method);
        return parameters == null ? null : new ArrayList<>(parameters);
    }

    public boolean addParameter(Method method, Symbol param) {
        return this.methods.get(method).add(param);
    }

    @Override
    public List<Symbol> getLocalVariables(Method method) {
        return new ArrayList<>(this.symbolTable.get(method));
    }

    public Symbol getVariable(Method scope, String name) {
        for (Symbol variable : this.symbolTable.getOrDefault(scope, new HashSet<>())) {
            if (variable.getName().equals(name)) {
                return variable;
            }
        }

        return null;
    }

    public boolean addLocalVariable(Method scope, Symbol symbol) {
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
        for (Symbol field : this.symbolTable.get(this.classMethod)) {
            stringBuilder.append(backspace).append(field).append("\n");
        }

        return stringBuilder.toString();
    }

    private String methodsToString(String backspace) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Method, Set<Symbol>> entry : this.methods.entrySet()) {
            stringBuilder.append(backspace).append("method={\n");
            stringBuilder.append(backspace).append("\t").append(entry.getKey().getMethodSymbol()).append("\n");

            stringBuilder.append(backspace).append("\tparameters={\n");
            for (Symbol parameter : entry.getValue()) {
                stringBuilder.append(backspace).append("\t\t").append(parameter).append("\n");
            }
            stringBuilder.append(backspace).append("\t}\n");

            stringBuilder.append(backspace).append("\tvariables={\n");
            for (Symbol variable : this.symbolTable.get(entry.getKey())) {
                if (entry.getValue().contains(variable)) continue;
                stringBuilder.append(backspace).append("\t\t").append(variable).append("\n");
            }
            stringBuilder.append(backspace).append("\t}\n");
            stringBuilder.append(backspace).append("}\n");
        }

        return stringBuilder.toString();
    }
}
