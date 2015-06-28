/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A data structure that manages two instances of a type for safe read/write.
 * For proper use, it is recommended to do all write functions to the back
 * object and then prior to read, swap the objects and read from the front.
 *
 * @author zmichaels
 * @param <T> the type
 */
public class PingPong<T> {

    private T front;
    private T back;

    /**
     * Applies the function to the front object owned by two PingPongs.
     *
     * @param <U> the type of the first PingPong object.
     * @param <V> the type of the second PingPong object.
     * @param action the action to perform on both
     * @param a the first PingPong object
     * @param b the second PingPong object
     * @since 15.05.26
     */
    public static <U, V> void applyFront(
            final BiConsumer<? super U, ? super V> action,
            final PingPong<U> a, final PingPong<V> b) {

        action.accept(a.front, b.front);
    }

    /**
     * Applies the function to the back object owned by two PingPongs.
     *
     * @param <U> the type of the first PingPong object
     * @param <V> the type of the second PingPong object
     * @param action the action to perform
     * @param a the first PingPong object
     * @param b the second PingPong object
     * @since 15.05.26
     */
    public static <U, V> void applyBack(
            final BiConsumer<? super U, ? super V> action,
            final PingPong<U> a, final PingPong<V> b) {

        action.accept(a.front, b.back);
    }

    /**
     * Applies a function to both the front and back of two PingPong objects
     *
     * @param <U> the type of the first PingPong object
     * @param <V> the type of the second PingPong object
     * @param action the action to perform
     * @param a the first PingPong object
     * @param b the second PingPong object
     * @since 15.05.26
     */
    public static <U, V> void applyBoth(
            final BiConsumer<? super U, ? super V> action,
            final PingPong<U> a, final PingPong<V> b) {

        action.accept(a.front, b.front);
        action.accept(a.back, b.back);
    }

    /**
     * Applies a function to two PingPong objects with interleaved inputs.
     *
     * @param <U> the type of the first PingPong object
     * @param <V> the type of the second PingPong object
     * @param action the action to perform
     * @param a the first PingPong object
     * @param b the second PingPong object
     * @since 15.05.26
     */
    public static <U, V> void applyCrossover(
            final BiConsumer<? super U, ? super V> action,
            final PingPong<U> a, final PingPong<V> b) {

        action.accept(a.front, b.back);
        action.accept(a.back, b.front);
    }

    /**
     * Constructs a new PingPong object backed by the two supplied objects.
     * Usually these will be containers.
     *
     * @param front the front object
     * @param back the back object
     * @since 15.05.26
     */
    public PingPong(final T front, final T back) {
        this.front = front;
        this.back = back;
    }

    /**
     * Constructs a new PingPong object backed by two objects.
     *
     * @param constructor A function that supplies the front and back objects.
     * Usually this will be a constructor.
     * @since 15.0.28
     */
    public PingPong(final Supplier<T> constructor) {
        this.front = constructor.get();
        this.back = constructor.get();
    }

    /**
     * Retrieves the front object
     *
     * @return the front object
     * @since 15.05.26
     */
    public T front() {
        return this.front;
    }

    /**
     * Retrieves the back object
     *
     * @return the back object
     * @since 15.05.26
     */
    public T back() {
        return this.back;
    }

    /**
     * Applies a function to both the front and the back objects.
     *
     * @param action the action to perform
     * @since 15.05.26
     */
    public void applyBoth(final Consumer<? super T> action) {
        action.accept(this.front);
        action.accept(this.back);
    }

    /**
     * Applies a function to the front object
     *
     * @param action the action to perform
     * @since 15.05.26
     */
    public void applyFront(final Consumer<? super T> action) {
        action.accept(this.front);
    }

    /**
     * Applies a function to the back object
     *
     * @param action the action to perform
     * @since 15.05.26
     */
    public void applyBack(final Consumer<? super T> action) {
        action.accept(this.back);
    }

    /**
     * Swaps the front and the back objects
     *
     * @since 15.05.26
     */
    public void swap() {
        final T temp = this.front;

        this.front = this.back;
        this.back = temp;
    }
}
