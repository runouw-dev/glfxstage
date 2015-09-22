/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import com.longlinkislong.gloop.GLVertexArray.DrawArraysTask;
import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.embed.AbstractEvents;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.EmbeddedStageInterface;
import com.sun.javafx.embed.HostInterface;
import com.sun.javafx.stage.EmbeddedWindow;
import com.sun.javafx.tk.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.scene.Scene;

/**
 * GLFXStage is an OpenGL object that can contain and render JavaFX scene
 * objects.
 *
 * @author zmichaels
 * @since 15.09.21
 */
public class GLFXStage extends GLObject {

    static {
        PlatformImpl.startup(() -> {
        });
    }

    private int width;
    private int height;

    private volatile EmbeddedWindow stage;
    private EmbeddedSceneInterface emScene;
    private EmbeddedStageInterface emStage;
    //private volatile boolean isDirty = true;
    private float scaleFactor = 1f;
    private GLTexture texture;
    private ByteBuffer tBuffer;
    private final Lazy<GLBuffer> vPos = new Lazy<>(() -> {
        final GLBuffer verts = new GLBuffer();

        verts.upload(GLTools.wrapFloat(
                1f, -1f,
                -1f, -1f,
                1f, 1f,
                -1f, 1f));

        return verts;
    });
    private final Lazy<GLBuffer> vUVs = new Lazy<>(() -> {
        final GLBuffer texCoord = new GLBuffer();

        texCoord.upload(GLTools.wrapFloat(
                1f, 1f,
                0f, 1f,
                1f, 0f,
                0f, 0f));

        return texCoord;
    });

    private static final GLVertexAttributes ATTRIBUTES;

    static {
        ATTRIBUTES = new GLVertexAttributes();
        ATTRIBUTES.setAttribute("vPos", 0);
        ATTRIBUTES.setAttribute("vUVs", 1);
    }

    private static final Lazy<GLProgram> PROGRAM = new Lazy<>(() -> {
        try (InputStream inVsh = GLFXStage.class.getResourceAsStream("fx.vs");
                InputStream inFsh = GLFXStage.class.getResourceAsStream("fx.fs")) {
            final String srcVsh = GLTools.readAll(inVsh);
            final String srcFsh = GLTools.readAll(inFsh);

            final GLShader shVsh = new GLShader(GLShaderType.GL_VERTEX_SHADER, srcVsh);
            final GLShader shFsh = new GLShader(GLShaderType.GL_FRAGMENT_SHADER, srcFsh);

            final GLProgram program = new GLProgram();

            program.setVertexAttributes(ATTRIBUTES);
            program.linkShaders(shVsh, shFsh);

            shVsh.delete();
            shFsh.delete();

            return program;
        } catch (IOException ioex) {
            throw new GLException("Unable to load shaders!", ioex);
        }
    });

    private final Lazy<GLVertexArray> vao = new Lazy<>(() -> {
        final GLVertexArray vaoObj = new GLVertexArray();

        vaoObj.attachBuffer(
                ATTRIBUTES.getLocation("vPos"), this.vPos.get(),
                GLVertexAttributeType.GL_FLOAT, GLVertexAttributeSize.VEC2);
        vaoObj.attachBuffer(
                ATTRIBUTES.getLocation("vUVs"), this.vUVs.get(),
                GLVertexAttributeType.GL_FLOAT, GLVertexAttributeSize.VEC2);

        return vaoObj;
    });

