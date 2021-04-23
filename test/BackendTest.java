import analysis.table.AnalysisTable;
import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.ollir.OllirUtils;
import pt.up.fe.specs.util.SpecsIo;

import java.nio.file.Path;
import java.util.ArrayList;

public class BackendTest {

    public void test(Path resource, boolean mustFail) {
        BackendStage stage = new BackendStage();
        AnalysisTable table = new AnalysisTable();
        //table.setSuper("SuperClass");
        OllirResult ollirRes = new OllirResult(
            OllirUtils.parse(SpecsIo.getResource(resource.toString())), table, new ArrayList<>()
        );

        JasminResult result = stage.toJasmin(ollirRes);

        //JasminResult result = TestUtils.backend(SpecsIo.getResource(resource.toString()));

        if (mustFail) {
            TestUtils.mustFail(result.getReports());
        } else {
            TestUtils.noErrors(result.getReports());
        }

        System.out.println(result.getJasminCode());

        //var output = result.run();
        //System.out.println(output);
        //assertEquals("Hello, World!", output.trim());
    }

    @Test
    public void testFac() {
        test(Path.of("fixtures/public/ollir/Fac.ollir"), false);
    }

    @Test
    public void testMyClass1() {
        test(Path.of("fixtures/public/ollir/myclass1.ollir"), false);
    }

    @Test
    public void testMyClass2() {
        test(Path.of("fixtures/public/ollir/myclass2.ollir"), false);
    }

    @Test
    public void testMyClass3() {
        test(Path.of("fixtures/public/ollir/myclass3.ollir"), false);
    }

    @Test
    public void testMyClass4() {
        test(Path.of("fixtures/public/ollir/myclass4.ollir"), false);
    }
}
