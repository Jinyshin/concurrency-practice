package com.example.concurrencypractice.redisson;

import org.redisson.Redisson;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class ClusterRedissonLockExample {
    public static void main(String[] args) {
        // 네임스페이스 설정
        String NAMESPACE = "jiny";

        // Redis 클러스터 설정
        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress("redis://ncp-4c4-000:6379", "redis://ncp-4c4-001:6379", "redis://ncp-4c4-002:6379");

        RedissonClient redisson = Redisson.create(config);

        // 네임스페이스 적용한 락 키
        String lockKey = NAMESPACE + ":lock:jinyLock";
        RLock lock = redisson.getLock(lockKey);

        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                // 락 획득 (5초 대기, 10초 유지)
                System.out.println("Lock acquired: " + lockKey);

                // 모든 키 조회
                RKeys keys = redisson.getKeys();
                Iterable<String> allKeys = keys.getKeys();
                Iterator<String> iterator = allKeys.iterator();

                System.out.println("==== Redis Keys ====");
                while (iterator.hasNext()) {
                    System.out.println(iterator.next());
                }

                // 락을 획득한 후 실행할 작업
                Thread.sleep(8000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock(); // 락 해제
            System.out.println("Lock released!");
        }
        redisson.shutdown();
    }
}
