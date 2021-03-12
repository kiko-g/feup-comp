package pt.up.fe.comp.jmm.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Report {

    private final ReportType type;
    private final Stage stage;
    private final int line;
    private final int column;
    private final String message;

    public Report(ReportType type, Stage stage, int line, int column, String message) {
        this.type = type;
        this.stage = stage;
        this.line = line;
        this.column = column;
        this.message = message;
    }

    public ReportType getType() {
        return this.type;
    }

    public Stage getStage() {
        return this.stage;
    }

    public int getLine() {
        return this.line;
    }

    public int getColumn() {
        return this.column;
    }

    public String getMessage() {
        return this.message;
    }

    public String toJson() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return gson.toJson(this, Report.class);
    }

    @Override
    public String toString() {
        return this.type + "@" + this.stage + ", line " + this.line + ", column " + this.column + ":\n" + this.message;
    }
}
