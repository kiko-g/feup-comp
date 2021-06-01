import report.StyleReport;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static InputStream toInputStream(String text) {
        try {
            return new ByteArrayInputStream(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String getResourceContent(String resource, String filename) throws IOException {
        String jmmExtension = ".jmm";
        String extension = filename.substring(filename.indexOf("."));
        if (!extension.equals(jmmExtension)) {
            throw new IllegalArgumentException(String.format("Resource must have a %s extension", jmmExtension));
        }

        Path fileName = Path.of(resource);
        return Files.readString(fileName);
    }

    public static String getFilename(String originalFilePath) {
        return originalFilePath.substring(originalFilePath.lastIndexOf(File.separator) + 1, originalFilePath.lastIndexOf(".")) + ".json";
    }

    public static void saveFile(String fileName, String folder, String content) throws IOException {
        Path path = Path.of(folder);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        Path jsonFile = Path.of(path.toString(), fileName);
        FileWriter writer = new FileWriter(String.valueOf(jsonFile));
        writer.write(content);
        writer.close();
        System.out.println("Saved file " + fileName + " in " + folder + "!");
    }

    public static void printReports(List<StyleReport> reports) {
        if(reports == null || reports.size() == 0) {
            System.out.println("No reports were generated");
            return;
        }

        for (StyleReport report : reports) {
            switch (report.getType()) {
                case WARNING:
                case ERROR:
                    System.err.println(report);
                    break;
                case DEBUG:
                case LOG:
                default:
                    System.out.println(report);
                    break;
            }
        }
    }

    public static List<StyleReport> concatReports(List<StyleReport> ... reports) {
        List<StyleReport> concatReports = new ArrayList<>();
        for(List<StyleReport> stageReport: reports) {
            concatReports.addAll(stageReport);
        }

        return concatReports;
    }

    public static void runTimeExec(String cmd) {
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
