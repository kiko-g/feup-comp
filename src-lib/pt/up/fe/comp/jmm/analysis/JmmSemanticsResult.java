package pt.up.fe.comp.jmm.analysis;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.report.Report;

import java.util.List;

/**
 * A semantic analysis returns the analysed tree and the generated symbol table.
 */
public class JmmSemanticsResult {

    private final JmmNode rootNode;
    private final SymbolTable symbolTable;
    private final List<Report> reports;

    public JmmSemanticsResult(JmmNode rootNode, SymbolTable symbolTable, List<Report> reports) {
        this.rootNode = rootNode;
        this.symbolTable = symbolTable;
        this.reports = reports;
    }
	
	public JmmNode getRootNode() {
		return this.rootNode;
	}

    public SymbolTable getSymbolTable() {
		return this.symbolTable;
	}
	
	public List<Report> getReports() {
		return this.reports;
	}	
}

