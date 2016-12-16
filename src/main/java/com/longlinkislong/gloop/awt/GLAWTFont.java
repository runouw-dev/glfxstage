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

import com.longlinkislong.gloop.GLException;
import com.longlinkislong.gloop.GLFont;
import com.longlinkislong.gloop.GLFontGlyphSet;
import com.longlinkislong.gloop.GLFontMetrics;
import com.longlinkislong.gloop.GLTask;
import static com.longlinkislong.gloop.GLTextureFormat.GL_BGRA;
import com.longlinkislong.gloop.GLTextureInternalFormat;
import com.longlinkislong.gloop.GLTextureMagFilter;
import com.longlinkislong.gloop.GLTextureMinFilter;
import com.longlinkislong.gloop.GLTextureParameters;
import com.longlinkislong.gloop.GLTextureWrap;
import com.longlinkislong.gloop.GLThread;
import com.longlinkislong.gloop.GLType;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * An implementation of GLFont that derives its texture from the AWT font
 * object.
 *
 * @author zmichaels
 * @since 15.06.11
 */
public class GLAWTFont extends GLFont {

    private static final boolean FONT_TO_FILE = System.getProperty("com.longlinkislong.gloop.GLFont.fontToFile", "0").equals("1");

    private int texWidth;
    private int texHeight;
    private final Font font;
    private static final Map<Font, GLFontMetrics> METRICS_MAP = new HashMap<>();

    /**
     * Constructs a new GLFont object on the default OpenGL thread from an AWT
     * font object
     *
     * @param font the font
     * @since 15.06.11
     */
    public GLAWTFont(final Font font) {
        this(GLThread.getDefaultInstance(), font, GLFontGlyphSet.ASCII);
    }

    /**
     * Constructs a new GLFont object on the specified thread with the AWT font
     * object.
     *
     * @param thread the OpenGL thread
     * @param font the AWT font.
     * @since 15.06.11
     */
    public GLAWTFont(final GLThread thread, final Font font) {
        super(thread, GLFontGlyphSet.ASCII);

        this.font = Objects.requireNonNull(font);
        this.init();
    }

    /**
     * Constructs a new GLFont object on the default OpenGL thread from an AWT
     * font object
     *
     * @param font the font
     * @param supportedGlyphs the set of supported glyphs
     * @since 15.06.11
     */
    public GLAWTFont(final Font font, final GLFontGlyphSet supportedGlyphs) {
        this(GLThread.getDefaultInstance(), font, supportedGlyphs);
    }

    /**
     * Constructs a new GLFont object on the specified thread with the AWT font
     * object.
     *
     * @param thread the OpenGL thread
     * @param font the AWT font.
     * @param supportedGlyphs the set of supported glyphs
     * @since 15.06.11
     */
    public GLAWTFont(final GLThread thread, final Font font, final GLFontGlyphSet supportedGlyphs) {
        super(thread, supportedGlyphs);

        this.font = Objects.requireNonNull(font);
        this.init();
    }

    /**
     * Retrieves the size of texture needed to rasterize the supplied font
     * glyphs.
     *
     * @return the size of the texture. In the ordered pair x,y.
     * @throws GLException if the required texture size is too big.
     * @since 15.06.11
     */
    public int[] getFontTextureSize() throws GLException {
        final Canvas dummy = new Canvas();
        final FontMetrics metrics = dummy.getFontMetrics(font);

        final float maxWidth = metrics.getHeight();
        final float maxHeight = metrics.getHeight();

        final int sqr = (int) Math.sqrt(getSupportedGlyphs().size()) + 1;

        final int requiredWidth = (int) (maxWidth * sqr);
        final int requiredHeight = (int) (maxHeight * sqr);

        if (requiredWidth > 16384 || requiredHeight > 16384) {
            throw new GLException("Font texture is too big!");
        }

        return new int[]{requiredWidth, requiredHeight};
    }

    @Override
    public int getWidth() {
        return this.texWidth;
    }

    @Override
    public int getHeight() {
        return this.texHeight;
    }

    /**
     * Rasterizes the ascii section of an AWT font to a BufferedImage object.
     *
     * @param font the font object to rasterize.
     * @return the rasterized font.
     * @since 15.06.11
     */
    public BufferedImage buildFont(final Font font) {
        final int[] size = getFontTextureSize();
        final int width = size[0];
        final int height = size[1];
        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = img.createGraphics();

        final int sqr = (int) Math.sqrt(getSupportedGlyphs().size()) + 1;

        g2d.setColor(Color.WHITE);
        g2d.setFont(font);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        final float gridH = height / sqr;
        final float gridW = width / sqr;

        int j = 0;
        g2d.translate(0, g2d.getFontMetrics().getAscent());
        for (int i = 0; i < supportedGlyphs.size(); i++) {
            g2d.drawString("" + supportedGlyphs.get(i), (i % sqr) * gridW, 0f);
            j++;
            if (j == sqr) {
                g2d.translate(0, gridH);
                j = 0;
            }
        }

        g2d.dispose();

        if (FONT_TO_FILE) {
            try {
                ImageIO.write(img, "PNG", new File("font.png"));
            } catch (IOException ex) {
                Logger.getLogger(GLAWTFont.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return img;
    }

    @Override
    public final void init() {
        this.newInitTask().glRun(this.getThread());
    }

    public GLTask newInitTask() {
        final BufferedImage img = buildFont(GLAWTFont.this.font);
        this.texWidth = img.getWidth();
        this.texHeight = img.getHeight();
        final int[] pixels = new int[texWidth * texHeight];
        final ByteBuffer pBuf = ByteBuffer.allocateDirect(texWidth * texHeight * 4).order(ByteOrder.nativeOrder());

        img.getRGB(0, 0, texWidth, texHeight, pixels, 0, texWidth);        
        Arrays.stream(pixels).forEach(pBuf::putInt);
        pBuf.flip();        

        return GLTask.join(
                this.texture.new AllocateImage2DTask(1, GLTextureInternalFormat.GL_RGBA8, texWidth, texHeight, GLType.GL_UNSIGNED_BYTE),
                this.texture.new UpdateImage2DTask(0, 0, 0, texWidth, texHeight, GL_BGRA, GLType.GL_UNSIGNED_BYTE, pBuf),
                this.texture.new SetAttributesTask(new GLTextureParameters()
                        .withFilter(GLTextureMinFilter.GL_LINEAR, GLTextureMagFilter.GL_LINEAR)
                        .withWrap(GLTextureWrap.GL_CLAMP_TO_EDGE, GLTextureWrap.GL_CLAMP_TO_EDGE, GLTextureWrap.GL_CLAMP_TO_EDGE))
        );
    }

    @Override
    public GLFontMetrics getMetrics() {
        return getFontMetrics(this.font, this.supportedGlyphs);
    }

    /**
     * Retrieves the metrics object associated with the given AWT Font object.
     *
     * @param font the AWT font object.
     * @param glyps the set of supported glyphs
     * @return the associated metrics object.
     * @since 15.06.11
     */
    public static GLFontMetrics getFontMetrics(final Font font, final GLFontGlyphSet glyps) {
        if (METRICS_MAP.containsKey(font)) {
            return METRICS_MAP.get(font);
        } else {
            final GLFontMetrics metrics = new GLAWTFontMetrics(font, glyps);

            METRICS_MAP.put(font, metrics);

            return metrics;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other instanceof GLAWTFont) {
            return this.font.equals(((GLAWTFont) other).font);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.font);
        return hash;
    }
}
