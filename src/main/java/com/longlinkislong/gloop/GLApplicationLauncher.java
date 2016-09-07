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
package com.longlinkislong.gloop;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 *
 * @author zmichaels
 */
class GLApplicationLauncher {

    private static final Marker MARKER = MarkerFactory.getMarker("GLOOP");
    private static final Logger LOGGER = LoggerFactory.getLogger(GLApplicationLauncher.class);

    private static class Holder {

        private static final GLApplicationLauncher INSTANCE = new GLApplicationLauncher();

        private Holder() {
        }
    }

    static GLApplicationLauncher getInstance() {
        return Holder.INSTANCE;
    }

    private GLApplicationLauncher() {
    }

    private final AtomicBoolean launchCalled = new AtomicBoolean(false);
    private volatile RuntimeException launchException = null;

    void launchApplication(final Class<? extends GLApplication> appClass, final String[] args) {
        if (launchCalled.getAndSet(true)) {
            throw new IllegalStateException("GLApplication launch must not be called more than once");
        }

        if (!GLApplication.class.isAssignableFrom(appClass)) {
            throw new IllegalArgumentException(String.format("Error: %s is not a subclass of com.longlinkislong.gloop.GLApplication", appClass));
        }

        final CountDownLatch launchLatch = new CountDownLatch(1);
        Thread launchThread = new Thread(() -> {
            try {
                doLaunchApplication(appClass, args);
            } catch (RuntimeException rte) {
                launchException = rte;
            } catch (Exception ex) {
                launchException = new RuntimeException("Exception occurred while launching GLApplication", ex);
            } catch (Error err) {
                launchException = new RuntimeException("Error occurrend while launching GLApplication", err);
            } finally {
                launchLatch.countDown();
            }
        });

        launchThread.setName("Gloop-Launcher");
        launchThread.start();

        try {
            launchLatch.await();
        } catch (InterruptedException ex) {
            throw new RuntimeException("GLApplication launch thread interrupted!", ex);
        }

        if (launchException != null) {
            throw launchException;
        }
    }

    private volatile Throwable stopException = null;

    void doLaunchApplication(final Class<? extends GLApplication> appClass, final String[] args) {                        
        GLApplication app = null;
        try {
            final Constructor<? extends GLApplication> c = appClass.getConstructor();

            app = c.newInstance();
            app.setParameters(new GLApplication.Parameters(args));
            LOGGER.trace(MARKER, "Constructed {}", app.getClass().getName());
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException t) {
            throw new RuntimeException("Unable to construct GLApplication", t);
        }

        try {
            app.init();
            LOGGER.trace(MARKER, "Initialized {}", app.getClass().getName());
        } catch (Throwable t) {
            throw new RuntimeException("Unable to initialize GLApplication", t);
        }

        final CountDownLatch shutdownLatch = new CountDownLatch(1);

        try {
            app.gloopInit.set(true);

            final GLWindow window = new GLWindow(app.initialWindowWidth, app.initialWindowHeight, app.initialWindowTitle);
            final GLApplication theApp = app;

            window.setOnClose(() -> {
                try {
                    theApp.stop();
                    theApp.setWindow(null);
                    LOGGER.trace(MARKER, "Stopped {}", theApp.getClass().getName());
                } catch (Throwable t) {
                    stopException = t;
                } finally {
                    shutdownLatch.countDown();
                }
            });

            app.setWindow(window);
            LOGGER.trace(MARKER, "Starting {}", app.getClass().getName());
            app.start(window);

            window.getGLThread().scheduleGLTask(GLTask.create(app::draw));
            window.getGLThread().scheduleGLTask(window.new UpdateTask());
        } catch (Throwable t) {
            throw new RuntimeException("Unable to start GLApplication", t);
        }

        try {
            shutdownLatch.await();
        } catch (InterruptedException ex) {
            throw new RuntimeException("GLApplication launch thread interrupted!", ex);
        }

        if (stopException != null) {
            throw new RuntimeException("Unable to stop GLApplication", stopException);
        }
    }
}
