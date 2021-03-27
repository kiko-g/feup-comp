import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
	public static void main(String[] args) {
		if(args.length != 2) {
			String usage = "java -jar comp2021-1a.jar Main";
			System.err.println("Usage: " + usage + " <jmm file path>\n" +
					"Example: " + usage + " test/fixtures/public/Simple.jmm");
			return;
		}

		String resource = args[1];
        JmmParserResult parserResult = new JmmParserResult(null, new ArrayList<>());
		JmmSemanticsResult semanticsResult = new JmmSemanticsResult(parserResult.getRootNode(), null, new ArrayList<>());

		try {
			parserResult = Parser.run(resource);
			semanticsResult = new Analyzer().semanticAnalysis(parserResult);
		} catch (IOException e) {
			System.err.println("Exception: " + e.getMessage());
		} catch (RuntimeException ignored) { } finally {
			if(semanticsResult.getReports().size() > 0) {
				switch (semanticsResult.getReports().get(semanticsResult.getReports().size() - 1).getStage()) {
					case SYNTATIC:
						Utils.printReports(parserResult.getReports());
						break;
					case SEMANTIC:
						Utils.printReports(semanticsResult.getReports());
						break;
					default:
						break;
				}
			}
		}
	}
}