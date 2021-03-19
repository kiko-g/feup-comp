import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;

import java.io.IOException;

public class Parser implements JmmParser {
    public static JmmParserResult run(String resource) throws ParserException, IOException {
        String content = Utils.getResourceContent(resource, resource.substring(resource.lastIndexOf("/")));
        JmmParserResult result = new Parser().parse(content);
        Utils.saveJson(Utils.getFilename(resource), result.toJson());

        return result;
    }

    public JmmParserResult parse(String jmmCode) {
        Jmm parser = new Jmm(Utils.toInputStream(jmmCode));
        JmmParserResult result = new JmmParserResult();
        try {
            result = parser.Program();
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        }

        return result;
    }
}
