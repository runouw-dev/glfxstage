/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop.jglsl;

import com.longlinkislong.gloop.j2glsl.Precision;
import com.longlinkislong.gloop.j2glsl.mat4;
import com.longlinkislong.gloop.j2glsl.uniform;
import com.longlinkislong.gloop.j2glsl.varying;
import com.longlinkislong.gloop.j2glsl.FragmentShader;
import com.longlinkislong.gloop.j2glsl.attribute;
import com.longlinkislong.gloop.j2glsl.ES2Vert;
import com.longlinkislong.gloop.j2glsl.vec4;
import com.longlinkislong.gloop.j2glsl.vec3;
import com.longlinkislong.gloop.j2glsl.ES2Frag;
import com.longlinkislong.gloop.j2glsl.GLSL130Frag;
import com.longlinkislong.gloop.j2glsl.GLSL130Vert;
import com.longlinkislong.gloop.j2glsl.VertexShader;
import com.longlinkislong.gloop.j2glsl.in;
import com.longlinkislong.gloop.j2glsl.out;

/**
 *
 * @author zmichaels
 */
public class TestJGLSL {           
    
    @VertexShader(version = 130)
    public static class PhongVert extends GLSL130Vert {
        @in vec3 inputPosition;
        @in vec3 inputTexCoord;
        @in vec3 inputNormal;

        @uniform mat4 projection;
        @uniform mat4 modelView;
        @uniform mat4 normalMat;    

        @out vec3 normalInterp;
        @out vec3 vertPos;

        @Override
        protected void main() {
            gl_Position = multiply(projection, multiply(modelView, vec4(inputPosition, 1.0F)));

            vec4 vertPos4 = multiply(modelView, vec4(inputPosition, 1.0F));

            vertPos = divide(vec3(vertPos4), vertPos4.w);
            normalInterp = vec3(multiply(normalMat, vec4(inputNormal, 0.0F)));
        }        
    }
    
    @FragmentShader(version = 130)
    public static class PhongFrag extends GLSL130Frag {
        @in vec3 normalInterp;
        @in vec3 vertPos;
        
        @uniform int mode;
        
        @uniform vec3 lightPos;
        @uniform vec3 diffuseColor;
        @uniform vec3 specColor;
        
        @out vec4 fragColor;
        
        @Override
        protected void main() {
            vec3 normal = normalize(normalInterp);
            vec3 lightDir = normalize(minus(lightPos, vertPos));
            
            float lambertian = max(dot(lightDir, normal), 0F);
            float specular = 0.0F;
            
            if (lambertian > 0.0F) {
                vec3 reflectDir = reflect(negative(lightDir), normal);
                vec3 viewDir = normalize(negative(vertPos));
                
                float specAngle = max(dot(reflectDir, viewDir), 0.0F);
                
                specular = pow(specAngle, 4.0F);                
                specular *= lambertian;
            }
            
            fragColor = vec4(plus(multiply(diffuseColor, lambertian), multiply(specColor, specular)), 1.0F);
        }
        
    }
}
