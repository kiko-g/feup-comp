package pt.up.fe.comp;

import java.util.Arrays;
import java.util.HashMap;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.examples.ExamplePostorderVisitor;
import pt.up.fe.comp.jmm.ast.examples.ExamplePreorderVisitor;
import pt.up.fe.comp.jmm.ast.examples.ExampleVisitor;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.specs.util.SpecsIo;

public class MainAnalysis implements JmmAnalysis { // }, JmmOptimization, JasminBackend {

    public static void main(String[] args) {
        System.out.println("Executing with args: " + Arrays.toString(args));

        if (args[0].contains("fail")) {
            throw new RuntimeException("It's supposed to fail");
        }

        var fileContents = SpecsIo.read(args[0]);
        System.out.println("Executing with input file: " + args[0]);

        JmmParserResult parserResult = TestUtils.parse(fileContents);

        // for CP2: symbol table generation and semantic analysis
        var analysis = new MainAnalysis();

        analysis.semanticAnalysis(parserResult);
    }

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {

        if (TestUtils.getNumReports(parserResult.getReports(), ReportType.ERROR) > 0) {
            return null;
        }

        if (parserResult.getRootNode() == null) {
            return null;
        }

        JmmNode node = parserResult.getRootNode().sanitize();

        System.out.println("VISITOR");
        ExampleVisitor visitor = new ExampleVisitor("Identifier", "id");
        System.out.println(visitor.visit(node, ""));

        System.out.println("PREORDER VISITOR");
        var preOrderVisitor = new ExamplePreorderVisitor("Identifier", "id");
        System.out.println(preOrderVisitor.visit(node, ""));

        System.out.println("POSTORDER VISITOR");
        var postOrderVisitor = new ExamplePostorderVisitor();
        var kindCount = new HashMap<String, Integer>();
        postOrderVisitor.visit(node, kindCount);
        System.out.println("Kinds count: " + kindCount);

        // No Symbol Table being calculated yet
        return new JmmSemanticsResult(node, null, parserResult.getReports());

    }

}