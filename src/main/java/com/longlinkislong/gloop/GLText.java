/*
 * Copyright (c) 2015-2016, longlinkislong.com
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

import com.runouw.util.Lazy;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object that represents visible text.
 *
 * @author zmichaels
 * @since 15.06.11
 */
public class GLText extends GLObject implements CharSequence {

    private static final Logger LOGGER = LoggerFactory.getLogger(GLText.class);
    private static Optional<GLProgram> DEFAULT_TEXT_RENDERER = Optional.empty();
    private static final String DEFAULT_SAMPLER_UNAME = "uFont";
    private static final String DEFAULT_PROJECTION_UNAME = "uProj";
    private static final String DEFAULT_TRANSLATION_UNAME = "uTrans";
    private static final GLVertexAttributes ATTRIBS = new GLVertexAttributes();

    static {
        ATTRIBS.setAttribute("vPos", 0);
        ATTRIBS.setAttribute("vUVs", 1);
        ATTRIBS.setAttribute("vCol", 2);
    }

    private String text;
    private final GLVec4D baseColor = GLColors.WHITE.getRGBA().asStaticVec();
    private GLFont font;

    private final Lazy<GLBuffer> vPos = new Lazy<>(() -> new GLBuffer(this.getThread()));
    private final Lazy<GLBuffer> vCol = new Lazy<>(() -> new GLBuffer(this.getThread()));
    private final Lazy<GLBuffer> vUVs = new Lazy<>(() -> new GLBuffer(this.getThread()));
    private final Lazy<GLVertexArray> vao = new Lazy<>(() -> {
        final GLVertexArray out = new GLVertexArray(this.getThread());

        out.attachBuffer(
                ATTRIBS.getLocation("vPos"), vPos.get(),
                GLVertexAttributeType.GL_FLOAT, GLVertexAttributeSize.VEC2);

        out.attachBuffer(
        ATTRIBS.getLocation("vCol"), vCol.get(),
                GLVertexAttributeType.GL_FLOAT, GLVertexAttributeSize.VEC4);

        out.attachBuffer(
        ATTRIBS.getLocation("vUVs"), vUVs.get(),
                GLVertexAttributeType.GL_FLOAT, GLVertexAttributeSize.VEC2);
       
        return out;
    });
    private int length;

    /**
     * Returns the time since the object was last used (in nanoseconds).
     *
     * @return the time since last used.
     * @since 16.09.05
     */
    @Override
    public long getTimeSinceLastUsed() {
        return System.nanoTime() - this.lastUsedTime;
    }

    /**
     * Constructs a new GLText object from the supplied font and text.
     *
     * @param font the font to use
     * @param seq the text to display
     * @since 15.06.11
     */
    public GLText(final GLFont font, final CharSequence seq) {
        this(GLThread.getDefaultInstance(), font, seq);
    }

    /**
     * Constructs a new GLText object from the supplied font and text.
     *
     * @param thread the thread to create the object on.
     * @param font the font to use
     * @param seq the text to display.
     * @since 15.06.11
     */
    public GLText(final GLThread thread, final GLFont font, final CharSequence seq) {
        super(thread);

        this.font = Objects.requireNonNull(font);

        setText(seq.toString());
        LOGGER.trace("Constructed GLText object on thread: {}", thread);
    }

    @Override
    public int length() {
        return this.text.length();
    }

    @Override
    public char charAt(int index) {
        return this.text.charAt(index);
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return new GLText(this.font, this.text.subSequence(start, end));
    }

    private static GLProgram newDefaultTextRenderer() {
        final GLProgram program = new GLProgram();

        program.setName("GLText.defaultTextRenderer");
        program.setVertexAttributes(ATTRIBS);

        final String vertexShader;
        final String fragmentShader;

        switch (GLWindow.CLIENT_API) {
            case OPENGLES:
                vertexShader = "legacy_text.vert";
                fragmentShader = "legacy_text.frag";
                break;
            case OPENGL:
                vertexShader = "default_text.vsh";
                fragmentShader = "default_text.fsh";
                break;
            default:
                throw new UnsupportedOperationException("Unsupported client api: " + GLWindow.CLIENT_API);
        }

        try (
                final InputStream inVsh = GLText.class.getResourceAsStream(vertexShader);
                final InputStream inFsh = GLText.class.getResourceAsStream(fragmentShader)) {

            final GLShader vsh = new GLShader(GLShaderType.GL_VERTEX_SHADER, GLTools.readAll(inVsh));
            final GLShader fsh = new GLShader(GLShaderType.GL_FRAGMENT_SHADER, GLTools.readAll(inFsh));

            program.linkShaders(vsh, fsh);
            vsh.delete();
            fsh.delete();
        } catch (IOException ex) {
            throw new GLException("Unable to parse default text shader!", ex);
        }

        DEFAULT_TEXT_RENDERER = Optional.of(program);

        return program;
    }

