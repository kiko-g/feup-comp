import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmParserResult;

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
        JmmParserResult result = new JmmParserResult(null, new ArrayList<>());

		try {
			result = Parser.run(resource);
			TestUtils.noErrors(result.getReports());
        } catch (IOException e) {
            System.err.println("Exception: " + e.getMessage());
		} catch (RuntimeException ignored) {
        } finally {
		    Utils.printReports(result.getReports());
        }
	}
}