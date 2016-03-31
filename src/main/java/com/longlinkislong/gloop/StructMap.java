/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.lwjgl.system.MemoryUtil;

/**
 * An implementation of Map for the intended purpose of accessing data from a
 * c-struct.
 *
 * @author zmichaels
 * @since 16.03.31
 */
public final class StructMap implements Map<String, Object> {

    private final long ptr;
    private final StructDef def;

    /**
     * Maps an array of StructMap objects to a pointer. Each subsequent
     * StructMap accesses the data following the previous. This mirrors an array
     * of structs in c.
     *
     * @param ptr the pointer to map.
     * @param def the StructDef to use for the entire array.
     * @param count the number of elements in the array.
     * @return the array of StructMaps.
     * @since 16.03.31
     */
    public static StructMap[] map(final long ptr, final StructDef def, final int count) {
        final StructMap[] out = new StructMap[count];

        for (int i = 0; i < count; i++) {
            out[i] = new StructMap(ptr + def.size, def);
        }

        return out;
    }

    /**
     * Constructs a new StructMap by wrapping a pointer. This mirrors a pointer
     * to a struct in c.
     *
     * @param ptr the pointer to wrap.
     * @param def the StructDef.
     * @since 16.03.31
     */
    public StructMap(final long ptr, final StructDef def) {
        this.ptr = ptr;
        this.def = Objects.requireNonNull(def);
    }

    @Override
    public void clear() {
        MemoryUtil.memSet(this.ptr, 0, this.def.size);
    }

    @Override
    public boolean containsKey(Object key) {
        return this.def.elements.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.def.elements.values().stream()
                .map(this::get)
                .filter(v -> v.equals(value))
                .findAny().isPresent();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.def.elements.keySet().stream()
                .map(key -> new Entry<String, Object>() {
                    @Override
                    public String getKey() {
                        return key;
                    }

                    @Override
                    public Object getValue() {
                        return StructMap.this.get(key);
                    }

                    @Override
                    public Object setValue(Object value) {
                        return StructMap.this.put(key, value);
                    }
                })
                .collect(Collectors.toSet());
    }

