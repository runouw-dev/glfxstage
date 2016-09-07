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

package com.longlinkislong.gloop.glfx;

import com.sun.javafx.embed.EmbeddedSceneDSInterface;
import com.sun.javafx.embed.EmbeddedSceneDTInterface;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.HostDragStartListener;
import com.sun.javafx.tk.Toolkit;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.input.TransferMode;

/**
 * An utility class to connect DnD mechanism of Swing and FX.
 */
final class GLFXDNDHandler {

    private final EmbeddedSceneInterface scene;
    private final GLFXStage glfxStage;


    private class DropInfo{
        private final EmbeddedSceneDSInterface dragSource;
        private final TransferMode dragAction;

        public DropInfo(EmbeddedSceneDSInterface dragSource, TransferMode dragAction) {
            this.dragSource = dragSource;
            this.dragAction = dragAction;
        }
    }
    private class DropHandlerData{
        private int x;
        private int y;
        private int sx;
        private int sy;
        private final EmbeddedSceneDTInterface dt;

        public DropHandlerData(EmbeddedSceneDTInterface dropTarget) {
            this.dt = dropTarget;
        }

        private void handleDragLeave(){
            dt.handleDragLeave();
        }
        private void handleDragEnter(){
            dt.handleDragEnter(x, y, sx, sy, staticDropInfo.dragAction, staticDropInfo.dragSource);
        }
        private void handleDragOver(){
            dt.handleDragOver(x, y, sx, sy, staticDropInfo.dragAction);
        }
        private TransferMode handleDragDrop(){
            return dt.handleDragDrop(x, y, x, y, staticDropInfo.dragAction);
        }

    }

    private static DropInfo staticDropInfo;
    private DropInfo _dropInfo;

    private EmbeddedSceneDTInterface dropTarget = null;
    private final static List<DropHandlerData> DROP_TARGETS = new ArrayList<>();

    GLFXDNDHandler(final EmbeddedSceneInterface scene, final GLFXStage glfxStage) {
        this.scene = scene;
        this.glfxStage = glfxStage;

        scene.setDragStartListener(getDragStartListener);
    }

    private DropHandlerData getDropTarget(int x, int y, int sx, int sy){
        DropHandlerData dt = DROP_TARGETS.stream().filter(data -> data.dt == dropTarget).findFirst().orElse(null);
        if(dt == null){
            dropTarget = scene.createDropTarget();
            dt = new DropHandlerData(dropTarget);
            DROP_TARGETS.add(dt);
        }

        dt.x = x;
        dt.y = y;
        dt.sx = sx;
        dt.sy = sy;
        return dt;
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

        this._dropInfo = new DropInfo(dragSource, dragAction);
        staticDropInfo = _dropInfo;
    };

    public void mousePosition(final int x, final int y, final int sx, final int sy) {
        // TODO: update drag image position
        if(staticDropInfo != null){
            DropHandlerData dt = getDropTarget(x, y, sx, sy);

            dt.handleDragLeave();

            dt.handleDragEnter();
            dt.handleDragOver();
        }
    }

    public void mouseReleased(final int x, final int y, final int sx, final int sy){
        if(staticDropInfo != null && _dropInfo == staticDropInfo){ // only one handler will handle the drop for all of them
            TransferMode finalMode = null;
            for(DropHandlerData dt:DROP_TARGETS){
                //dt.handleDragLeave();
                TransferMode newMode = dt.handleDragDrop();
                if(newMode != null){
                    finalMode = newMode;
                }
            }

            staticDropInfo.dragSource.dragDropEnd(finalMode);

            // clear
            _dropInfo = null;
            staticDropInfo = null;
            DROP_TARGETS.clear();
        }
        dropTarget = null;
    }
}