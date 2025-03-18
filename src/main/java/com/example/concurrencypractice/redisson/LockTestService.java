package com.example.concurrencypractice.redisson;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LockTestService {
    private static final String NAMESPACE = "jiny";
    private final RedissonClient redissonClient;

    public LockTestService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void testLock() {
        String lockKey = NAMESPACE + ":lock:jinyLock";
        RLock lock = redissonClient.getLock(lockKey);

        try {
            log.info("락 획득 시도: {}", lockKey);
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                log.info("락 획득 성공: {}", lockKey);
                Thread.sleep(8000); // 8초 동안 가상의 작업 진행
                log.info("작업 완료");
            } else {
                log.warn("락 획득 실패: {}", lockKey);
            }
        } catch (InterruptedException e) {
            log.error("락 테스트 interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("락 해제: {}", lockKey);
            }
        }
    }
}