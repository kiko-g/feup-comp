import static org.junit.Assert.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmParserResult;

public class ParserTest {
    private void test(String resource, boolean mustFail) {
        try {
            String content = Utils.getResourceContent(resource, resource.substring(resource.lastIndexOf("/")));
            JmmParserResult result = TestUtils.parse(content);
            Utils.saveJson(Utils.getFilename(resource), result.toJson());

            if (mustFail) {
                TestUtils.mustFail(result.getReports());
            } else {
                TestUtils.noErrors(result.getReports());
            }
        } catch (IOException | ParserException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testFindMaximum() {
        test("test/fixtures/public/FindMaximum.jmm", false);
    }

    @Test
    public void testHelloWorld() {
        test("test/fixtures/public/HelloWorld.jmm", false);
    }

    @Test
    public void testLazysort() {
        test("test/fixtures/public/Lazysort.jmm", false);
    }

    @Test
    public void testLife() {
        test("test/fixtures/public/Life.jmm", false);
    }

    @Test
    public void testMonteCarloPi() {
        test("test/fixtures/public/MonteCarloPi.jmm", false);
    }

    @Test
    public void testQuickSort() {
        test("test/fixtures/public/QuickSort.jmm", false);
    }

    @Test
    public void testSimple() {
        test("test/fixtures/public/Simple.jmm", false);
    }

    @Test
    public void testTicTacToe() {
        test("test/fixtures/public/TicTacToe.jmm", false);
    }

    @Test
    public void testWhileAndIF() {
        test("test/fixtures/public/WhileAndIF.jmm", false);
    }

    @Test
    public void testBlowUp() {
        test("test/fixtures/public/fail/syntactical/BlowUp.jmm", true);
    }

    @Test
    public void testCompleteWhileTest() {
        test("test/fixtures/public/fail/syntactical/CompleteWhileTest.jmm", true);
    }

    @Test
    public void testLengthError() {
        test("test/fixtures/public/fail/syntactical/LengthError.jmm", true);
    }

    @Test
    public void testMissingRightPar() {
        test("test/fixtures/public/fail/syntactical/MissingRightPar.jmm", true);
    }

    @Test
    public void testMultipleSequential() {
        test("test/fixtures/public/fail/syntactical/MultipleSequential.jmm", true);
    }

    @Test
    public void testNestedLoop() {
        test("test/fixtures/public/fail/syntactical/NestedLoop.jmm", true);
    }
}
