/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
            throw new Error("Cannot edit unmodifiable GLFontGlpyhSet!");
        }

        @Override
        public void add(char c) {
            throw new Error("Cannot edit unmodifiable GLFontGlpyhSet!");
        }
    }
}
