package analysis;

import parser.JmmParserResult;

/**
 * This stage deals with analysis performed at the AST level, essentially semantic analysis and symbol table generation. 
 */
public interface JmmAnalysis {

	JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult);
		
}