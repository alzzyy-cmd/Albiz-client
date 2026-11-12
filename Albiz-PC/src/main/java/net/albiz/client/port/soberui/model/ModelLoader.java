// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.model;

import net.albiz.client.port.soberui.model.ModelMesh;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ModelLoader {
    private static final int MAX_TRIANGLES = 60000;

    private ModelLoader() {
    }

    public static ModelMesh load(Path file) throws IOException {
        if(Files.exists(file)&&Files.size(file)>16L*1024*1024)throw new IOException("Model exceeds file budget");
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".stl")) {
            return ModelLoader.stl(Files.readAllBytes(file));
        }
        if (name.endsWith(".obj")) {
            return ModelLoader.obj(file);
        }
        throw new IOException("Unsupported model format: " + name);
    }

    private static ModelMesh stl(byte[] bytes)throws IOException{
      if(bytes.length>=84){ByteBuffer buffer=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);long count=Integer.toUnsignedLong(buffer.getInt(80));if(84L+count*50L==bytes.length){if(count==0||count>MAX_TRIANGLES)throw new IOException("STL exceeds triangle budget or is empty");return binaryStl(buffer,(int)count);}}
      return asciiStl(new String(bytes,StandardCharsets.US_ASCII));
    }

    private static ModelMesh binaryStl(ByteBuffer buf, int count) {
        boolean materialise = new String(buf.array(), 0, 80, StandardCharsets.US_ASCII).contains("COLOR=");
        float[] raw = new float[count * 9];
        int[] colors = new int[count];
        for (int t = 0; t < count; ++t) {
            boolean valid;
            int base = 84 + t * 50 + 12;
            for (int i = 0; i < 9; ++i) {
                raw[t * 9 + i] = buf.getFloat(base + i * 4);
            }
            int attr = buf.getShort(base + 36) & 0xFFFF;
            colors[t] = -1;
            if (attr == 0) continue;
            valid = materialise ? (attr & 0x8000) == 0 : (attr & 0x8000) != 0;
            if (!valid) continue;
            int r5 = materialise ? attr & 0x1F : attr >> 10 & 0x1F;
            int b5 = materialise ? attr >> 10 & 0x1F : attr & 0x1F;
            int g5 = attr >> 5 & 0x1F;
            colors[t] = r5 * 255 / 31 << 16 | g5 * 255 / 31 << 8 | b5 * 255 / 31;
        }
        if (ModelLoader.uniform(colors)) {
            Arrays.fill(colors, -1);
        }
        return ModelMesh.normalize(raw, colors, count);
    }

    private static ModelMesh asciiStl(String text) throws IOException {
        int triangles;
        ArrayList<Float> values = new ArrayList<Float>();
        for (String line : text.split("\\R")) {
            String[] parts;
            String trimmed = line.trim();
            if (!trimmed.startsWith("vertex") || (parts = trimmed.split("\\s+")).length < 4) continue;
            for (int i = 1; i <= 3; ++i) {
                values.add(Float.valueOf(Float.parseFloat(parts[i])));
            }
            if (values.size() >= 2700000) break;
        }
        if ((triangles = values.size() / 9) == 0) {
            throw new IOException("Empty model");
        }
        float[] raw = new float[triangles * 9];
        for (int i = 0; i < raw.length; ++i) {
            raw[i] = ((Float)values.get(i)).floatValue();
        }
        int[] colors = new int[triangles];
        Arrays.fill(colors, -1);
        return ModelMesh.normalize(raw, colors, triangles);
    }

    private static boolean uniform(int[] colors) {
        for (int color : colors) {
            if (color == colors[0]) continue;
            return false;
        }
        return true;
    }

    private static ModelMesh obj(Path file) throws IOException {
        int triangles;
        HashMap<String, Integer> materials = new HashMap<String, Integer>();
        ArrayList<float[]> vertices = new ArrayList<float[]>();
        ArrayList<Float> raw = new ArrayList<Float>();
        ArrayList<Integer> colors = new ArrayList<Integer>();
        int current = -1;
        for (String line : Files.readAllLines(file)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            String[] parts = trimmed.split("\\s+");
            switch (parts[0]) {
                case "mtllib": {
                    Path mtl = file.resolveSibling(trimmed.substring(7).trim());
                    if (!Files.exists(mtl, new LinkOption[0])) break;
                    materials.putAll(ModelLoader.materials(mtl));
                    break;
                }
                case "usemtl": {
                    current = materials.getOrDefault(parts.length > 1 ? parts[1] : "", -1);
                    break;
                }
                case "v": {
                    vertices.add(new float[]{Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])});
                    break;
                }
                case "f": {
                    int i;
                    int[] idx = new int[parts.length - 1];
                    for (i = 1; i < parts.length; ++i) {
                        int v = Integer.parseInt(parts[i].split("/")[0]);
                        idx[i - 1] = v < 0 ? vertices.size() + v : v - 1;
                    }
                    i = 1;
                    while (i + 1 < idx.length) {
                        for (int k : new int[]{idx[0], idx[i], idx[i + 1]}) {
                            float[] p = (float[])vertices.get(k);
                            raw.add(Float.valueOf(p[0]));
                            raw.add(Float.valueOf(p[1]));
                            raw.add(Float.valueOf(p[2]));
                        }
                        colors.add(current);
                        ++i;
                    }
                    break;
                }
            }
            if (colors.size() < 60000) continue;
            break;
        }
        if ((triangles = colors.size()) == 0) {
            throw new IOException("Empty model");
        }
        float[] positions = new float[triangles * 9];
        for (int i = 0; i < positions.length; ++i) {
            positions[i] = ((Float)raw.get(i)).floatValue();
        }
        int[] colorArray = colors.stream().mapToInt(Integer::intValue).toArray();
        return ModelMesh.normalize(positions, colorArray, triangles);
    }

    private static Map<String, Integer> materials(Path mtl) throws IOException {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        String name = null;
        for (String line : Files.readAllLines(mtl)) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length == 0) continue;
            if (parts[0].equals("newmtl") && parts.length > 1) {
                name = parts[1];
            }
            if (!parts[0].equals("Kd") || parts.length <= 3 || name == null) continue;
            int r = Math.round(Float.parseFloat(parts[1]) * 255.0f);
            int g = Math.round(Float.parseFloat(parts[2]) * 255.0f);
            int b = Math.round(Float.parseFloat(parts[3]) * 255.0f);
            result.put(name, Math.max(0, Math.min(255, r)) << 16 | Math.max(0, Math.min(255, g)) << 8 | Math.max(0, Math.min(255, b)));
        }
        return result;
    }
}

