import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import org.junit.Test;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.TestUtils;
import report.Report;

import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.fail;

public class ParserTest {
    private void test(Path resource, boolean mustFail) {
        try {
            String content = Utils.getResourceContent(resource.toString(), resource.getFileName().toString());
            JmmParserResult result = TestUtils.parse(content);
            Utils.saveFile(Utils.getFilename(resource.toString()), "generated/json", result.toJson());

            if (mustFail) {
                TestUtils.mustFail(result.getReports());
            } else {
                TestUtils.noErrors(result.getReports());
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
}
