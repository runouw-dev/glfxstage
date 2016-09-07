#version 130

in vec3 vPos;

uniform mat4 mvp;

void main() {
    gl_Position = mvp * vec4(vPos, 1.0);
}
