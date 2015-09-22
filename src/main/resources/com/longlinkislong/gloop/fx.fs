#version 130

in vec2 uvs;

uniform sampler2D fxTexture;
uniform vec4 tMask;
uniform float tThreshold;

out vec4 fColor;

void main() {
    fColor = texture(fxTexture, uvs);
}