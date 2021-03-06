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
package com.longlinkislong.gloop.awt;

import com.longlinkislong.gloop.GLFontGlyphSet;
import com.longlinkislong.gloop.GLFontMetrics;
import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.util.Objects;

/**
 * A font metrics object for GLOOP that wraps the AWT FontMetrics object
 *
 * @author zmichaels
 * @since 15.06.11
 */
public class GLAWTFontMetrics implements GLFontMetrics {

    private final Font font;
    private final GLFontGlyphSet supportedGlyphs;
    private final int texWidth;
    private final int texHeight;
    private final int sqr;
    
    private final FontMetrics metrics;
    
    private GlyphVector getGlyph(char c){
        return font.createGlyphVector(new FontRenderContext(new AffineTransform(), true, true), "" + c);
    }

    /**
     * Constructs a new GLAWTFontMetrics from the AWTFontMetrics object.
     * @param font the font to use
     * @param supportedGlyphs the set of supported glyphs.
     * @since 15.06.11
     */
    public GLAWTFontMetrics(final Font font, final GLFontGlyphSet supportedGlyphs) {
        this.font = Objects.requireNonNull(font);
        this.supportedGlyphs = supportedGlyphs;
        
        final Canvas dummy = new Canvas();
        this.metrics = dummy.getFontMetrics(font);
        
        final float maxWidth = metrics.getHeight();
        final float maxHeight = metrics.getHeight();
        
        sqr = (int)Math.sqrt(supportedGlyphs.size()) + 1;

        final int requiredWidth = (int) (maxWidth * sqr);
        final int requiredHeight = (int) (maxHeight * sqr);
        
        this.texWidth = requiredWidth;
        this.texHeight = requiredHeight;
    }
    
    @Override
    public boolean isCharSupported(char c) {
        return supportedGlyphs.contains(c);
    }

    @Override
    public float getCharWidth(char c) {
        // NOTE, this is because the uvs tile the glyphs this way
        return getLineHeight();
    }
    
    @Override
    public float getCharHeight(char c) {
        // // NOTE, this is because the uvs tile the glyphs this way
        return getLineHeight();
    }

    @Override
    public float getLineHeight() {
        return this.metrics.getHeight();
    }

    @Override
    public float getOffX(char c) {
        //return (float) getGlyph(c).getVisualBounds().getMinX() + getLineHeight()/2;
        return 0;
    }

    @Override
    public float getOffY(char c) {
        //return (float) getGlyph(c).getVisualBounds().getMinY() + getLineHeight()/2;
        return 0;
    }

    @Override
    public float getCharAdvancement(char c) {
        return (int) getGlyph(c).getGlyphMetrics(0).getAdvance();
    }

    @Override
    public float getU0(char c) {                
        final int index = supportedGlyphs.indexOf(c).orElseGet(() -> supportedGlyphs.indexOf('!').orElse(0));
        final float left = (index % sqr) * getLineHeight();
        
        return left / texWidth;
    }

    @Override
    public float getU1(char c) {
        return getU0(c) + getLineHeight() / texWidth;
    }

    @Override
    public float getV0(char c) {
        final int index = supportedGlyphs.indexOf('!').orElseGet(() -> supportedGlyphs.indexOf('!').orElse(0));
        final float top = (index / sqr) * getLineHeight();
        
        return top / texHeight;
    }

    @Override
    public float getV1(char c) {
        return getV0(c) + getLineHeight() / texWidth;
    }

}
