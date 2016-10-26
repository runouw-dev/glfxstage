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
public abstract class ES2Frag extends ES2GLSL {    

    protected abstract void main();

    protected @mediump vec4 gl_FragColor;

    protected boolean gl_FrontFacing;

    protected @mediump int gl_PointCoord;

    protected @mediump vec4 gl_FragCoord;
    
    protected @mediump int gl_MaxTextureImageUnits;
    
    protected @mediump int gl_MaxFragmentUniformVectors;
    
    protected @mediump int gl_MaxDrawBuffers;

}
