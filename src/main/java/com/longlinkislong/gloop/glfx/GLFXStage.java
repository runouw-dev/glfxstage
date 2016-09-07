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

import com.longlinkislong.gloop.GLBuffer;
import com.longlinkislong.gloop.GLDrawMode;
import com.longlinkislong.gloop.GLException;
import com.longlinkislong.gloop.GLFramebufferResizeListener;
import com.longlinkislong.gloop.GLKeyAction;
import com.longlinkislong.gloop.GLKeyCharListener;
import com.longlinkislong.gloop.GLKeyListener;
import com.longlinkislong.gloop.GLKeyModifier;
import com.longlinkislong.gloop.GLMat4D;
import com.longlinkislong.gloop.GLMat4F;
import com.longlinkislong.gloop.GLMouseButtonAction;
import com.longlinkislong.gloop.GLMouseButtonListener;
import com.longlinkislong.gloop.GLMousePositionListener;
import com.longlinkislong.gloop.GLMouseScrollListener;
import com.longlinkislong.gloop.GLObject;
import com.longlinkislong.gloop.GLProgram;
import com.longlinkislong.gloop.GLShader;
import com.longlinkislong.gloop.GLShaderType;
import com.longlinkislong.gloop.GLTask;
import com.longlinkislong.gloop.GLTexture;
import com.longlinkislong.gloop.GLTextureFormat;
import com.longlinkislong.gloop.GLTextureInternalFormat;
import com.longlinkislong.gloop.GLTextureMagFilter;
import com.longlinkislong.gloop.GLTextureMinFilter;
import com.longlinkislong.gloop.GLTextureParameters;
import com.longlinkislong.gloop.GLTextureWrap;
import com.longlinkislong.gloop.GLThread;
import com.longlinkislong.gloop.GLTools;
import com.longlinkislong.gloop.GLType;
import com.longlinkislong.gloop.GLVec2D;
import com.longlinkislong.gloop.GLVertexArray;
import com.longlinkislong.gloop.GLVertexArray.DrawArraysTask;
import com.longlinkislong.gloop.GLVertexAttributeSize;
import com.longlinkislong.gloop.GLVertexAttributeType;
import com.longlinkislong.gloop.GLVertexAttributes;
import com.longlinkislong.gloop.GLViewport;
import com.longlinkislong.gloop.GLWindow;
import com.runouw.util.Lazy;
import com.runouw.util.Replaceable;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_CAPS_LOCK;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_END;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F25;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_INSERT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAUSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PRINT_SCREEN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * GLFX3DStage is an OpenGL object that can contain and render JavaFX scene
 * objects.
 *
 * @author zmichaels, rhewitt (modifications)
 * @since 15.10.19
 */
public class GLFXStage extends GLObject {

    private static final Marker JAVAFX_MARKER = MarkerFactory.getMarker("JAVAFX");
    private static final Logger LOGGER = LoggerFactory.getLogger(GLFXStage.class);

    static {
        PlatformImpl.startup(() -> LOGGER.debug(JAVAFX_MARKER, "JavaFX initialized!"));
    }

    private GLFXDNDHandler dndHandler;
    private GLFXContextMenuHandler contextMenuHandler;

    private Replaceable<Function<GLVec2D, GLVec2D>> mouseTransform = new Replaceable<>(() -> {
        return (p) -> p;
    });

    public void setMouseTransform(Function<GLVec2D, GLVec2D> transformFunc){
        mouseTransform.set(transformFunc);
    }

    private class MousePos {

        double x;
        double y;

