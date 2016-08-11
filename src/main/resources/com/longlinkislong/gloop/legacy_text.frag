#version 100

#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

varying vec2 fUVs;
varying vec4 fCol;

uniform sampler2D uFont;

void main() {
    gl_FragColor = fCol * texture2D(uFont, fUVs);
}