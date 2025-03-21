package com.example.concurrencypractice.latch;

import java.util.concurrent.CountDownLatch;

public class CountDownLatchExample {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3); // 3개의 작업 대기

        for (int i = 1; i <= 3; i++) {
            final int workerId = i;
            new Thread(() -> {
                System.out.println("작업자 " + workerId + " 작업 시작");
                try {
                    Thread.sleep(1000); // 작업 시간
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("작업자 " + workerId + " 작업 완료");
                latch.countDown(); // 작업 완료 알림
            }).start();
        }

        System.out.println("메인 쓰레드: 작업자들 작업 끝날 때까지 대기...");
        latch.await(); // 3개 count가 끝날 때까지 대기
        System.out.println("모든 작업 완료!");
    }
}
