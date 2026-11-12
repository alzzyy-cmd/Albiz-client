#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

in vec2 local;
in vec4 tint;

out vec4 fragColor;

void main() {
    vec2 c = local;
    float d = length(c);
    float t = GameTime * 1200.0;
#if defined(DROP)
    if (d > 1.0) {
        discard;
    }
    float aa = smoothstep(1.0, 0.84, d);
    vec3 n = vec3(c, sqrt(max(0.0, 1.0 - d * d)));
    vec3 deep = tint.rgb * 0.7;
    vec3 lite = mix(tint.rgb, vec3(1.0), 0.5);
    vec3 col = mix(deep, lite, clamp(n.z * 0.75 - c.y * 0.3 + 0.15, 0.0, 1.0));
    float rim = smoothstep(0.62, 1.0, d);
    col = mix(col, mix(tint.rgb, vec3(1.0), 0.7), rim * 0.6);
    float spec = pow(max(dot(n, normalize(vec3(-0.45, 0.55, 0.7))), 0.0), 34.0);
    col += vec3(1.0) * spec * 1.25;
    col += mix(tint.rgb, vec3(1.0), 0.6) * smoothstep(0.42, 0.0, length(c - vec2(0.22, -0.38))) * 0.45;
    fragColor = vec4(col, tint.a * aa * (0.86 + 0.14 * rim)) * ColorModulator;
#elif defined(BUBBLE)
    if (d > 1.0) {
        discard;
    }
    float aa = smoothstep(1.0, 0.84, d);
    float ring = smoothstep(0.7, 0.96, d) * aa;
    float spec = smoothstep(0.3, 0.0, length(c - vec2(-0.36, 0.38)));
    vec3 col = mix(mix(tint.rgb, vec3(1.0), 0.6), vec3(1.0), spec);
    fragColor = vec4(col, tint.a * (ring * 0.8 + spec * 0.95 + 0.07 * aa)) * ColorModulator;
#elif defined(SPARKLE)
    float core = exp(-dot(c, c) * 24.0);
    float rays = exp(-abs(c.x) * 36.0) * exp(-abs(c.y) * 2.4) + exp(-abs(c.y) * 36.0) * exp(-abs(c.x) * 2.4);
    float glow = core * 1.3 + rays * 0.95;
    fragColor = vec4(mix(tint.rgb, vec3(1.0), 0.72) * min(glow, 1.0), tint.a) * ColorModulator;
#elif defined(FLASH)
    float glow = exp(-d * d * 3.2) + exp(-d * d * 16.0) * 0.8;
    fragColor = vec4(mix(tint.rgb, vec3(1.0), 0.45) * min(glow, 1.0), tint.a) * ColorModulator;
#elif defined(MIST)
    float glow = exp(-d * d * 2.6) * 0.55;
    fragColor = vec4(mix(tint.rgb, vec3(1.0), 0.55) * glow, tint.a) * ColorModulator;
#elif defined(PUDDLE)
    float ang = atan(c.y, c.x);
    float radius = 0.8 * (1.0 + 0.035 * sin(ang * 6.0 + t * 2.4) + 0.02 * sin(ang * 11.0 - t * 1.8));
    float inside = smoothstep(radius, radius - 0.04, d);
    if (inside <= 0.0) {
        discard;
    }
    float edge = smoothstep(radius - 0.12, radius - 0.02, d) * inside;
    float caustic = sin(c.x * 11.0 + t * 2.1 + sin(c.y * 6.0)) * sin(c.y * 11.5 - t * 1.7 + sin(c.x * 5.0));
    vec3 col = mix(tint.rgb * 0.62, mix(tint.rgb, vec3(1.0), 0.55), clamp(edge * 0.85 + smoothstep(0.55, 1.0, caustic) * 0.35, 0.0, 1.0));
    fragColor = vec4(col, inside * (0.5 + edge * 0.4) * tint.a) * ColorModulator;
#elif defined(RING)
    float ang = atan(c.y, c.x);
    float band = exp(-pow((d - 0.85) / 0.06, 2.0)) * (0.75 + 0.25 * sin(ang * 11.0 + t * 16.0));
    if (band < 0.004) {
        discard;
    }
    fragColor = vec4(mix(tint.rgb, vec3(1.0), 0.7), band * tint.a) * ColorModulator;
#else
    fragColor = tint * ColorModulator;
#endif
}
