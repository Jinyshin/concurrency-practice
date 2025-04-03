package com.example.concurrencypractice.lock.redisson;

import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonSpinLock;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RFencedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedissonSpinLockService {
    private static final String LOCK_KEY = "mySpinLock";
    private static final String RESOURCE_KEY = "mySpinLock:sharedResource";

    private final RedissonClient redissonClient;
    private final RAtomicLong sharedResource;

    public RedissonSpinLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.sharedResource = redissonClient.getAtomicLong(RESOURCE_KEY);
        log.info("✅ 공유자원 초기값: {}", sharedResource.get());
    }

    public void accessSharedResource(String clientName, String threadName) {
        String identifier = clientName + "-" + threadName;
        RLock lock = redissonClient.getSpinLock(LOCK_KEY);
        boolean acquired;

        try {
            log.info("[{}] 락 획득 시도 중...", identifier);
            acquired = lock.tryLock(10, 10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error("[{}] 락 처리 중 인터럽트 발생", identifier, e);
            Thread.currentThread().interrupt();
            return;
        }

        if (acquired) {
            try {
                log.info("🔒 [{}] 락 획득 성공, 공유 자원 작업 시작", identifier);

                // 공유 자원 작업 진행
                long before = sharedResource.get();
                Thread.sleep(30000 * 1);
                // 이 케이스는 클라이언트단에서 락의 소유권을 확인을 한번 해주고 리소스에 접근하면 된다.
                if (lock.isHeldByCurrentThread()) {
                    sharedResource.set(before + 1);
                }
                long after = sharedResource.get();

                log.info("[{}] 공유 자원 값 변경: {} → {}", identifier, before, after);
                log.info("[{}] 작업 완료", identifier);
            } catch (InterruptedException e) {
                log.error("[{}] 락 처리 중 인터럽트 발생", identifier, e);
                Thread.currentThread().interrupt();
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.info("[{}] 락 해제 완료", identifier);
                }
            }
        } else {
            log.warn("❌ [{}] 락 획득 실패, 대기 시간 초과", identifier);
        }
    }

    public long getSharedResourceValue() {
        return sharedResource.get();
    }
}