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
public class GLSLBase {
    protected static native vec2 vec2(float x, float y);

    protected static native vec2 vec2(float xy);

    protected static native vec2 vec2(vec3 xy);

    protected static native vec2 vec2(vec4 xy);

    protected static native vec3 vec3(float x, float y, float z);

    protected static native vec3 vec3(float xyz);

    protected static native vec3 vec3(vec4 xyz);

    protected static native vec3 vec3(vec2 xy, float z);

    protected static native vec3 vec3(float x, vec2 yz);

    protected static native vec4 vec4(float x, float y, float z, float w);

    protected static native vec4 vec4(float xyzw);

    protected static native vec4 vec4(vec2 xy, vec2 zw);

    protected static native vec4 vec4(vec2 xy, float z, float w);

    protected static native vec4 vec4(float x, vec2 yz, float w);

    protected static native vec4 vec4(float x, float y, vec2 zw);

    protected static native vec4 vec4(vec3 xyz, float w);

    protected static native vec4 vec4(float x, vec3 yzw);

    protected static native bvec2 bvec2(boolean x, boolean y);

    protected static native bvec2 bvec2(boolean xy);

    protected static native bvec3 bvec3(boolean x, boolean y, boolean z);

    protected static native bvec3 bvec3(boolean xyz);

    protected static native bvec3 bvec3(bvec2 xy, boolean z);

    protected static native bvec3 bvec3(boolean x, bvec2 yz);

    protected static native bvec4 bvec4(boolean x, boolean y, boolean z, boolean w);

    protected static native bvec4 bvec4(boolean xyzw);

    protected static native bvec4 bvec4(bvec2 xy, bvec2 zw);

    protected static native bvec4 bvec4(bvec2 xy, boolean z, boolean w);

    protected static native bvec4 bvec4(boolean x, bvec2 yz, boolean w);

    protected static native bvec4 bvec4(boolean x, boolean y, bvec2 zw);

    protected static native bvec4 bvec4(bvec3 xyz, boolean w);

    protected static native bvec4 bvec4(boolean x, bvec3 yzw);

    protected static native ivec2 ivec2(int x, int y);

    protected static native ivec2 ivec2(int xy);

    protected static native ivec3 ivec3(int x, int y, int z);

    protected static native ivec3 ivec3(int xyz);

    protected static native ivec3 ivec3(int x, ivec2 yz);

    protected static native ivec3 ivec3(ivec2 xy, int z);

    protected static native ivec4 ivec4(int x, int y, int z, int w);

    protected static native ivec4 ivec4(ivec2 xy, ivec2 zw);

    protected static native ivec4 ivec4(ivec2 xy, int z, int w);

    protected static native ivec4 ivec4(int x, ivec2 yz, int w);

    protected static native ivec4 ivec4(int x, int y, ivec2 zw);

    protected static native ivec4 ivec4(ivec3 xyz, int w);

    protected static native ivec4 ivec4(int x, ivec3 yzw);

    protected static native mat2 mat2(float e11, float e12, float e21, float e22);

    protected static native mat2 mat2(float a);

    protected static native mat2 mat2(vec2 c1, vec2 c2);

    protected static native mat3 mat3(float e11, float e12, float e13, float e21, float e22, float e23, float e31, float e32, float e33);

    protected static native mat3 mat3(float a);

    protected static native mat3 mat3(vec3 c1, vec3 c2, vec3 c3);

    protected static native mat4 mat4(float e11, float e12, float e13, float e14, float e21, float e22, float e23, float e24, float e31, float e32, float e33, float e34, float e41, float e42, float e43, float e44);

    protected static native mat4 mat4(float a);

    protected static native mat4 mat4(vec4 c1, vec4 c2, vec4 c3, vec4 c4);

    protected static native mat2 multiply(mat2 l, mat2 r);

    protected static native vec2 multiply(mat2 l, vec2 r);

    protected static native vec2 multiply(vec2 l, mat2 r);

    protected static native mat3 multiply(mat3 l, mat3 r);

    protected static native vec3 multiply(mat3 l, vec3 r);

    protected static native vec3 multiply(vec3 l, mat3 r);

    protected static native mat4 multiply(mat4 l, mat4 r);

    protected static native vec4 multiply(mat4 l, vec4 r);

    protected static native vec4 multiply(vec4 l, mat4 r);

    protected static native vec2 multiply(vec2 l, float r);

    protected static native vec2 multiply(float l, vec2 r);

    protected static native vec2 divide(vec2 l, float r);

    protected static native vec3 multiply(vec3 l, float r);

    protected static native vec3 multiply(float l, vec3 r);

    protected static native vec3 divide(vec3 l, float r);

    protected static native vec4 multiply(vec4 l, float r);

    protected static native vec4 multiply(float l, vec4 r);

    protected static native vec4 divide(vec4 l, float r);

    protected static native vec2 plus(vec2 l, vec2 r);

    protected static native ivec2 plus(ivec2 l, ivec2 r);

    protected static native vec2 minus(vec2 l, vec2 r);

    protected static native ivec2 minus(ivec2 l, ivec2 r);

    protected static native vec3 plus(vec3 l, vec3 r);

    protected static native vec3 minus(vec3 l, vec3 r);

    protected static native ivec3 plus(ivec3 l, ivec3 r);

    protected static native ivec3 minus(ivec3 l, ivec3 r);

    protected static native vec4 plus(vec4 l, vec4 r);

    protected static native vec4 minus(vec4 l, vec4 r);

    protected static native ivec4 plus(ivec4 l, ivec4 r);

    protected static native ivec4 minus(ivec4 l, ivec4 r);

    protected static native vec2 negative(vec2 x);

    protected static native vec3 negative(vec3 x);

    protected static native vec4 negative(vec4 x);

    protected static native ivec2 negative(ivec2 x);

    protected static native ivec3 negative(ivec3 x);

    protected static native ivec4 negative(ivec4 x);
}
