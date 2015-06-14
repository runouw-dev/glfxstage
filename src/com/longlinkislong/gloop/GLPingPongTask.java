/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop;

import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 *
 * @author zmichaels
 */
public class GLPingPongTask extends GLTask {

    private Optional<GLTask> front = Optional.empty();
    private Optional<GLTask> back = Optional.empty();
    private final Semaphore semaphore = new Semaphore(1);

    public GLPingPongTask() {
    }

    public GLPingPongTask(final GLTask front, final GLTask back) {
        this.front = Optional.ofNullable(front);
        this.back = Optional.ofNullable(back);
    }

    public void setBackTask(final GLTask task) {
        this.back = Optional.ofNullable(task);
    }

    public void swap() {
        try {
            this.semaphore.acquire();
            final Optional<GLTask> temp = this.front;

            this.front = this.back;
            this.back = temp;
            this.semaphore.release();
        } catch (InterruptedException iex) {
            throw new GLException("Unable to swap instructions!", iex);
        }
    }

    @Override
    public void run() {
        try {
            this.semaphore.acquire();
            this.front.ifPresent(Runnable::run);
            this.semaphore.release();
        } catch (InterruptedException iex) {
            throw new GLException("Unable get lock!", iex);
        }
    }

}
