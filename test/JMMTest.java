import org.junit.Assert;
import org.junit.Test;
import org.specs.comp.ollir.OllirErrorException;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.fail;


public class JMMTest {
    protected static class CorrectStageError extends Exception { }

    private void compile(Path resource) {
        try {
            String content = Utils.getResourceContent(resource.toString(), resource.getFileName().toString());

            // Parse stage
            JmmParserResult parserResult = TestUtils.parse(content);
            Utils.saveFile(Utils.getFilename(resource.toString()), "generated/json", parserResult.toJson());
            TestUtils.noErrors(parserResult.getReports());

            // Semantic stage
            JmmSemanticsResult semanticsResult = TestUtils.analyse(parserResult);
            TestUtils.noErrors(semanticsResult.getReports());

            // Optimization stage
            OllirResult ollirResult = TestUtils.optimize(semanticsResult, false);
            TestUtils.noErrors(ollirResult.getReports());

            // Backend stage
            JasminResult backendResult = TestUtils.backend(ollirResult);
            TestUtils.noErrors(backendResult.getReports());

            backendResult.getReports().forEach(System.err::println);
            backendResult.compile();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private String test(Path resource, String inputs, Stage failStage) {
        try {
            String content = Utils.getResourceContent(resource.toString(), resource.getFileName().toString());

            // Parse stage
            JmmParserResult parserResult = TestUtils.parse(content);
            Utils.saveFile(Utils.getFilename(resource.toString()), "generated/json", parserResult.toJson());
            checkErrors(parserResult.getReports(), failStage);

            // Semantic stage
            JmmSemanticsResult semanticsResult = TestUtils.analyse(parserResult);
            checkErrors(semanticsResult.getReports(), failStage);

            // Optimization stage
            OllirResult ollirResult = TestUtils.optimize(semanticsResult, false);
            checkErrors(ollirResult.getReports(), failStage);

            // Backend stage
            JasminResult backendResult = TestUtils.backend(ollirResult);
            checkErrors(backendResult.getReports(), failStage);

            backendResult.getReports().forEach(System.err::println);
            return backendResult.run(inputs);
        } catch (CorrectStageError ignored) {
        } catch (Exception e) {
            fail(e.getMessage());
        }

        return "";
    }

    private void test(Path resource, Path results, String inputs) {
        try {
            String result = Files.readString(results);
            Assert.assertEquals(result, test(resource, inputs, Stage.OTHER));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void test(Path resource, Path results, Path inputs) {
        try {
            test(resource, results, Files.readString(inputs));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testJasmin(Path resource, boolean mustFail) {
        OllirResult ollirResult = new OllirResult(SpecsIo.getResource(resource.toString()));

        try {
            ollirResult.getOllirClass().checkMethodLabels();
            ollirResult.getOllirClass().buildCFGs();
            ollirResult.getOllirClass().buildVarTables();
        } catch (OllirErrorException e) {
            e.printStackTrace();
        }

        JasminResult result = TestUtils.backend(ollirResult);
        result.compile();

        if (mustFail) {
            TestUtils.mustFail(result.getReports());
        } else {
            TestUtils.noErrors(result.getReports());
        }
    }

    private void checkErrors(List<Report> reports, Stage stage) throws CorrectStageError {
        for (Report report : reports) {
            if (report.getType() == ReportType.ERROR) {
                if(report.getStage() == stage) {
                    throw new CorrectStageError();
                } else {
                    fail(report.toString());
                }
            }
        }
    }

    @Test
    public void testFindMaximum() {
        test(Path.of("test/fixtures/public/FindMaximum.jmm"), "", Stage.OTHER);
    }

    @Test
    public void testHelloWorld() {
        test(Path.of("test/fixtures/public/HelloWorld.jmm"), "", Stage.OTHER);
    }

    @Test
    public void testLazysort() {
        compile(Path.of("test/fixtures/public/QuickSort.jmm"));
        test(Path.of("test/fixtures/public/Lazysort.jmm"), "", Stage.OTHER);
    }

    @Test
    public void testLife() {
        test(Path.of("test/fixtures/public/Life.jmm"), "", Stage.OTHER);
    }

    @Test
    public void testMonteCarloPi() {
        test(Path.of("test/fixtures/public/MonteCarloPi.jmm"), "100", Stage.OTHER);
    }

    @Test
    public void testQuickSort() {
        test(Path.of("test/fixtures/public/QuickSort.jmm"), Path.of("test/fixtures/public/QuickSort.txt"), "");
    }

    @Test
    public void testSimple() {
        test(Path.of("test/fixtures/public/Simple.jmm"), "", Stage.OTHER);
    }

    @Test
    public void testTicTacToe() {
        test(Path.of("test/fixtures/public/TicTacToe.jmm"), Path.of("test/fixtures/public/TicTacToe.txt"), Path.of("test/fixtures/public/TicTacToe.input"));
    }

    @Test
    public void testWhileAndIF() {
        test(Path.of("test/fixtures/public/WhileAndIF.jmm"), Path.of("test/fixtures/public/WhileAndIF.txt"), "");
    }

    /**
     * Public Tests - Syntactic: Fail
     */
    @Test
    public void testBlowUp() {
        test(Path.of("test/fixtures/public/fail/syntactical/BlowUp.jmm"), "", Stage.SYNTATIC);
    }

    @Test
    public void testCompleteWhileTest() {
        test(Path.of("test/fixtures/public/fail/syntactical/CompleteWhileTest.jmm"), "", Stage.SYNTATIC);
    }

    @Test
    public void testLengthError() {
        test(Path.of("test/fixtures/public/fail/syntactical/LengthError.jmm"), "", Stage.SYNTATIC);
    }

    @Test
    public void testMissingRightPar() {
        test(Path.of("test/fixtures/public/fail/syntactical/MissingRightPar.jmm"), "", Stage.SYNTATIC);
    }

    @Test
    public void testMultipleSequential() {
        test(Path.of("test/fixtures/public/fail/syntactical/MultipleSequential.jmm"), "", Stage.SYNTATIC);
    }

    @Test
    public void testNestedLoop() {
        test(Path.of("test/fixtures/public/fail/syntactical/NestedLoop.jmm"), "", Stage.SYNTATIC);
    }

    /**
     * Public Tests - Semantic: Fail
     */
    @Test
    public void testArrIndexNotInt() {
        test(Path.of("test/fixtures/public/fail/semantic/arr_index_not_int.jmm"), "", Stage.SEMANTIC);
    }

    @Test
    public void testArrSizeNotInt() {
        test(Path.of("test/fixtures/public/fail/semantic/arr_size_not_int.jmm"), "", Stage.SEMANTIC);
    }

    @Test
    public void testBadArguments() {
        test(Path.of("test/fixtures/public/fail/semantic/badArguments.jmm"), "", Stage.SEMANTIC);
    }

    @Test
    public void testBinopIncomp() {
        test(Path.of("test/fixtures/public/fail/semantic/binop_incomp.jmm"), "", Stage.SEMANTIC);
    }

    @Test
    public void testFuncNotFound() {
        test(Path.of("test/fixtures/public/fail/semantic/funcNotFound.jmm"), "", Stage.SEMANTIC);
    }

    @Test
    public void testSimpleLength() {
        test(Path.of("test/fixtures/public/fail/semantic/simple_length.jmm"), "", Stage.SEMANTIC);
    }

    @Test
    public void testVarExpIncom() {
        test(Path.of("test/fixtures/public/fail/semantic/var_exp_incomp.jmm"), "", Stage.SEMANTIC);
    }

    @Test
    public void testVarLitIncomp() {
        test(Path.of("test/fixtures/public/fail/semantic/var_lit_incomp.jmm"), "", Stage.SEMANTIC);
    }

    @Test
    public void testVarUndef() {
        test(Path.of("test/fixtures/public/fail/semantic/var_undef.jmm"), "", Stage.SEMANTIC);
    }

    @Test
    public void testMissType() {
        test(Path.of("test/fixtures/public/fail/semantic/extra/miss_type.jmm"), "", Stage.SEMANTIC);
    }

    /**
     * Private Tests
     */
    @Test
    public void testTuring() {
        test(Path.of("test/fixtures/private/Turing.jmm"), "", Stage.OTHER);
    }

    @Test
    public void testTuringV2() {
        test(Path.of("test/fixtures/private/TuringV2.jmm"), "", Stage.OTHER);
    }

    @Test
    public void testArrayAssign() {
        test(Path.of("test/fixtures/private/ArrayAssign.jmm"), "", Stage.OTHER);
    }

    @Test
    public void testTarget2() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_existence_target_with_super.jmm"), "", Stage.OTHER);
    }

    @Test
    public void testTargetWithImport() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_existence_import.jmm"), "", Stage.OTHER);
    }

    /**
     * Private Tests - Syntactic: Fail
     */
    @Test
    public void testArrayAssignFail() {
        test(Path.of("test/fixtures/private/fail/ArrayAssignFail.jmm"), "", Stage.SYNTATIC);
    }

    @Test
    public void testDirectIntegerArrayAccess() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_array_access.jmm"), "", Stage.SYNTATIC);
    }

    /**
     * Private Tests - Semantic: Fail
     */
    @Test
    public void testDifferentOperandTypes() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_op_same_type.jmm"), "", Stage.SEMANTIC);
    }

    @Test
    public void testDirectArrayOperations() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_direct_array_ops.jmm"), "", Stage.SEMANTIC);
    }

    @Test
    public void testArrayIndex1() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_array_access_index.jmm"), "", Stage.SEMANTIC);
    }

