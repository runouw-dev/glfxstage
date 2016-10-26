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
public abstract class GLSL130Frag extends GLSL130 {
    protected abstract void main();
    
    protected @in vec4 gl_FragCoord;
    
    protected @in boolean gl_FrontFacing;
    
    protected @in float gl_ClipDistance[];
    
    protected @out float gl_FragDepth;
    
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
