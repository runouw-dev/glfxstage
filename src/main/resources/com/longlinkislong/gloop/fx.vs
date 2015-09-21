#version 130

in vec2 vPos;
in vec2 vUVs;

out vec2 uvs;

void main() {
    gl_Position = vec4(vPos, 0.0, 1.0);
    uvs = vUVs;
}