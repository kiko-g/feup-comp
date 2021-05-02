import analysis.table.AnalysisTable;
import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.ollir.OllirUtils;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;
import report.Report;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;


public class JMMTest {
    protected static class CorrectStageError extends Exception { }

    private void test(Path resource, Stage failStage) {
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
            OllirResult ollirResult = TestUtils.optimize(semanticsResult, true);
            checkErrors(ollirResult.getReports(), failStage);

            // Backend stage
            JasminResult backendResult = TestUtils.backend(ollirResult);
            backendResult.compile();
            checkErrors(ollirResult.getReports(), failStage);
        } catch (CorrectStageError ignored) {
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testJasmin(Path resource, boolean mustFail) {
        OllirResult ollirRes = new OllirResult(
                OllirUtils.parse(SpecsIo.getResource(resource.toString())), new AnalysisTable(), new ArrayList<>()
        );

        JasminResult result = new BackendStage().toJasmin(ollirRes);
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
                    fail(report.getMessage());
                }
            }
        }
    }

    @Test
    public void testFindMaximum() {
        test(Path.of("test/fixtures/public/FindMaximum.jmm"), Stage.OTHER);
    }

    @Test
    public void testHelloWorld() {
        test(Path.of("test/fixtures/public/HelloWorld.jmm"), Stage.OTHER);
    }

    @Test
    public void testLazysort() {
        test(Path.of("test/fixtures/public/Lazysort.jmm"), Stage.OTHER);
    }

    @Test
    public void testLife() {
        test(Path.of("test/fixtures/public/Life.jmm"), Stage.OTHER);
    }

    @Test
    public void testMonteCarloPi() {
        test(Path.of("test/fixtures/public/MonteCarloPi.jmm"), Stage.OTHER);
    }

    @Test
    public void testQuickSort() {
        test(Path.of("test/fixtures/public/QuickSort.jmm"), Stage.OTHER);
    }

    @Test
    public void testSimple() {
        test(Path.of("test/fixtures/public/Simple.jmm"), Stage.OTHER);
    }

    @Test
    public void testTicTacToe() {
        test(Path.of("test/fixtures/public/TicTacToe.jmm"), Stage.OTHER);
    }

    @Test
    public void testWhileAndIF() {
        test(Path.of("test/fixtures/public/WhileAndIF.jmm"), Stage.OTHER);
    }

    /**
     * Public Tests - Syntactic: Fail
     */
    @Test
    public void testBlowUp() {
        test(Path.of("test/fixtures/public/fail/syntactical/BlowUp.jmm"), Stage.SYNTATIC);
    }

    @Test
    public void testCompleteWhileTest() {
        test(Path.of("test/fixtures/public/fail/syntactical/CompleteWhileTest.jmm"), Stage.SYNTATIC);
    }

    @Test
    public void testLengthError() {
        test(Path.of("test/fixtures/public/fail/syntactical/LengthError.jmm"), Stage.SYNTATIC);
    }

    @Test
    public void testMissingRightPar() {
        test(Path.of("test/fixtures/public/fail/syntactical/MissingRightPar.jmm"), Stage.SYNTATIC);
    }

    @Test
    public void testMultipleSequential() {
        test(Path.of("test/fixtures/public/fail/syntactical/MultipleSequential.jmm"), Stage.SYNTATIC);
    }

    @Test
    public void testNestedLoop() {
        test(Path.of("test/fixtures/public/fail/syntactical/NestedLoop.jmm"), Stage.SYNTATIC);
    }

    /**
     * Public Tests - Semantic: Fail
     */
    @Test
    public void testArrIndexNotInt() {
        test(Path.of("test/fixtures/public/fail/semantic/arr_index_not_int.jmm"), Stage.SEMANTIC);
    }

    @Test
    public void testArrSizeNotInt() {
        test(Path.of("test/fixtures/public/fail/semantic/arr_size_not_int.jmm"), Stage.SEMANTIC);
    }

    @Test
    public void testBadArguments() {
        test(Path.of("test/fixtures/public/fail/semantic/badArguments.jmm"), Stage.SEMANTIC);
    }

    @Test
    public void testBinopIncomp() {
        test(Path.of("test/fixtures/public/fail/semantic/binop_incomp.jmm"), Stage.SEMANTIC);
    }

    @Test
    public void testFuncNotFound() {
        test(Path.of("test/fixtures/public/fail/semantic/funcNotFound.jmm"), Stage.SEMANTIC);
    }

    @Test
    public void testSimpleLength() {
        test(Path.of("test/fixtures/public/fail/semantic/simple_length.jmm"), Stage.SEMANTIC);
    }

    @Test
    public void testVarExpIncom() {
        test(Path.of("test/fixtures/public/fail/semantic/var_exp_incomp.jmm"), Stage.SEMANTIC);
    }

    @Test
    public void testVarLitIncomp() {
        test(Path.of("test/fixtures/public/fail/semantic/var_lit_incomp.jmm"), Stage.SEMANTIC);
    }

    @Test
    public void testVarUndef() {
        test(Path.of("test/fixtures/public/fail/semantic/var_undef.jmm"), Stage.SEMANTIC);
    }

    @Test
    public void testMissType() {
        test(Path.of("test/fixtures/public/fail/semantic/extra/miss_type.jmm"), Stage.SEMANTIC);
    }

    /**
     * Private Tests
     */
    @Test
    public void testTuring() {
        test(Path.of("test/fixtures/private/Turing.jmm"), Stage.OTHER);
    }

    @Test
    public void testTuringV2() {
        test(Path.of("test/fixtures/private/TuringV2.jmm"), Stage.OTHER);
    }

    @Test
    public void testArrayAssign() {
        test(Path.of("test/fixtures/private/ArrayAssign.jmm"), Stage.OTHER);
    }

    @Test
    public void testTarget2() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_existence_target_with_super.jmm"), Stage.OTHER);
    }

    @Test
    public void testTargetWithImport() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_existence_import.jmm"), Stage.OTHER);
    }

    /**
     * Private Tests - Syntactic: Fail
     */
    @Test
    public void testArrayAssignFail() {
        test(Path.of("test/fixtures/private/fail/ArrayAssignFail.jmm"), Stage.SYNTATIC);
    }

    @Test
    public void testDirectIntegerArrayAccess() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_array_access.jmm"), Stage.SYNTATIC);
    }

    /**
     * Private Tests - Semantic: Fail
     */
    @Test
    public void testDifferentOperandTypes() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_op_same_type.jmm"), Stage.SEMANTIC);
    }

    @Test
    public void testDirectArrayOperations() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_direct_array_ops.jmm"), Stage.SEMANTIC);
    }

    @Test
    public void testArrayIndex1() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_array_access_index.jmm"), Stage.SEMANTIC);
    }

    @Test
    public void testAssigmentOk() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_assignment_ok.jmm"), Stage.SEMANTIC);
    }

    @Test
    public void testAssigmentFail() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_assignment_fail.jmm"), Stage.SEMANTIC);
    }

    @Test
    public void testTarget1() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_existence_target.jmm"), Stage.SEMANTIC);
    }

    @Test
    public void testMethodParameters() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_parameters_number.jmm"), Stage.SEMANTIC);
    }

    @Test
    public void testNumberParameters() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_number_parameters.jmm"), Stage.SEMANTIC);
    }

    /**
     * Private Tests - Ollir code
     */
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

}
