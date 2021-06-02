import org.specs.comp.ollir.parser.ParseException;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
	public static boolean OPTIMIZATIONS;
	public static int NUM_REGISTERS = 0;
	public static String INPUT_FILE;
	public static ArrayList<String> classpath = new ArrayList<>(Arrays.asList("generated/class", TestUtils.getLibsClasspath()));

	public static void main(String[] args) {
		try {
			parseArguments(args);
		} catch(Exception e) {
			System.err.println(e.getMessage());
			return;
		}

		JmmParserResult parserResult = null;
		JmmSemanticsResult semanticsResult = null;
		OllirResult ollirResult = null;
		JasminResult jasminResult = null;

		try {
			parserResult = Parser.run(INPUT_FILE);
            semanticsResult = Analysis.run(parserResult);
			ollirResult = OptimizationStage.run(semanticsResult, OPTIMIZATIONS, NUM_REGISTERS);
			jasminResult = BackendStage.run(ollirResult);
			jasminResult.run(new ArrayList<>(), classpath);
		} catch (IOException | RuntimeException ignored) {
		} finally {
			List<Report> reports = new ArrayList<>(parserResult.getReports());
            if(semanticsResult != null && semanticsResult.getReports().size() > reports.size()) {
                reports = semanticsResult.getReports();
            }

            if(ollirResult != null && ollirResult.getReports().size() > reports.size()) {
                reports = ollirResult.getReports();
            }

            if(jasminResult != null && jasminResult.getReports().size() > reports.size()) {
                reports = jasminResult.getReports();
            }

            reports.forEach(System.err::println);
		}
	}

	private static void parseArguments(String[] args) throws ParseException {
		if(args.length < 1) {
			System.err.println("Usage: \"java -jar jmm.jar [-r=<num>] [-o] <input_file.jmm>\"");
			throw new ParseException("Not enough arguments provided.");
		}

		List<String> arguments = new ArrayList<>(Arrays.asList(args));

		Pattern p = Pattern.compile("-r=\\d");
		Matcher m = p.matcher(arguments.get(0));
		if(m.find())  {
			try {
				int numRegisters = Integer.parseInt(arguments.get(0).split("=")[1]);
				if(numRegisters > 0 && numRegisters < 255) {
					NUM_REGISTERS = numRegisters;
				} else {
					throw new IllegalArgumentException("Invalid argument provided for -r option: Must be positive integer smaller than 255.");
				}
			} catch (NumberFormatException e) {
				throw new ParseException("No argument provided for -r option.");
			}
		}

		if(arguments.contains(("-o"))) {
			OPTIMIZATIONS = true;
		}

		INPUT_FILE = Path.of(arguments.get(arguments.size() - 1)).toAbsolutePath().toString();
	}
}