/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
     * @param supportedGlyphs
     * @since 15.07.01
     */
    public GLFont(final GLThread thread, GLFontGlpyhSet supportedGlyphs) {
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
