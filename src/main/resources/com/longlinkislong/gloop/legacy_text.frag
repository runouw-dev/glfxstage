#version 100

#ifdef GL_FRAGMENT_PRECISION_HIGH
    precision float highp;
#else
    precision float mediump;        
#endif

varying vec2 fUVs;
varying vec4 fCol;

uniform sampler2D uFont;

void main() {
    gl_FragColor = fCol * texture2D(uFont, fUVs);
}