import analysis.table.AnalysisTable;
import analysis.table.AnalysisTableBuilder;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmNode;
import parser.JmmParserResult;
import analysis.JmmAnalysis;
import analysis.JmmSemanticsResult;
import report.Report;

import java.util.ArrayList;
import java.util.List;

public class Analysis implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        // Checks input
        TestUtils.noErrors(parserResult.getReports());

        JmmNode root = parserResult.getRootNode();
        List<Report> reports = parserResult.getReports();

        AnalysisTable symbolTable = new AnalysisTable();
        AnalysisTableBuilder tableBuilder = new AnalysisTableBuilder(symbolTable, reports);

        System.out.println(tableBuilder.visit(root, ""));

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

        // No Symbol Table being calculated yet
        return new JmmSemanticsResult(parserResult, null, new ArrayList<>());

    }

}