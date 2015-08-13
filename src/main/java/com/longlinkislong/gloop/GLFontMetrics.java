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
    
    public boolean isCharSupported(char c);

    /**
     * Retrieves the height of a character in pixels.
     *
     * @return the height in pixels
     * @since 15.07.01
     */
    public float getLineHeight();
    
    /**
     * Retrieves the x offset of a character in pixels.
     *
     * @param c the character
     * @return the x offset in pixels
     * @since 15.07.01
     */
    public float getOffX(char c);
    
    /**
     * Retrieves the y offset of a character in pixels.
     *
     * @param c the character
     * @return the y offset in pixels
     * @since 15.07.01
     */
    public float getOffY(char c);
    
    /**
     * Retrieves the x advance of a character in pixels.
     *
     * @param c the character
     * @return the y offset in pixels
     * @since 15.07.01
     */
    public float getCharAdvancement(char c);
    
    /**
     * Retrieves the width of a character in pixels.
     *
     * @param c the character
     * @return the width in pixels
     * @since 15.07.01
     */
    public float getCharWidth(char c);
    
    /**
     * Retrieves the width of a character in pixels.
     *
     * @param c the character
     * @return the height in pixels
     * @since 15.07.01
     */
    public float getCharHeight(char c);

    
    public float getU0(char c);
    public float getU1(char c);
    public float getV0(char c);
    public float getV1(char c);
}
