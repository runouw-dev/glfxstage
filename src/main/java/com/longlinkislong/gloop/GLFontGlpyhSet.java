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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 * @author Robert
 */
public class GLFontGlpyhSet {
    public static GLFontGlpyhSet DEFAULT_GLYPH_SET = new GLFontGlpyhSet(' ', '~').asUnmodifiable();
    
    protected final List<Character> supported = new ArrayList<>();
    protected final List<Character> supportedReadOnly = Collections.unmodifiableList(supported);
    protected final Map<Character, Integer> supportedIndices = new HashMap<>();
    
    public GLFontGlpyhSet() {
        
    }
    public GLFontGlpyhSet(char c, char d) {
        addRange(c, d);
    }
    
    public void add(char c){
        if(!supported.contains(c)){
            supportedIndices.put(c, supported.size());
            supported.add(c);
        }
    }
    public void addRange(char c, char d){
        for(int i=(int)c;i<=(int)d;i++){
            add((char) i);
        }
    }
    public void add(CharSequence str){
        for(int i=0;i<str.length();i++){
            add(str.charAt(i));
        }
    }
    
    public List<Character> getAllSupported(){
        return supportedReadOnly;
    }
    
    
    public GLFontGlpyhSet asUnmodifiable(){
        return new UnmodifiableGLFontGlpyhSet(this);
    }
    
    // delagated methods for set
    public int size() {
        return supported.size();
    }

    public boolean contains(char o) {
        return supported.contains(o);
    }
    
    public int indexOf(char o) {
        return supportedIndices.get(o);
    }
    
    public char get(int i) {
        return supported.get(i);
    }

    public void forEach(Consumer<? super Character> action) {
        supported.forEach(action);
    }
    
    
    public static class UnmodifiableGLFontGlpyhSet extends GLFontGlpyhSet{
        public UnmodifiableGLFontGlpyhSet(GLFontGlpyhSet other) {
            other.forEach(super::add);
        }

        @Override
        public GLFontGlpyhSet asUnmodifiable() {
            return this;
        }

        @Override
        public void addRange(char c, char d) {
            throw new UnsupportedOperationException("Cannot edit unmodifiable GLFontGlpyhSet!");
        }

        @Override
        public void add(char c) {
            throw new UnsupportedOperationException("Cannot edit unmodifiable GLFontGlpyhSet!");
        }
    }
}
