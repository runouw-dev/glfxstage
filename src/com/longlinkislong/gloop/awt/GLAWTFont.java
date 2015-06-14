/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop.awt;

import com.longlinkislong.gloop.GLException;
import com.longlinkislong.gloop.GLFont;
import com.longlinkislong.gloop.GLFontMetrics;
import com.longlinkislong.gloop.GLTask;
import com.longlinkislong.gloop.GLTexture;
import com.longlinkislong.gloop.GLTextureFormat;
import com.longlinkislong.gloop.GLTextureInternalFormat;
import com.longlinkislong.gloop.GLThread;
import com.longlinkislong.gloop.GLTools;
import com.longlinkislong.gloop.GLType;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
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
    private int width;
    private int height;
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
        this(GLThread.getDefaultInstance(), font);
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
        super(thread);

        this.font = Objects.requireNonNull(font);
        this.init();
    }

    /**
     * Retrieves the size of texture needed to rasterize the supplied font
     * glyphs.
     *
     * @param font the font to rasterize.
     * @return the size of the texture. In the ordered pair x,y.
     * @throws GLException if the required texture size is too big.
     * @since 15.06.11
     */
    public static int[] getFontTextureSize(final Font font) throws GLException {
        final GLFontMetrics metrics = getFontMetrics(font);
        final float maxWidth = metrics.getMaxWidth();
        final float maxHeight = metrics.getMaxHeight();

        final int requiredWidth = GLTools.getNearestPowerOf2((int) (maxWidth * 10f));
        final int requiredHeight = GLTools.getNearestPowerOf2((int) (maxHeight * 10));

        if (requiredWidth > 16384 || requiredHeight > 16384) {
            throw new GLException("Font texture is too big!");
        }

        return new int[]{requiredWidth, requiredHeight};
    }

    @Override
    public int getWidth() {
        return this.width;
    }
    
    @Override
    public int getHeight() {
        return this.height;
    }
    
    /**
     * Rasterizes the ascii section of an AWT font to a BufferedImage object.
     *
     * @param font the font object to rasterize.
     * @return the rasterized font.
     * @since 15.06.11
     */
    public static BufferedImage buildFont(final Font font) {
        final int[] size = getFontTextureSize(font);
        final int width = size[0];
        final int height = size[1];
        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = img.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.setFont(font);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        final float gridH = height / 10f;

        int j = 0;
        g2d.translate(0, g2d.getFontMetrics().getAscent());
        for (char i = ' '; i <= '~'; i++) {
            g2d.drawString("" + i, ((i - ' ') % 10) / 10f * width, 0f);
            j++;
            if (j == 10) {
                g2d.translate(0, gridH);
                j = 0;
            }
        }

        g2d.dispose();

        try {
            ImageIO.write(img, "PNG", new File("font.png"));
        } catch (IOException ex) {
            Logger.getLogger(GLAWTFont.class.getName()).log(Level.SEVERE, null, ex);
        }
        return img;
    }    

    @Override
    public final void init() {
        this.newInitTask().glRun(this.getThread());
    }

    public GLTask newInitTask() {
        final BufferedImage img = GLAWTFont.buildFont(GLAWTFont.this.font);
        this.width = img.getWidth();
        this.height = img.getHeight();
        final int[] pixels = new int[width * height];
        final ByteBuffer pBuf = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());

        img.getRGB(0, 0, width, height, pixels, 0, width);

        Arrays.stream(pixels).forEach(pBuf::putInt);

        pBuf.flip();

        return this.texture.new SetImage2DTask(
                GLTexture.GENERATE_MIPMAP,
                GLTextureInternalFormat.GL_RGBA8, GLTextureFormat.GL_BGRA,
                width, height,
                GLType.GL_UNSIGNED_BYTE, pBuf);
    }

    @Override
    public GLFontMetrics getMetrics() {
        return getFontMetrics(this.font);
    }    

    /**
     * Retrieves the metrics object associated with the given AWT Font object.
     *
     * @param font the AWT font object.
     * @return the associated metrics object.
     * @since 15.06.11
     */
    public static GLFontMetrics getFontMetrics(final Font font) {
        if (METRICS_MAP.containsKey(font)) {
            return METRICS_MAP.get(font);
        } else {
            final Canvas dummy = new Canvas();
            final GLFontMetrics metrics = new GLAWTFontMetrics(dummy.getFontMetrics(font));

            METRICS_MAP.put(font, metrics);

            return metrics;
        }
    }
}
