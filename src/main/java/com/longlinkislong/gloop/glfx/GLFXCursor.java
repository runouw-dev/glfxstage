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
package com.longlinkislong.gloop.glfx;

import com.longlinkislong.gloop.GLQuery;
import com.longlinkislong.gloop.GLWindow;
import com.runouw.util.Lazy;
import java.util.function.LongSupplier;
import static org.lwjgl.glfw.GLFW.GLFW_ARROW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CROSSHAIR_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_HIDDEN;
import static org.lwjgl.glfw.GLFW.GLFW_HAND_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_HRESIZE_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_IBEAM_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_VRESIZE_CURSOR;
import static org.lwjgl.glfw.GLFW.glfwCreateStandardCursor;

public enum GLFXCursor {
    WAIT(() -> GLQuery.create(() -> glfwCreateStandardCursor(GLFW_ARROW_CURSOR)).glCall()),
    DEFAULT(() -> GLQuery.create(() -> glfwCreateStandardCursor(GLFW_ARROW_CURSOR)).glCall()),
    OPEN_HAND(() -> GLQuery.create(() -> glfwCreateStandardCursor(GLFW_HAND_CURSOR)).glCall()),
    MOVE(() -> GLQuery.create(() -> glfwCreateStandardCursor(GLFW_HAND_CURSOR)).glCall()),
    CROSSHAIR(() -> GLQuery.create(() -> glfwCreateStandardCursor(GLFW_CROSSHAIR_CURSOR)).glCall()),
    DISAPPEAR(() -> GLQuery.create(() -> glfwCreateStandardCursor(GLFW_CURSOR_HIDDEN)).glCall()),
    E_RESIZE(() -> GLQuery.create(() -> glfwCreateStandardCursor(GLFW_HRESIZE_CURSOR)).glCall()),
    W_RESIZE(() -> GLQuery.create(() -> glfwCreateStandardCursor(GLFW_HRESIZE_CURSOR)).glCall()),
    N_RESIZE(() -> GLQuery.create(() -> glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR)).glCall()),
    NE_RESIZE(() -> GLQuery.create(() -> glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR)).glCall()),
    NW_RESIZE(() -> GLQuery.create(() -> glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR)).glCall()),
    S_RESIZE(() -> GLQuery.create(() -> glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR)).glCall()),
    SE_RESIZE(() -> GLQuery.create(() -> glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR)).glCall()),
    SW_RESIZE(() -> GLQuery.create(() -> glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR)).glCall()),
    TEXT(() -> GLQuery.create(() -> glfwCreateStandardCursor(GLFW_IBEAM_CURSOR)).glCall());
    
    final Lazy<Long> glfwHandle;

    GLFXCursor(LongSupplier initializer) {
        this.glfwHandle = new Lazy<>(initializer::getAsLong);
    }

    public void apply(GLWindow window) {
        window.setCursor(glfwHandle.get());
    }
}
