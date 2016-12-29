/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 
 * @author zmichaels
 */
public final class TextureProxy {
    private final Supplier<GLTexture> constructor;
    private GLTexture instance;
    
    public TextureProxy(final Supplier<GLTexture> textureConstructor) {
        this.constructor = Objects.requireNonNull(textureConstructor);
    }
    
    public GLTexture get() {
        if (this.instance == null || !this.instance.isValid()) {
            this.instance = this.constructor.get();
        }
        
        return this.instance;
    }
}
