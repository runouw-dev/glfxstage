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