    private static GLProgram getDefaultTextRenderer() {
        return DEFAULT_TEXT_RENDERER.orElseGet(GLText::newDefaultTextRenderer);
    }

    @Override
    public String toString() {
        return this.text;
    }

    /**
     * Overwrites the text that the GLText object is displaying.
     *
     * @param seq the text to display
     * @since 15.06.11
     */
    public void setText(final CharSequence seq) {
        this.newSetTextTask(seq).glRun(this.getThread());
    }

    /**
     * Overwrites the text that the GLText object is displaying.
     *
     * @param baseColor the base color of the text (when there are no color
     * tags).
     * @param seq the text to display
     * @since 15.06.11
     */
    public void setText(GLVec4D baseColor, final CharSequence seq) {
        this.baseColor.set(baseColor);
        this.newSetTextTask(seq).glRun(this.getThread());
    }

    /**
     * Creates a new GLTask that updates the text object.
     *
     * @param seq the text to display
     * @return the GLTask
     * @since 15.06.11
     */
    public GLTask newSetTextTask(final CharSequence seq) {
        this.text = seq.toString();

        final int len = this.text.length();

        final GLVec2D offset = GLVec2D.create();
        final Deque<GLVec4D> colorStack = new ArrayDeque<>(4);
        final List<GLVec2> pos = new ArrayList<>(len);
        final List<GLVec2> uvs = new ArrayList<>(len);
        final List<GLVec4> col = new ArrayList<>(len);
        final GLFontMetrics metrics = GLText.this.font.getMetrics();

        GLVec4D color = baseColor;

        for (int i = 0; i < text.length(); i++) {
            final String tagStart = text.substring(i);

            //<editor-fold defaultstate="collapsed" desc="color tag parser">
            if (tagStart.startsWith("<")) {
                final int tagSize = tagStart.indexOf(">");

                if (tagSize > 0) {
                    final String tag = tagStart.substring(1, tagSize);

                    switch (tag.toLowerCase()) {
                        case "red":
                            colorStack.push(color);
                            color = GLColors.RED.color;
                            break;
                        case "blue":
                            colorStack.push(color);
                            color = GLColors.BLUE.color;
                            break;
                        case "green":
                            colorStack.push(color);
                            color = GLColors.GREEN.color;
                            break;
                        case "black":
                            colorStack.push(color);
                            color = GLColors.BLACK.color;
                            break;
                        case "white":
                            colorStack.push(color);
                            color = GLColors.WHITE.color;
                            break;
                        case "lightgray":
                        case "lightgrey":
                            colorStack.push(color);
                            color = GLColors.LIGHT_GRAY.color;
                            break;
                        case "gray":
                        case "grey":
                            colorStack.push(color);
                            color = GLColors.GRAY.color;
                            break;
                        case "darkgray":
                        case "darkgrey":
                            colorStack.push(color);
                            color = GLColors.DARK_GRAY.color;
                            break;
                        case "pink":
                            colorStack.push(color);
                            color = GLColors.PINK.color;
                            break;
                        case "orange":
                            colorStack.push(color);
                            color = GLColors.PINK.color;
                            break;
                        case "yellow":
                            colorStack.push(color);
                            color = GLColors.YELLOW.color;
                            break;
                        case "magenta":
                            colorStack.push(color);
                            color = GLColors.MAGENTA.color;
                            break;
                        case "cyan":
                            colorStack.push(color);
                            color = GLColors.CYAN.color;
                            break;
                        case "/red":
                        case "/green":
                        case "/blue":
                        case "/black":
                        case "/white":
                        case "/gray":
                        case "/grey":
                        case "/lightgray":
                        case "/lightgrey":
                        case "/darkgray":
                        case "/darkgrey":
                        case "/pink":
                        case "/orange":
                        case "/yellow":
                        case "/magenta":
                        case "/cyan":
                        case "/color":
                            color = colorStack.pop();
                            break;
                        default:
                            if (tag.toLowerCase().startsWith("color")) {
                                final String colorCode = tag.split("=")[1];

                                try {
                                    if (colorCode.startsWith("#") && (colorCode.length() == 4 || colorCode.length() == 5)) {
                                        // shorthand notation
                                        final int colorVal = Integer.decode(colorCode);

                                        int alpha = (colorVal & 0xF000) >> 12;
                                        final int red = (colorVal & 0x0F00) >> 8;
                                        final int green = (colorVal & 0xF0) >> 4;
                                        final int blue = colorVal & 0x000F;

                                        if (alpha == 0) {
                                            alpha = 15;
                                        }

                                        colorStack.push(color);
                                        color = GLVec4D.create(
                                                red / 15.0,
                                                green / 15.0,
                                                blue / 15.0,
                                                alpha / 15.0)
                                                .asStaticVec();
                                    } else {
                                        // argb notation
                                        final int colorVal = Integer.decode(colorCode);

                                        int alpha = colorVal & 0xFF000000 >> 24;
                                        final int red = (colorVal & 0x00FF0000) >> 16;
                                        final int green = (colorVal & 0x0000FF00) >> 8;
                                        final int blue = colorVal & 0x000000FF;

                                        if (alpha == 0) {
                                            alpha = 255;
                                        }

                                        colorStack.push(color);
                                        color = GLVec4D.create(
                                                red / 255.0,
                                                green / 255.0,
                                                blue / 255.0,
                                                alpha / 255.0)
                                                .asStaticVec();
                                    }
                                } catch (NumberFormatException err) {
                                    // text can be user input, so don't crash, instead print error
                                    LOGGER.error("Error decoding number in tag " + tag, err);
                                }

                            }
                            break;
                    }

                    i += tagSize;
                }

                continue;
            }
            //</editor-fold>

            final char c = text.charAt(i);
            final double advance = metrics.getCharAdvancement(c);
            final double lineHeight = metrics.getLineHeight();

            if (c == '\t') {
                offset.set(0, offset.x() + 4.0 * advance);
                continue;
            } else if (c == '\r') {
                offset.set(Vectors.X, 0.0);
                continue;
            } else if (c == '\n') {
                offset.set(Vectors.X, 0.0);
                offset.set(Vectors.Y, offset.y() + lineHeight);
                continue;
            } else if (c == ' ') {
                offset.set(Vectors.X, offset.x() + advance);
                continue;
            } else if (!metrics.isCharSupported(c)) {
                // skip supported text characters
                continue;
            }

            final double width = metrics.getCharWidth(c);
            final double height = metrics.getCharHeight(c);
            final double offX = metrics.getOffX(c);
            final double offY = metrics.getOffY(c);

            final double x0 = offset.x() + offX;
            final double x1 = offset.x() + offX + width;
            final double y0 = offset.y() + offY;
            final double y1 = offset.y() + offY + height;

            final double u0 = metrics.getU0(c);
            final double u1 = metrics.getU1(c);
            final double v0 = metrics.getV0(c);
            final double v1 = metrics.getV1(c);

            pos.add(GLVec2D.create(x0, y0));
            pos.add(GLVec2D.create(x0, y1));
            pos.add(GLVec2D.create(x1, y1));
            pos.add(GLVec2D.create(x1, y1));
            pos.add(GLVec2D.create(x1, y0));
            pos.add(GLVec2D.create(x0, y0));

            uvs.add(GLVec2D.create(u0, v0));
            uvs.add(GLVec2D.create(u0, v1));
            uvs.add(GLVec2D.create(u1, v1));
            uvs.add(GLVec2D.create(u1, v1));
            uvs.add(GLVec2D.create(u1, v0));
            uvs.add(GLVec2D.create(u0, v0));

            for (int j = 0; j < 6; j++) {
                col.add(color);
            }

            offset.set(0, offset.x() + advance);
        }        

        this.length = pos.size() / 6;

        return GLTask.create(() -> {            
            vPos.get().upload(GLTools.wrapVec2F(pos));
            vCol.get().upload(GLTools.wrapVec4F(col));                        
            vUVs.get().upload(GLTools.wrapVec2F(uvs));
            
            this.updateTimeUsed();
        });
    }

