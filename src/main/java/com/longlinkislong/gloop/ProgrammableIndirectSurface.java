/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import static com.longlinkislong.gloop.GLFramebufferMode.GL_COLOR_BUFFER_BIT;
import static com.longlinkislong.gloop.GLFramebufferMode.GL_DEPTH_BUFFER_BIT;
import static com.longlinkislong.gloop.GLFramebufferMode.GL_STENCIL_BUFFER_BIT;
import com.runouw.util.Lazy;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 *
 * @author zmichaels
 */
public class ProgrammableIndirectSurface {

    public static final String BICUBIC_SHADER = "#version 130\n"
            + "\n"
            + "in vec2 fUVs;\n"
            + "\n"
            + "out vec4 fColor;\n"
            + "\n"
            + "uniform sampler2D texture;\n"
            + "\n"
            + "vec4 cubic(float v) {\n"
            + "    vec4 n = vec4(1.0, 2.0, 3.0, 4.0) - v;\n"
            + "    vec4 s = n * n * n;\n"
            + "    float x = s.x;\n"
            + "    float y = s.y - 4.0 * s.x;\n"
            + "    float z = s.z - 4.0 * s.y + 6.0 * s.x;\n"
            + "    float w = 6.0 - x - y - z;\n"
            + "\n"
            + "    return vec4(x, y, z, w) * (1.0 / 6.0);\n"
            + "}\n"
            + "\n"
            + "vec4 textureBicubic(sampler2D sampler, vec2 texCoords) {\n"
            + "    vec2 texSize = textureSize(sampler, 0);\n"
            + "    vec2 invTexSize = 1.0 / texSize;\n"
            + "\n"
            + "    texCoords = texCoords * texSize - 0.5;\n"
            + "\n"
            + "    vec2 fxy = fract(texCoords);\n"
            + "\n"
            + "    texCoords -= fxy;\n"
            + "\n"
            + "    vec4 xcubic = cubic(fxy.x);\n"
            + "    vec4 ycubic = cubic(fxy.y);\n"
            + "\n"
            + "    vec4 c = texCoords.xxyy + vec2(-0.5, +1.5).xyxy;\n"
            + "    \n"
            + "    vec4 s = vec4(xcubic.xz + xcubic.yw, ycubic.xz + ycubic.yw);\n"
            + "    vec4 offset = c + vec4(xcubic.yw, ycubic.yw) / s;\n"
            + "\n"
            + "    offset *= invTexSize.xxyy;\n"
            + "\n"
            + "    vec4 sample0 = texture2D(sampler, offset.xz);\n"
            + "    vec4 sample1 = texture2D(sampler, offset.yz);\n"
            + "    vec4 sample2 = texture2D(sampler, offset.xw);\n"
            + "    vec4 sample3 = texture2D(sampler, offset.yw);\n"
            + "\n"
            + "    float sx = s.x / (s.x + s.y);\n"
            + "    float sy = s.y / (s.z + s.w);\n"
            + "\n"
            + "    return mix(\n"
            + "        mix(sample3, sample2, sx),\n"
            + "        mix(sample1, sample0, sx),\n"
            + "        sy);\n"
            + "    \n"
            + "}\n"
            + "\n"
            + "void main() {\n"
            + "    fColor = textureBicubic(texture, fUVs);\n"
            + "}\n";
    private final Lazy<GLBuffer> vPos;
    private final Lazy<GLBuffer> vUVs;
    private final Lazy<GLVertexArray> vao;
    private final Lazy<GLFramebuffer> framebuffer;
    private final Lazy<GLTexture> colorBuffer;
    private final Lazy<GLRenderbuffer> depthStencil;
    private final Lazy<GLProgram> program;

    public final GLTextureInternalFormat colorFormat;
    public final GLTextureInternalFormat depthStencilFormat;

    public final int width;
    public final int height;

    private GLClear clear;
    private final GLViewport viewport;

    private static final GLVertexAttributes ATTRIBS = new GLVertexAttributes();

    static {
        ATTRIBS.setAttribute("vPos", 0);
        ATTRIBS.setAttribute("vUVs", 1);
    }

