import analysis.AnalysisTableBuilder;
import analysis.InitializationAnalysis;
import analysis.TypeAnalysis;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class Analysis implements JmmAnalysis {
    public static JmmSemanticsResult run(JmmParserResult parserResult) {
        // Checks input
        TestUtils.noErrors(parserResult.getReports());

        return new Analysis().semanticAnalysis(parserResult);
    }

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        JmmNode root = parserResult.getRootNode();
        List<Report> reports = new ArrayList<>();

        AnalysisTableBuilder tableBuilder = new AnalysisTableBuilder(reports);
        tableBuilder.visit(root);

        if(TestUtils.getNumErrors(tableBuilder.getReports()) != 0) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, "Semantically invalid Program!"));
            return new JmmSemanticsResult(parserResult, null, reports);
        }

        TypeAnalysis typeAnalysis = new TypeAnalysis(tableBuilder.getSymbolTable(), reports);
        typeAnalysis.visit(root);

        if(TestUtils.getNumErrors(typeAnalysis.getReports()) != 0) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, "Semantically invalid Program!"));
            return new JmmSemanticsResult(parserResult, null, reports);
        }

        InitializationAnalysis initializationAnalysis = new InitializationAnalysis(tableBuilder.getSymbolTable(), reports);
        initializationAnalysis.visit(root);

        if(TestUtils.getNumErrors(initializationAnalysis.getReports()) != 0) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, "Semantically invalid Program!"));
            return new JmmSemanticsResult(parserResult, null, reports);
        }

        return new JmmSemanticsResult(parserResult, tableBuilder.getSymbolTable(), reports);
    }
}