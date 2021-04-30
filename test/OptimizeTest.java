
/**
 * Copyright 2021 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.nio.file.Path;

import static org.junit.Assert.fail;

public class OptimizeTest {
    private void test(Path resource, boolean mustFail) {
        try {
            String content = Utils.getResourceContent(resource.toString(), resource.getFileName().toString());

            OllirResult result = TestUtils.optimize(content);

            TestUtils.noErrors(result.getReports());
        } catch (Exception e) {
            if (!mustFail) {
                fail(e.getMessage());
            }
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
    public void testTarget2() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_existence_target_with_super.jmm"), false);
    }

    @Test
    public void testTargetWithImport() {
        test(Path.of("test/fixtures/private/semantic/method_verification/test_existence_import.jmm"), false);
    }
}
