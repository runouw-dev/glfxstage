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

    private final GLMat4F transformation = GLMat4F.create().asStaticMat();
    private final List<GLUIComponent> children = new ArrayList<>();

    /**
     * Constructs a new Menu object on the default
     *     
     * @param transformation the transformation matrix for the menu.
     * @since 15.08.21
     */
    public GLUIMenu(final GLMat4F transformation) {
        this(GLThread.getDefaultInstance(), transformation);
    }

    /**
     * Constructs a new Menu object on the specified OpenGL thread.
     *
     * @param thread the OpenGL thread.
     * @param transformation the transformation matrix for the menu.
     * @since 15.08.21
     */
    public GLUIMenu(final GLThread thread, final GLMat4F transformation) {
        super(thread);

        this.transformation.set(transformation);
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

        final GLMat4F tr = this.transformation.multiply(translation);
        final GLMat4F mvp = tr.multiply(projection);

        this.drawMenu(mvp);

        this.children.forEach(child -> child.drawComponent(projection, tr));
    }
    
    @Override
    public GLMat4F getTransformation() {
        return this.transformation.copyTo(Matrices.DEFAULT_FACTORY);
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
