package com.example.concurrencypractice.redisson;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicDouble;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LockTestService {
    private static final String NAMESPACE = "jiny";
    private static final String LOCK_KEY = "jiny:lock:sharedResource";
    private static final String RESOURCE_KEY = "jiny:sharedResource";
    private final RedissonClient redissonClient;
    private final RAtomicLong sharedResource;

    public LockTestService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.sharedResource = redissonClient.getAtomicLong(RESOURCE_KEY);
    }

    public void accessSharedResource(String clientName, String threadName) {
        RLock lock = redissonClient.getLock(LOCK_KEY);
        String identifier = clientName + "-" + threadName;
        log.info("[{}] 락 획득 시도 중... (현재 공유자원 값: {})", identifier, sharedResource.get());
        try {
            // 최대 7초 대기, 락 유지 시간 10초
            boolean acquired = lock.tryLock(7, 10, TimeUnit.SECONDS);
            if (acquired) {
                log.info("[{}] 🔒 락 획득 성공! 공유 자원 작업 시작", identifier);

                // 공유 자원 작업 진행
                long before = sharedResource.get();
                sharedResource.incrementAndGet();
                long after = sharedResource.get();
                Thread.sleep(3000);

                log.info("[{}] 공유 자원 값 변경: {} → {}", identifier, before, after);
                log.info("[{}] 작업 완료", identifier);
            } else {
                log.warn("[{}] 락 획득 실패, 대기 시간 초과", identifier);
            }
        } catch (InterruptedException e) {
            log.error("[{}] 락 처리 중 인터럽트 발생", identifier, e);
            Thread.currentThread().interrupt();
        } finally {
            // 현재 스레드가 락을 가지고 있으면 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("[{}] 락 해제 완료", identifier);
            }
        }
    }

    public long getSharedResourceValue() {
        return sharedResource.get();
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