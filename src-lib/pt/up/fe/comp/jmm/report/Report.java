package pt.up.fe.comp.jmm.report;

import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Report {

    private final ReportType type;
    private final Stage stage;
    private final int line;
    private final int column;
    private final String message;

    private Exception exception;

    public Report(ReportType type, Stage stage, int line, int column, String message) {
        this.type = type;
        this.stage = stage;
        this.line = line;
        this.column = column;
        this.message = message;
        this.exception = null;
    }

    public Report(ReportType type, Stage stage, int line, String message) {
        this(type, stage, line, -1, message);
    }

    public static Report newError(Stage stage, int line, int column, String message, Exception e) {
        var report = new Report(ReportType.ERROR, stage, line, message);
        report.setException(e);
        return report;
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

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

    public String toJson() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                // .excludeFieldsWithoutExposeAnnotation()
                .create();
        return gson.toJson(this, Report.class);
    }

    @Override
    public String toString() {
        String message = this.type + "@" + this.stage + ", line " + this.line + ", col " + this.column + ": "
                + this.message;

        if (exception != null) {
            message += " (exception: " + exception.getMessage() + ")";
        }

        return message;
    }
}
