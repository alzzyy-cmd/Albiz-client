package net.albiz.client.easyplace.schematic;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

@Environment(EnvType.CLIENT)
public record SchematicBlockState(class_2680 state, class_2960 blockId, Map<String, String> properties) {
   public static SchematicBlockState of(class_2680 state) {
      Map<String, String> properties = new LinkedHashMap<>();
      state.method_11656().forEach((property, value) -> properties.put(property.method_11899(), value.toString()));
      return new SchematicBlockState(state, class_7923.field_41175.method_10221(state.method_26204()), Collections.unmodifiableMap(properties));
   }

   public String debugString() {
      if (this.properties.isEmpty()) {
         return this.blockId.toString();
      } else {
         StringBuilder result = new StringBuilder(this.blockId.toString()).append('[');
         this.properties.forEach((name, value) -> result.append(name).append('=').append(value).append(','));
         result.setCharAt(result.length() - 1, ']');
         return result.toString();
      }
   }
}
