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
     * Retrieves the transformation matrix used for the component. It is
     * recommended to return a copy of the transformation matrix as opposed to
     * the actual transformation matrix.
     *
     * @return the transformation matrix.
     * @since 15.08.24
     */
    public abstract GLMat4F getTransformation();

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
     * Sets the callback for when the GLUIComponent is selected. The select
     * callback is called either through selecting the object with GLUIGraph or
     * immediately before the execute callback when fireMouseEvent determines
     * the component was clicked.
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

    /**
     * Executes the selection callback if present and marks the component as
     * selected.
     *
     * @since 15.08.24
     */
    public final void select() {
        this.isSelected = true;
        if (this.selectCallback != null) {
            this.selectCallback.accept(this);
        }
    }
    
    /**
     * Deselects the component.
     * @since 15.08.24
     */
    public final void deselect() {
        this.isSelected = false;
    }

    /**
     * Executes the action callback if present.
     *
     * @since 15.08.24
     */
    public final void execute() {
        if (this.actionCallback != null) {
            this.actionCallback.accept(this);
        }
    }

    /**
     * Draws the component at the specified location. All OpenGL calls should go here.
     * @param projection the projection matrix to use.
     * @param translation the model-view matrix to use.
     * @since 15.08.24
     */
    protected abstract void drawComponent(GLMat4F projection, GLMat4F translation);

    /**
     * Draws the component and all of its children.
     *
     * @since 15.08.24
     */
    public final void draw() {
        new DrawTask().glRun(this.getThread());
    }

    /**
     * GLTask for drawing the component and all of its children.
     *
     * @since 15.08.24
     */
    public final class DrawTask extends GLTask {

        @Override
        public void run() {
            final GLMat4F pr = GLUIComponent.this.getProjectionMatrix();
            final GLMat4F tr = GLMat4F.create();

            GLUIComponent.this.drawComponent(pr, tr);
        }
    }

    /**
     * Retrieves a projection matrix used for the component and all of its child
     * components. By default, this will return an ortho matrix on bounds x: [0,
     * 1], y: [0, 1], z: [0, 1]
     *
     * @return the projection matrix.
     * @since 15.08.24
     */
    protected GLMat4F getProjectionMatrix() {
        return GLMat4F.ortho(0f, 1f, 1f, 0f, 0f, 1f);
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

    private void fireClickEvent(final GLVec2F mousePos, final GLMat4F projection, final GLMat4F translation) {
        final GLMat4F modelView = this.getTransformation().multiply(translation);
        final Optional<GLVec2F> sz = this.getSize();

        // so much lambda
        this.getChildren()
                .ifPresent(children -> children
                        .forEach(child -> child
                                .fireClickEvent(mousePos, projection, modelView)));

        if (sz.isPresent()) {
            final GLViewport viewport = GLThread.getCurrent().orElseThrow(GLException::new).currentViewport;
            final GLMat4F modelViewProject = modelView.multiply(projection);
            final GLMat4F unproject = modelViewProject.inverse();
            // convert mouse from desktop space into screen space 
            final GLVec2F ssMouse = GLVec2F.create(mousePos.x() / viewport.width * 2f - 1f, 1f - mousePos.y() / viewport.height * 2f);
            // convert mouse from screen space to world space
            final GLVec2F wsMouse = unproject.multiply(ssMouse).asGLVec2F();

            final GLVec2F cSize = sz.get();

            if (wsMouse.x() > 0f && wsMouse.y() > 0f && wsMouse.x() < cSize.x() && wsMouse.y() < cSize.y()) {
                this.select();
                this.execute();
            }
        }
    }

    /**
     * Executes a click event on the given mouse position.
     *
     * @param mousePos the location of the mouse click.
     * @since 15.08.24
     */
    public final void fireClickEvent(final GLVec2 mousePos) {
        final GLMat4F pr = this.getProjectionMatrix();
        final GLMat4F tr = GLMat4F.create();

        this.fireClickEvent(mousePos.asGLVec2F(), pr, tr);
    }

    /**
     * Retrieves the component's size. This is only used for selectable objects.
     * Objects that should not be selectable should return Optional.empty(). By
     * default, this method returns Optional.empty().
     *
     * @return the size of the component if it is selectable.
     * @since 15.08.24
     */
    protected Optional<GLVec2F> getSize() {
        return Optional.empty();
    }
}
