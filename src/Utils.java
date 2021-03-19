import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

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
        return originalFilePath.substring(originalFilePath.lastIndexOf('/') + 1, originalFilePath.lastIndexOf(".")) + ".json";
    }

    public static void saveJson(String jsonFileName, String content) throws IOException {
        Path path = Path.of("generated/json");
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }

        Path jsonFile = Path.of(path.toString(), jsonFileName);
        FileWriter writer = new FileWriter(String.valueOf(jsonFile));
        writer.write(content);
        writer.close();
    }
}