    private final HostInterface hostContainer = new HostInterface() {

        @Override
        public void setEmbeddedStage(EmbeddedStageInterface embeddedStage) {
            if (GLFXStage.this.width > 0 && GLFXStage.this.height > 0) {
                embeddedStage.setSize(GLFXStage.this.width, GLFXStage.this.height);
            }

            embeddedStage.setLocation(0, 0);
            GLFXStage.this.emStage = embeddedStage;
        }

        @Override
        public void setEmbeddedScene(EmbeddedSceneInterface embeddedScene) {
            if (GLFXStage.this.emScene == embeddedScene) {
                return;
            }

            if (GLFXStage.this.width > 0 && GLFXStage.this.height > 0) {
                embeddedScene.setSize(GLFXStage.this.width, GLFXStage.this.height);
            }

            embeddedScene.setPixelScaleFactor(GLFXStage.this.scaleFactor);
            GLFXStage.this.emScene = embeddedScene;
        }

        @Override
        public boolean requestFocus() {
            return false;
        }

        @Override
        public boolean traverseFocusOut(boolean forward) {
            return false;
        }

        @Override
        public void repaint() {
            //GLFXStage.this.isDirty = true;
            GLFXStage.this.updateTexture();
        }

        @Override
        public void setPreferredSize(int width, int height) {

        }

        @Override
        public void setEnabled(boolean enabled) {

        }

        @Override
        public void setCursor(CursorFrame cursorFrame) {

        }

        @Override
        public boolean grabFocus() {
            return false;
        }

        @Override
        public void ungrabFocus() {

        }
    };

    /**
     * Retrieves the width of the stage.
     *
     * @return the stage width.
     * @since 15.09.21
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Retrieves the height of the stage.
     *
     * @return the stage height.
     * @since 15.09.21
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Constructs a new GLFXStage on the specified thread.
     *
     * @param thread the thread to use.
     * @param width the width of the stage.
     * @param height the height of the stage.
     * @since 15.09.21
     */
    public GLFXStage(final GLThread thread, final int width, final int height) {
        super(thread);

        this.resize(width, height);
    }

    /**
     * Constructs a new GLFXStage on the default thread used by GLObject.
     *
     * @param width the width of the stage.
     * @param height the height of the stage.
     * @since 15.09.21
     */
    public GLFXStage(final int width, final int height) {
        super();

        this.resize(width, height);
    }

    private void setSceneImpl(final Scene scene) {
        if ((this.stage != null) && (scene == null)) {
            this.stage.hide();
            this.stage = null;
        }

        if ((this.stage == null) && (scene != null)) {
            this.stage = new EmbeddedWindow(this.hostContainer);
        }

        if (this.stage != null) {
            this.stage.setScene(scene);

            if (!this.stage.isShowing()) {
                this.stage.show();
            }
        }
    }

    /**
     * Sets the scene.
     *
     * @param scene the scene.
     * @since 15.09.21
     */
    public void setScene(final Scene scene) {
        if (Toolkit.getToolkit().isFxUserThread()) {
            this.setSceneImpl(scene);
        } else {
            final CountDownLatch initLatch = new CountDownLatch(1);

            Platform.runLater(() -> {
                this.setSceneImpl(scene);
                initLatch.countDown();
            });

            boolean isInterrupted = false;
            boolean isComplete = false;

            while (!isComplete) {
                try {
                    initLatch.await();
                    isComplete = true;
                } catch (InterruptedException ex) {
                    isInterrupted = true;
                }
            }

            if (isInterrupted) {
                Thread.currentThread().interrupt();
            }

        }
    }

    /**
     * Resizes the stage.
     *
     * @param newWidth the new width of the stage.
     * @param newHeight the new height of the stage.
     * @since 15.09.21
     */
    public final void resize(final int newWidth, final int newHeight) {
        this.width = newWidth;
        this.height = newHeight;

        if (this.emScene != null) {
            this.emScene.setSize(width, height);
        }

        if (this.emStage != null) {
            this.emStage.setSize(width, height);
        }

        if (this.texture != null) {
            this.texture.delete();
        }

        this.tBuffer = ByteBuffer.allocateDirect(newWidth * newHeight * Integer.BYTES).order(ByteOrder.nativeOrder());
        this.texture = new GLTexture()
                .allocate(1, GLTextureInternalFormat.GL_RGBA8, newWidth, newHeight)
                .setAttributes(new GLTextureParameters()
                        .withFilter(GLTextureMinFilter.GL_LINEAR, GLTextureMagFilter.GL_LINEAR)
                        .withWrap(GLTextureWrap.GL_CLAMP_TO_EDGE, GLTextureWrap.GL_CLAMP_TO_EDGE, GLTextureWrap.GL_CLAMP_TO_EDGE));
    }

