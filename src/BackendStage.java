import analysis.table.SymbolTable;
import jasmin.JasminBackend;
import jasmin.JasminResult;
import ollir.OllirResult;
import org.specs.comp.ollir.*;
import pt.up.fe.comp.TestUtils;
import report.Report;
import report.Stage;

import java.util.Arrays;
import java.util.List;

import org.specs.comp.ollir.ElementType;

import report.ReportType;

/**
 * JASMIN Instructions
 * http://jasmin.sourceforge.net/instructions.html
 * http://jasmin.sourceforge.net/guide.html
 * http://www2.cs.uidaho.edu/~jeffery/courses/445/code-jasmin.html
 * http://web.mit.edu/javadev/packages/jasmin/doc/
 * http://www.ist.tugraz.at/_attach/Publish/Cb/Tutorial_CodeGeneration_2017.pdf
 * http://www.cs.sjsu.edu/faculty/pearce/modules/lectures/co/jvm/jasmin/data.html --------> Jasmin/JVM Data Types
 * http://www.cs.sjsu.edu/faculty/pearce/modules/lectures/co/jvm/jasmin/demos/demos.html
 * https://saksagan.ceng.metu.edu.tr/courses/ceng444/link/f3jasmintutorial.html
 */
public class BackendStage implements JasminBackend {
    private String className;
    private int labelNo = 0;

    public static JasminResult run(OllirResult ollirResult) {
        // Checks input
        TestUtils.noErrors(ollirResult.getReports());

        return new BackendStage().toJasmin(ollirResult);
    }

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();
        this.className = ollirClass.getClassName();

