package com.example.concurrencypractice;

import com.example.concurrencypractice.lock.redisson.LockTestService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class ConcurrencyPracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConcurrencyPracticeApplication.class, args);
    }

    @Bean
    CommandLineRunner run(LockTestService lockTestService) {
        return args -> {
            String clientName = System.getProperty("client.name", "JVM-Default");
            boolean useLock = Boolean.parseBoolean(System.getProperty("use.lock", "true")); // 락 사용 여부
            int threadCount = 4; // 각 JVM 마다 4개의 스레드

            lockTestService.incrementJvmCount();

            try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
                for (int i = 1; i <= threadCount; i++) {
                    String threadName = "Thread-" + i;
                    executor.submit(() -> lockTestService.accessSharedResource(clientName, threadName, useLock));
                }
            }
            System.out.println("✅ [" + clientName + "] 최종 공유 자원 값: " + lockTestService.getSharedResourceValue());
            lockTestService.decrementJvmCountAndCleanup();
        };
    }
}