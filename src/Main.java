import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static pt.up.fe.specs.util.SpecsIo.toInputStream;

public class Main implements JmmParser {
	public static void main(String[] args) {
		if(args.length != 2) {
			String usage = "java -jar comp2021-1a.jar Main";
			System.err.println("Usage: " + usage + " <jmm file path>\n" +
							   "Example: " + usage + " test/fixtures/public/Simple.jmm");
			return;
		}

		String resource = args[1];
		String fileName = resource.substring(resource.lastIndexOf("/"));
		String content = null;

		JmmParser main = new Main();
		try {
			// Retrieves Jmm file content
			content = getResourceContent(resource, fileName);
			// Parses Jmm file content
			JmmParserResult result = main.parse(content);
			// Saves json file from root node
			String jsonFileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".json";
			saveJson(jsonFileName, result.getRootNode().toJson());
		} catch (IOException e) {
			System.err.println("Exception: " + e.getMessage());
		}
	}

	private static String getResourceContent(String resource, String filename) throws IOException {
		String jmmExtension = ".jmm";
		String extension = filename.substring(filename.indexOf("."));
		if(!extension.equals(jmmExtension)) {
			throw new IllegalArgumentException(String.format("Resource must have a %s extension", jmmExtension));
		}

		Path fileName = Path.of(resource);
		return Files.readString(fileName);
	}

	public JmmParserResult parse(String jmmCode) {
		InputStream stream = toInputStream(jmmCode);
		Jmm parser = new Jmm(stream);
		SimpleNode root = null;
		
		try {
			root = parser.Program();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		return new JmmParserResult(root, parser.getReports());
	}

	private static void saveJson(String jsonFileName, String content) throws IOException {
		Path path = Path.of("json-generated/");
		if(!Files.exists(path)) {
			Files.createDirectory(path);
		}

		Path jsonFile = Path.of(path.toString(), jsonFileName);
		FileWriter writer = new FileWriter(String.valueOf(jsonFile));
		writer.write(content);
		writer.close();
	}
}