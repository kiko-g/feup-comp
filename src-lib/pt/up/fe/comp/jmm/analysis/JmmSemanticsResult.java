package pt.up.fe.comp.jmm.analysis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmSerializer;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsCollections;

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

    public JmmSemanticsResult(JmmParserResult parserResult, SymbolTable symbolTable, List<Report> reports) {
        this(parserResult.getRootNode(), symbolTable, SpecsCollections.concat(parserResult.getReports(), reports));
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

    public String toJson() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(JmmNode.class, new JmmSerializer())
                .create();
        return gson.toJson(this, JmmParserResult.class);
    }
}
