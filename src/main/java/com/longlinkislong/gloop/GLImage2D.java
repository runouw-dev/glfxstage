/*
 * Copyright (c) 2015 zmichaels.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    zmichaels - initial API and implementation and/or initial documentation
 */
package com.longlinkislong.gloop;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * The GLImage2D class provides a simple 2D image container intended for use as
 * intermediate 2D texture data. All pixels are stored as ints as 8_8_8_8.
 * However, GLImage2D doesn't care what format the pixels are in (such as ARGB,
 * BRGA, RGBA, ets). This decision is made because texture swizzling can be done
 * on upload to the GLTexture.
 *
 * @author zmichaels
 * @since 15.02.02
 */
public class GLImage2D implements Closeable {

    private static int[] TEMP = null;    
    protected final ByteBuffer data;
    private final int[] size;
    private Optional<Closeable> resource = Optional.empty();

    /**
     * Constructs a new GLImage with the specified width and height.
     *
     * @param width the width of the image. Must be greater than 1.
     * @param height the height of the image. Must be greater than 1.
     * @since 15.02.02
     */
    public GLImage2D(final int width, final int height) {
        this.size = new int[]{width, height};
        this.data = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
    }

    /**
     * Wraps the supplied data buffer as a GLImage2D
     *
     * @param width the width of the image
     * @param height the height of the image
     * @param data the data buffer
     * @since 15.04.03
     */
    public GLImage2D(final int width, final int height, final ByteBuffer data) {
        this.size = new int[]{width, height};
        this.data = data;
    }

    /**
     * Opens a RandomAccessFile as a GLImage2D. The entire file is then memory
     * mapped.
     *
     * @param width the width of the image
     * @param height the height of the image
     * @param data the image file
     * @param mode the mode to open the image
     * @throws IOException if the file could not be opened
     * @since 15.04.03
     */
    public GLImage2D(
            final int width, final int height,
            final RandomAccessFile data, final MapMode mode) throws IOException {

        this.size = new int[]{width, height};
        this.data = data.getChannel().map(mode, 0, width * height * 4);
        this.resource = Optional.of(data);
    }

    /**
     * Retrieves the total size of the image. This is the number of bytes that
     * the image occupies.
     *
     * @return size of image in bytes.
     * @since 15.02.02
     */
    public final int getSize() {
        return this.size[0] * this.size[1] * 4;
    }

    /**
     * Retrieves the width of the image measured in pixels.
     *
     * @return the width of the image in pixels.
     * @since 15.02.02
     */
    public final int getWidth() {
        return this.size[0];
    }

    /**
     * Retrieves the height of the image measured in pixels.
     *
     * @return the height of the image in pixels
     * @since 15.02.02
     */
    public final int getHeight() {
        return this.size[1];
    }

    /**
     * Calculates the offset index for the specified position
     *
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return The corresponding index location of the pixel.
     * @since 15.02.02
     */
    protected final int indexOf(final int x, final int y) {
        return x + y * this.getWidth();
    }

    /**
     * Copies a rectangle of pixel information from the image to an int array.
     *
     * @param x the x coordinate of the upper left pixel location
     * @param y the y coordinate of the upper left pixel location
     * @param w the width of the pixel rectangle
     * @param h the height of the pixel rectangle
     * @param data the array to store the data in. If null is supplied an int
     * array large enough to hold the rectangle will be created.
     * @return The reference to an int array containing the rectangle of pixel
     * information.
     * @since 15.02.02
     */
    public final int[] getData(
            final int x, final int y,
            final int w, final int h,
            final int[] data) {

        this.data.mark();
        
        final int[] out = data == null ? new int[w * h] : data;
        final IntBuffer src = this.data.asIntBuffer();
        final int scanlineStride = this.getWidth();
        int yOff = indexOf(x, y);
        int off = 0;

        for (int yStart = 0; yStart < h; yStart++) {
            src.position(yOff);
            src.get(out, off, w);
            off += w;
            yOff += scanlineStride;
        }
        
        this.data.reset();

        return out;
    }

    /**
     * Copies a rectangle of pixel information from the image to a float array.
     *
     * @param x the x coordinate of the upper left pixel location
     * @param y the y coordinate of the upper left pixel location
     * @param w the width of the pixel rectangle
     * @param h the height of the pixel rectangle
     * @param data the array to store the data in. If null is supplied a float
     * array large enough to hold the rectangle will be created.
     * @return The reference to a float array containing the rectangle of pixel
     * information.
     * @since 15.02.02
     */
    public final float[] getData(
            final int x, final int y,
            final int w, final int h,
            final float[] data) {
        
        this.data.mark();

        final float[] out = data == null ? new float[w * h] : data;
        final FloatBuffer src = this.data.asFloatBuffer();
        final int scanlineStride = this.getWidth();
        int yOff = indexOf(x, y);
        int off = 0;

        for (int yStart = 0; yStart < h; yStart++) {
            src.position(yOff);
            src.get(out, off, w);
            off += w;
            yOff += scanlineStride;
        }
        
        this.data.reset();

        return out;
    }

