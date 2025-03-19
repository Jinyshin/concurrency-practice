package com.example.concurrencypractice.redisson;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LockTestService {
    private static final String NAMESPACE = "jiny";
    private static final String LOCK_KEY = "jiny:lock:sharedResource";
    private static final String RESOURCE_KEY = "jiny:sharedResource";
    private static final String JVM_COUNT_KEY = "jiny:jvm:count";

    private final RedissonClient redissonClient;
    private final RAtomicLong sharedResource;
    private final RAtomicLong jvmCount;

    public LockTestService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.sharedResource = redissonClient.getAtomicLong(RESOURCE_KEY);
        this.jvmCount = redissonClient.getAtomicLong(JVM_COUNT_KEY);
        log.info("공유자원 초기겂: {}", sharedResource.get());
    }

    public void accessSharedResource(String clientName, String threadName, boolean useLock) {
        String identifier = clientName + "-" + threadName;
        log.info("[{}] 작업 시도 중... (현재 공유자원 값: {})", identifier, sharedResource.get());

        RLock lock = useLock ? redissonClient.getLock(LOCK_KEY) : null;
        boolean acquired = true; // 락 미사용시 기본값 true

        if (useLock) {
            try {
                log.info("[{}] 락 획득 시도 중...", identifier);
                acquired = lock.tryLock(7, 10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("[{}] 락 처리 중 인터럽트 발생", identifier, e);
                Thread.currentThread().interrupt();
                return;
            }
        }

        if (acquired) {
            try {
                if (useLock) {
                    log.info("[{}] 🔒 락 획득 성공! 공유 자원 작업 시작", identifier);
                } else {
                    log.info("[{}] 🔑 락 없이 공유 자원 작업 시작", identifier);
                }

                // 공유 자원 작업 진행
                long before = sharedResource.get();
                sharedResource.incrementAndGet();
                Thread.sleep(3000);
                long after = sharedResource.get();

                log.info("[{}] 공유 자원 값 변경: {} → {}", identifier, before, after);
                log.info("[{}] 작업 완료", identifier);

            } catch (InterruptedException e) {
                log.error("[{}] 락 처리 중 인터럽트 발생", identifier, e);
                Thread.currentThread().interrupt();
            } finally {
                // 현재 스레드가 락을 가지고 있으면 해제
                if (useLock && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.info("[{}] 락 해제 완료", identifier);
                }
            }
        } else {
            log.warn("[{}] 락 획득 실패, 대기 시간 초과", identifier);
        }
    }

    public long getSharedResourceValue() {
        return sharedResource.get();
    }

    public void incrementJvmCount() {
        jvmCount.incrementAndGet();
        log.info("JVM 카운트 증가: {}", jvmCount.get());
    }

    public void decrementJvmCountAndCleanup() {
        long count = jvmCount.decrementAndGet();
        log.info("JVM 카운트 감소: {}", count);
        if (count == 0) {
            sharedResource.delete();
            jvmCount.delete();
            log.info("모든 JVM 종료, 공유 자원 삭제 완료");
        }
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