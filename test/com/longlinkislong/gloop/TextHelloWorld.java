/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import com.longlinkislong.gloop.awt.GLAWTFont;
import java.awt.Font;

/**
 *
 * @author zmichaels
 */
public class TextHelloWorld {

    final int width = 640;
    final int height = 480;
    final GLWindow window = new GLWindow(width, height, "Text Test");
    final GLClear clear = new GLClear()
            .withClearColor(0.5f, 0.5f, 0.5f, 1f)
            .withClearDepth(1d);
    final GLBlending blend = new GLBlending()
            .withEnabled(GLEnableStatus.GL_ENABLED)
            .withBlendFunc(
                    GLBlendFunc.GL_SRC_ALPHA, GLBlendFunc.GL_ONE_MINUS_SRC_ALPHA,
                    GLBlendFunc.GL_SRC_ALPHA, GLBlendFunc.GL_ONE_MINUS_SRC_ALPHA);

    public TextHelloWorld() {
        this.clear.clear();
        this.blend.applyBlending();

        GLFont font = new GLAWTFont(new Font(Font.SERIF, Font.PLAIN, 24));

        GLText txt = new GLText(font, "Hello World!");

        this.window.setVisible(true);
        this.window.getGLThread().scheduleGLTask(GLTask.create(() -> {
            clear.clear();
            txt.setText("<red>Hello</red> <blue>World!</blue>");
            txt.draw(GLMat4F.ortho(0, width, height, 0, 0, 1), GLMat4F.create());
            this.window.update();
        }));
    }

    public static void main(String[] args) {
        new TextHelloWorld();
    }
}
