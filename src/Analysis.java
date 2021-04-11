import analysis.AnalysisTableBuilder;
import analysis.TypeAnalysis;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmNode;
import parser.JmmParserResult;
import analysis.JmmAnalysis;
import analysis.JmmSemanticsResult;
import report.Report;
import report.ReportType;
import report.Stage;

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
        List<Report> reports = parserResult.getReports();

        AnalysisTableBuilder tableBuilder = new AnalysisTableBuilder(reports);
        tableBuilder.visit(root);

        if(TestUtils.getNumErrors(tableBuilder.getReports()) != 0) {
            tableBuilder.getReports().add(new Report(ReportType.ERROR, Stage.SEMANTIC, "Semantically invalid Program!"));
            return new JmmSemanticsResult(parserResult, null, tableBuilder.getReports());
        }

        TypeAnalysis typeAnalysis = new TypeAnalysis(tableBuilder.getSymbolTable(), reports);
        typeAnalysis.visit(root);

        if(TestUtils.getNumErrors(typeAnalysis.getReports()) != 0) {
            tableBuilder.getReports().add(new Report(ReportType.ERROR, Stage.SEMANTIC, "Semantically invalid Program!"));
            return new JmmSemanticsResult(parserResult, null, tableBuilder.getReports());
        }

        /*System.out.println("Dump tree with Visitor where you control tree traversal");
        ExampleVisitor visitor = new ExampleVisitor("Identifier", "id");
        System.out.println(visitor.visit(node, ""));

        System.out.println("Dump tree with Visitor that automatically performs preorder tree traversal");
        var preOrderVisitor = new ExamplePreorderVisitor("Identifier", "id");
        System.out.println(preOrderVisitor.visit(node, ""));

        System.out.println(
                "Create histogram of node kinds with Visitor that automatically performs postorder tree traversal");
        var postOrderVisitor = new ExamplePostorderVisitor();
        var kindCount = new HashMap<String, Integer>();
        postOrderVisitor.visit(node, kindCount);
        System.out.println("Kinds count: " + kindCount + "\n");

        System.out.println(
                "Print variables name and line, and their corresponding parent with Visitor that automatically performs preorder tree traversal");
        var varPrinter = new ExamplePrintVariables("Variable", "name", "line");
        varPrinter.visit(node, null);*/

        return new JmmSemanticsResult(parserResult, tableBuilder.getSymbolTable(), tableBuilder.getReports());
    }
}