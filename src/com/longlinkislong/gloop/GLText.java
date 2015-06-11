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
import java.util.OptionalInt;

/**
 * An object that represents visible text.
 *
 * @author zmichaels
 * @since 15.06.11
 */
public class GLText extends GLObject implements CharSequence {

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
    private Optional<String> uName = Optional.empty();
    private Optional<String> uProj = Optional.empty();
    private Optional<String> uTrans = Optional.empty();
    private GLFont font;
    private int texWidth;
    private int texHeight;

    private GLBuffer vPos;
    private GLBuffer vCol;
    private GLBuffer vUVs;
    private GLVertexArray vao;
    private OptionalInt fontTarget = OptionalInt.empty();
    private Optional<GLProgram> program = Optional.empty();
    private Optional<GLVertexAttributes> vAttribs = Optional.empty();
    private int length;
    private volatile boolean isInit = false;

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
        this.text = seq.toString();

        this.vPos = new GLBuffer(thread);
        this.vCol = new GLBuffer(thread);
        this.vUVs = new GLBuffer(thread);
        this.vao = new GLVertexArray(thread);

        this.init();
    }

    private final GLTask initTask = new InitTask();

    /**
     * Initializes the GLText object. This is called automatically by the
     * constructor. A GLText object can be reinitialized if it has been deleted.
     *
     * @since 15.06.11
     */
    public final void init() {
        this.initTask.glRun(this.getThread());
    }

    /**
     * Checks if the GLText object is valid. A GLText object is valid if it has
     * been initialized and has not been deleted.
     *
     * @return true if valid.
     * @since 15.06.11
     */
    public boolean isValid() {
        return this.isInit;
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

    /**
     * A GLTask that initialized the GLText object.
     *
     * @since 15.06.11
     */
    public class InitTask extends GLTask {

        @Override
        public void run() {
            if (GLText.this.isValid()) {
                throw new GLException("Cannot initialize GLText! GLText is already initialized.");
            }

            if (!GLText.this.vPos.isValid()) {
                GLText.this.vPos.init();
            }
            if (!GLText.this.vUVs.isValid()) {
                GLText.this.vUVs.init();
            }
            if (!GLText.this.vCol.isValid()) {
                GLText.this.vCol.init();
            }
            if (!GLText.this.vao.isValid()) {
                GLText.this.vao.init();
            }

            GLText.this.isInit = true;
        }
    }

    private static GLProgram newDefaultTextRenderer() {
        final GLProgram program = new GLProgram();

        program.setVertexAttributes(DEFAULT_ATTRIBUTES);

        try (
                final InputStream inVsh = GLText.class.getResourceAsStream("default_text.vsh");
                final InputStream inFsh = GLText.class.getResourceAsStream("default_text.fsh")) {

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
     * @param seq the text to display.
     * @since 15.06.11
     */
    public void setText(final CharSequence seq) {
        new SetTextTask(seq).glRun(this.getThread());
    }

    /**
     * A GLTask that overwrites the text visible by the GLText object.
     *
     * @since 15.06.11
     */
    public class SetTextTask extends GLTask {

        private final String text;

        /**
         * Constructs a new SetTextTask with the supplied character sequence.
         *
         * @param seq the sequence to use
         * @since 15.06.11
         */
        public SetTextTask(final CharSequence seq) {
            this.text = seq.toString();
        }

        @Override
        public void run() {
            final GLVec2F offset = GLVec2F.create();
            final Deque<GLVec4F> colorStack = new LinkedList<>();
            final List<GLVec2F> pos = new ArrayList<>();
            final List<GLVec2F> uvs = new ArrayList<>();
            final List<GLVec4F> col = new ArrayList<>();
            final GLFontMetrics metrics = GLText.this.font.getMetrics();
            GLVec4F color = GLColors.WHITE.color;

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
                                    final int colorVal = Integer.decode(colorCode);
                                    final int red = colorVal & 0xFF000000;
                                    final int green = colorVal & 0x00FF0000;
                                    final int blue = colorVal & 0x0000FF00;
                                    final int alpha = colorVal & 0x000000FF;

                                    colorStack.push(GLVec4F.create(
                                            red / 255f,
                                            green / 255f,
                                            blue / 255f,
                                            alpha / 255f));
                                }
                        }

                        i += tagSize;
                    }

                    continue;
                }
                //</editor-fold>

                final char c = text.charAt(i);
                final int top = (c - ' ') / 10;
                final int left = (c - ' ') % 10;

                final float width = metrics.getCharWidth(c);
                final float height = metrics.getMaxHeight();
                final float txW = width / (float) texWidth;
                final float txH = height / (float) texHeight;

                float u = left / 10f;
                float v = top / 10f;

                if (c == '\t') {
                    offset.set(0, offset.x() + 2f * width);
                    continue;
                } else if (c == '\r') {
                    offset.set(0, 0f);
                    continue;
                } else if (c == '\n') {
                    offset.set(1, offset.y() + height);
                    continue;
                } else if (c == ' ') {
                    offset.set(0, offset.x() + width);
                    continue;
                } else if (c < ' ' || c > '~') {
                    // skip non-ascii text characters
                    continue;
                }

                final float x0 = offset.x();
                final float x1 = offset.x() + width;
                final float y0 = offset.y();
                final float y1 = offset.y() + height;

                final float u0 = u;
                final float u1 = u + txW;
                final float v0 = v;
                final float v1 = v + txH;

                pos.add(GLVec2F.create(x0, y0));
                pos.add(GLVec2F.create(x0, y1));
                pos.add(GLVec2F.create(x1, y1));
                pos.add(GLVec2F.create(x1, y1));
                pos.add(GLVec2F.create(x1, y0));
                pos.add(GLVec2F.create(x0, y0));

                uvs.add(GLVec2F.create(u0, v0));
                uvs.add(GLVec2F.create(u0, v1));
                uvs.add(GLVec2F.create(u1, v1));
                uvs.add(GLVec2F.create(u1, v1));
                uvs.add(GLVec2F.create(u1, v0));
                uvs.add(GLVec2F.create(u0, v0));

                for (int j = 0; j < 6; j++) {
                    col.add(color);
                }

                offset.set(0, offset.x() + width);
            }

            GLText.this.vPos.upload(GLTools.wrapVec2F(pos));
            GLText.this.vCol.upload(GLTools.wrapVec4F(col));
            GLText.this.vUVs.upload(GLTools.wrapVec2F(uvs));

            // allocate a new vao object. 
            //TODO: check if this is necessary
            GLText.this.vao.delete();
            GLText.this.vao.init();

            GLText.this.vao.attachBuffer(
                    GLText.this.vAttribs.orElse(DEFAULT_ATTRIBUTES).getLocation("vPos"),
                    vPos,
                    GLVertexAttributeType.GL_FLOAT, GLVertexAttributeSize.VEC2);
            GLText.this.vao.attachBuffer(
                    GLText.this.vAttribs.orElse(DEFAULT_ATTRIBUTES).getLocation("vCol"),
                    vCol,
                    GLVertexAttributeType.GL_FLOAT, GLVertexAttributeSize.VEC4);
            GLText.this.vao.attachBuffer(
                    GLText.this.vAttribs.orElse(DEFAULT_ATTRIBUTES).getLocation("vUVs"),
                    vUVs,
                    GLVertexAttributeType.GL_FLOAT, GLVertexAttributeSize.VEC2);

            GLText.this.length = pos.size() / 6; // 6 verts per character                        
        }
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

        this.program = Optional.ofNullable(program);
        this.vAttribs = Optional.ofNullable(attribs);
        this.uName = Optional.ofNullable(uName.toString());
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
        this.program = Optional.empty();
        this.vAttribs = Optional.empty();
        this.uName = Optional.empty();
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

        new DrawTask(pr, tr, target, percent).glRun(this.getThread());
    }

    /**
     * A GLTask that draws the text object.
     *
     * @since 15.06.11
     */
    public class DrawTask extends GLTask {

        final GLMat4F vPr;
        final GLMat4F vTr;
        final int target;
        final float percent;

        /**
         * Constructs a new GLText DrawTask using the supplied projection
         * matrix, transformation matrix, and target to bind the font texture
         * to.
         *
         * @param pr the projection matrix to use.
         * @param tr the transformation matrix to use.
         * @param target the target to bind the font texture to.
         * @since 15.06.11
         */
        public DrawTask(final GLMat4 pr, final GLMat4 tr, final int target) {
            this(pr, tr, target, 1f);
        }

        /**
         * Constructs a new GLText DrawTask for drawing a portion of the text.
         *
         * @param pr the projection matrix to use.
         * @param tr the transformation matrix to use.
         * @param target the target to bind the font texture to.
         * @param percent the percentage of the text to draw.
         * @since 15.06.11
         */
        public DrawTask(final GLMat4 pr, final GLMat4 tr, final int target, final float percent) {
            if ((this.target = target) < 0) {
                throw new GLException("Invalid target for the font!");
            }

            if (percent < 0f || percent > 1f) {
                throw new GLException("Percent must be in range of [0.0, 1.0]!");
            }

            this.vPr = pr.asGLMat4F().asStaticMat();
            this.vTr = tr.asGLMat4F().asStaticMat();
            this.percent = percent;
        }

        @Override
        public void run() {
            if (!GLText.this.isInit) {
                throw new GLException("GLText object is not initialized!");
            }

            final GLProgram program = GLText.this.program.orElseGet(GLText::getDefaultTextRenderer);
            final String uName = GLText.this.uName.orElse(GLText.DEFAULT_SAMPLER_UNAME);
            final String uProj = GLText.this.uProj.orElse(GLText.DEFAULT_PROJECTION_UNAME);
            final String uTrans = GLText.this.uTrans.orElse(GLText.DEFAULT_TRANSLATION_UNAME);

            final int verts = (int) (GLText.this.length * 6 * this.percent);

            GLText.this.font.bind(this.target);

            program.use();
            program.setUniformI(uName, this.target);
            program.setUniformMatrixF(uProj, this.vPr);
            program.setUniformMatrixF(uTrans, this.vTr);

            GLText.this.vao.drawArrays(
                    GLDrawMode.GL_TRIANGLES,
                    0,
                    verts);
        }

    }

    private final GLTask deleteTask = new DeleteTask();

    /**
     * Deletes the GLText object. The GLText object has to have been initialized
     * to be deleted.
     *
     * @since 15.06.11
     */
    public void delete() {
        this.deleteTask.glRun(this.getThread());
    }

    /**
     * A GLTask that deletes the GLText object.
     * @since 15.06.11
     */
    public class DeleteTask extends GLTask {

        @Override
        public void run() {
            if (!GLText.this.isInit) {
                throw new GLException("GLText needs to exist before being deleted!");
            }

            GLText.this.vao.delete();
            GLText.this.vPos.delete();
            GLText.this.vUVs.delete();
            GLText.this.vCol.delete();

            GLText.this.isInit = false;
        }
    }
}
