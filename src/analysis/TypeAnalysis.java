package analysis;

import analysis.table.AnalysisTable;
import analysis.table.Method;
import analysis.table.Symbol;
import analysis.table.Type;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import report.Report;
import report.ReportType;
import report.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TypeAnalysis extends AJmmVisitor<TypeAnalysis.TypeNScope, Type> {
    public final static Type INT = new Type("int", false);
    public final static Type INT_ARRAY = new Type("int", true);
    public final static Type BOOL = new Type("boolean", false);
    public final static Type LENGTH = new Type("length", false);

    private final AnalysisTable symbolTable;
    private final List<Report> reports;

    protected static class TypeNScope {
        final private Method scope;
        final private Type expected;
        final private Method previousScope;

        public TypeNScope(Method scope, Type expected, Method previousScope) {
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

    private Type visitClass(JmmNode jmmNode, TypeNScope typeNScope) {
        return defaultVisit(jmmNode, new TypeNScope(this.symbolTable.getClassMethod(), null, null));
    }

    private Type visitObject(JmmNode node, TypeNScope typeNScope) {
        JmmNode nameNode = node.getChildren().get(0);

        return new Type(nameNode.get("VALUE"), false);
    }

    private Type visitMethod(JmmNode node, TypeNScope typeNScope) {
        JmmNode returnType = node.getChildren().get(0);
        JmmNode name = node.getChildren().get(1);

        List<Symbol> parameters = new ArrayList<>();
        Symbol methodSymbol = new Symbol(getType(returnType), name.get("VALUE"));

        if(node.getChildren().size() >= 3 && node.getChildren().get(2).getKind().equals("MethodParameters")) {
            this.fillMethodParameters(node.getChildren().get(2), parameters);
        }

        List<Type> parametersType = parameters.stream().map(Symbol::getType).collect(Collectors.toList());
        TypeNScope scope = new TypeNScope(this.symbolTable.getMethod(name.get("VALUE"), parametersType), null, typeNScope.scope);

        for (JmmNode child : node.getChildren()){
            visit(child, scope);
        }

        return getType(returnType);
    }

    private Type visitMain(JmmNode node, TypeNScope typeNScope) {
        JmmNode params = node.getChildren().get(0);
        Symbol methodSymbol = new Symbol(new Type("void", false), AnalysisTable.MAIN_SCOPE);

        List<Symbol> parameters = new ArrayList<>();

        this.fillMethodParameters(params, parameters);

        List<Type> parametersType = parameters.stream().map(Symbol::getType).collect(Collectors.toList());

        TypeNScope scope = new TypeNScope(this.symbolTable.getMethod(AnalysisTable.MAIN_SCOPE, parametersType), null, typeNScope.scope);

        for (JmmNode child : node.getChildren()){
            visit(child, scope);
        }

        return null;
    }

    private Type visitConditionExpression(JmmNode node, TypeNScope typeNScope) {
        JmmNode conditionalNode = node.getChildren().get(0);

        checkSingleType(conditionalNode, new TypeNScope(typeNScope.scope, BOOL, typeNScope.scope));

        for (int i = 1; i < node.getNumChildren(); i++) {
            visit(node.getChildren().get(i), typeNScope);
        }

        return null;
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
                    this.symbolTable.getClassMethod() :
                    new Method(new Symbol(leftType, ""), new ArrayList<>()),
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
                    "Invalid type found. Expected \"<class>\" or \"this\", found \"" + leftType + "\""
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
                    "Invalid type found. Expected \"<any_type>[]\", found \"" + rightType + "\""
                )
            );
        }

        return leftType.getName().equals(symbolTable.getClassName()) ? rightType : (leftType.isArray() && LENGTH.equals(rightType) ? INT : rightType);
    }

    private Type visitMethodCall(JmmNode node, TypeNScope typeNScope) {
        JmmNode methodNameNode = node.getChildren().get(0);
        JmmNode paramsNode = node.getChildren().get(1);

        String methodName = methodNameNode.get("VALUE");

        TypeNScope newTypeNScope = new TypeNScope(typeNScope.previousScope, null, null);
        List<Type> givenParameters = paramsNode.getChildren().stream().map((child) -> this.visit(child, newTypeNScope)).collect(Collectors.toList());
        if(methodName.equals("quicksort"))
            System.out.println(givenParameters.stream().map(Type::toString).collect(Collectors.joining(", ")));

        Method method = this.symbolTable.getMethod(methodName, givenParameters);

        // check if method isn't from class
        if (method == null) {
            if (this.symbolTable.getClassMethod().equals(typeNScope.scope) && this.symbolTable.getSuper() != null) {
                return typeNScope.expected;
            }

            List<String> imports = this.symbolTable.getImports();
            boolean isImportedMethod = false;

            for (String importName : imports) {
                isImportedMethod |= importName.equals(typeNScope.scope.getMethodSymbol().getType().getName()) || importName.equals(methodName);
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

            return typeNScope.expected;
        }

        if (method.getParameters().size() != givenParameters.size()) {
            if (this.symbolTable.getClassMethod().equals(typeNScope.scope) && this.symbolTable.getSuper() != null) {
                return typeNScope.expected;
            }

            this.reports.add(
                new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("LINE")),
                    Integer.parseInt(node.get("COLUMN")),
                    "Invalid parameters given. Expected " + method.getParameters().size() + " parameters: \""
                        + method.getParameters().stream().map(Type::toString).collect(Collectors.joining(", "))
                        + "\"; found " + givenParameters.size() + " parameters: \""
                        + givenParameters.stream().map(Type::toString).collect(Collectors.joining(", ")) + "\""
                )
            );

            return method.getMethodSymbol().getType();
        }

        for (int i = 0; i < method.getParameters().size(); i++) {
            Type expected = method.getParameters().get(i);
            Type given = givenParameters.get(i);

            if (!expected.equals(given)) {
                this.reports.add(
                    new Report(
                        ReportType.ERROR,
                        Stage.SEMANTIC,
                        Integer.parseInt(node.get("LINE")),
                        Integer.parseInt(node.get("COLUMN")),
                        "Invalid type found. Expected \"" + expected + "\", found \"" + given + "\""
                    )
                );
            }
        }

        return method.getMethodSymbol().getType();
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
                    "Invalid type found. Expected \"<type>[]\", found \"" + leftType + "\""
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
                    "Invalid type found. Expected \"" + INT + "\", found \"" + rightType + "\""
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
            variable = this.symbolTable.getVariable(this.symbolTable.getClassMethod(), node.get("VALUE"));

            if (variable != null) {
                return variable.getType();
            }

            if (this.symbolTable.getImports().contains(node.get("VALUE"))) {
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
                        + " side operand. Expected \"" + typeNScope.expected + "\", found \"" + wrongType + "\""
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
                    "Invalid type found. Expected \"" + typeNScope.expected + "\", found \"" + childType + "\""
                )
            );
        }
    }

    private Type getType(JmmNode node) {
        if (node.getKind().equals("Array")) {
            return new Type(node.getChildren().get(0).get("VALUE"), true);
        }

        return new Type(node.get("VALUE"), false);
    }

    private void fillMethodParameters(JmmNode node, List<Symbol> parameters) {
        for(JmmNode child: node.getChildren()) {
            JmmNode firstChild = child.getChildren().get(0);
            JmmNode secondChild = child.getChildren().get(1);

            parameters.add(new Symbol(getType(firstChild), secondChild.get("VALUE")));
        }
    }

    private Type defaultVisit(JmmNode node, TypeNScope typeNScope) {
        Type type = null;
        for (JmmNode child : node.getChildren()) {
            type = visit(child, typeNScope);
        }

        return type;
    }

    public List<Report> getReports() {
        return this.reports;
    }
}
