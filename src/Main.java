import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.specs.util.SpecsCollections;
import report.Report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
	public static boolean OPTIMIZATIONS;
	public static int NUM_REGISTERS = 0;
	public static String INPUT_FILE;

	public static void main(String[] args) throws ParseException {
		parseArguments(args);

		JmmParserResult parserResult = new JmmParserResult(null, new ArrayList<>());
		JmmSemanticsResult semanticsResult = new JmmSemanticsResult((JmmNode) null, null, new ArrayList<>());
//		OllirResult ollirResult = new OllirResult(new ClassUnit(), semanticsResult.getSymbolTable(), new ArrayList<>());
//		JasminResult jasminResult = new JasminResult(ollirResult, "", new ArrayList<>());

		try {
			parserResult = Parser.run(INPUT_FILE);
            semanticsResult = Analysis.run(parserResult);
//			String ollirCode = new OptimizationStage().toCode(semanticsResult);
//            System.out.println(ollirCode);
//			ollirResult = new OptimizationStage().toOllir(semanticsResult);
//			jasminResult = BackendStage.run(ollirResult);
//			jasminResult.run();
//			jasminResult,compule();
		} catch (IOException e) {
			System.err.println("Exception: " + e.getMessage());
		} catch (RuntimeException e) {
		    e.printStackTrace();
        } finally {
			List<Report> reports = SpecsCollections.concat(parserResult.getReports(), semanticsResult.getReports());
//            reports = SpecsCollections.concat(reports, ollirResult.getReports());

			reports.forEach(System.err::println);
		}
	}

	private static void parseArguments(String[] args) throws ParseException {
		if(args.length < 1) {
			System.err.println("Not enough arguments provided.");
			System.err.println("Usage: \"java -jar jmm.jar [-r=<num>] [-o] <input_file.jmm>\"");
			return;
		}

		List<String> arguments = new ArrayList<>(Arrays.asList(args));

		int optIndex;
		if((optIndex = arguments.indexOf("-r")) != -1) {
			try {
				NUM_REGISTERS = Integer.parseInt(arguments.get(optIndex + 1));
				if(NUM_REGISTERS <= 0 || NUM_REGISTERS>255) {
					throw new IllegalArgumentException("Invalid argument provided for -r option: Must be positive integer smaller than 255.");
				}
			} catch (NumberFormatException e) {
				throw new ParseException("No argument provided for -r option.");
			}
		}

		if(arguments.contains(("-o"))) {
			OPTIMIZATIONS = true;
		}

		INPUT_FILE = arguments.get(arguments.size() - 1);
	}
}