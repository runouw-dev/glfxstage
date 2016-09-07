/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.Assert.assertEquals;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengles.GLES20;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zmichaels
 */
public class TestFramework {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestFramework.class);
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;

    private final GLWindow window;

    public TestFramework(final String name, final Runnable init, final Runnable doFrame) {
        this.window = new GLWindow(WIDTH, HEIGHT, name);

        final GLThread thread = window.getGLThread();

        if (init != null) {
            thread.submitGLTask(GLTask.create(init));
        }
        
        if (doFrame != null) {
            thread.scheduleGLTask(GLTask.create(doFrame));
        }
        
        thread.scheduleGLTask(window.new UpdateTask());
    }

    public TestFramework showWindow() {
        this.window.setVisible(true);
        this.window.getKeyboard().addKeyListener(
                GLKeyListener.newFullscreenToggleListener(
                        GLFW.GLFW_KEY_ENTER,
                        Collections.singleton(GLKeyModifier.CONTROL)));
        this.window.addWindowResizeListener((GLWindow glw, GLViewport glv) -> {
            LOGGER.info("Display resized: width={} height={}", glv.width, glv.height);
        });
        return this;
    }

    public static void assertNoGLError() {
        switch (GLWindow.CLIENT_API) {
            case OPENGL:
                assertEquals(GL11.GL_NO_ERROR, GL11.glGetError());
                break;
            case OPENGLES:
                assertEquals(GLES20.GL_NO_ERROR, GLES20.glGetError());
                break;
            default:
                LOGGER.warn("Unsure how to check for vulkan errors...");
        }
    }

    public static BufferedImage renderToImage(final GLFramebuffer fb) {
        final int width = WIDTH;
        final int height = HEIGHT;

        final BufferedImage bImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final int imgSize = width * height;
        final ByteBuffer readPixels = MemoryUtil.memAlloc(imgSize * Integer.BYTES);
        final int[] writePixels = new int[imgSize];        

        fb.readPixels(0, 0, width, height, GLTextureFormat.GL_RGBA, GLType.GL_UNSIGNED_BYTE, readPixels);

        readPixels.asIntBuffer().get(writePixels);

        MemoryUtil.memFree(readPixels);

        bImg.setRGB(0, 0, width, height, writePixels, 0, width);

        return bImg;
    }

    public static void linkProgram(final GLProgram out, final String vshName, final String fshName) throws IOException {
        try (
                InputStream invsh = TestFramework.class.getResourceAsStream(vshName);
                InputStream infsh = TestFramework.class.getResourceAsStream(fshName)) {

            final String vshSrc = GLTools.readAll(invsh);
            final String fshSrc = GLTools.readAll(infsh);

            final GLShader vsh = GLShader.newVertexShader(vshSrc);
            final GLShader fsh = GLShader.newFragmentShader(fshSrc);

            out.linkShaders(vsh, fsh);

            vsh.delete();
            fsh.delete();
        }
    }

    public void runFor(long ms) {
        final AtomicBoolean waitLock = new AtomicBoolean(true);

        window.setOnClose(() -> waitLock.set(false));

        final long timerLock = System.currentTimeMillis() + ms;

        while(waitLock.get()) {
            if (System.currentTimeMillis() > timerLock) {
                window.close();
            }

            Thread.yield();
        }
    }
}
