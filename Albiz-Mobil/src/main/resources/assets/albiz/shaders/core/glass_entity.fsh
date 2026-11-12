#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec2 texCoord0;
in vec3 viewNormal;
in vec3 viewPos;
in float diffuse;

out vec4 fragColor;

void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    vec3 n = normalize(viewNormal);
#ifdef GUI
    vec3 v = vec3(0.0, 0.0, 1.0);
#else
    vec3 v = normalize(-viewPos);
#endif
    float facing = dot(n, v);
    float back = facing < 0.0 ? 1.0 : 0.0;
    if (facing < 0.0) {
        n = -n;
    }
    float fres = pow(1.0 - abs(facing), 2.2);
#ifdef ITEM
    if (tex.a < 0.1) {
        discard;
    }
    float lum = dot(tex.rgb, vec3(0.299, 0.587, 0.114));
    float edge = 0.0;
#ifdef GUI
    float body = 0.42 + 0.3 * lum;
#else
    float body = 0.2 + 0.28 * lum;
#endif
    vec3 tint = mix(vec3(0.9, 0.95, 1.0), vertexColor.rgb, 0.3);
#else
    if (tex.a < 0.5) {
        discard;
    }
    float edge = tex.r;
    float body = 0.09;
    vec3 tint = vertexColor.rgb;
#endif
    float sheen = pow(max(0.0, dot(reflect(-v, n), normalize(vec3(-0.35, 0.8, 0.45)))), 18.0) * (1.0 - back);
    float alpha = body * (1.0 - 0.55 * back) + 0.28 * fres + 0.9 * edge * (1.0 - 0.75 * back) + 0.3 * sheen;
    vec3 color = mix(tint, vec3(1.0), clamp(0.2 + 0.5 * fres + edge + sheen, 0.0, 1.0));
    color *= mix(vec3(1.0), lightMapColor.rgb, 0.5);
    color += 0.05 * diffuse;
    fragColor = apply_fog(vec4(color, clamp(alpha, 0.0, 1.0) * vertexColor.a * ColorModulator.a), sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
