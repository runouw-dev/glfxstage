/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

/**
 * Minimum required support for a static text label.
 *
 * @author zmichaels
 * @since 15.08.28
 */
public abstract class GLUILabel extends GLUIComponent {

    private final String text;
    private final GLMat4F transformation = GLMat4F.create().asStaticMat();

    @Override
    public final GLMat4F getTransformation() {
        return this.transformation.copyTo(Matrices.DEFAULT_FACTORY);
    }

    /**
     * Constructs a new Label object on the default OpenGL thread.
     *
     * @param text the text
     * @param transformation the transformation matrix for the label.
     * @since 15.08.21
     */
    public GLUILabel(final CharSequence text, final GLMat4F transformation) {
        this(GLThread.getDefaultInstance(), text, transformation);
    }

    /**
     * Constructs a new Label object on the specified thread.
     *
     * @param thread the OpenGL thread to create the text object on.
     * @param text the text to display.
     * @param transformation the transformation matrix for the label.
     * @since 15.08.21
     */
    public GLUILabel(final GLThread thread, final CharSequence text, final GLMat4F transformation) {
        super(thread);

        this.text = text.toString();
        this.transformation.set(transformation);
    }

    /**
     * Method that actually draws the contents of the label.
     *
     * @param mvp the transformation matrix for the absolute location of the
     * label.
     * @param text the text to display.
     * @since 15.08.21
     */
    protected abstract void drawLabel(final GLMat4F mvp, final String text);

    @Override
    protected void drawComponent(final GLMat4F projection, final GLMat4F translation) {
        if (!this.isVisible()) {
            return;
        }
        
        final GLMat4F mvp = this.transformation.multiply(translation).multiply(projection);

        this.drawLabel(mvp, this.text);
    }

    /**
     * Retrieves the label's text.
     *
     * @return the text.
     * @since 15.08.21
     */
    public String getText() {
        return this.text;
    }
    
    @Override
    public String toString() {
        return String.format("Sample Label: [%s]", this.getText());
    }
}
