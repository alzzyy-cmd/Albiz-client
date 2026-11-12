#version 330
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};
uniform sampler2D Sampler0;
in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    float distanceValue = texture(Sampler0, texCoord0).a;
    float edge = max(fwidth(distanceValue) * 0.7, 0.018);
    float coverage = smoothstep(0.5 - edge, 0.5 + edge, distanceValue);
    fragColor = vec4(vertexColor.rgb, vertexColor.a * coverage) * ColorModulator;
}
