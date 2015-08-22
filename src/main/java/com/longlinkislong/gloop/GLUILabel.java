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
    private final GLVec2F position = GLVec2F.create().asStaticVec();

    @Override
    public final GLVec2F getRelativePosition() {
        return this.position.copyTo(Vectors.DEFAULT_FACTORY);
    }
    /**
     * Constructs a new Label object on the default OpenGL thread.
     *
     * @param position the position of the GLUILabel.
     * @param text the text
     * @since 15.08.21
     */
    public GLUILabel(final GLVec2F position, final CharSequence text) {
        this(GLThread.getDefaultInstance(), position, text);
    }

    /**
     * Constructs a new Label object on the specified thread.
     *
     * @param thread the OpenGL thread to create the text object on.
     * @param position the position of the label relative to its parent.
     * @param text the text to display.
     * @since 15.08.21
     */
    public GLUILabel(final GLThread thread, final GLVec2F position, final CharSequence text) {
        super(thread);

        this.text = text.toString();
        this.position.set(position);
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
        if(!this.isVisible()) {
            return;
        }
        
        final GLMat4F tr = GLMat4F.translation(this.position.x(), this.position.y()).multiply(translation);
        final GLMat4F mvp = tr.multiply(projection);

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
}
