import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import org.specs.comp.ollir.*;
import pt.up.fe.comp.TestUtils;
import report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
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
    private String extendsDef;
    private Method currMethod;
    private List<Report> reports = new ArrayList<>();

    private int opLabel = 0;

    public static JasminResult run(OllirResult ollirResult) {
        // Checks input
        TestUtils.noErrors(ollirResult.getReports());

        return new BackendStage().toJasmin(ollirResult);
    }

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();
        StringBuilder jasminCode = new StringBuilder();

        this.reports = ollirResult.getReports();
        this.className = ollirClass.getClassName();
        this.extendsDef = ollirResult.getSymbolTable().getSuper();

        try {
            // Example of what you can do with the OLLIR class
            ollirClass.checkMethodLabels(); // check the use of labels in the OLLIR loaded
            ollirClass.buildCFGs(); // build the CFG of each method
            //ollirClass.outputCFGs(); // output to .dot files the CFGs, one per method
            ollirClass.buildVarTables(); // build the table of variables for each method
            //ollirClass.show(); // print to console main information about the input OLLIR
            // More reports from this stage

            jasminCode.append(this.generateClassDecl(ollirClass));
            jasminCode.append(this.generateClassFields(ollirClass));
            jasminCode.append(this.generateClassMethods(ollirClass));

            return new JasminResult(ollirResult, jasminCode.toString(), reports);

        } catch (Exception e) {
            return new JasminResult(ollirClass.getClassName(), null,
                Arrays.asList(Report.newError(Stage.GENERATION, "Exception during Jasmin generation", e)));
        }
    }

    private String generateClassDecl(ClassUnit ollirClass) {
        StringBuilder classCode = new StringBuilder();

        // Class: Definition
        classCode.append(".class public ")
            .append(ollirClass.getClassName())
            .append("\n");

        // Class: Extends
        classCode.append(".super ")
            .append(BackendStage.getSuper(this.extendsDef))
            .append("\n");

        // Class: Used to initialize a new instance of the class
        classCode.append(".method public <init>()V\n")
            .append("\taload_0\n")
            .append("\tinvokenonvirtual ")
            .append(BackendStage.getSuper(this.extendsDef))
            .append("/<init>()V\n")
            .append("\treturn\n")
            .append(".end method\n\n");

        return classCode.toString();
    }

    private String generateClassFields(ClassUnit ollirClass) {
        StringBuilder classFieldsCode = new StringBuilder();

        for(Field field: ollirClass.getFields()) {
            classFieldsCode.append(".field private ")
                .append(field.getFieldName())
                .append(" ")
                .append(BackendStage.getType(field.getFieldType()))
                .append("\n");
        }

        return classFieldsCode.append("\n").toString();
    }

    public String generateClassMethods(ClassUnit ollirClass) {
        StringBuilder classMethodsCode = new StringBuilder();

        for(Method method: ollirClass.getMethods()) {
            this.currMethod = method;

            // Main Declaration
            if(method.isStaticMethod() && method.getReturnType().getTypeOfElement() == ElementType.VOID) {
                classMethodsCode.append(".method public static main([Ljava/lang/String;)V\n")
                    .append("\n.limit stack 99\n")
                    .append(".limit locals 99\n")
                    .append(this.generateClassMethodBody(method.getInstructions(), method.getMethodName()))
                    .append("\nreturn\n");
            }

            // Regular Method Declaration
            else {
                classMethodsCode.append(String.format(".method public %s(", method.getMethodName()));

                for(Element param:  method.getParams()) {
                    classMethodsCode.append(BackendStage.getType(param.getType()));
                }

                classMethodsCode.append(")")
                    .append(BackendStage.getType(method.getReturnType()))
                    .append("\n.limit stack 99\n")
                    .append(".limit locals 99\n")
                    .append(this.generateClassMethodBody(method.getInstructions(), method.getMethodName()))
                    .append("\n")
                    .append(BackendStage.generateReturn(method.getReturnType()))
                    .append("\n");
            }

            classMethodsCode.append(".end method\n\n");
        }

        return classMethodsCode.toString();
    }

    private String generateClassMethodBody(List<Instruction> instructions, String methodName) {
        StringBuilder methodInstCode = new StringBuilder();

        for(Instruction instr: instructions) {
            methodInstCode.append(this.generateOperation(instr, methodName));
        }

        return methodInstCode.toString();
    }

    private String generateOperation(Instruction instr, String methodName) {
        if(instr.getInstType().equals(InstructionType.NOPER)) {
            return "";
        }

        //case RETURN: methodInstCode.append(""); break;
        switch (instr.getInstType()) {
            case ASSIGN -> {
                AssignInstruction assign = (AssignInstruction) instr;
                Element dest = assign.getDest();
                if(dest.isLiteral()) {
                    // ?? converter par aliteral e dps?
                    String identifier = ((LiteralElement) dest).getLiteral();
                }

                ElementType elemType = dest.getType().getTypeOfElement();
                Instruction rhs = assign.getRhs();
                StringBuilder builder = new StringBuilder();

                String aux = this.generateOperation(rhs, methodName);

                /*switch (elemType) {
                    case ARRAYREF: {
                        builder.append(this.generateOperation(assign.getRhs(), methodName));

                        //builder.append("iastore\n");
                        return builder.toString();
                    }

                    case BOOLEAN: {
                        builder.append(this.generateOperation(assign.getRhs(), methodName));
                        return builder.toString();
                    }

                    case INT32: {
                        builder.append(this.generateOperation(assign.getRhs(), methodName));
                        return builder.toString();
                    }

                    case CLASS: {
                        return builder.toString();
                    }

                    case THIS: {
                        return builder.toString();
                    }

                    case VOID: {
                        return builder.toString();
                    }

                    case OBJECTREF: {
                        return builder.toString();
                    }
                }*/


                break;
            }

            case BINARYOPER -> {
                return generateBinaryOp((BinaryOpInstruction) instr);
            }

            case BRANCH -> {
                CondBranchInstruction condBranch = (CondBranchInstruction) instr;
                Element left = condBranch.getLeftOperand();
                Element right = condBranch.getRightOperand();
                String label = condBranch.getLabel();
                Operation operation = condBranch.getCondOperation();
                break;
            }

            case CALL -> {
                return generateCallOp((CallInstruction) instr, methodName);
            }

            case GETFIELD -> {
                return generateGetFieldOp((GetFieldInstruction) instr);
            }

            case GOTO -> {
                return generateGotoOp((GotoInstruction) instr);
            }

            case PUTFIELD -> {
                return generatePutFieldOp((PutFieldInstruction) instr);
            }

            case UNARYOPER -> {
                return generateUnaryOp((UnaryOpInstruction) instr);
            }

            case RETURN -> {
                ReturnInstruction returnInstr = (ReturnInstruction) instr;

                if(returnInstr.hasReturnValue()) {
                    Element operand = returnInstr.getOperand();
                    if(operand.isLiteral()) {
                        LiteralElement ret = (LiteralElement) operand;
                    } else {
                        Operand ret = (Operand) operand;
                    }

                }

                return switch (returnInstr.getElementType()) {
                    case INT32 -> "ireturn";
                    case BOOLEAN -> "ireturn";
                    default -> "areturn";
                };

            }
        }

        return "";
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
                String labelTrue = "LABEL " + this.opLabel++;
                String labelContinue = "LABEL " + this.opLabel++;

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

    private String generateCallOp(CallInstruction instr, String methodName) {
        Element first = instr.getFirstArg();
        Element second = instr.getSecondArg();
        List<Element> operands = instr.getListOfOperands();
        CallType invocationType = OllirAccesser.getCallInvocation(instr);

        switch (invocationType) {
            //case ldc -> break;
            case NEW -> {
                StringBuilder builder = new StringBuilder();

                if(first.getType().getTypeOfElement() == ElementType.OBJECTREF) {
                    builder.append("new ")
                        .append(((Operand)first).getName())
                        .append("\ndup\n");
                } else if(first.getType().getTypeOfElement() == ElementType.ARRAYREF) {
                    if(operands.size() > 0) {
                        Element elem = operands.get(0);
                        builder.append(generateElement(elem)).append("\n");
                        builder.append("\nnewarray int\n");
                        return builder.toString();
                    }
                }

                return "";
            }

            case arraylength -> {
                return "arraylength";
            }

            case invokespecial -> {
                StringBuilder builder = new StringBuilder();

                builder.append(invocationType.toString())
                    .append(" ");

                if(first.getType().getTypeOfElement() == ElementType.THIS) {
                    builder.append(this.className).append("/");
                }

                builder.append("<init>(");

                for(Element param: instr.getListOfOperands()) {
                    builder.append(BackendStage.getType(param.getType()));
                }

                builder.append(")")
                    .append(BackendStage.getType(instr.getReturnType()));

                return builder.toString();
            }

            case invokevirtual,
                invokestatic -> {
                StringBuilder builder = new StringBuilder();

                builder.append(invocationType.toString())
                    .append(" ");

                if(first.getType().getTypeOfElement() == ElementType.THIS) {
                    builder.append(this.className);
                } else if(first.getType().getTypeOfElement() == ElementType.CLASS) {
                    builder.append(((Operand)first).getName());
                } else {
                    return "";
                }

                builder.append(".")
                    .append(((LiteralElement)second).getLiteral().replace("\"", ""))
                    .append("(");

                for(Element param: instr.getListOfOperands()) {
                    var a = OllirAccesser.getVarTable(this.currMethod);
                    builder.append(BackendStage.getType(param.getType()));
                }

                builder.append(") ")
                    .append(BackendStage.getType(instr.getReturnType()))
                    .append("\n");

                return builder.toString();
            }
        }

        return "";
    }

    private String generateGetFieldOp(GetFieldInstruction instr) {
        Element fieldElem = instr.getFirstOperand();
        String field = ((Operand) fieldElem).getName();
        Element second = instr.getSecondOperand();

        StringBuilder builder = new StringBuilder();
        Descriptor descriptor = OllirAccesser.getVarTable(this.currMethod).get(field);

        switch (descriptor.getScope()) {
            case FIELD -> {
                builder.append("aload_0\n")
                    .append("getfield ")
                    .append(this.className)
                    .append("/")
                    .append(field)
                    .append(" ")
                    .append(getType(second.getType()))
                    .append("\n");
            }

            case PARAMETER, LOCAL -> {
                builder.append("aload_\n")
                    .append(descriptor.getVirtualReg())
                    .append("getfield ")
                    .append(this.className)
                    .append("/")
                    .append(field)
                    .append(" ")
                    .append(getType(second.getType()))
                    .append("\n");
            }
        }

        return builder.toString();
    }

    private String generateGotoOp(GotoInstruction instr) {
        return "goto " + instr.getLabel() + "\n";
    }

    private String generatePutFieldOp(PutFieldInstruction instr) {
        /*Element fieldElem = instr.getSecondOperand();
        String field = ((Operand) fieldElem).getName();
        Element first = instr.getFirstOperand();
        Element third = instr.getThirdOperand();

        if(true < false)

        StringBuilder builder = new StringBuilder();
        builder.append("aload_0\n")
            .append("putfield ")
            .append(this.className)
            .append("/")
            .append(field)
            .append(" ")
            .append(getType(third.getType()))
            .append("\n");

        return builder.toString();*/
        return "";
    }

    private String generateUnaryOp(UnaryOpInstruction instr) { ;
        OperationType op = instr.getUnaryOperation().getOpType();

        if (op == OperationType.NOTB) {
            StringBuilder builder = new StringBuilder();
            String labelTrue = "LABEL " + this.opLabel++;
            String labelContinue = "LABEL " + this.opLabel++;

            builder.append("ifeq  ").append(labelTrue).append("\n");
            builder.append("iconst_0\n");
            builder.append("goto ").append(labelContinue).append("\n");
            builder.append(labelTrue).append(":\n");
            builder.append("iconst_1\n");
            builder.append(labelContinue).append(":\n");
            return builder.toString();
        }

        return "";
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
                return "bipush " + literal.getLiteral();
            }

            if(value >= -32768 && value <= 32767) {
                return "sipush " + literal.getLiteral();
            }

            return "ldc " + literal.getLiteral();
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

    /*private String getVar(String var) {
        Descriptor descriptor = OllirAccesser.getVarTable(this.currMethod).get(var);
    }*/

    private static String getSuper(String extendsDef) {
        return extendsDef == null ? "java/lang/Object" : extendsDef;
    }

    private static String getType(Type type) {
        return switch (type.getTypeOfElement()) {
            case ARRAYREF -> "[I";
            case INT32 -> "I";
            case BOOLEAN -> "Z";
            case VOID -> "V";
            default -> throw new IllegalStateException("Unexpected value: " + type.getTypeOfElement());
        };
    }
}
