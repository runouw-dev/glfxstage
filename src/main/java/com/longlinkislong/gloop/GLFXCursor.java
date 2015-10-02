/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import java.util.function.LongSupplier;
import static org.lwjgl.glfw.GLFW.GLFW_ARROW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CROSSHAIR_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_HIDDEN;
import static org.lwjgl.glfw.GLFW.GLFW_HAND_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_HRESIZE_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_IBEAM_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_VRESIZE_CURSOR;
import static org.lwjgl.glfw.GLFW.glfwCreateStandardCursor;

enum GLFXCursor {
    WAIT(()->{
        return GLQuery.create(()->{
            
            return glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        }).glCall();
    }),
    DEFAULT(()->{
        return GLQuery.create(()->{
            return glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        }).glCall();
    }),
    OPEN_HAND(() -> {
        return GLQuery.create(() -> {
            return glfwCreateStandardCursor(GLFW_HAND_CURSOR);
        }).glCall();
    }),
    MOVE(() -> {
        return GLQuery.create(() -> {
            return glfwCreateStandardCursor(GLFW_HAND_CURSOR);
        }).glCall();
    }),
    CROSSHAIR(()->{
        return GLQuery.create(()->{
            return glfwCreateStandardCursor(GLFW_CROSSHAIR_CURSOR);
        }).glCall();
    }),
    DISAPPEAR(()->{
        return GLQuery.create(()->{
            return glfwCreateStandardCursor(GLFW_CURSOR_HIDDEN);
        }).glCall();
    }),
    E_RESIZE(()->{
        return GLQuery.create(()->{
            return glfwCreateStandardCursor(GLFW_HRESIZE_CURSOR);
        }).glCall();
    }),
    W_RESIZE(()->{
        return GLQuery.create(()->{
            return glfwCreateStandardCursor(GLFW_HRESIZE_CURSOR);
        }).glCall();
    }),
    N_RESIZE(()->{
        return GLQuery.create(()->{
            return glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR);
        }).glCall();
    }),
    NE_RESIZE(()->{
        return GLQuery.create(()->{
            return glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR);
        }).glCall();
    }),
    NW_RESIZE(()->{
        return GLQuery.create(()->{
            return glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR);
        }).glCall();
    }),
    S_RESIZE(()->{
        return GLQuery.create(()->{
            return glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR);
        }).glCall();
    }),
    SE_RESIZE(()->{
        return GLQuery.create(()->{
            return glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR);
        }).glCall();
    }),
    SW_RESIZE(()->{
        return GLQuery.create(()->{
            return glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR);
        }).glCall();
    }),
    TEXT(()->{
        return GLQuery.create(()->{
            return glfwCreateStandardCursor(GLFW_IBEAM_CURSOR);
        }).glCall();
    });
    final Lazy<Long> glfwHandle;

    GLFXCursor(LongSupplier initializer) {
        this.glfwHandle = new Lazy<>(initializer::getAsLong);
    }

    public void apply(GLWindow window) {
        window.setCursor(glfwHandle.get());
    }
}
