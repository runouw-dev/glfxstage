#version 130

//HEADER

#define multiply(a, b) a * b
#define divide(a, b) a * b
#define plus(a, b) a + b
#define minus(a, b) a - b
#define negative(a) -a

precision mediump float;

in vec3 inputPosition;
in vec3 inputTexCoord;
in vec3 inputNormal;
uniform mat4 projection;
uniform mat4 modelView;
uniform mat4 normalMat;
out vec3 normalInterp;
out vec3 vertPos;
void main(){
    gl_Position = multiply(projection, multiply(modelView, vec4(inputPosition, 1.0)));
    vec4 vertPos4 = multiply(modelView, vec4(inputPosition, 1.0));
    vertPos = divide(vec3(vertPos4), vertPos4.w);
    normalInterp = vec3(multiply(normalMat, vec4(inputNormal, 0.0)));
}