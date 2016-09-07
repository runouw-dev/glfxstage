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
 * A collection of colors for use in OpenGL applications.
 *
 * @author zmichaels
 * @since 15.06.11
 */
public enum GLColors {
    /**
     * The color red. Hex: 0xFF0000FF
     *
     * @since 15.06.11
     */
    RED(1.0, 0.0, 0.0, 1.0),
    /**
     * The color green. Hex: 0x00FF00FF
     *
     * @since 15.06.11
     */
    GREEN(0.0, 1.0, 0.0, 1.0),
    /**
     * The color blue. Hex: 0x0000FFFF
     *
     * @since 15.06.11
     */
    BLUE(0.0, 0.0, 1.0, 1.0),
    /**
     * The color black. Hex: 0x000000FF
     *
     * @since 15.06.11
     */
    BLACK(0.0, 0.0, 0.0, 1.0),
    /**
     * The color white. Hex: 0xFFFFFFFF
     *
     * @since 15.06.11
     */
    WHITE(1.0, 1.0, 1.0, 1.0),
    /**
     * A mix of black (75%) and white (25%). Hex: 0xCCCCCCFF
     *
     * @since 15.06.11
     */
    LIGHT_GRAY(0.75, 0.75, 0.75, 1.0),
    /**
     * A mix of black (75%) and white (25%). Hex: 0xCCCCCCFF
     *
     * @since 15.06.11
     */
    LIGHT_GREY(0.75, 0.75, 0.75, 1.0),
    /**
     * A mix of black (50%) and white (50%). Hex: 0x777777FF
     *
     * @since 15.06.11
     */
    GRAY(0.5, 0.5, 0.5, 1.0),
    /**
     * A mix of black (50%) and white (50%). Hex: 0x777777FF
     *
     * @since 15.06.11
     */
    GREY(0.5, 0.5, 0.5, 1.0),
    /**
     * A mix of black (25%) and white (75%). Hex: 0x333333FF
     *
     * @since 15.06.11
     */
    DARK_GRAY(0.25, 0.25, 0.25, 1.0),
    /**
     * A mix of black (25%) and white (75%). Hex: 0x333333FF
     *
     * @since 15.06.11
     */
    DARK_GREY(0.25, 0.25, 0.25, 1.0),
    /**
     * A mixture of red and white. Hex: 0xFFADADFF
     *
     * @since 15.06.11
     */
    PINK(1.0, 0.68, 0.68, 1.0),
    /**
     * A mixture of red and yellow. Hex: 0xFFC700FF
     *
     * @since 15.06.11
     */
    ORANGE(1.0, 0.78, 0.0, 1.0),
    /**
     * The color yellow. Hex: 0xFFFF00FF
     *
     * @since 15.06.11
     */
    YELLOW(1.0, 1.0, 0.0, 1.0),
    /**
     * The color magenta. Hex: 0xFF00FFFF
     *
     * @since 15.06.11
     */
    MAGENTA(1.0, 0.0, 1.0, 1.0),
    /**
     * The color cyan. Hex: 0x00FFFFFF
     *
     * @since 15.06.11
     */
    CYAN(0.0, 1.0, 1.0, 1.0);
    
    final GLVec4D color;

    GLColors(double r, double g, double b, double a) {
        this.color = new StaticVec4D(Vectors.DEFAULT_FACTORY, r, g, b, a);
    }

    /**
     * Retrieves the red value.
     *
     * @return the red value
     * @since 15.06.11
     */
    public double r() {
        return this.color.x();
    }

    /**
     * Retrieves the green value.
     *
     * @return the green value.
     * @since 15.06.11
     */
    public double g() {
        return this.color.y();
    }

    /**
     * Retrieves the blue value.
     *
     * @return the blue value.
     * @since 15.06.11
     */
    public double b() {
        return this.color.z();
    }

    /**
     * Retrieves the alpha value.
     *
     * @return the alpha value.
     * @since 15.06.11
     */
    public double a() {
        return this.color.w();
    }

    /**
     * Retrieves a vector representing the color in RGBA format.
     *
     * @return the color in RGBA.
     * @since 15.06.11
     */
    public GLVec4D getRGBA() {
        return this.color.copyTo(Vectors.DEFAULT_FACTORY);
    }

    /**
     * Retrieves a vector representing the color in BGRA format.
     *
     * @return the color in BGRA.
     * @since 15.06.11
     */
    public GLVec4D getBGRA() {
        return GLVec4D.create(this.r(), this.g(), this.b(), this.a());
    }

    /**
     * Retrieves a vector representing the color in ARGB format.
     *
     * @return the color in ARGB.
     * @since 15.06.11
     */
    public GLVec4D getARGB() {
        return GLVec4D.create(this.a(), this.r(), this.g(), this.b());
    }

    /**
     * Retrieves a vector representing the color in ABGR format.
     *
     * @return the color in ABGR.
     * @since 15.06.11
     */
    public GLVec4D getABGR() {
        return GLVec4D.create(this.a(), this.b(), this.g(), this.r());
    }

    /**
     * Retrieves a vector representing the color in RGB format.
     *
     * @return the color in RGB.
     * @since 15.06.11
     */
    public GLVec3D getRGB() {
        return this.color.asGLVec3D();
    }

    /**
     * Retrieves a vector representing the color in BGR format.
     *
     * @return the color in BGR.
     * @since 15.06.11
     */
    public GLVec3D getBGR() {
        return GLVec3D.create(this.b(), this.g(), this.r());
    }
}
