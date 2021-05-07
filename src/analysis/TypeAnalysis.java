package analysis;

import analysis.table.AnalysisTable;
import ollir.OllirUtils;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import report.Report;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TypeAnalysis extends AJmmVisitor<TypeAnalysis.TypeNScope, Type> {
    public final static Type INT = new Type("int", false);
    public final static Type INT_ARRAY = new Type("int", true);
    public final static Type BOOL = new Type("boolean", false);
    public final static Type LENGTH = new Type("length", false);
    public final static Type NULL = new Type("void", false);

    private final AnalysisTable symbolTable;
    private final List<Report> reports;

    protected static class TypeNScope {
        final private String scope;
        final private Type expected;
        final private String previousScope;

        public TypeNScope(String scope, Type expected, String previousScope) {
            this.scope = scope;
            this.expected = expected;
            this.previousScope = previousScope;
        }
    }

    public TypeAnalysis(AnalysisTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;

        addVisit("Class", this::visitClass);
        addVisit("Method", this::visitMethod);
        addVisit("Main", this::visitMain);
        addVisit("VarDecl", this::visitVarDecl);
        addVisit("If", this::visitConditionExpression);
        addVisit("While", this::visitConditionExpression);
        addVisit("Assign", this::visitEqual);
        addVisit("Dot", this::visitDot);
        addVisit("MethodCall", this::visitMethodCall);
        addVisit("Add", this::visitInt);
        addVisit("Sub", this::visitInt);
        addVisit("Mul", this::visitInt);
        addVisit("Div", this::visitInt);
        addVisit("LogicalAnd", this::visitBool);
        addVisit("Less", this::visitIntReturnBool);
        addVisit("Not", this::visitSingleBool);
        addVisit("ArrayAccess", this::visitArray);
        addVisit("Object", this::visitObject);
        addVisit("IntArray", this::returnIntArray);
        addVisit("Index", this::visitSingleInt);
        addVisit("Size", this::visitSingleInt);
        addVisit("Var", this::visitVar);
        addVisit("IntegerVal", this::returnInt);
        addVisit("Bool", this::returnBool);
        addVisit("Length", this::returnLength);
        addVisit("This", this::returnThis);
        setDefaultVisit(this::defaultVisit);
    }

    private Type visitVarDecl(JmmNode node, TypeNScope typeNScope) {
        JmmNode leftOperand = node.getChildren().get(0);

        if (leftOperand.getKind().equals("ClassName")) {
            String className = leftOperand.get("VALUE");

            if (className.equals(this.symbolTable.getClassName())) {
                return typeNScope.expected;
            }

            List<String> imports = this.symbolTable.getImports();
            for (String importClass : imports) {
                if (getImportName(importClass).equals(className)) {
                    return typeNScope.expected;
                }
            }

            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("LINE")),
                    Integer.parseInt(node.get("COLUMN")),
                    "Could not find the class \"" + className + "\", are you missing an import?"
                )
            );
        }

        return typeNScope.expected;
    }

    private Type visitClass(JmmNode node, TypeNScope typeNScope) {
        TypeNScope scope = new TypeNScope(AnalysisTable.CLASS_SCOPE, NULL, "");
        for (JmmNode child : node.getChildren()) {
            visit(child, scope);
        }

        return NULL;
    }

    private Type visitObject(JmmNode node, TypeNScope typeNScope) {
        JmmNode nameNode = node.getChildren().get(0);

        return new Type(nameNode.get("VALUE"), false);
    }

    private Type visitMethod(JmmNode node, TypeNScope typeNScope) {
        List<Symbol> methodSymbols = new ASTMethodGenerator().visit(node);
        Symbol methodSymbol = methodSymbols.remove(0);

        TypeNScope scope = new TypeNScope(
            AnalysisTable.getMethodString(methodSymbol.getName(), methodSymbols.stream().map(Symbol::getType).collect(Collectors.toList())),
                NULL,
            typeNScope.scope
        );

        for (JmmNode child : node.getChildren()){
            visit(child, scope);
        }

        return methodSymbol.getType();
    }

    private Type visitMain(JmmNode node, TypeNScope typeNScope) {
        List<Symbol> methodSymbols = new ASTMethodGenerator().visit(node);
        methodSymbols.remove(0);

        TypeNScope scope = new TypeNScope(
            AnalysisTable.getMethodString(AnalysisTable.MAIN_SCOPE, methodSymbols.stream().map(Symbol::getType).collect(Collectors.toList())),
                NULL,
            typeNScope.scope
        );

        for (JmmNode child : node.getChildren()){
            visit(child, scope);
        }

        return NULL;
    }

    private Type visitConditionExpression(JmmNode node, TypeNScope typeNScope) {
        JmmNode conditionalNode = node.getChildren().get(0);

        checkSingleType(conditionalNode, new TypeNScope(typeNScope.scope, BOOL, typeNScope.scope));

        for (int i = 1; i < node.getNumChildren(); i++) {
            visit(node.getChildren().get(i), typeNScope);
        }

        return NULL;
    }

    private Type visitEqual(JmmNode node, TypeNScope typeNScope) {
        JmmNode leftOperand = node.getChildren().get(0);
        JmmNode rightOperand = node.getChildren().get(1);

        Type leftType = visit(leftOperand, typeNScope);

        checkSingleType(rightOperand, new TypeNScope(typeNScope.scope, leftType, typeNScope.scope));

        return leftType;
    }

    private Type visitDot(JmmNode node, TypeNScope typeNScope) {
        JmmNode leftOperand = node.getChildren().get(0);
        JmmNode rightOperand = node.getChildren().get(1);

        Type leftType = visit(leftOperand, typeNScope);
        Type rightType = visit(
            rightOperand,
            new TypeNScope(
                leftType.getName().equals(symbolTable.getClassName()) ?
                    AnalysisTable.CLASS_SCOPE :
                    leftType.getName(),
                typeNScope.expected,
                typeNScope.scope
            )
        );

        if (leftType.equals(INT) || leftType.equals(BOOL) || leftType.equals(LENGTH)) {
            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("LINE")),
                    Integer.parseInt(node.get("COLUMN")),
                    "Invalid type found. Expected \"<class>\" or \"this\", found \"" + typeToString(leftType) + "\""
                )
            );
        }

        if (LENGTH.equals(rightType) && !leftType.isArray()) {
            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("LINE")),
                    Integer.parseInt(node.get("COLUMN")),
                    "Invalid type found. Expected \"<any_type>[]\", found \"" + typeToString(rightType) + "\""
                )
            );
        }

        return leftType.getName().equals(symbolTable.getClassName()) ? rightType : (leftType.isArray() && LENGTH.equals(rightType) ? INT : rightType);
    }

    private Type visitMethodCall(JmmNode node, TypeNScope typeNScope) {
        JmmNode methodNameNode = node.getChildren().get(0);
        JmmNode paramsNode = node.getChildren().get(1);

        String methodName = methodNameNode.get("VALUE");

        TypeNScope newTypeNScope = new TypeNScope(typeNScope.previousScope, NULL, "");
        List<Type> givenParameters = paramsNode.getChildren().stream().map((child) -> this.visit(child, newTypeNScope)).collect(Collectors.toList());

        String method = AnalysisTable.getMethodString(methodName, givenParameters);
        boolean hasMethod = this.symbolTable.hasMethod(methodName);

        // check if method isn't from class
        if (!hasMethod) {
            if (AnalysisTable.CLASS_SCOPE.equals(typeNScope.scope) && this.symbolTable.getSuper() != null) {
                node.put("RETURN", OllirUtils.typeToOllir(typeNScope.expected));
                return typeNScope.expected;
            }

            List<String> imports = this.symbolTable.getImports().stream().map(this::getImportName).collect(Collectors.toList());
            boolean isImportedMethod = false;

            for (String importName : imports) {
                isImportedMethod |= importName.equals(typeNScope.scope) || importName.equals(methodName);
            }

            if (!isImportedMethod) {
                this.reports.add(
                    new Report(
                        ReportType.ERROR,
                        Stage.SEMANTIC,
                        Integer.parseInt(node.get("LINE")),
                        Integer.parseInt(node.get("COLUMN")),
                        "Could not find the method \"" + methodName + "\", are you missing an import?"
                    )
                );
            }

            node.put("RETURN", OllirUtils.typeToOllir(typeNScope.expected));
            return typeNScope.expected;
        }

        List<Symbol> methodParameters = this.symbolTable.getParameters(method);

        if (methodParameters == null) {
            if (AnalysisTable.CLASS_SCOPE.equals(typeNScope.scope) && this.symbolTable.getSuper() != null) {
                node.put("RETURN", OllirUtils.typeToOllir(typeNScope.expected));
                return typeNScope.expected;
            }

            for (String knownMethod : this.symbolTable.getMethods()) {
                String knownMethodName = knownMethod.substring(0, knownMethod.indexOf(AnalysisTable.PARAM_SEPARATOR));

                if (knownMethodName.equals(methodName)) {
                    List<Symbol> knownMethodParams = this.symbolTable.getParameters(knownMethod);
                    if (methodParameters == null) {
                        methodParameters = knownMethodParams;
                    } else if(Math.abs(givenParameters.size() - methodParameters.size()) > (Math.abs(givenParameters.size() - knownMethodParams.size()))) {
                        methodParameters = knownMethodParams;
                    }
                }
            }

            if (methodParameters.size() == givenParameters.size()) {
                for (int i = 0; i < methodParameters.size(); i++) {
                    Type expected = methodParameters.get(i).getType();
                    Type given = givenParameters.get(i);

                    if (!expected.equals(given)) {
                        this.reports.add(
                            new Report(
                                ReportType.ERROR,
                                Stage.SEMANTIC,
                                Integer.parseInt(node.get("LINE")),
                                Integer.parseInt(node.get("COLUMN")),
                                "Invalid type found. Expected \"" + typeToString(expected) + "\", found \"" + typeToString(given) + "\""
                            )
                        );
                    }
                }
            } else {
                this.reports.add(
                    new Report(
                        ReportType.ERROR,
                        Stage.SEMANTIC,
                        Integer.parseInt(node.get("LINE")),
                        Integer.parseInt(node.get("COLUMN")),
                        "Invalid parameters given. Expected " + methodParameters.size() + " parameters: \""
                            + methodParameters.stream().map(symbol -> this.typeToString(symbol.getType())).collect(Collectors.joining(", "))
                            + "\"; found " + givenParameters.size() + " parameters: \""
                            + givenParameters.stream().map(this::typeToString).collect(Collectors.joining(", ")) + "\""
                    )
                );
            }

            Type returnType = this.symbolTable.getReturnType(AnalysisTable.getMethodString(methodName, methodParameters.stream().map(Symbol::getType).collect(Collectors.toList())));
            node.put("RETURN", OllirUtils.typeToOllir(returnType));
            return returnType;
        }

        Type returnType = this.symbolTable.getReturnType(method);
        node.put("RETURN", OllirUtils.typeToOllir(returnType));
        return returnType;
    }

    private Type visitArray(JmmNode node, TypeNScope typeNScope) {
        JmmNode leftOperand = node.getChildren().get(0);
        JmmNode rightOperand = node.getChildren().get(1);

        Type leftType = visit(leftOperand, typeNScope);
        Type rightType = visit(rightOperand, typeNScope);

        if (!leftType.isArray()) {
            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("LINE")),
                    Integer.parseInt(node.get("COLUMN")),
                    "Invalid type found. Expected \"<type>[]\", found \"" + typeToString(leftType) + "\""
                )
            );
        }

        if (!rightType.equals(INT)) {
            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("LINE")),
                    Integer.parseInt(node.get("COLUMN")),
                    "Invalid type found. Expected \"" + typeToString(INT) + "\", found \"" + typeToString(rightType) + "\""
                )
            );
        }

        return new Type(leftType.getName(), false);
    }

    private Type visitIntReturnBool(JmmNode node, TypeNScope typeNScope) {
        checkTwoTypes(node, new TypeNScope(typeNScope.scope, INT, typeNScope.previousScope));

        return BOOL;
    }

    private Type visitBool(JmmNode node, TypeNScope typeNScope) {
        checkTwoTypes(node, new TypeNScope(typeNScope.scope, BOOL, typeNScope.previousScope));

        return BOOL;
    }

    private Type visitSingleInt(JmmNode node, TypeNScope typeNScope) {
        checkSingleType(node.getChildren().get(0), new TypeNScope(typeNScope.scope, INT, typeNScope.previousScope));

        return INT;
    }

    private Type visitSingleBool(JmmNode node, TypeNScope typeNScope) {
        checkSingleType(node.getChildren().get(0), new TypeNScope(typeNScope.scope, BOOL, typeNScope.previousScope));

        return BOOL;
    }

    private Type visitInt(JmmNode node, TypeNScope typeNScope) {
        checkTwoTypes(node, new TypeNScope(typeNScope.scope, INT, typeNScope.previousScope));

        return INT;
    }

    private Type visitVar(JmmNode node, TypeNScope typeNScope) {
        Symbol variable = this.symbolTable.getVariable(typeNScope.scope, node.get("VALUE"));

        if (variable == null) {
            variable = this.symbolTable.getVariable(AnalysisTable.CLASS_SCOPE, node.get("VALUE"));

            if (variable != null) {
                return variable.getType();
            }

            Set<String> imports = this.symbolTable.getImports().stream().map(this::getImportName).collect(Collectors.toSet());

            if (imports.contains(node.get("VALUE"))) {
                return new Type(node.get("VALUE"), false);
            }

            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("LINE")),
                    Integer.parseInt(node.get("COLUMN")),
                    "Variable \"" + node.get("VALUE") + "\" has not been initialized in this scope"
                )
            );

            return typeNScope.expected;
        }

        return variable.getType();
    }

    private Type returnInt(JmmNode node, TypeNScope typeNScope) {
        return INT;
    }

    private Type returnIntArray(JmmNode node, TypeNScope typeNScope) {
        defaultVisit(node, typeNScope);

        return INT_ARRAY;
    }

    private Type returnBool(JmmNode node, TypeNScope typeNScope) {
        return BOOL;
    }

    private Type returnLength(JmmNode node, TypeNScope typeNScope) {
        return LENGTH;
    }

    private Type returnThis(JmmNode node, TypeNScope typeNScope) {
        return new Type(this.symbolTable.getClassName(), false);
    }

    private void checkTwoTypes(JmmNode node, TypeNScope typeNScope) {
        JmmNode leftOperand = node.getChildren().get(0);
        JmmNode rightOperand = node.getChildren().get(1);

        Type leftType = visit(leftOperand, typeNScope);
        Type rightType = visit(rightOperand, typeNScope);

        if (!leftType.equals(typeNScope.expected) || !rightType.equals(typeNScope.expected)) {
            Type wrongType = leftType.equals(typeNScope.expected) ? rightType : leftType;

            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("LINE")),
                    Integer.parseInt(node.get("COLUMN")),
                    "Invalid type found on " + (wrongType == rightType ? "right" : "left")
                        + " side operand. Expected \"" + typeToString(typeNScope.expected) + "\", found \"" + typeToString(wrongType) + "\""
                )
            );
        }
    }

    private void checkSingleType(JmmNode node, TypeNScope typeNScope) {
        Type childType = visit(node, typeNScope);

        if (!childType.equals(typeNScope.expected)) {
            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("LINE")),
                    Integer.parseInt(node.get("COLUMN")),
                    "Invalid type found. Expected \"" + typeToString(typeNScope.expected) + "\", found \"" + typeToString(childType) + "\""
                )
            );
        }
    }

    private String getImportName(String importStatement) {
        int delimiterIndex = importStatement.lastIndexOf(AnalysisTableBuilder.DELIMITER);

        if (delimiterIndex == -1) {
            return importStatement;
        }

        return importStatement.substring(delimiterIndex + 1);
    }

    private Type defaultVisit(JmmNode node, TypeNScope typeNScope) {
        Type type = NULL;
        for (JmmNode child : node.getChildren()) {
            type = visit(child, typeNScope);
        }

        return type;
    }

    private String typeToString(Type type) {
        return type.getName() + (type.isArray() ? "[]" : "");
    }

    public List<Report> getReports() {
        return this.reports;
    }
}
