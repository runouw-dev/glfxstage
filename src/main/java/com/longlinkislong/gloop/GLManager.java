/*
 * Copyright (c) 2015-2016, longlinkislong.com
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

import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * GLManager is a resource manager for GLObjects. Other resources can also be
 * managed by GLManager.
 *
 * @author zmichaels
 * @since 16.09.06
 */
public final class GLManager {

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
        if (obj == null) {
            // do nothing if the ManagedObject does not exist.
        } else if (!obj.keepManaged) {
            // do nothing if the ManagedObject is no longer managed
        } else if (obj.instance == null) {
            // do nothing if the ManagedObject has already been GCd
        } else {
            final T theInstance = obj.instance.get();

            if (theInstance == null) {
                // do nothing if the reference held has already been GCd
            } else if (obj.timeoutCheck.test(theInstance)) {
                // clean up the object if it is kill
                obj.cleanup.accept(theInstance);
                this.managedObjs.remove(obj);
                obj.instance = null;
            } else {
                // reschedule the check if it is still alive...
                final Runnable nextCheck = () -> this.check(obj);
                
                glgc.schedule(nextCheck, OBJ_TIMEOUT, OBJ_TIMEOUT_UNITS);
            }
        }
    }

    /**
     * Constructs a new managed vertex shader.
     *
     * @param code the code for the shader.
     * @return the managed vertex shader.
     * @since 16.09.06
     */
    public ManagedReference<GLShader> newManagedVertexShader(final String code) {
        final Supplier<GLShader> init = () -> GLShader.newVertexShader(code);

        return makeManaged(init.get(), init);
    }

    /**
     * Constructs a new managed fragment shader.
     *
     * @param code the code for the shader.
     * @return the managed fragment shader.
     * @since 16.09.06
     */
    public ManagedReference<GLShader> newManagedFragmentShader(final String code) {
        final Supplier<GLShader> init = () -> GLShader.newFragmentShader(code);

        return makeManaged(init.get(), init);
    }

    /**
     * Constructs a new managed compute shader.
     *
     * @param code the code for the shader.
     * @return the managed compute shader.
     * @since 16.09.06
     */
    public ManagedReference<GLShader> newManagedComputeShader(final String code) {
        final Supplier<GLShader> init = () -> GLShader.newComputeShader(code);

        return makeManaged(init.get(), init);
    }

    /**
     * Constructs a new managed geometry shader.
     *
     * @param code the code for the shader.
     * @return the managed geometry shader.
     * @since 16.09.06
     */
    public ManagedReference<GLShader> newManagedGeometryShader(final String code) {
        final Supplier<GLShader> init = () -> GLShader.newGeometryShader(code);

        return makeManaged(init.get(), init);
    }

    /**
     * Constructs a new managed tess control shader.
     *
     * @param code the code for the shader.
     * @return the managed tess control shader.
     * @since 16.09.06
     */
    public ManagedReference<GLShader> newManagedTessControlShader(final String code) {
        final Supplier<GLShader> init = () -> GLShader.newTessControlShader(code);

        return makeManaged(init.get(), init);
    }

    /**
     * Constructs a new managed tess evaluation shader.
     *
     * @param code the code for the shader.
     * @return the managed tess evaluation shader.
     * @since 16.09.06
     */
    public ManagedReference<GLShader> newManagedTessEvaluationShader(final String code) {
        final Supplier<GLShader> init = () -> GLShader.newTessEvaluationShader(code);

        return makeManaged(init.get(), init);
    }

    /**
     * Constructs a managed GLProgram out of an array of managed GLShaders.
     *
     * @param thread the OpenGL thread to create the GLProgram on.
     * @param attribs vertex attributes
     * @param managedShaders the array of shaders.
     * @return the managed GLProgram.
     * @since 16.09.06
     */
    public ManagedReference<GLProgram> newManagedProgram(
            final GLThread thread,
            final GLVertexAttributes attribs,
            final ManagedReference<GLShader>... managedShaders) {

        final Supplier<GLProgram> init = () -> {
            final GLProgram out = new GLProgram(thread);
            final GLShader[] shaders = new GLShader[managedShaders.length];

            Arrays.stream(managedShaders)
                    .map(ManagedReference::get)
                    .map(Optional::get)
                    .collect(Collectors.toList())
                    .toArray(shaders);

            out.setVertexAttributes(attribs);
            out.linkShaders(shaders, 0, shaders.length);

            return out;
        };

        return makeManaged(init.get(), init);
    }

    /**
     * Constructs a managed GLTexture out of a BufferedImage. This will use
     * whatever OpenGL thread available and the default GLTextureParameters.
     * This may generate mipmaps.
     *
     * @param img the BufferedImage to use.
     * @return the managed GLTexture.
     * @since 16.09.06
     */
    public ManagedReference<GLTexture> newManagedTexture(final BufferedImage img) {
        final Supplier<GLTexture> init = () -> TextureUtils.newTexture(img);

        return makeManaged(init.get(), init);
    }

    /**
     * Constructs a managed GLTexture out of a BufferedImage. The default
     * GLTextureParameters will be used. This may generate mipmaps.
     *
     * @param thread
     * @param img the BufferedImage to use.
     * @return the managed GLTexture.
     * @since 16.09.06
     */
    public ManagedReference<GLTexture> newManagedTexture(final GLThread thread, final BufferedImage img) {
        final Supplier<GLTexture> init = () -> TextureUtils.newTexture(thread, img);

        return makeManaged(init.get(), init);
    }

    /**
     * Constructs a managed GLTexture out of a BufferedImage. This may generate
     * mipmaps if the GLTextureParameters require mipmaps.
     *
     * @param thread the OpenGL thread to create the managed GLTexture on.
     * @param img the BufferedImage to use.
     * @param params the TextureParameters to use.
     * @return the managed GLTexture.
     * @since 16.09.06
     */
    public ManagedReference<GLTexture> newManagedTexture(
            final GLThread thread,
            final BufferedImage img, final GLTextureParameters params) {

        final Supplier<GLTexture> init = () -> TextureUtils.newTexture(thread, img, params);

        return makeManaged(init.get(), init);
    }

    /**
     * Makes a GLShader managed.
     *
     * @param instance the GLShader instance.
     * @param restore a method of restoring the GLShader.
     * @return the managed GLShader.
     * @since 16.09.06
     */
    public ManagedReference<GLShader> makeManaged(final GLShader instance, final Supplier<GLShader> restore) {
        return new ManagedReference<>(instance, getTimeoutTest(), GLShader::delete, restore);
    }

    /**
     * Makes a GLVertexArray managed.
     *
     * @param instance the GLVertexArray instance.
     * @param restore a method of restoring the GLVertexArray.
     * @return the managed GLVertexArray.
     * @since 16.09.06
     */
    public ManagedReference<GLVertexArray> makeManaged(final GLVertexArray instance, final Supplier<GLVertexArray> restore) {
        return new ManagedReference<>(instance, getTimeoutTest(), GLVertexArray::delete, restore);
    }

    /**
     * Makes a GLSampler managed.
     *
     * @param instance the GLSampler instance.
     * @param restore a method of restoriung the GLSampler.
     * @return the managed GLSampler.
     * @since 16.09.06
     */
    public ManagedReference<GLSampler> makeManaged(final GLSampler instance, final Supplier<GLSampler> restore) {
        return new ManagedReference<>(instance, getTimeoutTest(), GLSampler::delete, restore);
    }

    /**
     * Makes a GLFramebuffer managed.
     *
     * @param instance the GLFramebuffer instance.
     * @param restore a method of restoring the GLFramebuffer.
     * @return the managed GLFramebuffer.
     * @since 16.09.06
     */
    public ManagedReference<GLFramebuffer> makeManaged(final GLFramebuffer instance, final Supplier<GLFramebuffer> restore) {
        return new ManagedReference<>(instance, getTimeoutTest(), GLFramebuffer::delete, restore);
    }

    /**
     * Makes a GLRenderbuffer managed.
     *
     * @param instance the GLRenderbuffer instance.
     * @param restore the method of restoring the GLRenderbuffer.
     * @return the managed GLRenderbuffer.
     * @since 16.09.06
     */
    public ManagedReference<GLRenderbuffer> makeManaged(final GLRenderbuffer instance, final Supplier<GLRenderbuffer> restore) {
        return new ManagedReference<>(instance, getTimeoutTest(), GLRenderbuffer::delete, restore);
    }

    /**
     * Makes a GLProgram managed.
     *
     * @param instance the GLProgram instance.
     * @param restore the method of restoring the GLProgram.
     * @return the managed GLProgram.
     * @since 16.09.06
     */
    public ManagedReference<GLProgram> makeManaged(final GLProgram instance, final Supplier<GLProgram> restore) {
        return new ManagedReference<>(instance, getTimeoutTest(), GLProgram::delete, restore);
    }

    /**
     * Makes a GLBuffere managed.
     *
     * @param instance the GLBuffer instance.
     * @param restore the method of restoring the GLBuffer.
     * @return the managed GLBuffer.
     * @since 16.09.06
     */
    public ManagedReference<GLBuffer> makeManaged(final GLBuffer instance, final Supplier<GLBuffer> restore) {
        return new ManagedReference<>(instance, getTimeoutTest(), GLBuffer::delete, restore);
    }

    /**
     * Makes a GLTexture managed.
     *
     * @param instance the GLTexture instance.
     * @param restore the method of restoring the GLTexture.
     * @return the managed GLTexture.
     * @since 16.09.06
     */
    public ManagedReference<GLTexture> makeManaged(final GLTexture instance, final Supplier<GLTexture> restore) {
        return new ManagedReference<>(instance, getTimeoutTest(), GLTexture::delete, restore);
    }

    /**
     * A timeout test function that used
     * [code]GLObject.getTimeSinceLastUsed()[/code].
     *
     * @param <T> the type of GLObject.
     * @return the test predicate.
     * @since 16.09.06
     */
    public <T extends GLObject> Predicate<T> getTimeoutTest() {
        return i -> i.getTimeSinceLastUsed() > getTimeout();
    }

    // this is the true owner of the objects.
    private final Set<Object> managedObjs = new HashSet<>(0);

    /**
     * Removes all watched objects.
     *
     * @return A set containing all objects removed from the managed set.
     * @since 16.09.06
     */
    public Set<Object> clearManagedReferences() {
        final Set<Object> copy = new HashSet<>(this.managedObjs);

        this.managedObjs.clear();

        return copy;
    }

    /**
     * A wrapper for a reference managed by GLManager. This object does not own
     * the reference it wraps.
     *
     * @param <T> the type of object wrapped.
     * @since 16.09.06
     */
    public class ManagedReference<T> {

        private WeakReference<T> instance;
        private transient final Supplier<T> restore;
        private transient final Consumer<T> cleanup;
        private transient final Predicate<T> timeoutCheck;
        private boolean keepManaged = true;

        /**
         * Constructs a new ManagedReference.
         *
         * @param instance the instance to wrap.
         * @param timeoutCheck the function that checks for timeout.
         * @param cleanup the function to call when the reference is considered
         * dead.
         * @param restore the function to call when restoring the reference.
         * @since 16.09.06
         */
        public ManagedReference(final T instance, final Predicate<T> timeoutCheck, final Consumer<T> cleanup, final Supplier<T> restore) {
            this.instance = new WeakReference<>(Objects.requireNonNull(instance));
            this.restore = restore;
            this.timeoutCheck = Objects.requireNonNull(timeoutCheck);
            this.cleanup = Objects.requireNonNull(cleanup);

            final Runnable nextCheck = () -> check(this);
            
            glgc.schedule(nextCheck, OBJ_TIMEOUT, OBJ_TIMEOUT_UNITS);
            GLManager.this.managedObjs.add(this.instance);
        }

        /**
         * Gets the actual reference pointed to by the ManagedReference wrapper.
         * This will attempt to restore it if it no longer exists.
         *
         * @return the instance wrapped in an Optional. This may return
         * Optional.empty if the instance could not be restored.
         * @since 16.09.06
         */
        public Optional<T> get() {
            final T theInstance = (this.instance == null) ? null : this.instance.get();

            if (theInstance == null) {
                // we can't restore the object, so it is no longer managed...
                if (this.restore == null) {
                    this.keepManaged = false;
                    return Optional.empty();
                } else {
                    final T newInstance = this.restore.get();

                    // check if we can restore the object.
                    if (newInstance != null) {
                        this.instance = new WeakReference<>(theInstance);
                        // reschedule a gc check since the object was refreshed.
                        
                        final Runnable nextCheck = () -> check(this);
                        
                        glgc.schedule(nextCheck, OBJ_TIMEOUT, OBJ_TIMEOUT_UNITS);
                    } else {
                        this.keepManaged = false;
                    }

                    return Optional.ofNullable(theInstance);
                }
            } else {
                return Optional.of(theInstance);
            }
        }

        /**
         * Makes the ManagedReference no longer managed. This method will not
         * restore the object if it has already been garbage collected.
         *
         * @return the instance wrapped in an Optional. This may return
         * Optional.empty if the previously managed object has already been
         * garbage collected.
         * @since 16.09.06
         */
        public Optional<T> makeUnmanaged() {
            this.keepManaged = false;

            if (this.instance == null) {
                return Optional.empty();
            } else {
                return Optional.ofNullable(this.instance.get());
            }
        }
    }

    /**
     * Retrieves the instance of the GLManager.
     *
     * @return the GLManager.
     * @since 16.09.06
     */
    public static GLManager getInstance() {
        return Holder.INSTANCE;
    }

    private static final class Holder {

        private Holder() {
        }
        private static final GLManager INSTANCE = new GLManager();
    }
}
