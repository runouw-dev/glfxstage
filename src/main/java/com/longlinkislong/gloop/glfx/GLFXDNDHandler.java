/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.longlinkislong.gloop.glfx;

import com.sun.javafx.embed.EmbeddedSceneDSInterface;
import com.sun.javafx.embed.EmbeddedSceneDTInterface;
import javafx.scene.input.TransferMode;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.HostDragStartListener;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Platform;

/**
 * An utility class to connect DnD mechanism of Swing and FX.
 */
final class GLFXDNDHandler {

    private final EmbeddedSceneInterface scene;
    private final GLFXStage glfxStage;

    private static EmbeddedSceneDSInterface dragSource = null;
    private static TransferMode dragAction = null;
    private EmbeddedSceneDTInterface dropTarget = null;

    GLFXDNDHandler(final EmbeddedSceneInterface scene, final GLFXStage glfxStage) {
        this.scene = scene;
        this.glfxStage = glfxStage;

        scene.setDragStartListener(getDragStartListener);
    }

    private EmbeddedSceneDTInterface getDropTarget(){
        if(dropTarget == null){
            dropTarget = scene.createDropTarget();
        }
        return dropTarget;
    }

    HostDragStartListener getDragStartListener = (EmbeddedSceneDSInterface dragSource, TransferMode dragAction) -> {
        if(!Toolkit.getToolkit().isFxUserThread()){
            throw new Error("Not on FX thread!");
        }
        if(dragSource == null){
            throw new Error("Drag source cannot be null!");
        }

        // TODO: find out how to get dragboard?
        // TODO: render dragboard image to texture and expose it so user can draw it

        this.dragSource = dragSource;
        this.dragAction = dragAction;

        System.out.println("Drag start! " + dragSource);
    };

    public void mousePosition(final int x, final int y, final int sx, final int sy) {
        // TODO: update drag image position
        if(dragSource != null){
            EmbeddedSceneDTInterface dt = getDropTarget();

            dt.handleDragLeave();

            dt.handleDragEnter(x, y, sx, sy, dragAction, dragSource);
            dt.handleDragOver(x, y, sx, sy, dragAction);
        }
    }

    public void mouseReleased(final int x, final int y, final int sx, final int sy){
        if(dragSource != null){
            getDropTarget().handleDragDrop(x, y, sx, sy, dragAction);

            // clear these next frame
            Platform.runLater(() -> {
                if(dragSource != null){
                    dragSource.dragDropEnd(dragAction);

                    dragSource = null;
                    dropTarget = null;
                }
            });
        }
    }
}