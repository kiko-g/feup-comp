import org.specs.comp.ollir.*;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import report.Report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

            Utils.saveFile(this.className + ".j", "jasmin", jasminCode.toString());

            return new JasminResult(ollirResult, jasminCode.toString(), reports);
        } catch (Exception e) {
            return new JasminResult(ollirClass.getClassName(), null,
                Arrays.asList(Report.newError(Stage.GENERATION, "Exception during Jasmin generation", e)));
        }
    }

    private String generateSuper() {
        return this.extendsDef == null ? "java/lang/Object" : this.extendsDef;
    }

    private String generateClassDecl(ClassUnit ollirClass) {
        StringBuilder classCode = new StringBuilder();

        // Class: Definition
        classCode.append(".class public ")
            .append(ollirClass.getClassName())
            .append("\n");

        // Class: Extends
        classCode.append(".super ")
            .append(this.generateSuper())
            .append("\n");

        // Class: Fields
        classCode.append(this.generateClassFields(ollirClass));

        // Class: Used to initialize a new instance of the class
        classCode.append("\t.method public <init>()V\n")
            .append("\t\taload_0\n")
            .append("\t\tinvokenonvirtual ")
            .append(this.generateSuper())
            .append("/<init>()V\n")
            .append("\t\treturn\n")
            .append("\t.end method\n\n");

        return classCode.toString();
    }

    private String generateClassFields(ClassUnit ollirClass) {
        StringBuilder classFieldsCode = new StringBuilder();

        for(Field field: ollirClass.getFields()) {
            classFieldsCode.append("\t.field private ")
                .append(field.getFieldName())
                .append(" ")
                .append(BackendStage.generateType(field.getFieldType()))
                .append("\n");
        }

        return classFieldsCode.append("\n").toString();
    }

    private String generateClassMethods(ClassUnit ollirClass) {
        StringBuilder classMethodsCode = new StringBuilder();

        for(Method method: ollirClass.getMethods()) {
            this.currMethod = method;

            if(method.getMethodName().equals("main")) {
                classMethodsCode.append("\t.method public static main([Ljava/lang/String;)V\n")
                    .append("\t\t.limit stack 99\n")
                    .append("\t\t.limit locals 99\n")
                    .append(this.generateClassMethodBody(method.getInstructions()))
                    .append("\t\treturn\n");
            }

            else {
                classMethodsCode.append(String.format("\t.method public %s(", method.isConstructMethod() ?
                    this.className : method.getMethodName()));

                for(Element param:  method.getParams()) {
                    classMethodsCode.append(BackendStage.generateType(param.getType()));
                }

                classMethodsCode.append(")")
                    .append(BackendStage.generateType(method.getReturnType()))
                    .append("\n\t\t.limit stack 99\n")
                    .append("\t\t.limit locals 99\n")
                    .append(this.generateClassMethodBody(method.getInstructions()));

                if(method.getReturnType().getTypeOfElement() == ElementType.VOID) {
                    classMethodsCode.append("\t\treturn");
                }

                classMethodsCode.append("\n");
            }

            classMethodsCode.append("\t.end method\n\n");
        }

        return classMethodsCode.toString();
    }

    private String generateClassMethodBody(List<Instruction> instructions) {
        StringBuilder methodInstCode = new StringBuilder();

        for(Instruction instr: instructions) {
            for(Map.Entry<String, Instruction> entry: this.currMethod.getLabels().entrySet()) {
                if(entry.getValue().getId() == instr.getId()) {
                    methodInstCode.append("\t" + entry.getKey() + ":\n");
                }
            }

            methodInstCode.append(this.generateOperation(instr));
        }

        return methodInstCode.toString();
    }

    private String generateOperation(Instruction instr) {
        return switch (instr.getInstType()) {
            case NOPER -> this.generateLoad(((SingleOpInstruction) instr).getSingleOperand());
            case ASSIGN -> this.generateAssign((AssignInstruction) instr);
            case BINARYOPER -> this.generateBinaryOp((BinaryOpInstruction) instr);
            case CALL -> this.generateCallOp((CallInstruction) instr);
            case GETFIELD -> this.generateGetFieldOp((GetFieldInstruction) instr);
            case PUTFIELD -> this.generatePutFieldOp((PutFieldInstruction) instr);
            case GOTO -> this.generateGotoOp((GotoInstruction) instr);
            case RETURN -> this.generateReturnOp((ReturnInstruction) instr);
            case BRANCH -> this.generateBranch((CondBranchInstruction) instr);
            default -> "";
        };
    }

    private String generateBranch(CondBranchInstruction instr) {
        String label = instr.getLabel();
        Operation operation = instr.getCondOperation();

        String operatorsLoads = this.generateLoad(instr.getLeftOperand()) + this.generateLoad(instr.getRightOperand());
        return operatorsLoads + switch(operation.getOpType()) {
            case NOTB, NOT -> "\t\tif_icmpne " + label + "\n";
            case LTH, LTHI32 -> "\t\tif_icmplt " + label + "\n";
            case EQ, ANDB, ANDI32 -> "\t\tif_icmpeq " + label + "\n";
            default -> throw new IllegalStateException("Unexpected value: " + operation.getOpType());
        };
    }

    private Descriptor getDescriptor(Element elem) {
        if(elem.isLiteral()) {
            this.reports.add(new Report(ReportType.ERROR, Stage.GENERATION, "Tried to get a descriptor of a literal"));
            return null;
        }

        if(elem.getType().getTypeOfElement() == ElementType.THIS) {
            return this.currMethod.getVarTable().get("this");
        }
        return this.currMethod.getVarTable().get(((Operand) elem).getName());
    }

    // TODO: if we uncommented lines int will be converted to boolean (see Simple.class)
    private String generateLoad(Element elem) {
        if(elem.isLiteral()) {
            String literal = ((LiteralElement) elem).getLiteral();

            try {
                int value = Integer.parseInt(literal);
                //if(value == -1) return "\t\ticonst_m1\n";
                //if(value >= 0 && value <= 5) return "\t\ticonst_" + value + "\n";
                //if(value >= -128 && value <= 127) return "\t\tbipush " + value + "\n";
                //if(value >= -32768 && value <= 32767) return "\t\tsipush " + value + "\n";
                return "\t\tldc " + value + "\n";
            } catch (NumberFormatException ignored) {
                this.reports.add(new Report(ReportType.ERROR, Stage.GENERATION, "Literal" + literal + "is not an integer!"));
            }
        }

        Descriptor descriptor = this.getDescriptor(elem);
        if(descriptor.getScope() == VarScope.FIELD) {
            Instruction getfield = new GetFieldInstruction(
                new Element(new Type(ElementType.THIS)),
                new Element(descriptor.getVarType()),
                descriptor.getVarType()
            );
            return generateOperation(getfield);
        } else {
            return "\t\t" + switch (descriptor.getVarType().getTypeOfElement()) {
                case INT32, BOOLEAN -> descriptor.getVirtualReg() <= 3 ?
                    "iload_" + descriptor.getVirtualReg() + "\n" : "iload " + descriptor.getVirtualReg() + "\n";
                case THIS -> "aload_0\n";
                case ARRAYREF, CLASS, OBJECTREF -> descriptor.getVirtualReg() <= 3 ?
                    "aload_" + descriptor.getVirtualReg() + "\n" : "aload " + descriptor.getVirtualReg() + "\n";
                default -> "";
            };
        }
    }

    private String generateAssign(AssignInstruction instr) {
        Descriptor descriptor = this.getDescriptor(instr.getDest());
        String instruction = this.generateOperation(instr.getRhs());

        return instruction + "\t\t" + switch (descriptor.getVarType().getTypeOfElement()) {
            case INT32, BOOLEAN -> descriptor.getVirtualReg() <= 3 ?
                "istore_" + descriptor.getVirtualReg() + "\n" : "istore " + descriptor.getVirtualReg() + "\n";
            case THIS -> "astore_0\n";
            case ARRAYREF, CLASS, OBJECTREF -> descriptor.getVirtualReg() <= 3 ?
                "astore_" + descriptor.getVirtualReg() + "\n" : "astore " + descriptor.getVirtualReg() + "\n";
            default -> "";
        };
    }

    private String generateBinaryOp(BinaryOpInstruction instr) {
        Element leftElem = instr.getLeftOperand();
        Element rightElem = instr.getRightOperand();

        String left = this.generateLoad(leftElem);
        String right = this.generateLoad(rightElem);

        OperationType op = instr.getUnaryOperation().getOpType();

        switch (op) {
            case NOT, NOTB -> {
                StringBuilder builder = new StringBuilder();
                String labelTrue = "LABEL_" + this.opLabel++;
                String labelContinue = "LABEL_" + this.opLabel++;

                return this.generateLoad(leftElem) +
                    this.generateLoad(rightElem) +
                    builder.append("\t\tif_icmpne ")
                        .append(labelTrue)
                        .append("\n\t\ticonst_0\n")
                        .append("\t\tgoto ")
                        .append(labelContinue).append("\n\t")
                        .append(labelTrue).append(":\n")
                        .append("\t\ticonst_1\n\t")
                        .append(labelContinue).append(":\n");
            }

            case ANDB, ANDI32 -> {
                return left + right + "\n\t\tiand\n";
            }

            case LTH, LTHI32 -> {
                StringBuilder builder = new StringBuilder();
                String labelTrue = "LABEL_" + this.opLabel++;
                String labelContinue = "LABEL_" + this.opLabel++;

                return this.generateLoad(leftElem) +
                    this.generateLoad(rightElem) +
                    builder.append("\t\tif_icmplt ")
                        .append(labelTrue)
                        .append("\n\t\ticonst_0\n")
                        .append("\t\tgoto ")
                        .append(labelContinue).append("\n\t")
                        .append(labelTrue).append(":\n")
                        .append("\t\ticonst_1\n\t")
                        .append(labelContinue).append(":\n");
            }

            case ADD, ADDI32 -> {
                return left + right + "\t\tiadd\n";
            }

            case SUB, SUBI32 -> {
                return left + right + "\t\tisub\n";
            }

            case MUL, MULI32 -> {
                return left + right + "\t\timul\n";
            }

            case DIV, DIVI32 -> {
                return left + right + "\t\tidiv\n";
            }
        }
        return left;
    }

    private String generateCallOp(CallInstruction instr) {
        Element first = instr.getFirstArg();
        Element second = instr.getSecondArg();
        List<Element> operands = instr.getListOfOperands();
        CallType invocationType = instr.getInvocationType();

        switch (invocationType) {
            case ldc -> { return ""; }
            case NEW -> {
                StringBuilder builder = new StringBuilder();

                if(first.getType().getTypeOfElement() == ElementType.OBJECTREF) {
                    return builder.append("\t\tnew ")
                        .append(((Operand)first).getName())
                        .append("\n\t\tdup\n")
                        .toString();
                } else if(first.getType().getTypeOfElement() == ElementType.ARRAYREF) {
                    if(operands.size() > 0) {
                        Element elem = operands.get(0);

                        return builder.append(generateLoad(elem))
                            .append("\t\tnewarray int\n")
                            .toString();
                    }
                }

                return "";
            }

            case arraylength -> {
                return this.generateLoad(first) + "\t\tarraylength\n";
            }

            // TODO: fix invokestatic, virtual, special
            case invokevirtual -> {
                StringBuilder builder = new StringBuilder();
                builder.append(this.generateLoad(first))
                        .append("\t\t")
                        .append(invocationType.toString())
                        .append(" ");

                if(first.getType().getTypeOfElement() == ElementType.THIS) {
                    builder.append(this.className);
                } else {
                    builder.append(((Operand)first).getName());
                }

                builder.append(".")
                    .append(((LiteralElement)second).getLiteral().replace("\"", ""))
                    .append("(");

                for(Element param: instr.getListOfOperands()) {
                    builder.append(BackendStage.generateType(param.getType()));
                }

                builder.append(")")
                    .append(BackendStage.generateType(instr.getReturnType()))
                    .append("\n");

                return builder.toString();
            }

            case invokespecial,
                invokestatic -> {
                StringBuilder builder = new StringBuilder();

                if(first.getType().getTypeOfElement() == ElementType.THIS) {
                    builder.append(this.generateLoad(first))
                        .append("\t\t")
                        .append(invocationType.toString())
                        .append(" ")
                        .append(this.className);
                } else {
                    builder.append(this.generateLoad(first))
                        .append("\t\t")
                        .append(invocationType.toString())
                        .append(" ")
                        .append(((Operand)first).getName());
                }

                builder.append(".")
                    .append(((LiteralElement)second).getLiteral().replace("\"", ""))
                    .append("(");

                for(Element param: instr.getListOfOperands()) {
                    builder.append(BackendStage.generateType(param.getType()));
                }

                builder.append(")")
                    .append(BackendStage.generateType(instr.getReturnType()))
                    .append("\n");

                return builder.toString();
            }
        }

        return "";
    }

    private String generateGetFieldOp(GetFieldInstruction instr) {
        Element second = instr.getSecondOperand();
        Operand obj = (Operand) instr.getSecondOperand();

        StringBuilder builder = new StringBuilder();
        return builder.append(this.generateLoad(second))
            .append("\n\t\tgetfield ")
            .append(this.className)
            .append("/")
            .append(obj.getName())
            .append(" ")
            .append(generateType(second.getType()))
            .append("\n")
            .toString();
    }

    private String generatePutFieldOp(PutFieldInstruction instr) {
        Operand obj = (Operand)instr.getFirstOperand();
        Element third = instr.getThirdOperand();

        StringBuilder builder = new StringBuilder();
        return builder.append(this.generateLoad(instr.getSecondOperand()))
            .append("\n\t\tputfield ")
            .append(this.className)
            .append("/")
            .append(obj.getName())
            .append(" ")
            .append(generateType(third.getType()))
            .append("\n")
            .toString();
    }

    private String generateGotoOp(GotoInstruction instr) {
        return "\t\tgoto " + instr.getLabel() + "\n";
    }

    private String generateReturnOp(ReturnInstruction instr) {
        if(!instr.hasReturnValue()) {
            return "";
        }

        Element elem = instr.getOperand();
        return this.generateLoad(elem) + switch (elem.getType().getTypeOfElement()) {
            case INT32, BOOLEAN -> "\t\tireturn";
            case ARRAYREF -> "\t\tareturn";
            default -> "";
        };
    }

    private static String generateType(Type type) {
        return switch (type.getTypeOfElement()) {
            case ARRAYREF -> "[I";
            case INT32 -> "I";
            case BOOLEAN -> "Z";
            case VOID -> "V";
            case CLASS -> "L";
            default -> throw new IllegalStateException("Unexpected value: " + type.getTypeOfElement());
        };
    }
}
