import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.specs.util.SpecsIo;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class TestUtils {
	
	private static final Properties PARSER_CONFIG = TestUtils.loadProperties("parser.properties");
	
	public static Properties loadProperties(String filename) {
   		try {
			Properties props = new Properties();
			props.load(new StringReader(SpecsIo.read(filename)));
			return props;
		} catch(IOException e) {
			throw new RuntimeException("Error while loading properties file '"+filename+"'", e);
		}
	}

	public static JmmParserResult parse(String code) {
		try {
			String parserClassName = PARSER_CONFIG.getProperty("ParserClass");
            Class<?> parserClass = Class.forName(parserClassName);
			JmmParser parser = (JmmParser) parserClass.getConstructor().newInstance();
			return parser.parse(code);
        } catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not parse code\n\t at TestUtils.parse()");
        }
	}
}