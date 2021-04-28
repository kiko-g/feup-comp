import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import org.specs.comp.ollir.*;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.report.ReportType;
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
    private String extendsDef = null;
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
            ollirClass.checkMethodLabels();
            ollirClass.buildCFGs();
            ollirClass.buildVarTables();

            jasminCode.append(this.generateClassDecl(ollirClass));
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
            .append("\n\n");

        // Class: Fields
        classCode.append(this.generateClassFields(ollirClass));

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
            classFieldsCode.append("\t.field private ")
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

            if(method.getMethodName().equals("main")) {
                classMethodsCode.append("\t.method public static main([Ljava/lang/String;)V\n")
                    .append("\t\t.limit stack 99\n")
                    .append("\t\t.limit locals 99\n")
                    .append(this.generateClassMethodBody(method.getInstructions()))
                    .append("\n\t\treturn\n");
            }

            else {
                classMethodsCode.append(String.format("\t.method public %s(", method.getMethodName()));

                for(Element param:  method.getParams()) {
                    classMethodsCode.append(BackendStage.getType(param.getType()));
                }

                classMethodsCode.append(")")
                    .append(BackendStage.getType(method.getReturnType()))
                    .append("\n\t\t.limit stack 99\n")
                    .append("\t\t.limit locals 99\n")
                    .append(this.generateClassMethodBody(method.getInstructions()))
                    .append("\n");

                if(method.getReturnType().getTypeOfElement() == ElementType.VOID) {
                    classMethodsCode.append("\t\treturn\n");
                }
            }

            classMethodsCode.append("\t.end method\n\n");
        }

        return classMethodsCode.toString();
    }

    private String generateClassMethodBody(List<Instruction> instructions) {
        StringBuilder methodInstCode = new StringBuilder();

        for(Instruction instr: instructions) {
            methodInstCode.append(this.generateOperation(instr));
        }

        return methodInstCode.toString();
    }

    private String generateOperation(Instruction instr) {
        return switch (instr.getInstType()) {
            case NOPER -> "\t\t" + generateLoad(((SingleOpInstruction) instr).getSingleOperand());
            //case ASSIGN -> generateAssign((AssignInstruction) instr);
            case BINARYOPER -> generateBinaryOp((BinaryOpInstruction) instr);
            case UNARYOPER -> generateUnaryOp((UnaryOpInstruction) instr);
            //case CALL -> generateCallOp((CallInstruction) instr);
            //case GETFIELD -> generateGetFieldOp((GetFieldInstruction) instr);
            //case PUTFIELD -> generatePutFieldOp((PutFieldInstruction) instr);
            case GOTO -> generateGotoOp((GotoInstruction) instr);
            case RETURN -> generateReturnOp((ReturnInstruction) instr);



            /*case BRANCH -> {
                CondBranchInstruction condBranch = (CondBranchInstruction) instr;
                Element left = condBranch.getLeftOperand();
                Element right = condBranch.getRightOperand();
                String label = condBranch.getLabel();
                Operation operation = condBranch.getCondOperation();
                break;
            } */
            default -> "";
        };
    }

    private String generateLoad(Element elem) {
        if(!elem.isLiteral()) {
            Descriptor descriptor = OllirAccesser.getVarTable(this.currMethod).get(((Operand) elem).getName());

            if(descriptor.getScope() == VarScope.FIELD) {
                Instruction getfield = new GetFieldInstruction(
                        new Element(new Type(ElementType.THIS)),
                        new Element(descriptor.getVarType()),
                        descriptor.getVarType()
                );
                return generateOperation(getfield);
            } else {
                return switch (descriptor.getVarType().getTypeOfElement()) {
                    case INT32, BOOLEAN -> "iload_" + descriptor.getVirtualReg();
                    case THIS -> "aload_0";
                    case ARRAYREF, CLASS, OBJECTREF -> "aload_" + descriptor.getVirtualReg();
                    default -> "";
                };
            }
        } else {
            String literal = ((LiteralElement) elem).getLiteral();

            try {
                int value = Integer.parseInt(literal);
                if(value == -1) return "iconst_m1";
                if(value >= 0 && value <= 5) return "iconst_" + value;
                if(value >= -128 && value <= 127) return "bipush " + value;
                if(value >= -32768 && value <= 32767) return "sipush " + value;
                return "ldc " + value;
            } catch (NumberFormatException ignored) {
                this.reports.add(new Report(ReportType.ERROR, Stage.GENERATION, "Literal" + literal + "is not an integer!"));
            }
        }

        return "";
    }

    private String generateAssign(AssignInstruction instr) {
        Element assignDest = instr.getDest();

        String destName;
        if(assignDest.isLiteral()) {
            LiteralElement dest = (LiteralElement) assignDest;
            destName = dest.getLiteral();
        } else {
            Operand dest = (Operand) assignDest;
            destName = dest.getName();
        }

        Descriptor descriptor = OllirAccesser.getVarTable(this.currMethod).get(destName);
        StringBuilder builder = new StringBuilder();
        String instruction = this.generateOperation(instr.getRhs());

        switch (descriptor.getScope()) {
            case PARAMETER, LOCAL -> {
                if(descriptor.getVarType().getTypeOfElement() == ElementType.INT32 ||
                    descriptor.getVarType().getTypeOfElement() == ElementType.BOOLEAN) {
                        return builder.append("istore_\n")
                            .append(descriptor.getVirtualReg())
                            .append("\n")
                            .toString();
                }

                return builder.append("astore\n")
                    .append(descriptor.getVirtualReg())
                    .append("\n")
                    .toString();

            }
            case FIELD -> {
                return builder.append("aload_0\n")
                .append(instruction)
                .append("putfield ")
                .append(this.className)
                .append("/")
                .append(destName)
                .append(" ")
                .append(getType(assignDest.getType()))
                .append("\n")
                .toString();
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
                return "\t\t" + left + right + "iand\n";
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
                return "\t\t" + builder.toString();
            }

            case ADDI32 -> {
                return "\t\t" + left + right + "iadd\n";
            }

            case SUBI32 -> {
                return "\t\t" + left + right + "isub\n";
            }

            case MULI32 -> {
                return "\t\t" + left + right + "imul\n";
            }

            case DIVI32 -> {
                return "\t\t" + left + right + "idiv\n";
            }
        }
        return "\t\t" + left;
    }

    private String generateUnaryOp(UnaryOpInstruction instr) { ;
        OperationType op = instr.getUnaryOperation().getOpType();

        if (op == OperationType.NOTB) {
            StringBuilder builder = new StringBuilder();
            String labelTrue = "LABEL " + this.opLabel++;
            String labelContinue = "LABEL " + this.opLabel++;

            builder.append("ifeq ").append(labelTrue).append("\n");
            builder.append("iconst_0\n");
            builder.append("goto ").append(labelContinue).append("\n");
            builder.append(labelTrue).append(":\n");
            builder.append("iconst_1\n");
            builder.append(labelContinue).append(":\n");
            return builder.toString();
        }

        return "";
    }

    private String generateCallOp(CallInstruction instr) {
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
        Element first = instr.getFirstOperand();
        Element second = instr.getSecondOperand();

        StringBuilder builder = new StringBuilder();
        builder.append("aload_0\n")
            .append("getfield ")
            .append(this.className)
            .append("/")
            .append(((Operand) first).getName())
            .append(" ")
            .append(getType(second.getType()))
            .append("\n");

        return builder.toString();
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

    private String generateGotoOp(GotoInstruction instr) {
        return "goto " + instr.getLabel() + "\n";
    }

    private String generateReturnOp(ReturnInstruction instr) {
        if(!instr.hasReturnValue()) {
            return "";
        }

        Element elem = instr.getOperand();
        return "\t\t" + this.generateLoad(elem) + switch (elem.getType().getTypeOfElement()) {
            case INT32, BOOLEAN -> "\n\t\tireturn";
            default -> "\n\t\tareturn";
        };
    }

    // TODO: DELETE THIS
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
