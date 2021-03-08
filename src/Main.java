import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import static pt.up.fe.specs.util.SpecsIo.toInputStream;

public class Main implements JmmParser {
	public JmmParserResult parse(String jmmCode) {
		try {
			InputStream stream = toInputStream(jmmCode);
			Jmm parser = new Jmm(stream);
    		SimpleNode root = parser.Program();
    		root.dump("");
    		return new JmmParserResult((JmmNode) root, new ArrayList<Report>());
		} catch(Exception e) {
			throw new RuntimeException("Error while parsing", e);
		}
	}

    public static void main(String[] args) {
        System.out.println("Executing with args: " + Arrays.toString(args));
        if (args[0].contains("fail")) {
            throw new RuntimeException("It's supposed to fail");
        }
    }
}