    private void updateTexture() {
        if (this.emScene != null) {            
            this.tBuffer.rewind();
            this.emScene.getPixels(this.tBuffer.asIntBuffer(), this.width, this.height);            
            
            this.texture.updateImage(0, 0, 0, this.width, this.height, GLTextureFormat.GL_BGRA, GLType.GL_UNSIGNED_BYTE, this.tBuffer);
            this.texture.setAttributes(new GLTextureParameters()
                        .withFilter(GLTextureMinFilter.GL_LINEAR, GLTextureMagFilter.GL_LINEAR)
                        .withWrap(GLTextureWrap.GL_CLAMP_TO_EDGE, GLTextureWrap.GL_CLAMP_TO_EDGE, GLTextureWrap.GL_CLAMP_TO_EDGE));
        }
    }

    /**
     * Draws the stage.
     *
     * @since 15.09.21
     */
    public void draw() {
        newDrawTask().glRun(this.getThread());
    }

    /**
     * Creates a new GLTask that draws the stage.
     *
     * @return the GLTask.
     * @since 15.09.21
     */
    public GLTask newDrawTask() {
        final GLTask bindTex = GLTask.create(() -> {            
            this.texture.bind(0);
        });

        return GLTask.join(
                PROGRAM.get().new UseTask(),
                PROGRAM.get().new SetUniformITask("fxTexture", 0),
                bindTex,
                vao.get().new DrawArraysTask(GLDrawMode.GL_TRIANGLE_STRIP, 0, 4));
    }

    /**
     * A GLKeyListener that handles all key press and release events needed by
     * the JavaFX objects.
     *
     * @since 15.09.21
     */
    public class KeyListener implements GLKeyListener {

        @Override
        public void keyActionPerformed(GLWindow glw, int key, int scanCode, GLKeyAction action, Set<GLKeyModifier> modifiers) {
            switch (action) {
                case KEY_PRESSED:
                case KEY_REPEAT:
                    if (scanCode == 22) {
                        GLFXStage.this.emScene.keyEvent(
                                AbstractEvents.KEYEVENT_PRESSED,
                                com.sun.glass.events.KeyEvent.VK_BACKSPACE,
                                new char[]{}, 0);
                    }

                    if (modifiers.contains(GLKeyModifier.SHIFT)) {
                        GLFXStage.this.shift = true;
                    }

                    if (modifiers.contains(GLKeyModifier.ALT)) {
                        GLFXStage.this.alt = true;
                    }

                    if (modifiers.contains(GLKeyModifier.CONTROL)) {
                        GLFXStage.this.ctrl = true;
                    }

                    if (modifiers.contains(GLKeyModifier.SUPER)) {
                        GLFXStage.this.meta = true;
                    }
                    break;
                case KEY_RELEASE:
                    if (scanCode == 22) {
                        GLFXStage.this.emScene.keyEvent(
                                AbstractEvents.KEYEVENT_RELEASED,
                                com.sun.glass.events.KeyEvent.VK_BACKSPACE,
                                new char[]{}, 0);
                    }

                    if (modifiers.contains(GLKeyModifier.SHIFT)) {
                        GLFXStage.this.shift = false;
                    }

                    if (modifiers.contains(GLKeyModifier.ALT)) {
                        GLFXStage.this.alt = false;
                    }

                    if (modifiers.contains(GLKeyModifier.CONTROL)) {
                        GLFXStage.this.ctrl = false;
                    }

                    if (modifiers.contains(GLKeyModifier.SUPER)) {
                        GLFXStage.this.meta = false;
                    }
                    break;
            }
        }
    }

    /**
     * A GLKeyCharListener that handles all key type events needed by the JavaFX
     * objects.
     *
     * @since 15.09.21
     */
    public class KeyCharListener implements GLKeyCharListener {

        @Override
        public void charTypePerformed(GLWindow glw, char c) {
            GLFXStage.this.emScene.keyEvent(AbstractEvents.KEYEVENT_TYPED, com.sun.glass.events.KeyEvent.VK_UNDEFINED, new char[]{c}, 0);
        }
    }

