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
import java.util.ArrayList;
import java.util.List;
import javafx.scene.input.TransferMode;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.HostDragStartListener;
import com.sun.javafx.scene.input.DragboardHelper;
import com.sun.javafx.tk.Toolkit;
import java.util.function.Consumer;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;

/**
 * An utility class to connect DnD mechanism of Swing and FX.
 */
final class GLFXDNDHandler {

    private final EmbeddedSceneInterface scene;
    private final GLFXStage glfxStage;

    private static EmbeddedSceneDSInterface dragSource = null;
    private static TransferMode dragAction = null;
    private EmbeddedSceneDTInterface dropTarget = null;

    /**
     * List of targets that the mouse is over
     */
    private List<Node> dragEntered = new ArrayList<>();

    /**
     * Event to assign to nodes when they are moused over or dropped on
     */
    private Dragboard dragBoard = null;

    private Parent getRootNode(){
        return glfxStage.getRootNode();
    }

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
            this.processDragOver(x, y, sx, sy);
        }
    }

    public void mouseReleased(final int x, final int y, final int sx, final int sy){
        if(dragSource != null){
            getDropTarget().handleDragDrop(x, y, sx, sy, dragAction);

            /*
            dragEntered.forEach(node -> {
                callDragExited(node, x, y, sx, sy, dragAction);
            });
            dragEntered.clear();


            Node under = pick(getRootNode(), x, y);
            if(under != null){
                callDragDropped(under, x, y, sx, sy, dragAction);
            }
            */

            dragSource.dragDropEnd(dragAction);
            dragSource = null;
            dropTarget = null;
        }
    }

    /**
     * Processes the drag enter and exit events, also updates the overTargets
     * container.
     * @param current
     * @param x
     * @param y
     * @param sx
     * @param sy
     * @return
     */
    private void processDragOver(final int x, final int y, final int sx, final int sy) {
        getDropTarget().handleDragLeave();

        getDropTarget().handleDragEnter(x, y, sx, sy, dragAction, dragSource);
        getDropTarget().handleDragOver(x, y, sx, sy, dragAction);

        // looks like the above relaces all of this:

        //List<Node> underNodes = new ArrayList<>();

        //forAllUnder(getRootNode(), sx, sy, node -> {
        //    underNodes.add(node);
        //});



        /*
        // exited
        for(int i=0;i<dragEntered.size();i++){
            Node node = dragEntered.get(i);
            if(!underNodes.contains(node)){
                i--;
                dragEntered.remove(node);



                //callDragExited(node, x, y, sx, sy, dragAction);
            }
        }

        // entered
        for(Node node:underNodes){
            if(!dragEntered.contains(node)){
                dragEntered.add(node);

                callDragEntered(node, x, y, sx, sy, dragAction);
            }
            callDragOver(node, x, y, sx, sy, dragAction);
        }
        */
    }

    private void callDragOver(Node node, double x, double y, double sx, double sy, TransferMode mode){
        if (node.getOnDragOver() != null) {
            final DragEvent event = new DragEvent(null, node, DragEvent.DRAG_OVER, this.dragBoard, x, y, sx, sy, mode, null, null, null);
            try {
                node.getOnDragOver().handle(event);
            } catch (final Exception e) {
                throw e;
            }
        }
    }
    private void callDragEntered(Node node, double x, double y, double sx, double sy, TransferMode mode){
        if (node.getOnDragEntered() != null) {
            final DragEvent event = new DragEvent(null, node, DragEvent.DRAG_ENTERED_TARGET, this.dragBoard, x, y, sx, sy, mode, null, null, null);
            try {
                node.getOnDragEntered().handle(event);
            } catch (final Exception e) {
                throw e;
            }
        }
    }
    private void callDragExited(Node node, double x, double y, double sx, double sy, TransferMode mode){
        if (node.getOnDragExited() != null) {
            final DragEvent event = new DragEvent(null, node, DragEvent.DRAG_EXITED_TARGET, this.dragBoard, x, y, sx, sy, mode, null, null, null);
            try {
                node.getOnDragExited().handle(event);
            } catch (final Exception e) {
                throw e;
            }
        }
    }
    private void callDragDropped(Node node, double x, double y, double sx, double sy, TransferMode mode){
        if (node.getOnDragDropped() != null) {
            final DragEvent event = new DragEvent(null, node, DragEvent.DRAG_DROPPED, this.dragBoard, x, y, sx, sy, mode, null, null, null);
            try {
                node.getOnDragDropped().handle(event);
            } catch (final Exception e) {
                throw e;
            }
        }
    }

    public static Node pick(Node node, double sceneX, double sceneY) {
        Point2D p = node.sceneToLocal(sceneX, sceneY, true /* rootScene */);

        // check if the given node has the point inside it, or else we drop out
        if (!node.contains(p)) {
            return null;
        }

        // at this point we know that _at least_ the given node is a valid
        // answer to the given point, so we will return that if we don't find
        // a better child option
        if (node instanceof Parent) {
            // we iterate through all children in reverse order, and stop when we find a match.
            // We do this as we know the elements at the end of the list have a higher
            // z-order, and are therefore the better match, compared to children that
            // might also intersect (but that would be underneath the element).
            Node bestMatchingChild = null;
            List<Node> children = ((Parent) node).getChildrenUnmodifiable();
            for (int i = children.size() - 1; i >= 0; i--) {
                Node child = children.get(i);
                p = child.sceneToLocal(sceneX, sceneY, true /* rootScene */);
                if (child.isVisible() && !child.isMouseTransparent() && child.contains(p)) {
                    bestMatchingChild = child;
                    break;
                }
            }

            if (bestMatchingChild != null) {
                return pick(bestMatchingChild, sceneX, sceneY);
            }
        }

        return node;
    }
    public static void forAllUnder(Node node, double sceneX, double sceneY, Consumer<Node> onUnder) {
        Point2D p = node.sceneToLocal(sceneX, sceneY, true /* rootScene */);

        // check if the given node has the point inside it, or else we drop out
        if (!node.contains(p)) {
            return;
        }

        // at this point we know that _at least_ the given node is a valid
        // answer to the given point, so we will return that if we don't find
        // a better child option
        if (node instanceof Parent) {
            // we iterate through all children in reverse order, and stop when we find a match.
            // We do this as we know the elements at the end of the list have a higher
            // z-order, and are therefore the better match, compared to children that
            // might also intersect (but that would be underneath the element).
            Node bestMatchingChild = null;
            List<Node> children = ((Parent) node).getChildrenUnmodifiable();
            for (int i = children.size() - 1; i >= 0; i--) {
                Node child = children.get(i);
                p = child.sceneToLocal(sceneX, sceneY, true /* rootScene */);
                if (child.isVisible() && !child.isMouseTransparent() && child.contains(p)) {
                    bestMatchingChild = child;
                    break;
                }
            }

            if (bestMatchingChild != null) {
                onUnder.accept(bestMatchingChild);
                forAllUnder(bestMatchingChild, sceneX, sceneY, onUnder);
            }
        }
    }

}