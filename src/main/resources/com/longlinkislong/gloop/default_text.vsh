#version 130

in vec2 vPos;
in vec2 vUVs;
in vec4 vCol;

uniform mat4 uProj;
uniform mat4 uTrans;

out vec2 fUVs;
out vec4 fCol;
void main() {
    gl_Position = uProj * uTrans * vec4(vPos, 0.0, 1.0);
    fUVs = vUVs;
    fCol = vCol;
}
