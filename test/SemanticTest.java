import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import org.junit.Test;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.TestUtils;
import report.Report;

import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.fail;

public class SemanticTest {
    private void test(Path resource, boolean mustFail) {
        try {
            String content = Utils.getResourceContent(resource.toString(), resource.getFileName().toString());
            JmmParserResult result = TestUtils.parse(content);
            Utils.saveFile(Utils.getFilename(resource.toString()), "generated/json", result.toJson());

            List<Report> reports = result.getReports();

            if(result.getRootNode() != null) {
                JmmSemanticsResult semanticsResult = TestUtils.analyse(result);
                reports = semanticsResult.getReports();
            }

            if (mustFail) {
                TestUtils.mustFail(reports);
            } else {
                TestUtils.noErrors(reports);
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testFindMaximum() {
        test(Path.of("test/fixtures/public/FindMaximum.jmm"), false);
    }

    @Test
    public void testHelloWorld() {
        test(Path.of("test/fixtures/public/HelloWorld.jmm"), false);
    }

    @Test
    public void testLazysort() {
        test(Path.of("test/fixtures/public/Lazysort.jmm"), false);
    }

    @Test
    public void testLife() {
        test(Path.of("test/fixtures/public/Life.jmm"), false);
    }

    @Test
    public void testMonteCarloPi() {
        test(Path.of("test/fixtures/public/MonteCarloPi.jmm"), false);
    }

    @Test
    public void testQuickSort() {
        test(Path.of("test/fixtures/public/QuickSort.jmm"), false);
    }

    @Test
    public void testSimple() {
        test(Path.of("test/fixtures/public/Simple.jmm"), false);
    }

    @Test
    public void testTicTacToe() {
        test(Path.of("test/fixtures/public/TicTacToe.jmm"), false);
    }

    @Test
    public void testWhileAndIF() {
        test(Path.of("test/fixtures/public/WhileAndIF.jmm"), false);
    }

    /**
     * Public Tests - Syntactic: Fail
     */
    @Test
    public void testBlowUp() {
        test(Path.of("test/fixtures/public/fail/syntactical/BlowUp.jmm"), true);
    }

    @Test
    public void testCompleteWhileTest() {
        test(Path.of("test/fixtures/public/fail/syntactical/CompleteWhileTest.jmm"), true);
    }

    @Test
    public void testLengthError() {
        test(Path.of("test/fixtures/public/fail/syntactical/LengthError.jmm"), true);
    }

    @Test
    public void testMissingRightPar() {
        test(Path.of("test/fixtures/public/fail/syntactical/MissingRightPar.jmm"), true);
    }

    @Test
    public void testMultipleSequential() {
        test(Path.of("test/fixtures/public/fail/syntactical/MultipleSequential.jmm"), true);
    }

    @Test
    public void testNestedLoop() {
        test(Path.of("test/fixtures/public/fail/syntactical/NestedLoop.jmm"), true);
    }

    /**
     * Public Tests - Semantic: Fail
     */
    @Test
    public void testArrIndexNotInt() {
        test(Path.of("test/fixtures/public/fail/semantic/arr_index_not_int.jmm"), true);
    }

    @Test
    public void testArrSizeNotInt() {
        test(Path.of("test/fixtures/public/fail/semantic/arr_size_not_int.jmm"), true);
    }

    @Test
    public void testBadArguments() {
        test(Path.of("test/fixtures/public/fail/semantic/badArguments.jmm"), true);
    }

    @Test
    public void testBinopIncomp() {
        test(Path.of("test/fixtures/public/fail/semantic/binop_incomp.jmm"), true);
    }

    @Test
    public void testFuncNotFound() {
        test(Path.of("test/fixtures/public/fail/semantic/funcNotFound.jmm"), true);
    }

    @Test
    public void testSimpleLength() {
        test(Path.of("test/fixtures/public/fail/semantic/simple_length.jmm"), true);
    }

    @Test
    public void testVarExpIncom() {
        test(Path.of("test/fixtures/public/fail/semantic/var_exp_incomp.jmm"), true);
    }

    @Test
    public void testVarLitIncomp() {
        test(Path.of("test/fixtures/public/fail/semantic/var_lit_incomp.jmm"), true);
    }

    @Test
    public void testVarUndef() {
        test(Path.of("test/fixtures/public/fail/semantic/var_undef.jmm"), true);
    }

    @Test
    public void testMissType() {
        test(Path.of("test/fixtures/public/fail/semantic/extra/miss_type.jmm"), true);
    }

    /**
     * Private Tests
     */
    @Test
    public void testTuring() {
        test(Path.of("test/fixtures/private/Turing.jmm"), false);
    }

    @Test
    public void testTuringV2() {
        test(Path.of("test/fixtures/private/TuringV2.jmm"), false);
    }

    @Test
    public void testArrayAssign() {
        test(Path.of("test/fixtures/private/ArrayAssign.jmm"), false);
    }

    @Test
    public void testArrayAssignFail() {
        test(Path.of("test/fixtures/private/fail/ArrayAssignFail.jmm"), true);
    }

    @Test
    public void testDifferentOperandTypes() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_op_same_type.jmm"), true);
    }

    @Test
    public void testDirectArrayOperations() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_direct_array_ops.jmm"), true);
    }

    @Test
    public void testDirectIntegerArrayAccess() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_array_access.jmm"), true);
    }

    @Test
    public void testArrayIndex1() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_array_access_index.jmm"), true);
    }

    @Test
    public void testAssigmentOk() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_assignment_ok.jmm"), true);
    }

    @Test
    public void testAssigmentFail() {
        test(Path.of("test/fixtures/private/semantic/type_verification/test_assignment_fail.jmm"), true);
    }

    @Test
    public void testTarget1() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_existence_target.jmm"), true);
    }

    @Test
    public void testTarget2() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_existence_target_with_super.jmm"), false);
    }

    @Test
    public void testTargetWithImport() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_existence_import.jmm"), false);
    }

    @Test
    public void testMethodParameters() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_parameters_number.jmm"), true);
    }

    @Test
    public void testNumberParameters() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_number_parameters.jmm"), true);
    }
}
