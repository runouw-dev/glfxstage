/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

/**
 * An interface that represents the measurements of a GLFont object.
 * @author zmichaels
 * @since 15.06.18
 */
public interface GLFontMetrics {

    /**
     * Retrieves the width of a character in pixels.
     *
     * @param c the character
     * @return the width in pixels
     * @since 15.06.18
     */
    public float getCharWidth(char c);

    /**
     * Retrieves the max height of a character in pixels. Max height is defined
     * as the combination of max ascent and max descent for a character.
     *
     * @return the max height
     * @since 15.06.18
     */
    public float getMaxHeight();

    /**
     * Retrieves the max width of a character in pixels.
     * @return the max width
     * @since 15.06.18
     */
    public float getMaxWidth();
}
