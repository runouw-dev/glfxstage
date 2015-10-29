/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

/**
 * An application that uses a GLWindow object.
 *
 * @author zmichaels
 * @sinec 15.10.29
 */
public abstract class GLApplication {

    private GLWindow window;
    private Parameters parameters = new Parameters(null);
    final AtomicBoolean gloopInit = new AtomicBoolean(false);

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
     * Retrieves the application's title. This is used for setting the GLWindow
     * title. Its default behavior is to return the simple name of the
     * Application class.
     *
     * @return the application title.
     * @since 15.10.29
     */
    public String getTitle() {
        return this.getClass().getSimpleName();
    }

    /**
     * Enables the debug output specified by gloop. This must be called in
     * either the constructor or the init method.
     *
     * @throws IllegalStateException if called after GLWindow is created.
     * @since 15.10.29
     */
    public final void enableDebug() {
        if (this.gloopInit.get()) {
            throw new IllegalStateException("Debug can only be enabled before Gloop is initialized!");
        }

        System.setProperty("debug", "true");
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
    public final void setProperty(final String property, final Object value) {
        if (this.gloopInit.get()) {
            throw new IllegalStateException("Gloop properties can only be enabled before Gloop is initialized!");
        }

        System.setProperty(property, value.toString());
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
    public static void launch(String... args) {
        // based on javafx.application.Application.launch
        StackTraceElement[] cause = Thread.currentThread().getStackTrace();

        boolean foundThisMethod = false;
        String callingNameClass = null;

        for (StackTraceElement se : cause) {
            String className = se.getClassName();
            String methodName = se.getMethodName();

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
         * @return a map of command line arguments.
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
    }
}
