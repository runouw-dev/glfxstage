/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Minimal implementation for a menu object. Menus can contain child components.
 * All child components are rendered with positions relative to its parent.
 *
 * @author zmichaels
 * @since 15.08.21
 */
public abstract class GLUIMenu extends GLUIComponent {

    private final GLVec2F position = GLVec2F.create().asStaticVec();
    private final List<GLUIComponent> children = new ArrayList<>();

    /**
     * Constructs a new Menu object on the default
     *
     * @param pos the position for the menu.
     * @since 15.08.21
     */
    public GLUIMenu(final GLVec2F pos) {
        this(GLThread.getDefaultInstance(), pos);
    }

    /**
     * Constructs a new Menu object on the specified OpenGL thread.
     *
     * @param thread the OpenGL thread.
     * @param pos the position of the menu.
     * @since 15.08.21
     */
    public GLUIMenu(final GLThread thread, final GLVec2F pos) {
        super(thread);

        this.position.set(pos);
    }

    /**
     * Method that actually draws the menu.
     *
     * @param mvp the model-view-projection matrix for the absolute value of the
     * menu.
     * @since 15.08.21
     */
    protected abstract void drawMenu(final GLMat4F mvp);

    @Override
    protected void drawComponent(final GLMat4F projection, final GLMat4F translation) {
        if (!this.isVisible()) {
            return;
        }

        final GLMat4F tr = GLMat4F.translation(position.x(), position.y()).multiply(translation);
        final GLMat4F mvp = tr.multiply(projection);

        this.drawMenu(mvp);

        this.children.forEach(child -> child.drawComponent(projection, tr));
    }
    
    @Override
    public final GLVec2F getRelativePosition() {
        return this.position.copyTo(Vectors.DEFAULT_FACTORY);
    }

    /**
     * Adds a child component to the menu.
     *
     * @param child the child to add.
     * @since 15.08.21
     */
    public void add(final GLUIComponent child) {
        this.children.add(Objects.requireNonNull(child));
    }

    /**
     * Removes a child component from the menu.
     *
     * @param child the child to remove.
     * @return true if the child was removed.
     * @since 15.08.21
     */
    public boolean remove(final GLUIComponent child) {
        return this.children.remove(child);
    }

    @Override
    public Optional<List<GLUIComponent>> getChildren() {
        if(this.children.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(Collections.unmodifiableList(this.children));
        }
    }        
}
