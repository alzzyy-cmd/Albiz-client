#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

// 24 directions, 15 degrees apart (cos, sin), so no trigonometry runs per pixel
const vec2 DIRS[24] = vec2[24](
    vec2(1.0, 0.0), vec2(0.9659258, 0.2588190), vec2(0.8660254, 0.5), vec2(0.7071068, 0.7071068),
    vec2(0.5, 0.8660254), vec2(0.2588190, 0.9659258), vec2(0.0, 1.0), vec2(-0.2588190, 0.9659258),
    vec2(-0.5, 0.8660254), vec2(-0.7071068, 0.7071068), vec2(-0.8660254, 0.5), vec2(-0.9659258, 0.2588190),
    vec2(-1.0, 0.0), vec2(-0.9659258, -0.2588190), vec2(-0.8660254, -0.5), vec2(-0.7071068, -0.7071068),
    vec2(-0.5, -0.8660254), vec2(-0.2588190, -0.9659258), vec2(0.0, -1.0), vec2(0.2588190, -0.9659258),
    vec2(0.5, -0.8660254), vec2(0.7071068, -0.7071068), vec2(0.8660254, -0.5), vec2(0.9659258, -0.2588190)
);

float coverage(vec2 uv) {
    return step(texture(InSampler, uv).r, 0.99999);
}

void main() {
    if (coverage(texCoord) > 0.5) {
        discard;
    }
    vec2 px = 1.0 / ScreenSize;
    float width = ModelOffset.x;
    float glow = ModelOffset.y;

    // quick look first: nearly all of the screen is far away from the held item, so 8 directions at 4 distances
    // decide that and skip the full ring below
    float near = 0.0;
    for (int i = 0; i < 24; i += 3) {
        vec2 d = DIRS[i] * px;
        near = max(near, coverage(texCoord + d * width * 0.5));
        near = max(near, coverage(texCoord + d * width));
        near = max(near, coverage(texCoord + d * (width + glow * 0.45)));
        near = max(near, coverage(texCoord + d * (width + glow)));
    }
    if (near < 0.5) {
        discard;
    }

    float edge = 0.0;
    float soft = 0.0;
    for (int i = 0; i < 24; i++) {
        vec2 d = DIRS[i] * px;
        edge = max(edge, coverage(texCoord + d * width));
        edge = max(edge, coverage(texCoord + d * width * 0.5));
        soft += coverage(texCoord + d * (width + glow * 0.45)) * 0.6 + coverage(texCoord + d * (width + glow)) * 0.4;
    }
    soft /= 24.0;
    float alpha = max(edge, clamp(soft * 2.4, 0.0, 1.0) * 0.7) * ColorModulator.a;
    if (alpha <= 0.002) {
        discard;
    }
    vec3 color = mix(ColorModulator.rgb, vec3(1.0), edge * 0.12);
    fragColor = vec4(color * alpha, alpha);
}
