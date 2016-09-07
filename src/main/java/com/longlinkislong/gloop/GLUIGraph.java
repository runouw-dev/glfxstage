/*
 * Copyright (c) 2015, longlinkislong.com
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A graph structure that manages selection of GLUIComponents.
 *
 * @author zmichaels
 * @since 15.08.21
 */
public class GLUIGraph {

    private final Node root = new Node(null);
    private final Map<GLUIComponent, Node> nodes = new HashMap<>();    
    private Node currentSelected = root;

    /**
     * Constructs a new graph mapping between menu items.
     *
     * @since 15.08.21
     */
    public GLUIGraph() {
        this.nodes.put(null, this.root);
    }

    /**
     * Clears the GLUIGraph object of all nodes.
     * @since 15.08.21
     */
    public void clear() {
        this.nodes.values()
                .stream()
                .map(node -> node.neighbors)
                .forEach(Map::clear);
                        
        this.nodes.clear();        
    }        
  

    /**
     * Adds an edge between two components. This should be used if selecting by
     * directional input device is possible.
     *
     * @param type how the nodes are connected
     * @param from the start position of the connection.
     * @param to the end position of the connection.
     * @param direction the direction of the connection.
     * @since 15.08.21
     */
    public void addEdge(final EdgeType type,
            final GLUIComponent from, final GLUIComponent to,
            final GLVec2 direction) {

        Objects.requireNonNull(type, "Edge type cannot be null!");

        final Node nFrom = this.nodes.containsKey(from) ? this.nodes.get(from) : new Node(from);
        final Node nTo = this.nodes.containsKey(to) ? this.nodes.get(to) : new Node(to);
        final GLVec2D dir = direction.asGLVec2D().normalize();

        switch (type) {
            case BI_DIRECTIONAL:
                nFrom.neighbors.put(dir.asStaticVec(), nTo);
                nTo.neighbors.put(dir.negative().asStaticVec(), nFrom);
                break;
            case MONO_DIRECTIONAL:
                nFrom.neighbors.put(dir.asStaticVec(), nTo);
        }
    }    

    /**
     * Moves the selector in the given direction.
     *
     * @param direction the direction to move the selector.
     * @since 15.08.21
     */
    public void moveSelector(final GLVec2 direction) {
        final GLVec2D nDir = direction.asGLVec2D().normalize().asStaticVec();

        double closestDistance = Double.POSITIVE_INFINITY;
        Node closestNeighbor = null;

        for (GLVec2D vec : this.currentSelected.neighbors.keySet()) {
            final double distance = vec.minus(nDir).length();

            if (distance < closestDistance) {
                closestNeighbor = this.currentSelected.neighbors.get(vec);
                closestDistance = distance;
            }
        }

        if (closestNeighbor != null) {
            if(this.currentSelected.component != null) {
                this.currentSelected.component.deselect();
            }            
                        
            if (closestNeighbor.component != null) {
                closestNeighbor.component.select();
            }
            
            this.currentSelected = closestNeighbor;
        }
    }

    /**
     * Selects the current menu item.
     *
     * @since 15.08.21
     */
    public void select() {
        if (this.currentSelected.component != null) {
            this.currentSelected.component.execute();
        }
    }

    private class Node {

        final GLUIComponent component;
        final Map<GLVec2D, Node> neighbors = new HashMap<>();

        Node(GLUIComponent component) {
            this.component = component;
        }
    }

    /**
     * Type of edge connection
     *
     * @since 15.08.21
     */
    public enum EdgeType {

        /**
         * Movement is only allowed in one direction between nodes.
         *
         * @since 15.08.21
         */
        MONO_DIRECTIONAL,
        /**
         * Movement is allowed in both direction between nodes.
         *
         * @since 15.08.21
         */
        BI_DIRECTIONAL
    }
}
