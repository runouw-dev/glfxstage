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
    BYTE(Byte.BYTES),
    SHORT(Short.BYTES),
    INT(Integer.BYTES),
    LONG(Long.BYTES),
    FLOAT(Float.BYTES),
    DOUBLE(Double.BYTES),
    VEC2F(GLVec2F.VECTOR_WIDTH),
    VEC3F(GLVec3F.VECTOR_WIDTH),
    VEC4F(GLVec4F.VECTOR_WIDTH),
    MAT2F(GLMat2F.MATRIX_WIDTH),
    MAT3F(GLMat3F.MATRIX_WIDTH),
    MAT4F(GLMat4F.MATRIX_WIDTH);

    public int width;

    StructElementType(int size) {
        this.width = size;
    }
}
