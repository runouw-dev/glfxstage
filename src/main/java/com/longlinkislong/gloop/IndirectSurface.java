/*
 * Copyright (c) 2016, longlinkislong.com
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

import static com.longlinkislong.gloop.GLFramebufferMode.GL_COLOR_BUFFER_BIT;
import com.runouw.util.Lazy;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * IndirectSurface is a container object for a Framebuffer and two Renderbuffers
 * which should behave similar to the default Framebuffer. Its intended purpose
 * is to act as either an intermediate rendering target or an offscreen
 * rendering target.
 *
 * @author zmichaels
 * @since 16.09.06
 */
public final class IndirectSurface {

    /**
     * The texture format used for the color renderbuffer.
     *
     * @since 16.09.06
     */
    public final GLTextureInternalFormat colorFormat;
    /**
     * The texture format used for the depth-stencil renderbuffer.
     *
     * @since 16.09.06
     */
    public final GLTextureInternalFormat depthStencilFormat;
    /**
     * The width of the IndirectSurface.
     *
     * @since 16.09.06
     */
    public final int width;
    /**
     * The height of the IndirectSurface.
     *
     * @since 16.09.06
     */
    public final int height;

    private final Lazy<GLRenderbuffer> color;
    private final Lazy<GLRenderbuffer> depthStencil;
    private final Lazy<GLFramebuffer> framebuffer;
    private GLClear clear;
    private final GLViewport viewport;

    /**
     * The default color format used for the color renderbuffer. Evaluates as
     * RGBA8.
     *
     * @since 16.09.06
     */
    public static final GLTextureInternalFormat DEFAULT_COLOR_FORMAT = TextureUtils.COLOR_DEPTH_32BIT;
    /**
     * The default depth-stencil format used for the depth-stencil renderbuffer.
     *
     * @since 16.09.06
     */
    public static final GLTextureInternalFormat DEFAULT_DEPTH_STENCIL_FORMAT = GLTextureInternalFormat.GL_DEPTH24_STENCIL8;

    /**
     * Constructs an IndirectSurface object without specifying the OpenGL
     * thread. The default color format is used for the color renderbuffer and
     * the default depth-stencil format is used for the depth-stencil
     * renderbuffer.
     *
     * @param width the width of the IndirectSurface.
     * @param height the height of the IndirectSurface.
     * @since 16.09.06
     */
    public IndirectSurface(final int width, final int height) {
        this(width, height, DEFAULT_COLOR_FORMAT, DEFAULT_DEPTH_STENCIL_FORMAT);
    }

    /**
     * Constructs an IndirectSurface object without specifying the OpenGL thread
     * used.
     *
     * @param width the width of the IndirectSurface.
     * @param height the height of the IndirectSurface.
     * @param colorFormat the color format for the color renderbuffer.
     * @param depthStencilFormat the depth-stencil format for the depth-stencil
     * renderbuffer.
     * @since 16.09.06
     */
    public IndirectSurface(
            final int width, final int height,
            final GLTextureInternalFormat colorFormat, final GLTextureInternalFormat depthStencilFormat) {

        this.width = width;
        this.height = height;
        this.colorFormat = Objects.requireNonNull(colorFormat);
        this.depthStencilFormat = Objects.requireNonNull(depthStencilFormat);

        this.color = new Lazy<>(() -> new GLRenderbuffer(colorFormat, width, height));
        this.depthStencil = new Lazy<>(() -> new GLRenderbuffer(depthStencilFormat, width, height));

        this.framebuffer = new Lazy<>(() -> {
            final GLFramebuffer out = new GLFramebuffer();

            out.addRenderbufferAttachment("color", this.color.get());
            out.addRenderbufferAttachment("depthStencil", this.depthStencil.get());

            return out;
        });

        this.clear = new GLClear()
                .withClearBits(GLFramebufferMode.GL_COLOR_BUFFER_BIT, GLFramebufferMode.GL_DEPTH_BUFFER_BIT, GLFramebufferMode.GL_STENCIL_BUFFER_BIT)
                .withClearColor(0f, 0f, 0f, 1f)
                .withClearDepth(1.0);

        this.viewport = new GLViewport(0, 0, width, height);

    }

