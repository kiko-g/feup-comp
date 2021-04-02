import pt.up.fe.comp.jmm.JmmNode;
import parser.JmmParserResult;
import analysis.JmmSemanticsResult;
import report.Report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
	public static void main(String[] args) {
		if(args.length != 1) {
			String usage = "java -jar comp2021-1a.jar Main";
			System.err.println("Usage: " + usage + " <jmm file path>\n" +
					"Example: " + usage + " test/fixtures/public/Simple.jmm");
			return;
		}

		String resource = args[0];
        JmmParserResult parserResult = new JmmParserResult(null, new ArrayList<>());
		JmmSemanticsResult semanticsResult = new JmmSemanticsResult((JmmNode) null, null, new ArrayList<>());

		try {
			parserResult = Parser.run(resource);
			semanticsResult = new Analysis().semanticAnalysis(parserResult);
		} catch (IOException e) {
			System.err.println("Exception: " + e.getMessage());
		} catch (RuntimeException ignored) { } finally {
			List<Report> reports = Utils.concatReports(parserResult.getReports(), semanticsResult.getReports());
			reports.forEach(System.err::println);
		}
	}
}