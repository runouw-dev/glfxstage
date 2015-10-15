/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import com.longlinkislong.gloop.GLVertexArray.DrawArraysTask;
import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.cursor.CursorType;
import com.sun.javafx.embed.AbstractEvents;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.EmbeddedStageInterface;
import com.sun.javafx.embed.HostInterface;
import com.sun.javafx.stage.EmbeddedWindow;
import com.sun.javafx.tk.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.scene.Scene;
import org.lwjgl.glfw.GLFW;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_END;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_INSERT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;

/**
 * GLFX3DStage is an OpenGL object that can contain and render JavaFX scene
 * objects.
 *
 * @author zmichaels
 * @since 15.09.21
 */
public class GLFX3DStage extends GLObject {

    static {
        PlatformImpl.startup(() -> {
        });
    }

    private GLMat4F matrix = GLMat4F.create().asStaticMat();

    public void setMatrix(GLMat4F matrix) {
        this.matrix.set(matrix);
    }
    
    
    protected class MousePos{
        double x;
        double y;

        public MousePos(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    
    protected MousePos transformMouse(double x, double y){
        System.out.println("Before " + x + ", " + y);
        
        x /= Math.max(windowWidth, 1);
        y /= Math.max(windowHeight, 1);
        
        GLVec4F vec = GLVec4F.create((float)x, (float)y, 0, 1);
        GLVec4F after = matrix.inverse().multiply(vec);
        
        x = after.x() * width;
        y = after.y() * height;
        
        System.out.println("After " + x + ", " + y);
        
        return new MousePos(x, y);
    }
    
    private int windowWidth;
    private int windowHeight;
    private int width;
    private int height;
    private final GLMat projection = GLMat4F.ortho(0, 1f, 1f, 0f, 0f, 1f).asStaticMat();
    private volatile EmbeddedWindow stage;
    private EmbeddedSceneInterface emScene;
    private EmbeddedStageInterface emStage;
    //private volatile boolean isDirty = true;
    private float scaleFactor = 1f;
    private GLTexture texture;
    private volatile ByteBuffer tBuffer;
    private final Lazy<GLBuffer> vPos = new Lazy<>(() -> {
        final GLBuffer verts = new GLBuffer();

        verts.upload(GLTools.wrapFloat(
                1f, 1f,
                0f, 1f,
                1f, 0f,
                0f, 0f));

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
        try (InputStream inVsh = GLFX3DStage.class.getResourceAsStream("fx.vs");
                InputStream inFsh = GLFX3DStage.class.getResourceAsStream("fx.fs")) {
            final String srcVsh = GLTools.readAll(inVsh);
            final String srcFsh = GLTools.readAll(inFsh);

            final GLShader shVsh = new GLShader(GLShaderType.GL_VERTEX_SHADER, srcVsh);
            final GLShader shFsh = new GLShader(GLShaderType.GL_FRAGMENT_SHADER, srcFsh);

            final GLProgram program = new GLProgram();

            program.setVertexAttributes(ATTRIBUTES);
            program.linkShaders(shVsh, shFsh);

            program.setUniformMatrixF("vProj", GLMat4F.ortho(-1f, 1f, -1f, 1f, 0f, 1f));

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
            if (GLFX3DStage.this.width > 0 && GLFX3DStage.this.height > 0) {
                embeddedStage.setSize(GLFX3DStage.this.width, GLFX3DStage.this.height);
            }

            embeddedStage.setLocation(0, 0);
            GLFX3DStage.this.emStage = embeddedStage;
        }

        @Override
        public void setEmbeddedScene(EmbeddedSceneInterface embeddedScene) {
            if (GLFX3DStage.this.emScene == embeddedScene) {
                return;
            }

            if (GLFX3DStage.this.width > 0 && GLFX3DStage.this.height > 0) {
                embeddedScene.setSize(GLFX3DStage.this.width, GLFX3DStage.this.height);
            }

            embeddedScene.setPixelScaleFactor(GLFX3DStage.this.scaleFactor);
            GLFX3DStage.this.emScene = embeddedScene;
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
            GLFX3DStage.this.needsUpdate = true;
        }

        @Override
        public void setPreferredSize(int width, int height) {

        }

        @Override
        public void setEnabled(boolean enabled) {

        }

        @Override
        public void setCursor(CursorFrame cursorFrame) {
            final CursorType cursorType = cursorFrame.getCursorType();

            switch (cursorType) {
                case DEFAULT:
                    updateCursor(GLFXCursor.DEFAULT);
                    break;
                case MOVE:
                    updateCursor(GLFXCursor.MOVE);
                    break;
                case OPEN_HAND:
                    updateCursor(GLFXCursor.OPEN_HAND);
                    break;
                case CROSSHAIR:
                    updateCursor(GLFXCursor.CROSSHAIR);
                    break;
                case DISAPPEAR:
                    updateCursor(GLFXCursor.DISAPPEAR);
                    break;
                case E_RESIZE:
                    updateCursor(GLFXCursor.E_RESIZE);
                    break;
                case W_RESIZE:
                    updateCursor(GLFXCursor.W_RESIZE);
                    break;
                case N_RESIZE:
                    updateCursor(GLFXCursor.N_RESIZE);
                    break;
                case S_RESIZE:
                    updateCursor(GLFXCursor.S_RESIZE);
                    break;
                case NE_RESIZE:
                    updateCursor(GLFXCursor.NE_RESIZE);
                    break;
                case SE_RESIZE:
                    updateCursor(GLFXCursor.SE_RESIZE);
                    break;
                case NW_RESIZE:
                    updateCursor(GLFXCursor.NW_RESIZE);
                    break;
                case SW_RESIZE:
                    updateCursor(GLFXCursor.SW_RESIZE);
                    break;
                case TEXT:
                    updateCursor(GLFXCursor.TEXT);
                    break;
                case WAIT:
                    updateCursor(GLFXCursor.WAIT);
                    break;
                default:
                    break;
            }
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
     * Constructs a new GLFX3DStage on the specified thread.
     *
     * @param thread the thread to use.
     * @param width the width of the stage.
     * @param height the height of the stage.
     * @since 15.09.21
     */
    public GLFX3DStage(final GLThread thread, final int width, final int height) {
        super(thread);

        this.resize(width, height);
    }

    /**
     * Constructs a new GLFX3DStage on the default thread used by GLObject.
     *
     * @param width the width of the stage.
     * @param height the height of the stage.
     * @since 15.09.21
     */
    public GLFX3DStage(final int width, final int height) {
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
    public final void setParentWindowSize(final int newWidth, final int newHeight) {
        this.windowWidth = newWidth;
        this.windowHeight = newHeight;
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

        this.needsRecreate = true;
    }

    public final void scroll(final double deltaX, final double deltaY) {
        // TODO: this doesn't support horizontal scrolling! 
        // there must be a better way
        GLFX3DStage.this.emScene.mouseEvent(
                AbstractEvents.MOUSEEVENT_WHEEL, AbstractEvents.MOUSEEVENT_NONE_BUTTON,
                leftButton, middleButton, rightButton,
                mouseX, mouseY, mouseX, mouseY,
                shift, ctrl, alt, meta, -(int) deltaY, false);
    }

    // netbeans thinks these are unused. They are definitely being used.
    private volatile boolean needsRecreate = false;
    private volatile boolean needsUpdate = false;

    private void updateTexture() {
        if (this.emScene != null) {
            final int neededSize = this.width * this.height * Integer.BYTES;

            if (this.tBuffer == null || neededSize > this.tBuffer.capacity()) {
                this.tBuffer = ByteBuffer.allocateDirect(neededSize).order(ByteOrder.nativeOrder());
            }

            this.tBuffer.rewind();
            this.emScene.getPixels(this.tBuffer.asIntBuffer(), this.width, this.height);
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

    private void updateCursor(GLFXCursor cursor) {

        if (this.window == null || this.window.get() == null) {
            cursor.apply(GLWindow.listActiveWindows().get(0));
        } else {
            cursor.apply(this.window.get());
        }

    }

    /**
     * Creates a new GLTask that draws the stage.
     *
     * @return the GLTask.
     * @since 15.09.21
     */
    public GLTask newDrawTask() {
        final GLTask bindTask = GLTask.create(() -> {
            
            PROGRAM.get().setUniformMatrixF("vProj", this.matrix.multiply(this.projection));
            
            if (this.needsRecreate) {
                if (this.texture != null) {
                    this.texture.delete();
                }

                this.texture = new GLTexture(this.getThread())
                        .allocate(1, GLTextureInternalFormat.GL_RGBA8, this.width, this.height);

                this.needsRecreate = false;
            }

            if (this.needsUpdate) {
                this.updateTexture();

                this.texture
                        .updateImage(0, 0, 0, this.width, this.height, GLTextureFormat.GL_BGRA, GLType.GL_UNSIGNED_BYTE, this.tBuffer)
                        .setAttributes(new GLTextureParameters()
                                .withFilter(GLTextureMinFilter.GL_LINEAR, GLTextureMagFilter.GL_LINEAR)
                                .withWrap(GLTextureWrap.GL_CLAMP_TO_EDGE, GLTextureWrap.GL_CLAMP_TO_EDGE, GLTextureWrap.GL_CLAMP_TO_EDGE));
                this.needsUpdate = false;
            }

            this.texture.bind(0);
        });

        return GLTask.join(
                PROGRAM.get().new UseTask(),
                PROGRAM.get().new SetUniformITask("fxTexture", 0),
                bindTask,
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
            int keyId = -1;
            int mods = 0;

            if (modifiers.contains(GLKeyModifier.ALT)) {
                mods |= AbstractEvents.MODIFIER_ALT;
            }

            if (modifiers.contains(GLKeyModifier.CONTROL)) {
                mods |= AbstractEvents.MODIFIER_CONTROL;
            }

            if (modifiers.contains(GLKeyModifier.SHIFT)) {
                mods |= AbstractEvents.MODIFIER_SHIFT;
            }

            if (modifiers.contains(GLKeyModifier.SUPER)) {
                mods |= AbstractEvents.MODIFIER_META;
            }

            switch (key) {
                case GLFW_KEY_BACKSPACE:
                    keyId = com.sun.glass.events.KeyEvent.VK_BACKSPACE;
                    break;
                case GLFW_KEY_LEFT:
                    keyId = com.sun.glass.events.KeyEvent.VK_LEFT;
                    break;
                case GLFW_KEY_RIGHT:
                    keyId = com.sun.glass.events.KeyEvent.VK_RIGHT;
                    break;
                case GLFW_KEY_UP:
                    keyId = com.sun.glass.events.KeyEvent.VK_UP;
                    break;
                case GLFW_KEY_DOWN:
                    keyId = com.sun.glass.events.KeyEvent.VK_DOWN;
                    break;
                case GLFW_KEY_TAB:
                    keyId = com.sun.glass.events.KeyEvent.VK_TAB;
                    break;
                case GLFW_KEY_DELETE:
                    keyId = com.sun.glass.events.KeyEvent.VK_DELETE;
                    break;
                case GLFW_KEY_HOME:
                    keyId = com.sun.glass.events.KeyEvent.VK_HOME;
                    break;
                case GLFW_KEY_END:
                    keyId = com.sun.glass.events.KeyEvent.VK_END;
                    break;
                case GLFW_KEY_PAGE_UP:
                    keyId = com.sun.glass.events.KeyEvent.VK_PAGE_UP;
                    break;
                case GLFW_KEY_PAGE_DOWN:
                    keyId = com.sun.glass.events.KeyEvent.VK_PAGE_DOWN;
                    break;
                case GLFW_KEY_INSERT:
                    keyId = com.sun.glass.events.KeyEvent.VK_INSERT;
                    break;
                default:
                    if ((key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) || (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9)) {
                        keyId = key; // they're all the same -\_0_0_/-                        
                    }
            }

            switch (action) {
                case KEY_PRESSED:
                case KEY_REPEAT:
                    if (keyId > -1) {
                        GLFX3DStage.this.emScene.keyEvent(
                                AbstractEvents.KEYEVENT_PRESSED,
                                keyId,
                                new char[]{}, mods);
                    }

                    if (modifiers.contains(GLKeyModifier.SHIFT)) {
                        GLFX3DStage.this.shift = true;
                    }

                    if (modifiers.contains(GLKeyModifier.ALT)) {
                        GLFX3DStage.this.alt = true;
                    }

                    if (modifiers.contains(GLKeyModifier.CONTROL)) {
                        GLFX3DStage.this.ctrl = true;
                    }

                    if (modifiers.contains(GLKeyModifier.SUPER)) {
                        GLFX3DStage.this.meta = true;
                    }
                    break;
                case KEY_RELEASE:
                    if (keyId > -1) {
                        GLFX3DStage.this.emScene.keyEvent(
                                AbstractEvents.KEYEVENT_RELEASED,
                                keyId,
                                new char[]{}, mods);
                    }

                    if (modifiers.contains(GLKeyModifier.SHIFT)) {
                        GLFX3DStage.this.shift = false;
                    }

                    if (modifiers.contains(GLKeyModifier.ALT)) {
                        GLFX3DStage.this.alt = false;
                    }

                    if (modifiers.contains(GLKeyModifier.CONTROL)) {
                        GLFX3DStage.this.ctrl = false;
                    }

                    if (modifiers.contains(GLKeyModifier.SUPER)) {
                        GLFX3DStage.this.meta = false;
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
            int mods = GLFX3DStage.this.ctrl ? AbstractEvents.MODIFIER_CONTROL : 0;
            GLFX3DStage.this.emScene.keyEvent(AbstractEvents.KEYEVENT_TYPED, com.sun.glass.events.KeyEvent.VK_UNDEFINED, new char[]{c}, mods);
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
                GLFX3DStage.this.emStage.setFocused(true, AbstractEvents.FOCUSEVENT_ACTIVATED);
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

                GLFX3DStage.this.emScene.mouseEvent(
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

                GLFX3DStage.this.emScene.mouseEvent(
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
            if (GLFX3DStage.this.emScene == null) {
                return;
            }
            
            MousePos mouse = transformMouse(x, y);

            GLFX3DStage.this.mouseX = (int) mouse.x;
            GLFX3DStage.this.mouseY = (int) mouse.y;

            if (GLFX3DStage.this.leftButton) {
                GLFX3DStage.this.emScene.mouseEvent(
                        AbstractEvents.MOUSEEVENT_DRAGGED, AbstractEvents.MOUSEEVENT_PRIMARY_BUTTON,
                        GLFX3DStage.this.leftButton, GLFX3DStage.this.middleButton, GLFX3DStage.this.rightButton,
                        GLFX3DStage.this.mouseX, GLFX3DStage.this.mouseY, GLFX3DStage.this.mouseX, GLFX3DStage.this.mouseY,
                        GLFX3DStage.this.shift, GLFX3DStage.this.ctrl, GLFX3DStage.this.alt, GLFX3DStage.this.meta,
                        0, false);
            } else if (GLFX3DStage.this.rightButton) {
                GLFX3DStage.this.emScene.mouseEvent(
                        AbstractEvents.MOUSEEVENT_DRAGGED, AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON,
                        GLFX3DStage.this.leftButton, GLFX3DStage.this.middleButton, GLFX3DStage.this.rightButton,
                        GLFX3DStage.this.mouseX, GLFX3DStage.this.mouseY, GLFX3DStage.this.mouseX, GLFX3DStage.this.mouseY,
                        GLFX3DStage.this.shift, GLFX3DStage.this.ctrl, GLFX3DStage.this.alt, GLFX3DStage.this.meta,
                        0, false);
            } else if (GLFX3DStage.this.middleButton) {
                GLFX3DStage.this.emScene.mouseEvent(
                        AbstractEvents.MOUSEEVENT_DRAGGED, AbstractEvents.MOUSEEVENT_MIDDLE_BUTTON,
                        GLFX3DStage.this.leftButton, GLFX3DStage.this.middleButton, GLFX3DStage.this.rightButton,
                        GLFX3DStage.this.mouseX, GLFX3DStage.this.mouseY, GLFX3DStage.this.mouseX, GLFX3DStage.this.mouseY,
                        GLFX3DStage.this.shift, GLFX3DStage.this.ctrl, GLFX3DStage.this.alt, GLFX3DStage.this.meta,
                        0, false);
            } else {
                GLFX3DStage.this.emScene.mouseEvent(
                        AbstractEvents.MOUSEEVENT_MOVED, AbstractEvents.MOUSEEVENT_NONE_BUTTON,
                        GLFX3DStage.this.leftButton, GLFX3DStage.this.middleButton, GLFX3DStage.this.rightButton,
                        GLFX3DStage.this.mouseX, GLFX3DStage.this.mouseY, GLFX3DStage.this.mouseX, GLFX3DStage.this.mouseY,
                        GLFX3DStage.this.shift, GLFX3DStage.this.ctrl, GLFX3DStage.this.alt, GLFX3DStage.this.meta,
                        0, false);
            }
        }
    }

    public class StageResizeListener implements GLFramebufferResizeListener {

        @Override
        public void framebufferResizedActionPerformed(GLWindow glw, GLViewport view) {
            GLFX3DStage.this.setParentWindowSize(view.width, view.height);
        }
    }

    public class StageScrollListener implements GLMouseScrollListener {

        @Override
        public void mouseScrollActionPerformed(GLWindow glw, double x, double y) {
            GLFX3DStage.this.scroll(x, y);
        }
    }
    private Reference<GLWindow> window = null;
    private final Set<Object> activeListeners = new HashSet<>();

    public void addEvents(GLWindow window) {
        Objects.requireNonNull(window);
        this.window = new WeakReference<>(window);

        final GLKeyCharListener kcListener = new KeyCharListener();
        final GLKeyListener kListener = new KeyListener();
        final GLMouseButtonListener mbListener = new MouseButtonListener();
        final GLMousePositionListener mpListener = new MousePositionListener();
        final GLMouseScrollListener msListener = new StageScrollListener();
        final GLFramebufferResizeListener fbListener = new StageResizeListener();

        activeListeners.addAll(Arrays.asList(kcListener, kListener, mbListener, mpListener, msListener, fbListener));

        window.getKeyboard().addCharListener(kcListener);
        window.getKeyboard().addKeyListener(kListener);
        window.getMouse().addButtonListener(mbListener);
        window.getMouse().addPositionListener(mpListener);
        window.getMouse().addScrollListener(msListener);
        window.addWindowResizeListener(fbListener);
    }

    public void removeEvents(GLWindow window) {
        if (this.window == null) {
            return;
        } else if (this.window.get() != window) {
            throw new GLException("Supplied window is not the same reference as the current window!");
        }

        for (Object listener : activeListeners) {
            if (listener instanceof KeyCharListener) {
                window.getKeyboard().removeCharListener((KeyCharListener) listener);
            } else if (listener instanceof KeyListener) {
                window.getKeyboard().removeKeyListener((KeyListener) listener);
            } else if (listener instanceof MouseButtonListener) {
                window.getMouse().removeButtonListener((MouseButtonListener) listener);
            } else if (listener instanceof MousePositionListener) {
                window.getMouse().removePositionListener((MousePositionListener) listener);
            } else if (listener instanceof StageScrollListener) {
                window.getMouse().removeScrollListener((StageScrollListener) listener);
            } else if (listener instanceof StageResizeListener) {
                window.removeWindowResizeListener((StageResizeListener) listener);
            }
        }

        activeListeners.clear();
        this.window = null;
    }

}
