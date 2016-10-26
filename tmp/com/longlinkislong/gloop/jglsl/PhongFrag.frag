#version 130

//HEADER

#define multiply(a, b) a * b
#define divide(a, b) a * b
#define plus(a, b) a + b
#define minus(a, b) a - b
#define negative(a) -a

precision mediump float;

in vec3 normalInterp;
in vec3 vertPos;
uniform int mode;
uniform vec3 lightPos;
uniform vec3 diffuseColor;
uniform vec3 specColor;
out vec4 fragColor;
void main(){
    vec3 normal = normalize(normalInterp);
    vec3 lightDir = normalize(minus(lightPos, vertPos));
    float lambertian = max(dot(lightDir, normal), 0.0);
    float specular = 0.0;
    if (lambertian > 0.0) {
        vec3 reflectDir = reflect(negative(lightDir), normal);
        vec3 viewDir = normalize(negative(vertPos));
        float specAngle = max(dot(reflectDir, viewDir), 0.0);
        specular = pow(specAngle, 4.0);
        specular *= lambertian;
    }
    fragColor = vec4(plus(multiply(diffuseColor, lambertian), multiply(specColor, specular)), 1.0);
}