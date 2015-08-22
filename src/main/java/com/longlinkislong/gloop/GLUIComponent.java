/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The base unit for UI objects.
 *
 * @author zmichaels
 * @since 15.08.21
 */
public abstract class GLUIComponent extends GLObject {

    private boolean isSelected = false;
    private boolean isVisible = true;
    private Consumer<GLUIComponent> selectCallback;
    private Consumer<GLUIComponent> actionCallback;

    
    /**
     * Retrieves the relative position of the GLUIMenu.
     *
     * @return the relative position.
     * @since 15.08.21
     */
    public abstract GLVec2F getRelativePosition();
    /**
     * Sets the visibility for the component. Invisible components and all of
     * their children will not render.
     *
     * @param isVisible visibility flag.
     * @since 15.08.21
     */
    public void setVisibility(final boolean isVisible) {
        this.isVisible = isVisible;
    }

    /**
     * Checks if the component is currently visible.
     *
     * @return true if the component is visible.
     * @since 15.08.21
     */
    public boolean isVisible() {
        return this.isVisible;
    }

    /**
     * Checks if the component is currently selected.
     *
     * @return true if the component is selected.
     * @since 15.08.21
     */
    public boolean isSelected() {
        return this.isSelected;
    }

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
        this.isSelected = true;
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
            final GLMat4F pr = GLMat4F.ortho(0, 1f, 1f, 0f, 0f, 1f).asStaticMat();
            final GLMat4F tr = GLMat4F.create();

            GLUIComponent.this.drawComponent(pr, tr);
        }
    }

    /**
     * Retrieves a list of all child components.
     *
     * @return a list of the components direct children or Optional.empty if it
     * has no children.
     * @since 15.08.21
     */
    public Optional<List<GLUIComponent>> getChildren() {
        return Optional.empty();
    }        
}
