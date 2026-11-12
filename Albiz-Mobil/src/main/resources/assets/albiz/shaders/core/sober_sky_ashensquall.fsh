#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <albiz:sky_common.glsl>
#moj_import <albiz:sky_cubetex.glsl>

uniform sampler2D Sampler0;

in vec3 skyDir;

out vec4 fragColor;

void main() {
    vec3 col = ys_cubeSky(Sampler0, normalize(skyDir), ColorModulator.x) * ColorModulator.y;
    fragColor = vec4(clamp(col, 0.0, 1.0), 1.0);
}