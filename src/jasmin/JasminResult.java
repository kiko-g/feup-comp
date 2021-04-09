package jasmin;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import pt.up.fe.comp.TestUtils;
import ollir.OllirResult;
import report.Report;
import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;

/**
 * A semantic analysis returns the analysed tree and the generated symbol table.
 */
public class JasminResult {
    private final String className;
    private final String jasminCode;
    private final List<Report> reports;

    public JasminResult(String className, String jasminCode, List<Report> reports) {
        this.className = className;
        this.jasminCode = jasminCode;
        this.reports = reports;
    }

    public JasminResult(OllirResult ollirResult, String jasminCode, List<Report> reports) {
        this(ollirResult.getOllirClass().getClassName(), jasminCode,
            SpecsCollections.concat(ollirResult.getReports(), reports));
    }

    public String getClassName() {
        return className;
    }

    public String getJasminCode() {
        return this.jasminCode;
    }

    public List<Report> getReports() {
        return this.reports;
    }

    /**
     * Compiles the generated Jasmin code using the Jasmin tool.
     * 
     * @param outputDir
     *            the folder where the class file will written
     * @return a reference to the .class file
     */
    public File compile(File outputDir) {
        File jasminFile = new File(SpecsIo.getTempFolder("jasmin"), getClassName() + ".j");
        SpecsIo.write(jasminFile, getJasminCode());
        return JasminUtils.assemble(jasminFile, outputDir);
    }

    /**
     * Compiles the generated Jasmin code using the Jasmin tool.
     * 
     * @return the compiled class file
     */
    public File compile() {
        File outputDir = SpecsIo.getTempFolder("jasmin");
        return compile(outputDir);
    }

    /**
     * Compiles and runs the current Jasmin code.
     * 
     * @param classpath
     *            additional paths for the classpath
     * @return the output that is printed by the Jasmin program
     */
    public String run(List<String> classpath) {
        var classFile = compile();

        StringBuilder classpathArg = new StringBuilder(classFile.getParentFile().getAbsolutePath());
        if (!classpath.isEmpty()) {
            var sep = System.getProperty("path.separator");
            for (var classpathElement : classpath) {
                classpathArg.append(sep).append(classpathElement);
            }
        }

        var classname = SpecsIo.removeExtension(classFile.getName());
        var output = SpecsSystem.runProcess(Arrays.asList("java", "-cp", classpathArg.toString(), classname), true, true);
        return output.getOutput();
    }

    /**
     * Compiles and runs the current Jasmin code.
     * 
     * @return the output that is printed by the Jasmin program
     */
    public String run() {
        return run(Arrays.asList(TestUtils.getLibsClasspath()));
    }
}