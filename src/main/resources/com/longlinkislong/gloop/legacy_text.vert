#version 100

#ifdef GL_FRAGMENT_PRECISION_HIGH
    precision float highp;
#else
    precision float mediump;        
#endif

attribute vec2 vPos;
attribute vec2 vUVs;
attribute vec4 vCol;

uniform mat4 uProj;
uniform mat4 uTrans;

varying vec2 fUVs;
varying vec4 fCol;

void main() {
    gl_Position = uProj * uTrans * vec4(vPos, 0.0, 1.0);
    fUVs = vUVs;
    fCol = vCol;
}