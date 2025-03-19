package com.example.concurrencypractice;

import com.example.concurrencypractice.redisson.LockTestService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class ConcurrencyPracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConcurrencyPracticeApplication.class, args);
    }

    @Bean
    CommandLineRunner run(LockTestService lockTestService) {
        return args -> {
            int threadCount = 8;

            try (ExecutorService executor = Executors.newFixedThreadPool(5)) {
                for (int i = 1; i <= threadCount; i++) {
                    String threadName = "Thread-" + i;
                    executor.submit(() -> lockTestService.accessSharedResource(threadName));
                }
            }
            System.out.println("✅ 최종 공유 자원 값: " + lockTestService.getSharedResourceValue());
        };
    }
}
