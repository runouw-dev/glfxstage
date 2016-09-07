/*
 * Copyright (c) 2015, longlinkislong.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.longlinkislong.gloop;

/**
 * An OpenGL object that represents a font as a texture-like object. This is the
 * skeleton object for a font used by GLText. The actual implementation (init
 * and getMetrics) need to be implemented.
 *
 * @author zmichaels
 * @since 15.06.11
 */
public abstract class GLFont extends GLObject {
    protected final GLFontGlpyhSet supportedGlyphs;
    
    /**
     * The underlying OpenGL texture object. This object should be reinitialized
     * in the init function when needed since it may be deleted by
     * GLFont.delete.
     *
     * @since 15.06.11
     */
    protected GLTexture texture;

    /**
     * Constructs a new GLFont object on the default OpenGL thread.
     *
     * @since 15.07.01
     */
    public GLFont() {
        this(GLThread.getDefaultInstance(), GLFontGlpyhSet.DEFAULT_GLYPH_SET);
    }

    /**
     * Constructs a new GLFont object on the default OpenGL thread.
     *
     * @param thread the thread to create the object on.
     * @since 15.07.01
     */
    public GLFont(final GLThread thread) {
        this(thread, GLFontGlpyhSet.DEFAULT_GLYPH_SET);
    }
    
    /**
     * Constructs a new GLFont object on the default OpenGL thread.
     *
     * @param thread the thread to create the object on.
     * @param supportedGlyphs the set of glyphs supported.
     * @since 15.07.01
     */
    public GLFont(final GLThread thread, final GLFontGlpyhSet supportedGlyphs) {
        super(thread);
        
        this.texture = new GLTexture(thread);
        this.supportedGlyphs = supportedGlyphs.asUnmodifiable();
    }

    /**
     * Sets the parameters of the underlying texture object.
     *
     * @param params the
     */
    public void setParameters(final GLTextureParameters params) {
        this.texture.setAttributes(params);
    }    

    /**
     * Binds the GLFont object. This calls bind using the underlying GLTexture
     * object.
     *
     * @param target the target to bind to.
     * @since 15.06.11
     */
    public void bind(final int target) {
        this.newBindTask(target).glRun(this.getThread());
    }
    
    /**
     * Creates a new bind task for the underlying texture.
     * @param target the target to bind the texture to.
     * @return the GLTask.
     * @since 15.06.12
     */
    public GLTask newBindTask(final int target) {
        return this.texture.new BindTask(target);
    }       

    /**
     * Deletes the GLFont object. This will delete the underlying texture
     * object.
     *
     * @since 15.06.11
     */
    public void delete() {
        this.newDeleteTask().glRun(this.getThread());
    }
    
    /**
     * Creates a new GLTask for deleting the underlying texture object.
     * @return the GLTask
     * @since 15.06.12
     */
    public GLTask newDeleteTask() {
        return this.texture.new DeleteTask();
    }    

    /**
     * Initializes the GLFont object. This will fail if the GLFont is already
     * initialized.
     * 
     * @since 15.06.11
     */
    public abstract void init() throws GLException;

    /**
     * Retrieves the metrics associated with the underlying font object.
     * @return the metrics
     * @since 15.06.11
     */
    public abstract GLFontMetrics getMetrics();

    public GLFontGlpyhSet getSupportedGlyphs() {
        return supportedGlyphs;
    }

    public abstract int getWidth();
    public abstract int getHeight();
}
