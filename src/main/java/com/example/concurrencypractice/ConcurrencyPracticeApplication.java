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
            String clientName = System.getProperty("client.name", "JVM-Default");
            int threadCount = 4; // 각 JVM 마다 4개의 스레드

            try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
                for (int i = 1; i <= threadCount; i++) {
                    String threadName = "Thread-" + i;
                    executor.submit(() -> lockTestService.accessSharedResource(clientName, threadName));
                }
            }
            System.out.println("✅ [" + clientName + "] 최종 공유 자원 값: " + lockTestService.getSharedResourceValue());
        };
    }
}
