#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec2 code = floor(texCoord0 / 4.0);
    vec2 local = texCoord0 - code * 4.0 - 1.5;
    vec2 size = 1.0 / max(abs(vec2(dFdx(local.x), dFdy(local.y))), vec2(0.00001));
    vec2 halfSize = size * 0.5;
    vec2 p = (local - 0.5) * size;
    float radius = min(code.x, min(halfSize.x, halfSize.y));
    vec2 q = abs(p) - (halfSize - radius);
    float d = length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
    float coverage = clamp(0.5 - d, 0.0, 1.0);
    if (coverage <= 0.0) discard;
    vec2 uv = clamp(local, vec2(0.0), vec2(1.0));
    if (code.y > 0.5) uv.y = 1.0 - uv.y;
    vec4 color = texture(Sampler0, uv);
    fragColor = vec4(color.rgb, color.a * coverage) * vertexColor * ColorModulator;
}
