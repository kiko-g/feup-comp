package pt.up.fe.comp.jmm.analysis;

import pt.up.fe.comp.jmm.JmmParserResult;

/**
 * This stage deals with analysis performed at the AST level, essentially semantic analysis and symbol table generation. 
 */
public interface JmmAnalysis {

	JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult);
		
}