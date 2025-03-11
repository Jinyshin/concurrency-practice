package com.example.concurrencypractice.thread;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ThreadTestController {

    @GetMapping("/thread")
    public String checkThread() {
        String threadName = Thread.currentThread().getName();
        AsyncService service = new AsyncService();
        service.asyncTask();
        return "Current Thread: " + threadName;
    }
}
