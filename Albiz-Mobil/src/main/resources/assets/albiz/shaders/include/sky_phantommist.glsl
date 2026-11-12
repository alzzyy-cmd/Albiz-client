vec3 ys_phantommist(vec3 dir, float t) {
    vec3 p = dir * 0.95;
    vec3 warp = vec3(
        ys_fbm3(p * 1.3 + vec3(0.0, t * 0.02, 0.0), 4),
        ys_fbm3(p * 1.3 + vec3(4.1, -t * 0.018, 2.3), 4),
        ys_fbm3(p * 1.3 + vec3(7.4, 1.9, t * 0.015), 4));
    vec3 w = p + (warp - 0.5) * 2.2;
    float n = ys_fbm3(w * 1.6 + vec3(t * 0.01), 5);
    float ridge = 1.0 - abs(2.0 * n - 1.0);
    float n2 = ys_fbm3(w * 2.7 + vec3(3.3, t * 0.015, 1.1), 4);
    float ridge2 = 1.0 - abs(2.0 * n2 - 1.0);
    vec3 base = mix(vec3(0.13, 0.14, 0.16), vec3(0.2, 0.21, 0.235), smoothstep(0.25, 0.75, warp.y));
    float haze = smoothstep(0.35, 0.75, ys_fbm3(p * 0.9 - vec3(t * 0.008), 4));
    vec3 col = base + vec3(0.07, 0.075, 0.085) * haze;
    float filament = pow(ridge, 70.0) * 0.8 + pow(ridge2, 110.0) * 0.35;
    float glow = pow(ridge, 14.0) * 0.12 + pow(ridge2, 18.0) * 0.04;
    col += vec3(0.82, 0.85, 0.9) * (filament + glow);
    return col;
}