    /**
     * Draws all of the text. The font texture is bound to target 0.
     *
     * @param pr the projection matrix to use.
     * @param tr the transformation matrix to use
     * @since 15.06.11
     */
    public void draw(final GLMat4 pr, final GLMat4 tr) {
        this.draw(pr, tr, 0, 1.0f);
    }

    /**
     * Draws a percentage of the text.
     *
     * @param pr the projection matrix to use
     * @param tr the transformation matrix to use
     * @param target the texture target to bind the font to
     * @param percent the percentage of the text to draw.
     * @since 15.06.11
     */
    public void draw(
            final GLMat4 pr, final GLMat4 tr,
            final int target, final float percent) {

        this.newDrawTask(pr, tr, target, percent).glRun(this.getThread());
    }

    /**
     * Constructs a new draw task for the GLText object. All of the text will be
     * drawn.
     *
     * @param pr the projection matrix to use
     * @param tr the transformation matrix to use
     * @param target the texture target for the font.
     * @return the GLTask object
     * @throws GLException if the texture target is invalid or the draw percent
     * is out of bounds.
     * @since 15.06.12
     */
    public GLTask newDrawTask(
            final GLMat4 pr, final GLMat4 tr, final int target) throws GLException {

        return newDrawTask(pr, tr, target, 1f);
    }

