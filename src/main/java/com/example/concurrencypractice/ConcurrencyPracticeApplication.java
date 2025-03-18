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
            int threadCount = 5;

            // try-with-resources로 ExecutorService 관리
            try (ExecutorService executor = Executors.newFixedThreadPool(5)) {
                // 각 스레드가 공유 자원에 접근 시도
                for (int i = 1; i <= threadCount; i++) {
                    String threadName = "Thread-" + i;
                    executor.submit(() -> lockTestService.accessSharedResource(threadName));
                }
                // 모든 작업이 끝날 때까지 대기 (try 블록 끝나면 자동 종료)
            } // executor.close()가 자동 호출됨

            System.out.println("테스트 완료!");
        };
    }
}
