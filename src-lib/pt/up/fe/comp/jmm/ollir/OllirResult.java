package pt.up.fe.comp.jmm.ollir;

import java.util.Collections;
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

    private final String ollirCode;
    private final ClassUnit ollirClass;
    private final SymbolTable symbolTable;
    private final List<Report> reports;

    private OllirResult(String ollirCode, ClassUnit ollirClass, SymbolTable symbolTable, List<Report> reports) {
        this.ollirCode = ollirCode;
        this.ollirClass = ollirClass;
        this.symbolTable = symbolTable;
        this.reports = reports;
    }

    public OllirResult(String ollirCode) {
        this.ollirCode = ollirCode;
        this.ollirClass = OllirUtils.parse(ollirCode);
        this.symbolTable = null;
        this.reports = Collections.emptyList();
    }

    /**
     * Creates a new instance from the analysis stage results and a String containing OLLIR code.
     * 
     * @param semanticsResult
     * @param ollirCode
     * @param reports
     */
    public OllirResult(JmmSemanticsResult semanticsResult, String ollirCode, List<Report> reports) {
        this(ollirCode, OllirUtils.parse(ollirCode), semanticsResult.getSymbolTable(),
                SpecsCollections.concat(semanticsResult.getReports(), reports));
    }

    public String getOllirCode() {
        return ollirCode;
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
