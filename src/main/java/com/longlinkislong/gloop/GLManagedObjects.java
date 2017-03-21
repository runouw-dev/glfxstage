/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import com.longlinkislong.gloop.glspi.Buffer;
import com.longlinkislong.gloop.glspi.Framebuffer;
import com.longlinkislong.gloop.glspi.Program;
import com.longlinkislong.gloop.glspi.Renderbuffer;
import com.longlinkislong.gloop.glspi.Sampler;
import com.longlinkislong.gloop.glspi.Shader;
import com.longlinkislong.gloop.glspi.Texture;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A utility class for creating managed GLOOP objects.
 *
 * @author zmichaels
 */
public final class GLManagedObjects extends GLObject {

    private static final Map<GLThread, GLManagedObjects> CACHES = new HashMap<>();

    /**
     * Retrieves the instance of the GLManagedCache associated with the supplied
     * GLThread.
     *
     * @param thread the GLThread.
     * @return the GLManagedCache instance.
     */
    public static GLManagedObjects getInstance(final GLThread thread) {
        return CACHES.computeIfAbsent(thread, GLManagedObjects::new);
    }

    /**
     * Retrieves the instance of the GLManagedCache associated with either the
     * current thread or the default OpenGL thread.
     *
     * @return the GLManagedCache instance.
     */
    public static GLManagedObjects getInstance() {
        return getInstance(GLThread.getAny());
    }

    private GLManagedObjects(final GLThread thread) {
        super(thread);
        GLGC.schedule(GCHook::gc, 60, TimeUnit.SECONDS);
    }

    private static final class GCHook extends PhantomReference {

        private static final ReferenceQueue DUMMY = new ReferenceQueue();
        private static final List<GCHook> HOOKS = new LinkedList<>();

        private final Runnable onGC;

        private GCHook(Object referent, Runnable onGC) {
            super(referent, DUMMY);
            this.onGC = onGC;
        }

        private static <T> T createManaged(T ref, Runnable onGC) {
            HOOKS.add(new GCHook(ref, onGC));

            return ref;
        }

        private boolean clean() {
            if (this.isEnqueued()) {
                this.onGC.run();
                this.clear();

                return true;
            } else {
                return false;
            }
        }

        private static void gc() {
            Object dummy = DUMMY.poll();

            if (dummy == null) {
                return;
            }

            while (dummy != null) {
                dummy = DUMMY.poll();
            }

            HOOKS.removeIf(GCHook::clean);
            GLGC.schedule(GCHook::gc, 60, TimeUnit.SECONDS);
        }
    }

    /**
     * Invokes the GLGC
     */
    public static void gc() {
        GCHook.gc();
    }

    /**
     * Creates a new managed GLBuffer. The backing resource will be cleared
     * sometime after the GC claims the GLBuffer.
     *
     * @return the GLBuffer
     */
    public GLBuffer newBuffer() {
        final GLBuffer out = new GLBuffer(this.getThread());
        final Buffer handle = out.buffer;
        final GLTask deleteTask = GLTask.create(() -> {
            if (handle.isValid()) {
                GLTools.getDriverInstance().bufferDelete(handle);
            }
        });

        return GCHook.createManaged(out, () -> deleteTask.glRun(this.getThread()));
    }

    /**
     * Creates a new managed GLTexture. The backing resource will be cleared
     * sometime after the GC claims the GLTexture.
     *
     * @return the managed GLTexture.
     */
    public GLTexture newTexture() {
        final GLTexture out = new GLTexture(this.getThread());
        final Texture handle = out.texture;
        final GLTask deleteTask = GLTask.create(() -> {
            if (handle.isValid()) {
                GLTools.getDriverInstance().textureDelete(handle);
            }
        });

        return GCHook.createManaged(out, () -> deleteTask.glRun(this.getThread()));
    }

    /**
     * Creates a new managed GLShader. The backing resource will be cleared
     * sometime after the GC claims the GLShader.
     *
     * @param type the shader type
     * @param src the source.
     * @return the managed GLShader
     */
    public GLShader newShader(final GLShaderType type, final String src) {
        final GLShader out = new GLShader(this.getThread(), type, src);
        final Shader handle = out.shader;
        final GLTask deleteTask = GLTask.create(() -> GLTools.getDriverInstance().shaderDelete(handle));

        return GCHook.createManaged(out, () -> deleteTask.glRun(this.getThread()));
    }

    /**
     * Creates a new managed vertex shader. The backing resource will be cleared
     * sometime after the GC claims the GLShader.
     *
     * @param src the source.
     * @return the managed GLShader
     */
    public GLShader newVertexShader(final String src) {
        return newShader(GLShaderType.GL_VERTEX_SHADER, src);
    }

