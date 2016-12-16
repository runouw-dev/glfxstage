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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A collection of glyphs. Used to indicate to GLFont which characters should be
 * included.
 *
 * @author Robert
 * @author zmichaels - Restructured as persistent object
 * @since 16.12.16
 */
public final class GLFontGlyphSet implements Iterable<Character> {

    /**
     * The GLFontGlyphSet that includes all printable characters included in the
     * ASCII set.
     *
     * @since 16.12.16
     */
    public static final GLFontGlyphSet ASCII = new GLFontGlyphSet().withRangeClosed(' ', '~');

    /**
     * The GLFontGlyphSet that includes all printable characters included in the
     * Extended ASCII set.
     *
     * @since 16.12.16
     */
    public static final GLFontGlyphSet EXTENDED_ASCII = ASCII.withRangeClosed((char) 128, (char) 254);

    /**
     * The list of supported characters (in order). Adding or removing elements
     * from this list is prohibited.
     *
     * @since 16.12.16
     */
    public final List<Character> supported;

    /**
     * Constructs a new GLFontGlyphSet from the sequence of characters.
     *
     * @param chars the sequence of characters.
     * @since 16.12.16
     */
    public GLFontGlyphSet(final Character... chars) {
        this(Arrays.asList(chars));
    }

    /**
     * Constructs a GLFontGlyphSet from a collection of Characters.
     *
     * @param chars the collection of characters.
     * @since 16.12.16
     */
    public GLFontGlyphSet(final Collection<Character> chars) {
        this.supported = Collections.unmodifiableList(chars.stream()
                .distinct()
                .sorted(Character::compare)
                .collect(Collectors.toList()));
    }

    /**
     * Constructs a new GLFontGlyphSet that will include the supplied range of
     * characters.
     *
     * @param startInclusive the character to start on.
     * @param endExclusive the character to end at (exclusive)
     * @return the new GLFontGlyphSet.
     * @since 16.12.16
     */
    public GLFontGlyphSet withRange(final char startInclusive, final char endExclusive) {
        final Set<Character> range = new HashSet<>();

        for (char c = startInclusive; c < endExclusive; c++) {
            range.add(c);
        }

        return withCharacters(range);
    }

    /**
     * Constructs a new GLFontGlyphSet that will include the supplied closed
     * range of characters.
     *
     * @param startInclusive the character to start on.
     * @param endInclusive the character to end at (inclusive)
     * @return the new GLFontGlyphSet.
     * @since 16.12.16
     */
    public GLFontGlyphSet withRangeClosed(final char startInclusive, final char endInclusive) {
        final Set<Character> range = new HashSet<>();

        for (char c = startInclusive; c <= endInclusive; c++) {
            range.add(c);
        }

        return withCharacters(range);
    }

    /**
     * Constructs a new GLFontGlyphSet that will include all of the characters
     * in the Collection.
     *
     * @param chars the characters to add to the GLFontGlyphSet.
     * @return the new GLFontGlyphSet.
     * @since 16.12.16
     */
    public GLFontGlyphSet withCharacters(final Collection<Character> chars) {
        final Set<Character> newSupported = new HashSet<>(this.supported);

        newSupported.addAll(chars);

        return new GLFontGlyphSet(newSupported);
    }

    /**
     * Constructs a new GLFontGlyphSet that will include all of the characters
     * in the CharSequence.
     *
     * @param chars the String of characters to include
     * @return the new GLFontGlyphSet.
     * @since 16.12.16
     */
    public GLFontGlyphSet withCharacters(final CharSequence chars) {
        final Set<Character> newSupported = new HashSet<>(this.supported);
        final char[] cArr = chars.toString().toCharArray();

        for (int i = 0; i < cArr.length; i++) {
            newSupported.add(cArr[i]);
        }

        return new GLFontGlyphSet(newSupported);
    }

    /**
     * Constructs a new GLFontGlyphSet that will include all of the characters
     * in the sequence.
     *
     * @param chars the characters to add to the GLFontGlyphSet.
     * @return the new GLFontGlyphSet.
     * @since 16.12.16
     */
    public GLFontGlyphSet withCharacters(final Character... chars) {
        return withCharacters(Arrays.asList(chars));
    }

    /**
     * Retrieves the number of characters in the GLFontGlyphSet.
     *
     * @return the number of characters.
     * @since 16.12.16
     */
    public int size() {
        return supported.size();
    }

    /**
     * Checks if the GLFontGlyphSet contains the specified character.
     *
     * @param o the character to check for.
     * @return true if the character is included in the GLFontGlyphSet.
     * @since 16.12.16
     */
    public boolean contains(char o) {
        return supported.contains(o);
    }

    /**
     * Retrieves the index of the supported character.
     *
     * @param o the character to find.
     * @return the index of the character if it is included in the
     * GLFontGlyphSet. An empty OptionalInt will be returned if the character is
     * not included in the set.
     * @since 16.12.16
     */
    public OptionalInt indexOf(char o) {
        final int index = this.supported.indexOf(o);

        if (index == -1) {
            return OptionalInt.empty();
        } else {
            return OptionalInt.of(index);
        }
    }

    /**
     * Retrieves the character at the specified index.
     *
     * @param i the character's index to fetch.
     * @return the character.
     * @throws IndexOutOfBoundsException if the index exceeds the bounds of the
     * GLFontGlyphSet.
     * @since 16.12.16
     */
    public char get(int i) {
        return supported.get(i);
    }

    @Override
    public Iterator<Character> iterator() {
        return this.supported.iterator();
    }
}