    public ProgrammableIndirectSurface(
            final String fragShaderCode,
            final int width, final int height,
            final GLTextureInternalFormat colorFormat,
            final GLTextureInternalFormat depthStencilFormat) {

        this.width = width;
        this.height = height;
        this.colorFormat = Objects.requireNonNull(colorFormat);
        this.depthStencilFormat = Objects.requireNonNull(depthStencilFormat);

        this.vPos = new Lazy<>(() -> {
            final GLBuffer out = new GLBuffer();

            out.upload(GLTools.wrapFloat(
                    -1f, 1f,
                    -1f, -1f,
                    1f, 1f,
                    1f, -1f));

            return out;
        });

        this.vUVs = new Lazy<>(() -> {
            final GLBuffer out = new GLBuffer();

            out.upload(GLTools.wrapFloat(
                    0f, 1f,
                    0f, 0f,
                    1f, 1f,
                    1f, 0f));

            return out;
        });

        this.vao = new Lazy<>(() -> {
            final GLVertexArray out = new GLVertexArray();

            out.attachBuffer(ATTRIBS.getLocation("vPos"), vPos.get(), GLVertexAttributeType.GL_FLOAT, GLVertexAttributeSize.VEC2);
            out.attachBuffer(ATTRIBS.getLocation("vUVs"), vUVs.get(), GLVertexAttributeType.GL_FLOAT, GLVertexAttributeSize.VEC2);

            return out;
        });

        this.colorBuffer = new Lazy<>(() -> {
            final GLTexture out = new GLTexture();

            out.allocate(1, colorFormat, width, height);
            out.setAttributes(new GLTextureParameters()
                    .withFilter(GLTextureMinFilter.GL_LINEAR, GLTextureMagFilter.GL_LINEAR)
                    .withWrap(GLTextureWrap.GL_CLAMP_TO_EDGE, GLTextureWrap.GL_CLAMP_TO_EDGE, GLTextureWrap.GL_CLAMP_TO_EDGE));

            return out;
        });

        this.depthStencil = new Lazy<>(() -> new GLRenderbuffer(depthStencilFormat, width, height));

        this.framebuffer = new Lazy<>(() -> {
            final GLFramebuffer out = new GLFramebuffer();

            out.addColorAttachment("color", this.colorBuffer.get());
            out.addRenderbufferAttachment("depthStencil", this.depthStencil.get());

            return out;
        });

        this.program = new Lazy<>(() -> {
            final GLProgram out = new GLProgram();

            out.setVertexAttributes(ATTRIBS);

            try (InputStream vIn = ProgrammableIndirectSurface.class.getResourceAsStream("programmableIndirectSurface.vert")) {
                final String vertexShaderCode = GLTools.readAll(vIn);

                final GLShader vsh = GLShader.newVertexShader(vertexShaderCode);
                final GLShader fsh = GLShader.newFragmentShader(fragShaderCode);

                out.linkShaders(vsh, fsh);
                vsh.delete();
                fsh.delete();
            } catch (IOException ex) {
                throw new GLException("Unable to load vertex shader!", ex);
            }

            return out;
        });

        this.clear = new GLClear()
                .withClearBits(GL_COLOR_BUFFER_BIT, GL_DEPTH_BUFFER_BIT, GL_STENCIL_BUFFER_BIT)
                .withClearColor(0f, 0f, 0f, 1f)
                .withClearDepth(1.0);

        this.viewport = new GLViewport(0, 0, width, height);
    }

