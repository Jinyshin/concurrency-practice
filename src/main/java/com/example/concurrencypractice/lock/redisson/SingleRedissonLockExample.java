package com.example.concurrencypractice.lock.redisson;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

public class SingleRedissonLockExample {
    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        RedissonClient redisson = Redisson.create(config);
        // 이 부분에서 Config 객체를 넘긴 후에 RedissonClient 가 생성되는구나! 저 string address 도 이 과정에서 파싱됨

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
