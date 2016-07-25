#version 130

in vec2 vPos;
in vec2 vUVs;

uniform mat4 vProj;

out vec2 uvs;

void main() {
    gl_Position = vProj * vec4(vPos, 0.0, 1.0);
    uvs = vUVs;
}