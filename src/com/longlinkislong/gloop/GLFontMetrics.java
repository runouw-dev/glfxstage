/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

/**
 *
 * @author zmichaels
 */
public interface GLFontMetrics {
    public float getCharWidth(char c);
    public float getMaxHeight();
    public float getMaxWidth();
}
