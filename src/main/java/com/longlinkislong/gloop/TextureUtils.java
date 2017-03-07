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

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.lwjgl.system.MemoryUtil;

/**
 * A collection of utilities that may aid in texture use or creation.
 *
 * @author zmichaels
 * @since 16.09.06
 */
public final class TextureUtils {

    /**
     * An RGBA color format that is 2 bits per channel (8 bits total).
     *
     * @since 16.09.06
     */
    public static final GLTextureInternalFormat COLOR_DEPTH_8BIT = GLTextureInternalFormat.GL_RGBA2;
    /**
     * An RGBA color format that is 4 bits per channel (16 bits total).
     *
     * @since 16.09.06
     */
    public static final GLTextureInternalFormat COLOR_DEPTH_16BIT = GLTextureInternalFormat.GL_RGBA4;
    /**
     * An RGBA color format that is 8 bits per channel (32 bits total).
     *
     * @since 16.09.06
     */
    public static final GLTextureInternalFormat COLOR_DEPTH_32BIT = GLTextureInternalFormat.GL_RGBA8;
    /**
     * An RGBA color format that is 12 bits per channel (48 bits total).
     *
     * @since 16.09.06
     */
    public static final GLTextureInternalFormat COLOR_DEPTH_48BIT = GLTextureInternalFormat.GL_RGBA12;
    /**
     * An RGBA color format that is 16 bits per channel (stored as float; 64
     * bits total).
     *
     * @since 16.09.06
     */
    public static final GLTextureInternalFormat COLOR_DEPTH_64BIT = GLTextureInternalFormat.GL_RGBA16F;
    /**
     * An RGBA color format that is 32 bits per channel (stored as float; 128
     * bits total).
     *
     * @since 16.09.06
     */
    public static final GLTextureInternalFormat COLOR_DEPTH_128BIT = GLTextureInternalFormat.GL_RGBA32F;

    /**
     * Checks if the GLTextureParameters specify the need for mipmaps.
     *
     * @param params the parameters to check.
     * @return true if minFilter is: [NEAREST | LINEAR]_MIPMAP_[NEAREST |
     * LINEAR]
     * @since 16.09.06
     */
    public static boolean needsMipmaps(final GLTextureParameters params) {
        switch (params.minFilter) {
            case GL_NEAREST_MIPMAP_NEAREST:
            case GL_LINEAR_MIPMAP_NEAREST:
            case GL_NEAREST_MIPMAP_LINEAR:
            case GL_LINEAR_MIPMAP_LINEAR:
                return true;
            default:
                return false;
        }
    }

    /**
     * Copies pixels from a texture to a BufferedImage.
     *
     * @param texture the texture to copy from.
     * @param mipmapLevel the mipmap level to copy from. Assumes mipmaps are
     * power of 2 apart.
     * @return the image read from the texture.
     * @since 16.09.06
     */
    public static BufferedImage textureToImage(final GLTexture texture, final int mipmapLevel) {
        final int width = texture.getWidth() >> mipmapLevel;
        final int height = texture.getHeight() >> mipmapLevel;
        final BufferedImage bImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final int[] writePixels = new int[width * height];
        final ByteBuffer readPixels = MemoryUtil.memAlloc(writePixels.length * Integer.BYTES);

        texture.downloadImage(height, GLTextureFormat.GL_RGBA, GLType.GL_UNSIGNED_BYTE, readPixels);

        readPixels.asIntBuffer().get(writePixels);
        readPixels.rewind();

        GLTask.create(() -> MemoryUtil.memFree(readPixels)).glRun(texture.getThread());

        bImg.setRGB(0, 0, width, height, writePixels, 0, width);

        return bImg;
    }

    /**
     * Copies the pixels from the specified GLFramebuffer to a BufferedImage
     *
     * @param fb the framebuffer to copy from
     * @param x the x-offset of the copy rectangle.
     * @param y the y-offset of the copy rectangle.
     * @param width the width of the copy rectangle.
     * @param height the height of the copy rectangle.
     * @return the BufferedImage
     * @since 16.09.06
     */
    public static BufferedImage framebufferToImage(final GLFramebuffer fb, final int x, final int y, final int width, final int height) {
        final BufferedImage bImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final int[] writePixels = new int[width * height];
        final ByteBuffer readPixels = MemoryUtil.memAlloc(writePixels.length * Integer.BYTES);

        fb.readPixels(x, y, width, height, GLTextureFormat.GL_RGBA, GLType.GL_UNSIGNED_BYTE, readPixels);

        readPixels.asIntBuffer().get(writePixels);
        readPixels.rewind();

        // clear the temp buffer on the same thread as GLFramebuffer.readPixels
        GLTask.create(() -> MemoryUtil.memFree(readPixels)).glRun(fb.getThread());

        bImg.setRGB(0, 0, width, height, writePixels, 0, width);

        return bImg;
    }

    /**
     * Constructs a new GLTexture from the BufferedImage. The default texture
     * parameters are used.
     *
     * @param img the BufferedImage.
     * @return the GLTexture.
     * @since 16.09.06
     */
    public static GLTexture newTexture(final BufferedImage img) {
        return newTexture(GLThread.getAny(), img, GLTextureParameters.DEFAULT_PARAMETERS);
    }

