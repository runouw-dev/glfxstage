#version 130

in vec2 uvs;

uniform sampler2D fxTexture;
uniform vec4 tMask;
uniform float tThreshold;

out vec4 fColor;

void main() {
    fColor = texture(fxTexture, uvs);

    vec4 dColor = abs(fColor - tMask);

    if(dColor.r < tThreshold && dColor.g < tThreshold && dColor.b < tThreshold) {
        fColor.a = 0.0;
    }
}