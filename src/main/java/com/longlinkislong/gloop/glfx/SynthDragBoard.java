/**
 * From: https://github.com/empirephoenix/JME3-JFX/tree/master/src/main/java/com/jme3x/jfx
 */
package com.longlinkislong.gloop.glfx;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;

public class SynthDragBoard {

    public static final String DRAGPROXY_SPECIAL_TOFRONT = "dragimage:true;";

    private Map<String, Object> dataTransfer = new HashMap<>();
    private Node dragProxy;
    boolean abort;

    public void setDragProxy(final Node dragProxy) {
        dragProxy.setMouseTransparent(true);
        dragProxy.setStyle(DRAGPROXY_SPECIAL_TOFRONT); // marker layout
        dragProxy.setVisible(true);

        this.dragProxy = dragProxy;
    }

    public void setAbort(final boolean abort) {
        this.abort = abort;
    }

    public boolean isAbort() {
        return this.abort;
    }

    public Map<String, Object> getDataTransfer() {
        return this.dataTransfer;
    }

    public boolean hasDragProxy() {
        return this.dragProxy != null;
    }

    public Node getDragProxy() {
        return this.dragProxy;
    }

}
