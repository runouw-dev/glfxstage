/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import com.runouw.util.Lazy;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import org.lwjgl.opengl.ARBSync;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengles.GLES;
import org.lwjgl.opengles.GLES20;
import org.lwjgl.opengles.GLES30;

/**
 * An implementation of sync objects that uses Java Future objects for syncing
 * between the OpenGL context, the OpenGL thread, and the current thread.
 *
 * @author zmichaels
 */
public final class GLFutureSync extends GLObject implements Future<Void> {

    private static enum Implementations {
        GL32,
        ARBSYNC,
        GLES30,
        NONE
    }

    private static final Lazy<Implementations> IMPLEMENTATION = new Lazy<>(() -> {
        switch (GLWindow.CLIENT_API) {
            case OPENGL:
                final GLCapabilities glCaps = GL.getCapabilities();

                if (glCaps.OpenGL32) {
                    return Implementations.GL32;
                } else if (glCaps.GL_ARB_sync) {
                    return Implementations.ARBSYNC;
                } else {
                    return Implementations.NONE;
                }
            case OPENGLES:
                if (GLES.getCapabilities().GLES30) {
                    return Implementations.GLES30;
                } else {
                    return Implementations.NONE;
                }
            default:
                return Implementations.NONE;
        }
    });

    /**
     * Fences a new GLFutureSync object on the supplied GLThread. This call
     * operates asynchronously.
     *
     * @param thread the thread to insert a sync object on
     * @return the GLFutureSync.
     */
    public static GLFutureSync fence(final GLThread thread) {
        return new GLFutureSync(thread);
    }

    private final AtomicLong syncObj = new AtomicLong(0);

    private static void waitSyncGL32(final long sync) {
        while (true) {
            final int result = GL32.glClientWaitSync(sync, GL32.GL_SYNC_FLUSH_COMMANDS_BIT, 1);

            if (result == GL32.GL_ALREADY_SIGNALED || result == GL32.GL_CONDITION_SATISFIED) {
                GL32.glDeleteSync(sync);
                break;
            }
        }
    }

    private static void waitSyncGLES30(final long sync) {
        while (true) {
            final int result = GLES30.glClientWaitSync(sync, GLES30.GL_SYNC_FLUSH_COMMANDS_BIT, 1);

            if (result == GLES30.GL_ALREADY_SIGNALED || result == GLES30.GL_CONDITION_SATISFIED) {
                GLES30.glDeleteSync(sync);
                break;
            }
        }
    }

    private static void waitSyncARBsync(final long sync) {
        while (true) {
            final int result = ARBSync.glClientWaitSync(sync, ARBSync.GL_SYNC_FLUSH_COMMANDS_BIT, 1);

            if (result == ARBSync.GL_ALREADY_SIGNALED || result == ARBSync.GL_CONDITION_SATISFIED) {
                ARBSync.glDeleteSync(sync);
                break;
            }
        }
    }

    private final Lazy<Future<?>> futureSync = new Lazy<>(() -> {
        return getThread().submitGLQuery(GLQuery.create(() -> {
            final long sync = syncObj.getAndSet(0L);

            if (sync != 0L) {
                switch (IMPLEMENTATION.get()) {
                    case GL32:
                        waitSyncGL32(sync);
                        break;
                    case ARBSYNC:
                        waitSyncARBsync(sync);
                        break;
                    case GLES30:
                        waitSyncGLES30(sync);
                        break;
                    default:
                    // do nothing
                }
            }

            return null;
        }));
    });

    private static final int SYNC_OBJECT_THRESHOLD = Integer.getInteger("com.longlinkislong.gloop.max_sync_objects", 16);
    private static final ThreadLocal<Queue<AtomicLong>> SYNC_OBJECTS = new ThreadLocal<Queue<AtomicLong>>() {
        @Override
        public Queue<AtomicLong> initialValue() {
            return new ArrayDeque<>();
        }
    };

    private static boolean testSyncGL32(final AtomicLong testSync) {
        final long sync = testSync.get();

        if (sync == 0L) {
            return true;
        } else {
            final int result = GL32.glClientWaitSync(sync, GL32.GL_SYNC_FLUSH_COMMANDS_BIT, 1);

            switch (result) {
                case GL32.GL_ALREADY_SIGNALED:
                case GL32.GL_CONDITION_SATISFIED:
                    GL32.glDeleteSync(sync);
                    testSync.set(0L);
                    return true;
                case GL32.GL_TIMEOUT_EXPIRED:
                    return false;
                case GL32.GL_WAIT_FAILED:
                    throw new GLException("glClientWaitSync failed! Err: " + GL11.glGetError());
                default:
                    throw new GLException("Unknown result for glClientWaitSync: " + result);
            }
        }
    }

