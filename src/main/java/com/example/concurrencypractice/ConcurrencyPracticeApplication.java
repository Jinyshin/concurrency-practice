package com.example.concurrencypractice;

import com.example.concurrencypractice.lock.redisson.RedissonFencedLockService;
import com.example.concurrencypractice.lock.redisson.BasicRedissonLockService;
import com.example.concurrencypractice.lock.redisson.RedissonSpinLockService;
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
    CommandLineRunner run(
            BasicRedissonLockService basicRedissonLockService,
            RedissonFencedLockService redissonFencedLockService,
            RedissonSpinLockService redissonSpinLockService
    ) {
        return args -> {
            String clientName = System.getProperty("client.name", "JVM-Default");
            String lockType = System.getProperty("lock.type", "basic");
            int threadCount = 4;

            if ("basic".equals(lockType)) {
                testLock(basicRedissonLockService, clientName, threadCount, "RedissonLock");
            } else if ("fenced".equals(lockType)) {
                testLock(redissonFencedLockService, clientName, threadCount, "RedissonFencedLock");
            } else if ("spin".equals(lockType)) {
                testLock(redissonSpinLockService, clientName, threadCount, "RedissonSpinLock");
            } else {
                log.warn("지원하지 않는 lock.type: {}", lockType);
                System.exit(1);
            }
        };
    }

    private void testLock(Object service, String clientName, int threadCount, String lockType) {
        long finalValue = 0L;
        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 1; i <= threadCount; i++) {
                String threadName = "Thread-" + i;
                if (service instanceof BasicRedissonLockService) {
                    futures.add(executor.submit(() -> ((BasicRedissonLockService) service).accessSharedResource(clientName, threadName)));
                } else if (service instanceof RedissonFencedLockService) {
                    futures.add(executor.submit(() -> ((RedissonFencedLockService) service).accessSharedResource(clientName, threadName)));
                } else if (service instanceof RedissonSpinLockService) {
                    futures.add(executor.submit(() -> ((RedissonSpinLockService) service).accessSharedResource(clientName, threadName)));
                }
            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    log.error("스레드 실행 중 오류 발생: ", e);
                }
            }
            
            
        }
        
        if (service instanceof BasicRedissonLockService) {
            finalValue = ((BasicRedissonLockService) service).getSharedResourceValue();
        } else if (service instanceof RedissonFencedLockService) {
            finalValue = ((RedissonFencedLockService) service).getSharedResourceValue();
        } else if (service instanceof RedissonSpinLockService) {
            finalValue = ((RedissonSpinLockService) service).getSharedResourceValue();
        }
        System.out.println("✅ [" + clientName + "] " + lockType + " 테스트 - 최종 공유 자원 값: " + finalValue);
        System.exit(0);
    }
}