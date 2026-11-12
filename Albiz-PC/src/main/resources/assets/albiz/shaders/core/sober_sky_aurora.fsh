#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <albiz:sky_common.glsl>
#moj_import <albiz:sky_aurora.glsl>

in vec3 skyDir;

out vec4 fragColor;

void main() {
    vec3 dir = normalize(skyDir);
    vec3 col = ys_aurora(dir, ColorModulator.x, ColorModulator.z, ModelOffset) * ColorModulator.y;
    fragColor = vec4(ys_tonemap(col), 1.0);
}