    /**
     * Constructs an IndirectSurface object.
     *
     * @param thread the OpenGL thread to use.
     * @param width the width of the IndirectSurface.
     * @param height the height of the IndirectSurface.
     * @param colorFormat the color format for the color renderbuffer.
     * @param depthStencilFormat the depth-stencil format for the depth-stencil
     * renderbuffer.
     * @since 16.09.06
     */
    public IndirectSurface(final GLThread thread,
            final int width, final int height,
            final GLTextureInternalFormat colorFormat,
            final GLTextureInternalFormat depthStencilFormat) {

        this.width = width;
        this.height = height;
        this.colorFormat = Objects.requireNonNull(colorFormat);
        this.depthStencilFormat = Objects.requireNonNull(depthStencilFormat);

        this.color = new Lazy<>(() -> new GLRenderbuffer(thread, colorFormat, width, height));
        this.depthStencil = new Lazy<>(() -> new GLRenderbuffer(thread, depthStencilFormat, width, height));

        this.framebuffer = new Lazy<>(() -> {
            final GLFramebuffer out = new GLFramebuffer(thread);

            out.addRenderbufferAttachment("color", this.color.get());
            out.addRenderbufferAttachment("depthStencil", this.depthStencil.get());

            return out;
        });

        this.clear = new GLClear(thread)
                .withClearBits(GLFramebufferMode.GL_COLOR_BUFFER_BIT, GLFramebufferMode.GL_DEPTH_BUFFER_BIT, GLFramebufferMode.GL_STENCIL_BUFFER_BIT)
                .withClearColor(0f, 0f, 0f, 1f)
                .withClearDepth(1.0);

        this.viewport = new GLViewport(thread, 0, 0, width, height);
    }

    /**
     * Sets the clear color for the IndirectSurface.
     *
     * @param r the red value.
     * @param g the green value.
     * @param b the blue value.
     * @param a the alpha value.
     * @return self reference.
     * @since 16.09.06
     */
    public IndirectSurface setClearColor(final float r, final float g, final float b, final float a) {
        this.clear = clear.withClearColor(r, g, b, a);
        return this;
    }

    /**
     * Sets the clear depth for the IndirectSurface.
     *
     * @param depth the depth value.
     * @return self reference.
     * @since 16.09.06
     */
    public IndirectSurface setClearDepth(final double depth) {
        this.clear = clear.withClearDepth(depth);
        return this;
    }

    /**
     * Renders the task to the OffscreenSurface.
     *
     * @param task the task to render to the OffscreenSurface.
     * @since 16.09.06
     */
    public void render(final GLTask task) {
        this.bind();
        task.glRun(this.framebuffer.get().getThread());
        this.unbind();
    }

    /**
     * Pushes the current bound framebuffer then binds and clears the
     * OffscreenSurface framebuffer.
     *
     * @since 16.09.06
     */
    public void bind() {
        final GLFramebuffer fb = this.framebuffer.get();
        final GLThread thread = fb.getThread();

        thread.pushFramebufferBind();
        thread.pushViewport();

        fb.bind();
        viewport.applyViewport();
        clear.clear();
    }

    /**
     * Restores the previous Framebuffer object.
     *
     * @since 16.09.06
     */
    public void unbind() {
        final GLThread thread = this.framebuffer.get().getThread();

        thread.popViewport();
        thread.popFramebufferBind();
    }

    /**
     * Blits the IndirectSurface to a Framebuffer. Only the color renderbuffer
     * will be copied. The surface will be scaled using NEAREST interpolation.
     *
     * @param dst the Framebuffer to copy to.
     * @param dstX0 x0 of the destination rectangle.
     * @param dstY0 y0 of the destination rectangle.
     * @param dstX1 x1 of the destination rectangle.
     * @param dstY1 y1 of the destination rectangle.
     * @since 16.09.06
     */
    public void blit(
            final GLFramebuffer dst,
            final int dstX0, final int dstY0,
            final int dstX1, final int dstY1) {

        blit(
                dst,
                0, 0, this.width, this.height,
                dstX0, dstY0, dstX1, dstY1);
    }

    /**
     * Blits the IndirectSurface to a Framebuffer. The surface will be scaled
     * using NEAREST interpolation.
     *
     * @param dst the Framebuffer to copy to.
     * @param dstX0 x0 of the destination rectangle.
     * @param dstY0 y0 of the destination rectangle.
     * @param dstX1 x1 of the destination rectangle.
     * @param dstY1 y1 of the destination rectangle.
     * @param mask the renderbuffers to select.
     * @since 16.09.06
     */
    public void blit(
            final GLFramebuffer dst,
            final int dstX0, final int dstY0,
            final int dstX1, final int dstY1,
            final Set<GLFramebufferMode> mask) {

        blit(dst, 0, 0, this.width, this.height, dstX0, dstY0, dstX1, dstY1, mask, GLTextureMagFilter.GL_NEAREST);
    }

