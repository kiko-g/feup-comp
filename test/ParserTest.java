import static org.junit.Assert.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public class ParserTest {
    private void test(String resource, boolean mustFail) {
        String jmmExtension = ".jmm";

        String file = resource.substring(resource.lastIndexOf("/"));
        String extension = file.substring(file.indexOf("."));
        if(!extension.equals(jmmExtension)) {
            throw new IllegalArgumentException(String.format("Test resource must have a %s extension", jmmExtension));
        }

        Path fileName = Path.of(resource);
        String content = null;
        try {
            content = Files.readString(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        boolean success = true;

        try {
            TestUtils.parse(content);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            e.printStackTrace();
            success = false;
        } finally { }

        if (mustFail) {
            success = !success;
        }

        if (!success) {
            if (mustFail) {
                fail("Expected exception to be thrown!");
            } else {
                fail("Expected parser to succeed!");
            }
        }
    }


    /*@Test
    public void testExpression() {		
		assertEquals("Expression", TestUtils.parse("2+3\n").getRootNode().getKind());
	}*/

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
    public void testmiss_type() {
        test("test/fixtures/public/fail/semantic/extra/miss_type.jmm", true);
    }

    @Test
    public void testarr_index_not_int() {
        test("test/fixtures/public/fail/semantic/arr_index_not_int.jmm", true);
    }

    @Test
    public void testarr_size_not_int() {
        test("test/fixtures/public/fail/semantic/arr_size_not_int.jmm", true);
    }

    @Test
    public void testbadArguments() {
        test("test/fixtures/public/fail/semantic/badArguments.jmm", true);
    }

    @Test
    public void testbinop_incomp() {
        test("test/fixtures/public/fail/semantic/binop_incomp.jmm", true);
    }

    @Test
    public void testfuncNotFound() {
        test("test/fixtures/public/fail/semantic/funcNotFound.jmm", true);
    }

    @Test
    public void testsimple_length() {
        test("test/fixtures/public/fail/semantic/simple_length.jmm", true);
    }

    @Test
    public void testvar_exp_incomp() {
        test("test/fixtures/public/fail/semantic/var_exp_incomp.jmm", true);
    }

    @Test
    public void testvar_lit_incomp() {
        test("test/fixtures/public/fail/semantic/var_lit_incomp.jmm", true);
    }

    @Test
    public void testvar_undef() {
        test("test/fixtures/public/fail/semantic/var_undef.jmm", true);
    }

    @Test
    public void testvarNotInit() {
        test("test/fixtures/public/fail/semantic/varNotInit.jmm", true);
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
