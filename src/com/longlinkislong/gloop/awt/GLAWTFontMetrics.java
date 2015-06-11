/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop.awt;

import com.longlinkislong.gloop.GLFontMetrics;
import java.awt.FontMetrics;
import java.util.Objects;

/**
 * A font metrics object for GLOOP that wraps the AWT FontMetrics object
 *
 * @author zmichaels
 * @since 15.06.11
 */
public class GLAWTFontMetrics implements GLFontMetrics {

    private final FontMetrics metrics;

    /**
     * Constructs a new GLAWTFontMetrics from the AWTFontMetrics object.
     * @param metrics the metrics to use
     * @since 15.06.11
     */
    public GLAWTFontMetrics(final FontMetrics metrics) {
        this.metrics = Objects.requireNonNull(metrics);
    }

    @Override
    public float getCharWidth(char c) {
        return this.metrics.charWidth(c);
    }

    @Override
    public float getMaxHeight() {
        return this.metrics.getMaxAscent() + this.metrics.getMaxDescent();
    }

    @Override
    public float getMaxWidth() {
        return this.metrics.getMaxAdvance();
    }

}
