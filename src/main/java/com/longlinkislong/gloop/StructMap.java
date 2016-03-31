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
public final class StructMap implements Map<String, Number> {

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
    public Set<Entry<String, Number>> entrySet() {
        return this.def.elements.keySet().stream()
                .map(key -> new Entry<String, Number>() {
                    @Override
                    public String getKey() {
                        return key;
                    }

                    @Override
                    public Number getValue() {
                        return StructMap.this.get(key);
                    }

                    @Override
                    public Number setValue(Number value) {
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
    public Number put(String key, Number value) {
        final StructDef.Element e = this.def.elements.get(key);

        if (e == null) {
            throw new IllegalArgumentException("Struct definition does not contain key: " + key + "!");
        } else {
            final Number prevValue;
            final long p = this.ptr + e.offset;

            switch (e.type) {
                case BYTE:
                    prevValue = MemoryUtil.memGetByte(p);
                    MemoryUtil.memPutByte(p, value.byteValue());
                    break;
                case SHORT:
                    prevValue = MemoryUtil.memGetShort(p);
                    MemoryUtil.memPutShort(p, value.shortValue());
                    break;
                case INT:
                    prevValue = MemoryUtil.memGetInt(p);
                    MemoryUtil.memPutInt(p, value.intValue());
                    break;
                case LONG:
                    prevValue = MemoryUtil.memGetLong(p);
                    MemoryUtil.memPutLong(p, value.longValue());
                    break;
                case FLOAT:
                    prevValue = MemoryUtil.memGetFloat(p);
                    MemoryUtil.memPutFloat(p, value.floatValue());
                    break;
                case DOUBLE:
                    prevValue = MemoryUtil.memGetDouble(p);
                    MemoryUtil.memPutDouble(p, value.doubleValue());
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown element type: " + e.type + "!");
            }

            return prevValue;
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends Number> m) {
        m.forEach(this::put);
    }

    @Override
    public Number remove(Object key) {
        final StructDef.Element e = this.def.elements.get((String) key);

        if (e == null) {
            return null;
        } else {
            final long p = this.ptr + e.offset;
            final Number value;

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
    public Collection<Number> values() {
        return this.def.elements.keySet().stream()
                .map(this::get)
                .collect(Collectors.toList());
    }

}
