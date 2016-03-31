/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

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

    final int offset;
    final int stride;
    final int size;
    final Map<String, Element> elements;

    /**
     * Constructs a new empty StructDef object.
     *
     * @since 16.03.31
     */
    public StructDef() {
        this.elements = new LinkedHashMap<>();
        this.size = 0;
        this.stride = 0;
        this.offset = 0;
    }

    private StructDef(final StructDef base, final int stride, final int offset) {
        this.elements = new LinkedHashMap<>(base.elements);
        this.size = base.size;
        this.stride = stride;
        this.offset = offset;
    }

    private StructDef(final StructDef base, final String name, final StructElementType newElement) {
        this.elements = new LinkedHashMap<>(base.elements);
        this.elements.put(name, new Element(base.size, newElement));
        this.size = base.size + newElement.width;
        this.stride = base.stride;
        this.offset = base.offset;
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

    public StructDef withStride(final int stride) {
        return new StructDef(this, stride, this.offset);
    }

    public StructDef withOffset(final int offset) {
        return new StructDef(this, this.stride, offset);
    }

    /**
     * Attaches a GLBuffer described by this StructDef to a GLVertexArray.
     *
     * @param vao the GLVertexArray.
     * @param data the GLBuffer to attach.
     * @return the attribute descriptor.
     * @since 16.03.31
     */
    public GLVertexAttributes attach(final GLVertexArray vao, final GLBuffer data) {
        return this.attach(vao, data, null);
    }

    /**
     * Attaches a GLBuffer described by this StructDef to a GLVertexArray.
     *
     * @param vao the GLVertexArray.
     * @param data the GLBuffer to attach.
     * @param attrib the GLVertexAttributes to copy from. This may be null.
     * @return the attributes to use with the GLVertexArray.
     * @since 16.03.31
     */
    public GLVertexAttributes attach(final GLVertexArray vao, final GLBuffer data, final GLVertexAttributes attrib) {
        final GLVertexAttributes out = new GLVertexAttributes();
        int index = 0;

        if (attrib != null) {
            out.nameMap.putAll(attrib.nameMap);
            out.feedbackVaryings.addAll(attrib.feedbackVaryings);

            index = out.nameMap.values().stream()
                    .mapToInt(i -> i)
                    .max()
                    .getAsInt();
        }

        for (Entry<String, Element> e : this.elements.entrySet()) {
            final String name = e.getKey();
            final StructElementType type = e.getValue().type;

            out.setAttribute(name, index);
            vao.attachBuffer(index, data, type.getVertexType(), type.getVertexSize(), this.stride, this.offset);
            index++;
        }

        return out;
    }
}
