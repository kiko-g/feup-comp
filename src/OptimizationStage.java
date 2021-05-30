import ollir.SethiUllmanGenerator;
import ollir.SethiUllmanLabeler;
import optimizations.ast.ConstantPropagator;
import optimizations.ollir.GraphPainterException;
import optimizations.ollir.RegisterAllocater;
import org.specs.comp.ollir.OllirErrorException;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import report.StyleReport;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OptimizationStage implements JmmOptimization {
    public static OllirResult run(JmmSemanticsResult semanticsResult, boolean optimize, int nRegisters) {
        // Checks input
        TestUtils.noErrors(semanticsResult.getReports());

        OptimizationStage optStage = new OptimizationStage();

        if(optimize) {
            optStage.optimize(semanticsResult);
        }

        OllirResult ollirRes = optStage.toOllir(semanticsResult, optimize, nRegisters);

        if(optimize) {
            ollirRes = optStage.optimize(ollirRes);
        }

        return ollirRes;
    }

    public OllirResult toOllir(JmmSemanticsResult semanticsResult, boolean optimize, int nRegisters) {
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

        if (nRegisters > 0) {
            ollirRes = allocateRegisters(ollirRes, nRegisters);
        }

        return ollirRes;
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult, boolean optimize) {
        return toOllir(semanticsResult, optimize, 0);
    }

    public OllirResult allocateRegisters(OllirResult ollirResult, int nRegisters) {
        try {
            new RegisterAllocater().allocate(ollirResult.getOllirClass(), nRegisters);
        } catch (GraphPainterException e) {
            ollirResult.getReports().add(StyleReport.newError(Stage.OPTIMIZATION, "Exception during allocation of registers", e));
        }

        return ollirResult;
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        new ConstantPropagator().visit(semanticsResult.getRootNode());
        return semanticsResult;
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        return toOllir(semanticsResult, false);
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        // THIS IS JUST FOR CHECKPOINT 3
        return ollirResult;
    }
}
