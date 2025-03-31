package com.example.concurrencypractice.lock.redisson;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RFencedLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedissonFencedLockService {
    private static final String LOCK_KEY = "myFencedLock";
    private static final String RESOURCE_KEY = "myFencedLock:sharedResource";

    private final RedissonClient redissonClient;
    private final RAtomicLong sharedResource;

    public RedissonFencedLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.sharedResource = redissonClient.getAtomicLong(RESOURCE_KEY);
        log.info("✅ 공유자원 초기값: {}", sharedResource.get());
    }

    public void accessSharedResource(String clientName, String threadName) {
        String identifier = clientName + "-" + threadName;
        RFencedLock lock = redissonClient.getFencedLock(LOCK_KEY);
        Long token = null;

        try {
            log.info("[{}] 락 획득 시도 중...", identifier);
            token = lock.tryLockAndGetToken(10, 10, TimeUnit.MINUTES);
            if (token != null) {
                log.info("🔒 [{}] 락 획득 성공, 공유 자원 작업 시작 (토큰: {})", identifier, token);

                long before = sharedResource.get();
                Thread.sleep(60000); // 1분 작업 진행

                // 펜싱 토큰 확인 로직
                Long currentToken = lock.getToken();
                if (currentToken != null && currentToken.equals(token)) {
                    sharedResource.set(before + 1);
                    long after = sharedResource.get();
                    log.info("[{}] 공유 자원 값 변경: {} → {} (토큰: {})", identifier, before, after, token);
                } else {
                    log.warn("[{}] 토큰 불일치 (획득 토큰: {}, 현재 토큰: {}), 작업 취소", identifier, token, currentToken);
                }
                log.info("[{}] 작업 완료", identifier);
            } else {
                log.warn("❌ [{}] 락 획득 실패, 대기 시간 초과", identifier);
            }
        } catch (InterruptedException e) {
            log.error("[{}] 락 처리 중 인터럽트 발생", identifier, e);
            Thread.currentThread().interrupt();
        } finally {
            if (token != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("[{}] 작업 완료 후 락 해제 완료 (토큰: {})", identifier, lock.getToken());
            }
        }
    }

    public long getSharedResourceValue() {
        return sharedResource.get();
    }
}