    public ProgrammableIndirectSurface(
            final GLThread thread,
            final String fragShaderCode,
            final int width, final int height,
            final GLTextureInternalFormat colorFormat,
            final GLTextureInternalFormat depthStencilFormat) {

        this.width = width;
        this.height = height;
        this.colorFormat = Objects.requireNonNull(colorFormat);
        this.depthStencilFormat = Objects.requireNonNull(depthStencilFormat);

        this.vPos = new Lazy<>(() -> {
            final GLBuffer out = new GLBuffer(thread);

            out.upload(GLTools.wrapFloat(
                    0f, 0f,
                    0f, 1f,
                    1f, 0f,
                    1f, 1f));

            return out;
        });

        this.vUVs = new Lazy<>(() -> {
            final GLBuffer out = new GLBuffer(thread);

            out.upload(GLTools.wrapFloat(
                    0f, 0f,
                    0f, 1f,
                    1f, 0f,
                    1f, 1f));

            return out;
        });

        this.vao = new Lazy<>(() -> {
            final GLVertexArray out = new GLVertexArray(thread);

            out.attachBuffer(ATTRIBS.getLocation("vPos"), vPos.get(), GLVertexAttributeType.GL_FLOAT, GLVertexAttributeSize.VEC2);
            out.attachBuffer(ATTRIBS.getLocation("vUVs"), vUVs.get(), GLVertexAttributeType.GL_FLOAT, GLVertexAttributeSize.VEC2);

            return out;
        });

        this.colorBuffer = new Lazy<>(() -> {
            final GLTexture out = new GLTexture(thread);

            out.allocate(1, colorFormat, width, height);
            out.setAttributes(new GLTextureParameters()
                    .withFilter(GLTextureMinFilter.GL_LINEAR, GLTextureMagFilter.GL_LINEAR)
                    .withWrap(GLTextureWrap.GL_CLAMP_TO_EDGE, GLTextureWrap.GL_CLAMP_TO_EDGE, GLTextureWrap.GL_CLAMP_TO_EDGE));

            return out;
        });

        this.depthStencil = new Lazy<>(() -> new GLRenderbuffer(thread, depthStencilFormat, width, height));

        this.framebuffer = new Lazy<>(() -> {
            final GLFramebuffer out = new GLFramebuffer(thread);

            out.addColorAttachment("color", this.colorBuffer.get());
            out.addRenderbufferAttachment("depthStencil", this.depthStencil.get());

            return out;
        });

        this.program = new Lazy<>(() -> {
            final GLProgram out = new GLProgram(thread);

            out.setVertexAttributes(ATTRIBS);

            try (InputStream vIn = ProgrammableIndirectSurface.class.getResourceAsStream("programmableIndirectSurface.vert")) {
                final String vertexShaderCode = GLTools.readAll(vIn);

                final GLShader vsh = GLShader.newVertexShader(vertexShaderCode);
                final GLShader fsh = GLShader.newFragmentShader(fragShaderCode);

                out.linkShaders(vsh, fsh);
                vsh.delete();
                fsh.delete();
            } catch (IOException ex) {
                throw new GLException("Unable to load vertex shader!", ex);
            }

            return out;
        });

        this.clear = new GLClear(thread)
                .withClearBits(GL_COLOR_BUFFER_BIT, GL_DEPTH_BUFFER_BIT, GL_STENCIL_BUFFER_BIT)
                .withClearColor(0f, 0f, 0f, 1f)
                .withClearDepth(1.0);

        this.viewport = new GLViewport(thread, 0, 0, width, height);
    }

    public ProgrammableIndirectSurface setClearColor(final float r, final float g, final float b, final float a) {
        this.clear = clear.withClearColor(r, g, b, a);
        return this;
    }

    public ProgrammableIndirectSurface setClearDepth(final double depth) {
        this.clear = clear.withClearDepth(depth);
        return this;
    }

    public void bind() {
        final GLFramebuffer fb = this.framebuffer.get();
        final GLThread thread = fb.getThread();

        thread.pushFramebufferBind();
        thread.pushViewport();

        fb.bind();

        viewport.applyViewport();
        clear.clear();
    }

    public void unbind() {
        final GLThread thread = this.framebuffer.get().getThread();

        thread.popViewport();
        thread.popFramebufferBind();
    }

    public void blit() {
        final GLProgram p = this.program.get();

        this.colorBuffer.get().bind(0);
        p.use();
        p.setUniformI("texture", 0);        

        vao.get().drawArrays(GLDrawMode.GL_TRIANGLE_STRIP, 0, 4);
    }

    public void delete() {
        this.framebuffer.ifInitialized(GLFramebuffer::delete);
        this.colorBuffer.ifInitialized(GLTexture::delete);
        this.depthStencil.ifInitialized(GLRenderbuffer::delete);
        this.vao.ifInitialized(GLVertexArray::delete);
        this.vPos.ifInitialized(GLBuffer::delete);
        this.vUVs.ifInitialized(GLBuffer::delete);
    }
}
