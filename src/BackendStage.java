import org.specs.comp.ollir.*;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import report.StyleReport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private int opLabel = 0;
    private int instrCurrStackSize = 0;
    private int instrMaxStackSize = 0;
    private final List<Report> reports = new ArrayList<>();

    public static JasminResult run(OllirResult ollirResult) {
        // Checks input
        TestUtils.noErrors(ollirResult.getReports());
        return new BackendStage().toJasmin(ollirResult);
    }

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();
        StringBuilder jasminCode = new StringBuilder();

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
                Arrays.asList(StyleReport.newError(Stage.GENERATION, "Exception during Jasmin generation", e)));
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
            this.generateLocalLimits();

            if(method.isConstructMethod()) continue;

            if(method.getMethodName().equals("main")) {
                String body = this.generateClassMethodBody(method.getInstructions());
                classMethodsCode.append("\t.method public static main([Ljava/lang/String;)V\n")
                    .append(this.generateStackLimits())
                    .append(this.generateLocalLimits())
                    .append(body)
                    .append("\t\treturn\n");
            }

            else {
                classMethodsCode.append(String.format("\t.method public %s(", method.getMethodName()));

                for(Element param:  method.getParams()) {
                    classMethodsCode.append(BackendStage.generateType(param.getType()));
                }

                String body = this.generateClassMethodBody(method.getInstructions());

                classMethodsCode.append(")")
                    .append(BackendStage.generateType(method.getReturnType()))
                    .append(this.generateStackLimits())
                    .append(this.generateLocalLimits())
                    .append(body);

                if(method.getReturnType().getTypeOfElement() == ElementType.VOID) {
                    classMethodsCode.append("\t\treturn");
                }

                classMethodsCode.append("\n");
            }

            classMethodsCode.append("\t.end method\n\n");
            this.instrCurrStackSize = 0;
            this.instrMaxStackSize = 0;
        }

        return classMethodsCode.toString();
    }

    private String generateStackLimits() {
        return "\n\t\t.limit stack " + (this.instrMaxStackSize + 2) + "\n";
    }

    private String generateLocalLimits() {
        if(this.currMethod.isConstructMethod()) {
            return "";
        }

        int locals = this.currMethod.getVarTable().size();
        if(!this.currMethod.isStaticMethod()) {
            locals++;
        }

        return "\t\t.limit locals " + locals + "\n";
    }

    private String generateClassMethodBody(List<Instruction> instructions) {
        StringBuilder methodInstCode = new StringBuilder();

        for(Instruction instr: instructions) {
            for(Map.Entry<String, Instruction> entry: this.currMethod.getLabels().entrySet()) {
                if(entry.getValue().getId() == instr.getId()) {
                    methodInstCode.append(this.generatePops())
                        .append("\t")
                        .append(entry.getKey())
                        .append(":\n");
                }
            }

            methodInstCode.append(this.generateOperation(instr));
        }

        return methodInstCode.toString();
    }

    private String generateOperation(Instruction instr) {
        return switch (instr.getInstType()) {
            case NOPER -> this.generateSingleOp((SingleOpInstruction) instr);
            case ASSIGN -> this.generateAssignOp((AssignInstruction) instr);
            case BINARYOPER -> this.generateBinaryOp((BinaryOpInstruction) instr);
            case CALL -> this.generateCallOp((CallInstruction) instr);
            case GETFIELD -> this.generateGetFieldOp((GetFieldInstruction) instr);
            case PUTFIELD -> this.generatePutFieldOp((PutFieldInstruction) instr);
            case GOTO -> this.generateGotoOp((GotoInstruction) instr);
            case RETURN -> this.generateReturnOp((ReturnInstruction) instr);
            case BRANCH -> this.generateBranchOp((CondBranchInstruction) instr);
            default -> "";
        };
    }

    private String generateSingleOp(SingleOpInstruction instr) {
        if(instr.getSingleOperand() instanceof ArrayOperand) {
            Element index = ((ArrayOperand)instr.getSingleOperand()).getIndexOperands().get(0);
            String load = this.generateLoad(instr.getSingleOperand()) + this.generateLoad(index);
            this.instrCurrStackSize -= 1;

            return load + "\t\tiaload\n";
        }

        return this.generateLoad(instr.getSingleOperand());
    }

    private Descriptor getDescriptor(Element elem) {
        if(elem.isLiteral()) {
            this.reports.add(new StyleReport(ReportType.ERROR, Stage.GENERATION, "Tried to get a descriptor of a literal"));
            return null;
        }

        if(elem.getType().getTypeOfElement() == ElementType.THIS) {
            return this.currMethod.getVarTable().get("this");
        }
        return this.currMethod.getVarTable().get(((Operand) elem).getName());
    }

    private String generateLoad(Element elem) {
        ++this.instrCurrStackSize;
        if(this.instrCurrStackSize > this.instrMaxStackSize) {
            this.instrMaxStackSize = this.instrCurrStackSize;
        }

        if(elem.isLiteral()) {
            String literal = ((LiteralElement) elem).getLiteral();

            try {
                int value = Integer.parseInt(literal);
                if(value == -1) return "\t\ticonst_m1\n";
                if(value >= 0 && value <= 5) return "\t\ticonst_" + value + "\n";
                if(value >= -128 && value <= 127) return "\t\tbipush " + value + "\n";
                if(value >= -32768 && value <= 32767) return "\t\tsipush " + value + "\n";
                return "\t\tldc " + value + "\n";
            } catch (NumberFormatException ignored) {
                this.reports.add(new StyleReport(ReportType.ERROR, Stage.GENERATION, "Literal" + literal + "is not an integer!"));
            }
        }

        if(elem instanceof Operand && elem.getType().getTypeOfElement() == ElementType.BOOLEAN) {
            if(((Operand) elem).getName().equals("true")) {
                return "\t\ticonst_1" + "\n";
            }

            if(((Operand) elem).getName().equals("false")) {
                return "\t\ticonst_0" + "\n";
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

    private String generateAssignOp(AssignInstruction instr) {
        if (instr.getDest() instanceof ArrayOperand) {
            String instruction = this.generateOperation(instr.getRhs());
            ArrayOperand arrayOperand = (ArrayOperand) instr.getDest();
            Element index = arrayOperand.getIndexOperands().get(0);

            String load = this.generateLoad(arrayOperand) + this.generateLoad(index);
            this.instrCurrStackSize -= 3;

            return load + instruction + "\t\tiastore\n";
        }

        if(instr.getRhs().getInstType() == InstructionType.BINARYOPER) {
            String destName = ((Operand)(instr.getDest())).getName();
            BinaryOpInstruction rhs = (BinaryOpInstruction)instr.getRhs();
            Element leftOperand = rhs.getLeftOperand();
            Element rightOperand = rhs.getRightOperand();

            if(rhs.getUnaryOperation().getOpType() == OperationType.ADD ||
                rhs.getUnaryOperation().getOpType() == OperationType.SUB) {
                if(!leftOperand.isLiteral() && ((Operand)leftOperand).getName().equals(destName)
                        && rightOperand.isLiteral()) {
                    Descriptor descriptor = this.currMethod.getVarTable().get(((Operand)leftOperand).getName());

                    return "\t\tiinc " + descriptor.getVirtualReg() + " " + ((LiteralElement)rightOperand).getLiteral() + "\n";
                }

                if(!((BinaryOpInstruction)instr.getRhs()).getRightOperand().isLiteral()
                        && ((Operand)((BinaryOpInstruction)instr.getRhs()).getRightOperand()).getName().equals(destName)
                        && ((BinaryOpInstruction)instr.getRhs()).getLeftOperand().isLiteral()) {
                    Descriptor descriptor = this.currMethod.getVarTable().get(((Operand)rightOperand).getName());

                    return "\t\tiinc " + descriptor.getVirtualReg() + " " + ((LiteralElement)leftOperand).getLiteral() + "\n";
                }
            }
        }

        String instruction = this.generateOperation(instr.getRhs());
        Descriptor descriptor = this.getDescriptor(instr.getDest());
        this.instrCurrStackSize -= 1;

        return instruction + "\t\t" + switch (descriptor.getVarType().getTypeOfElement()) {
            case INT32, BOOLEAN -> (descriptor.getVirtualReg() <= 3 ? "istore_" : "istore ") +
                    descriptor.getVirtualReg() + "\n";
            case THIS -> "astore_0\n";
            case ARRAYREF, CLASS, OBJECTREF ->  (descriptor.getVirtualReg() <= 3 ? "astore_" : "astore ")
                    + descriptor.getVirtualReg() + "\n";
            default -> "";
        };
    }

    private String generateBinaryOp(BinaryOpInstruction instr) {
        Element leftElem = instr.getLeftOperand();
        Element rightElem = instr.getRightOperand();

        String left = this.generateLoad(leftElem);
        String right = this.generateLoad(rightElem);
        this.instrCurrStackSize -= 1;

        switch (instr.getUnaryOperation().getOpType()) {
            case NOT, NOTB -> {
                StringBuilder builder = new StringBuilder();
                String labelTrue = "LABEL_" + this.opLabel++;
                String labelContinue = "LABEL_" + this.opLabel++;

                return left + right + builder.append("\t\tif_icmpne ")
                    .append(labelTrue)
                    .append("\n\t\ticonst_0\n")
                    .append("\t\tgoto ")
                    .append(labelContinue).append("\n\t")
                    .append(labelTrue).append(":\n")
                    .append("\t\ticonst_1\n\t")
                    .append(labelContinue).append(":\n").toString();
            }

            case EQ, EQI32 -> {
                StringBuilder builder = new StringBuilder();
                String labelTrue = "LABEL_" + this.opLabel++;
                String labelContinue = "LABEL_" + this.opLabel++;

                return left + right + builder.append("\t\tif_icmpeq ")
                    .append(labelTrue)
                    .append("\n\t\ticonst_0\n")
                    .append("\t\tgoto ")
                    .append(labelContinue).append("\n\t")
                    .append(labelTrue).append(":\n")
                    .append("\t\ticonst_1\n\t")
                    .append(labelContinue).append(":\n").toString();
            }

            case ANDB, ANDI32 -> {
                return left + right + "\n\t\tiand\n";
            }

            case ORB, ORI32 -> {
                return left + right + "\n\t\tior\n";
            }

            case LTH, LTHI32 -> {
                StringBuilder builder = new StringBuilder();
                String labelTrue = "LABEL_" + this.opLabel++;
                String labelContinue = "LABEL_" + this.opLabel++;

                return left + right + builder.append("\t\tif_icmplt ")
                    .append(labelTrue)
                    .append("\n\t\ticonst_0\n")
                    .append("\t\tgoto ")
                    .append(labelContinue).append("\n\t")
                    .append(labelTrue).append(":\n")
                    .append("\t\ticonst_1\n\t")
                    .append(labelContinue).append(":\n").toString();
            }

            case GTE, GTEI32 -> {
                StringBuilder builder = new StringBuilder();
                String labelTrue = "LABEL_" + this.opLabel++;
                String labelContinue = "LABEL_" + this.opLabel++;

                return left + right + builder.append("\t\tif_icmpge ")
                    .append(labelTrue)
                    .append("\n\t\ticonst_0\n")
                    .append("\t\tgoto ")
                    .append(labelContinue).append("\n\t")
                    .append(labelTrue).append(":\n")
                    .append("\t\ticonst_1\n\t")
                    .append(labelContinue).append(":\n").toString();
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
            case NEW -> {
                StringBuilder builder = new StringBuilder();

                if(first.getType().getTypeOfElement() == ElementType.OBJECTREF) {
                    this.instrCurrStackSize += 2;

                    return builder.append("\t\tnew ")
                        .append(((Operand)first).getName())
                        .append("\n\t\tdup\n")
                        .toString();
                } else if(first.getType().getTypeOfElement() == ElementType.ARRAYREF) {
                    if(operands.size() > 0) {
                        Element elem = operands.get(0);
                        String load = generateLoad(elem);

                        return builder.append(load)
                            .append("\t\tnewarray int\n")
                            .toString();
                    }
                }

                return "";
            }

            case arraylength -> {
                return this.generateLoad(first) + "\t\tarraylength\n";
            }

            case invokestatic -> {
                StringBuilder builder = new StringBuilder()
                    .append(instr.getListOfOperands().stream().map(this::generateLoad).collect(Collectors.joining()))
                    .append("\t\t")
                    .append(invocationType.toString())
                    .append(" ");

                this.instrCurrStackSize -= instr.getListOfOperands().size();
                builder.append(generateMethodCallBody(instr, first, (LiteralElement) second));
                this.instrCurrStackSize += instr.getReturnType().getTypeOfElement() != ElementType.VOID ? 1 : 0;

                return builder.toString();
            }

            case invokevirtual -> {
                StringBuilder builder = new StringBuilder()
                    .append(this.generateLoad(first))
                    .append(instr.getListOfOperands().stream().map(this::generateLoad).collect(Collectors.joining()))
                    .append("\t\t")
                    .append(invocationType.toString())
                    .append(" ");

                this.instrCurrStackSize -= (instr.getListOfOperands().size() + 1);
                builder.append(generateMethodCallBody(instr, first, (LiteralElement) second));
                this.instrCurrStackSize += instr.getReturnType().getTypeOfElement() != ElementType.VOID ? 1 : 0;

                return builder.toString();
            }

            case invokespecial -> {
                StringBuilder builder = new StringBuilder()
                    .append(this.generateLoad(first))
                    .append(instr.getListOfOperands().stream().map(this::generateLoad).collect(Collectors.joining()))
                    .append("\t\t")
                    .append(invocationType.toString())
                    .append(" ");

                this.instrCurrStackSize -= (instr.getListOfOperands().size() + 2);
                builder.append(generateMethodCallBody(instr, first, (LiteralElement) second));

                return builder.toString();
            }
        }

        return "";
    }

    private String generateMethodCallBody(CallInstruction instr, Element element, LiteralElement method) {
        StringBuilder builder = new StringBuilder();

        switch (element.getType().getTypeOfElement()) {
            case THIS, OBJECTREF -> builder.append(((ClassType) element.getType()).getName());
            case CLASS -> builder.append(((Operand) element).getName());
        }

        builder.append(".")
            .append(method.getLiteral().replace("\"", ""))
            .append("(");

        for (Element param : instr.getListOfOperands()) {
            builder.append(BackendStage.generateType(param.getType()));
        }

        return builder.append(")")
            .append(BackendStage.generateType(instr.getReturnType()))
            .append("\n").toString();
    }

    private String generateGetFieldOp(GetFieldInstruction instr) {
        Operand second = (Operand) instr.getSecondOperand();
        Element obj = instr.getFirstOperand();
        return this.generateLoad(obj) +
            "\t\tgetfield " + this.className + "/" + second.getName() + " " + generateType(second.getType()) + "\n";
    }

    private String generatePutFieldOp(PutFieldInstruction instr) {
        Element obj = instr.getFirstOperand();
        Operand field = (Operand) instr.getSecondOperand();
        Element third = instr.getThirdOperand();

        String load = this.generateLoad(obj) + this.generateLoad(third);
        this.instrCurrStackSize -= 2;

        return load +
            "\t\tputfield " + this.className + "/" + field.getName() + " " + generateType(field.getType()) + "\n";
    }

    private String generateGotoOp(GotoInstruction instr) {
        return this.generatePops() + "\t\tgoto " + instr.getLabel() + "\n";
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

    private String generateBranchOp(CondBranchInstruction instr) {
        String label = instr.getLabel();
        Operation operation = instr.getCondOperation();

        String operatorsLoads = this.generateLoad(instr.getLeftOperand()) + this.generateLoad(instr.getRightOperand());
        this.instrCurrStackSize -= 2;

        return this.generatePops() + operatorsLoads + switch(operation.getOpType()) {
            case NOTB, NOT -> "\t\tif_icmpne " + label + "\n";
            case LTH, LTHI32 -> "\t\tif_icmplt " + label + "\n";
            case GTE, GTEI32 -> "\t\tif_icmpge " + label + "\n";
            case ORB, ORI32 -> "\t\tior\n" + "\t\ticonst_0\n" + "\t\tif_icmpgt " + label + "\n";
            case EQ, ANDB, ANDI32 -> "\t\tif_icmpeq " + label + "\n";
            default -> throw new IllegalStateException("Unexpected value: " + operation.getOpType());
        };
    }

    private String generatePops() {
        StringBuilder pop = new StringBuilder();

        for(int i = this.instrCurrStackSize; i > 0; i--) {
            if(this.instrCurrStackSize > 1) {
                pop.append("\t\tpop2\n");
                i--;
            } else {
                pop.append("\t\tpop\n");
            }
        }

        this.instrCurrStackSize = 0;
        return pop.toString();
    }

    private static String generateType(Type type) {
        return switch (type.getTypeOfElement()) {
            case ARRAYREF -> "[I";
            case INT32 -> "I";
            case BOOLEAN -> "Z";
            case VOID -> "V";
            case OBJECTREF -> "L" + ((ClassType) type).getName() + ";";
            default -> throw new IllegalStateException("Unexpected value: " + type.getTypeOfElement());
        };
    }
}
