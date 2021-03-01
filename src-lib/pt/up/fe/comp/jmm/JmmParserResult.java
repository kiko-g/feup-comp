package pt.up.fe.comp.jmm;

import pt.up.fe.comp.jmm.report.Report;

import java.util.List;

public class JmmParserResult {

    private final JmmNode rootNode;
    private final List<Report> reports;

    public JmmParserResult(JmmNode rootNode, List<Report> reports) {
        this.rootNode = rootNode;
        this.reports = reports;
    }
	
	public JmmNode getRootNode() {
		return this.rootNode;
	}
	
	public List<Report> getReports() {
		return this.reports;
	}	
}

