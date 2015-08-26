/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Minimum required support for text input fields.
 *
 * @author zmichaels
 * @since 15.08.21
 */
public abstract class GLUITextField extends GLUIComponent {

    private static final Set<Character> VALID_CHARS;

    static {
        VALID_CHARS = new HashSet<>();

        // ascii text characters
        registerValidCharRange(' ', '~');
    }

    /**
     * Registers all of the characters that fall within the range as valid. By
     * default, ASCII text between ' ' and '~' is supported.
     *
     * @param startInclusive the first supported character in a range.
     * @param endInclusive the last supported character in a range.
     * @since 15.08.21
     */
    public static void registerValidCharRange(final char startInclusive, final char endInclusive) {
        for (char c = startInclusive; c <= endInclusive; c++) {
            VALID_CHARS.add(c);
        }
    }

    private final GLMat4F transformation = GLMat4F.create().asStaticMat();
    private final Deque<Character> input = new LinkedList<>();
    private Supplier<Character> keyPoll;
    private Consumer<String> onComplete;
    private volatile String currentText = "";

    @Override
    public GLMat4F getTransformation() {
        return this.transformation.copyTo(Matrices.DEFAULT_FACTORY);
    }
    
    /**
     * Constructs a new GLUITextField on the default OpenGL thread.
     *     
     * @param transformation the transformation matrix for the text field.
     * @since 15.08.21
     */
    public GLUITextField(final GLMat4F transformation) {
        this(GLThread.getDefaultInstance(), transformation);
    }

    /**
     * Constructs a new GLUITextField on the specified OpenGL thread.
     *
     * @param thread the OpenGL thread.     
     * @param transformation the transformation matrix for the text field.    
     * @since 15.08.21
     */
    public GLUITextField(final GLThread thread, final GLMat4F transformation) {
        super(thread);

        this.transformation.set(transformation);
    }

    /**
     * Sets the text value.
     *
     * @param text the string to set the text to.
     * @since 15.08.21
     */
    public void setText(final CharSequence text) {
        final String sText = text.toString();

        this.input.clear();

        for (int i = 0; i < sText.length(); i++) {
            this.input.push(sText.charAt(i));
        }

        this.rebuildText();
    }

    /**
     * Sets the provider for new character values. The supplier is allowed to
     * return null for when the input should be skipped.
     *
     * @param input the input supplier.
     * @since 15.08.21
     */
    public void setInputSupplier(final Supplier<Character> input) {
        this.keyPoll = input;
    }

    /**
     * Sets the callback for when the user types 'enter'.
     *
     * @param onCompleteCallback the callback
     * @since 15.08.21
     */
    public void setOnComplete(final Consumer<String> onCompleteCallback) {
        this.onComplete = onCompleteCallback;
    }

    /**
     * Retrieves the current text value.
     *
     * @return the current text value.
     * @since 15.08.21
     */
    public String getText() {
        return this.currentText;
    }

    /**
     * Method that actually draws the contents of the text field.
     *
     * @param mvp the transformation matrix for the absolute location of the
     * text field.
     * @param text string representing the currently entered text.
     * @since 15.08.21
     */
    protected abstract void drawTextField(final GLMat4F mvp, final String text);

    private void rebuildText() {
        final StringBuilder out = new StringBuilder();

        this.input.forEach(out::append);
        this.currentText = out.toString();
    }

    @Override
    protected void drawComponent(GLMat4F projection, GLMat4F translation) {
        if (this.isSelected() && this.keyPoll != null) {
            final Character c = this.keyPoll.get();
           
            if (c != null) {                
                if (/* backspace */c == 8) {
                    this.input.pollLast();
                    this.rebuildText();
                } else if (/* return */c == '\r' || c == '\n') {
                    this.onComplete.accept(this.currentText);
                } else if (VALID_CHARS.contains(c)) {
                    this.input.offerLast(c);
                    this.rebuildText();
                } else {
                    System.out.printf("Invalid character: [value=%d]\n", c);
                }
            }
        }
        
        if(!this.isVisible()) {
            return;
        }

        final GLMat4F tr = this.transformation.multiply(translation);
        final GLMat4F mvp = tr.multiply(projection);

        this.drawTextField(mvp, this.currentText);
    }

}
