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
