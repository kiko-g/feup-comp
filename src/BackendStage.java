import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import org.specs.comp.ollir.*;
import pt.up.fe.comp.TestUtils;
import report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.Arrays;
import java.util.List;

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

    private int ltLabel = 0;

    public static JasminResult run(OllirResult ollirResult) {
        // Checks input
        TestUtils.noErrors(ollirResult.getReports());

        return new BackendStage().toJasmin(ollirResult);
    }

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();
        this.className = ollirClass.getClassName();
        StringBuilder jasminCode = new StringBuilder();

        try {
            // Example of what you can do with the OLLIR class
            ollirClass.checkMethodLabels(); // check the use of labels in the OLLIR loaded
            ollirClass.buildCFGs(); // build the CFG of each method
            //ollirClass.outputCFGs(); // output to .dot files the CFGs, one per method
            ollirClass.buildVarTables(); // build the table of variables for each method
            //ollirClass.show(); // print to console main information about the input OLLIR
            // More reports from this stage
            List<Report> reports = ollirResult.getReports();


            jasminCode.append(BackendStage.generateClassDecl(ollirClass, ollirResult.getSymbolTable(), reports));
            jasminCode.append(BackendStage.generateClassFields(ollirClass, ollirResult.getSymbolTable(), reports));
            System.out.println(jasminCode);

            jasminCode.append(this.generateClassMethods(ollirClass, ollirResult.getSymbolTable(), reports));

            // TODO: duvidas -> Temos de ver stack limits etc?
            // TODO: duvidas -> Perceber o L data type
            // TODO: add initial constructor


            return new JasminResult(ollirResult, jasminCode.toString(), reports);

        } catch (Exception e) {
            return new JasminResult(ollirClass.getClassName(), null,
                Arrays.asList(Report.newError(Stage.GENERATION, "Exception during Jasmin generation", e)));
        }
    }

    private static String generateClassDecl(ClassUnit ollirClass, SymbolTable table, List<Report> reports) {
        StringBuilder classCode = new StringBuilder();

        // Class: Error Checking
        if(!ollirClass.getClassAccessModifier().equals(AccessModifiers.DEFAULT)) {
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
                    BackendStage.getType(field.getFieldType())));
            if(field.isInitialized()) {
                classFieldsCode.append(String.format(" = %s", field.getInitialValue()));
            }

            classFieldsCode.append("\n");
        }

        return classFieldsCode.append("\n").toString();
    }

    public String generateClassMethods(ClassUnit ollirClass, SymbolTable table, List<Report> reports) {
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

                classMethodsCode.append(String.format("%s\n", this.generateClassMethodBody(method.getInstructions(), table)));
                classMethodsCode.append(String.format("%s\n", BackendStage.generateReturn(method.getReturnType())));
            }

            // Regular Method Declaration
            else {
                classMethodsCode.append(String.format(".method public %s(", method.getMethodName()));
                for(Element param:  method.getParams()) {
                    classMethodsCode.append(BackendStage.getType(method.getReturnType()));
                    param.isLiteral(); // TODO: What should we do with this ?
                }
                classMethodsCode.append(String.format(")%s\n",
                    BackendStage.getType(method.getReturnType())));
                classMethodsCode.append(String.format("%s\n", this.generateClassMethodBody(method.getInstructions(), table)));
            }

            classMethodsCode.append(".end method\n\n");
        }

        return classMethodsCode.toString();
    }

    private String generateClassMethodBody(List<Instruction> instructions, SymbolTable table) {
        StringBuilder methodInstCode = new StringBuilder();

        for(Instruction instr: instructions) {
            methodInstCode.append(this.generateOperation(instr));
        }

        return methodInstCode.append("\n\n").toString();
    }

    private String generateOperation(Instruction instr) {
        if(instr.getInstType().equals(InstructionType.NOPER)) {
            return "";
        }

        switch (instr.getInstType()) {
            case ASSIGN: {
                AssignInstruction assign = (AssignInstruction) instr;
                Element elem = assign.getDest();
                
                ElementType elemType = elem.getType().getTypeOfElement();
                String identifier = ((LiteralElement) elem).getLiteral();

                StringBuilder builder = new StringBuilder();
                switch (elemType) {
                    case ARRAYREF: {



                        builder.append("iastore\n");
                    }

                    case BOOLEAN: {

                    }

                    case INT32: {

                    }

                    default: {

                        builder.append("aload_0\n");
                    }
                }


                break;
            }

            case BINARYOPER: {
                return generateBinaryOp((BinaryOpInstruction) instr);
            }

            case BRANCH: {
                CondBranchInstruction condBranch = (CondBranchInstruction) instr;
                Element left = condBranch.getLeftOperand();
                Element right = condBranch.getRightOperand();
                String label = condBranch.getLabel();
                Operation operation = condBranch.getCondOperation();
                break;
            }

            case CALL: {
                CallInstruction call = (CallInstruction) instr;
                Element first = call.getFirstArg();
                Element second = call.getSecondArg();
                Type returnType = call.getReturnType();
                List<Element> operands = call.getListOfOperands();
                break;
            }

            case GETFIELD: {
                GetFieldInstruction getField = (GetFieldInstruction) instr;
                Element first = getField.getFirstOperand();
                Element second = getField.getSecondOperand();
                break;
            }

            case GOTO: {
                return generateGotoOp((GotoInstruction) instr);
            }

            case PUTFIELD: {
                return generatePutFieldOp((PutFieldInstruction) instr);
            }

            case UNARYOPER: {
                UnaryOpInstruction unaryOp = (UnaryOpInstruction) instr;
                Element right = unaryOp.getRightOperand();
                OperationType op = unaryOp.getUnaryOperation().getOpType();
                break;
            }

            //case RETURN: methodInstCode.append(""); break;
        }

        return "";
    }

    private String generateGotoOp(GotoInstruction instr) {
        return "goto " + instr.getLabel() + "\n";
    }

    private String generatePutFieldOp(PutFieldInstruction instr) {
        Element fieldElem = instr.getFirstOperand();
        String field = ((Operand) fieldElem).getName();
        Element second = instr.getSecondOperand();

        StringBuilder builder = new StringBuilder();
        builder.append("aload_0\n")
            .append("putfield ")
            .append(this.className)
            .append("/")
            .append(field)
            .append(" ")
            .append(getType(second.getType()))
            .append("\n");

        return builder.toString();
    }

    private String generateBinaryOp(BinaryOpInstruction instr) {
        Element leftElem = instr.getLeftOperand();
        Element rightElem = instr.getRightOperand();

        String left = generateElement(leftElem);
        String right = generateElement(rightElem);

        OperationType op = instr.getUnaryOperation().getOpType();

        switch (op) {
            case ANDI32 -> {
                return left + right + "iand\n";
            }

            case LTHI32 -> {
                StringBuilder builder = new StringBuilder();
                String labelTrue = "LABEL " + this.ltLabel++;
                String labelContinue = "LABEL " + this.ltLabel++;

                builder.append("if_icmplt ").append(labelTrue).append("\n");
                builder.append("iconst_0\n");
                builder.append("goto ").append(labelContinue).append("\n");
                builder.append(labelTrue).append(":\n");
                builder.append("iconst_1\n");
                builder.append(labelContinue).append(":\n");
                return builder.toString();
            }

            case ADDI32 -> {
                return left + right + "iadd\n";
            }

            case SUBI32 -> {
                return left + right + "isub\n";
            }

            case MULI32 -> {
                return left + right + "imul\n";
            }

            case DIVI32 -> {
                return left + right + "idiv\n";
            }
        }
        return left;
    }

    private static String generateElement(Element elem) {
        if (elem instanceof LiteralElement) {
            LiteralElement literal =(LiteralElement) elem;

            int value = Integer.parseInt(literal.getLiteral());

            if(value == -1) {
                return "iconst_m1";
            }

            if(value >= 0 && value <= 5) {
                return "iconst_" + literal.getLiteral();
            }

            if(value >= -128 && value <= 127) {
                return "bipush" + literal.getLiteral();
            }

            if(value >= -32768 && value <= 32767) {
                return "sipush" + literal.getLiteral();
            }

            return "ldc" + literal.getLiteral();
        }

        // TODO: check if array is only for unary operation
        /*if (elem instanceof ArrayOperand) {
            ArrayOperand arrayOperand = (ArrayOperand) elem;
            String type = arrayOperand.
        }*/

        if(elem instanceof Operand) {
            Operand operand = (Operand) elem;
            var pos = 3;
            return "iload_" + pos;
        }

        return "";
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
    private static String getType(Type type) {
        return switch (type.getTypeOfElement()) {
            case ARRAYREF -> "[I";
            case INT32 -> "I";
            case BOOLEAN -> "Z";
            case VOID -> "V";
            //case CLASS -> String.format("L  %s", className); // TODO: understand better this value, where can it be used?
            default -> throw new IllegalStateException("Unexpected value: " + type.getTypeOfElement());
        };
    }
}
