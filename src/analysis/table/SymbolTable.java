package analysis.table;

import java.util.List;

public interface SymbolTable {
    /** 
     * @return a list of fully qualified names of imports
     */
    List<String> getImports();

    /** 
     * @return the name of the main class
     */
    String getClassName();
    
    /**
     * 
     * @return the name that the classes extends, or null if the class does not extend another class 
     */
    String getSuper();
/**
     * 
     * @return a list of Symbols that represent the fields of the class
     */
    List<Symbol> getFields();
    /**
     * 
     * @return a list with the names of the methods of the class
     */
    List<Method> getMethods();

    /**
     * 
     * @return the return type of the given method 
     */
    Type getReturnType(String methodName, List<Type> parameters);

    /**
     * 
     * @param method
     * @return a list of parameters of the given method
     */
    List<Symbol> getParameters(Method method);

    /**
     * 
     * @param method
     * @return a list of local variables declared in the given method
     */
    List<Symbol> getLocalVariables(Method method);

}


