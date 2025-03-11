package com.example.concurrencypractice.thread;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncService {

    @Async
    public void asyncTask() {
        System.out.println("Running on thread: " + Thread.currentThread().getName());
    }
}
