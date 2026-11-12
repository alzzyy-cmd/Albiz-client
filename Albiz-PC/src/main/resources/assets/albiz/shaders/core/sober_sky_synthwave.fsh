#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <albiz:skyfx_common.glsl>
#moj_import <albiz:sky_synthwave.glsl>

in vec3 skyDir;

out vec4 fragColor;

void main() {
    vec3 dir = normalize(skyDir);
    vec3 col = skyfx_synthwave(dir, ColorModulator.x) * ColorModulator.y;
    fragColor = vec4(skyfx_tonemap(col), 1.0);
}
