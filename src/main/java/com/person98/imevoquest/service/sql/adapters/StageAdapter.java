package com.person98.imevoquest.service.sql.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.person98.imevoquest.data.stage.Stage;
import com.person98.imevoquest.data.stage.impl.StageData;
import java.lang.reflect.Type;
import java.util.List;

public class StageAdapter implements JsonSerializer<Stage>, JsonDeserializer<Stage> {
   public JsonElement serialize(Stage src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("startMessages", context.serialize(src.getStartMessages()));
      jsonObject.add("finishMessages", context.serialize(src.getFinishMessages()));
      Type type = (new TypeToken<List<StageData>>() {}).getType();
      jsonObject.add("data", context.serialize(src.getData(), type));
      return jsonObject;
   }

   public Stage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      Stage stage = new Stage();
      JsonObject jsonObject = json.getAsJsonObject();
      Type stringListType = (new TypeToken<List<String>>() {}).getType();
      stage.setStartMessages(context.deserialize(jsonObject.get("startMessages"), stringListType));
      stage.setFinishMessages(context.deserialize(jsonObject.get("finishMessages"), stringListType));
      Type type = (new TypeToken<List<StageData>>() {}).getType();
      stage.setData(context.deserialize(jsonObject.get("data"), type));
      return stage;
   }
}
