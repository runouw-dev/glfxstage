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
