#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <albiz:water_item.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec2 texCoord0;
in vec3 viewNormal;
in vec3 viewPos;
in vec3 localPos;
in float diffuse;

out vec4 fragColor;

void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    if (tex.a < 0.1) {
        discard;
    }
    vec3 n = normalize(viewNormal);
    vec3 v = normalize(-viewPos);
    if (dot(n, v) < 0.0) {
        n = -n;
    }
    float fog = total_fog_value(sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd);
#ifdef RIM
    vec4 rim = ys_water_rim(n, v, vertexColor.rgb);
    fragColor = vec4(rim.rgb, rim.a * vertexColor.a * (1.0 - fog) * ColorModulator.a);
#else
    vec4 water = ys_water(n, v, texCoord0, vertexColor.rgb, GameTime * 1200.0, diffuse);
    water.rgb *= mix(vec3(1.0), lightMapColor.rgb, 0.5);
    fragColor = apply_fog(vec4(water.rgb, water.a * vertexColor.a * ColorModulator.a), sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
#endif
}
