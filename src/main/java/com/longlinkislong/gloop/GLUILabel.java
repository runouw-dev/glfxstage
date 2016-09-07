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
