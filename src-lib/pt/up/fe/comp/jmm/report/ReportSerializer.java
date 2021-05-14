package pt.up.fe.comp.jmm.report;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import report.StyleReport;

class ReportSerializer implements JsonSerializer<StyleReport> {

    @Override
    public JsonElement serialize(StyleReport jsonElement, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", jsonElement.getType().name());
        jsonObject.addProperty("stage", jsonElement.getStage().name());
        jsonObject.addProperty("message", jsonElement.getMessage());
        jsonObject.addProperty("line", jsonElement.getLine());
        return jsonObject;
    }

}