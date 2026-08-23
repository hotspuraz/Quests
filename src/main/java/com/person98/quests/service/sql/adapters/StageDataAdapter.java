package com.person98.quests.service.sql.adapters;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.person98.quests.controller.impl.QuestControllerImpl;
import com.person98.quests.data.stage.RootType;
import com.person98.quests.data.stage.impl.StageData;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map.Entry;

public class StageDataAdapter implements JsonSerializer<List<StageData>>, JsonDeserializer<List<StageData>> {
   public JsonElement serialize(List<StageData> src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();

      for (int i = 0; i < src.size(); i++) {
         StageData data = src.get(i);
         if (data != null) {
            JsonObject dataObject = new JsonObject();
            dataObject.add("type", new JsonPrimitive(data.getType()));
            dataObject.add("rootType", new JsonPrimitive(data.getRootType()));
            dataObject.add("root", new JsonPrimitive(data.getRootAsString()));
            dataObject.add("amount", new JsonPrimitive(data.getAmount()));
            jsonObject.add(i + "", dataObject);
         }
      }

      return jsonObject;
   }

   public List<StageData> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      List<StageData> data = Lists.newArrayList();
      JsonObject jsonObject = json.getAsJsonObject();

      for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
         JsonObject object = entry.getValue().getAsJsonObject();
         String type = object.get("type").getAsString();
         String root = object.get("root").getAsString();
         int amount = object.get("amount").getAsInt();
         String rootType = object.get("rootType").getAsString();
         data.add(new StageData(type, QuestControllerImpl.findFirstRoot(RootType.valueOf(rootType), root), amount, rootType));
      }

      return data;
   }
}
