import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;

import java.io.StringReader;
import java.util.Arrays;

public class Main implements JmmParser {


	public JmmParserResult parse(String jmmCode) {
		
		try {
			Jmm parser = new Jmm(new StringReader(jmmCode));
    		// SimpleNode root = parser.Program(); // returns reference to root node
    		// root.dump(""); // prints the tree on the screen
    		// return new JmmParserResult(root, new ArrayList<Report>());
		} catch(Exception e) {
			throw new RuntimeException("Error while parsing", e);
		}

		return null;
	}

    public static void main(String[] args) {
        System.out.println("Executing with args: " + Arrays.toString(args));
        if (args[0].contains("fail")) {
            throw new RuntimeException("It's supposed to fail");
        }
    }
}