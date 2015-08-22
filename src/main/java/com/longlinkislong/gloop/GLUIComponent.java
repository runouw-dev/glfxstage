/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import java.util.function.Consumer;

/**
 * The base unit for UI objects.
 *
 * @author zmichaels
 * @since 15.08.21
 */
public abstract class GLUIComponent extends GLObject {

    private Consumer<GLUIComponent> selectCallback;
    private Consumer<GLUIComponent> actionCallback;

    /**
     * Constructs a new UI component on the specified OpenGL thread.
     *
     * @param thread the OpenGL thread.
     * @since 15.08.21
     */
    public GLUIComponent(final GLThread thread) {
        super(thread);
    }

    /**
     * Constructs a new UI component on the default OpenGL thread.
     *
     * @since 15.08.21
     */
    public GLUIComponent() {
        super();
    }

    /**
     * Sets the callback for when the GLUIComponent is selected. This would be
     * for something like a mouse-over.
     *
     * @param callback the callback function.
     * @since 15.08.21
     */
    public final void setOnSelect(final Consumer<GLUIComponent> callback) {
        this.selectCallback = callback;
    }

    /**
     * Sets the callback for when the GLUIComponent is executed.
     *
     * @param callback the callback function.
     * @since 15.08.21
     */
    public final void setOnAction(final Consumer<GLUIComponent> callback) {
        this.actionCallback = callback;
    }

    void select() {
        if (this.selectCallback != null) {
            this.selectCallback.accept(this);
        }
    }

    void execute() {
        if (this.actionCallback != null) {
            this.actionCallback.accept(this);
        }
    }

    protected abstract void drawComponent(GLMat4F projection, GLMat4F translation);

    public final void draw() {
        new DrawTask().glRun(this.getThread());
    }

    public final class DrawTask extends GLTask {

        @Override
        public void run() {
            final GLThread thread = GLThread.getCurrent().orElseThrow(GLException::new);            
            final GLMat4F pr = GLMat4F.ortho(0, 1f, 1f, 0f, 0f, 1f).asStaticMat();
            final GLMat4F tr = GLMat4F.create();

            GLUIComponent.this.drawComponent(pr, tr);
        }
    }
}