    /**
     * Constructs a new draw task for the GLText object.
     *
     * @param pr the projection matrix to use
     * @param tr the transformation matrix to use
     * @param target the texture target for the font.
     * @param percent the percentage of the text to draw.
     * @return the GLTask object
     * @throws GLException if the texture target is invalid or the draw percent
     * is out of bounds.
     * @since 15.06.12
     */
    public GLTask newDrawTask(
            final GLMat4 pr, final GLMat4 tr,
            final int target, final float percent) throws GLException {

        if (target < 0) {
            throw new GLException("Texture target for the font cannot be less than 0!");
        } else if (percent < 0 || percent > 1f) {
            throw new GLException("Draw percent out of range! Draw percent must be on bounds [0.0,1.0]!");
        }

        final GLProgram prog = getDefaultTextRenderer();
        final String uFont = DEFAULT_SAMPLER_UNAME;
        final String uPr = DEFAULT_PROJECTION_UNAME;
        final String uTr = DEFAULT_TRANSLATION_UNAME;
        final int verts = (int) (this.length * percent) * 6;

        if (verts == 0) {
            // NO-OP
            return GLTask.NO_OP;
        } else {
            return GLTask.create(() -> {
                this.font.bind(target);

                prog.use();
                prog.setUniformMatrixF(uPr, pr.asGLMat4F());
                prog.setUniformMatrixF(uTr, tr.asGLMat4F());
                prog.setUniformI(uFont, target);

                this.vao.get().drawArrays(GLDrawMode.GL_TRIANGLES, 0, verts);
                this.updateTimeUsed();
            });
        }
    }

    /**
     * Deletes the GLText object. The GLText object has to have been initialized
     * to be deleted.
     *
     * @since 15.06.11
     */
    public void delete() {
        newDeleteTask().glRun(this.getThread());
    }

    /**
     * Constructs a new
     *
     * @return the delete task
     * @since 15.06.11
     */
    public GLTask newDeleteTask() {
        return GLTask.create(() -> {
            this.vao.get().delete();
            this.vPos.get().delete();
            this.vCol.get().delete();
            this.vUVs.get().delete();
            LOGGER.trace("Deleting GLText object!");
            this.lastUsedTime = 0L;
        });
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        } else if (other instanceof GLText) {
            final GLText oText = (GLText) other;

            return (this.font.equals(oText.font) && this.text.equals(oText.text));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.text);
        hash = 11 * hash + Objects.hashCode(this.font);
        return hash;
    }
}
