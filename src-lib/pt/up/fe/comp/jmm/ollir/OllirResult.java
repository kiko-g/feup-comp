package pt.up.fe.comp.jmm.ollir;

import java.util.List;

import org.specs.comp.ollir.ClassUnit;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsCollections;

/**
 * An OLLIR result returns the parsed OLLIR code and the corresponding symbol table.
 */
public class OllirResult {

    private final ClassUnit ollirClass;
    private final SymbolTable symbolTable;
    private final List<Report> reports;

    public OllirResult(ClassUnit ollirClass, SymbolTable symbolTable, List<Report> reports) {
        this.ollirClass = ollirClass;
        this.symbolTable = symbolTable;
        this.reports = reports;
    }

    /**
     * Creates a new instance from the analysis stage results and a String containing OLLIR code.
     * 
     * @param semanticsResult
     * @param ollirCode
     * @param reports
     */
    public OllirResult(JmmSemanticsResult semanticsResult, String ollirCode, List<Report> reports) {
        this(OllirUtils.parse(ollirCode), semanticsResult.getSymbolTable(),
                SpecsCollections.concat(semanticsResult.getReports(), reports));
    }

    public ClassUnit getOllirClass() {
        return this.ollirClass;
    }

    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    public List<Report> getReports() {
        return this.reports;
    }
}
