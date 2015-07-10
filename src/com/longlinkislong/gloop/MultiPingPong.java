/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Supplier;

/**
 * MultiPingPong is a structure that is backed by multiple objects.
 *
 * @author zmichaels
 * @param <InnerType> the type of the object to back.
 * @since 15.07.10
 */
public class MultiPingPong<InnerType> {

    private final Queue<InnerType> objQueue;
    private Optional<Runnable> onQueueDepleted = Optional.empty();
    private int swapsUntilDepleted = 0;
    private final int size;

    /**
     * Constructs a new PingPongQueue
     *
     * @param count the number of objects to store in the queue.
     * @param constructor the constructor used for building all of the objects.
     * @since 15.07.10
     */
    public MultiPingPong(final int count, final Supplier<InnerType> constructor) {
        if ((this.size = count) < 2) {
            throw new IllegalStateException("PingPongQueue requires at least 2 inner objects!");
        }

        this.objQueue = new ArrayDeque(count);
        this.swapsUntilDepleted = count;

        for (int i = 0; i < count; i++) {
            switch (i) {
                case 0:
                    this.objQueue.offer(this.readObj = constructor.get());
                    break;
                case 1:
                    this.objQueue.offer(this.writeObj = constructor.get());
                    break;
                default:
                    this.objQueue.offer(constructor.get());
            }
        }

    }

    /**
     * Sets a callback to run when the queue has been depleted.
     *
     * @param callback the callback to add.
     * @since 15.07.10
     */
    public void setOnQueueDepletedCallback(final Runnable callback) {
        this.onQueueDepleted = Optional.ofNullable(callback);
    }

    private InnerType readObj;
    private InnerType writeObj;

    /**
     * Retrieves the object that should be read from.
     *
     * @return the read object.
     */
    public InnerType front() {
        return this.readObj;
    }

    /**
     * Retrieves the object that should be written to.
     *
     * @return the write object.
     * @since 15.07.10
     */
    public InnerType back() {
        return this.writeObj;
    }

    /**
     * Swaps the objects.
     *
     * @since 15.07.10
     */
    public void swap() {
        this.objQueue.offer(this.readObj); // put the read object at the back of the queue.        
        this.readObj = this.writeObj; // read object is now the last write object
        this.writeObj = this.objQueue.poll(); // get the next write object

        this.swapsUntilDepleted--;

        // run the queue depletion callback if the queue has been depleted.
        if (this.swapsUntilDepleted == 0) {
            this.onQueueDepleted.ifPresent(Runnable::run);
            this.swapsUntilDepleted = this.size;
        }
    }
}
