package com.example.concurrencypractice;

import com.example.concurrencypractice.lock.redisson.LockTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@SpringBootApplication
public class ConcurrencyPracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConcurrencyPracticeApplication.class, args);
    }


    @Bean
    CommandLineRunner run(LockTestService lockTestService) {
        return args -> {
            String clientName = System.getProperty("client.name", "JVM-Default");
            int threadCount = 6;

            try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
                List<Future<?>> futures = new ArrayList<>();
                for (int i = 1; i <= threadCount; i++) {
                    String threadName = "Thread-" + i;
                    futures.add(executor.submit(() -> lockTestService.accessSharedResource(clientName, threadName)));
                }
                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (Exception e) {
                        log.error("스레드 실행 중 오류 발생: ", e);
                    }
                }
            }
            System.out.println("✅ [" + clientName + "] 최종 공유 자원 값: " + lockTestService.getSharedResourceValue());
        };
    }
}