#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * ColorModulator;
    float fog = total_fog_value(sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd);
    vec3 cream = color.rgb;
    float luma = dot(cream, vec3(0.299, 0.587, 0.114));
    cream = mix(vec3(luma), cream, 1.08) * 0.94 + 0.04;
#ifdef FULL_COVER
    float cover = 1.0;
#else
    float cover = clamp(GlintAlpha, 0.0, 1.0);
#endif
    fragColor = vec4(cream, cover * (1.0 - fog));
}
