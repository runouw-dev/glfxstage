/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

/**
 * Element types supported by StructMap and StructDef.
 *
 * @author zmichaels
 * @since 16.03.31
 */
public enum StructElementType {
    BYTE(Byte.SIZE),
    SHORT(Short.SIZE),
    INT(Integer.SIZE),
    LONG(Long.SIZE),
    FLOAT(Float.SIZE),
    DOUBLE(Double.SIZE);

    public int width;

    StructElementType(int size) {
        this.width = size;
    }
}
