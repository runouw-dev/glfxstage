#version 100

#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

varying vec2 fUVs;
uniform sampler2D fxTexture;

void main() {
    gl_FragColor = texture2D(fxTexture, fUVs);
}