float ys_water_wave(vec3 p, float t) {
    vec3 q = p * 7.0;
    float a = sin(q.x * 1.7 + q.y * 0.9 + t * 1.6);
    float b = sin(q.y * 2.3 - q.z * 1.1 - t * 1.2);
    float c = sin((q.x - q.y) * 1.3 + q.z * 2.0 + t * 2.3);
    return (a + b + c) / 3.0;
}

float ys_water_lines(vec2 uv, float t) {
    vec3 p = vec3(uv, 0.37);
    float w = ys_water_wave(p * 0.55, t * 0.35) + 0.6 * ys_water_wave(p * 1.3 + 2.3, t * 0.5);
    float band = abs(fract(w * 1.4) - 0.5);
    return smoothstep(0.045, 0.0, band) * smoothstep(0.2, 0.6, abs(w));
}

vec3 ys_water_streak(vec2 uv, float t) {
    float s = dot(uv - 0.5, normalize(vec2(1.0, 0.55))) * 1.4 + 1.6 - fract(t * 0.08) * 3.2;
    s += 0.05 * sin(uv.y * 9.0 + t * 0.4);
    float band = exp(-s * s * 900.0);
    float halo = exp(-s * s * 90.0) * 0.25;
    vec3 tint = mix(vec3(1.0, 0.84, 0.45), vec3(0.45, 0.95, 1.0), smoothstep(-0.03, 0.03, s));
    return tint * (band + halo);
}

vec4 ys_water(vec3 n, vec3 v, vec2 uv, vec3 base, float t, float diffuse) {
    float ndv = clamp(dot(n, v), 0.0, 1.0);
    float fres = pow(1.0 - ndv, 3.0);
    vec3 deep = base * 0.13;
    vec3 body = base * 0.47;
    float shade = clamp(diffuse * 0.55 + pow(ndv, 1.5) * 0.55 - 0.12, 0.0, 1.0);
    vec3 col = mix(deep, body, shade);
    float caustic = smoothstep(0.55, 1.0, ys_water_wave(vec3(uv * 1.6, 0.2), t * 0.6));
    col += base * caustic * 0.14;
    col += mix(base, vec3(0.8, 0.93, 1.0), 0.55) * ys_water_lines(uv, t) * 0.28;
    col += ys_water_streak(uv, t) * 0.85;
    vec3 l1 = normalize(vec3(-0.45, 0.8, 0.45));
    vec3 l2 = normalize(vec3(0.55, 0.25, 0.8));
    float spec = pow(max(dot(n, normalize(l1 + v)), 0.0), 110.0) * 1.7;
    float broad = pow(max(dot(n, normalize(l2 + v)), 0.0), 16.0) * 0.18;
    col += vec3(0.93, 0.98, 1.0) * (spec + broad);
    vec3 r = reflect(-v, n);
    float env = smoothstep(0.3, 0.8, r.y) * (0.65 + 0.35 * sin(r.x * 5.0 + 1.3));
    float window = pow(max(dot(r, l1), 0.0), 28.0);
    col += vec3(0.72, 0.87, 1.0) * (env * 0.22 + window * 0.55);
    col += vec3(0.25, 0.6, 1.0) * fres * 0.8;
    col += vec3(0.6, 0.92, 1.0) * smoothstep(0.14, 0.0, ndv) * 0.95;
    float alpha = clamp(0.84 + fres * 0.16 + spec * 0.3, 0.0, 1.0);
    return vec4(col, alpha);
}

vec4 ys_water_rim(vec3 n, vec3 v, vec3 base) {
    float ndv = clamp(abs(dot(n, v)), 0.0, 1.0);
    float glow = smoothstep(0.0, 0.55, ndv);
    vec3 col = mix(base, vec3(0.55, 0.88, 1.0), 0.55);
    return vec4(col, glow * 0.6);
}
