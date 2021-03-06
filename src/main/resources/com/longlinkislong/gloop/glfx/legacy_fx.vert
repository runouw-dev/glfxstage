#version 100

#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

attribute vec2 vPos;
attribute vec2 vUVs;

uniform mat4 vProj;

varying vec2 fUVs;

void main() {
    fUVs = vUVs;
    gl_Position = vProj * vec4(vPos, 0.0, 1.0);
}