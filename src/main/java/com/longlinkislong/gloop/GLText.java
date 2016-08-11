/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
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
    private static final GLVertexAttributes DEFAULT_ATTRIBUTES = new GLVertexAttributes();

    static {
        DEFAULT_ATTRIBUTES.setAttribute("vPos", 0);
        DEFAULT_ATTRIBUTES.setAttribute("vUVs", 1);
        DEFAULT_ATTRIBUTES.setAttribute("vCol", 2);
    }

    private String text;
    private final GLVec4D baseColor = GLColors.WHITE.getRGBA().asStaticVec();
    private Optional<String> uSampler = Optional.empty();
    private Optional<String> uProj = Optional.empty();
    private Optional<String> uTrans = Optional.empty();
    private GLFont font;

    private GLBuffer vPos;
    private GLBuffer vCol;
    private GLBuffer vUVs;
    private GLVertexArray vao;
    private Optional<GLProgram> program = Optional.empty();
    private Optional<GLVertexAttributes> vAttribs = Optional.empty();
    private int length;

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

        this.vPos = new GLBuffer(thread);
        this.vPos.setName("GLText.vPos");
        this.vCol = new GLBuffer(thread);
        this.vCol.setName("GLText.vCol");
        this.vUVs = new GLBuffer(thread);
        this.vUVs.setName("GLText.vUVs");
        this.vao = new GLVertexArray(thread);
        this.vao.setName("GLText.vao");

        setText(seq.toString());
        LOGGER.trace("Constructed GLText object on thread: {}", thread);
    }

    /**
     * Initializes the GLText object. This is called automatically by the
     * constructor. A GLText object can be reinitialized if it has been deleted.
     *
     * @since 15.06.11
     */
    public final void init() {
        this.newInitTask().glRun(this.getThread());
    }

    /**
     * Constructs a new GLTask that initializes all of the internal GLObjects.
     *
     * @return the GLTask
     * @since 15.06.12
     */
    public final GLTask newInitTask() {
        return GLTask.join(
                this.vao.new InitTask(),
                this.vPos.new InitTask(),
                this.vCol.new InitTask(),
                this.vUVs.new InitTask(),
                GLTask.create(() -> LOGGER.trace("GLText object initialized!")));
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
        program.setVertexAttributes(DEFAULT_ATTRIBUTES);

        final String vertexShader;
        final String fragmentShader;

        switch(GLWindow.CLIENT_API) {
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

        final GLVec2D offset = GLVec2D.create();
        final Deque<GLVec4D> colorStack = new LinkedList<>();
        final List<GLVec2> pos = new ArrayList<>();
        final List<GLVec2> uvs = new ArrayList<>();
        final List<GLVec4> col = new ArrayList<>();
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
                                
                                try{
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
                                }catch(NumberFormatException err){
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

        final GLVertexAttributes attribs = this.vAttribs.orElse(DEFAULT_ATTRIBUTES);

        this.length = pos.size() / 6;

        return GLTask.join(
                this.vPos.new UploadTask(GLTools.wrapVec2F(pos), GLBufferUsage.GL_DYNAMIC_DRAW),
                this.vCol.new UploadTask(GLTools.wrapVec4F(col), GLBufferUsage.GL_DYNAMIC_DRAW),
                this.vUVs.new UploadTask(GLTools.wrapVec2F(uvs), GLBufferUsage.GL_DYNAMIC_DRAW),
                this.vao.new AttachBufferTask(
                        attribs.getLocation("vPos"), this.vPos,
                        GLVertexAttributeType.GL_FLOAT, GLVertexAttributeSize.VEC2),
                this.vao.new AttachBufferTask(
                        attribs.getLocation("vCol"), this.vCol,
                        GLVertexAttributeType.GL_FLOAT, GLVertexAttributeSize.VEC4),
                this.vao.new AttachBufferTask(
                        attribs.getLocation("vUVs"), this.vUVs,
                        GLVertexAttributeType.GL_FLOAT, GLVertexAttributeSize.VEC2));
    }

    /**
     * Replaces the text rendering OpenGL program used in this GLText object
     * with another.
     *
     * @param program the OpenGL program to use
     * @param attribs the vertex attributes and their locations.
     * @param uProj the projection matrix uniform name.
     * @param uTrans the transformation matrix uniform name.
     * @param uName the font sampler uniform name.
     * @since 15.06.11
     */
    public void overrideTextRenderer(
            final GLProgram program,
            final GLVertexAttributes attribs,
            final CharSequence uProj, final CharSequence uTrans,
            final CharSequence uName) {

        LOGGER.trace("Replaced text renderer with [{}]! Old renderer: [{}].", this.program.orElse(getDefaultTextRenderer()).getName(), program != null ? program.getName() : getDefaultTextRenderer().getName());
        this.program = Optional.ofNullable(program);
        this.vAttribs = Optional.ofNullable(attribs);
        this.uSampler = Optional.ofNullable(uName.toString());
        this.uProj = Optional.ofNullable(uProj.toString());
        this.uTrans = Optional.ofNullable(uTrans.toString());
    }

    /**
     * Sets the text rendering OpenGL program used for this GLText object to the
     * default.
     *
     * @since 15.06.11
     */
    public void restoreTextRenderer() {
        LOGGER.trace("Replaced text renderer with default text renderer.");
        this.program = Optional.empty();
        this.vAttribs = Optional.empty();
        this.uSampler = Optional.empty();
        this.uProj = Optional.empty();
        this.uTrans = Optional.empty();
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

        final GLProgram prog = this.program.orElseGet(GLText::getDefaultTextRenderer);
        final String uFont = this.uSampler.orElse(GLText.DEFAULT_SAMPLER_UNAME);
        final String uPr = this.uProj.orElse(GLText.DEFAULT_PROJECTION_UNAME);
        final String uTr = this.uTrans.orElse(GLText.DEFAULT_TRANSLATION_UNAME);
        final int verts = (int) (this.length * percent) * 6;

        if(verts == 0){
            // NO-OP
            return GLTask.create(() -> {});
        }else{
            return GLTask.join(
                    this.font.newBindTask(target),
                    prog.new SetUniformMatrixFTask(uPr, pr.asGLMat4F()),
                    prog.new SetUniformMatrixFTask(uTr, tr.asGLMat4F()),
                    prog.new SetUniformITask(uFont, target),
                    prog.new UseTask(),
                    this.vao.new DrawArraysTask(GLDrawMode.GL_TRIANGLES, 0, verts)
            );
        }
    }

    /**
     * Deletes the GLText object. The GLText object has to have been initialized
     * to be deleted.
     *
     * @since 15.06.11
     */
    public void delete() {
        final GLTask deleteAll = GLTask.join(
                this.vao.new DeleteTask(),
                this.vPos.new DeleteTask(),
                this.vCol.new DeleteTask(),
                this.vUVs.new DeleteTask());

        deleteAll.glRun(this.getThread());
    }

    /**
     * Constructs a new
     *
     * @return the delete task
     * @since 15.06.11
     */
    public GLTask newDeleteTask() {
        return GLTask.join(
                this.vao.new DeleteTask(),
                this.vPos.new DeleteTask(),
                this.vCol.new DeleteTask(),
                this.vUVs.new DeleteTask(),
                GLTask.create(() -> LOGGER.trace("Deleting GLText object!")));
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
