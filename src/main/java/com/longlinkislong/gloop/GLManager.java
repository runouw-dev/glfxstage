/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zmichaels
 */
public final class GLManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GLManager.class);

    private final ScheduledExecutorService glgc = Executors.newSingleThreadScheduledExecutor();
    private static final long OBJ_TIMEOUT = 3; // 3
    private static final TimeUnit OBJ_TIMEOUT_UNITS = TimeUnit.MINUTES;

    private static long getTimeout() {
        return OBJ_TIMEOUT_UNITS.toNanos(OBJ_TIMEOUT);
    }

    private GLManager() {
        // name the GLGC thread
        glgc.execute(() -> Thread.currentThread().setName("GLGC"));
    }

    private <T> void check(final ManagedReference<T> obj) {
        if (obj == null || obj.instance == null || obj.timeoutCheck == null) {
            // do nothing if the texture is already kill.
        } else if (obj.timeoutCheck.test(obj.instance)) {
            // delete the texture if it timesout.
            obj.cleanup.accept(obj.instance);
            obj.instance = null;
        } else {
            // reschedule the check if it has not timed out.
            glgc.schedule(() -> check(obj), OBJ_TIMEOUT, OBJ_TIMEOUT_UNITS);
        }
    }

    public ManagedReference<GLText> makeManaged(final GLText instance, final Supplier<GLText> restore) {
        final Predicate<GLText> timeout = text -> text.getTimeSinceLastUsed() > getTimeout();
        final ManagedReference<GLText> out = new ManagedReference<>(instance, timeout, GLText::delete, restore);

        glgc.schedule(() -> check(out), OBJ_TIMEOUT, OBJ_TIMEOUT_UNITS);

        return out;
    }


    public ManagedReference<GLVertexArray> makeManaged(final GLVertexArray instance, final Supplier<GLVertexArray> restore) {
        //TODO: fix implementation
        final Predicate<GLVertexArray> timeout = vao -> false;
        final ManagedReference<GLVertexArray> out = new ManagedReference<>(instance, timeout, GLVertexArray::delete, restore);

        glgc.schedule(() -> check(out), OBJ_TIMEOUT, OBJ_TIMEOUT_UNITS);

        return out;
    }

    public ManagedReference<GLShader> makeManaged(final GLShader instance, final Supplier<GLShader> restore) {
        final Predicate<GLShader> timeout = shader -> shader.getTimeSinceLastUpdate() > getTimeout();
        final ManagedReference<GLShader> out = new ManagedReference<>(instance, timeout, GLShader::delete, restore);

        glgc.schedule(() -> check(out), OBJ_TIMEOUT, OBJ_TIMEOUT_UNITS);

        return out;
    }

    public ManagedReference<GLSampler> makeManaged(final GLSampler instance, final Supplier<GLSampler> restore) {
        final Predicate<GLSampler> timeout = sampler -> sampler.getTimeSinceLastUpdate() > getTimeout();
        final ManagedReference<GLSampler> out = new ManagedReference<>(instance, timeout, GLSampler::delete, restore);

        glgc.schedule(() -> check(out), OBJ_TIMEOUT, OBJ_TIMEOUT_UNITS);

        return out;
    }

    public ManagedReference<GLFramebuffer> makeManaged(final GLFramebuffer instance, final Supplier<GLFramebuffer> restore) {
        final Predicate<GLFramebuffer> timeout = fb -> fb.getTimeSinceLastUsed() > getTimeout();
        final ManagedReference<GLFramebuffer> out = new ManagedReference<>(instance, timeout, GLFramebuffer::delete, restore);

        glgc.schedule(() -> check(out), OBJ_TIMEOUT, OBJ_TIMEOUT_UNITS);

        return out;
    }

    public ManagedReference<GLRenderbuffer> makeManaged(final GLRenderbuffer instance, final Supplier<GLRenderbuffer> restore) {
        //TODO: fix implement;
        final Predicate<GLRenderbuffer> timeout = rb -> false;
        final ManagedReference<GLRenderbuffer> out = new ManagedReference<>(instance, timeout, GLRenderbuffer::delete, restore);

        glgc.schedule(() -> check(out), OBJ_TIMEOUT, OBJ_TIMEOUT_UNITS);

        return out;
    }

    public ManagedReference<GLProgram> makeManaged(final GLProgram instance, final Supplier<GLProgram> restore) {
        final Predicate<GLProgram> timeout = program -> program.getTimeSinceLastUpdate() > getTimeout();
        final ManagedReference<GLProgram> out = new ManagedReference<>(instance, timeout, GLProgram::delete, restore);

        glgc.schedule(() -> check(out), OBJ_TIMEOUT, OBJ_TIMEOUT_UNITS);

        return out;
    }

    public ManagedReference<GLBuffer> makeManaged(final GLBuffer instance, final Supplier<GLBuffer> restore) {
        final Predicate<GLBuffer> timeout = buffer -> buffer.getTimeSinceLastUsed() > getTimeout();
        final ManagedReference<GLBuffer> out = new ManagedReference<>(instance, timeout, GLBuffer::delete, restore);

        glgc.schedule(() -> check(out), OBJ_TIMEOUT, OBJ_TIMEOUT_UNITS);

        return out;
    }

    public ManagedReference<GLTexture> makeManaged(final GLTexture instance, final Supplier<GLTexture> restore) {
        final Predicate<GLTexture> timeout = texture -> texture.getTimeSinceLastUsed() > getTimeout();
        final ManagedReference<GLTexture> out = new ManagedReference<>(instance, timeout, GLTexture::delete, restore);

        glgc.schedule(() -> check(out), OBJ_TIMEOUT, OBJ_TIMEOUT_UNITS);

        return out;
    }

    public class ManagedReference<T> {

        private T instance;
        private transient final Supplier<T> restore;
        private transient final Consumer<T> cleanup;
        private transient final Predicate<T> timeoutCheck;

        public ManagedReference(final T instance, final Predicate<T> timeoutCheck, final Consumer<T> cleanup, final Supplier<T> restore) {
            this.instance = Objects.requireNonNull(instance);
            this.restore = restore;
            this.timeoutCheck = Objects.requireNonNull(timeoutCheck);
            this.cleanup = Objects.requireNonNull(cleanup);
        }

        public T get() {
            if (this.instance == null) {
                // allowed to throw NullPointerException
                this.instance = this.restore.get();

                glgc.schedule(() -> check(this), OBJ_TIMEOUT, OBJ_TIMEOUT_UNITS);
            }

            return this.instance;
        }
    }

    public static GLManager getInstance() {
        return Holder.INSTANCE;
    }

    private static final class Holder {

        private Holder() {
        }
        private static final GLManager INSTANCE = new GLManager();
    }
}
