#version 130

in vec2 fUVs;
in vec4 fCol;

out vec4 fColor;

uniform sampler2D uFont;

void main() {
    fColor = fCol * texture(uFont, fUVs);        
}