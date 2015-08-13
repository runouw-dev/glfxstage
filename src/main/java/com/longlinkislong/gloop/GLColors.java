/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

/**
 * A collection of colors for use in OpenGL applications.
 * @author zmichaels
 * @since 15.06.11
 */
public enum GLColors {
    /**
     * The color red.
     * Hex: 0xFF0000FF      
     * @since 15.06.11
     */
    RED(1f, 0f, 0f, 1f),
    /**
     * The color green.
     * Hex: 0x00FF00FF
     * @since 15.06.11
     */
    GREEN(0f, 1f, 0f, 1f),
    /**
     * The color blue.
     * Hex: 0x0000FFFF
     * @since 15.06.11
     */
    BLUE(0f, 0f, 1f, 1f),
    /**
     * The color black.
     * Hex: 0x000000FF
     * @since 15.06.11
     */
    BLACK(0f, 0f, 0f, 1f),
    /**
     * The color white.
     * Hex: 0xFFFFFFFF
     * @since 15.06.11
     */
    WHITE(1f, 1f, 1f, 1f),
    /**
     * A mix of black (75%) and white (25%).
     * Hex: 0xCCCCCCFF
     * @since 15.06.11
     */
    LIGHT_GRAY(0.75f, 0.75f, 0.75f, 1f),    
    /**
     * A mix of black (75%) and white (25%).
     * Hex: 0xCCCCCCFF
     * @since 15.06.11
     */
    LIGHT_GREY(0.75f, 0.75f, 0.75f, 1f),
    /**
     * A mix of black (50%) and white (50%).
     * Hex: 0x777777FF
     * @since 15.06.11
     */
    GRAY(0.5f, 0.5f, 0.5f, 1f),
    /**
     * A mix of black (50%) and white (50%).
     * Hex: 0x777777FF
     * @since 15.06.11
     */
    GREY(0.5f, 0.5f, 0.5f, 1f),
    /**
     * A mix of black (25%) and white (75%).
     * Hex: 0x333333FF
     * @since 15.06.11
     */
    DARK_GRAY(0.25f, 0.25f, 0.25f, 1f),
    /**
     * A mix of black (25%) and white (75%).
     * Hex: 0x333333FF
     * @since 15.06.11
     */
    DARK_GREY(0.25f, 0.25f, 0.25f, 1f),
    /**
     * A mixture of red and white.
     * Hex: 0xFFADADFF
     * @since 15.06.11
     */
    PINK(1f, 0.68f, 0.68f, 1f),
    /**
     * A mixture of red and yellow.
     * Hex: 0xFFC700FF
     * @since 15.06.11
     */
    ORANGE(1f, 0.78f, 0f, 1f),
    /**
     * The color yellow.
     * Hex: 0xFFFF00FF
     * @since 15.06.11
     */
    YELLOW(1f, 1f, 0f, 1f),
    /**
     * The color magenta.
     * Hex: 0xFF00FFFF
     * @since 15.06.11
     */
    MAGENTA(1f, 0f, 1f, 1f),
    /**
     * The color cyan.
     * Hex: 0x00FFFFFF
     * @since 15.06.11
     */
    CYAN(0f, 1f, 1f, 1f);

    final GLVec4F color;

    GLColors(float r, float g, float b, float a) {
        this.color = new StaticVec4F(Vectors.DEFAULT_FACTORY, r, g, b, a);
    }

    /**
     * Retrieves the red value.
     * @return the red value
     * @since 15.06.11
     */
    public float r() {
        return this.color.x();
    }

    /**
     * Retrieves the green value.     
     * @return the green value.
     * @since 15.06.11
     */
    public float g() {
        return this.color.y();
    }

    /**
     * Retrieves the blue value.
     * @return the blue value.
     * @since 15.06.11
     */
    public float b() {
        return this.color.z();
    }

    /**
     * Retrieves the alpha value.
     * @return the alpha value.
     * @since 15.06.11
     */
    public float a() {
        return this.color.w();
    }

    /**
     * Retrieves a vector representing the color in RGBA format.
     * @return the color in RGBA.
     * @since 15.06.11
     */
    public GLVec4F getRGBA() {
        return this.color.copyTo(Vectors.DEFAULT_FACTORY);
    }

    /**
     * Retrieves a vector representing the color in BGRA format.     
     * @return the color in BGRA.
     * @since 15.06.11
     */
    public GLVec4F getBGRA() {
        return GLVec4F.create(this.r(), this.g(), this.b(), this.a());
    }

    /**
     * Retrieves a vector representing the color in ARGB format.
     * @return the color in ARGB.
     * @since 15.06.11
     */
    public GLVec4F getARGB() {
        return GLVec4F.create(this.a(), this.r(), this.g(), this.b());
    }
    
    /**
     * Retrieves a vector representing the color in ABGR format.
     * @return the color in ABGR.
     * @since 15.06.11
     */
    public GLVec4F getABGR() {
        return GLVec4F.create(this.a(), this.b(), this.g(), this.r());
    }
    
    /**
     * Retrieves a vector representing the color in RGB format.
     * @return the color in RGB.
     * @since 15.06.11
     */
    public GLVec3F getRGB() {
        return this.color.asGLVec3F();
    }
    
    /**
     * Retrieves a vector representing the color in BGR format.
     * @return the color in BGR.
     * @since 15.06.11
     */
    public GLVec3F getBGR() {
        return GLVec3F.create(this.b(), this.g(), this.r());
    }
}