    /**
     * Creates a new managed fragment shader. The backing resource will be
     * cleared sometime after the GC claims the GLShader.
     *
     * @param src the source.
     * @return the managed GLShader
     */
    public GLShader newFragmentShader(final String src) {
        return newShader(GLShaderType.GL_FRAGMENT_SHADER, src);
    }

    /**
     * Creates a new managed geometry shader. The backing resource will be
     * cleared sometime after the GC claims the GLShader.
     *
     * @param src the source.
     * @return the managed GLShader
     */
    public GLShader newGeometryShader(final String src) {
        return newShader(GLShaderType.GL_GEOMETRY_SHADER, src);
    }

    /**
     * Creates a new managed tessellation control shader. The backing resource
     * will be cleared sometime after the GC claims the GLShader.
     *
     * @param src the shader source
     * @return the managed GLShader.
     */
    public GLShader newTessControlShader(final String src) {
        return newShader(GLShaderType.GL_TESS_CONTROL_SHADER, src);
    }

    /**
     * Creates a new managed tessellation evaluation shader. The backing
     * resource will be cleared sometime after the GC claims the GLShader.
     *
     * @param src the shader source.
     * @return the managed GLShader
     */
    public GLShader newTessEvaluationShader(final String src) {
        return newShader(GLShaderType.GL_TESS_EVALUATION_SHADER, src);
    }

    /**
     * Creates a new managed compute shader. The backing resource will be
     * cleared sometime after the GC claims the GLShader.
     *
     * @param src the source.
     * @return the managed GLShader
     */
    public GLShader newComputeShader(final String src) {
        return newShader(GLShaderType.GL_COMPUTE_SHADER, src);
    }

    /**
     * Creates a new managed GLProgram. The backing resource will be cleared
     * sometime after the GC claims the GLProgram.
     *
     * @return the managed GLProgram.
     */
    public GLProgram newProgram() {
        final GLProgram out = new GLProgram(this.getThread());
        final Program handle = out.program;
        final GLTask deleteTask = GLTask.create(() -> {
            if (handle.isValid()) {
                GLTools.getDriverInstance().programDelete(handle);
            }
        });

        return GCHook.createManaged(out, () -> deleteTask.glRun(this.getThread()));
    }

    /**
     * Creates a new managed GLSampler. The backing resource will be cleared
     * sometime after the GC claims the GLSampler.
     *
     * @param params the texture parameters
     * @return the managed GLSampler.
     */
    public GLSampler newSampler(final GLTextureParameters params) {
        final GLSampler out = new GLSampler(this.getThread(), params);
        final Sampler handle = out.sampler;
        final GLTask deleteTask = GLTask.create(() -> {
            if (handle.isValid()) {
                GLTools.getDriverInstance().samplerDelete(handle);
            }
        });

        return GCHook.createManaged(out, () -> deleteTask.glRun(this.getThread()));
    }

    /**
     * Creates a new managed GLFramebuffer. The backing resource will be cleared
     * sometime after the GC claims the GLFramebuffer.
     *
     * @return the managed GLFramebuffer
     */
    public GLFramebuffer newFramebuffer() {
        final GLFramebuffer out = new GLFramebuffer(this.getThread());
        final Framebuffer handle = out.framebuffer;
        final GLTask deleteTask = GLTask.create(() -> {
            if (handle.isValid()) {
                GLTools.getDriverInstance().framebufferDelete(handle);
            }
        });

        return GCHook.createManaged(out, () -> deleteTask.glRun(this.getThread()));
    }

    /**
     * Creates a new managed GLRenderbuffer. The backing resource will be
     * cleared sometime after the GC claims GLRenderbuffer.
     *
     * @param fmt the internal texture format
     * @param width the width of the renderbuffer
     * @param height the height of the renderbuffer
     * @return the managed GLRenderbuffer
     */
    public GLRenderbuffer newRenderbuffer(final GLTextureInternalFormat fmt, final int width, final int height) {
        final GLRenderbuffer out = new GLRenderbuffer(this.getThread(), fmt, width, height);
        final Renderbuffer handle = out.renderbuffer;
        final GLTask deleteTask = GLTask.create(() -> {
            if (handle.isValid()) {
                GLTools.getDriverInstance().renderbufferDelete(handle);
            }
        });

        return GCHook.createManaged(out, () -> deleteTask.glRun(this.getThread()));
    }

    private static final ScheduledExecutorService GLGC = Executors.newSingleThreadScheduledExecutor(task -> {
        final Thread out = new Thread(task);

        out.setName("GLGC");
        out.setDaemon(true);

        return out;
    });
}
