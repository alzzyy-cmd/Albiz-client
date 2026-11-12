#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float ys_glassShape(vec2 p, vec2 halfSize, float radius) {
    vec2 q = abs(p) - (halfSize - radius);
    vec2 m = max(q, 0.0) / max(radius, 0.001);
    float corner = pow(pow(m.x, 3.0) + pow(m.y, 3.0), 1.0 / 3.0) * radius;
    return corner + min(max(q.x, q.y), 0.0) - radius;
}

float ys_glassGrain(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

vec3 ys_glassSample(vec2 frag, vec2 screenSize) {
    vec3 sum = vec3(0.0);
    float total = 0.0;
    for (int i = 0; i < 12; i++) {
        float angle = float(i) * 2.39996323;
        float reach = sqrt((float(i) + 0.5) / 12.0);
        float weight = 1.0 - reach * 0.5;
        sum += texture(Sampler0, (frag + vec2(cos(angle), sin(angle)) * reach * 11.0) / screenSize).rgb * weight;
        total += weight;
    }
    return sum / total;
}

vec3 ys_glassTrace(vec3 normal, float height, float gap, float ior, out float trapped) {
    vec3 down = vec3(0.0, 0.0, -1.0);
    vec3 inside = refract(down, normal, 1.0 / ior);
    vec2 travel = inside.xy * (height / max(-inside.z, 0.05));
    vec3 leaving = refract(inside, vec3(0.0, 0.0, 1.0), ior);
    trapped = dot(leaving, leaving) < 0.0001 ? 1.0 : 0.0;
    if (trapped > 0.5) leaving = normalize(vec3(inside.xy, -0.08));
    travel += leaving.xy * (gap / max(-leaving.z, 0.08));
    return vec3(travel, 0.0);
}

void main() {
    vec2 code = floor(texCoord0 / 4.0);
    vec2 local = texCoord0 - code * 4.0 - 1.5;
    vec2 size = 1.0 / max(abs(vec2(dFdx(local.x), dFdy(local.y))), vec2(0.00001));
    vec2 halfSize = size * 0.5;
    vec2 p = (local - 0.5) * size;
    float radius = min(code.x, min(halfSize.x, halfSize.y));
    float band = max(code.y, 1.0);

    float d = ys_glassShape(p, halfSize, radius);
    float coverage = clamp(0.5 - d, 0.0, 1.0);
    if (coverage <= 0.0) discard;
    float soft = min(max(radius, band * 3.0), min(halfSize.x, halfSize.y));
    vec2 grad = vec2(
        ys_glassShape(p + vec2(0.5, 0.0), halfSize, soft) - ys_glassShape(p - vec2(0.5, 0.0), halfSize, soft),
        ys_glassShape(p + vec2(0.0, 0.5), halfSize, soft) - ys_glassShape(p - vec2(0.0, 0.5), halfSize, soft));
    vec2 outward = grad / max(length(grad), 0.0001);
    float inside = max(-d, 0.0);

    float bevel = min(band * 0.9, min(halfSize.x, halfSize.y));
    float thickness = bevel * 0.4;
    float rim = 1.0 - clamp(inside / bevel, 0.0, 1.0);
    float lift = sqrt(max(1.0 - rim * rim, 0.0));
    float height = thickness * (0.25 + 0.75 * lift);
    float slope = rim / max(lift, 0.08) * (thickness / bevel);
    vec3 normal = normalize(vec3(outward * slope, 1.0));
    float gap = bevel * 0.15;

    vec2 screenSize = vec2(textureSize(Sampler0, 0)) * 2.0;
    float trapG;
    vec2 shiftG = ys_glassTrace(normal, height, gap, 1.50, trapG).xy;
    float limit = bevel * 1.6;
    shiftG = clamp(shiftG, vec2(-limit), vec2(limit));
    vec3 col = ys_glassSample(gl_FragCoord.xy + vec2(shiftG.x, -shiftG.y), screenSize);
    col = mix(vec3(dot(col, vec3(0.299, 0.587, 0.114))), col, 1.04);

    vec3 view = vec3(0.0, 0.0, 1.0);
    vec3 mirror = reflect(-view, normal);
    vec2 bounce = mirror.xy * bevel * 2.5;
    vec3 env = inside < bevel * 1.5 ? ys_glassSample(gl_FragCoord.xy + vec2(bounce.x, -bounce.y), screenSize) : col;
    env = env * 1.15 + 0.1;
    float cosine = clamp(dot(normal, view), 0.0, 1.0);
    float fresnel = 0.04 + 0.96 * pow(1.0 - cosine, 5.0);
    float trapped = trapG;
    col = mix(col, env, clamp(fresnel * 0.5 + trapped * 0.3, 0.0, 1.0));

    vec3 light = normalize(vec3(-0.55, -0.6, 0.58));
    vec3 halfway = normalize(light + view);
    float facing = max(dot(normal, halfway), 0.0);
    float spec = pow(facing, 90.0) * 1.1 + pow(facing, 14.0) * 0.12;
    vec3 back = normalize(vec3(0.5, 0.55, 0.62));
    float backSpec = pow(max(dot(normal, normalize(back + view)), 0.0), 60.0) * 0.35;

#ifdef YS_DENSE
    col = mix(col, vertexColor.rgb, 0.24);
#else
    col = mix(col, vertexColor.rgb, 0.12);
#endif
    col = mix(col, vec3(1.0), 0.09);
    float edgeLine = exp(-inside / 0.8) * (0.12 + 0.3 * clamp(-outward.y, 0.0, 1.0)) + exp(-inside / 6.0) * 0.035;
    col += vec3(1.0) * edgeLine;
    col += (ys_glassGrain(gl_FragCoord.xy) - 0.5) * 0.012;
    fragColor = vec4(clamp(col, 0.0, 1.0), coverage * vertexColor.a) * ColorModulator;
}