    /**
     * Blits the IndirectSurface to a Framebuffer.
     * @param dst the Framebuffer to copy to.
     * @param dstX0 x0 of the destination rectangle.
     * @param dstY0 y0 of the destination rectangle.
     * @param dstX1 x1 of the destination rectangle.
     * @param dstY1 y1 of the destination rectangle.
     * @param mask the renderbuffers to copy.
     * @param filter the scaling mode.
     * @since 16.09.06
     */
    public void blit(
            final GLFramebuffer dst,
            final int dstX0, final int dstY0,
            final int dstX1, final int dstY1,
            final Set<GLFramebufferMode> mask,
            final GLTextureMagFilter filter) {

        blit(dst, 0, 0, this.width, this.height, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    /**
     * Blits the IndirectSurface to a Framebuffer. Only the color renderbuffer
     * will be copied. The surface will be scaled using NEAREST interpolation
     *
     * @param dst the Framebuffer to copy to.
     * @param srcX0 x0 of the copy rectangle.
     * @param srcY0 y0 of the copy rectangle.
     * @param srcX1 x1 of the copy rectangle.
     * @param srcY1 y1 of the copy rectangle.
     * @param dstX0 x0 of the destination rectangle.
     * @param dstY0 y0 of the destination rectangle.
     * @param dstX1 x1 of the destination rectangle.
     * @param dstY1 y1 of the destination rectangle.
     * @since 16.09.06
     */
    public void blit(final GLFramebuffer dst,
            final int srcX0, final int srcY0, final int srcX1, final int srcY1,
            final int dstX0, final int dstY0, final int dstX1, final int dstY1) {

        blit(dst, srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, EnumSet.of(GL_COLOR_BUFFER_BIT));
    }

    /**
     * Blits the IndirectSurface to a Framebuffer. The surface will be scaled
     * using NEAREST interpolation
     *
     * @param dst the Framebuffer to copy to.
     * @param srcX0 x0 of the copy rectangle.
     * @param srcY0 y0 of the copy rectangle.
     * @param srcX1 x1 of the copy rectangle.
     * @param srcY1 y1 of the copy rectangle.
     * @param dstX0 x0 of the destination rectangle.
     * @param dstY0 y0 of the destination rectangle.
     * @param dstX1 x1 of the destination rectangle.
     * @param dstY1 y1 of the destination rectangle.
     * @param mask the buffers to copy.
     * @since 16.09.06
     */
    public void blit(
            final GLFramebuffer dst,
            final int srcX0, final int srcY0, final int srcX1, final int srcY1,
            final int dstX0, final int dstY0, final int dstX1, final int dstY1,
            final Set<GLFramebufferMode> mask) {

        blit(dst, srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, GLTextureMagFilter.GL_LINEAR);
    }

    /**
     * Blits the IndirectSurface to a Framebuffer.
     *
     * @param dst the Framebuffer to copy to.
     * @param srcX0 x0 of the copy rectangle.
     * @param srcY0 y0 of the copy rectangle.
     * @param srcX1 x1 of the copy rectangle.
     * @param srcY1 y1 of the copy rectangle.
     * @param dstX0 x0 of the destination rectangle.
     * @param dstY0 y0 of the destination rectangle.
     * @param dstX1 x1 of the destination rectangle.
     * @param dstY1 y1 of the destination rectangle.
     * @param mask the buffers to copy.
     * @param filter the filtering used for scaling.
     * @since 16.09.06
     */
    public void blit(
            final GLFramebuffer dst,
            final int srcX0, final int srcY0, final int srcX1, final int srcY1,
            final int dstX0, final int dstY0, final int dstX1, final int dstY1,
            final Set<GLFramebufferMode> mask,
            final GLTextureMagFilter filter) {

        GLFramebuffer.blit(this.framebuffer.get(), dst, srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    /**
     * Copies all of the pixels from the OffscreenSurface to a BufferedImage.
     *
     * @return the copied pixels as a BufferedImage.
     * @since 16.09.06
     */
    public BufferedImage toImage() {
        return this.toImage(0, 0, this.width, this.height);
    }

    /**
     * Copies the pixels from the OffscreenSurface to a BufferedImage.
     *
     * @param x the x-offset of the copy rectangle.
     * @param y the y-offset of the copy rectangle.
     * @param width the width of the copy rectangle.
     * @param height the height of the copy rectangle.
     * @return the copied pixels as a BufferedImage.
     * @since 16.09.06
     */
    public BufferedImage toImage(final int x, final int y, final int width, final int height) {
        if (x < 0 || y < 0 || width < 0 || height < 0) {
            throw new IllegalArgumentException("Cannot read negative pixels!");
        } else if ((width + x) > this.width || (height + y) > this.height) {
            throw new IndexOutOfBoundsException("Cannot read rectangle larger than framebuffer!");
        } else {
            return TextureUtils.framebufferToImage(this.framebuffer.get(), x, y, width, height);
        }
    }

    /**
     * Deletes all resources required by the OffscreenSurface.
     *
     * @since 16.09.06
     */
    public void delete() {
        this.framebuffer.ifInitialized(GLFramebuffer::delete);
        this.depthStencil.ifInitialized(GLRenderbuffer::delete);
        this.color.ifInitialized(GLRenderbuffer::delete);
    }
}
