package pt.up.fe.comp.jmm.parser;

/**
 * Parses J-- code.
 * 
 * @author COMP2021
 *
 */
public interface JmmParser {
	
	JmmParserResult parse(String jmmCode);
	
}