    /**
     * Writes an int array of data to a rectangle of pixels.
     *
     * @param x the x coordinate of the upper left pixel location.
     * @param y the y coordinate of the upper left pixel location.
     * @param w the width of the pixel rectangle.
     * @param h the height of the pixel rectangle.
     * @param data the array to read data from.
     * @since 15.02.02
     */
    public final void setData(
            final int x, final int y,
            final int w, final int h,
            final int[] data) {
        
        this.data.mark();

        final IntBuffer dst = this.data.asIntBuffer();
        final int scanlineSize = this.getWidth();
        int yOff = indexOf(x, y);
        int off = 0;

        for (int yStart = 0; yStart < h; yStart++) {
            dst.position(yOff);
            dst.put(data, off, w);
            off += w;
            yOff += scanlineSize;
        }
        
        this.data.reset();
    }

    /**
     * Writes a segment of an int array of data to a rectangle of pixels.
     *
     * @param dstX the x coordinate of the upper left pixel to write
     * @param dstY the y coordinate of the upper left pixel to write
     * @param dstW the width of the pixel rectangle
     * @param dstH the height of the pixel rectangle
     * @param srcX the x coordinate of the upper left pixel to read
     * @param srcY the y coordinate of the upper left pixel to read
     * @param data the array to read data from.
     * @since 15.02.05
     */
    public final void setData(
            final int dstX, final int dstY,
            final int dstW, final int dstH,
            final int srcX, final int srcY,
            final int[] data) {
        
        this.data.mark();

        final IntBuffer dst = this.data.asIntBuffer();
        final int scanlineSize = this.getWidth();
        int dstYOff = indexOf(dstX, dstY);
        int dstXOff = srcX + srcY * dstW;

        for (int yStart = 0; yStart < dstH; yStart++) {
            dst.position(dstYOff);
            dst.put(data, dstXOff, dstW);
            dstXOff += dstW;
            dstYOff += scanlineSize;
        }
        
        this.data.reset();
    }

    /**
     * Writes a float array of data to a rectangle of pixels.
     *
     * @param x the x coordinate of the upper left pixel location.
     * @param y the y coordinate of the upper left pixel location.
     * @param w the width of the pixel rectangle.
     * @param h the height of the pixel rectangle.
     * @param data the array to read data from.
     * @since 15.02.02
     */
    public final void setData(
            final int x, final int y,
            final int w, final int h,
            final float[] data) {
        
        this.data.mark();

        final FloatBuffer dst = this.data.asFloatBuffer();
        final int scanlineSize = this.getWidth();
        int yOff = indexOf(x, y);
        int off = 0;

        for (int yStart = 0; yStart < h; yStart++) {
            dst.position(yOff);
            dst.put(data, off, w);
            off += w;
            yOff += scanlineSize;
        }
        
        this.data.reset();
    }

    /**
     * Writes a section of a float array of data to another rectangle.
     *
     * @param dstX the x coordinate of the upper left pixel to write.
     * @param dstY the y coordinate of the upper left pixel to write.
     * @param dstW the width of the pixel rectangle.
     * @param dstH the height of the pixel rectangle.
     * @param srcX the x coordinate of the upper left pixel to read.
     * @param srcY the y coordinate of the upper left pixel to read.
     * @param data the array to read data from.
     * @since 15.02.05
     */
    public final void setData(
            final int dstX, final int dstY,
            final int dstW, final int dstH,
            final int srcX, final int srcY,
            final float[] data) {
        
        this.data.mark();

        final FloatBuffer dst = this.data.asFloatBuffer();
        final int scanlineSize = this.getWidth();
        int yOff = indexOf(dstX, dstY);
        int xOff = srcX + srcY * dstW;

        for (int yStart = 0; yStart < dstH; yStart++) {
            dst.position(yOff);
            dst.put(data, xOff, dstW);
            xOff += dstW;
            yOff += scanlineSize;
        }
        
        this.data.reset();
    }

    /**
     * Writes a GLImage to another GLImage.
     *
     * @param x the x coordinate of the upper left pixel location.
     * @param y the y coordinate of the upper left pixel location.
     * @param w the width of the pixel rectangle.
     * @param h the height of the pixel rectangle.
     * @param data the array to read data from
     * @since 15.02.02
     */
    public final void setData(
            final int x, final int y,
            final int w, final int h,
            final GLImage2D data) {

        setData(x, y, w, h, 0, 0, data);
    }

