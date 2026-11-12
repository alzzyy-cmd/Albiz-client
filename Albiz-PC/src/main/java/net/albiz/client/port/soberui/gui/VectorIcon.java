// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class VectorIcon {
    private static final Pattern TOKEN = Pattern.compile("[MmLlHhVvCcSsZz]|-?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][-+]?\\d+)?");
    private static final int CURVE_STEPS = 24;

    private VectorIcon() {
    }

    static List<float[]> parse(String path) {
        ArrayList<String> tokens = new ArrayList<String>();
        Matcher matcher = TOKEN.matcher(path);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        ArrayList<float[]> shapes = new ArrayList<float[]>();
        Points points = null;
        float x = 0.0f;
        float y = 0.0f;
        float startX = 0.0f;
        float startY = 0.0f;
        float lastCx = 0.0f;
        float lastCy = 0.0f;
        int command = 77;
        boolean curve = false;
        int i = 0;
        block8: while (i < tokens.size()) {
            boolean relative;
            String token = (String)tokens.get(i);
            if (Character.isLetter(token.charAt(0))) {
                command = token.charAt(0);
                ++i;
                if (command == 90 || command == 122) {
                    if (points != null) {
                        shapes.add(points.done());
                    }
                    points = null;
                    x = startX;
                    y = startY;
                    curve = false;
                    continue;
                }
            }
            float ox = (relative = Character.isLowerCase((char)command)) ? x : 0.0f;
            float oy = relative ? y : 0.0f;
            switch (Character.toUpperCase((char)command)) {
                case 'M': {
                    if (points != null) {
                        shapes.add(points.done());
                    }
                    x = ox + VectorIcon.number(tokens, i++);
                    y = oy + VectorIcon.number(tokens, i++);
                    startX = x;
                    startY = y;
                    points = new Points();
                    points.add(x, y);
                    command = relative ? 108 : 76;
                    curve = false;
                    continue block8;
                }
                case 'L': {
                    x = ox + VectorIcon.number(tokens, i++);
                    y = oy + VectorIcon.number(tokens, i++);
                    points = VectorIcon.ensure(points, startX, startY);
                    points.add(x, y);
                    curve = false;
                    continue block8;
                }
                case 'H': {
                    x = ox + VectorIcon.number(tokens, i++);
                    points = VectorIcon.ensure(points, startX, startY);
                    points.add(x, y);
                    curve = false;
                    continue block8;
                }
                case 'V': {
                    y = oy + VectorIcon.number(tokens, i++);
                    points = VectorIcon.ensure(points, startX, startY);
                    points.add(x, y);
                    curve = false;
                    continue block8;
                }
                case 'C': 
                case 'S': {
                    float c1y;
                    float c1x;
                    if (Character.toUpperCase((char)command) == 'C') {
                        c1x = ox + VectorIcon.number(tokens, i++);
                        c1y = oy + VectorIcon.number(tokens, i++);
                    } else {
                        c1x = curve ? 2.0f * x - lastCx : x;
                        c1y = curve ? 2.0f * y - lastCy : y;
                    }
                    float c2x = ox + VectorIcon.number(tokens, i++);
                    float c2y = oy + VectorIcon.number(tokens, i++);
                    float ex = ox + VectorIcon.number(tokens, i++);
                    float ey = oy + VectorIcon.number(tokens, i++);
                    points = VectorIcon.ensure(points, startX, startY);
                    for (int s = 1; s <= 24; ++s) {
                        float t = (float)s / 24.0f;
                        float u = 1.0f - t;
                        float px = u * u * u * x + 3.0f * u * u * t * c1x + 3.0f * u * t * t * c2x + t * t * t * ex;
                        float py = u * u * u * y + 3.0f * u * u * t * c1y + 3.0f * u * t * t * c2y + t * t * t * ey;
                        points.add(px, py);
                    }
                    lastCx = c2x;
                    lastCy = c2y;
                    x = ex;
                    y = ey;
                    curve = true;
                    continue block8;
                }
            }
            ++i;
        }
        if (points != null) {
            shapes.add(points.done());
        }
        return shapes;
    }

    private static Points ensure(Points points, float x, float y) {
        if (points != null) {
            return points;
        }
        Points created = new Points();
        created.add(x, y);
        return created;
    }

    private static float number(List<String> tokens, int index) {
        return index < tokens.size() ? Float.parseFloat(tokens.get(index)) : 0.0f;
    }

    static float[] coverage(List<float[]> shapes, int from, int to, float viewBox, int size) {
        float[] result = new float[size * size];
        int samples = 8;
        float scale = (float)size / viewBox;
        float[] crossings = new float[64];
        for (int row = 0; row < size; ++row) {
            for (int s = 0; s < samples; ++s) {
                float sy = ((float)row + ((float)s + 0.5f) / (float)samples) / scale;
                int count = 0;
                for (int shape = from; shape < to; ++shape) {
                    float[] p = shapes.get(shape);
                    int n = p.length / 2;
                    for (int k = 0; k < n; ++k) {
                        float by;
                        float ax = p[k * 2];
                        float ay = p[k * 2 + 1];
                        float bx = p[(k + 1) % n * 2];
                        if (ay <= sy == (by = p[(k + 1) % n * 2 + 1]) <= sy) continue;
                        if (count >= crossings.length) {
                            crossings = Arrays.copyOf(crossings, crossings.length * 2);
                        }
                        crossings[count++] = (ax + (sy - ay) / (by - ay) * (bx - ax)) * scale;
                    }
                }
                Arrays.sort(crossings, 0, count);
                int c = 0;
                while (c + 1 < count) {
                    float left = Math.max(0.0f, crossings[c]);
                    float right = Math.min((float)size, crossings[c + 1]);
                    if (!(right <= left)) {
                        int first = (int)Math.floor(left);
                        int last = Math.min(size - 1, (int)Math.ceil(right) - 1);
                        for (int px = first; px <= last; ++px) {
                            float overlap = Math.min(right, (float)(px + 1)) - Math.max(left, (float)px);
                            if (!(overlap > 0.0f)) continue;
                            int n = row * size + px;
                            result[n] = result[n] + overlap / (float)samples;
                        }
                    }
                    c += 2;
                }
            }
        }
        return result;
    }

    private static final class Points {
        private float[] values = new float[64];
        private int count;

        private Points() {
        }

        void add(float x, float y) {
            if (this.count + 2 > this.values.length) {
                this.values = Arrays.copyOf(this.values, this.values.length * 2);
            }
            this.values[this.count++] = x;
            this.values[this.count++] = y;
        }

        float[] done() {
            return Arrays.copyOf(this.values, this.count);
        }
    }
}

