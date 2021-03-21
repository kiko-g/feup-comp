package pt.up.fe.comp.jmm;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pt.up.fe.comp.jmm.report.Report;

public class JmmParserResult {

    private final JmmNode rootNode;
    private final List<Report> reports;

    public JmmParserResult() {
        this.rootNode = null;
        this.reports = new ArrayList<>();
    }

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

    public String toJson() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(JmmNode.class, new JmmSerializer())
                .create();
        return gson.toJson(this, JmmParserResult.class);
    }
}
