/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

/**
 * An interface that represents the measurements of a GLFont object.
 *
 * @author zmichaels
 * @since 15.06.18
 */
public interface GLFontMetrics {

    boolean isCharSupported(char c);

    /**
     * Retrieves the height of a character in pixels.
     *
     * @return the height in pixels
     * @since 15.07.01
     */
    float getLineHeight();

    /**
     * Retrieves the x offset of a character in pixels.
     *
     * @param c the character
     * @return the x offset in pixels
     * @since 15.07.01
     */
    float getOffX(char c);

    /**
     * Retrieves the y offset of a character in pixels.
     *
     * @param c the character
     * @return the y offset in pixels
     * @since 15.07.01
     */
    float getOffY(char c);

    /**
     * Retrieves the x advance of a character in pixels.
     *
     * @param c the character
     * @return the y offset in pixels
     * @since 15.07.01
     */
    float getCharAdvancement(char c);

    /**
     * Retrieves the width of a character in pixels.
     *
     * @param c the character
     * @return the width in pixels
     * @since 15.07.01
     */
    float getCharWidth(char c);

    /**
     * Retrieves an estimation on the length of a string in pixels.
     *
     * @param string the string to calculate the length.
     * @return the length of the string in pixels.
     * @since 15.08.24
     */
    default float getStringWidth(final CharSequence string) {
        float width = 0f;

        for (int i = 0; i < string.length(); i++) {
            width += this.getCharWidth(string.charAt(i));
        }

        return width;
    }

    /**
     * Retrieves the width of a character in pixels.
     *
     * @param c the character
     * @return the height in pixels
     * @since 15.07.01
     */
    float getCharHeight(char c);

    float getU0(char c);

    float getU1(char c);

    float getV0(char c);

    float getV1(char c);
}
