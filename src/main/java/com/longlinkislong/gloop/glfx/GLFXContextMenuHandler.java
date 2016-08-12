/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop.glfx;

import com.sun.javafx.embed.EmbeddedSceneInterface;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.PickResult;

/**
 *
 * @author runou
 */
public class GLFXContextMenuHandler {
    private final EmbeddedSceneInterface scene;
    private final GLFXStage glfxStage;

    public GLFXContextMenuHandler(EmbeddedSceneInterface scene, GLFXStage glfxStage) {
        this.scene = scene;
        this.glfxStage = glfxStage;
    }

    public void fireContextMenuFromMouse(double mouseX, double mouseY, double mouseAbsX, double mouseAbsY){
        forAllUnder(glfxStage.getRootNode(), mouseX, mouseY, node -> fireContextMenuEventFromMouse(node, mouseX, mouseY, mouseAbsX, mouseAbsY));

        /*
        Node node = pickUnderMouse(glfxStage.getRootNode(), mouseX, mouseY);
        if(node != null){
            fireContextMenuEventFromMouse(node, mouseX, mouseY, mouseAbsX, mouseAbsY);
        }
        */
    }
    public void fireContextMenuFromKeyboard(){
        Platform.runLater(() -> {
            Node focusOwner = glfxStage.getScene().getFocusOwner();
            if(focusOwner != null){
                fireContextMenuEventFromKeyboard(focusOwner);
            }
        });
    }

    public void fireContextMenuEventFromKeyboard(Node node){
        Platform.runLater(() -> {
            Bounds tabBounds = node.getBoundsInLocal();
            double centerX = tabBounds.getMinX() + tabBounds.getWidth()/2;
            double centerY = tabBounds.getMinY()+tabBounds.getHeight()/2;
            Point2D pos = node.localToScreen(centerX, centerY);
            double x = pos.getX();
            double y = pos.getY();
            Event contextMenuEvent = new ContextMenuEvent(ContextMenuEvent.CONTEXT_MENU_REQUESTED, centerX, centerY, x, y, true, new PickResult(node, x, y));
            Event.fireEvent(node, contextMenuEvent);
        });
    }
    public void fireContextMenuEventFromMouse(Node node, double x, double y, double screenX, double screenY){
        Event contextMenuEvent = new ContextMenuEvent(ContextMenuEvent.CONTEXT_MENU_REQUESTED, x, y, screenX, screenY, false, new PickResult(node, x, y));
        Event.fireEvent(node, contextMenuEvent);
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

    private Node pickUnderMouse(final Node current, final double x, final double y) {
        if (current.getOnDragDropped() != null) {
            return current;
        }

        if (current instanceof Parent) {
            final Parent p = (Parent) current;
            for (int i = p.getChildrenUnmodifiable().size() - 1; i >= 0; i--) {
                final Node child = p.getChildrenUnmodifiable().get(i);
                final Bounds bounds = child.localToScene(child.getBoundsInLocal());
                if (bounds.contains(x, y)) {
                    final Node found = this.pickUnderMouse(child, x, y);
                    return found;
                }

            }
        }

        return null;
    }

}
