package pt.up.fe.comp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Properties;


import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.JmmParser;

import pt.up.fe.specs.util.SpecsIo;


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

			// Get Parser class
			String parserClassName = PARSER_CONFIG.getProperty("ParserClass");

            // Get class with main
            Class<?> parserClass = Class.forName(parserClassName);

            // It is expected that the Parser class can be instantiated without arguments
			JmmParser parser = (JmmParser) parserClass.getConstructor().newInstance();
			
			return parser.parse(code);

        } catch (Exception e) {
			throw new RuntimeException("Could not parse code", e);
        }		
		
	}

}