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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * An application that uses a GLWindow object.
 *
 * @author zmichaels
 * @since 15.10.29
 */
public abstract class GLApplication {
    private static final Marker MARKER = MarkerFactory.getMarker("GLOOP");
    private static final Logger LOGGER = LoggerFactory.getLogger(GLApplication.class);
    private GLWindow window;
    private Parameters parameters = new Parameters(null);
    final AtomicBoolean gloopInit = new AtomicBoolean(false);
    int initialWindowWidth = 640;
    int initialWindowHeight = 480;
    String initialWindowTitle = this.getClass().getSimpleName();

    void setWindow(final GLWindow window) {
        this.window = window;
    }

    /**
     * Requests the GLWindow Object. This may return an empty Optional if the
     * GLWindow has not yet been initialized.
     *
     * @return the GLWindow object wrapped in an Optional.
     */
    public Optional<GLWindow> getGLWindow() {
        return Optional.ofNullable(this.window);
    }

    void setParameters(final Parameters params) {
        this.parameters = params;
        LOGGER.trace(MARKER, "Set GLApplication parameters to: {}", params);
    }

    /**
     * Retrieves formatted command line arguments.
     *
     * @return the formatted command line arguments.
     * @since 15.10.29
     */
    public Parameters getParameters() {
        return this.parameters;
    }

    /**
     * Enables off-heap vectors and initializes the reference cache to 20000.
     * This must be called in either the constructor or the init method.
     *
     * @since 15.11.11
     */
    public final void enableOffHeapVectors() {
        this.enableOffHeapVectors(20000);
    }

    /**
     * Enabled off-heap vectors. This must be called in either the constructor
     * or the init method.
     *
     * @param offHeapSize the number of references to allow off-heap.
     * @since 15.11.11
     */
    public final void enableOffHeapVectors(int offHeapSize) {
        if (this.gloopInit.get()) {            
            throw new IllegalStateException("Off-heap vectors can only be enabled before Gloop is initialized!");
        }

        this.setProperty("gloop.object_mapper", "com.longlinkislong.gloop.OffHeapMapper");
        this.setProperty("gloop.offheapmapper.max_refs", offHeapSize);
    }

    /**
     * Enables the debug output specified by gloop. This must be called in
     * either the constructor or the init method. This will only work if
     * slf4j-simple is used.
     *
     * @throws IllegalStateException if called after GLWindow is created.
     * @since 15.10.29
     */
    public final void enableDebug() {
        if (this.gloopInit.get()) {
            throw new IllegalStateException("Debug can only be enabled before Gloop is initialized!");
        }

        try {
            Class.forName("org.slf4j.impl.SimpleLogger");
            System.setProperty("org.slf4j.simpleLogger.log.com.longlinkislong.gloop", "debug");
            LOGGER.info(MARKER, "Set logging level for package: com.longlinkislong.gloop to debug.");
        } catch (ClassNotFoundException ex) {
            LOGGER.warn(MARKER, "Could not find SLF4J on classpath! GLApplication.enableDebug() only works with SLF4J-Simple.");
            LOGGER.trace(MARKER, ex.getMessage(), ex);
        }

    }

    /**
     * Enabled the trace output for gloop logs. This must be called in either
     * the constructor or the init method. This will only work if slf4j-simple
     * is used.
     *
     * @throws IllegalStateException if called after GLWindow is created.
     * @since 15.11.11
     */
    public final void enableTrace() {
        if (this.gloopInit.get()) {
            throw new IllegalStateException("Trace can only be enabled before Gloop is initialized!");
        }

        try {
            Class.forName("org.slf4j.impl.SimpleLogger");
            System.setProperty("org.slf4j.simpleLogger.log.com.longlinkislong.gloop", "trace");
        } catch (ClassNotFoundException ex) {
            LOGGER.warn(MARKER, "Could not find SLF4J on classpath! GLApplication.enableTrace() only works with SLF4J-Simple");
            LOGGER.trace(MARKER, ex.getMessage(), ex);
        }
    }

    /**
     * Sets the title of the window. This must be called in either the
     * constructor or the init method.
     *
     * @param title the title to use.
     * @throws IllegalStateException if called after GLWindow is created.
     * @since 15.10.29
     */
    public final void setTitle(final CharSequence title) {
        if (this.gloopInit.get()) {
            throw new IllegalStateException("The window title can only be set before gloop is initialized!");
        }

        this.initialWindowTitle = title.toString();
        LOGGER.trace(MARKER, "Set GLApplication.window.title = {}", title);
    }

    /**
     * Sets the initial size of the window. This must be called in either the
     * constructor or the init method.
     *
     * @param width the initial width of the window.
     * @param height the initial height of the window.
     * @throws IllegalStateException if called after GLWindow is created.
     * @since 15.10.29
     */
    public final void setInitialWindowSize(final int width, final int height) {        
        if (this.gloopInit.get()) {            
            throw new IllegalStateException("The initial window size can only be set before gloop is initialized!");
        }                

        if((this.initialWindowWidth = width) < 0) {
            LOGGER.error(MARKER, "Received initial width value: {}!", width);
            throw new IllegalArgumentException("Initial width cannot be less than 0!");
        } else if((this.initialWindowHeight = height) < 0) {
            LOGGER.error(MARKER, "Received initial height value: {}!", height);
            throw new IllegalArgumentException("Initial height cannot be less than 0!");
        }
        
        LOGGER.trace(MARKER, "Set GLApplication window initial size to [width={}, height={}]", width, height);
    }

