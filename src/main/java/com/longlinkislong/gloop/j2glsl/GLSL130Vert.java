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
public abstract class GLSL130Vert extends GLSL130 {

    protected abstract void main();
    
    protected @out vec4 gl_Position;
    
    protected @out float gl_PointSize;
    
    protected @in int gl_VertexID;
    
    protected @out float gl_ClipDistance[];
    
    protected final int gl_MaxTextureUnits = 16;
    
    protected final int gl_MaxVertexAttribs = 16;
    
    protected final int gl_MaxVertexUniformComponents = 1024;
    
    protected final int gl_MaxVaryingFloats = 64;
    
    protected final int gl_MaxVaryingComponents = 64;
    
    protected final int gl_MaxVertexTextureImageUnits = 16;
    
    protected final int gl_MaxCombinedTextureImageUnits = 16;
    
    protected final int gl_MaxTextureImageUnits = 16;
    
    protected final int gl_MaxFragmentUniformComponents = 1024;
    
    protected final int gl_MaxDrawBuffers = 8;
    
    protected final int gl_MaxClipDistances = 8;
}