    /**
     * Writes a section of a GLImage to another GLImage.
     *
     * @param dstX the x coordinate of the upper left pixel to write
     * @param dstY the y coordinate of the upper left pixel to write
     * @param dstW the width of the pixel rectangle
     * @param dstH the height of the pixel rectangle
     * @param srcX the x coordinate of the upper left pixel to read
     * @param srcY the y coordinate of the upper left pixel to read
     * @param data the array to read data from.
     * @since 15.02.05
     */
    public final void setData(
            final int dstX, final int dstY,
            final int dstW, final int dstH,
            final int srcX, final int srcY,
            final GLImage2D data) {

        
        data.data.mark();
        this.data.mark();
        
        final IntBuffer src = data.data.asIntBuffer();
        final IntBuffer dst = this.data.asIntBuffer();
        final int[] tmp;
        final int scanlineSize = dstW;

        if (TEMP == null || TEMP.length < scanlineSize) {
            TEMP = new int[scanlineSize];
        }

        tmp = TEMP;

        for (int yStart = 0; yStart < dstH; yStart++) {
            int destOff = indexOf(dstX, dstY + yStart);
            int srcOff = data.indexOf(srcX, srcY + yStart);

            dst.position(destOff);
            src.position(srcOff);
            src.get(tmp, 0, scanlineSize);
            dst.put(tmp, 0, scanlineSize);
        }
        
        data.data.reset();
        this.data.reset();
    }

    /**
     * Retrieves the data of this GLImage as a ByteBuffer object.
     *
     * @return A ByteBuffer holding the same data as this object.
     * @since 15.02.02
     */
    public final ByteBuffer asByteBuffer() {        
        return this.data.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
    }

    /**
     * Retrieves the data of this GLImage as a read-only ShortBuffer object.
     *
     * @return A read-only ShortBuffer holding the same data as this object.
     * @since 15.02.02
     */
    public final ShortBuffer asShortBuffer() {
        return this.asByteBuffer().asShortBuffer();
    }

    /**
     * Retrieves the data of this GLImage as a read-only IntBuffer object.
     *
     * @return A read-only IntBuffer holding the same data as this object.
     */
    public final IntBuffer asIntBuffer() {        
        return this.asByteBuffer().asIntBuffer();
    }

    /**
     * Retrieves the data of this GLImage as a read-only FloatBuffer object.
     *
     * @return A read-only IntBuffer holding the same data as this object.
     * @since 15.02.02
     */
    public final FloatBuffer asFloatBuffer() {        
        return this.asByteBuffer().asFloatBuffer();
    }            
    
    /**
     * Mirrors the image along the x-axis.
     * @return the mirrored image
     * @since 15.04.30
     */
    public GLImage2D asMirrorX(){
        final GLImage2D img = new GLImage2D(getWidth(), getHeight());        
        
        img.data.mark();
        this.data.mark();
        
        final IntBuffer src = this.data.asIntBuffer();
        final IntBuffer dest = img.data.asIntBuffer();
        
        for(int y=0;y<getHeight();y++){
            for(int x=0;x<getWidth();x++){
                int mx = getWidth() - x - 1;
                
                dest.put(src.get(y*getWidth() + mx));
            }
        }
        
        img.data.reset();
        this.data.reset();
        
        return img;
    }
    
    /**
     * Mirrors the image along the y-axis
     * @return the mirrored image
     * @since 15.04.30
     */
    public GLImage2D asMirrorY(){
        final GLImage2D img = new GLImage2D(getWidth(), getHeight());        
        
        img.data.mark();
        this.data.mark();
        
        final IntBuffer src = this.data.asIntBuffer();
        final IntBuffer dest = img.data.asIntBuffer();
        
        for(int y=0;y<getHeight();y++){
            for(int x=0;x<getWidth();x++){
                int my = getHeight() - y - 1;
                
                // TODO: this could be sped up by copying the whole stride at a time
                src.put(dest.get(my*getWidth() + x));
            }
        }
        
        img.data.reset();
        this.data.reset();
        
        return img;
    }

    @Override
    public void close() throws IOException {
        if(this.resource.isPresent()) {
            this.resource.get().close();
            this.resource = Optional.empty();
        }
    }

    @Override
    public boolean equals(Object other) {
        if(this == other) {
            return true;
        } else if(other instanceof GLImage2D) {
            final GLImage2D oImg = (GLImage2D) other;
            
            return Arrays.equals(this.size, oImg.size) && this.data.equals(oImg.data);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.data);
        hash = 43 * hash + Arrays.hashCode(this.size);
        return hash;
    }
}
