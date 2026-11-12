#version 330

uniform sampler2D InSampler;
uniform sampler2D PreviousSampler;
uniform sampler2D StrengthSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    float strength = texture(StrengthSampler, vec2(0.5, 0.5)).r;
    vec3 current = texture(InSampler, texCoord).rgb;
    vec3 previous = texture(PreviousSampler, texCoord).rgb;
    fragColor = vec4(mix(current, previous, strength), 1.0);
}