    @Test
    public void testAssigmentOk() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_assignment_ok.jmm"), "", Stage.SEMANTIC);
    }

    @Test
    public void testAssigmentFail() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_assignment_fail.jmm"), "", Stage.SEMANTIC);
    }

    @Test
    public void testTarget1() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_existence_target.jmm"), "", Stage.SEMANTIC);
    }

    @Test
    public void testMethodParameters() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_parameters_number.jmm"), "", Stage.SEMANTIC);
    }

    @Test
    public void testNumberParameters() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_number_parameters.jmm"), "", Stage.SEMANTIC);
    }

    /**
     * Private Tests - Ollir code
     */
    @Test
    public void testEx1() {
        testJasmin(Path.of("fixtures/private/ollir/ex1a.ollir"), false);
    }

    @Test
    public void testEx2() {
        testJasmin(Path.of("fixtures/private/ollir/ex1b.ollir"), false);
    }

    @Test
    public void testFac() {
        testJasmin(Path.of("fixtures/public/ollir/Fac.ollir"), false);
    }

    @Test
    public void testMyClass1() {
        testJasmin(Path.of("fixtures/public/ollir/myclass1.ollir"), false);
    }

    @Test
    public void testMyClass2() {
        testJasmin(Path.of("fixtures/public/ollir/myclass2.ollir"), false);
    }

    @Test
    public void testMyClass3() {
        testJasmin(Path.of("fixtures/public/ollir/myclass3.ollir"), false);
    }

    @Test
    public void testMyClass4() {
        testJasmin(Path.of("fixtures/public/ollir/myclass4.ollir"), false);
    }

    /**
     * Demo Tests
     */
    @Test
    public void testDemo1() {
        test(Path.of("test/fixtures/private/demo/Test1.jmm"), "", Stage.OTHER);
    }

    @Test
    public void testDemo2() {
        test(Path.of("test/fixtures/private/demo/Test2.jmm"), "", Stage.OTHER);
    }

    @Test
    public void testDemo3() {
        test(Path.of("test/fixtures/private/demo/Test3.jmm"), "", Stage.OTHER);
    }

    @Test
    public void testDemo4() {
        compile(Path.of("test/fixtures/private/demo/Shape.jmm"));
        compile(Path.of("test/fixtures/private/demo/Rectangle.jmm"));
        compile(Path.of("test/fixtures/private/demo/Triangle.jmm"));
        test(Path.of("test/fixtures/private/demo/Test4.jmm"), "", Stage.OTHER);
    }

    @Test
    public void testDemo5() {
        compile(Path.of("test/fixtures/private/demo/Shape.jmm"));
        compile(Path.of("test/fixtures/private/demo/Rectangle.jmm"));
        compile(Path.of("test/fixtures/private/demo/Triangle.jmm"));
        test(Path.of("test/fixtures/private/demo/Test5.jmm"), "", Stage.OTHER);
    }
}
