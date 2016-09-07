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
package com.longlinkislong.gloop;

/**
 * Element types supported by StructMap and StructDef.
 *
 * @author zmichaels
 * @since 16.03.31
 */
public enum StructElementType {
    BYTE(Byte.BYTES) {
        @Override
        public GLVertexAttributeSize getVertexSize() {
            return GLVertexAttributeSize.BYTE;
        }

        @Override
        public GLVertexAttributeType getVertexType() {
            return GLVertexAttributeType.GL_BYTE;
        }
    },
    SHORT(Short.BYTES) {
        @Override
        public GLVertexAttributeSize getVertexSize() {
            return GLVertexAttributeSize.SHORT;
        }

        @Override
        public GLVertexAttributeType getVertexType() {
            return GLVertexAttributeType.GL_SHORT;
        }
    },
    INT(Integer.BYTES) {
        @Override
        public GLVertexAttributeSize getVertexSize() {
            return GLVertexAttributeSize.INT;
        }

        @Override
        public GLVertexAttributeType getVertexType() {
            return GLVertexAttributeType.GL_INT;
        }
    },
    LONG(Long.BYTES) {
        @Override
        public GLVertexAttributeSize getVertexSize() {
            throw new UnsupportedOperationException("No compatible size!");
        }

        @Override
        public GLVertexAttributeType getVertexType() {
            throw new UnsupportedOperationException("No compatible type!");
        }
    },
    FLOAT(Float.BYTES) {
        @Override
        public GLVertexAttributeSize getVertexSize() {
            return GLVertexAttributeSize.FLOAT;
        }

        @Override
        public GLVertexAttributeType getVertexType() {
            return GLVertexAttributeType.GL_FLOAT;
        }
    },
    DOUBLE(Double.BYTES) {
        @Override
        public GLVertexAttributeSize getVertexSize() {
            throw new UnsupportedOperationException("No compatible size!");
        }
        @Override
        public GLVertexAttributeType getVertexType() {
            return GLVertexAttributeType.GL_DOUBLE;
        }
    },
    VEC2F(GLVec2F.VECTOR_WIDTH) {
        @Override
        public GLVertexAttributeSize getVertexSize() {
            return GLVertexAttributeSize.VEC2;
        }
        @Override
        public GLVertexAttributeType getVertexType() {
            return GLVertexAttributeType.GL_FLOAT;
        }
    },
    VEC3F(GLVec3F.VECTOR_WIDTH) {
        @Override
        public GLVertexAttributeSize getVertexSize() {
            return GLVertexAttributeSize.VEC3;
        }
        @Override
        public GLVertexAttributeType getVertexType() {
            return GLVertexAttributeType.GL_FLOAT;
        }
    },
    VEC4F(GLVec4F.VECTOR_WIDTH) {
        @Override
        public GLVertexAttributeSize getVertexSize() {
            return GLVertexAttributeSize.VEC4;
        }
        @Override
        public GLVertexAttributeType getVertexType() {
            return GLVertexAttributeType.GL_FLOAT;
        }
    },
    MAT2F(GLMat2F.MATRIX_WIDTH) {
        @Override
        public GLVertexAttributeSize getVertexSize() {
            throw new UnsupportedOperationException("No compatible size!");
        }
        @Override
        public GLVertexAttributeType getVertexType() {
            return GLVertexAttributeType.GL_FLOAT;
        }
    },
    MAT3F(GLMat3F.MATRIX_WIDTH) {
        @Override
        public GLVertexAttributeSize getVertexSize() {
            throw new UnsupportedOperationException("No compatible size!");
        }
        @Override
        public GLVertexAttributeType getVertexType() {
            return GLVertexAttributeType.GL_FLOAT;
        }
    },
    MAT4F(GLMat4F.MATRIX_WIDTH) {
        @Override
        public GLVertexAttributeSize getVertexSize() {
            throw new UnsupportedOperationException("No compatible size!");
        }
        @Override
        public GLVertexAttributeType getVertexType() {
            return GLVertexAttributeType.GL_FLOAT;
        }
    };

    public int width;

    StructElementType(int size) {
        this.width = size;
    }

    public abstract GLVertexAttributeType getVertexType();

    public abstract GLVertexAttributeSize getVertexSize();
}
