import ollir.SethiUllmanGenerator;
import ollir.SethiUllmanLabeler;
import optimizations.LivenessAnalysis;
import optimizations.MethodNode;
import optimizations.VarNode;
import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import report.StyleReport;

import java.util.*;
import java.util.stream.Collectors;

public class OptimizationStage implements JmmOptimization {
    public static OllirResult run(JmmSemanticsResult semanticsResult) {
        // Checks input
        TestUtils.noErrors(semanticsResult.getReports());

        OptimizationStage optStage = new OptimizationStage();

        if(Main.OPTIMIZATIONS) {
            optStage.optimize(semanticsResult);
        }

        OllirResult ollirRes = optStage.toOllir(semanticsResult);

        if(Main.NUM_REGISTERS > 0) {
            optStage.allocateRegisters(ollirRes);
        }

        if(Main.OPTIMIZATIONS) {
            optStage.optimize(ollirRes);
        }

        return ollirRes;
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        JmmNode root = semanticsResult.getRootNode();
        // More reports from this stage
        List<Report> reports = new ArrayList<>();

        SethiUllmanLabeler labeler = new SethiUllmanLabeler();
        labeler.visit(root);

        // Convert the AST to a String containing the equivalent OLLIR code
        SethiUllmanGenerator generator = new SethiUllmanGenerator(semanticsResult.getSymbolTable());
        String ollirCode = generator.visit(root).stream().map(inst -> inst.toString("")).collect(Collectors.joining());

        try {
            Utils.saveFile(semanticsResult.getSymbolTable().getClassName() + ".ollir", "ollir", ollirCode);
        } catch (Exception e) {
            return new OllirResult(semanticsResult, "",
                Arrays.asList(StyleReport.newError(Stage.OPTIMIZATION, "Exception during Ollir code generation", e)));
        }

        return new OllirResult(semanticsResult, ollirCode, reports);
    }

    public void allocateRegisters(OllirResult ollirResult) {
        ClassUnit classUnit = ollirResult.getOllirClass();
        Map<MethodNode, Map<VarNode, Set<VarNode>>> graph = new LivenessAnalysis(classUnit).analyze();

    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        // THIS IS JUST FOR CHECKPOINT 3
        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        // THIS IS JUST FOR CHECKPOINT 3
        return ollirResult;
    }
}
