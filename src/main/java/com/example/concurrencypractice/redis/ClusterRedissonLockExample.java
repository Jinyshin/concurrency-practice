package com.example.concurrencypractice.redis;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

public class ClusterRedissonLockExample {
    public static void main(String[] args) {
        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress("redis://ncp-4c4-000:6379", "redis://ncp-4c4-001:6379", "redis://ncp-4c4-002:6379");

        RedissonClient redisson = Redisson.create(config);

        RLock lock = redisson.getLock("myLock");
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                // 락 획득 (5초 대기, 10초 유지)
                System.out.println("Lock acquired!");

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