    private boolean leftButton = false;
    private boolean rightButton = false;
    private boolean middleButton = false;
    private int mouseX = 0;
    private int mouseY = 0;
    private boolean shift = false;
    private boolean alt = false;
    private boolean ctrl = false;
    private boolean meta = false;

    /**
     * A GLMouseButtonListener that handles all mouse click events needed by the
     * JavaFX objects.
     *
     * @since 15.09.21
     */
    public class MouseButtonListener implements GLMouseButtonListener {

        @Override
        public void mouseButtonActionPerformed(GLWindow glw, int button, GLMouseButtonAction action, Set<GLKeyModifier> set) {
            int buttonId = 0;

            if (action == GLMouseButtonAction.PRESSED) {
                GLFXStage.this.emStage.setFocused(true, AbstractEvents.FOCUSEVENT_ACTIVATED);
                switch (button) {
                    case 0:
                        buttonId = AbstractEvents.MOUSEEVENT_PRIMARY_BUTTON;
                        leftButton = true;
                        break;
                    case 1:
                        buttonId = AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON;
                        rightButton = true;
                        break;
                    case 2:
                        buttonId = AbstractEvents.MOUSEEVENT_MIDDLE_BUTTON;
                        middleButton = true;
                        break;
                }

                GLFXStage.this.emScene.mouseEvent(
                        AbstractEvents.MOUSEEVENT_PRESSED, buttonId,
                        leftButton, middleButton, rightButton,
                        mouseX, mouseY, mouseX, mouseY,
                        shift, ctrl, alt, meta, 0, false);
            } else if (action == GLMouseButtonAction.RELEASED) {
                switch (button) {
                    case 0:
                        buttonId = AbstractEvents.MOUSEEVENT_PRIMARY_BUTTON;
                        leftButton = false;
                        break;
                    case 1:
                        buttonId = AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON;
                        rightButton = false;
                        break;
                    case 2:
                        buttonId = AbstractEvents.MOUSEEVENT_MIDDLE_BUTTON;
                        middleButton = false;
                        break;
                }

                GLFXStage.this.emScene.mouseEvent(
                        AbstractEvents.MOUSEEVENT_RELEASED, buttonId,
                        leftButton, middleButton, rightButton,
                        mouseX, mouseY, mouseX, mouseY,
                        shift, ctrl, alt, meta, 0, false);
            }
        }
    }

    /**
     * A GLMousePositionListener that handles all mouse movement needed by the
     * JavaFX objects.
     *
     * @since 15.09.21
     */
    public class MousePositionListener implements GLMousePositionListener {

        @Override
        public void mousePositionActionPerformed(GLWindow glw, double x, double y) {
            if (GLFXStage.this.emScene == null) {
                return;
            }

            GLFXStage.this.mouseX = (int) x;
            GLFXStage.this.mouseY = (int) y;

            if (GLFXStage.this.leftButton || GLFXStage.this.rightButton || GLFXStage.this.middleButton) {
                GLFXStage.this.emScene.mouseEvent(
                        AbstractEvents.MOUSEEVENT_DRAGGED, AbstractEvents.MOUSEEVENT_NONE_BUTTON,
                        GLFXStage.this.leftButton, GLFXStage.this.middleButton, GLFXStage.this.rightButton,
                        GLFXStage.this.mouseX, GLFXStage.this.mouseY, GLFXStage.this.mouseX, GLFXStage.this.mouseY,
                        GLFXStage.this.shift, GLFXStage.this.ctrl, GLFXStage.this.alt, GLFXStage.this.meta,
                        0, false);
            } else {
                GLFXStage.this.emScene.mouseEvent(
                        AbstractEvents.MOUSEEVENT_MOVED, AbstractEvents.MOUSEEVENT_NONE_BUTTON,
                        GLFXStage.this.leftButton, GLFXStage.this.middleButton, GLFXStage.this.rightButton,
                        GLFXStage.this.mouseX, GLFXStage.this.mouseY, GLFXStage.this.mouseX, GLFXStage.this.mouseY,
                        GLFXStage.this.shift, GLFXStage.this.ctrl, GLFXStage.this.alt, GLFXStage.this.meta,
                        0, false);
            }
        }
    }
}