        try {
            // Example of what you can do with the OLLIR class
            ollirClass.checkMethodLabels(); // check the use of labels in the OLLIR loaded
            ollirClass.buildCFGs(); // build the CFG of each method
            ollirClass.outputCFGs(); // output to .dot files the CFGs, one per method
            ollirClass.buildVarTables(); // build the table of variables for each method
            ollirClass.show(); // print to console main information about the input OLLIR
            // More reports from this stage
            List<Report> reports = ollirResult.getReports();

            StringBuilder jasminCode = new StringBuilder();
            jasminCode.append(BackendStage.generateClassDecl(ollirClass, ollirResult.getSymbolTable(), reports));
            jasminCode.append(BackendStage.generateClassFields(ollirClass, ollirResult.getSymbolTable(), reports));
            jasminCode.append(BackendStage.generateClassMethods(ollirClass, ollirResult.getSymbolTable(), reports));

            // TODO: duvidas -> Temos de ver stack limits etc?
            // TODO: duvidas -> Perceber o L data type


            return new JasminResult(ollirResult, jasminCode.toString(), reports);

        } catch (OllirErrorException e) {
            return new JasminResult(ollirClass.getClassName(), null,
                Arrays.asList(Report.newError(Stage.GENERATION, "Exception during Jasmin generation", e)));
        }
    }

    private static String generateClassDecl(ClassUnit ollirClass, SymbolTable table, List<Report> reports) {
        StringBuilder classCode = new StringBuilder();

        // Class: Error Checking
        if(!ollirClass.getClassAccessModifier().equals(AccessModifiers.PUBLIC)) {
            reports.add(new Report(ReportType.ERROR, Stage.GENERATION, "Only public classes can exist!"));
        }

        // Class: Definition
        classCode.append(String.format(".class public %s\n", ollirClass.getClassName()));

        // Class: Extends
        classCode.append(String.format(".super %s\n",
                BackendStage.getSuper(table.getSuper())));

        // Class: Used to initialize a new instance of the class
        classCode.append(".method public <init>()V\n");
        classCode.append("aload_0\n");
        classCode.append(String.format("invokenonvirtual %s\n",
                BackendStage.getSuper(table.getSuper())));
        classCode.append("return\n");
        classCode.append(".end method\n\n");

        return classCode.toString();
    }

    private static String generateClassFields(ClassUnit ollirClass, SymbolTable table, List<Report> reports) {
        StringBuilder classFieldsCode = new StringBuilder();

        for(Field field: ollirClass.getFields()) {
            // Class Fields: Error Checking
            if(!field.getFieldAccessModifier().equals(AccessModifiers.PRIVATE)) {
                reports.add(new Report(ReportType.ERROR, Stage.GENERATION, "Only private class fields can exist!"));
            }

            // Class Fields: Assembling
            classFieldsCode.append(String.format(".field private %s %s", field.getFieldName(),
                    BackendStage.getType(field.getFieldType(), ollirClass.getClassName())));
            if(field.isInitialized()) {
                classFieldsCode.append(String.format(" = %s", field.getInitialValue()));
            }

            classFieldsCode.append("\n");
        }

        return classFieldsCode.append("\n").toString();
    }

    public static String generateClassMethods(ClassUnit ollirClass, SymbolTable table, List<Report> reports) {
        StringBuilder classMethodsCode = new StringBuilder();

        for(Method method: ollirClass.getMethods()) {
            if(!method.getMethodAccessModifier().equals(AccessModifiers.PUBLIC)) {
                reports.add(new Report(ReportType.ERROR, Stage.GENERATION, "Only public class methods can exist!"));
            }

            if(method.isConstructMethod()) {
                reports.add(new Report(ReportType.ERROR, Stage.GENERATION, "There cannot exist a construct method!"));
            }

            // Main Declaration
            if(method.isStaticMethod() && method.getReturnType().getTypeOfElement().equals(ElementType.VOID)) {
                classMethodsCode.append(".method public static main([Ljava/lang/String;)V\n");
                if(!(method.getInstructions().size() > 0)) {
                    reports.add(new Report(ReportType.ERROR, Stage.GENERATION, "Main needs to have a body!"));
                }

                classMethodsCode.append(String.format("%s\n", BackendStage.generateClassMethodBody(method.getInstructions(), table)));
                classMethodsCode.append(String.format("%s\n", BackendStage.generateReturn(method.getReturnType())));
            }

            // Regular Method Declaration
            else {
                classMethodsCode.append(String.format(".method public %s(", method.getMethodName()));
                for(Element param:  method.getParams()) {
                    classMethodsCode.append(BackendStage.getType(method.getReturnType(), ollirClass.getClassName()));
                    param.isLiteral(); // TODO: What should we do with this ?
                }
                classMethodsCode.append(String.format(")%s\n",
                    BackendStage.getType(method.getReturnType(), ollirClass.getClassName())));
                classMethodsCode.append(String.format("%s\n", BackendStage.generateClassMethodBody(method.getInstructions(), table)));
            }

            classMethodsCode.append(".end method\n\n");
        }

        return classMethodsCode.toString();
    }

    private static String generateClassMethodBody(List<Instruction> instructions, SymbolTable table) {
        StringBuilder methodInstCode = new StringBuilder();


        for(Instruction instr: instructions) {
            switch (instr.getInstType()) {
                case ASSIGN: break;
                case BINARYOPER: break;
                case BRANCH: break;
                case CALL: break;
                case GETFIELD: break;
                case GOTO: break;
                case NOPER: break;
                case PUTFIELD: break;
                case RETURN: methodInstCode.append(""); break;
                case UNARYOPER: break;
            }
        }

        return methodInstCode.append("\n").toString();
    }

    private static String generateReturn(Type type) {
        return switch (type.getTypeOfElement()) {
            case INT32 -> "ireturn";
            case BOOLEAN -> "ireturn";
            default -> "areturn";
        };
    }

    private static String getSuper(String extendsDef) {
        return extendsDef == null ? "java/lang/Object" : extendsDef;
    }

    // TODO: Review this
    private static String getType(Type type, String className) {
        return switch (type.getTypeOfElement()) {
            case ARRAYREF -> "[I";
            case INT32 -> "I";
            case BOOLEAN -> "Z";
            case VOID -> "V";
            case CLASS -> String.format("L  %s", className); // TODO: understand better this value, where can it be used?
            default -> throw new IllegalStateException("Unexpected value: " + type.getTypeOfElement());
        };
    }
}
