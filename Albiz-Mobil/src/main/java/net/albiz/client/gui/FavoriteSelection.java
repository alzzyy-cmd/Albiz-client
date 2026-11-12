package net.albiz.client.gui;

import com.google.gson.*;
import java.util.*;

/** UI membership only: never toggles modules or changes bindings. */
public final class FavoriteSelection {
 private final LinkedHashSet<String> ids=new LinkedHashSet<>();
 public boolean contains(String id){return ids.contains(id);}
 public Set<String> snapshot(){return Collections.unmodifiableSet(new LinkedHashSet<>(ids));}
 public boolean set(String id,boolean selected){
  if(id==null || id.length()>128 || !id.matches("[a-z0-9][a-z0-9._-]*"))return false;
  return selected ? ids.size()<256 && ids.add(id) : ids.remove(id);
 }
 public void load(JsonElement value){
  ids.clear();if(value==null || !value.isJsonArray())return;
  for(JsonElement element:value.getAsJsonArray())if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())set(element.getAsString(),true);
 }
 public JsonArray json(){JsonArray a=new JsonArray();ids.forEach(a::add);return a;}
}
