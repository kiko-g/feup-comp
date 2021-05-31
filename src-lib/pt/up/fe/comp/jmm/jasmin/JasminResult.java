package pt.up.fe.comp.jmm.jasmin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.system.OutputType;
import pt.up.fe.specs.util.system.ProcessOutputAsString;
import pt.up.fe.specs.util.system.StreamToString;
import pt.up.fe.specs.util.utilities.StringLines;

/**
 * A semantic analysis returns the analysed tree and the generated symbol table.
 */
public class JasminResult {

    private static Long HUMAN_DELAY_MS = 250l;
    private static Long TIMEOUT_NS = 5_000_000_000l;

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

        // Clean all class files in folder
        SpecsIo.deleteFolderContents(outputDir);

        return compile(outputDir);
    }

    /**
     * Compiles and runs the current Jasmin code.
     * 
     * @param args
     *            arguments for the Jasmin program
     * @param classpath
     *            additional paths for the classpath
     * @param input
     *            input to give to the program that will run
     * 
     * @return the output that is printed by the Jasmin program
     */
    public String run(List<String> args, List<String> classpath, String input) {
        // Compile
        var classFile = compile();

        var classpathArg = classFile.getParentFile().getAbsolutePath();
        if (!classpath.isEmpty()) {
            var sep = System.getProperty("path.separator");
            for (var classpathElement : classpath) {
                classpathArg += sep + classpathElement;
            }
        }

        var classname = SpecsIo.removeExtension(classFile.getName());

        var command = new ArrayList<String>();
        command.add("java");
        command.add("-cp");
        command.add(classpathArg);
        command.add(classname);
        command.addAll(args);

        // Build process
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(SpecsIo.getWorkingDir());
        Consumer<OutputStream> stdin = null;
        if (input != null && !input.isEmpty()) {
            stdin = outputStream -> {
                try (PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream)))) {
                    for (var line : StringLines.getLines(input)) {
                        // Simulate person typing (1s between each iteration)
                        SpecsSystem.sleep(HUMAN_DELAY_MS);
                        pw.println(line);
                        pw.flush();
                    }
                }

            };
        }

        var stdout = new StreamToString(true, true, OutputType.StdOut);
        var stderr = new StreamToString(true, true, OutputType.StdErr);

        var output = SpecsSystem.runProcess(builder, stdout, stderr, stdin, TIMEOUT_NS);

        // var output2 = SpecsSystem.runProcess(command, true, true);
        var processedOutput = new ProcessOutputAsString(output.getReturnValue(), output.getStdOut(),
                output.getStdErr());

        return processedOutput.getOutput();
    }

    public String run(List<String> args, List<String> classpath) {
        return run(args, classpath, null);
    }

    /**
     * Compiles and runs the current Jasmin code.
     * 
     * @param args
     *            arguments for the Jasmin program
     * @return the output that is printed by the Jasmin program
     */
    public String run(List<String> args) {
        return run(args, Arrays.asList(TestUtils.getLibsClasspath()));
    }

    /**
     * Compiles and runs the current Jasmin code.
     * 
     * @return the output that is printed by the Jasmin program
     */
    public String run() {
        return run(Collections.emptyList());
    }

    public String run(String input) {
        return run(Collections.emptyList(), Arrays.asList(TestUtils.getLibsClasspath()), input);
    }

    public String run(List<String> args, String input) {
        return run(args, Arrays.asList(TestUtils.getLibsClasspath()), input);
    }
}