    /**
     * Constructs a new GLTexture from the BufferedImage.
     *
     * @param img the BufferedImage.
     * @param params the GLTextureParameters.
     * @return the GLTexture.
     * @since 16.09.06
     */
    public static GLTexture newTexture(final BufferedImage img, final GLTextureParameters params) {
        return newTexture(GLThread.getAny(), img, params);
    }

    /**
     * Constructs a new GLTexture from the BufferedImage.
     *
     * @param thread the OpenGL thread to use.
     * @param img the BufferedImage.
     * @return the new GLTexture.
     * @since 16.09.06
     */
    public static GLTexture newTexture(final GLThread thread, final BufferedImage img) {
        return newTexture(thread, img, GLTextureParameters.DEFAULT_PARAMETERS);
    }

    /**
     * Constructs a new GLTexture from the BufferedImage.
     *
     * @param thread the OpenGL thread to use.
     * @param img the BufferedImage.
     * @param params the GLTextureParameters.
     * @return the new GLTexture.
     * @since 16.09.06
     */
    public static GLTexture newTexture(final GLThread thread, final BufferedImage img, final GLTextureParameters params) {
        final int width = img.getWidth();
        final int height = img.getHeight();
        final int pixelCount = width * height;
        final int bytesNeeded = pixelCount * Integer.BYTES;
        final int[] pixels = new int[pixelCount];
        final ByteBuffer data = MemoryUtil.memAlloc(bytesNeeded);

        img.getRGB(0, 0, width, height, pixels, 0, width);

        data.asIntBuffer().put(pixels);
        data.position(0).limit(bytesNeeded);

        final GLTexture out;
        if (needsMipmaps(params)) {
            final int mipmaps = GLTools.recommendedMipmaps(width, height);

            out = new GLTexture(thread).allocate(mipmaps, GLTextureInternalFormat.GL_RGBA8, width, height)
                    .setAttributes(params)
                    .updateImage(0, 0, 0, GLTextureFormat.GL_BGRA, GLType.GL_UNSIGNED_BYTE, data)
                    .generateMipmap();
        } else {
            out = new GLTexture(thread)
                    .allocate(1, GLTextureInternalFormat.GL_RGBA8, width, height)
                    .setAttributes(params)
                    .updateImage(0, 0, 0, GLTextureFormat.GL_BGRA, GLType.GL_UNSIGNED_BYTE, data);
        }

        // delete the temp buffer AFTER the update. This must happen on the same thread as the update.
        GLTask.create(() -> MemoryUtil.memFree(data)).glRun(thread);
        return out;
    }

    private static final ExecutorService WORKERS = Executors.newCachedThreadPool(task -> {
        final Thread t = new Thread(task);

        t.setDaemon(true);
        t.setName("TextureUtils - Worker Thread");
        return t;
    });    
    
    public static Future<GLTexture> newTextureAsync(final GLThread thread, final GLTextureParameters params, final BufferedImage img) {
        final GLTexture out = new GLTexture(thread);
        final int width = img.getWidth();
        final int height = img.getHeight();
        final int pixelCount = width * height;
        final int bytesNeeded = pixelCount * Integer.BYTES;
        final GLBuffer pbo = new GLBuffer(thread);
        
        out.allocate(1, GLTextureInternalFormat.GL_RGBA8, width, height);
        pbo.allocate(bytesNeeded, GLBufferUsage.GL_STREAM_DRAW);
        
        final ByteBuffer data = pbo.map(0, bytesNeeded, GLBufferAccess.GL_MAP_WRITE, GLBufferAccess.GL_MAP_INVALIDATE_BUFFER);
        final Future<?> uploadTask = WORKERS.submit(() -> {
            final int[] pixels = new int[pixelCount];
            
            img.getRGB(0, 0, width, height, pixels, 0, width);
            data.asIntBuffer().put(pixels);
            data.position(0).limit(bytesNeeded);
        });
        
        return new Future<GLTexture>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                final boolean res = uploadTask.cancel(mayInterruptIfRunning);
                
                pbo.unmap();
                pbo.delete();
                out.delete();
                
                return res;
            }

            @Override
            public boolean isCancelled() {
                return uploadTask.isCancelled();
            }

            @Override
            public boolean isDone() {
                return uploadTask.isDone();
            }

            @Override
            public GLTexture get() throws InterruptedException, ExecutionException {
                // sync with upload thread
                uploadTask.get();
                pbo.unmap();
                out.updateImage(0, 0, 0, width, height, GLTextureFormat.GL_BGRA, GLType.GL_UNSIGNED_BYTE, pbo);
                pbo.delete();
                
                return out;
            }

            @Override
            public GLTexture get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                uploadTask.get(timeout, unit);
                pbo.unmap();
                out.updateImage(0, 0, 0, width, height, GLTextureFormat.GL_BGRA, GLType.GL_UNSIGNED_BYTE, pbo);
                pbo.delete();
                
                return out;
            }
        };
    }

    private TextureUtils() {
    }
}
