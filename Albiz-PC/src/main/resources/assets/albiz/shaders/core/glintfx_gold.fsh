#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <albiz:skyfx_common.glsl>
#moj_import <albiz:glint_common.glsl>
#moj_import <albiz:glint_gold.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec2 texCoord0;

out vec4 fragColor;

void main() {

    if (texture(Sampler0, texCoord0).a < -1.0) {
        discard;
    }
    vec3 color = skyfx_glint(texCoord0, GameTime * 1200.0) * ColorModulator.rgb;
#ifdef FULL_COVER
    float fog = total_fog_value(sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd);
    fragColor = vec4(sqrt(max(color, vec3(0.0))), 1.0 - fog);
#else
    float fade = (1.0f - total_fog_value(sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd)) * GlintAlpha;
    color = max(color * fade, vec3(0.0));
    if (color.r + color.g + color.b < 0.003) {
        discard;
    }
    fragColor = vec4(sqrt(color), 1.0);
#endif
}
