package report;

import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class StyleReport extends Report {
    public StyleReport(ReportType type, Stage stage, int line, int column, String message) {
        super(type, stage, line, column, message);
    }

    public StyleReport(ReportType type, Stage stage, String message) {
        super(type, stage, -1, -1, message);
    }

    public StyleReport(ReportType type, Stage stage, int line, String message) {
        super(type, stage, line, message);
    }

    public static StyleReport newError(Stage stage, String message, Exception e) {
        var report = new StyleReport(ReportType.ERROR, stage, message);
        report.setException(e);
        return report;
    }

    @Override
    public String toString() {
        String message = "";

        if (this.getException().isPresent()) {
            message += " (exception: " + this.getException().get().getMessage() + ")\n\t";
        }

        if(this.getLine() != -1 && this.getColumn() != -1) {
            message += this.getType() + "@" + this.getStage() + ", line " + this.getLine() + ", column " + this.getColumn() + ":\n" + this.getMessage();
        } else {
            message += this.getType() + "@" + this.getStage() + ":\n" + this.getMessage();
        }

        return message;
    }
}
