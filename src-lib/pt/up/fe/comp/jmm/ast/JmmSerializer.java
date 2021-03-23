package pt.up.fe.comp.jmm.ast;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import pt.up.fe.comp.jmm.JmmNode;

public class JmmSerializer implements JsonSerializer<JmmNode> {

    private Type ListOfJmm = new TypeToken<List<JmmNode>>() {
    }.getType();

    private Type MapOfAttrs = new TypeToken<Map<String, String>>() {
    }.getType();

    @Override
    public JsonElement serialize(JmmNode jsonElement, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("kind", jsonElement.getKind());
        Map<String, String> attrs = new HashMap<>();
        for (String attr : jsonElement.getAttributes()) {
            attrs.put(attr, jsonElement.get(attr));
        }
        jsonObject.add("attributes", jsonSerializationContext.serialize(attrs, this.MapOfAttrs));
        jsonObject.add("children", jsonSerializationContext.serialize(jsonElement.getChildren(), this.ListOfJmm));
        return jsonObject;
    }

}