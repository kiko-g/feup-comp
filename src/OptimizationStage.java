import ollir.SethiUllmanGenerator;
import ollir.SethiUllmanLabeler;
import optimizations.*;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.OllirErrorException;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
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
            ollirRes = optStage.allocateRegisters(ollirRes);
        }

        if(Main.OPTIMIZATIONS) {
            ollirRes = optStage.optimize(ollirRes);
        }

        return ollirRes;
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        JmmNode root = semanticsResult.getRootNode();
        List<Report> reports = new ArrayList<>();

        SethiUllmanLabeler labeler = new SethiUllmanLabeler();
        labeler.visit(root);

        // Convert the AST to a String containing the equivalent OLLIR code
        SethiUllmanGenerator generator = new SethiUllmanGenerator(semanticsResult.getSymbolTable());
        String ollirCode = generator.visit(root).stream().map(inst -> inst.toString("")).collect(Collectors.joining());

        try {
            Utils.saveFile(semanticsResult.getSymbolTable().getClassName() + ".ollir", "ollir", ollirCode);
        } catch (Exception e) {
            reports.add(StyleReport.newError(Stage.LLIR, "Exception during Ollir code generation", e));
        }

        OllirResult ollirRes = new OllirResult(semanticsResult, ollirCode, reports);

        try {
            ollirRes.getOllirClass().checkMethodLabels();
            ollirRes.getOllirClass().buildCFGs();
            ollirRes.getOllirClass().buildVarTables();
        } catch (OllirErrorException e) {
            reports.add(StyleReport.newError(Stage.LLIR, "Exception during Ollir code generation", e));
        }

        return ollirRes;
    }

    public OllirResult allocateRegisters(OllirResult ollirResult) {
        ClassUnit classUnit = ollirResult.getOllirClass();
        Map<MethodNode, Map<VarNode, Set<VarNode>>> graph = new InterferenceGraphMaker(classUnit).create();
        try {
            new GraphPainter(graph).paint(Main.NUM_REGISTERS);
        } catch (GraphPainter.GraphPainterException e) {
            e.printStackTrace();
        }

        new RegisterAllocater().allocate(graph);

        return ollirResult;
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {




        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        // THIS IS JUST FOR CHECKPOINT 3
        return ollirResult;
    }
}
