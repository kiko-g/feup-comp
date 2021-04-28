package ollir;

import pt.up.fe.comp.jmm.analysis.table.Type;

public class OllirUtils {
    public static String typeToOllir(Type type) {
        StringBuilder builder = new StringBuilder();

        if (type.isArray()) {
            builder.append("array.");
        }

        switch (type.getName()) {
            case "int" -> builder.append("i32");
            case "boolean" -> builder.append("bool");
            case "void" -> builder.append("V");
            default -> builder.append(type.getName());
        }

        return builder.toString();
    }
}