    private static boolean testSyncGLES30(final AtomicLong testSync) {
        final long sync = testSync.get();

        if (sync == 0L) {
            return true;
        } else {
            final int result = GLES30.glClientWaitSync(sync, GLES30.GL_SYNC_FLUSH_COMMANDS_BIT, 1);

            switch (result) {
                case GLES30.GL_ALREADY_SIGNALED:
                case GLES30.GL_CONDITION_SATISFIED:
                    GLES30.glDeleteSync(sync);
                    testSync.set(0L);
                    return true;
                case GLES30.GL_TIMEOUT_EXPIRED:
                    return false;
                case GLES30.GL_WAIT_FAILED:
                    throw new GLException("glClientWaitSync failed! Err: " + GLES20.glGetError());
                default:
                    throw new GLException("Unknown result for glClientWaitSync: " + result);
            }
        }
    }

    private static boolean testSyncARBsync(final AtomicLong testSync) {
        final long sync = testSync.get();

        if (sync == 0L) {
            return true;
        } else {
            final int result = ARBSync.glClientWaitSync(sync, ARBSync.GL_SYNC_FLUSH_COMMANDS_BIT, 1);

            switch (result) {
                case ARBSync.GL_ALREADY_SIGNALED:
                case ARBSync.GL_CONDITION_SATISFIED:
                    ARBSync.glDeleteSync(sync);
                    testSync.set(0L);
                    return true;
                case ARBSync.GL_TIMEOUT_EXPIRED:
                    return false;
                case ARBSync.GL_WAIT_FAILED:
                    throw new GLException("glClientWaitSync failed! Err: " + GL11.glGetError());
                default:
                    throw new GLException("Unknown result for glClientWaitSync: " + result);
            }
        }
    }

    private GLFutureSync(final GLThread thread) {
        super(thread);

        thread.submitGLTask(GLTask.create(() -> {
            final Queue<AtomicLong> readQueue = SYNC_OBJECTS.get();
            Queue<AtomicLong> writeQueue = readQueue;

            // check for cleared sync objects in case the implementor forgot to clear a sync object.
            if (readQueue.size() > SYNC_OBJECT_THRESHOLD) {
                writeQueue = new ArrayDeque<>();

                while (!readQueue.isEmpty()) {
                    final AtomicLong testSync = readQueue.poll();
                    final boolean requeue;

                    switch (IMPLEMENTATION.get()) {
                        case GL32:
                            requeue = !testSyncGL32(testSync);
                            break;
                        case ARBSYNC:
                            requeue = !testSyncARBsync(testSync);
                            break;
                        case GLES30:
                            requeue = !testSyncGLES30(testSync);
                            break;
                        default:
                            requeue = false;
                    }

                    if (requeue) {
                        writeQueue.offer(testSync);
                    }
                }

                // update the thread's queue of Sync objects
                SYNC_OBJECTS.set(writeQueue);
            }

            switch (IMPLEMENTATION.get()) {
                case GL32:
                    syncObj.set(GL32.glFenceSync(GL32.GL_SYNC_GPU_COMMANDS_COMPLETE, 0));
                    writeQueue.offer(syncObj);
                    break;
                case ARBSYNC:
                    syncObj.set(ARBSync.glFenceSync(ARBSync.GL_SYNC_GPU_COMMANDS_COMPLETE, 0));
                    writeQueue.offer(syncObj);
                    break;
                case GLES30:
                    syncObj.set(GLES30.glFenceSync(GLES30.GL_SYNC_GPU_COMMANDS_COMPLETE, 0));
                    writeQueue.offer(syncObj);
                    break;
                default:
                // ???
            }
        }));
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (this.futureSync.isInitialized()) {
            return this.futureSync.get().cancel(mayInterruptIfRunning);
        } else {
            return false;
        }
    }

    @Override
    public boolean isCancelled() {
        if (this.futureSync.isInitialized()) {
            return this.futureSync.get().isCancelled();
        } else {
            return false;
        }
    }

    @Override
    public boolean isDone() {
        if (this.syncObj.get() == 0L) {
            // return true if the sync object was cleared while we weren't looking
            return true;
        } else if (this.futureSync.isInitialized()) {
            // return the forward the future's isDone status.
            return this.futureSync.get().isDone();
        } else {
            return false;
        }
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException {
        if (this.syncObj.get() != 0L) {
            this.futureSync.get().get();
        }

        return null;
    }

    @Override
    public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (this.syncObj.get() != 0L) {
            this.futureSync.get().get(timeout, unit);
        }

        return null;
    }

}
