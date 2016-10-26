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
public abstract class ES2Vert extends ES2GLSL {
    protected abstract void main();
    
    protected @highp vec4 gl_Position;
    
    protected @mediump float gl_PointSize;
    
    protected int gl_MaxVertexAttribs;
    
    protected @mediump int gl_MaxVertexUniformVectors;
    
    protected @mediump int gl_MaxVaryingVectors;
    
    protected @mediump int gl_MaxVertexTextureImageUnits;
    
    protected @mediump int gl_MaxCombinedTextureImageUnits;
    
}