        MousePos(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private MousePos transformMouse(double x, double y) {
        final GLVec2D mousePos = mouseTransform.get().apply(GLVec2D.create(x, y));

        x = mousePos.x();
        y = mousePos.y();

        return new MousePos(x, y);
    }

    private int windowWidth;
    private int windowHeight;
    private int width;
    private int height;
    private final GLMat4D projection = GLMat4D.ortho(0.0, 1.0, 1.0, 0.0, 0.0, 1.0).asStaticMat();
    private boolean focus = true;
    private boolean applyCursors = true;
    private CursorType cursorType = CursorType.DEFAULT;

    public boolean isFocus() {
        return this.focus;
    }

    public void setFocus(boolean focus) {
        if (this.focus == focus) {
            // no change
            return;
        }

        this.focus = focus;

        if (focus == false) {
            // release modifiers
            this.shift = false;
            this.alt = false;
            this.ctrl = false;
            this.meta = false;

            // I don't know if this line does anything
            //emStage.focusUngrab();
            if (GLFXStage.this.emStage != null) {
                GLFXStage.this.emStage.setFocused(false, AbstractEvents.FOCUSEVENT_DEACTIVATED);
            }
        } else if (GLFXStage.this.emStage != null) {
            GLFXStage.this.emStage.setFocused(true, AbstractEvents.FOCUSEVENT_ACTIVATED);
        }
    }

    public void setApplyCursors(boolean applyCursors) {
        this.applyCursors = applyCursors;
    }

    public CursorType getCursorType() {
        return cursorType;
    }

    private volatile EmbeddedWindow stage;
    private EmbeddedSceneInterface emScene;
    private EmbeddedStageInterface emStage;
    private float scaleFactor = 1f;
    private GLTexture texture;
    private volatile ByteBuffer tBuffer;
    private final Lazy<GLBuffer> vPos = new Lazy<>(() -> {
        final GLBuffer verts = new GLBuffer();

        verts.setName("GLFX3DStage.verts");
        verts.upload(GLTools.wrapFloat(
                0, 0f,
                0, 1f,
                1f, 0f,
                1f, 1f));

        return verts;
    });

    private final Lazy<GLBuffer> vUVs = new Lazy<>(() -> {
        final GLBuffer texCoord = new GLBuffer();

        texCoord.setName("GLFX3DStage.vUVs");
        texCoord.upload(GLTools.wrapFloat(
                0f, 0f,
                0f, 1f,
                1f, 0f,
                1f, 1f));

        return texCoord;
    });

    private static final GLVertexAttributes ATTRIBUTES;

    static {
        ATTRIBUTES = new GLVertexAttributes();
        ATTRIBUTES.setAttribute("vPos", 0);
        ATTRIBUTES.setAttribute("vUVs", 1);
    }

    private static final Lazy<GLProgram> PROGRAM = new Lazy<>(() -> {

        final String vertexShader;
        final String fragmentShader;

        switch (GLWindow.CLIENT_API) {
            case OPENGLES:
                vertexShader = "legacy_fx.vert";
                fragmentShader = "legacy_fx.frag";
                break;
            case OPENGL:
                vertexShader = "fx.vs";
                fragmentShader = "fx.fs";
                break;
            default:
                throw new UnsupportedOperationException("Unsupported client api: " + GLWindow.CLIENT_API);
        }

        try (InputStream inVsh = GLFXStage.class.getResourceAsStream(vertexShader);
                InputStream inFsh = GLFXStage.class.getResourceAsStream(fragmentShader)) {
            final String srcVsh = GLTools.readAll(inVsh);
            final String srcFsh = GLTools.readAll(inFsh);

            final GLShader shVsh = new GLShader(GLShaderType.GL_VERTEX_SHADER, srcVsh);
            final GLShader shFsh = new GLShader(GLShaderType.GL_FRAGMENT_SHADER, srcFsh);

            final GLProgram program = new GLProgram();

            program.setName("GLFX3DStage.PROGRAM");
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
        final GLVertexArray vaoObj = new GLVertexArray(this.getThread());

        vaoObj.setName("GLFX3DStage.vao");
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

            dndHandler = new GLFXDNDHandler(emScene, GLFXStage.this);
            contextMenuHandler = new GLFXContextMenuHandler(emScene, GLFXStage.this);
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
            GLFXStage.this.needsUpdate = true;
        }

        @Override
        public void setPreferredSize(int width, int height) {

        }

        @Override
        public void setEnabled(boolean enabled) {

        }

        @Override
        public void setCursor(CursorFrame cursorFrame) {

            GLFXStage.this.cursorType = cursorFrame.getCursorType();

            if (applyCursors) {
                switch (GLFXStage.this.cursorType) {
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
                        LOGGER.warn("Unknown cursor " + cursorFrame.getCursorType());
                        break;
                }
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
    public GLFXStage(final GLThread thread, final int width, final int height) {
        super(thread);

        if (width < 1) {
            throw new IllegalArgumentException("Width [" + width + "] must be at least 1!");
        } else if (height < 1) {
            throw new IllegalArgumentException("Height [" + height + "] must be at least 1!");
        }

        this.resize(width, height);

    }

    /**
     * Constructs a new GLFX3DStage on the default thread used by GLObject.
     *
     * @param width the width of the stage.
     * @param height the height of the stage.
     * @since 15.09.21
     */
    public GLFXStage(final int width, final int height) {
        this(GLThread.getDefaultInstance(), width, height);

        if (width < 1) {
            throw new IllegalArgumentException("Width [" + width + "] must be at least 1!");
        } else if (height < 1) {
            throw new IllegalArgumentException("Height [" + height + "] must be at least 1!");
        }

        this.resize(width, height);
    }

    private int oldEMX = 0;
    private int oldEMY = 0;

    /**
     * Sets the stage's absolute position. This must be accurate for popup menus
     * to appear in the correct location.
     * @param x
     * @param y
     */
    public void setStageABSLocation(int x, int y) {
        // caling the same position actually breaks menu components, so this avoids it
        if (oldEMX == x && oldEMY == y) {
            return;
        }

        this.emStage.setLocation(x, y);
        oldEMX = x;
        oldEMY = y;
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

    public Scene getScene(){
        if(this.stage != null){
            return this.stage.getScene();
        }
        return null;
    }
    public Parent getRootNode(){
        if(getScene() != null){
            return getScene().getRoot();
        }
        return null;
    }
    public ObservableList<Node> getRootChildren() {
        if (this.getRootNode() instanceof Group) {
            return ((Group) this.getRootNode()).getChildren();
        } else if (this.getRootNode() instanceof Pane) {
            return ((Pane) this.getRootNode()).getChildren();
        } else {
            return FXCollections.emptyObservableList();
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
                    LOGGER.warn("Thread was interrupted!");
                    LOGGER.warn(ex.getMessage(), ex);
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
        if (newWidth > 0 && newHeight > 0) {
            this.windowWidth = newWidth;
            this.windowHeight = newHeight;
        } else {
            LOGGER.debug("Parent window resize rejected; width or height is less than 1.");
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
        LOGGER.trace("Requested resize: [{}, {}]", newWidth, newHeight);

        if (newWidth > 0 && newHeight > 0) {
            this.width = newWidth;
            this.height = newHeight;

            if (this.emScene != null) {
                this.emScene.setSize(width, height);
            }

            if (this.emStage != null) {
                this.emStage.setSize(width, height);
            }

            this.needsRecreate = true;
        } else {
            LOGGER.debug("Resize rejected; width or height is less than 1.");
        }
    }

    public final void scroll(final double deltaX, final double deltaY) {
        // TODO: this doesn't support horizontal scrolling!
        // there must be a better way
        GLFXStage.this.emScene.mouseEvent(
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

            if (neededSize > 0) {
                if (this.tBuffer == null || neededSize > this.tBuffer.capacity()) {
                    if(this.tBuffer != null) {
                        MemoryUtil.memFree(this.tBuffer);
                    }

                    this.tBuffer = MemoryUtil.memAlloc(neededSize);
                }

                this.tBuffer.rewind();
                this.emScene.getPixels(this.tBuffer.asIntBuffer(), this.width, this.height);
            } else {
                LOGGER.trace("Request to read 0 bytes ignored.");
            }
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

    private void updateCursor(final GLFXCursor cursor) {

        if (this.window == null || this.window.get() == null) {
            cursor.apply(GLWindow.listActiveWindows().get(0));
        } else {
            cursor.apply(this.window.get());
        }
    }

    public GLTexture getAndUpdateTexture(){
        if (this.needsRecreate) {
            if (this.width > 0 && this.height > 0) {
                if (this.texture != null) {
                    this.texture.delete();
                }

                this.texture = new GLTexture(this.getThread())
                        .allocate(1, GLTextureInternalFormat.GL_RGBA8, this.width, this.height);

                this.needsRecreate = false;
            } else {
                LOGGER.debug("Ignored invalid request to resize texture to [width={}, height={}]", this.width, this.height);
            }
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
        return this.texture;
    }

    public GLTask newTextureBindTask(int loc){
        final GLTask bindTask = GLTask.create(() -> {
            getAndUpdateTexture();

            this.texture.bind(loc);
        });

        return bindTask;
    }

    /**
     * Creates a new GLTask that draws the stage.
     *
     * @return the GLTask.
     * @since 15.09.21
     */
    public GLTask newDrawTask() {
        final int bindLoc = 0;
        final GLTask bindTask = GLTask.create(() -> {
            this.projection.set(GLMat4D.ortho(0, (double)windowWidth/width, (double)windowHeight/height, 0, -1, 1));

            PROGRAM.get().setUniformMatrixF("vProj", this.projection);
        });
        final GLTask newTextureBindTask = newTextureBindTask(bindLoc);

        return GLTask.join(
                PROGRAM.get().new UseTask(),
                PROGRAM.get().new SetUniformITask("fxTexture", bindLoc),
                bindTask,
                newTextureBindTask,
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
            Platform.runLater(() -> {
                doKeyEvent(key, scanCode, action, modifiers);
            });
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
            Platform.runLater(() -> {
                doKeyCharEvent(c);
            });
        }
    }

    private boolean leftButton = false;
    private boolean rightButton = false;
    private boolean middleButton = false;
    private int mouseX = 0;
    private int mouseY = 0;
    private int mouseAbsX = 0;
    private int mouseAbsY = 0;
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
            Platform.runLater(() -> {
                doMouseButtonEvent(button, action, set);
            });
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
            Platform.runLater(() -> {
                doMousePositionEvent(x, y);
            });
        }
    }

    public class MouseScrollListener implements GLMouseScrollListener {

        @Override
        public void mouseScrollActionPerformed(GLWindow glw, double x, double y) {
            Platform.runLater(() -> {
                doMouseScrollEvent(x, y);
            });
        }
    }

    public class StageResizeListener implements GLFramebufferResizeListener {

        @Override
        public void framebufferResizedActionPerformed(GLWindow glw, GLViewport view) {
            Platform.runLater(() -> {
                GLFXStage.this.setParentWindowSize(view.width, view.height);
            });
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
        final GLMouseScrollListener msListener = new MouseScrollListener();
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
            } else if (listener instanceof MouseScrollListener) {
                window.getMouse().removeScrollListener((MouseScrollListener) listener);
            } else if (listener instanceof StageResizeListener) {
                window.removeWindowResizeListener((StageResizeListener) listener);
            }
        }

        activeListeners.clear();
        this.window = null;
    }

    public void doKeyCharEvent(char c) {
        if (!focus) {
            return;
        }

        int mods = GLFXStage.this.ctrl ? AbstractEvents.MODIFIER_CONTROL : 0;
        GLFXStage.this.emScene.keyEvent(AbstractEvents.KEYEVENT_TYPED, com.sun.glass.events.KeyEvent.VK_UNDEFINED, new char[]{c}, mods);
    }

    public void doKeyEvent(int key, int scanCode, GLKeyAction action, Set<GLKeyModifier> modifiers) {
        if (!focus) {
            return;
        }

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
            case GLFW_KEY_ENTER:
                keyId = com.sun.glass.events.KeyEvent.VK_ENTER;
                break;
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
            case GLFW_KEY_ESCAPE:
                keyId = com.sun.glass.events.KeyEvent.VK_ESCAPE;
                break;
            case GLFW_KEY_CAPS_LOCK:
                keyId = com.sun.glass.events.KeyEvent.VK_CAPS_LOCK;
                break;
            case GLFW_KEY_PAUSE:
                keyId = com.sun.glass.events.KeyEvent.VK_PAUSE;
                break;
            case GLFW_KEY_PRINT_SCREEN:
                keyId = com.sun.glass.events.KeyEvent.VK_PRINTSCREEN;
                break;
            case GLFW_KEY_LEFT_SHIFT:
            case GLFW_KEY_RIGHT_SHIFT:
                keyId = com.sun.glass.events.KeyEvent.VK_SHIFT;
                break;
            case GLFW_KEY_LEFT_CONTROL:
            case GLFW_KEY_RIGHT_CONTROL:
                keyId = com.sun.glass.events.KeyEvent.VK_CONTROL;
                break;
            case GLFW_KEY_LEFT_ALT:
            case GLFW_KEY_RIGHT_ALT:
                keyId = com.sun.glass.events.KeyEvent.VK_ALT;
                break;
            case 348:
                keyId = com.sun.glass.events.KeyEvent.VK_CONTEXT_MENU;
                break;
            default:
                if (key >= GLFW_KEY_F1 && key <= GLFW_KEY_F25) {
                    // F1 -> f12
                    keyId = com.sun.glass.events.KeyEvent.VK_F1 + (key - GLFW_KEY_F1);
                }else if (key > 0) {
                    keyId = key; // yolo -\_0_0_/-
                }
        }
        // TODO:
        // keyId = com.sun.glass.events.KeyEvent.VK_CONTEXT_MENU;

        GLFXStage.this.shift = modifiers.contains(GLKeyModifier.SHIFT);
        GLFXStage.this.alt = modifiers.contains(GLKeyModifier.ALT);
        GLFXStage.this.ctrl = modifiers.contains(GLKeyModifier.CONTROL);
        GLFXStage.this.meta = modifiers.contains(GLKeyModifier.SUPER);

        switch (action) {
            case KEY_PRESSED:
            case KEY_REPEAT:
                if (keyId > -1) {
                    GLFXStage.this.emScene.keyEvent(
                            AbstractEvents.KEYEVENT_PRESSED,
                            keyId,
                            new char[]{}, mods);
                }
                break;
            case KEY_RELEASE:
                if (keyId > -1) {

                    // window's shortcut to fire context menu
                    if(GLFXStage.this.shift && keyId == com.sun.glass.events.KeyEvent.VK_F10){
                        contextMenuHandler.fireContextMenuFromKeyboard();
                    }
                    if(keyId == com.sun.glass.events.KeyEvent.VK_CONTEXT_MENU){
                        contextMenuHandler.fireContextMenuFromKeyboard();
                    }

                    GLFXStage.this.emScene.keyEvent(
                            AbstractEvents.KEYEVENT_RELEASED,
                            keyId,
                            new char[]{}, mods);
                }
                break;
        }
    }

    public void doMouseButtonEvent(int button, GLMouseButtonAction action, Set<GLKeyModifier> set) {
        int buttonId = 0;

        if (action == GLMouseButtonAction.PRESSED) {

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
                    mouseX, mouseY, mouseAbsX, mouseAbsY,
                    shift, ctrl, alt, meta, 0, false);

            if(button == 0){
                dndHandler.mouseReleased(GLFXStage.this.mouseX, GLFXStage.this.mouseY, GLFXStage.this.mouseAbsX, GLFXStage.this.mouseAbsY);
            }
            if(button == 1){ // right click
                contextMenuHandler.fireContextMenuFromMouse(mouseX, mouseY, mouseAbsX, mouseAbsY);
            }
        }
    }

    public void doMousePositionEvent(double x, double y) {
        if (GLFXStage.this.emScene == null) {
            return;
        }

        MousePos mouse = transformMouse(x, y);

        GLFXStage.this.mouseX = (int) mouse.x;
        GLFXStage.this.mouseY = (int) mouse.y;
        GLFXStage.this.mouseAbsX = GLFXStage.this.mouseX + oldEMX;
        GLFXStage.this.mouseAbsY = GLFXStage.this.mouseY + oldEMY;

        if (GLFXStage.this.leftButton) {
            GLFXStage.this.emScene.mouseEvent(AbstractEvents.MOUSEEVENT_DRAGGED, AbstractEvents.MOUSEEVENT_PRIMARY_BUTTON,
                    GLFXStage.this.leftButton, GLFXStage.this.middleButton, GLFXStage.this.rightButton,
                    GLFXStage.this.mouseX, GLFXStage.this.mouseY, GLFXStage.this.mouseAbsX, GLFXStage.this.mouseAbsY,
                    GLFXStage.this.shift, GLFXStage.this.ctrl, GLFXStage.this.alt, GLFXStage.this.meta,
                    0, false);
        } else if (GLFXStage.this.rightButton) {
            GLFXStage.this.emScene.mouseEvent(AbstractEvents.MOUSEEVENT_DRAGGED, AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON,
                    GLFXStage.this.leftButton, GLFXStage.this.middleButton, GLFXStage.this.rightButton,
                    GLFXStage.this.mouseX, GLFXStage.this.mouseY, GLFXStage.this.mouseAbsX, GLFXStage.this.mouseAbsY,
                    GLFXStage.this.shift, GLFXStage.this.ctrl, GLFXStage.this.alt, GLFXStage.this.meta,
                    0, false);
        } else if (GLFXStage.this.middleButton) {
            GLFXStage.this.emScene.mouseEvent(AbstractEvents.MOUSEEVENT_DRAGGED, AbstractEvents.MOUSEEVENT_MIDDLE_BUTTON,
                    GLFXStage.this.leftButton, GLFXStage.this.middleButton, GLFXStage.this.rightButton,
                    GLFXStage.this.mouseX, GLFXStage.this.mouseY, GLFXStage.this.mouseAbsX, GLFXStage.this.mouseAbsY,
                    GLFXStage.this.shift, GLFXStage.this.ctrl, GLFXStage.this.alt, GLFXStage.this.meta,
                    0, false);
        } else {
            GLFXStage.this.emScene.mouseEvent(AbstractEvents.MOUSEEVENT_MOVED, AbstractEvents.MOUSEEVENT_NONE_BUTTON,
                    GLFXStage.this.leftButton, GLFXStage.this.middleButton, GLFXStage.this.rightButton,
                    GLFXStage.this.mouseX, GLFXStage.this.mouseY, GLFXStage.this.mouseAbsX, GLFXStage.this.mouseAbsY,
                    GLFXStage.this.shift, GLFXStage.this.ctrl, GLFXStage.this.alt, GLFXStage.this.meta,
                    0, false);
        }

        dndHandler.mousePosition(GLFXStage.this.mouseX, GLFXStage.this.mouseY, GLFXStage.this.mouseAbsX, GLFXStage.this.mouseAbsY);
    }

    public void doMouseScrollEvent(double x, double y) {
        if (!focus) {
            return;
        }

        GLFXStage.this.scroll(x, y);
    }

    public void clean() {
        this.texture.delete();
        this.vao.ifInitialized(GLVertexArray::delete);
        this.vPos.ifInitialized(GLBuffer::delete);
        this.vUVs.ifInitialized(GLBuffer::delete);

    }

}