    @Override
    public Number get(final Object key) {
        final StructDef.Element e = this.def.elements.get(key);

        if (e == null) {
            return null;
        } else {
            final long p = this.ptr + e.offset;

            switch (e.type) {
                case BYTE:
                    return MemoryUtil.memGetByte(p);
                case SHORT:
                    return MemoryUtil.memGetShort(p);
                case INT:
                    return MemoryUtil.memGetInt(p);
                case LONG:
                    return MemoryUtil.memGetLong(p);
                case FLOAT:
                    return MemoryUtil.memGetFloat(p);
                case DOUBLE:
                    return MemoryUtil.memGetDouble(p);
                default:
                    return null;
            }
        }
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < this.def.size; i++) {
            if (MemoryUtil.memGetByte(this.ptr + i) != 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Set<String> keySet() {
        return this.def.elements.keySet();
    }

    @Override
    public Object put(String key, Object value) {
        final StructDef.Element e = this.def.elements.get(key);

        if (e == null) {
            throw new IllegalArgumentException("Struct definition does not contain key: " + key + "!");
        } else {
            final Object prevValue;
            final long p = this.ptr + e.offset;

            switch (e.type) {
                case BYTE:
                    prevValue = MemoryUtil.memGetByte(p);
                    MemoryUtil.memPutByte(p, (byte) value);
                    break;
                case SHORT:
                    prevValue = MemoryUtil.memGetShort(p);
                    MemoryUtil.memPutShort(p, (short) value);
                    break;
                case INT:
                    prevValue = MemoryUtil.memGetInt(p);
                    MemoryUtil.memPutInt(p, (int) value);
                    break;
                case LONG:
                    prevValue = MemoryUtil.memGetLong(p);
                    MemoryUtil.memPutLong(p, (long) value);
                    break;
                case FLOAT:
                    prevValue = MemoryUtil.memGetFloat(p);
                    MemoryUtil.memPutFloat(p, (float) value);
                    break;
                case DOUBLE:
                    prevValue = MemoryUtil.memGetDouble(p);
                    MemoryUtil.memPutDouble(p, (double) value);
                    break;
                case VEC2F: {
                    final GLVec2F ov = GLVec2F.create();
                    final int sz = ov.size();
                    
                    for(int i = 0; i < sz; i++) {
                        ov.data()[ov.offset() + i] = MemoryUtil.memGetFloat(p + Float.BYTES * i);
                    }
                    
                    prevValue = ov;

                    final GLVec2F v = ((GLVec2) value).asGLVec2F();

                    for(int i = 0; i < sz; i++) {
                        MemoryUtil.memPutFloat(p + Float.BYTES * i, v.data()[v.offset() + i]);
                    }
                }
                break;
                case VEC3F: {
                    final GLVec3F ov = GLVec3F.create();
                    final int sz = ov.size();
                    
                    for(int i = 0; i < sz; i++) {
                        ov.data()[ov.offset() + i] = MemoryUtil.memGetFloat(p + Float.BYTES * i);
                    }
                    
                    prevValue = ov;

                    final GLVec3F v = ((GLVec3) value).asGLVec3F();

                    for(int i = 0; i < sz; i++) {
                        MemoryUtil.memPutFloat(p + Float.BYTES * i, v.data()[v.offset() + i]);
                    }
                }
                break;
                case VEC4F: {
                    final GLVec4F ov = GLVec4F.create();
                    final int sz = ov.size();
                    
                    for(int i = 0; i < sz; i++) {
                        ov.data()[ov.offset() + i] = MemoryUtil.memGetFloat(p + Float.BYTES * i);
                    }
                    
                    prevValue = ov;

                    final GLVec4F v = ((GLVec4) value).asGLVec4F();

                    for(int i = 0; i < sz; i++) {
                        MemoryUtil.memPutFloat(p + Float.BYTES * i, v.data()[v.offset() + i]);
                    }
                }
                break;
                case MAT2F: {
                    final GLMat2F old = GLMat2F.create();
                    final int sz = old.size() * old.size();

                    for (int i = 0; i < sz; i++) {
                        old.data()[old.offset() + i] = MemoryUtil.memGetFloat(p + Float.BYTES * i);
                    }

                    prevValue = old;

                    final GLMat2F m = ((GLMat2) value).asGLMat2F();

                    for (int i = 0; i < sz; i++) {
                        MemoryUtil.memPutFloat(p + Float.BYTES * i, m.data()[m.offset() + i]);
                    }
                }
                break;
                case MAT3F: {
                    final GLMat3F old = GLMat3F.create();
                    final int sz = old.size() * old.size();

                    for (int i = 0; i < sz; i++) {
                        old.data()[old.offset() + i] = MemoryUtil.memGetFloat(p + Float.BYTES * i);
                    }

                    prevValue = old;

                    final GLMat3F m = ((GLMat3) value).asGLMat3F();

                    for (int i = 0; i < sz; i++) {
                        MemoryUtil.memPutFloat(p + Float.BYTES * i, m.data()[m.offset() + i]);
                    }
                }
                break;
                case MAT4F: {
                    final GLMat4F old = GLMat4F.create();
                    final int sz = old.size() * old.size();

                    for (int i = 0; i < sz; i++) {
                        old.data()[old.offset() + i] = MemoryUtil.memGetFloat(p + Float.BYTES * i);
                    }

                    prevValue = old;

                    final GLMat4F m = ((GLMat4) value).asGLMat4F();

                    for (int i = 0; i < sz; i++) {
                        MemoryUtil.memPutFloat(p + Float.BYTES * i, m.data()[m.offset() + i]);
                    }
                }
                break;
                default:
                    throw new UnsupportedOperationException("Unknown element type: " + e.type + "!");
            }

            return prevValue;
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        m.forEach(this::put);
    }

    @Override
    public Object remove(Object key) {
        final StructDef.Element e = this.def.elements.get((String) key);

        if (e == null) {
            return null;
        } else {
            final long p = this.ptr + e.offset;
            final Object value;

            switch (e.type) {
                case BYTE:
                    value = MemoryUtil.memGetByte(p);
                    break;
                case SHORT:
                    value = MemoryUtil.memGetShort(p);
                    break;
                case INT:
                    value = MemoryUtil.memGetInt(p);
                    break;
                case LONG:
                    value = MemoryUtil.memGetLong(p);
                    break;
                case FLOAT:
                    value = MemoryUtil.memGetFloat(p);
                    break;
                case DOUBLE:
                    value = MemoryUtil.memGetDouble(p);
                    break;
                case VEC2F: {
                    final GLVec2F v = GLVec2F.create();

                    for (int i = 0; i < v.size(); i++) {
                        v.data()[v.offset() + i] = MemoryUtil.memGetFloat(p + Float.BYTES * i);
                    }

                    value = v;
                }
                break;
                case VEC3F: {
                    final GLVec3F v = GLVec3F.create();

                    for (int i = 0; i < v.size(); i++) {
                        v.data()[v.offset() + i] = MemoryUtil.memGetFloat(p + Float.BYTES * i);
                    }

                    value = v;
                }
                break;
                case VEC4F: {
                    final GLVec4F v = GLVec4F.create();

                    for (int i = 0; i < v.size(); i++) {
                        v.data()[v.offset() + i] = MemoryUtil.memGetFloat(p + Float.BYTES * i);
                    }

                    value = v;
                }
                break;
                case MAT2F: {
                    final GLMat2F m = GLMat2F.create();
                    final int sz = m.size() * m.size();
                    
                    for(int i = 0; i < sz; i++) {
                        m.data()[m.offset() + i] = MemoryUtil.memGetFloat(p + Float.BYTES * i);
                    }
                                        
                    value = m;
                }
                break;
                case MAT3F: {
                    final GLMat3F m = GLMat3F.create();
                    final int sz = m.size() * m.size();
                    
                    for(int i = 0; i < sz; i++) {
                        m.data()[m.offset() + i] = MemoryUtil.memGetFloat(p + Float.BYTES * i);
                    }
                                        
                    value = m;
                }
                break;
                case MAT4F: {
                    final GLMat4F m = GLMat4F.create();
                    final int sz = m.size() * m.size();
                    
                    for(int i = 0; i < sz; i++) {
                        m.data()[m.offset() + i] = MemoryUtil.memGetFloat(p + Float.BYTES * i);
                    }
                                        
                    value = m;
                }
                break;

                default:
                    throw new UnsupportedOperationException("Unsupported element type: " + e.type + "!");
            }

            MemoryUtil.memSet(p, 0, e.type.width);

            return value;
        }
    }

    @Override
    public int size() {
        return this.def.elements.size();
    }

    @Override
    public Collection<Object> values() {
        return this.def.elements.keySet().stream()
                .map(this::get)
                .collect(Collectors.toList());
    }

}
