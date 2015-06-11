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

    /**
     * The underlying OpenGL texture object. This object should be reinitialized
     * in the init function when needed since it may be deleted by
     * GLFont.delete.
     *
     * @since 15.06.11
     */
    protected GLTexture texture;
    private volatile boolean isInit = false;

    /**
     * Marks the GLFont object as being initialized. This should only be called
     * by the init function.
     *
     * @since 15.06.11
     */
    protected final void markInitializedAfter() {
        this.isInit = true;
    }

    /**
     * Constructs a new GLFont object on the default OpenGL thread.
     *
     * @since 15.06.11
     */
    public GLFont() {
        this(GLThread.getDefaultInstance());
    }

    /**
     * Constructs a new GLFont object on the default OpenGL thread.
     *
     * @param thread the thread to create the object on.
     * @since 15.06.11
     */
    public GLFont(final GLThread thread) {
        super(thread);

        this.texture = new GLTexture();
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
     * Checks if the GLFont object is valid. A GLFont object is considered valid
     * if it has been initialized and not deleted.
     *
     * @return true if valid
     * @since 15.06.11
     */
    public boolean isValid() {
        return this.isInit;
    }

    /**
     * Binds the GLFont object. This calls bind using the underlying GLTexture
     * object.
     *
     * @param target the target to bind to.
     * @since 15.06.11
     */
    public void bind(final int target) {
        this.texture.bind(target);
    }

    /**
     * A GLTask that binds the GLFont texture object to the specified sampler
     * target.
     *
     * @since 15.06.11
     */
    public class BindTask extends GLTask {

        final int target;

        /**
         * Constructs a new bind task with the specified sampler target.
         *
         * @param target the sampler target to bind the texture to.
         * @since 15.06.11
         */
        public BindTask(final int target) {
            if ((this.target = target) < 0) {
                throw new GLException("Invalid Font target!");
            }
        }

        @Override
        public void run() {
            if (!GLFont.this.isValid()) {
                throw new GLException("GLFont object is not valid!");
            }

            GLFont.this.texture.bind(target);
        }
    }

    private final GLTask deleteTask = new DeleteTask();

    /**
     * Deletes the GLFont object. This will delete the underlying texture
     * object.
     *
     * @since 15.06.11
     */
    public void delete() {
        this.deleteTask.glRun(this.getThread());
    }

    /**
     * A GLTask that deletes the underlying GLTexture object.
     *
     * @since 15.06.11
     */
    public class DeleteTask extends GLTask {

        @Override
        public void run() {
            if (!GLFont.this.isValid()) {
                throw new GLException("GLFont object needs to be initialized before calling delete!");
            }

            GLFont.this.texture.delete();
            GLFont.this.isInit = false;
        }
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

}
