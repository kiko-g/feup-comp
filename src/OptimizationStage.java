import java.util.ArrayList;
import java.util.List;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmNode;
import analysis.JmmSemanticsResult;
import ollir.JmmOptimization;
import ollir.OllirResult;
import report.Report;

public class OptimizationStage implements JmmOptimization {
    public static OllirResult run(JmmSemanticsResult semanticsResult) {
        // Checks input
        TestUtils.noErrors(semanticsResult.getReports());

        return new OptimizationStage().toOllir(semanticsResult);
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        JmmNode node = semanticsResult.getRootNode();
        // Convert the AST to a String containing the equivalent OLLIR code
        String ollirCode = ""; // Convert node ...
        // More reports from this stage
        List<Report> reports = semanticsResult.getReports();

        return new OllirResult(semanticsResult, ollirCode, reports);
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
