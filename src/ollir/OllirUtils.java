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

    public static Type ollirToType(String returnType) {
        String[] types = returnType.split("\\.");

        switch (types[0]) {
            case "array" -> {
                return new Type("int", true);
            }
            case "i32" -> {
                return new Type("int", false);
            }
            case "bool" -> {
                return new Type("bool", false);
            }
            case "V" -> {
                return new Type("void", false);
            }
            default -> {
                return new Type(types[0], false);
            }
        }
    }
}
