/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An immutable definition of a c-struct composed of primitive objects.
 *
 * @author zmichaels
 * @since 16.03.31
 */
public final class StructDef {

    static final class Element {

        final int offset;
        final StructElementType type;

        Element(final int offset, final StructElementType type) {
            this.offset = offset;
            this.type = type;
        }
    }

    final int size;
    final Map<String, Element> elements;

    /**
     * Constructs a new empty StructDef object.
     *
     * @since 16.03.31
     */
    public StructDef() {
        this.elements = new HashMap<>();
        this.size = 0;
    }

    private StructDef(StructDef oldDef, final String name, final StructElementType newElement) {
        this.elements = new HashMap<>(oldDef.elements);
        this.elements.put(name, new Element(oldDef.size, newElement));
        this.size = oldDef.size + newElement.width;
    }

    /**
     * Creates a new StructDef object by appending the new element type to the
     * end.
     *
     * @param name the name of the new element. This is needed for access.
     * @param type the type of the element.
     * @return the new StructDef.
     * @since 16.03.31
     */
    public StructDef withElement(final CharSequence name, final StructElementType type) {
        return new StructDef(this, name.toString(), Objects.requireNonNull(type));
    }
}
