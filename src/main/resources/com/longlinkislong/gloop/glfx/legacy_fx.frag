#version 100

#ifdef GL_FRAGMENT_PRECISION_HIGH
    precision float highp;
#else
    precision float mediump;        
#endif

varying vec2 fUVs;
uniform sampler2D fxTexture;

void main() {
    gl_FragColor = texture2D(fxTexture, fUVs);
}