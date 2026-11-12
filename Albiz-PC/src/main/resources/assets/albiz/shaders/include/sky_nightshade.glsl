vec3 ys_nightshade(vec3 dir, float t) {
    vec3 p = dir * 1.7;
    vec3 q = vec3(
        ys_fbm3(p + vec3(0.0, 0.0, t * 0.03), 4),
        ys_fbm3(p + vec3(5.2, 1.3, -t * 0.025), 4),
        ys_fbm3(p + vec3(2.1, 7.7, t * 0.02), 4));
    vec3 r = vec3(
        ys_fbm3(p + q * 3.2 + vec3(1.7, 9.2, t * 0.04), 4),
        ys_fbm3(p + q * 3.2 + vec3(8.3, 2.8, -t * 0.03), 4),
        ys_fbm3(p + q * 3.2 + vec3(4.4, 6.1, t * 0.035), 4));
    float f = ys_fbm3(p + r * 3.4, 5);
    vec3 deep = vec3(0.025, 0.008, 0.045);
    vec3 mid = vec3(0.19, 0.07, 0.33);
    vec3 high = vec3(0.36, 0.17, 0.58);
    vec3 col = mix(deep, mid, smoothstep(0.32, 0.62, f));
    col = mix(col, high, smoothstep(0.6, 0.82, f) * 0.75);
    col *= 0.75 + 0.5 * smoothstep(0.2, 0.8, q.x);
    float px = max(fwidth(f), 1e-4);
    float edgeA = exp(-abs(f - 0.6) / (px * 1.3));
    float edgeB = exp(-abs(f - 0.47) / (px * 1.1)) * 0.45;
    float sparkle = smoothstep(0.55, 0.75, ys_noise3(dir * 9.0 + t * 0.05));
    col += vec3(0.78, 0.62, 1.0) * (edgeA * (0.35 + 0.65 * sparkle) + edgeB * 0.6) * 0.85;
    return col;
}
