/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop.j2glsl;

/**
 *
 * @author zmichaels
 */
public class ES2GLSL extends GLSLBase {

    // vector functions
    protected static native float radians(float degrees);

    protected static native <genType> genType radians(genType degrees);

    protected static native float degrees(float radians);

    protected static native <genType> genType degrees(genType radians);

    protected static native float sin(float angle);

    protected static native <genType> genType sin(genType angle);

    protected static native float cos(float angle);
    
    protected static native <genType> genType cos(genType angle);

    protected static native float tan(float angle);
    
    protected static native <genType> genType tan(genType angle);

    protected static native float asin(float x);
    
    protected static native <genType> genType asin(genType x);

    protected static native float acos(float x);
    
    protected static native <genType> genType acos(genType x);   
    
    protected static native <genType> genType atan(genType y_over_x);

    protected static native float atan(float y, float x);
    
    protected static native <genType> genType atan(genType y, genType x);

    protected static native float pow(float x, float y);
    
    protected static native <genType> genType pow(genType x, genType y);

    protected static native float exp(float x);
    
    protected static native <genType> genType exp(genType x);

    protected static native float log(float x);
    
    protected static native <genType> genType log(genType x);

    protected static native float exp2(float x);
    
    protected static native <genType> genType exp2(genType x);

    protected static native float log2(float x);
    
    protected static native <genType> genType log2(genType x);

    protected static native float sqrt(float x);
    
    protected static native <genType> genType sqrt(genType x);

    protected static native float inversesqrt(float x);
    
    protected static native <genType> genType inversesqrt(genType x);

    protected static native float abs(float x);
    
    protected static native <genType> genType abs(genType x);

    protected static native float sign(float x);
    
    protected static native <genType> genType sign(genType x);

    protected static native float floor(float x);
    
    protected static native <genType> genType floor(genType x);

    protected static native float ceil(float x);
    
    protected static native <genType> genType ceil(genType x);

    protected static native float fract(float x);
    
    protected static native <genType> genType fract(genType x);

    protected static native float mod(float x, float y);
    
    protected static native <genType> genType mod(genType x, genType y);

    protected static native float min(float x, float y);
    
    protected static native <genType> genType min(genType x, genType y);

    protected static native float max(float x, float y);
    
    protected static native <genType> genType max(genType x, genType y);

    protected static native float clamp(float x, float min, float max);
    
    protected static native <genType> genType clamp(genType x, genType min, genType max);

    protected static native float mix(float x, float y, float a);
    
    protected static native <genType> genType mix(genType x, genType y, genType a);

    protected static native float step(float edge, float x);
    
    protected static native <genType> genType step(genType edge, genType x);

    protected static native float smoothstep(float edge0, float edge1, float x);
    
    protected static native <genType> genType smoothstep(genType edge0, genType edge1, genType x);

    protected static native float length(float x);
    
    protected static native <genType> float length(genType x);

    protected static native float distance(float p0, float p1);
    
    protected static native <genType> genType distance(genType p0, genType p1);           
    
    protected static native <genType> float dot(genType x, genType y);

    protected static native vec3 cross(vec3 x, vec3 y);    
    
    protected static native <genType> genType normalize(genType x);

    protected static native <genType> genType faceforward(genType N, genType I, genType Nref);

    protected static native <genType> genType reflect(genType I, genType N);

    protected static native <genType> genType refract(genType I, genType N, float eta);

    protected static native mat2 matrixCompMult(mat2 x, mat2 y);

    protected static native mat3 matrixCompMult(mat3 x, mat3 y);

    protected static native mat4 matrixCompMult(mat4 x, mat4 y);

    protected static native bvec2 lessThan(vec2 x, vec2 y);

    protected static native bvec3 lessThan(vec3 x, vec3 y);

    protected static native bvec4 lessThan(vec4 x, vec4 y);

    protected static native bvec2 lessThan(ivec2 x, ivec2 y);

    protected static native bvec3 lessThan(ivec3 x, ivec3 y);

    protected static native bvec4 lessThan(ivec4 x, ivec4 y);

    protected static native bvec2 lessThanEqual(vec2 x, vec2 y);

    protected static native bvec3 lessThanEqual(vec3 x, vec3 y);

    protected static native bvec4 lessThanEqual(vec4 x, vec4 y);

    protected static native bvec2 lessThanEqual(ivec2 x, ivec2 y);

    protected static native bvec3 lessThanEqual(ivec3 x, ivec3 y);

    protected static native bvec4 lessThanEqual(ivec4 x, ivec4 y);

    protected static native bvec2 greaterThan(vec2 x, vec2 y);

    protected static native bvec3 greaterThan(vec3 x, vec3 y);

    protected static native bvec4 greaterThan(vec4 x, vec4 y);

    protected static native bvec2 greaterThan(ivec2 x, ivec2 y);

    protected static native bvec3 greaterThan(ivec3 x, ivec3 y);

    protected static native bvec4 greaterThan(ivec4 x, ivec4 y);

    protected static native bvec2 greaterThanEqual(vec2 x, vec2 y);

    protected static native bvec3 greaterThanEqual(vec3 x, vec3 y);

    protected static native bvec4 greaterThanEqual(vec4 x, vec4 y);

    protected static native bvec2 greaterThanEqual(ivec2 x, ivec2 y);

    protected static native bvec3 greaterThanEqual(ivec3 x, ivec3 y);

    protected static native bvec4 greaterThanEqual(ivec4 x, ivec4 y);

    protected static native bvec2 equal(vec2 x, vec2 y);

    protected static native bvec3 equal(vec3 x, vec3 y);

    protected static native bvec4 equal(vec4 x, vec4 y);

    protected static native bvec2 equal(ivec2 x, ivec2 y);

    protected static native bvec3 equal(ivec3 x, ivec3 y);

    protected static native bvec4 equal(ivec4 x, ivec4 y);

    protected static native bvec2 notEqual(vec2 x, vec2 y);

    protected static native bvec3 notEqual(vec3 x, vec3 y);

    protected static native bvec4 notEqual(vec4 x, vec4 y);

    protected static native bvec2 notEqual(ivec2 x, ivec2 y);

    protected static native bvec3 notEqual(ivec3 x, ivec3 y);

    protected static native bvec4 notEqual(ivec4 x, ivec4 y);

    protected static native boolean any(bvec2 x);

    protected static native boolean any(bvec3 x);

    protected static native boolean any(bvec4 x);

    protected static native boolean all(bvec2 x);

    protected static native boolean all(bvec3 x);

    protected static native boolean all(bvec4 x);

    protected static native boolean not(bvec2 x);

    protected static native boolean not(bvec3 x);

    protected static native boolean not(bvec4 x);

    protected static native vec4 texture2D(sampler2D sampler, vec2 coord);

    protected static native vec4 texture2D(sampler2D sampler, vec2 coord, float bias);

    protected static native vec4 textureCube(samplerCube sampler, vec3 coord);

    protected static native vec4 textureCube(samplerCube sampler, vec3 coord, float bias);        
}