    /**
     * Sets gloop to use checked OpenGL calls. This must be called in either the
     * constructor or the init method.
     *
     * @throws IllegalStateException if called after GLWindow is created.
     * @since 15.10.29
     */
    public final void enableGloopAsserts() {
        if (this.gloopInit.get()) {
            throw new IllegalStateException("Assertions can only be enabled before Gloop is initialized!");
        }

        GLApplication.class.getClassLoader().setPackageAssertionStatus("com.longlinkislong.gloop", true);
        LOGGER.trace(MARKER, "Enabled assertions on package: com.longlinkislong.gloop!");
    }

    /**
     * Shortcut to System.setProperty(key, value). This must be called in either
     * the constructor or the init method.
     *
     * @param property the property to set.
     * @param value the value to set.
     * @throws IllegalStateException if called after GLWindow is created.
     * @since 15.10.29
     */
    public final void setProperty(final CharSequence property, final Object value) {
        if (this.gloopInit.get()) {
            throw new IllegalStateException("Gloop properties can only be enabled before Gloop is initialized!");
        }

        System.setProperty(property.toString(), value.toString());
        LOGGER.trace(MARKER, "Set system property [{}] = [{}]", property, value);
    }

    /**
     * Initializes any components that do not require Gloop.
     *
     * @throws Exception if init fails.
     * @since 15.10.29
     */
    public void init() throws Exception {
    }

    /**
     * Initializes all OpenGL objects and creates the GLWindow.
     *
     * @param mainWindow the GLWindow object
     * @throws Exception if start fails.
     * @since 15.10.29
     */
    public abstract void start(final GLWindow mainWindow) throws Exception;

    /**
     * Clears all resources. This is assigned to the onClose callback for the
     * GLWindow.
     *
     * @throws Exception if stop fails.
     * @since 15.10.29
     */
    public void stop() throws Exception {
    }

    /**
     * A function that is ran once per frame on the OpenGL thread.
     *
     * @since 15.10.29
     */
    public void draw() {
    }

    /**
     * Launches the GLApplication.
     *
     * @param args the unprocessed command line arguments.
     * @since 15.10.29
     */
    @SuppressWarnings("unchecked")
    public static void launch(String... args) {
        // based on javafx.application.Application.launch
        final StackTraceElement[] cause = Thread.currentThread().getStackTrace();

        boolean foundThisMethod = false;
        String callingNameClass = null;

        for (StackTraceElement se : cause) {
            final String className = se.getClassName();
            final String methodName = se.getMethodName();

            if (foundThisMethod) {
                callingNameClass = className;
                break;
            } else if (GLApplication.class.getName().equals(className) && "launch".equals(methodName)) {
                foundThisMethod = true;
            }
        }

        if (callingNameClass == null) {
            throw new RuntimeException("Error: unable to determine GLApplication class");
        }

        try {
            final Class<?> theClass = Class.forName(callingNameClass, true,
                    Thread.currentThread().getContextClassLoader());

            if (GLApplication.class.isAssignableFrom(theClass)) {
                final Class<? extends GLApplication> appClass = (Class<? extends GLApplication>) theClass;

                LOGGER.trace(MARKER, "Launching application: [{}]!", appClass.getSimpleName());
                GLApplicationLauncher.getInstance().launchApplication(appClass, args);
            } else {
                throw new RuntimeException(String.format("Error: %s is not a subclass of com.longlinkislong.gloop.GLApplication", theClass));
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Unable to initialize GLApplication!", ex);
        }
    }

    /**
     * Parameters is a collection of processed command line arguments.
     *
     * @since 15.10.29
     */
    public static final class Parameters {

        private final List<String> rawArgs = new ArrayList<>();
        private final Map<String, String> namedArgs = new HashMap<>();
        private final List<String> unnamedArgs = new ArrayList<>();

        Parameters(final String[] params) {
            if (params != null) {
                this.init(Arrays.asList(params));
            }
        }

        private void init(final List<String> params) {
            this.rawArgs.addAll(params);
            this.computeNamedParams();
            this.computeUnnamedParams();
        }

        private boolean validFirstChar(final char c) {
            return Character.isLetter(c) || c == '_';
        }

        private boolean isNamedParam(final String arg) {
            if (arg.startsWith("--")) {
                return (arg.indexOf('=') > 2 && validFirstChar(arg.charAt(2)));
            } else {
                return false;
            }
        }

        private void computeNamedParams() {
            rawArgs.stream()
                    .filter(this::isNamedParam)
                    .forEach(arg -> {
                        final int eqIdx = arg.indexOf('=');
                        final String key = arg.substring(2, eqIdx);
                        final String value = arg.substring(eqIdx + 1);

                        this.namedArgs.put(key, value);
                    });
        }

        private void computeUnnamedParams() {
            rawArgs.stream()
                    .filter(arg -> !isNamedParam(arg))
                    .forEach(this.unnamedArgs::add);
        }

        /**
         * Retrieves all named parameters. Named parameters are declared as
         * '--[key]=[value]'.
         *
         * @return a map &lt;key, value&gt; of command line arguments.
         * @since 15.10.29
         */
        public Map<String, String> getNamed() {
            return Collections.unmodifiableMap(this.namedArgs);
        }

        /**
         * Retrieves all arguments in their unprocessed state.
         *
         * @return the list of arguments.
         * @since 15.10.29
         */
        public List<String> getRaw() {
            return Collections.unmodifiableList(this.rawArgs);
        }

        /**
         * Retrieves all unnamed arguments. Unnamed arguments are anything that
         * does not follow the rules for named arguments. They are left in their
         * unprocessed state.
         *
         * @return the list of unnamed arguments.
         * @since 15.10.29
         */
        public List<String> getUnnamed() {
            return Collections.unmodifiableList(this.unnamedArgs);
        }

        @Override
        public String toString() {
            return String.format("named: %s, unnamed: %s", getNamed(), getUnnamed());
        }
    }
}
